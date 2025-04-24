package sk.uniba.fmph.dai.cats.algorithms;

import sk.uniba.fmph.dai.cats.common.LogMessage;
import sk.uniba.fmph.dai.cats.common.StaticPrinter;
import sk.uniba.fmph.dai.cats.data.Explanation;
import sk.uniba.fmph.dai.cats.data_processing.ExplanationManager;
import sk.uniba.fmph.dai.cats.data_processing.TreeStats;

public class ClassicNodeProcessor implements NodeProcessor {

    private final RuleChecker ruleChecker;
    private final ConsistencyChecker consistencyChecker;
    private final ExplanationManager explanationManager;

    private final TreeStats stats;

    private final AlgorithmSolver solver;

    ClassicNodeProcessor(AlgorithmSolver solver){
        ruleChecker = solver.ruleChecker;
        consistencyChecker = solver.consistencyChecker;
        explanationManager = solver.explanationManager;
        stats = solver.stats;
        this.solver = solver;
    }

    @Override
    public boolean canCreateRoot(boolean extractModel) {
        boolean isConsistent = consistencyChecker.checkOntologyConsistency(extractModel);
        if (!isConsistent){
            solver.message = LogMessage.INFO_NOTHING_TO_EXPLAIN;
            solver.currentLevel.message = "nothing to explain";
        }
        return isConsistent;
    }

    @Override
    public boolean isInvalidExplanation(Explanation explanation) {
        if (!ruleChecker.isRelevant(explanation)) {
            StaticPrinter.debugPrint("[PRUNING] IRRELEVANT EXPLANATION!");
            return true;
        }
        if (!ruleChecker.isConsistent(explanation)) {
            StaticPrinter.debugPrint("[PRUNING] INCONSISTENT EXPLANATION!");
            return true;
        }
        return false;
    }

    @Override
    public boolean findExplanations(Explanation explanation, boolean extractModel) {
        if (consistencyChecker.checkOntologyConsistencyWithPath(extractModel, false))
            return false;
        StaticPrinter.debugPrint("[CLOSING] EXPLANATION FOUND!");
        explanationManager.addPossibleExplanation(explanation);
        stats.getCurrentLevelStats().prunedEdges += 1;
        stats.getCurrentLevelStats().explanationEdges += 1;

        return true;
    }

    @Override
    public void postProcessExplanations() {
        explanationManager.finalisePossibleExplanations();
        explanationManager.groupFinalExplanationsBySize();
    }
}
