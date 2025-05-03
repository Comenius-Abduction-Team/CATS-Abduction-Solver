package sk.uniba.fmph.dai.cats.algorithms;

import org.semanticweb.owlapi.model.OWLAxiom;
import sk.uniba.fmph.dai.cats.common.StaticPrinter;
import sk.uniba.fmph.dai.cats.data.Explanation;
import sk.uniba.fmph.dai.cats.data_processing.ExplanationManager;
import sk.uniba.fmph.dai.cats.model.Model;
import sk.uniba.fmph.dai.cats.reasoner.Loader;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class MhsTreeBuilder implements ITreeBuilder {

    final AlgorithmSolver solver;
    final Loader loader;
    final INodeProcessor nodeProcessor;
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
        return new AxiomSetAbducibles(abducibles);
    }

    @Override
    public boolean pruneNode(TreeNode node, Explanation explanation) {

        if (solver.isPathAlreadyStored()){
            StaticPrinter.debugPrint("[PRUNING] PATH ALREADY STORED!");
            return true;
        }

        solver.storePath();

        RuleChecker ruleChecker = solver.ruleChecker;
        ExplanationManager explanationManager = solver.explanationManager;

        if (!ruleChecker.isMinimal(explanationManager.getPossibleExplanations(), explanation)){
            StaticPrinter.debugPrint("[PRUNING] NON-MINIMAL EXPLANATION!");
            return true;
        }

        if (nodeProcessor.isInvalidExplanation(explanation)){
            return true;
        }

        return false;

    }

    @Override
    public TreeNode createRoot(){
        if (nodeProcessor.canCreateRoot(true))
            return createNode(null, TreeNode.DEFAULT_DEPTH);
        return null;
    }

    @Override
    public TreeNode createChildNode(TreeNode parent, Explanation label){

        return createNode(label, parent.depth + 1);
    }

    private TreeNode createNode(Explanation label, Integer depth){


        TreeNode node = new TreeNode();

        if (label != null) {
            node.path = label.getAxioms();
        }
        node.depth = depth;

        Model modelToReuse = solver.findAndGetModelToReuse();

        if (modelToReuse == null)
            return null;

        node.model = solver.removePathAxiomsFromModel(modelToReuse);

        return node;
    }

    @Override
    public boolean shouldExtractModel() {
        return true;
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


    public boolean startIteratingNodeChildren(TreeNode node){
        parentNode = node;
        iteratedChildren = new ArrayList<>(parentNode.model.getNegatedData());
        return true;
    }

    public boolean noChildrenLeft(){
        return iteratedChildren.isEmpty();
    }

    public OWLAxiom getNextChild(){
        OWLAxiom child = iteratedChildren.get(0);
        iteratedChildren.remove(0);
        return child;
    }

    @Override
    public void labelNodeWithModel(TreeNode node){

        Model model = solver.findAndGetModelToReuse();

        if (model == null)
            return;

        node.model = solver.removePathAxiomsFromModel(model);

    }
}
