package sk.uniba.fmph.dai.cats.algorithms.hybrid;

import org.semanticweb.owlapi.model.OWLAxiom;
import sk.uniba.fmph.dai.cats.common.Configuration;
import sk.uniba.fmph.dai.cats.data.AxiomSet;
import sk.uniba.fmph.dai.cats.data.Explanation;
import sk.uniba.fmph.dai.cats.model.Model;
import sk.uniba.fmph.dai.cats.reasoner.AxiomManager;
import sk.uniba.fmph.dai.cats.reasoner.Loader;

import java.util.*;

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
    public boolean isIncorrectPath(List<OWLAxiom> path, OWLAxiom child) {
        return  path.contains(AxiomManager.getComplementOfOWLAxiom(solver.loader, child)) ||
                child.equals(solver.loader.getObservationAxiom());
    }

    @Override
    public boolean pruneTree(TreeNode node, Explanation explanation) {

        if (solver.isPathAlreadyStored()){
            //node.closeNode();
            if (Configuration.DEBUG_PRINT)
                System.out.println("[PRUNING] PATH ALREADY STORED!");
            return true;
        }

        solver.storePath();

        RuleChecker ruleChecker = solver.ruleChecker;
        ExplanationManager explanationManager = solver.explanationManager;

        if (!ruleChecker.isMinimal(explanationManager.getPossibleExplanations(), explanation)){
            //node.closeNode();
            if (Configuration.DEBUG_PRINT)
                System.out.println("[PRUNING] NON-MINIMAL EXPLANATION!");
            return true;
        }

        if (nodeProcessor.isInvalidExplanation(explanation)){
            //node.closeNode();
            return true;
        }

        return false;

    }

    @Override
    public TreeNode createRoot(){
        if (nodeProcessor.canCreateRoot())
            return createNode(null, 0);
        return null;
    }

    @Override
    public TreeNode createChildNode(TreeNode parent, Explanation label){

        return createNode(label, parent.depth + 1);
    }

    public TreeNode createNode(Explanation label, Integer depth){


        TreeNode node = new TreeNode();

        if (label != null) {
            node.label = label.getAxioms();
        }
        node.depth = depth;

        Model modelToReuse = solver.findAndGetModelToReuse();

        if (modelToReuse == null)
            return null;

        node.model = solver.removePathAxiomsFromModel(modelToReuse);

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
}