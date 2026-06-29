package sk.uniba.fmph.dai.cats.algorithms.data;

import java.util.HashSet;
import java.util.Set;

public class LubmInput {

    private final String id;
    private final String observation;
    private final int expectedExplanationCount;

    private final Set<Set<String>> expectedExplanations;

    public LubmInput(String id,
                     String observation,
                     int expectedCount) {
        this.id = id;
        this.observation = observation;
        this.expectedExplanationCount = expectedCount;
        this.expectedExplanations = new HashSet<>();
    }

    public LubmInput(String id,
                     String observation,
                     int expectedCount,
                     Set<Set<String>> expectedExplanations
                     ) {
        this.id = id;
        this.observation = observation;
        this.expectedExplanationCount = expectedCount;
        this.expectedExplanations = expectedExplanations;
    }

    public String getId() {
        return id;
    }

    public String getObservation() {
        return observation;
    }

    public int getExpectedExplanationCount() {
        return expectedExplanationCount;
    }

    public Set<Set<String>> getExpectedExplanations() {
        return expectedExplanations;
    }
}
