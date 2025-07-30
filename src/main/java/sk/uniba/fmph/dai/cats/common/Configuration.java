package sk.uniba.fmph.dai.cats.common;

import sk.uniba.fmph.dai.cats.algorithms.Algorithm;
import sk.uniba.fmph.dai.cats.reasoner.ReasonerType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Configuration {
    public static String OBSERVATION = "";
    public static String INPUT_ONT_FILE = "";
    public static String INPUT_FILE_NAME = "";
    public static String OUTPUT_PATH = "";
    public static String ABDUCIBLES_FILE_NAME = null;
    public static ReasonerType REASONER = ReasonerType.JFACT;     // we work only with JFact for now
    public static int DEPTH = 0;
    public static long TIMEOUT = 0;
    public static ArrayList<String> ABDUCIBLES_CONCEPTS = new ArrayList<>();
    public static ArrayList<String> ABDUCIBLES_INDIVIDUALS = new ArrayList<>();
    public static ArrayList<String> ABDUCIBLES_ROLES = new ArrayList<>();
    public static ArrayList<String> AXIOM_BASED_ABDUCIBLES = new ArrayList<>();
    public static ArrayList<String> PREFIXES = new ArrayList<>();
    public static boolean NEGATION_ALLOWED = true;
    public static boolean LOOPING_ALLOWED = true;
    public static boolean ROLES_IN_EXPLANATIONS_ALLOWED = false; // unstable for now
    public static boolean STRICT_RELEVANCE = true;
    public static boolean PRINT_PROGRESS = false;

    //constants set before run program
    public static boolean REUSE_OF_MODELS = true;
    public static boolean CHECKING_MINIMALITY_BY_QXP = false;
    public static boolean CACHED_CONFLICTS_LONGEST_CONFLICT = false;
    public static boolean CACHED_CONFLICTS_MEDIAN = false;
    public static boolean CONTINUOUS_HYBRID_RELEVANCE_CHECKS = false;
    public static boolean CONTINUOUS_HYBRID_MINIMALITY_CHECKS = false;
    public static boolean MOVE_CHECKS_AFTER_MODEL_REUSE = false;

    public static boolean ALWAYS_EXTRACT_MODELS_IN_MXP = true;

    public static boolean SORT_MODELS = false;

    public static boolean REMOVE_COMPLEMENTS_FROM_MXP = false;

    //public static boolean RETURN_CACHED_EXPLANATION_IN_QXP = true;

    public static Algorithm ALGORITHM = Algorithm.MHS_MXP;

    /** Use to wrap testing prints in an if block. These block should not get to production code. **/
    public static boolean DEBUG_PRINT = false;

    public static boolean LOGGING = true;

    public static List<String> getInfo() {
//        String optimizationQXP = "Optimization QXP: " + CHECKING_MINIMALITY_BY_QXP;
//        String optimizationLongestConf = "Optimization Cached Conflicts - The Longest Conflict: " + CACHED_CONFLICTS_LONGEST_CONFLICT;
//        String optimizationMedian = "Optimization Cached Conflicts - Median: " + CACHED_CONFLICTS_MEDIAN;
        String roles = "Roles: " + ROLES_IN_EXPLANATIONS_ALLOWED;
        String looping = "Looping allowed: " + LOOPING_ALLOWED;
        String negation = "Negation: " +  NEGATION_ALLOWED;
        String mhs_mode = "Algorithm: " + ALGORITHM;
        String relevance = "Strict relevance: " + STRICT_RELEVANCE;
        String depth = "Depth limit: ";
        if (DEPTH > 0) depth += DEPTH; else depth += "none";
        String timeout = "Timeout: ";
        if (TIMEOUT > 0) timeout += TIMEOUT; else timeout += "none";

        return Arrays.asList(
                roles, looping, negation, mhs_mode, relevance, depth, timeout);
    }
}
