package sk.uniba.fmph.dai.cats.algorithms.data;

import java.util.Arrays;
import java.util.List;

public final class LubmS4Data extends LubmData {

    private LubmS4Data() {}

    private static final String NAME_PREFIX = "lubm_0_4_";
    private static final int REQUIRED_DEPTH_LIMIT = 5;

    public static final List<LubmInput> LUBM_INPUTS = Arrays.asList(

            new LubmInput(
                    NAME_PREFIX + 0,
                    ONT_PREFIX +
                            "Class: prefix0:Organization " +
                            "Class: prefix0:Student " +
                            "Class: prefix0:Course " +
                            "Class: prefix0:Professor " +
                            "Individual: prefix0:a " +
                            "Types: prefix0:Organization and prefix0:Student " +
                            "and prefix0:Course and prefix0:Professor",
                    293,
                    loadExpectedExplanations("4_0"),
                    REQUIRED_DEPTH_LIMIT
            ),

            new LubmInput(
                    NAME_PREFIX + 1,
                    ONT_PREFIX +
                            "Class: prefix0:Publication " +
                            "Class: prefix0:Work " +
                            "Class: prefix0:Student " +
                            "Class: prefix0:AdministrativeStaff " +
                            "Individual: prefix0:a " +
                            "Types: prefix0:Publication and prefix0:Work " +
                            "and prefix0:Student and prefix0:AdministrativeStaff",
                    359,
                    loadExpectedExplanations("4_1"),
                    REQUIRED_DEPTH_LIMIT
            ),

            new LubmInput(
                    NAME_PREFIX + 2,
                    ONT_PREFIX +
                            "Class: prefix0:Organization " +
                            "Class: prefix0:Work " +
                            "Class: prefix0:Student " +
                            "Class: prefix0:AdministrativeStaff " +
                            "Individual: prefix0:a " +
                            "Types: prefix0:Organization and prefix0:Work " +
                            "and prefix0:Student and prefix0:AdministrativeStaff",
                    251,
                    loadExpectedExplanations("4_2"),
                    REQUIRED_DEPTH_LIMIT
            ),

            new LubmInput(
                    NAME_PREFIX + 3,
                    ONT_PREFIX +
                            "Class: prefix0:Publication " +
                            "Class: prefix0:Work " +
                            "Class: prefix0:Student " +
                            "Class: prefix0:Professor " +
                            "Individual: prefix0:a " +
                            "Types: prefix0:Publication and prefix0:Work " +
                            "and prefix0:Student and prefix0:Professor",
                    839,
                    loadExpectedExplanations("4_3"),
                    REQUIRED_DEPTH_LIMIT
            ),

            new LubmInput(
                    NAME_PREFIX + 4,
                    ONT_PREFIX +
                            "Class: prefix0:Publication " +
                            "Class: prefix0:Course " +
                            "Class: prefix0:AdministrativeStaff " +
                            "Class: prefix0:Professor " +
                            "Individual: prefix0:a " +
                            "Types: prefix0:Publication and prefix0:Course " +
                            "and prefix0:AdministrativeStaff and prefix0:Professor",
                    419,
                    loadExpectedExplanations("4_4"),
                    REQUIRED_DEPTH_LIMIT
            ),

            new LubmInput(
                    NAME_PREFIX + 5,
                    ONT_PREFIX +
                            "Class: prefix0:Work " +
                            "Class: prefix0:Student " +
                            "Class: prefix0:AdministrativeStaff " +
                            "Class: prefix0:Professor " +
                            "Individual: prefix0:a " +
                            "Types: prefix0:Work and prefix0:Student " +
                            "and prefix0:AdministrativeStaff and prefix0:Professor",
                    251,
                    loadExpectedExplanations("4_5"),
                    REQUIRED_DEPTH_LIMIT
            ),

            new LubmInput(
                    NAME_PREFIX + 6,
                    ONT_PREFIX +
                            "Class: prefix0:Employee " +
                            "Class: prefix0:Student " +
                            "Class: prefix0:Article " +
                            "Class: prefix0:Course " +
                            "Individual: prefix0:a " +
                            "Types: prefix0:Employee and prefix0:Student " +
                            "and prefix0:Article and prefix0:Course",
                    383,
                    loadExpectedExplanations("4_6"),
                    REQUIRED_DEPTH_LIMIT

            ),

            new LubmInput(
                    NAME_PREFIX + 7,
                    ONT_PREFIX +
                            "Class: prefix0:Article " +
                            "Class: prefix0:Course " +
                            "Class: prefix0:AdministrativeStaff " +
                            "Class: prefix0:Professor " +
                            "Individual: prefix0:a " +
                            "Types: prefix0:Article and prefix0:Course " +
                            "and prefix0:AdministrativeStaff and prefix0:Professor",
                    167,
                    loadExpectedExplanations("4_7"),
                    REQUIRED_DEPTH_LIMIT
            ),

            new LubmInput(
                    NAME_PREFIX + 8,
                    ONT_PREFIX +
                            "Class: prefix0:Organization " +
                            "Class: prefix0:Article " +
                            "Class: prefix0:AdministrativeStaff " +
                            "Class: prefix0:Professor " +
                            "Individual: prefix0:a " +
                            "Types: prefix0:Organization and prefix0:Article " +
                            "and prefix0:AdministrativeStaff and prefix0:Professor",
                    587,
                    loadExpectedExplanations("4_8"),
                    REQUIRED_DEPTH_LIMIT
            ),

            new LubmInput(
                    NAME_PREFIX + 9,
                    ONT_PREFIX +
                            "Class: prefix0:Publication " +
                            "Class: prefix0:Student " +
                            "Class: prefix0:AdministrativeStaff " +
                            "Class: prefix0:Professor " +
                            "Individual: prefix0:a " +
                            "Types: prefix0:Publication and prefix0:Student " +
                            "and prefix0:AdministrativeStaff and prefix0:Professor",
                    629,
                    loadExpectedExplanations("4_9"),
                    REQUIRED_DEPTH_LIMIT
            )
    );
}