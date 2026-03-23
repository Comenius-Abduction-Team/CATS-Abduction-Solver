package sk.uniba.fmph.dai.cats.algorithms.hsdag;

import org.semanticweb.owlapi.model.ClassExpressionType;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
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

        ModelData axioms = root.model.getNegatedData();
        root.childrenToProcess.addAll(axioms);
        root.positiveAxioms.addAll(filterNegations(axioms));


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
        }
        node.depth = depth;

        Model modelToReuse = solver.findAndGetModelToReuse();

        if (modelToReuse == null)
            return null;

        node.model = solver.removePathAxiomsFromModel(modelToReuse);
        node.labelAxiom = label.lastAxiom;
        node.parent = parent;

        ModelData axioms =  node.model.getNegatedData();

        node.childrenToProcess.addAll(axioms);
        node.positiveAxioms.addAll(filterNegations(axioms));

//        if (solver.currentLevel.reusedModels > currentLevelReusedModels) {
//            node.modelWasNotReused = false;
//            currentLevelReusedModels = solver.currentLevel.reusedModels;
//        } else {
//            node.modelWasNotReused = true;
//        }

        if(parent != null) {
            parent.children.add(node);
        }
        nodesAtCurrentDepth.put(label.getAxiomSet(),node);

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
//        }
        relabel();
        if (!currentNode.HaveParent() || currentNode.childrenToProcess.isEmpty()) return false;
        //iteratedChildren = new ArrayList<>(parentNode.model.getNegatedData());
        return true;
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

            if(polledNode.currentlyRelabeled){
                continue;
            }

            polledNode.currentlyRelabeled = true;
            relabeledNodes.add(polledNode);

            if(currentNode.isSubsetOf(polledNode)){
                StaticPrinter.debugPrint("[HS-DAG] " + currentNode + " is subset of " + polledNode);

                Model Ci = currentNode.model;

                Set<OWLAxiom> difference = new HashSet<>(polledNode.positiveAxioms);
                difference.removeAll(currentNode.positiveAxioms);

                StaticPrinter.debugPrint("[HS-DAG] Relabelling " + polledNode + " with " + Ci);
                polledNode.model = Ci;
                polledNode.positiveAxioms = currentNode.positiveAxioms;

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

            if(polledNode.HaveParent()){
                continue;
            }

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

    private void resetCurrentlyRelabeled(List<HsdagNode> relabeledNodes){
        for (HsdagNode node : relabeledNodes){
            node.currentlyRelabeled = false;
        }
    }

    private Set<OWLAxiom> filterNegations(Collection<OWLAxiom> axioms) {
        Set<OWLAxiom> filtered = new HashSet<>();

        for (OWLAxiom ax : axioms) {
            if (ax instanceof OWLClassAssertionAxiom) {
                OWLClassAssertionAxiom classAssertion = (OWLClassAssertionAxiom) ax;

                if (classAssertion.getClassExpression().getClassExpressionType() != ClassExpressionType.OBJECT_COMPLEMENT_OF) {
                    filtered.add(ax);
                }
            }
        }
        return filtered;
    }

}
