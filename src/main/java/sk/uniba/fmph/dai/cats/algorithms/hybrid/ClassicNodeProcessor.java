package sk.uniba.fmph.dai.cats.algorithms.hybrid;

import sk.uniba.fmph.dai.cats.common.Configuration;
import sk.uniba.fmph.dai.cats.data.Explanation;
import sk.uniba.fmph.dai.cats.explanation_processing.ExplanationManager;

public class ClassicNodeProcessor implements NodeProcessor {

    private final RuleChecker ruleChecker;
    private final ConsistencyChecker consistencyChecker;
    private final ExplanationManager explanationManager;

    ClassicNodeProcessor(AlgorithmSolver solver){
        ruleChecker = solver.ruleChecker;
        consistencyChecker = solver.consistencyChecker;
        explanationManager = solver.explanationManager;
    }

    @Override
    public boolean canCreateRoot() {
        return consistencyChecker.checkConsistencyWithModelExtraction();
    }

    @Override
    public boolean isInvalidExplanation(Explanation explanation) {
        if (!ruleChecker.isRelevant(explanation)) {
            if (Configuration.DEBUG_PRINT)
                System.out.println("[PRUNING] IRRELEVANT EXPLANATION!");
            return true;
        }
        if (!ruleChecker.isConsistent(explanation)) {
            if (Configuration.DEBUG_PRINT)
                System.out.println("[PRUNING] INCONSISTENT EXPLANATION!");
            return true;
        }
        return false;
    }

    @Override
    public boolean cannotAddExplanation(Explanation explanation, boolean extractModel) {
        if (extractModel){
            if (consistencyChecker.checkConsistencyWithModelExtraction())
                return false;
        } else {
            if (consistencyChecker.checkConsistency())
                return false;
        }

        if (Configuration.DEBUG_PRINT)
            System.out.println("[PRUNING] INCONSISTENT WITH ONTOLOGY!");

        explanationManager.addPossibleExplanation(explanation);
        return true;
    }
}
