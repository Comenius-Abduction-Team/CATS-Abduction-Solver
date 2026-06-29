package sk.uniba.fmph.dai.cats.algorithms.data;

import java.util.Arrays;
import java.util.List;

public final class LubmS2Data {

    private LubmS2Data() {}

    private static final String ONT_PREFIX =
            "Prefix: prefix0: <http://swat.cse.lehigh.edu/onto/univ-bench.owl#>\n";

    private static final String NAME_PREFIX = "lubm_0_2_";

    public static final Integer DEPTH_LIMIT = 4;

    public static final List<LubmInput> LUBM_INPUTS = Arrays.asList(

            new LubmInput(
                    NAME_PREFIX + 0,
                    ONT_PREFIX +
                            "Class: prefix0:Publication " +
                            "Class: prefix0:Employee " +
                            "Individual: prefix0:a " +
                            "Types: prefix0:Publication and prefix0:Employee",
                    159
            ),

            new LubmInput(
                    NAME_PREFIX + 1,
                    ONT_PREFIX +
                            "Class: prefix0:Work " +
                            "Class: prefix0:Employee " +
                            "Individual: prefix0:a " +
                            "Types: prefix0:Work and prefix0:Employee",
                    63
            ),

            new LubmInput(
                    NAME_PREFIX + 2,
                    ONT_PREFIX +
                            "Class: prefix0:Person " +
                            "Class: prefix0:Article " +
                            "Individual: prefix0:a " +
                            "Types: prefix0:Person and prefix0:Article",
                    83
            ),

            new LubmInput(
                    NAME_PREFIX + 3,
                    ONT_PREFIX +
                            "Class: prefix0:Organization " +
                            "Class: prefix0:Publication " +
                            "Individual: prefix0:a " +
                            "Types: prefix0:Organization and prefix0:Publication",
                    69
            ),

            new LubmInput(
                    NAME_PREFIX + 4,
                    ONT_PREFIX +
                            "Class: prefix0:Publication " +
                            "Class: prefix0:Faculty " +
                            "Individual: prefix0:a " +
                            "Types: prefix0:Publication and prefix0:Faculty",
                    99
            ),

            new LubmInput(
                    NAME_PREFIX + 5,
                    ONT_PREFIX +
                            "Class: prefix0:Organization " +
                            "Class: prefix0:Employee " +
                            "Individual: prefix0:a " +
                            "Types: prefix0:Organization and prefix0:Employee",
                    111
            ),

            new LubmInput(
                    NAME_PREFIX + 6,
                    ONT_PREFIX +
                            "Class: prefix0:Organization " +
                            "Class: prefix0:Faculty " +
                            "Individual: prefix0:a " +
                            "Types: prefix0:Organization and prefix0:Faculty",
                    69
            ),

            new LubmInput(
                    NAME_PREFIX + 7,
                    ONT_PREFIX +
                            "Class: prefix0:Student " +
                            "Class: prefix0:AdministrativeStaff " +
                            "Individual: prefix0:a " +
                            "Types: prefix0:Student and prefix0:AdministrativeStaff",
                    8
            ),

            new LubmInput(
                    NAME_PREFIX + 8,
                    ONT_PREFIX +
                            "Class: prefix0:Course " +
                            "Class: prefix0:Faculty " +
                            "Individual: prefix0:a " +
                            "Types: prefix0:Course and prefix0:Faculty",
                    19
            ),

            new LubmInput(
                    NAME_PREFIX + 9,
                    ONT_PREFIX +
                            "Class: prefix0:Work " +
                            "Class: prefix0:Article " +
                            "Individual: prefix0:a " +
                            "Types: prefix0:Work and prefix0:Article",
                    15
            )
    );
}