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
            EventPublisher.publishExplanationEvent(solver, EventType.NONMINIMAL_PATH, explanation);
            return true;
        }

        if (nodeProcessor.shouldPruneBranch(explanation)){
            return true;
        }
        return false;
    }

    @Override
    public TreeNode createRoot(){
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
        node.model = modelToReuse;
        node.path = path.getAxioms();
        node.depth = depth;

        node.incomingLabels.put(parent, path.lastAxiom);
        nodesAtCurrentDepth.put(path.getAxiomSet(),node);

        parent.children.add(node);

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

        pruneTree();

        iteratedChildren = new ArrayList<>(currentNode.model.getNegatedData());
        return currentNode == root || currentNode.hasParent();
    }

    public boolean noChildrenLeft(){
        return iteratedChildren.isEmpty();
    }

    public OWLAxiom getNextChild(){
        return iteratedChildren.remove(0);
    }

    @Override
    public void resetLevel() {
        nodesAtCurrentDepth.clear();
        currentLevelReusedModels ++;
    }
    // check if nodes have same path and merge them
    public boolean mergeIfPossible(Explanation label) {

        Set<OWLAxiom> candidateSet = label.getAxiomSet();

        if (nodesAtCurrentDepth.containsKey(candidateSet)) {
            HsdagNode child = nodesAtCurrentDepth.get(candidateSet);
            currentNode.children.add(child);
            child.addParent(currentNode, label.lastAxiom);

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

        Queue<HsdagNode> nodes = new ArrayDeque<>();
        nodes.add(root);

        Set<HsdagNode> relabeledNodes = new HashSet<>();

        while (!nodes.isEmpty()){

            HsdagNode polledNode = nodes.poll();

            if (polledNode == currentNode){
                continue;
            }

            // avoid redundant processing of shared nodes during a single pruneTree update
            if (relabeledNodes.contains(polledNode)){
                continue;
            }

            relabeledNodes.add(polledNode);

            // nodes n' labeled with some Cj from CS such that Ci C Cj
            if (currentNode.isProperSubsetOf(polledNode)){
                StaticPrinter.debugPrint("[HS-DAG] " + currentNode + " is subset of " + polledNode);

                Model Ci = currentNode.model;
                Model Cj = polledNode.model;

                // Cj\Ci
                Set<OWLAxiom> difference = new HashSet<>(Cj.getNegatedData());
                difference.removeAll(Ci.getNegatedData());

                // Relabel n' with Ci
                StaticPrinter.debugPrint("[HS-DAG] Relabelling " + polledNode + " with " + Ci);
                polledNode.model = Ci;

                // for any ci in Cj\Ci, the edge labeled ci originating from n' is no longer allowed
                deleteNodeDescendants(polledNode, difference);
            }

                nodes.addAll(polledNode.children);
        }
    }

    private void deleteNodeDescendants(HsdagNode polledNode, Set<OWLAxiom> difference) {
        List<HsdagNode> children = new ArrayList<>(polledNode.children);

        for (HsdagNode child : children) {

            OWLAxiom edgeLabel = child.getLabelFrom(polledNode);

            if (difference.contains(edgeLabel)){
                deleteNode(polledNode, child);
            }
        }
    }

    private void deleteNode(HsdagNode parent, HsdagNode child){

        parent.children.remove(child);
        child.removeParent(parent);

        Queue<HsdagNode> deletionQueue = new ArrayDeque<>();
        deletionQueue.add(child);

        while (!deletionQueue.isEmpty()){

            HsdagNode polledNode = deletionQueue.poll();

            // if polledNode has parent, do not delete it
            if (polledNode == root || polledNode.hasParent()){
                continue;
            }

            // else, delete node from structure
            queue.remove(polledNode);
            removeFromNodesAtCurrentDepth(polledNode);

            for (HsdagNode polledNodeChild : new ArrayList<>(polledNode.children)) {
                // deleted node is no longer parent of its children
                polledNodeChild.removeParent(polledNode);

                // if its child does not have other parents, it should be deleted
                if (!polledNodeChild.hasParent()) {
                    deletionQueue.add(polledNodeChild);
                }
            }

            polledNode.children.clear();

            if (polledNode.processed)
                EventPublisher.publishNodeEvent(solver, EventType.DELETED_PROCESSED_NODE, polledNode);
            else {
                EventPublisher.publishNodeEvent(solver, EventType.DELETED_UNPROCESSED_NODE, polledNode);
            }
        }

    }

    private void removeFromNodesAtCurrentDepth(HsdagNode child) {
        if (nodesAtCurrentDepth.containsValue(child)) {
            Set<OWLAxiom> pathAxioms = new HashSet<>(child.path);
            nodesAtCurrentDepth.remove(pathAxioms, child);
        }
    }

}
