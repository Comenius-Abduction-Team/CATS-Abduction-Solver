package sk.uniba.fmph.dai.cats.algorithms.data;

import java.util.Arrays;
import java.util.List;

public final class LubmS1Data extends LubmData {

    private LubmS1Data() {}

    private static final String NAME_PREFIX = "lubm_0_1_";
    private static final int REQUIRED_DEPTH_LIMIT = 1;

    public static final List<LubmInput> LUBM_INPUTS = Arrays.asList(

            new LubmInput(
                    NAME_PREFIX + 0,
                    ONT_PREFIX +
                            "Class: prefix0:Article " +
                            "Individual: prefix0:a " +
                            "Types: prefix0:Article",
                    3,
                    loadExpectedExplanations("1_0"),
                    REQUIRED_DEPTH_LIMIT
            ),

            new LubmInput(
                    NAME_PREFIX + 1,
                    ONT_PREFIX +
                            "Class: prefix0:AdministrativeStaff " +
                            "Individual: prefix0:a " +
                            "Types: prefix0:AdministrativeStaff",
                    2,
                    loadExpectedExplanations("1_1"),
                    REQUIRED_DEPTH_LIMIT
            ),

            new LubmInput(
                    NAME_PREFIX + 2,
                    ONT_PREFIX +
                            "Class: prefix0:Person " +
                            "Individual: prefix0:a " +
                            "Types: prefix0:Person",
                    20,
                    loadExpectedExplanations("1_2"),
                    REQUIRED_DEPTH_LIMIT
            ),

            new LubmInput(
                    NAME_PREFIX + 3,
                    ONT_PREFIX +
                            "Class: prefix0:Professor " +
                            "Individual: prefix0:a " +
                            "Types: prefix0:Professor",
                    6,
                    loadExpectedExplanations("1_3"),
                    REQUIRED_DEPTH_LIMIT
            ),

            new LubmInput(
                    NAME_PREFIX + 4,
                    ONT_PREFIX +
                            "Class: prefix0:Employee " +
                            "Individual: prefix0:a " +
                            "Types: prefix0:Employee",
                    15,
                    loadExpectedExplanations("1_4"),
                    REQUIRED_DEPTH_LIMIT
            ),

            new LubmInput(
                    NAME_PREFIX + 5,
                    ONT_PREFIX +
                            "Class: prefix0:Student " +
                            "Individual: prefix0:a " +
                            "Types: prefix0:Student",
                    2,
                    loadExpectedExplanations("1_5"),
                    REQUIRED_DEPTH_LIMIT
            ),

            new LubmInput(
                    NAME_PREFIX + 6,
                    ONT_PREFIX +
                            "Class: prefix0:Publication " +
                            "Individual: prefix0:a " +
                            "Types: prefix0:Publication",
                    9,
                    loadExpectedExplanations("1_6"),
                    REQUIRED_DEPTH_LIMIT
            ),

            new LubmInput(
                    NAME_PREFIX + 7,
                    ONT_PREFIX +
                            "Class: prefix0:Work " +
                            "Individual: prefix0:a " +
                            "Types: prefix0:Work",
                    3,
                    loadExpectedExplanations("1_7"),
                    REQUIRED_DEPTH_LIMIT
            ),

            new LubmInput(
                    NAME_PREFIX + 8,
                    ONT_PREFIX +
                            "Class: prefix0:Faculty " +
                            "Individual: prefix0:a " +
                            "Types: prefix0:Faculty",
                    9,
                    loadExpectedExplanations("1_8"),
                    REQUIRED_DEPTH_LIMIT
            ),

            new LubmInput(
                    NAME_PREFIX + 9,
                    ONT_PREFIX +
                            "Class: prefix0:Course " +
                            "Individual: prefix0:a " +
                            "Types: prefix0:Course",
                    1,
                    loadExpectedExplanations("1_9"),
                    REQUIRED_DEPTH_LIMIT
            )
    );
}