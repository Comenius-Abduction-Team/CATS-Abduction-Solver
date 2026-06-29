package sk.uniba.fmph.dai.cats.algorithms.data;

import java.util.Arrays;
import java.util.List;

public final class LubmS5Data {

    private LubmS5Data() {}

    private static final String ONT_PREFIX =
            "Prefix: prefix0: <http://swat.cse.lehigh.edu/onto/univ-bench.owl#>\n";

    private static final String NAME_PREFIX = "lubm_0_5_";

    public static final Integer DEPTH_LIMIT = 6;

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
                    55
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
                    64
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
                    66
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
                    58
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
                    52
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
                    71
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
                    40
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
                    84
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
                    104
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
                    32
            )
    );
}