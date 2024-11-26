package sk.uniba.fmph.dai.cats.data_processing;

public class LevelStats {

    public int  processed_nodes, deleted_unprocessed, deleted_processed;
    public int created_edges;
    public int pruned_edges, explanation_edges, created_nodes, reused, modelExtractions, explanations;
    public double start, finish, firstExplanation, lastExplanation;

    @Override
    public String toString() {
        return "LevelStats{" +
                "processed_nodes=" + processed_nodes +
                ", rct_deleted_nodes=" + deleted_unprocessed +
                ", rct_retrospectively_deleted_nodes=" + deleted_processed +
                ", edges=" + created_edges +
                ", pruned_edges=" + pruned_edges +
                ", explanations=" + explanation_edges +
                ", created_nodes=" + created_nodes +
                ", reused_model_labels=" + reused +
                ", extracted_models=" + modelExtractions +
                ", start_time=" + start +
                ", finish_time=" + finish +

                "}\n";
    }
}
