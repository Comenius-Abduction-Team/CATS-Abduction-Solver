package sk.uniba.fmph.dai.cats.metrics;

import sk.uniba.fmph.dai.cats.algorithms.AlgorithmSolver;
import sk.uniba.fmph.dai.cats.events.*;

public class StatEventSubscriber implements IEventSubscriber {

    private final AlgorithmSolver solver;

    public StatEventSubscriber(AlgorithmSolver solver) {
        this.solver = solver;
    }

    @Override
    public void processEvent(Event event) {

        EventType type = event.getEventType();

        switch (type){
            case MAX_DEPTH_REACHED:
                solver.message = "max depth";
                break;
            case PROCESSING_NODE:
                trackProcessedNode(solver, (NodeEvent)event);
                break;
            case CHILDLESS_NODE:
                solver.currentLevel.childlessNodes += 1;
                break;
            case EDGE_CREATED:
                solver.currentLevel.createdEdges += 1;
                break;

            case INVALID_PATH:
            case EDGE_PRUNED:
                solver.currentLevel.prunedEdges += 1;
                break;
            case EXPLANATION_EDGE:
                solver.stats.getCurrentLevelStats().prunedEdges += 1;
                solver.stats.getCurrentLevelStats().explanationEdges += 1;
                break;

            case IRELEVANT_EXPLANATION:
            case INCONSISTENT_EXPLANATION:
                solver.stats.getCurrentLevelStats().originalExplanations += 1;
                solver.stats.getCurrentLevelStats().filteredExplanations += 1;

            case MODEL_REUSE:
                solver.currentLevel.reusedModels += 1;
                break;

            case LEVEL_FINISHED:
            case TREE_FINISHED:
                solver.currentLevel.memory = solver.metrics.measureAverageMemory();
                solver.currentLevel.finish = solver.metrics.getRunningTime();
                break;

            case LEVEL_STARTED:
                Level newLevel = solver.stats.getNewLevelStats(solver.currentDepth);
                solver.currentLevel = newLevel;
                newLevel.start = solver.metrics.getRunningTime();
                break;

            case MXP_CALL:
                solver.stats.getCurrentLevelStats().mxpCalls++;
                break;
            case QXP_CALL:
                solver.stats.getCurrentLevelStats().qxpCalls++;
                break;

            case CONSISTENCY_CHECK:
                solver.stats.getCurrentLevelStats().consistencyChecks += 1;
                break;
            case MODEL_EXTRACTION:
                solver.stats.getCurrentLevelStats().modelExtractions += 1;
                break;
        }

    }

    private void trackProcessedNode(AlgorithmSolver solver, NodeEvent event){
        if (!event.node.processed) {
            solver.currentLevel.processedNodes += 1;
        } else {
            solver.currentLevel.repeatedProcessing += 1;
        }
    }
}
