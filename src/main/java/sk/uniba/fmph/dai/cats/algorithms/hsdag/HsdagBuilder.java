package sk.uniba.fmph.dai.cats.algorithms.hsdag;

import org.semanticweb.owlapi.model.OWLAxiom;
import sk.uniba.fmph.dai.cats.algorithms.*;
import sk.uniba.fmph.dai.cats.common.StaticPrinter;
import sk.uniba.fmph.dai.cats.data.Explanation;
import sk.uniba.fmph.dai.cats.data_processing.ExplanationManager;
import sk.uniba.fmph.dai.cats.events.EventPublisher;
import sk.uniba.fmph.dai.cats.events.EventType;
import sk.uniba.fmph.dai.cats.model.Model;

import java.util.*;

public class HsdagBuilder implements ITreeBuilder {
    final AlgorithmSolver solver;
    final INodeProcessor nodeProcessor;
    final Queue<HsdagNode> queue = new PriorityQueue<>();
    public int idToAssign = 0;


    HsdagNode currentNode;
    Map<Set<OWLAxiom>, HsdagNode> nodesAtCurrentDepth = new HashMap<>();
    HsdagNode root;

    int currentLevelReusedModels = 0;

    List<OWLAxiom> iteratedChildren;

    public HsdagBuilder(AlgorithmSolver solver){
        this.solver = solver;
        this.nodeProcessor = solver.nodeProcessor;
    }

    @Override
    public IAbducibleAxioms createAbducibles(TransformedAbducibles abducibles) {
        return new AxiomSetAbducibles(abducibles);
    }

    @Override
    public boolean shouldPruneChildBranch(TreeNode node, Explanation explanation) {

        if (mergeIfPossible(explanation))
            return true;

        RuleChecker ruleChecker = solver.ruleChecker;
        ExplanationManager explanationManager = solver.explanationManager;

        if (!ruleChecker.isMinimal(explanationManager.getPossibleExplanations(), explanation)){
            StaticPrinter.debugPrint("[PRUNING] NON-MINIMAL EXPLANATION!");
            EventPublisher.publishNodeEvent(solver, EventType.EDGE_PRUNED, node);
            return true;
        }

        if (nodeProcessor.shouldPruneBranch(explanation)){
            EventPublisher.publishNodeEvent(solver, EventType.EDGE_PRUNED, node);
            return true;
        }
        return false;
    }

    @Override
    public TreeNode createRoot(){

        if (!nodeProcessor.canCreateRoot(true))
            return null;

        Model modelToReuse = solver.findAndGetModelToReuse();

        if (modelToReuse == null)
            return null;

        root = new HsdagNode(getAndIncreaseId());

        root.model = modelToReuse;
        return root;
    }

    @Override
    public TreeNode createChildNode(TreeNode parent, Explanation label){

        return createNode(label, parent.depth + 1, (HsdagNode) parent);
    }

    private TreeNode createNode(Explanation path, Integer depth, HsdagNode parent){

        Model modelToReuse = solver.findAndGetModelToReuse();

        if (modelToReuse == null)
            return null;

        HsdagNode node = new HsdagNode(getAndIncreaseId());


        node.path = path.getAxioms();
        node.labelAxiom = path.lastAxiom;
        nodesAtCurrentDepth.put(path.getAxiomSet(),node);
        node.depth = depth;
        node.model = solver.removePathAxiomsFromModel(modelToReuse);
        node.parent = parent;

        parent.children.add(node);


//        if (solver.currentLevel.reusedModels > currentLevelReusedModels) {
//            node.modelWasNotReused = false;
//            currentLevelReusedModels = solver.currentLevel.reusedModels;
//        } else {
//            node.modelWasNotReused = true;
//        }


        return node;
    }

    @Override
    public boolean shouldExtractModel() {
        return true;
    }

    @Override
    public void addNodeToTree(TreeNode node) {
        queue.add((HsdagNode) node);
    }

