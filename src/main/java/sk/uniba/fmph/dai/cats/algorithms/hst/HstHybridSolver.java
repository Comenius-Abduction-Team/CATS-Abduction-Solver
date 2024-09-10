package sk.uniba.fmph.dai.cats.algorithms.hst;

import sk.uniba.fmph.dai.cats.algorithms.ITreeNode;
import sk.uniba.fmph.dai.cats.algorithms.hybrid.*;
import sk.uniba.fmph.dai.cats.model.Model;
import sk.uniba.fmph.dai.cats.common.Configuration;
import sk.uniba.fmph.dai.cats.common.IPrinter;
import sk.uniba.fmph.dai.cats.common.StringFactory;
import sk.uniba.fmph.dai.cats.data.Explanation;
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
    int globalMin, currentChildIndex;

    public HstHybridSolver(ThreadTimer timer, IExplanationManager explanationManager,
                           IProgressManager progressManager, IPrinter printer){
        super(timer, explanationManager, progressManager, printer);
    }

    @Override
    protected void initialize() {

        setupCollections();
        addNegatedObservation();

        abducibleAxioms = new NumberedAxiomsUnindexedSet(createAbducibleAxioms());
        modelManager.setExtractor(new ModelExtractor(this, abducibleAxioms));

        //Initially, MIN is set to |COMP|
        globalMin = abducibleAxioms.size();
    }

    @Override
    protected void startSolving() throws OWLOntologyCreationException {
        currentDepth = 0;

        Queue<ITreeNode> queue = new ArrayDeque<>();

        HstTreeNode root = (HstTreeNode) createRoot();
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

            HstTreeNode node = (HstTreeNode) queue.poll();

            if(increaseDepth(node)){
                currentDepth++;
                if (Configuration.DEBUG_PRINT)
                    System.out.println("----------CURRENT DEPTH: " + currentDepth);
            }
            if(isTimeout()){
                makeTimeoutPartialLog();
                break;
            }

            if (depthLimitReached(node)) {
                break;
            }

            if (Configuration.DEBUG_PRINT)
                System.out.println("*********\n" + "PROCESSING node: "
                        + node.index + ". "
                        + StringFactory.getRepresentation(node.model.getNegatedData()));

            NumberedAxiomsUnindexedSet abducibles = (NumberedAxiomsUnindexedSet) abducibleAxioms;

            if (globalMin > 0)
                indexAxiomsFromModel(node, abducibles);

            //Let min(v) be MIN + 1
            node.min = globalMin + 1;

            // If i(v) > min(v) create a new array ranging over min(v), . . . , i(v)−1.
            // Otherwise, let mark(v) = × and create no child nodes for v.

            if (node.index <= node.min){
                if (Configuration.DEBUG_PRINT)
                    System.out.println(node.index + " <= " + node.min + " , closing");
                continue;
            }

            for (int index = node.min; index < node.index; index++){

                currentChildIndex = index;

                OWLAxiom child = abducibles.getAxiomByIndex(index);

                if (Configuration.DEBUG_PRINT)
                    System.out.println("TRYING EDGE: " + index + ". " + StringFactory.getRepresentation(child));

                if (child == null)
                    continue;

                if(isTimeout()){
                    makeTimeoutPartialLog();
                    return;
                }

                if(isIncorrectPath(node, child)){
                    continue;
                }

                Explanation explanation = createPossibleExplanation(node, child);
                //explanation.setLevel(currentDepth + 1);

                path = new HashSet<>(explanation.getAxioms());

                if(canBePruned(explanation)){
                    path.clear();
                    continue;
                }


                if (Configuration.REUSE_OF_MODELS)
                    findModelToUse();

                if (!Configuration.REUSE_OF_MODELS || !modelManager.canReuseModel()) {
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
                        if(!checkConsistency(abducibles)){
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
                addNodeToTree(queue, explanation, node);
            }
        }

        finishTreeTraversal();
    }

    private void indexAxiomsFromModel(TreeNode node, INumberedAbducibles abducibles){
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

    @Override
    protected ITreeNode createNodeFromExistingModel(boolean isRoot, Explanation explanation, Integer depth){

        INumberedAbducibles numAbducibles = (INumberedAbducibles) abducibleAxioms;

        if (numAbducibles.areAllAbduciblesIndexed()){

            TreeNode node = createTreeNode();
            node.label = explanation.getAxioms();
            node.depth = depth;
            node.model = new Model();
            return node;
        }

        return super.createNodeFromExistingModel(isRoot, explanation, depth);
    }

    @Override
    protected TreeNode createTreeNode() {
        HstTreeNode node = new HstTreeNode();
        node.index = currentChildIndex;
        if (Configuration.DEBUG_PRINT)
            System.out.println("Creating node: " + node.index);
        return node;
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
    protected boolean isIncorrectPath(TreeNode model, OWLAxiom child){
        return  model.label.contains(AxiomManager.getComplementOfOWLAxiom(loader, child)) ||
                child.equals(loader.getObservation().getOwlAxiom());
    }

    protected boolean checkConsistency(INumberedAbducibles abducibles){
        if (abducibles.areAllAbduciblesIndexed())
            return checkConsistency();
        return checkConsistencyWithModelExtraction();
    }

}