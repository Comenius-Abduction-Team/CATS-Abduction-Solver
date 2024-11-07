package sk.uniba.fmph.dai.cats.algorithms.rctree;

import org.semanticweb.owlapi.model.OWLAxiom;
import sk.uniba.fmph.dai.cats.algorithms.hybrid.*;
import sk.uniba.fmph.dai.cats.common.Configuration;
import sk.uniba.fmph.dai.cats.data.AxiomSet;
import sk.uniba.fmph.dai.cats.data.Explanation;
import sk.uniba.fmph.dai.cats.explanation_processing.ExplanationManager;
import sk.uniba.fmph.dai.cats.model.Model;
import sk.uniba.fmph.dai.cats.reasoner.AxiomManager;

import java.util.*;

public class RcTreeBuilder implements TreeBuilder {

    final AlgorithmSolver solver;
    final NodeProcessor nodeProcessor;

    final Queue<RcTreeNode> queue  = new PriorityQueue<>();

    int index = 0;

    RcTreeNode root;

    RcTreeNode currentNode;

    public RcTreeBuilder(AlgorithmSolver solver){
        this.solver = solver;
        this.nodeProcessor = solver.nodeProcessor;
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

    void deleteNode(RcTreeNode child, RcTreeNode parent){

        if (Configuration.DEBUG_PRINT)
            System.out.println("[RC-TREE] Pruning child: " + child);

        parent.children.remove(child);

        Queue<RcTreeNode> localQueue = new ArrayDeque<>();
        localQueue.add(child);

        while (!localQueue.isEmpty()){
            RcTreeNode node = localQueue.poll();
            if (Configuration.DEBUG_PRINT)
                System.out.println("[RC-TREE] Closing node: " + node);
            queue.remove(node);
            node.closeNode();
            localQueue.addAll(node.children);

        }

    }

    void updateIgnoredChildren(RcTreeNode node, Set<OWLAxiom> difference){
        Set<OWLAxiom> removedIgnored = node.childrenToIgnore.removeAllAndReturn(difference);

        if (!removedIgnored.isEmpty()){

//            if (Configuration.DEBUG_PRINT)
//                System.out.println("[RC-TREE] " + node + " is subset of " + polledNode);

            // create for all n'' and n''' all the edges that are not avoided anymore
            // (due to the updates to their Θs), and process the new nodes in a breadth-first order
            node.childrenToProcess.addAll(removedIgnored);
            node.closed = false;
            if (!queue.contains(node))
                queue.add(node);
        }

    }

    @Override
    public boolean hasIncorrectPath(TreeNode node) {
        if (node.path == null || node.labelAxiom == null)
            return false;
        return  node.path.contains(AxiomManager.getComplementOfOWLAxiom(solver.loader, node.labelAxiom)) ||
                node.labelAxiom.equals(solver.loader.getObservationAxiom());
    }

    @Override
    public boolean closeExplanation(Explanation explanation) {
        return nodeProcessor.cannotAddExplanation(explanation, true);
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
    public TreeNode createChildNode(TreeNode parent, OWLAxiom edge) {
        return createNode((RcTreeNode) parent, edge);
    }

    private RcTreeNode createNode(RcTreeNode parent, OWLAxiom edge){

        RcTreeNode node = new RcTreeNode(getAndIncreaseIndex());

        if (parent.path != null) {
            node.setPath(parent.path, edge);
        }
        node.depth = parent.depth + 1;

        parent.children.add(node);
        node.parent = parent;
        parent.usedLabels.add(edge);

        node.childrenToIgnore.addAll(parent.childrenToIgnore.getAxioms());
        node.childrenToIgnore.addAll(parent.usedLabels);

        return node;
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
                //TODO mozeme nahradit cely model alebo treba iba neg.data?
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

    @Override
    public boolean noChildrenLeft(){
        return currentNode.childrenToProcess.isEmpty();
    }

    @Override
    public OWLAxiom getNextChild(){
        return currentNode.childrenToProcess.remove(0);

    }

    @Override
    public void assignModel(TreeNode node){

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
