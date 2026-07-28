package sk.uniba.fmph.dai.cats.algorithms.data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

abstract public class LubmData {

    private static final String PATH_TO_RESOURCES =
            "src/test/java/sk/uniba/fmph/dai/cats/algorithms/lubm/resources/expected";

    protected static final String ONT_PREFIX =
            "Prefix: prefix0: <http://swat.cse.lehigh.edu/onto/univ-bench.owl#>\n";

    protected static Set<Set<String>> loadExpectedExplanations(String inputIndex) {
        try {
            return Files.lines(
                    Paths.get( PATH_TO_RESOURCES + inputIndex + ".txt"))
                    .map(line -> line.replace("{", "")
                            .replace("}", "")
                            .replace(" ", ""))
                    .filter(line -> !line.isEmpty())
                    .map(line -> Arrays.stream(line.split(","))
                            .collect(Collectors.toSet()))
                    .collect(Collectors.toSet());

        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

}
