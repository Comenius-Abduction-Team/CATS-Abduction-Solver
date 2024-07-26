package sk.uniba.fmph.dai.cats.algorithms.hst;

import sk.uniba.fmph.dai.cats.algorithms.hybrid.*;
import sk.uniba.fmph.dai.cats.common.Configuration;
import sk.uniba.fmph.dai.cats.common.IPrinter;
import sk.uniba.fmph.dai.cats.models.Explanation;
import org.semanticweb.owlapi.model.*;
import sk.uniba.fmph.dai.cats.progress.IProgressManager;
import sk.uniba.fmph.dai.cats.reasoner.AxiomManager;
import sk.uniba.fmph.dai.cats.timer.ThreadTimer;

import java.util.*;

/**
 * Base = knowledgeBase + negObservation
 * Literals = set of all literals / concepts with named individual except observation
 */

public class HstHybridSolver extends HybridSolver {

    //MIN
    int globalMin;

    public HstHybridSolver(ThreadTimer timer, IExplanationManager explanationManager,
                           IProgressManager progressManager, IPrinter printer){
        super(timer, explanationManager, progressManager, printer);
    }

    @Override
    protected void initialize() {

        setupCollections();
        addNegatedObservation();

        abducibleAxioms = new NumberedAxiomsUnindexedSet(createAbducibleAxioms());
        modelExtractor.initialiseAbducibles();

        //Initially, MIN is set to |COMP|
        globalMin = abducibleAxioms.size();
    }

    @Override
    protected void startSolving() throws OWLOntologyCreationException {
        currentDepth = 0;

        Queue<TreeNode> queue = new ArrayDeque<>();
        ModelNode root = createRoot();
        if (root == null)
            return;

        queue.add(root);

        //Set i(v) = |COMP| + 1
        root.index = abducibleAxioms.size() + 1;

        if (isTimeout()) {
            makeTimeoutPartialLog();
            return;
        }

        while (!queue.isEmpty()) {
            TreeNode node = queue.poll();

            if(increaseDepth(node)){
                currentDepth++;
                if (Configuration.DEBUG_PRINT)
                    System.out.println("----------CURRENT DEPTH: " + currentDepth);
            }
            if(isTimeout() || !ModelNode.class.isAssignableFrom(node.getClass())){
                makeTimeoutPartialLog();
                break;
            }

            ModelNode model = (ModelNode) node;
            if (depthLimitReached(model)) {
                break;
            }

            //System.out.println("IDEME SPRACOVAT VRCHOL ID " + model.id + "s indexom " + model.index);

            NumberedAxiomsUnindexedSet abducibles = (NumberedAxiomsUnindexedSet) abducibleAxioms;

            if (globalMin > 0)
                indexAxiomsFromModel(model, abducibles);

            //Let min(v) be MIN + 1
            model.min = globalMin + 1;

            // If i(v) > min(v) create a new array ranging over min(v), . . . , i(v)−1.
            // Otherwise, let mark(v) = × and create no child nodes for v.

            if (model.index <= model.min){
                continue;
            }

            for (int index = model.min; index < model.index; index++){
                //for (int index = model.index-1; index >= model.min; index--){

                OWLAxiom child = abducibles.getAxiomByIndex(index);

                if (child == null)
                    continue;

                if(isTimeout()){
                    makeTimeoutPartialLog();
                    return;
                }

                if(isIncorrectPath(model, child)){
                    continue;
                }

                Explanation explanation = createPossibleExplanation(model, child);
                //explanation.setLevel(currentDepth + 1);

                path = new HashSet<>(explanation.getAxioms());

                if(canBePruned(explanation)){
                    path.clear();
                    continue;
                }

                int reuseIndex = -1;

                if (Configuration.REUSE_OF_MODELS)
                    reuseIndex = findReuseIndex();

                if (reuseIndex == -1) {
                    if(isTimeout()){
                        makeTimeoutPartialLog();
                        return;
                    }
                    if (Configuration.ALGORITHM.usesMxp()){
                        {
                            boolean newExplanationsFound = addNewExplanations();
                            if (!newExplanationsFound)
                                continue;
                            if (isTimeout()) {
                                makeTimeoutPartialLog();
                                return;
                            }
                        }
                    }
                    else {
                        if(!isOntologyConsistent(abducibles)){
                            explanation.setDepth(explanation.getAxioms().size());
                            explanationManager.addPossibleExplanation(explanation);
                            path.clear();
                            continue;
                        }
                    }
                }
                else{
                    explanationManager.setLengthOneExplanations(new ArrayList<>());
                }
                addNodeToTree(abducibles, queue, explanation, model, index, reuseIndex);
            }
        }
        path.clear();
        if(!levelTimes.containsKey(currentDepth)){
            makePartialLog();
        }
        currentDepth = 0;
    }

    private void indexAxiomsFromModel(ModelNode model, INumberedAbducibles abducibles){
        for (OWLAxiom child : model.data){
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

    private void addNodeToTree(INumberedAbducibles abducibles, Queue<TreeNode> queue, Explanation explanation, ModelNode parent, int index, int reuseIndex){
        ModelNode newNode = createModelNodeFromExistingModel(abducibles, explanation, parent.depth + 1, reuseIndex);
        if(newNode == null){
            path.clear();
            return;
        }

        newNode.index = index;
        newNode.parentIndex = parent.index;

        queue.add(newNode);
        numberOfNodes++;
        path.clear();
    }

    private ModelNode createModelNodeFromExistingModel(INumberedAbducibles abducibles, Explanation explanation, Integer depth, int reuseIndex){

        if (abducibles.areAllAbduciblesIndexed()){

            ModelNode node = new ModelNode();
            node.label = explanation.getAxioms();
            node.depth = depth;
            return node;
        }

        return createModelNodeFromExistingModel(false,explanation,depth,reuseIndex);
    }

    @Override
    protected boolean canBePruned(Explanation explanation) throws OWLOntologyCreationException {
        if (!ruleChecker.isMinimal(explanationManager.getPossibleExplanations(), explanation)){
            return true;
        }

        if(Configuration.CHECK_RELEVANCE_DURING_BUILDING_TREE_IN_MHS_MXP){
            if(!ruleChecker.isRelevant(explanation)){
                return true;
            }
        }

        if(!Configuration.ALGORITHM.usesMxp()){
            if(!ruleChecker.isRelevant(explanation)){
                return true;
            }
            if(!ruleChecker.isConsistent(explanation)){
                return true;
            }
        }

        if(Configuration.ALGORITHM.usesMxp()){
            if (ruleChecker.isExplanation(explanation)){
                addToExplanations(explanation);
                return true;
            }
        }
        return false;
    }

    @Override
    protected boolean isIncorrectPath(ModelNode model, OWLAxiom child){
        return  model.label.contains(AxiomManager.getComplementOfOWLAxiom(loader, child)) ||
                child.equals(loader.getObservation().getOwlAxiom());
    }

    protected boolean isOntologyConsistent(INumberedAbducibles abducibles){
        if (abducibles.areAllAbduciblesIndexed())
            return modelExtractor.isOntologyConsistentWithPath();
        return isOntologyConsistent();
    }

}