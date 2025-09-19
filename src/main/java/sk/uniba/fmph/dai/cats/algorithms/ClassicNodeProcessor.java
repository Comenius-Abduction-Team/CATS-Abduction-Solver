package sk.uniba.fmph.dai.cats.algorithms;

import sk.uniba.fmph.dai.cats.common.Configuration;
import sk.uniba.fmph.dai.cats.common.LogMessage;
import sk.uniba.fmph.dai.cats.data.Explanation;
import sk.uniba.fmph.dai.cats.data_processing.ExplanationManager;
import sk.uniba.fmph.dai.cats.events.Event;
import sk.uniba.fmph.dai.cats.events.EventPublisher;
import sk.uniba.fmph.dai.cats.events.EventType;
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
        if (!Configuration.MOVE_CHECKS_AFTER_MODEL_REUSE){
            if (!ruleChecker.isRelevant(explanation)) {
                stats.getCurrentLevelStats().explanationEdges += 1;
                EventPublisher.publishExplanationEvent(solver, EventType.IRELEVANT_EXPLANATION, explanation);
                return true;
            }
            if (!ruleChecker.isConsistent(explanation)) {
                stats.getCurrentLevelStats().explanationEdges += 1;
                EventPublisher.publishExplanationEvent(solver, EventType.INCONSISTENT_EXPLANATION, explanation);
                return true;
            }
        }
        return false;
    }

    @Override
    public int findExplanations(Explanation explanation, boolean extractModel) {

        if (consistencyChecker.checkOntologyConsistencyWithPath(extractModel, false))
            return 0;

        EventPublisher.publishGenericEvent(solver, EventType.EXPLANATION_EDGE);

        if (Configuration.MOVE_CHECKS_AFTER_MODEL_REUSE) {

            if (!ruleChecker.isRelevant(explanation)) {
                EventPublisher.publishExplanationEvent(solver, EventType.IRELEVANT_EXPLANATION, explanation);
                return 1;
            }
            if (!ruleChecker.isConsistent(explanation)) {
                EventPublisher.publishExplanationEvent(solver, EventType.INCONSISTENT_EXPLANATION, explanation);
                return 1;
            }
        }

        explanationManager.addPossibleExplanation(explanation);
        return 1;
    }

    @Override
    public boolean shouldCloseNode(int explanationsFound) {
        return explanationsFound > 0;
    }

    @Override
    public void postProcessExplanations() {
        explanationManager.finalisePossibleExplanations();
        explanationManager.groupFinalExplanationsBySize();
    }

    @Override
    public void storeAbduciblesIfNeeded(IAbducibleAxioms abducibles) {}
}
