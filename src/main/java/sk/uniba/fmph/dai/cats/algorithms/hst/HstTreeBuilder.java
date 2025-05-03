package sk.uniba.fmph.dai.cats.algorithms.hst;

import org.semanticweb.owlapi.model.OWLAxiom;
import sk.uniba.fmph.dai.cats.algorithms.*;
import sk.uniba.fmph.dai.cats.common.StaticPrinter;
import sk.uniba.fmph.dai.cats.data.Explanation;
import sk.uniba.fmph.dai.cats.data_processing.ExplanationManager;
import sk.uniba.fmph.dai.cats.model.Model;

import java.util.ArrayDeque;
import java.util.Queue;

public class HstTreeBuilder implements ITreeBuilder {

    final AlgorithmSolver solver;
    final INodeProcessor nodeProcessor;
    final Queue<HstTreeNode> queue = new ArrayDeque<>();

    HstTreeNode parentNode;

    int globalMin;

    int nextChildIndex;

    INumberedAbducibles abducibles;

    public HstTreeBuilder(AlgorithmSolver solver){
        this.solver = solver;
        this.nodeProcessor = solver.nodeProcessor;
    }

    @Override
    public IAbducibleAxioms createAbducibles(TransformedAbducibles abducibles) {

        this.abducibles = new NumberedAxiomsUnindexedSet(abducibles);

        //Initially, MIN is set to |COMP|
        globalMin = this.abducibles.size();
        return this.abducibles;
    }

    @Override
    public TreeNode createRoot(){
        solver.currentLevel.hstGlobalMin = globalMin;
        if (nodeProcessor.canCreateRoot(true))
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
    public boolean shouldExtractModel() {
        return !abducibles.areAllAbduciblesIndexed();
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
            StaticPrinter.debugPrint("[PRUNING] NON-MINIMAL EXPLANATION!");
            return true;
        }

        return nodeProcessor.isInvalidExplanation(explanation);
    }

    //int index = node.min; index < node.index; index++
    @Override
    public boolean startIteratingNodeChildren(TreeNode node){
        parentNode = (HstTreeNode) node;

        if (globalMin > 0)
            indexAxiomsFromModel(parentNode, abducibles);
        solver.currentLevel.hstGlobalMin = globalMin;

        //Let min(v) be MIN + 1
        parentNode.min = globalMin + 1;

        StaticPrinter.debugPrint("[HST] min changed to: " + parentNode.min);

        // If i(v) > min(v) create a new array ranging over min(v), . . . , i(v)−1.
        // Otherwise, let mark(v) = × and create no child nodes for v.

        if (parentNode.index <= parentNode.min){
            StaticPrinter.debugPrint("[HST] CLOSING: " + parentNode.index + " <= " + parentNode.min);
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
        StaticPrinter.debugPrint("[HST] child index: " + nextChildIndex);
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
