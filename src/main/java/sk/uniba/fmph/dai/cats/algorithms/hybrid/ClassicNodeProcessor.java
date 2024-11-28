package sk.uniba.fmph.dai.cats.algorithms.hybrid;

import sk.uniba.fmph.dai.cats.common.Configuration;
import sk.uniba.fmph.dai.cats.data.Explanation;
import sk.uniba.fmph.dai.cats.data_processing.ExplanationManager;
import sk.uniba.fmph.dai.cats.data_processing.TreeStats;

public class ClassicNodeProcessor implements NodeProcessor {

    private final RuleChecker ruleChecker;
    private final ConsistencyChecker consistencyChecker;
    private final ExplanationManager explanationManager;

    private final TreeStats stats;

    ClassicNodeProcessor(AlgorithmSolver solver){
        ruleChecker = solver.ruleChecker;
        consistencyChecker = solver.consistencyChecker;
        explanationManager = solver.explanationManager;
        stats = solver.stats;
    }

    @Override
    public boolean canCreateRoot(boolean extractModel) {
        return consistencyChecker.checkOntologyConsistency(extractModel);
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
    public boolean findExplanations(Explanation explanation, boolean extractModel) {
        if (consistencyChecker.checkOntologyConsistencyWithPath(extractModel))
            return false;
        if (Configuration.DEBUG_PRINT)
            System.out.println("[CLOSING] EXPLANATION FOUND!");
        explanationManager.addPossibleExplanation(explanation);
        stats.getCurrentLevelStats().explanation_edges += 1;

        return true;
    }
}
