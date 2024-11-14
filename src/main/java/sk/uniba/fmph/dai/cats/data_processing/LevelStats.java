package sk.uniba.fmph.dai.cats.data_processing;

public class LevelStats {

    public int created, deleted, pruned, reused, branchingFactor, modelExtractions, consistencyChecks;
    public double start, finish, firstExplanation, lastExplanation;

    @Override
    public String toString() {
        return "LevelStats{" +
                "created=" + created +
                ", reused=" + reused +
                ", deleted=" + deleted +
                ", pruned=" + pruned +
                ", model_extractions=" + modelExtractions +
                ", start=" + start +
                ", finish=" + finish +
                "}\n";
    }
}
