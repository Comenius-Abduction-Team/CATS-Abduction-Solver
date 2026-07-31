package sk.uniba.fmph.dai.cats.algorithms.rctree;

import org.semanticweb.owlapi.model.OWLAxiom;
import sk.uniba.fmph.dai.cats.algorithms.*;
import sk.uniba.fmph.dai.cats.common.StaticPrinter;
import sk.uniba.fmph.dai.cats.common.StringFactory;
import sk.uniba.fmph.dai.cats.data.Explanation;
import sk.uniba.fmph.dai.cats.data_processing.ExplanationManager;
import sk.uniba.fmph.dai.cats.events.EventPublisher;
import sk.uniba.fmph.dai.cats.events.EventType;
import sk.uniba.fmph.dai.cats.metrics.TreeStats;
import sk.uniba.fmph.dai.cats.model.Model;

import java.util.*;

public class RctTreeBuilder implements ITreeBuilder {

    final AlgorithmSolver solver;
    final INodeProcessor nodeProcessor;
    final TreeStats stats;

    final Queue<RctNode> queue  = new PriorityQueue<>();

    public int idToAssign = 0;

    RctNode root;

    RctNode currentNode;

    public RctTreeBuilder(AlgorithmSolver solver){
        this.solver = solver;
        this.nodeProcessor = solver.nodeProcessor;
        this.stats = solver.stats;
    }

    @Override
    public IAbducibleAxioms createAbducibles(TransformedAbducibles abducibles) {
        return new AxiomSetAbducibles(abducibles);
    }

    @Override
    public boolean shouldPruneChildBranch(TreeNode originalNode, Explanation explanation){

        RuleChecker ruleChecker = solver.ruleChecker;
        ExplanationManager explanationManager = solver.explanationManager;

        if (!ruleChecker.isMinimal(explanationManager.getPossibleExplanations(), explanation)){
            EventPublisher.publishNodeEvent(solver, EventType.EDGE_PRUNED, originalNode);
            StaticPrinter.debugPrint("[PRUNING] NON-MINIMAL EXPLANATION!");
            return true;
        }

        if (nodeProcessor.shouldPruneBranch(explanation)){
            EventPublisher.publishNodeEvent(solver, EventType.EDGE_PRUNED, originalNode);
            return true;
        }
        return false;
    }

    private int getAndIncreaseId(){
        int oldIndex = idToAssign;
        idToAssign++;
        return oldIndex;
    }

    @Override
    public TreeNode createRoot() {

        if (!nodeProcessor.canCreateRoot(true))
            return null;

        Model modelToReuse = solver.findAndGetModelToReuse();

        if (modelToReuse == null)
            return null;

        root = new RctNode(getAndIncreaseId());
        root.model = modelToReuse;

        root.childrenToProcess.addAll(root.model.getNegatedData());

        return root;
    }

    @Override
    public TreeNode createChildNode(TreeNode parent, Explanation label) {
        return createNode(label, parent.depth + 1, (RctNode) parent);
    }

    private RctNode createNode(Explanation path, int depth, RctNode parent){

        Model modelToReuse = solver.findAndGetModelToReuse();

        if (modelToReuse == null)
            return null;

        RctNode node = new RctNode(getAndIncreaseId());
        node.model = modelToReuse;
        node.path = path.getAxioms();
        node.depth = depth;

        OWLAxiom label = path.lastAxiom;
        node.labelAxiom = label;
        parent.usedLabels.add(label);

        parent.children.add(node);
        node.parent = parent;

        node.childrenToIgnore.addAll(parent.childrenToIgnore.getAxioms());
        node.childrenToIgnore.addAll(parent.usedLabels);

        for (OWLAxiom axiom : node.model.getNegatedData()){
            if (!node.childrenToIgnore.contains(axiom))
                node.childrenToProcess.add(axiom);
        }

        StaticPrinter.debugPrint("[RCT] Created node " + node.id + ". Ignored children: " + node.childrenToIgnore);

        return node;
    }

    @Override
    public boolean shouldExtractModel() {
        return true;
    }

    @Override
    public void addNodeToTree(TreeNode node) {
        queue.add((RctNode) node);
    }

    @Override
    public boolean isTreeClosed() {
        return queue.isEmpty();
    }

    @Override
    public TreeNode getNextNodeFromTree() {
        return queue.poll();
    }

