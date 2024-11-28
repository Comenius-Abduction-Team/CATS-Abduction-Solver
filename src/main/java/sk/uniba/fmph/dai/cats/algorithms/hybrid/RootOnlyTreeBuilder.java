package sk.uniba.fmph.dai.cats.algorithms.hybrid;

import org.semanticweb.owlapi.model.OWLAxiom;
import sk.uniba.fmph.dai.cats.data.AxiomSet;
import sk.uniba.fmph.dai.cats.data.Explanation;
import sk.uniba.fmph.dai.cats.model.Model;

import java.util.List;

public class RootOnlyTreeBuilder implements TreeBuilder {

    final AlgorithmSolver solver;

    RootOnlyTreeBuilder(AlgorithmSolver solver){
        this.solver = solver;
    }

    @Override
    public IAbducibleAxioms createAbducibles(TransformedAbducibles abducibles) {
        return new AxiomSet(abducibles.getAbducibleAxioms());
    }

    @Override
    public boolean pruneNode(TreeNode node, Explanation explanation) {
        return true;
    }

    @Override
    public TreeNode createRoot() {
        if (!solver.nodeProcessor.canCreateRoot(false))
            return null;

        return new TreeNode();

    }

    @Override
    public TreeNode createChildNode(TreeNode parent, Explanation label) {
        return null;
    }

    @Override
    public boolean closeExplanation(Explanation explanation) {
        return true;
    }

    @Override
    public void addNodeToTree(TreeNode node) {}

    @Override
    public boolean isTreeClosed() {
        return true;
    }

    @Override
    public TreeNode getNextNodeFromTree() {
        return null;
    }

    @Override
    public boolean startIteratingNodeChildren(TreeNode parent) {
        return false;
    }

    @Override
    public boolean noChildrenLeft() {
        return true;
    }

    @Override
    public OWLAxiom getNextChild() {
        return null;
    }

    @Override
    public void labelNodeWithModel(TreeNode node){}
}
