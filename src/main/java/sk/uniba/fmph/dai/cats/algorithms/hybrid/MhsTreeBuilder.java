package sk.uniba.fmph.dai.cats.algorithms.hybrid;

import org.semanticweb.owlapi.model.OWLAxiom;
import sk.uniba.fmph.dai.cats.common.Configuration;
import sk.uniba.fmph.dai.cats.data.AxiomSet;
import sk.uniba.fmph.dai.cats.data.Explanation;
import sk.uniba.fmph.dai.cats.data_processing.ExplanationManager;
import sk.uniba.fmph.dai.cats.model.Model;
import sk.uniba.fmph.dai.cats.reasoner.AxiomManager;
import sk.uniba.fmph.dai.cats.reasoner.Loader;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class MhsTreeBuilder implements TreeBuilder {

    final AlgorithmSolver solver;
    final Loader loader;
    final NodeProcessor nodeProcessor;
    final Queue<TreeNode> queue = new ArrayDeque<>();

    TreeNode parentNode;

    List<OWLAxiom> iteratedChildren;

    MhsTreeBuilder(AlgorithmSolver solver){
        this.solver = solver;
        this.loader = solver.loader;
        this.nodeProcessor = solver.nodeProcessor;
    }

    @Override
    public IAbducibleAxioms createAbducibles(TransformedAbducibles abducibles) {
        return new AxiomSet(abducibles.getAbducibleAxioms());
    }

    @Override
    public boolean hasIncorrectPath(TreeNode node) {
        if (node.path == null || node.labelAxiom == null)
            return false;
        return  node.path.contains(AxiomManager.getComplementOfOWLAxiom(solver.loader, node.labelAxiom)) ||
                node.labelAxiom.equals(solver.loader.getObservationAxiom());
    }

    @Override
    public boolean pruneNode(TreeNode node, Explanation explanation) {

        if (solver.isPathAlreadyStored()){
            if (Configuration.DEBUG_PRINT)
                System.out.println("[PRUNING] PATH ALREADY STORED!");
            return true;
        }

        solver.storePath();

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

    @Override
    public boolean closeExplanation(Explanation explanation) {
        return nodeProcessor.cannotAddExplanation(explanation, true);
    }

    @Override
    public TreeNode createRoot(){
        if (nodeProcessor.canCreateRoot())
            return createNode(null, null, 0);
        return null;
    }

    @Override
    public TreeNode createChildNode(TreeNode parent, OWLAxiom edge){

        return createNode(parent.path, edge, parent.depth + 1);
    }

    private TreeNode createNode(List<OWLAxiom> parentPath, OWLAxiom edge, Integer depth){

        TreeNode node = new TreeNode();

        if (parentPath != null) {
            node.setPath(parentPath, edge);
        }
        node.depth = depth;

        return node;
    }

    @Override
    public void addNodeToTree(TreeNode node) {
        queue.add(node);
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
        parentNode = node;
        iteratedChildren = new ArrayList<>(parentNode.model.getNegatedData());
        return true;
    }

    @Override
    public boolean noChildrenLeft(){
        return iteratedChildren.isEmpty();
    }

    @Override
    public OWLAxiom getNextChild(){
        OWLAxiom child = iteratedChildren.get(0);
        iteratedChildren.remove(0);
        return child;
    }

    @Override
    public void assignModel(TreeNode node){

        Model model = solver.findAndGetModelToReuse();

        if (model == null)
            return;

        node.model = solver.removePathAxiomsFromModel(model);

    }
}