    @Override
    public boolean startIteratingNodeChildren(TreeNode node){
        currentNode = (RctNode) node;

        pruneTree();

        return !currentNode.closed && !currentNode.childrenToProcess.isEmpty();
    }

    private void pruneTree(){

        Queue<RctNode> nodes = new ArrayDeque<>();
        nodes.add(root);

        while (!nodes.isEmpty()){

            RctNode polledNode = nodes.poll();

            if (polledNode == currentNode)
                continue;

            // nodes n' labeled with some Cj from CS such that Ci C Cj
            if (currentNode.isProperSubsetOf(polledNode)){

                StaticPrinter.debugPrint("[RCT] " + currentNode + " is subset of " + polledNode);

                Model Ci = currentNode.model;
                Model Cj = polledNode.model;

                // Cj\Ci
                Set<OWLAxiom> difference = new HashSet<>(Cj.getNegatedData());
                difference.removeAll(Ci.getNegatedData());

                // Relabel n' with Ci
                StaticPrinter.debugPrint("[RCT] Relabelling " + polledNode + " with " + Ci);
                polledNode.model = Ci;

                // for any ci in Cj\Ci, the edge labeled ci originating from n' is no longer allowed
                // the node connected by this edge and all of its descendants are removed
                deleteNodeDescendants(polledNode, difference);
                polledNode.childrenToProcess.removeAll(difference);

                // update ΘCs
                traverseTreeToUpdateIgnoredChildren(polledNode, difference);

            }

            nodes.addAll(polledNode.children);
        }
    }

    private void deleteNodeDescendants(RctNode polledNode, Set<OWLAxiom> difference) {
        List<RctNode> children = new ArrayList<>(polledNode.children);

        for (RctNode child : children){
            if (difference.contains(child.labelAxiom)){
                deleteNode(child, polledNode);
            }
        }
    }

    void deleteNode(RctNode child, RctNode parent){
        parent.children.remove(child);

        Queue<RctNode> deletionQueue = new ArrayDeque<>();
        deletionQueue.add(child);

        while (!deletionQueue.isEmpty()){
            RctNode node = deletionQueue.poll();

            if (node.closed)
                continue;

            queue.remove(node);
            node.closeNode();
            deletionQueue.addAll(node.children);
            node.children.clear();
            node.childrenToProcess.clear();

            StaticPrinter.debugPrint("[RCT] Deleting node: " + node);

            if (node.processed)
                node.assignedLevel.deletedProcessed += 1;
            else {
                node.parent.assignedLevel.deletedCreated += 1;
            }

        }

    }

    private void traverseTreeToUpdateIgnoredChildren(RctNode originalPolledNode, Set<OWLAxiom> difference){
        // update ΘC(n') to ΘC(n')\(Cj\Ci)
        originalPolledNode.childrenToIgnore.removeAll(difference);

        Queue<RctNode> nodes = new ArrayDeque<>();
        nodes.add(originalPolledNode);

        while(!nodes.isEmpty()){

            RctNode polledNode = nodes.poll();
            polledNode.usedLabels.removeAll(difference);

            // for all children n'' of n' update ΘC(n'') to ΘC(n'')\(Cj\Ci)
            for (RctNode child : polledNode.children) {

                updateIgnoredChildren(child, difference);

                // for all descendants n''' of some n'' propagate the update accordingly
                nodes.add(child);
            }
        }
    }

    void updateIgnoredChildren(RctNode node, Set<OWLAxiom> difference){
        Set<OWLAxiom> removedIgnored = node.childrenToIgnore.removeAllAndReturn(difference);

        if (!removedIgnored.isEmpty()){

            // create for all n'' and n''' all the edges that are not avoided anymore
            // (due to the updates to their Θs), and process the new nodes in a breadth-first order
            boolean added = node.childrenToProcess.addAll(removedIgnored);
            if (added)
                StaticPrinter.debugPrint("[RCT] Added " + StringFactory.getRepresentation(removedIgnored) + " to "
                        + node + "'s children to be processed.");
            node.closed = false;
            if (!queue.contains(node) && !node.childrenToProcess.isEmpty()) {
                StaticPrinter.debugPrint("[RCT] Added " + node + " to the queue.");
                queue.add(node);
            }
        }

    }

    @Override
    public boolean noChildrenLeft(){
        return currentNode.childrenToProcess.isEmpty();
    }

    @Override
    public OWLAxiom getNextChild(){
        return currentNode.childrenToProcess.remove(0);
    }

}
