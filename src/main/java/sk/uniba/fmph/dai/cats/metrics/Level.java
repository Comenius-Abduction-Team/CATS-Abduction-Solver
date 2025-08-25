package sk.uniba.fmph.dai.cats.metrics;

import sk.uniba.fmph.dai.cats.common.StringFactory;
import sk.uniba.fmph.dai.cats.data.Explanation;

import java.util.ArrayList;
import java.util.List;

public class Level {

    public int depth;

    public int processedNodes, childlessNodes, repeatedProcessing, deletedProcessed;
    public int createdEdges, prunedEdges, explanationEdges, createdNodes, deletedCreated;
    public int reusedModels, modelExtractions, storedModels, consistencyChecks;

    public int qxpCalls, mxpCalls;

    public int hstGlobalMin;
    public int originalExplanations, filteredExplanations, finalExplanations;

    public double memory;

    public double start = -1, finish = -1;

    public Double firstExplanationTime, lastExplanationTime;

    public boolean error=false;

    public String errorMessage, message;

    final List<Explanation> explanations = new ArrayList<>();

    public Level(int depth){
        this.depth = depth;
    }

    public void addFinalExplanation(Explanation explanation){
        explanations.add(explanation);
    }

    @Override
    public String toString() {
        return "LevelStats{" +
                "processed_nodes=" + processedNodes +
                ", rct_deleted_nodes=" + deletedCreated +
                ", rct_retrospectively_deleted_nodes=" + deletedProcessed +
                ", edges=" + createdEdges +
                ", pruned_edges=" + prunedEdges +
                ", explanation_edges=" + explanationEdges +
                ", created_nodes=" + createdNodes +
                ", reused_model_labels=" + reusedModels +
                ", extracted_models=" + modelExtractions +
                ", consistency_checks=" + consistencyChecks +
                ", explanations=" + originalExplanations +
                ", finalExplanations=" + finalExplanations +
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
                createdEdges, prunedEdges-explanationEdges, explanationEdges, createdNodes, deletedCreated,
                reusedModels, modelExtractions, storedModels, consistencyChecks, qxpCalls, mxpCalls,
                hstGlobalMin,
                originalExplanations, filteredExplanations, finalExplanations,
                memory,
                start, finish, finish-start, firstExplanationTime, lastExplanationTime,
                message, (error ? "error" : null), errorMessage
        );
    }
}
