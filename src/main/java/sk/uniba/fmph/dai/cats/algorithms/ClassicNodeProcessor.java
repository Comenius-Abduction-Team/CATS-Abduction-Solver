package sk.uniba.fmph.dai.cats.algorithms;

import sk.uniba.fmph.dai.cats.common.Configuration;
import sk.uniba.fmph.dai.cats.common.LogMessage;
import sk.uniba.fmph.dai.cats.common.StaticPrinter;
import sk.uniba.fmph.dai.cats.data.Explanation;
import sk.uniba.fmph.dai.cats.data_processing.ExplanationManager;
import sk.uniba.fmph.dai.cats.metrics.TreeStats;

public class ClassicNodeProcessor implements INodeProcessor {

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
    public boolean shouldPruneBranch(Explanation explanation) {
        if (!Configuration.optimisations.contains(Optimisation.MOVE_CONSISTENCY_CHECKS)){
            if (!ruleChecker.isRelevant(explanation)) {
                stats.getCurrentLevelStats().originalExplanations += 1;
                stats.getCurrentLevelStats().filteredExplanations += 1;
                stats.getCurrentLevelStats().explanationEdges += 1;
                StaticPrinter.debugPrint("[PRUNING] IRRELEVANT EXPLANATION!");
                return true;
            }
            if (!ruleChecker.isConsistent(explanation)) {
                stats.getCurrentLevelStats().originalExplanations += 1;
                stats.getCurrentLevelStats().filteredExplanations += 1;
                stats.getCurrentLevelStats().explanationEdges += 1;
                StaticPrinter.debugPrint("[PRUNING] INCONSISTENT PATH!");
                return true;
            }
        }
        return false;
    }

    @Override
    public int findExplanations(Explanation explanation, boolean extractModel) {

        if (consistencyChecker.checkOntologyConsistencyWithPath(extractModel, false))
            return 0;

        stats.getCurrentLevelStats().prunedEdges += 1;
        stats.getCurrentLevelStats().explanationEdges += 1;

        if (Configuration.optimisations.contains(Optimisation.MOVE_CONSISTENCY_CHECKS)) {

            if (!ruleChecker.isRelevant(explanation)) {
                StaticPrinter.debugPrint("[FILTERING] IRRELEVANT EXPLANATION!");
                stats.getCurrentLevelStats().originalExplanations += 1;
                stats.getCurrentLevelStats().filteredExplanations += 1;
                return 1;
            }
            if (!ruleChecker.isConsistent(explanation)) {
                StaticPrinter.debugPrint("[FILTERING] INCONSISTENT EXPLANATION!");
                stats.getCurrentLevelStats().originalExplanations += 1;
                stats.getCurrentLevelStats().filteredExplanations += 1;
                return 1;
            }
        }

        explanationManager.addPossibleExplanation(explanation);
        return 1;
    }

    @Override
    public boolean shouldCloseNode(int explanationsFound) {
        boolean result = explanationsFound > 0;
        if (result)
            StaticPrinter.debugPrint("[CLOSING] EXPLANATION FOUND!");
        return result;
    }

    @Override
    public void postProcessExplanations() {
        explanationManager.finalisePossibleExplanations();
        explanationManager.groupFinalExplanationsBySize();
    }

    @Override
    public void storeAbduciblesIfNeeded(IAbducibleAxioms abducibles) {}
}
