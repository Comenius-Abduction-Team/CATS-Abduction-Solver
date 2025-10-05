package sk.uniba.fmph.dai.cats.events;

import sk.uniba.fmph.dai.cats.algorithms.AlgorithmSolver;
import sk.uniba.fmph.dai.cats.common.StaticPrinter;
import sk.uniba.fmph.dai.cats.common.StringFactory;
import sk.uniba.fmph.dai.cats.data.Explanation;

public class DebugPrintEventSubscriber implements IEventSubscriber {

    private final AlgorithmSolver solver;

    public DebugPrintEventSubscriber(AlgorithmSolver solver) {
        this.solver = solver;
    }

    @Override
    public void processEvent(Event event) {

        EventType type = event.getEventType();

        switch (type) {
            case ROOT_NOT_CREATED:
                StaticPrinter.debugPrint("[!!!] ROOT COULD NOT BE CREATED!");
                break;
            case PROCESSING_NODE:
                StaticPrinter.debugPrint("\n*********\n" + "[TREE] PROCESSING node: " + ((NodeEvent)event).node);
                break;
            case CHILDLESS_NODE:
                StaticPrinter.debugPrint("[TREE] NO CHILDREN TO ITERATE!");
                break;
            case EDGE_CREATED:
                StaticPrinter.debugPrint("[TREE] TRYING EDGE: " + StringFactory.getRepresentation(((EdgeEvent)event).branchLabel));
                break;
            case INVALID_PATH:
                StaticPrinter.debugPrint("[PRUNING] INVALID PATH!");
                break;
            case EDGE_PRUNED:
                StaticPrinter.debugPrint("[PRUNING] PRUNED PATH!");
                break;
            case IRELEVANT_EXPLANATION:
                StaticPrinter.debugPrint("[FILTERING] IRRELEVANT EXPLANATION: " + ((ExplanationEvent)event).explanation);
                break;
            case INCONSISTENT_EXPLANATION:
                StaticPrinter.debugPrint("[FILTERING] INCONSISTENT EXPLANATION: " + ((ExplanationEvent)event).explanation);
                break;
            case POSSIBLE_EXPLANATION:
                Explanation explanation = ((ExplanationEvent)event).explanation;
                StaticPrinter.debugPrint("[EXPLANATION] " + explanation + " at time: " + explanation.getAcquireTime() );
                break;
            case MODEL_REUSE:
                StaticPrinter.debugPrint("[MODEL] Model was reused.");
                break;
            case NODE_CREATED:
                StaticPrinter.debugPrint("[TREE] Created node: " + ((NodeEvent)event).node);
                break;
            case CLOSING_NODE:
                StaticPrinter.debugPrint("[TREE] Closing node: " + ((NodeEvent)event).node);
                break;
            case TREE_FINISHED:
                StaticPrinter.debugPrint("[TREE] Finished iterating the tree.");
                break;
            case LEVEL_STARTED:
                StaticPrinter.debugPrint("[TREE] entering depth " + solver.currentLevel.depth);
                break;
        }

    }
}
