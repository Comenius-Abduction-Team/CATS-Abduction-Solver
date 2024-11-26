package sk.uniba.fmph.dai.cats.algorithms.rctree;

import org.semanticweb.owlapi.model.OWLAxiom;
import sk.uniba.fmph.dai.cats.algorithms.hybrid.*;
import sk.uniba.fmph.dai.cats.common.Configuration;
import sk.uniba.fmph.dai.cats.common.StringFactory;
import sk.uniba.fmph.dai.cats.data.AxiomSet;
import sk.uniba.fmph.dai.cats.data.Explanation;
import sk.uniba.fmph.dai.cats.data_processing.ExplanationManager;
import sk.uniba.fmph.dai.cats.data_processing.TreeStats;
import sk.uniba.fmph.dai.cats.model.Model;

import java.util.*;

public class RcTreeBuilder implements TreeBuilder {

    final AlgorithmSolver solver;
    final NodeProcessor nodeProcessor;
    final TreeStats stats;

    final Queue<RcTreeNode> queue  = new PriorityQueue<>();

    int index = 0;

    RcTreeNode root;

    RcTreeNode currentNode;

    public RcTreeBuilder(AlgorithmSolver solver){
        this.solver = solver;
        this.nodeProcessor = solver.nodeProcessor;
        this.stats = solver.stats;
    }

    @Override
    public IAbducibleAxioms createAbducibles(TransformedAbducibles abducibles) {
        return new AxiomSet(abducibles.getAbducibleAxioms());
    }

    @Override
    public boolean pruneNode(TreeNode originalNode, Explanation explanation){

        RuleChecker ruleChecker = solver.ruleChecker;
        ExplanationManager explanationManager = solver.explanationManager;

        if (!ruleChecker.isMinimal(explanationManager.getPossibleExplanations(), explanation)){
            if (Configuration.DEBUG_PRINT)
                System.out.println("[PRUNING] NON-MINIMAL EXPLANATION!");
            return true;
        }

        if (nodeProcessor.isInvalidExplanation(explanation)){
            return true;
        }

        return false;
    }

    private int getAndIncreaseIndex(){
        int oldIndex = index;
        index++;
        return oldIndex;
    }

    @Override
    public TreeNode createRoot() {

        if (!nodeProcessor.canCreateRoot())
            return null;

        Model modelToReuse = solver.findAndGetModelToReuse();

        if (modelToReuse == null)
            return null;

        root = new RcTreeNode(getAndIncreaseIndex());
        root.model = modelToReuse;

        root.childrenToProcess.addAll(root.model.getNegatedData());

        return root;
    }

    @Override
    public TreeNode createChildNode(TreeNode parent, Explanation label) {
        return createNode(label, parent.depth + 1, (RcTreeNode) parent);
    }

    private RcTreeNode createNode(Explanation path, Integer depth, RcTreeNode parent){

        Model modelToReuse = solver.findAndGetModelToReuse();

        if (modelToReuse == null)
            return null;

        RcTreeNode node = new RcTreeNode(getAndIncreaseIndex());
        node.model = solver.removePathAxiomsFromModel(modelToReuse);
        node.path = path.getAxioms();
        node.depth = depth;

        OWLAxiom label = path.lastAxiom;
        parent.usedLabels.add(label);
        node.labelAxiom = label;

        parent.children.add(node);
        node.parent = parent;

        node.childrenToIgnore.add(label);
        node.childrenToIgnore.addAll(parent.childrenToIgnore.getAxioms());
        node.childrenToIgnore.addAll(parent.usedLabels);

        for (OWLAxiom axiom : node.model.getNegatedData()){
            if (!node.childrenToIgnore.contains(axiom))
                node.childrenToProcess.add(axiom);
        }

        return node;
    }

    @Override
    public boolean closeExplanation(Explanation explanation) {
        return nodeProcessor.findExplanations(explanation, true);
    }

    @Override
    public void addNodeToTree(TreeNode node) {
        queue.add((RcTreeNode) node);
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
        currentNode = (RcTreeNode) node;
        pruneTree();
        if (currentNode.closed || currentNode.childrenToProcess.isEmpty())
            return false;
        return true;
    }

