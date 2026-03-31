package sk.uniba.fmph.dai.cats.algorithms.hsdag;

import org.semanticweb.owlapi.model.OWLAxiom;
import sk.uniba.fmph.dai.cats.algorithms.*;
import sk.uniba.fmph.dai.cats.common.StaticPrinter;
import sk.uniba.fmph.dai.cats.data.Explanation;
import sk.uniba.fmph.dai.cats.data_processing.ExplanationManager;
import sk.uniba.fmph.dai.cats.events.EventPublisher;
import sk.uniba.fmph.dai.cats.events.EventType;
import sk.uniba.fmph.dai.cats.model.Model;
import sk.uniba.fmph.dai.cats.model.ModelData;
import sk.uniba.fmph.dai.cats.reasoner.Loader;

import java.util.*;

public class HsdagBuilder implements ITreeBuilder {
    final AlgorithmSolver solver;
    final Loader loader;
    final INodeProcessor nodeProcessor;
    final Queue<HsdagNode> queue = new PriorityQueue<>();
    public int idToAssign = 0;


    HsdagNode currentNode;
    HashMap<Set<OWLAxiom>, HsdagNode> nodesAtCurrentDepth = new HashMap<>();
    HsdagNode root;

    int currentLevelReusedModels = 0;


    //List<OWLAxiom> iteratedChildren;

    public HsdagBuilder(AlgorithmSolver solver){
        this.solver = solver;
        this.loader = solver.loader;
        this.nodeProcessor = solver.nodeProcessor;
    }

    @Override
    public IAbducibleAxioms createAbducibles(TransformedAbducibles abducibles) {
        return new AxiomSetAbducibles(abducibles);
    }

    @Override
    public boolean shouldPruneChildBranch(TreeNode node, Explanation explanation) {

        if(mergeable(explanation)){
            return true;
        }

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
        root.childrenToProcess.addAll(root.model.getNegatedData());
        return root;
    }

    @Override
    public TreeNode createChildNode(TreeNode parent, Explanation label){

        return createNode(label, parent.depth + 1, (HsdagNode) parent);
    }

    private TreeNode createNode(Explanation label, Integer depth, HsdagNode parent){

        HsdagNode node = new HsdagNode(getAndIncreaseId());

        if (label != null) {
            node.path = label.getAxioms();
            node.labelAxiom = label.lastAxiom;
            nodesAtCurrentDepth.put(label.getAxiomSet(),node);
        }
        node.depth = depth;

        Model modelToReuse = solver.findAndGetModelToReuse();

        if (modelToReuse == null)
            return null;
        node.model = solver.removePathAxiomsFromModel(modelToReuse);

        node.parent = parent;

        node.childrenToProcess.addAll(node.model.getNegatedData());

//        if (solver.currentLevel.reusedModels > currentLevelReusedModels) {
//            node.modelWasNotReused = false;
//            currentLevelReusedModels = solver.currentLevel.reusedModels;
//        } else {
//            node.modelWasNotReused = true;
//        }

        if(parent != null) {
            parent.children.add(node);
        }

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
//        if(currentNode.modelWasNotReused) {
//          relabel();
//          node.assignedLevel.relabeledCalls ++;
//       }
        relabel();
        node.assignedLevel.relabeledCalls ++;
        return currentNode.HaveParent() && !currentNode.childrenToProcess.isEmpty();
    }

    public boolean noChildrenLeft(){
        return currentNode.childrenToProcess.isEmpty();
    }

    public OWLAxiom getNextChild(){
        OWLAxiom child = currentNode.childrenToProcess.get(0);
        currentNode.childrenToProcess.remove(0);
        return child;
    }

    @Override
    public void resetLevel() {
        nodesAtCurrentDepth.clear();
        currentLevelReusedModels ++;
    }
    // check if node have same path and merge them
    public boolean mergeable(Explanation label) {

        Set<OWLAxiom> candidateSet = label.getAxiomSet();

        if (nodesAtCurrentDepth.containsKey(candidateSet)) {
            HsdagNode child = nodesAtCurrentDepth.get(candidateSet);
            currentNode.children.add(child);
            child.referenceCount ++;
            currentNode.assignedLevel.mergedNodes ++;

            StaticPrinter.debugPrint("[MERGING] Path already exists in history.");

            return true;
        }
        return false;
    }

    private int getAndIncreaseId(){
        int oldIndex = idToAssign;
        idToAssign++;
        return oldIndex;
    }

    private void relabel(){
        Queue<HsdagNode> localQueue = new ArrayDeque<>();
        localQueue.add(root);

        List<HsdagNode> relabeledNodes = new ArrayList<>();

        while (!localQueue.isEmpty()){
            HsdagNode polledNode = localQueue.poll();

            if (polledNode == currentNode){
                continue;
            }
            // avoid redundant processing of shared nodes during a single relabeling update
            if(polledNode.currentlyRelabeled){
                continue;
            }

            polledNode.currentlyRelabeled = true;
            relabeledNodes.add(polledNode);

            // nodes n' labeled with some Cj from CS such that Ci C Cj
            if(currentNode.isSubsetOf(polledNode)){
                StaticPrinter.debugPrint("[HS-DAG] " + currentNode + " is subset of " + polledNode);

                Model Ci = currentNode.model;
                Model Cj = polledNode.model;

                // Cj\Ci
                Set<OWLAxiom> difference = new HashSet<>(Cj.getNegatedData());
                difference.removeAll(Ci.getNegatedData());
                StaticPrinter.debugPrint("[HS-DAG] Relabelling " + polledNode + " with " + Ci);

                // Relabel n' with Ci
                polledNode.model = Ci;

                polledNode.childrenToProcess.removeAll(difference);

                List<HsdagNode> children = new ArrayList<>(polledNode.children);

                for (HsdagNode child : children) {
                    if(difference.contains(child.labelAxiom)){
                        deleteNode(polledNode, child);
                    }

                }
            }
                localQueue.addAll(polledNode.children);
        }
        resetCurrentlyRelabeled(relabeledNodes);
    }
    private void deleteNode(HsdagNode parent, HsdagNode child){

        parent.children.remove(child);

        Queue<HsdagNode> localQueue = new ArrayDeque<>();
        localQueue.add(child);

        while (!localQueue.isEmpty()){

            HsdagNode polledNode = localQueue.poll();
            polledNode.referenceCount --;

            // decrease referenceCount if  pollednode has more than 1 parent
            if(polledNode.HaveParent()){
                continue;
            }

            // delete node from structure
            queue.remove(polledNode);
            localQueue.addAll(polledNode.children);
            polledNode.children.clear();


            StaticPrinter.debugPrint("[HS-DAG] Deleting node: " + polledNode);

            if (polledNode.processed)
                polledNode.assignedLevel.deletedProcessed += 1;
            else {
                polledNode.parent.assignedLevel.deletedCreated += 1;
            }
        }

    }
    // after relabel reset all nodes for next relabeling
    private void resetCurrentlyRelabeled(List<HsdagNode> relabeledNodes){
        for (HsdagNode node : relabeledNodes){
            node.currentlyRelabeled = false;
        }
    }

}
