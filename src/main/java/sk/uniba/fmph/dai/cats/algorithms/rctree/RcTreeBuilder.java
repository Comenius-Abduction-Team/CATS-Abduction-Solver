package sk.uniba.fmph.dai.cats.algorithms.rctree;

import org.semanticweb.owlapi.model.OWLAxiom;
import sk.uniba.fmph.dai.cats.algorithms.hybrid.*;
import sk.uniba.fmph.dai.cats.common.Configuration;
import sk.uniba.fmph.dai.cats.data.AxiomSet;
import sk.uniba.fmph.dai.cats.data.Explanation;
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
    public boolean pruneTree(TreeNode originalNode, Explanation explanation){

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
    public boolean isIncorrectPath(List<OWLAxiom> path, OWLAxiom child) {
        return  path.contains(AxiomManager.getComplementOfOWLAxiom(solver.loader, child)) ||
                child.equals(solver.loader.getObservationAxiom());
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

        node.childrenToIgnore.addAll(parent.childrenToIgnore.getAxioms());
        node.childrenToIgnore.addAll(parent.usedLabels);

        for (OWLAxiom axiom : node.model.getNegatedData()){
            if (!node.childrenToIgnore.contains(axiom))
                node.childrenToProcess.add(axiom);
        }

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
}
