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
     * Number of child edges that have been created.
     * **/
    public int createdEdges;

    //TODO pomaha nam nejak zaratavat explanation edges ako pruned? asi by bolo krajsie ich oddelit
    //TODO pozriet ci sa pruned vola vsade kde sa ma
    /**
     * Number of child edges that have been pruned by the 1st and 2nd pruning condition OR closed as an explanation.
     * **/
    public int prunedEdges;

    /**
     * Number of child edges that have been closed because the paths that they are concluding are valid explanations.
     * **/
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

    //TODO S TYMTO SA NIC NEROBI, JE TO V TABULKE ALE REALNE NETRACKUJEME ZIADEN UDAJ
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
     * Number of all explanations that have been found, including undesirable ones.
     * **/
    public int originalExplanations;

    /**
     * Number of undesirable explanations that have been found filtered out.
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
                "processed_nodes=" + processedNodes +
                ", rct_deleted_nodes=" + deletedUnprocessed +
                ", rct_retrospectively_deleted_nodes=" + deletedProcessed +
                ", edges=" + createdEdges +
                ", pruned_edges=" + prunedEdges +
                ", merged_nodes=" + mergedNodes +
                ", explanation_edges=" + explanationEdges +
                ", created_nodes=" + createdNodes +
                ", reused_model_labels=" + reusedModels +
                ", extracted_models=" + modelExtractions +
                ", consistency_checks=" + consistencyChecks +
                ", explanations=" + originalExplanations +
                ", finalExplanations=" + finalExplanationsCount +
                ", start_time=" + start +
                ", first_explanation=" + firstExplanationTime +
                ", last_explanation=" + lastExplanationTime +
                ", finish_time=" + finish +
                ", message=" + message +
                ", error=" + error +
                ", error_message=" + errorMessage +

                "}\n";
    }

    public void buildCsvRow(StringBuilder builder, boolean addCommas){
        StringFactory.buildCsvRow(builder, addCommas,
                processedNodes, childlessNodes, repeatedProcessing, deletedProcessed,
                createdEdges, prunedEdges-explanationEdges, mergedNodes, explanationEdges, createdNodes, deletedUnprocessed,
                reusedModels, modelExtractions, storedModels, consistencyChecks, qxpCalls, mxpCalls,
                hstGlobalMin,
                originalExplanations, filteredExplanations, finalExplanationsCount,
                memory,
                start, finish, finish-start, firstExplanationTime, lastExplanationTime,
                message, (error ? "error" : null), errorMessage
        );
    }
}
