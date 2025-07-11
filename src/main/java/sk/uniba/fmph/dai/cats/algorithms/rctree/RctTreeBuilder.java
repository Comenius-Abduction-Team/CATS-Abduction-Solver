package sk.uniba.fmph.dai.cats.algorithms.rctree;

import org.semanticweb.owlapi.model.OWLAxiom;
import sk.uniba.fmph.dai.cats.algorithms.*;
import sk.uniba.fmph.dai.cats.common.StaticPrinter;
import sk.uniba.fmph.dai.cats.common.StringFactory;
import sk.uniba.fmph.dai.cats.data.Explanation;
import sk.uniba.fmph.dai.cats.data_processing.ExplanationManager;
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
            StaticPrinter.debugPrint("[PRUNING] NON-MINIMAL EXPLANATION!");
            return true;
        }

        if (nodeProcessor.shouldPruneBranch(explanation)){
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
        node.model = solver.removePathAxiomsFromModel(modelToReuse);
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
        RctNode node = queue.poll();
        //System.out.println(queue.stream().map(n -> n.depth).collect(Collectors.toSet()));
        return node;
    }

    @Override
    public boolean startIteratingNodeChildren(TreeNode node){
        currentNode = (RctNode) node;
        pruneTree();
        if (currentNode.closed || currentNode.childrenToProcess.isEmpty())
            return false;
        return true;
    }

    private void pruneTree(){

        Queue<RctNode> nodes = new ArrayDeque<>();
        nodes.add(root);

        while (!nodes.isEmpty()){

            RctNode polledNode = nodes.poll();

            if (polledNode == currentNode)
                continue;

            // nodes n' labeled with some Cj from CS such that Ci C Cj
            if (currentNode.isSubsetOf(polledNode)){

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
                polledNode.childrenToIgnore.addAll(difference);
                polledNode.childrenToProcess.removeAll(difference);

                List<RctNode> children = new ArrayList<>(polledNode.children);

                for (RctNode child : children){
                    if (difference.contains(child.labelAxiom)){
                        deleteNode(child, polledNode);
                    }
                }

                traverseTreeToUpdateIgnoredChildren(polledNode, difference);

            }

            nodes.addAll(polledNode.children);
        }

    }

    void deleteNode(RctNode child, RctNode parent){

        parent.children.remove(child);

        Queue<RctNode> localQueue = new ArrayDeque<>();
        localQueue.add(child);

        while (!localQueue.isEmpty()){
            RctNode node = localQueue.poll();

            if (node.closed)
                continue;

            queue.remove(node);
            node.closeNode();
            localQueue.addAll(node.children);
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

        Queue<RctNode> nodes = new ArrayDeque<>();
        nodes.add(originalPolledNode);

        while(!nodes.isEmpty()){

            RctNode polledNode = nodes.poll();

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
        OWLAxiom child = currentNode.childrenToProcess.remove(0);
        if (child != null){
            currentNode.childrenToIgnore.add(child);
        }
        return child;
    }

}