    private void pruneTree(){

        Queue<RcTreeNode> nodes = new ArrayDeque<>();
        nodes.add(root);

        while (!nodes.isEmpty()){

            RcTreeNode polledNode = nodes.poll();

            if (polledNode == currentNode)
                continue;

            // nodes n' labeled with some Cj from CS such that Ci C Cj
            if (currentNode.isSubsetOf(polledNode)){

                if (Configuration.DEBUG_PRINT)
                    System.out.println("[RC-TREE] " + currentNode + " is subset of " + polledNode);

                Model Ci = currentNode.model;
                Model Cj = polledNode.model;

                // Cj\Ci
                Set<OWLAxiom> difference = new HashSet<>(Cj.getNegatedData());
                difference.removeAll(Ci.getNegatedData());

                // Relabel n' with Ci
                if (Configuration.DEBUG_PRINT)
                    System.out.println("[RC-TREE] Relabelling " + polledNode + " with " + Ci);
                polledNode.model = Ci;

                // for any ci in Cj\Ci, the edge labeled ci originating from n' is no longer allowed
                polledNode.childrenToIgnore.addAll(difference);
                polledNode.childrenToProcess.removeAll(difference);

                List<RcTreeNode> children = new ArrayList<>(polledNode.children);

                for (RcTreeNode child : children){
                    if (difference.contains(child.labelAxiom)){
                        deleteNode(child, polledNode);
                    }
                }

                traverseTreeToUpdateIgnoredChildren(polledNode, difference);

                // ??? ? ???
                // Interchange the sets Cj and Ci in CS (Note that this has the same effect as eliminating Cj from CS.)
                // ??? ? ???

            }

            nodes.addAll(polledNode.children);
        }

    }

    void deleteNode(RcTreeNode child, RcTreeNode parent){

        parent.children.remove(child);

        Queue<RcTreeNode> localQueue = new ArrayDeque<>();
        localQueue.add(child);

        while (!localQueue.isEmpty()){
            RcTreeNode node = localQueue.poll();

            if (node.closed)
                continue;

            queue.remove(node);
            node.closeNode();
            localQueue.addAll(node.children);
            node.children.clear();
            node.childrenToProcess.clear();

            if (Configuration.DEBUG_PRINT)
                System.out.println("[RC-TREE] Deleting node: " + node);

            if (node.processed)
                stats.getLevelStatsNoSetting(node.depth).deleted_processed += 1;
            else
                stats.getLevelStatsNoSetting(node.depth).deleted_unprocessed += 1;

        }

    }

    private void traverseTreeToUpdateIgnoredChildren(RcTreeNode originalPolledNode, Set<OWLAxiom> difference){

        // TREBA PROPAGOVAT ZMENU V CELOM PODSTROME... TAKZE TREBA ZNOVA PRECHADZAT V QUEUECKU VRCHOLY
        Queue<RcTreeNode> nodes = new ArrayDeque<>();
        nodes.add(originalPolledNode);

        while(!nodes.isEmpty()){

            RcTreeNode polledNode = nodes.poll();

            // for all children n'' of n' update ΘC(n'') to ΘC(n'')\(Cj\Ci)
            for (RcTreeNode child : polledNode.children) {

                updateIgnoredChildren(child, difference);

                // for all descendants n''' of some n'' propagate the update accordingly
                nodes.add(child);
            }
        }
    }

    void updateIgnoredChildren(RcTreeNode node, Set<OWLAxiom> difference){
        Set<OWLAxiom> removedIgnored = node.childrenToIgnore.removeAllAndReturn(difference);

        if (!removedIgnored.isEmpty()){

            // create for all n'' and n''' all the edges that are not avoided anymore
            // (due to the updates to their Θs), and process the new nodes in a breadth-first order
            boolean added = node.childrenToProcess.addAll(removedIgnored);
            if (Configuration.DEBUG_PRINT && added)
                System.out.println("[RC-TREE] Added " + StringFactory.getRepresentation(removedIgnored) + " to "
                        + node + "'s children to be processed.");
            node.closed = false;
            if (!queue.contains(node) && !node.childrenToProcess.isEmpty()) {
                if (Configuration.DEBUG_PRINT)
                    System.out.println("[RC-TREE] Added " + node + " to the queue.");
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

    @Override
    public void labelNodeWithModel(TreeNode node){

        Model model = solver.findAndGetModelToReuse();

        if (model == null)
            return;

        node.model = solver.removePathAxiomsFromModel(model);

        RcTreeNode node_ = (RcTreeNode) node;

        for (OWLAxiom axiom : model.getNegatedData()){
            if (!node_.childrenToIgnore.contains(axiom))
                node_.childrenToProcess.add(axiom);
        }
    }

}
