package sk.uniba.fmph.dai.cats.algorithms.mhs;

import org.semanticweb.owlapi.model.OWLAxiom;
import sk.uniba.fmph.dai.cats.algorithms.*;
import sk.uniba.fmph.dai.cats.common.StaticPrinter;
import sk.uniba.fmph.dai.cats.data.Explanation;
import sk.uniba.fmph.dai.cats.data_processing.ExplanationManager;
import sk.uniba.fmph.dai.cats.model.Model;
import sk.uniba.fmph.dai.cats.reasoner.Loader;

import java.util.*;

public class MhsTreeBuilder implements ITreeBuilder {

    final AlgorithmSolver solver;
    final Loader loader;
    final INodeProcessor nodeProcessor;
    final Queue<TreeNode> queue = new ArrayDeque<>();

    TreeNode parentNode;

    List<OWLAxiom> iteratedChildren;
    private final Set<Set<OWLAxiom>> pathsInCurrentLevel = new HashSet<>();

    public MhsTreeBuilder(AlgorithmSolver solver){
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

        if (pathsInCurrentLevel.contains(solver.path)){
            StaticPrinter.debugPrint("[PRUNING] PATH ALREADY STORED!");
            return true;
        }

        pathsInCurrentLevel.add(new HashSet<>(solver.path));

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
    public void resetLevel() {
        pathsInCurrentLevel.clear();
    }
}
