package sk.uniba.fmph.dai.cats.algorithms.hybrid;

import org.semanticweb.owlapi.model.OWLAxiom;
import sk.uniba.fmph.dai.cats.algorithms.hst.HstTreeNode;
import sk.uniba.fmph.dai.cats.algorithms.hst.INumberedAbducibles;
import sk.uniba.fmph.dai.cats.algorithms.hst.NumberedAxiomsUnindexedSet;
import sk.uniba.fmph.dai.cats.common.Configuration;
import sk.uniba.fmph.dai.cats.data.Explanation;
import sk.uniba.fmph.dai.cats.data_processing.ExplanationManager;
import sk.uniba.fmph.dai.cats.model.Model;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.Set;

public class HstTreeBuilder implements TreeBuilder {

    final AlgorithmSolver solver;
    final NodeProcessor nodeProcessor;
    final Queue<HstTreeNode> queue = new ArrayDeque<>();

    HstTreeNode parentNode;

    int globalMin;

    int nextChildIndex;

    INumberedAbducibles abducibles;

    HstTreeBuilder(AlgorithmSolver solver){
        this.solver = solver;
        this.nodeProcessor = solver.nodeProcessor;
    }

    @Override
    public IAbducibleAxioms createAbducibles(TransformedAbducibles abducibles) {

        Set<OWLAxiom> abducibleAxioms = abducibles.abducibleAxioms;

        this.abducibles = new NumberedAxiomsUnindexedSet(abducibleAxioms);

        //Initially, MIN is set to |COMP|
        globalMin = abducibleAxioms.size();

        return this.abducibles;
    }

    @Override
    public TreeNode createRoot(){
        if (nodeProcessor.canCreateRoot())
            return createNode(null, TreeNode.DEFAULT_DEPTH, abducibles.size() + 1);
        return null;
    }

    @Override
    public TreeNode createChildNode(TreeNode parent, Explanation label){

        return createNode(label, parent.depth + 1, nextChildIndex -1);

    }

    private HstTreeNode createNode(Explanation label, Integer depth, int index){

        HstTreeNode node = new HstTreeNode();
        node.index = index;

        if (label != null) {
            node.path = label.getAxioms();
        }
        node.depth = depth;

        if (abducibles.areAllAbduciblesIndexed()){

            node.model = new Model();
            return node;
        }

        Model modelToReuse = solver.findAndGetModelToReuse();

        if (modelToReuse == null)
            return null;

        node.model = solver.removePathAxiomsFromModel(modelToReuse);

        return node;
    }

    @Override
    public boolean closeExplanation(Explanation explanation) {
        boolean extractModel = !abducibles.areAllAbduciblesIndexed();
        return nodeProcessor.findExplanations(explanation, extractModel);
    }

    @Override
    public void addNodeToTree(TreeNode node){
        queue.add((HstTreeNode) node);
    }

    @Override
    public boolean isTreeClosed() {
        return queue.isEmpty();
    }

    @Override
    public TreeNode getNextNodeFromTree() {
        HstTreeNode node = queue.poll();
        if (node == null)
            return null;
        return node;
    }

    @Override
    public boolean pruneNode(TreeNode node, Explanation explanation) {

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

    //int index = node.min; index < node.index; index++
    @Override
    public boolean startIteratingNodeChildren(TreeNode node){
        parentNode = (HstTreeNode) node;

        if (globalMin > 0)
            indexAxiomsFromModel(parentNode, abducibles);

        //Let min(v) be MIN + 1
        parentNode.min = globalMin + 1;

        // If i(v) > min(v) create a new array ranging over min(v), . . . , i(v)−1.
        // Otherwise, let mark(v) = × and create no child nodes for v.

        if (parentNode.index <= parentNode.min){
            if (Configuration.DEBUG_PRINT)
                System.out.println("[HST] CLOSING: " + parentNode.index + " <= " + parentNode.min);
            return false;
        }

        nextChildIndex = parentNode.min;

        return true;
    }

    private void indexAxiomsFromModel(HstTreeNode node, INumberedAbducibles abducibles){
        for (OWLAxiom child : node.model.getNegatedData()){
            //For every component C in y with no previously defined index ci,
            // let ci(C) be MIN and decrement MIN afterwards.
            if (abducibles.shouldBeIndexed(child)){
                if (globalMin > 0){
                    abducibles.addWithIndex(child,globalMin);
                    globalMin -= 1;
                }
                else
                    return;
            }
        }
    }

    //int index = node.min; index < node.index; index++
    @Override
    public boolean noChildrenLeft(){
        return nextChildIndex == parentNode.index;
    }

    //int index = node.min; index < node.index; index++
    @Override
    public OWLAxiom getNextChild(){
        OWLAxiom child = abducibles.getAxiomByIndex(nextChildIndex);
        if (Configuration.DEBUG_PRINT)
            System.out.println("[HST] child index: " + nextChildIndex);
        nextChildIndex++;
        return child;
    }

    @Override
    public void labelNodeWithModel(TreeNode node){

        if (abducibles.areAllAbduciblesIndexed()){

            node.model = new Model();
            return;

        }

        Model modelToReuse = solver.findAndGetModelToReuse();

        if (modelToReuse == null)
            return;

        node.model = solver.removePathAxiomsFromModel(modelToReuse);

    }
}
