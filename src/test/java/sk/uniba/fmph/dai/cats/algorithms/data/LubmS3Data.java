package sk.uniba.fmph.dai.cats.algorithms.data;

import java.util.Arrays;
import java.util.List;

public final class LubmS3Data extends LubmData {

    private LubmS3Data() {}

    private static final String NAME_PREFIX = "lubm_0_3_";

    public static final List<LubmInput> LUBM_INPUTS = Arrays.asList(

            new LubmInput(
                    NAME_PREFIX + 0,
                    ONT_PREFIX +
                            "Class: prefix0:Organization " +
                            "Class: prefix0:Work " +
                            "Class: prefix0:Faculty " +
                            "Individual: prefix0:a " +
                            "Types: prefix0:Organization and " +
                            "prefix0:Work and prefix0:Faculty",
                    279,
                    loadExpectedExplanations("3_0")
            ),

            new LubmInput(
                    NAME_PREFIX + 1,
                    ONT_PREFIX +
                            "Class: prefix0:Publication " +
                            "Class: prefix0:AdministrativeStaff " +
                            "Class: prefix0:Faculty " +
                            "Individual: prefix0:a " +
                            "Types: prefix0:Publication and " +
                            "prefix0:AdministrativeStaff and prefix0:Faculty",
                    299,
                    loadExpectedExplanations("3_1")
            ),

            new LubmInput(
                    NAME_PREFIX + 2,
                    ONT_PREFIX +
                            "Class: prefix0:Student " +
                            "Class: prefix0:AdministrativeStaff " +
                            "Class: prefix0:Professor " +
                            "Individual: prefix0:a " +
                            "Types: prefix0:Student and " +
                            "prefix0:AdministrativeStaff and prefix0:Professor",
                    62,
                    loadExpectedExplanations("3_2")

            ),

            new LubmInput(
                    NAME_PREFIX + 3,
                    ONT_PREFIX +
                            "Class: prefix0:Organization " +
                            "Class: prefix0:AdministrativeStaff " +
                            "Class: prefix0:Professor " +
                            "Individual: prefix0:a " +
                            "Types: prefix0:Organization and " +
                            "prefix0:AdministrativeStaff and prefix0:Professor",
                    146,
                    loadExpectedExplanations("3_3")

            ),

            new LubmInput(
                    NAME_PREFIX + 4,
                    ONT_PREFIX +
                            "Class: prefix0:Organization " +
                            "Class: prefix0:Course " +
                            "Class: prefix0:Faculty " +
                            "Individual: prefix0:a " +
                            "Types: prefix0:Organization and " +
                            "prefix0:Course and prefix0:Faculty",
                    139,
                    loadExpectedExplanations("3_4")
            ),

            new LubmInput(
                    NAME_PREFIX + 5,
                    ONT_PREFIX +
                            "Class: prefix0:Publication " +
                            "Class: prefix0:Employee " +
                            "Class: prefix0:Student " +
                            "Individual: prefix0:a " +
                            "Types: prefix0:Publication and " +
                            "prefix0:Employee and prefix0:Student",
                    479,
                    loadExpectedExplanations("3_5")
            ),

            new LubmInput(
                    NAME_PREFIX + 6,
                    ONT_PREFIX +
                            "Class: prefix0:Work " +
                            "Class: prefix0:Article " +
                            "Class: prefix0:AdministrativeStaff " +
                            "Individual: prefix0:a " +
                            "Types: prefix0:Work and prefix0:Article and " +
                            "prefix0:AdministrativeStaff",
                    47,
                    loadExpectedExplanations("3_6")
            ),

            new LubmInput(
                    NAME_PREFIX + 7,
                    ONT_PREFIX +
                            "Class: prefix0:Work " +
                            "Class: prefix0:Employee " +
                            "Class: prefix0:Article " +
                            "Individual: prefix0:a " +
                            "Types: prefix0:Work and prefix0:Employee and " +
                            "prefix0:Article",
                    255,
                    loadExpectedExplanations("3_7")

            ),

            new LubmInput(
                    NAME_PREFIX + 8,
                    ONT_PREFIX +
                            "Class: prefix0:Organization " +
                            "Class: prefix0:Publication " +
                            "Class: prefix0:Student " +
                            "Individual: prefix0:a " +
                            "Types: prefix0:Organization and " +
                            "prefix0:Publication and prefix0:Student",
                    209,
                    loadExpectedExplanations("3_8")

            ),

            new LubmInput(
                    NAME_PREFIX + 9,
                    ONT_PREFIX +
                            "Class: prefix0:Organization " +
                            "Class: prefix0:AdministrativeStaff " +
                            "Class: prefix0:Faculty " +
                            "Individual: prefix0:a " +
                            "Types: prefix0:Organization and " +
                            "prefix0:AdministrativeStaff and prefix0:Faculty",
                    209,
                    loadExpectedExplanations("3_9")
            )
    );
}