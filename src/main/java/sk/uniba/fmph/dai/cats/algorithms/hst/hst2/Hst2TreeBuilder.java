package sk.uniba.fmph.dai.cats.algorithms.hst.hst2;

import org.semanticweb.owlapi.model.OWLAxiom;
import sk.uniba.fmph.dai.cats.algorithms.*;
import sk.uniba.fmph.dai.cats.algorithms.hst.INumberedAbducibles;
import sk.uniba.fmph.dai.cats.algorithms.hst.UnindexedSetNumberedAbducibles;
import sk.uniba.fmph.dai.cats.common.StaticPrinter;
import sk.uniba.fmph.dai.cats.data.Explanation;
import sk.uniba.fmph.dai.cats.data_processing.ExplanationManager;
import sk.uniba.fmph.dai.cats.events.EventPublisher;
import sk.uniba.fmph.dai.cats.events.EventType;
import sk.uniba.fmph.dai.cats.model.Model;

import java.util.ArrayDeque;
import java.util.Queue;

public class Hst2TreeBuilder implements ITreeBuilder {

    final AlgorithmSolver solver;
    final INodeProcessor nodeProcessor;
    final Queue<Hst2Node> queue = new ArrayDeque<>();

    Hst2Node parentNode;

    int globalMin;

    int nextChildIndex;

    INumberedAbducibles abducibles;

    public Hst2TreeBuilder(AlgorithmSolver solver){
        this.solver = solver;
        this.nodeProcessor = solver.nodeProcessor;
    }

    @Override
    public IAbducibleAxioms createAbducibles(TransformedAbducibles abducibles) {

        this.abducibles = new UnindexedSetNumberedAbducibles(abducibles);

        //Initially, MIN is set to |COMP|
        globalMin = this.abducibles.size();
        return this.abducibles;
    }

    @Override
    public TreeNode createRoot(){
        solver.currentLevel.hstGlobalMin = globalMin;
        return createNode(null, TreeNode.DEFAULT_DEPTH, abducibles.size() + 1, null);
    }

    @Override
    public TreeNode createChildNode(TreeNode parent, Explanation path){

        return createNode(path, parent.depth + 1, nextChildIndex -1, (Hst2Node)parent);

    }

    private Hst2Node createNode(Explanation path, Integer depth, int index, Hst2Node parent){

        Hst2Node node = new Hst2Node();
        node.index = index;

        if (path != null) {
            node.path = path.getAxioms();
        }
        node.depth = depth;

        if (abducibles.areAllAbduciblesIndexed()){

            node.model = new Model();

        } else {
            Model modelToReuse = solver.findAndGetModelToReuse();

            if (modelToReuse == null)
                return null;

            node.model = modelToReuse;
        }

        if (parent != null) {
            node.parent = parent;
            parent.children.add(node);
        }

        return node;
    }

    @Override
    public boolean shouldExtractModel() {
        return !abducibles.areAllAbduciblesIndexed();
    }

    @Override
    public void addNodeToTree(TreeNode node){
        queue.add((Hst2Node) node);
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
    public boolean shouldPruneChildBranch(TreeNode node, Explanation explanation) {

        RuleChecker ruleChecker = solver.ruleChecker;
        ExplanationManager explanationManager = solver.explanationManager;

        if (!ruleChecker.isMinimal(explanationManager.getPossibleExplanations(), explanation)){
            EventPublisher.publishExplanationEvent(solver, EventType.NONMINIMAL_PATH, explanation);
            return true;
        }

        return nodeProcessor.shouldPruneBranch(explanation);
    }

    //int index = node.min; index < node.index; index++
    @Override
    public boolean startIteratingNodeChildren(TreeNode node){
        parentNode = (Hst2Node) node;

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
            pruneTree(parentNode);
            return false;
        }

        nextChildIndex = parentNode.min;

        return true;
    }

    private void pruneTree(Hst2Node node){
        Queue<Hst2Node> queue = new ArrayDeque<>();
        queue.add(node);

        while (!queue.isEmpty()) {
            Hst2Node polledNode = queue.poll();
            Hst2Node parent = polledNode.parent;
            if (polledNode.hasExplanationEdgeChild || !polledNode.children.isEmpty() || parent == null) {
                return;
            }
            parent.children.remove(polledNode);
            queue.add(parent);
        }
    }

    private void indexAxiomsFromModel(Hst2Node node, INumberedAbducibles abducibles){
        for (OWLAxiom child : node.model.getNegatedData()){
            //For every component C in y with no previously defined index ci,
            // let ci(C) be MIN and decrement MIN afterwards.
            if (!abducibles.isIndexed(child)){
                if (globalMin > 0){
                    abducibles.assignIndex(child,globalMin);
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
        boolean noChildrenLeft = nextChildIndex == parentNode.index;

        if (noChildrenLeft) {
            pruneTree(parentNode);
        }

        return noChildrenLeft;
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
    public void processExplanationEdge(boolean isExplanationEdge, TreeNode node) {
        if (isExplanationEdge) {
            ((Hst2Node)node).hasExplanationEdgeChild = true;
        }
    }
}
