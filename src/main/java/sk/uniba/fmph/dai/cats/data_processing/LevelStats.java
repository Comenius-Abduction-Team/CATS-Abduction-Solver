package sk.uniba.fmph.dai.cats.data_processing;

import sk.uniba.fmph.dai.cats.common.StringFactory;

public class LevelStats {

    public int  processed_nodes, deleted_unprocessed, deleted_processed;
    public int created_edges;
    public int pruned_edges, explanation_edges, created_nodes, reused, modelExtractions, explanations, finalExplanations, consistencyChecks;
    public double start = -1, finish = -1;

    public Double firstExplanation, lastExplanation;

    @Override
    public String toString() {
        return "LevelStats{" +
                "processed_nodes=" + processed_nodes +
                ", rct_deleted_nodes=" + deleted_unprocessed +
                ", rct_retrospectively_deleted_nodes=" + deleted_processed +
                ", edges=" + created_edges +
                ", pruned_edges=" + pruned_edges +
                ", explanation_edges=" + explanation_edges +
                ", created_nodes=" + created_nodes +
                ", reused_model_labels=" + reused +
                ", extracted_models=" + modelExtractions +
                ", consistency_checks=" + consistencyChecks +
                ", explanations=" + explanations +
                ", finalExplanations=" + finalExplanations +
                ", start_time=" + start +
                ", first_explanation=" + firstExplanation +
                ", last_explanation=" + lastExplanation +
                ", finish_time=" + finish +

                "}\n";
    }

    public String toCsvRow(boolean addCommas){
        return StringFactory.buildCsvRow(addCommas,
                processed_nodes, deleted_unprocessed, deleted_processed,
                created_edges, pruned_edges, explanation_edges, created_nodes,
                reused, modelExtractions, consistencyChecks,
                explanations,finalExplanations,
                start, finish, finish-start, firstExplanation, lastExplanation);
    }
}
