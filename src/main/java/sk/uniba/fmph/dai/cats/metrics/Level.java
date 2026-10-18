package sk.uniba.fmph.dai.cats.metrics;

import sk.uniba.fmph.dai.cats.common.StringFactory;
import sk.uniba.fmph.dai.cats.data.Explanation;

import java.util.ArrayList;
import java.util.List;

/**
 * A class that stores statistics about a single level in the HS tree.
 */
public class Level {

    /// /// /// GENERAL STATS
    /**
     * The depth of the level. Usually, there is one level per depth in a tree. However, in RCT, we consider returning
     * to a depth that had already been processed before as a new "level".
     * **/
    public int depth;

    /**
     * Average memory used by the application during the computation of this level, measured in short time intervals.
     * **/
    public double memory;

    /// /// /// NODES
    /**
     * Number of nodes that have been processed in this level.
     * **/
    public int processedNodes;

    /**
     * Number of nodes that have been processed, but had no child edges.
     * **/
    public int childlessNodes;

    /**
     * Number of new nodes that have been created (on the child edges of the nodes that have been processed in this level).
     * **/
    public int createdNodes;

    /**
     * RCT – Number of nodes that have been processed again, after they had already been processed in the previous levels.
     * This can occur if a new child edge is created under a node by RCT after it had been processed for the first time.
     * **/
    public int repeatedProcessing;

    /**
     * RCT/HS-DAG – Number of nodes that have been deleted after they had already been processed.
     * **/
    public int deletedProcessed;

    /**
     * RCT/HS-DAG – Number of nodes that have been deleted before they could be processed.
     * **/
    public int deletedUnprocessed;

    /**
     * HS-DAG – Number of node pairs that have been merged into one node.
     * **/
    public int mergedNodes;

    /// /// /// EDGES
    /**
     * Number of candidate child edges generated and examined in this level.
     * An edge is counted here before it is classified as invalid, pruned, an explanation edge,
     * or continued into a child node.
     * **/
    public int createdEdges;

    /**
     * Number of generated child edges rejected because they would create an invalid path
     * (e.g. a path containing complementary axioms or the observation itself).
     * Invalid paths are tracked separately from algorithmic pruning.
     * **/
    public int invalidPaths;

    /**
     * Number of valid candidate child edges whose branches were cut by an algorithmic pruning condition
     * (e.g. duplicate path or a path that contains an already known explanation).
     * Invalid paths, explanation edges, and explanations rejected by filtering conditions are not included.
     * **/
    public int prunedEdges;

    /**
     * Number of child edges on which the current path was identified as an explanation.
     * The explanation may subsequently be rejected by explanation filtering.
     */
    public int explanationEdges;

    /// /// /// MODELS AND REASONING
    /**
     * Number of times a model has been reused.
     * **/
    public int reusedModels;

    /**
     * Number of times model extraction was executed.
     * **/
    public int modelExtractions;

    /**
     * Number of new unique models added to the model store during this level.
     * Since models are not removed from the store, the total number stored at the end of a level
     * can be obtained as the cumulative sum of this value.
     * **/
    public int storedModels;

    /**
     * Number of times a consistency check of any kind has been called.
     * **/
    public int consistencyChecks;

    /**
     * Number of times the QXP procedure has been called.
     * **/
    public int qxpCalls;

    /**
     * Number of times MXP procedure has been called.
     * **/
    public int mxpCalls;

    /// /// /// EXPLANATIONS
    /**
     * Number of unique explanations that have been found (may include undesirable ones if the filtering is done during post-processing).
     * **/
    public int possibleExplanations;

    /**
     * Number of candidate explanations rejected by explanation filtering conditions
     * (irrelevance, inconsistency, or non-minimality).
     *
     * Filtering can happen either during the tree search or during final post-processing (level "f").
     * **/
    public int filteredExplanations;

    /**
     * Number of desirable explanations left after filtering.
     * **/
    public int finalExplanationsCount;

    /**
     * The desirable explanations left after filtering.
     * **/
    final List<Explanation> finalExplanations = new ArrayList<>();

    /// /// /// TIMES (in seconds since the process started)
    /**
     * The time when this level started.
     * **/
    public double start = -1;

    /**
     * The time when this level finished.
     * **/
    public double finish = -1;

    /**
     * The time when the first desirable explanation was found in this level.
     * **/
    public Double firstExplanationTime;

    /**
     * The time when the last desirable explanation was found in this level.
     * **/
    public Double lastExplanationTime;

    /// /// /// INFO
    /**
     * If this level crashed on a catchable error/exception.
     * **/
    public boolean error=false;

    /**
     * The message from a catchable error/exception if this level crashed on it.
     * **/
    public String errorMessage;

    /**
     * Information about other exceptional states that ocurred in this level.
     * **/
    public String message = "";

    /// /// /// OTHERS
    /**
     * Value of the global MIN used in HST at the end of this level.
     * **/
    public int hstGlobalMin;

    public Level(int depth){
        this.depth = depth;
    }

    public void addFinalExplanation(Explanation explanation){
        finalExplanations.add(explanation);
    }

    @Override
    public String toString() {
        return "LevelStats{" +
                "processedNodes=" + processedNodes +
                ", rctDeletedNodes=" + deletedUnprocessed +
                ", rctDeletedProcessedNodes=" + deletedProcessed +
                ", edges=" + createdEdges +
                ", invalidPaths=" + invalidPaths +
                ", prunedEdges=" + prunedEdges +
                ", hs-dagMergedNodes=" + mergedNodes +
                ", explanationEdges=" + explanationEdges +
                ", createdNodes=" + createdNodes +
                ", reusedModelLabels=" + reusedModels +
                ", extractedModels=" + modelExtractions +
                ", consistencyChecks=" + consistencyChecks +
                ", possibleExplanations=" + possibleExplanations +
                ", finalExplanations=" + finalExplanationsCount +
                ", startTime=" + start +
                ", firstExplanation=" + firstExplanationTime +
                ", lastExplanation=" + lastExplanationTime +
                ", finishTime=" + finish +
                ", message=" + message +
                ", error=" + error +
                ", errorMessage=" + errorMessage +

                "}\n";
    }

    public void buildCsvRow(StringBuilder builder, boolean addCommas){
        StringFactory.buildCsvRow(builder, addCommas,
                processedNodes,
                childlessNodes,
                repeatedProcessing,
                deletedProcessed,
                createdEdges,
                invalidPaths,
                prunedEdges,
                mergedNodes,
                explanationEdges,
                createdNodes,
                deletedUnprocessed,
                reusedModels,
                modelExtractions,
                storedModels,
                consistencyChecks,
                qxpCalls,
                mxpCalls,
                hstGlobalMin,
                possibleExplanations,
                filteredExplanations,
                finalExplanationsCount,
                memory,
                start,
                finish,
                finish-start,
                firstExplanationTime,
                lastExplanationTime,
                message,
                (error ? "error" : null),
                errorMessage
        );
    }
}
