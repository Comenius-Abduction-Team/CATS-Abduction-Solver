package sk.uniba.fmph.dai.cats.algorithms.data;

import java.util.Arrays;
import java.util.List;

public final class LubmS5Data extends LubmData {

    private LubmS5Data() {}

    private static final String NAME_PREFIX = "lubm_0_5_";
    private static final int REQUIRED_DEPTH_LIMIT = 6;

    public static final List<LubmInput> LUBM_INPUTS = Arrays.asList(

            new LubmInput(
                    NAME_PREFIX + 0,
                    ONT_PREFIX +
                            "Class: prefix0:Organization " +
                            "Class: prefix0:Employee " +
                            "Class: prefix0:Student " +
                            "Class: prefix0:Article " +
                            "Class: prefix0:Course " +
                            "Individual: prefix0:a " +
                            "Types: prefix0:Organization and prefix0:Employee " +
                            "and prefix0:Student and prefix0:Article " +
                            "and prefix0:Course",
                    2687,
                    loadExpectedExplanations("5_0"),
                    REQUIRED_DEPTH_LIMIT
            ),

            new LubmInput(
                    NAME_PREFIX + 1,
                    ONT_PREFIX +
                            "Class: prefix0:Organization " +
                            "Class: prefix0:Work " +
                            "Class: prefix0:Student " +
                            "Class: prefix0:AdministrativeStaff " +
                            "Class: prefix0:Faculty " +
                            "Individual: prefix0:a " +
                            "Types: prefix0:Organization and prefix0:Work " +
                            "and prefix0:Student " +
                            "and prefix0:AdministrativeStaff and prefix0:Faculty",
                    2519,
                    loadExpectedExplanations("5_1"),
                    REQUIRED_DEPTH_LIMIT
            ),

            new LubmInput(
                    NAME_PREFIX + 2,
                    ONT_PREFIX +
                            "Class: prefix0:Organization " +
                            "Class: prefix0:Work " +
                            "Class: prefix0:Article " +
                            "Class: prefix0:AdministrativeStaff " +
                            "Class: prefix0:Professor " +
                            "Individual: prefix0:a " +
                            "Types: prefix0:Organization and prefix0:Work " +
                            "and prefix0:Article " +
                            "and prefix0:AdministrativeStaff and prefix0:Professor",
                    2351,
                    loadExpectedExplanations("5_2"),
                    REQUIRED_DEPTH_LIMIT
            ),

            new LubmInput(
                    NAME_PREFIX + 3,
                    ONT_PREFIX +
                            "Class: prefix0:Organization " +
                            "Class: prefix0:Article " +
                            "Class: prefix0:Course " +
                            "Class: prefix0:AdministrativeStaff " +
                            "Class: prefix0:Faculty " +
                            "Individual: prefix0:a " +
                            "Types: prefix0:Organization and prefix0:Article " +
                            "and prefix0:Course " +
                            "and prefix0:AdministrativeStaff and prefix0:Faculty",
                    1679,
                    loadExpectedExplanations("5_3"),
                    REQUIRED_DEPTH_LIMIT
            ),

            new LubmInput(
                    NAME_PREFIX + 4,
                    ONT_PREFIX +
                            "Class: prefix0:Organization " +
                            "Class: prefix0:Student " +
                            "Class: prefix0:Article " +
                            "Class: prefix0:Course " +
                            "Class: prefix0:Faculty " +
                            "Individual: prefix0:a " +
                            "Types: prefix0:Organization and prefix0:Student " +
                            "and prefix0:Article and prefix0:Course " +
                            "and prefix0:Faculty",
                    1679,
                    loadExpectedExplanations("5_4"),
                    REQUIRED_DEPTH_LIMIT
            ),

            new LubmInput(
                    NAME_PREFIX + 5,
                    ONT_PREFIX +
                            "Class: prefix0:Publication " +
                            "Class: prefix0:Work " +
                            "Class: prefix0:Student " +
                            "Class: prefix0:AdministrativeStaff " +
                            "Class: prefix0:Faculty " +
                            "Individual: prefix0:a " +
                            "Types: prefix0:Publication and prefix0:Work and " +
                            "prefix0:Student " +
                            "and prefix0:AdministrativeStaff and prefix0:Faculty",
                    3599,
                    loadExpectedExplanations("5_5"),
                    REQUIRED_DEPTH_LIMIT
            ),

            new LubmInput(
                    NAME_PREFIX + 6,
                    ONT_PREFIX +
                            "Class: prefix0:Organization " +
                            "Class: prefix0:Article " +
                            "Class: prefix0:Course " +
                            "Class: prefix0:AdministrativeStaff " +
                            "Class: prefix0:Professor " +
                            "Individual: prefix0:a " +
                            "Types: prefix0:Organization and prefix0:Article " +
                            "and prefix0:Course " +
                            "and prefix0:AdministrativeStaff and prefix0:Professor",
                    1175,
                    loadExpectedExplanations("5_6"),
                    REQUIRED_DEPTH_LIMIT
            ),

            new LubmInput(
                    NAME_PREFIX + 7,
                    ONT_PREFIX +
                            "Class: prefix0:Organization " +
                            "Class: prefix0:Publication " +
                            "Class: prefix0:Employee " +
                            "Class: prefix0:Student " +
                            "Class: prefix0:Course " +
                            "Individual: prefix0:a " +
                            "Types: prefix0:Organization and prefix0:Publication " +
                            "and prefix0:Employee " +
                            "and prefix0:Student and prefix0:Course",
                    6719,
                    loadExpectedExplanations("5_7"),
                    REQUIRED_DEPTH_LIMIT
            ),

            new LubmInput(
                    NAME_PREFIX + 8,
                    ONT_PREFIX +
                            "Class: prefix0:Organization " +
                            "Class: prefix0:Work " +
                            "Class: prefix0:Article " +
                            "Class: prefix0:AdministrativeStaff " +
                            "Class: prefix0:Faculty " +
                            "Individual: prefix0:a " +
                            "Types: prefix0:Organization and prefix0:Work " +
                            "and prefix0:Article " +
                            "and prefix0:AdministrativeStaff and prefix0:Faculty",
                    3359,
                    loadExpectedExplanations("5_8"),
                    REQUIRED_DEPTH_LIMIT
            ),

            new LubmInput(
                    NAME_PREFIX + 9,
                    ONT_PREFIX +
                            "Class: prefix0:Student " +
                            "Class: prefix0:Article " +
                            "Class: prefix0:Course " +
                            "Class: prefix0:AdministrativeStaff " +
                            "Class: prefix0:Professor " +
                            "Individual: prefix0:a " +
                            "Types: prefix0:Student and prefix0:Article " +
                            "and prefix0:Course " +
                            "and prefix0:AdministrativeStaff and prefix0:Professor",
                    503,
                    loadExpectedExplanations("5_9"),
                    REQUIRED_DEPTH_LIMIT
            )
    );
}