    @Override
    public boolean isTreeClosed() {
        return queue.isEmpty();
    }

    @Override
    public TreeNode getNextNodeFromTree() {
        return queue.poll();
    }


    public boolean startIteratingNodeChildren(TreeNode node){
        currentNode = (HsdagNode) node;
        iteratedChildren = new ArrayList<>(currentNode.model.getNegatedData());
//        if(currentNode.modelWasNotReused) {
//          relabel();
//          node.assignedLevel.relabeledCalls ++;
//       }
        pruneTree();
        return currentNode.hasParent();
    }

    public boolean noChildrenLeft(){
        return iteratedChildren.isEmpty();
    }

    public OWLAxiom getNextChild(){
        OWLAxiom child = iteratedChildren.get(0);
       iteratedChildren.remove(0);
        return child;
    }

    @Override
    public void resetLevel() {
        nodesAtCurrentDepth.clear();
        currentLevelReusedModels ++;
    }
    // check if node have same path and merge them
    public boolean mergeIfPossible(Explanation label) {

        Set<OWLAxiom> candidateSet = label.getAxiomSet();

        if (nodesAtCurrentDepth.containsKey(candidateSet)) {
            HsdagNode child = nodesAtCurrentDepth.get(candidateSet);
            currentNode.children.add(child);
            child.referenceCount ++;

            EventPublisher.publishNodeEvent(solver, EventType.MERGING_NODE, currentNode);

            return true;
        }
        return false;
    }

    private int getAndIncreaseId(){
        int oldIndex = idToAssign;
        idToAssign++;
        return oldIndex;
    }

    private void pruneTree(){
        Queue<HsdagNode> localQueue = new ArrayDeque<>();
        localQueue.add(root);

        Set<HsdagNode> relabeledNodes = new HashSet<>();

        while (!localQueue.isEmpty()){
            HsdagNode polledNode = localQueue.poll();

            if (polledNode == currentNode){
                continue;
            }

            // avoid redundant processing of shared nodes during a single pruneTree update
            if (relabeledNodes.contains(polledNode)){
                continue;
            }

            relabeledNodes.add(polledNode);

            // nodes n' labeled with some Cj from CS such that Ci C Cj
            if (currentNode.isSubsetOf(polledNode)){
                StaticPrinter.debugPrint("[HS-DAG] " + currentNode + " is subset of " + polledNode);

                Model Ci = currentNode.model;
                Model Cj = polledNode.model;

                // Cj\Ci
                Set<OWLAxiom> difference = new HashSet<>(Cj.getNegatedData());
                difference.removeAll(Ci.getNegatedData());
                StaticPrinter.debugPrint("[HS-DAG] Relabelling " + polledNode + " with " + Ci);

                // Relabel n' with Ci
                polledNode.model = Ci;

                List<HsdagNode> children = new ArrayList<>(polledNode.children);

                for (HsdagNode child : children) {
                    if (difference.contains(child.labelAxiom)){
                        deleteNode(polledNode, child);
                    }

                }
            }

                localQueue.addAll(polledNode.children);
        }
    }
    private void deleteNode(HsdagNode parent, HsdagNode child){

        parent.children.remove(child);

        Queue<HsdagNode> localQueue = new ArrayDeque<>();
        localQueue.add(child);

        while (!localQueue.isEmpty()){

            HsdagNode polledNode = localQueue.poll();
            polledNode.referenceCount --;

            // decrease referenceCount if  polledNode has more than 1 parent
            if(polledNode.hasParent()){
                continue;
            }

            // delete node from structure
            queue.remove(polledNode);
            localQueue.addAll(polledNode.children);
            polledNode.children.clear();

            if (polledNode.processed)
                EventPublisher.publishNodeEvent(solver, EventType.DELETED_PROCESSED_NODE, polledNode);
            else {
                EventPublisher.publishNodeEvent(solver, EventType.DELETED_UNPROCESSED_NODE, polledNode);
            }
        }

    }

}
