package sk.uniba.fmph.dai.cats.algorithms.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

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

    public LubmInput(String id,
                     String observation,
                     int expectedCount,
                     String expectedExplanations
    ) {
        this.id = id;
        this.observation = observation;
        this.expectedExplanationCount = expectedCount;
        this.expectedExplanations = parseExpectedExplanations(expectedExplanations);
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

    private Set<Set<String>> parseExpectedExplanations(String expectedExplanationsString) {
        Set<Set<String>> expectedExplanations = new HashSet<>();

        for (String explanationString : expectedExplanationsString.replace(" ", "").split("\\R")) {
            explanationString = explanationString.replace("{", "")
                    .replace("}", "");

            Set<String> explanationSet = Arrays.stream(explanationString.split(","))
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toSet());

            expectedExplanations.add(explanationSet);
        }

        return expectedExplanations;
    }
}
