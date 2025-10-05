package sk.uniba.fmph.dai.cats.parser;

import sk.uniba.fmph.dai.cats.algorithms.Algorithm;
import sk.uniba.fmph.dai.cats.algorithms.Optimisation;
import sk.uniba.fmph.dai.cats.common.Configuration;
import sk.uniba.fmph.dai.cats.common.DLSyntax;
import sk.uniba.fmph.dai.cats.reasoner.ReasonerType;

import java.io.*;
import java.util.ArrayList;


public class ArgumentParser {

    public void parse(String[] args) {

        if (args.length != 1){
            String message = "Wrong number of argument for main function: Run program with one configuration input file as argument";
            throw new RuntimeException(message);
        }
        Configuration.INPUT_FILE_NAME = new File(args[0]).getName().split("\\.")[0];
        ArrayList<String[]> lines = read_input_file(args[0]);

        boolean read_concepts = false;
        boolean read_individuals = false;
        boolean read_prefixes= false;
        boolean read_roles = false;
        boolean read_abducibles = false;

        for (String[] line: lines){
            String new_line = line[0].trim();

            if (new_line.equals("//") || new_line.equals("#"))
                    continue;

            if (read_concepts || read_individuals || read_prefixes || read_roles || read_abducibles){
                if (new_line.equals("}")){
                    read_prefixes = false;
                    read_concepts = false;
                    read_individuals = false;
                    read_roles = false;
                    read_abducibles = false;
                } else if (read_concepts) {
                    addAbducible(new_line, false, true, false);
                } else if (read_individuals) {
                    addAbducible(new_line, false,false, false);
                } else if (read_roles) {
                    addAbducible(new_line, false,false, true);
                } else if (read_abducibles) {
                    add_axiom_based_abd(line);
                } else{
                    String last = (line.length == 2) ? line[1] : "";
                    add_prefix(new_line + " " + last);
                }
                continue;
            }

            boolean silentTrue = false;
            String next = "";

            if (line.length == 1)
                silentTrue = true;
            else
                next = line[1];

            switch(new_line) {
                case "-f:":
                case "-f":
                    if (!(new File(next).exists())){
                        String message = "Could not open -f file " + next;
                        throw new RuntimeException(message);
                    }
                    Configuration.INPUT_ONT_FILE = next;
                    break;
                case "-o:":
                case "-o":
                    Configuration.OBSERVATION = String.join(" ", line).replace("-o: ", "");
                    break;
                case "-out:":
                case "-out":
                    String path = String.join(" ", line).replace("-out: ", "");
                    if (path.matches("^[\\w\\\\/]*\\w+$")) {
                        Configuration.OUTPUT_PATH = path;
                    } else {
                        String message = "Wrong 'output path (-out)' value: " + path + ". Output path should contain only alphanumeric symbols, _ and separators (\\,/) and cannot end with a separator";
                        throw new RuntimeException(message);
                    }
                    break;

                    // Note: we work with only JFact reasoner for now

//                case "-r:":
//                    try {
//                        Configuration.REASONER = ReasonerType.valueOf(next.toUpperCase());
//                    }
//                    catch (IllegalArgumentException e){
//                        String message = "Reasoner type -r " + next + " is unknown, the only allowed reasoners are hermit|pellet|jfact");
//                        throw new RuntimeException(message);
//                    }
//                    break;

                case "-d:":
                case "-d":
                    try {
                        Configuration.DEPTH = Integer.parseInt(next);
                    }
                    catch (NumberFormatException e) {
                        String message = "Wrong 'tree depth (-d)' value: " + next + ", must be an integer!";
                        throw new RuntimeException(message);
                    }
                    break;
                case "-t:":
                case "-t":
                    try {
                        Configuration.TIMEOUT = Long.parseLong(next);
                    }
                    catch (NumberFormatException e) {
                        String message = "Wrong 'timeout (-t)' value: " + next + ", must be an integer!";
                        throw new RuntimeException(message);
                    }
                    break;
                case "-aI:":
                case "-aI":
                    if (next.equals("{")){
                        read_individuals = true;
                    } else {
                        addAbducible(next, false, false, false);
                    }
                    break;
                case "-aC:":
                case "-aC":
                    if (next.equals("{")){
                        read_concepts = true;
                    } else {
                        addAbducible(next, false,true, false);
                    }
                    break;
                case "-aR:":
                case "-aR":
                    if (next.equals("{")){
                        read_roles = true;
                    } else {
                        addAbducible(next, false,false, true);
                    }
                    break;
                case "-abd:":
                case "-abd":
                    if (next.equals("{")){
                        read_abducibles = true;
                    } else {
                        addAbducible(next, true,false, false);
                    }
                    break;
                case "-l:":
                case "-l":
                    if (next.equals("false")) {
                        Configuration.LOOPING_ALLOWED = false;
                    } else if (!silentTrue && !next.equals("true")) {
                        System.err.println("Wrong 'looping allowed (-l)' value: " + next + ", allowed values are 'true' and 'false'");
                    }
                    break;
                case "-r:":
                case "-r":
                    if (silentTrue || next.equals("true")) {
                        Configuration.ROLES_IN_EXPLANATIONS_ALLOWED = true;
                    } else if (!next.equals("false")) {
                        System.err.println("Wrong 'roles in explanations allowed (-r)' value: " + next + ", allowed values are 'true' and 'false'");
                    }
                    break;
                case "-sR:":
                case "-sR":
                    if (next.equals("false")) {
                        Configuration.STRICT_RELEVANCE = false;
                    } else if (!silentTrue && !next.equals("true")) {
                        System.err.println("Wrong 'strict relevance (-sR)' value: " + next + ", allowed values are 'true' and 'false'");
                }
                case "-n:":
                case "-n":
                    if (next.equals("false")) {
                        Configuration.NEGATION_ALLOWED = false;
                    } else if (!silentTrue && !next.equals("true")) {
                        System.err.println("Wrong 'negation (-n)' allowed value: " + next + ", allowed values are 'true' and 'false'");
                    }
                    break;
                case "-log:":
                case "-log":
                    if (next.equals("false")) {
                        Configuration.LOGGING = false;
                    } else if (!silentTrue && !next.equals("true")) {
                        System.err.println("Wrong 'logging (-log)' value: " + next + ", allowed values are 'true' and 'false'");
                    }
                    break;
                case "-abdF:":
                case "-abdF":
                    if (!(new File(next).exists())){
                        String message = "Could not open -abdF file " + next;
                        throw new RuntimeException(message);
                    }
                    Configuration.ABDUCIBLES_FILE_NAME = next;
                    break;
                case "-alg:":
                case "-alg":
                    chooseAlgorithm(next);
                    break;
                case "-p:":
                case "-p":
                    if (silentTrue || next.equals("true")) {
                        Configuration.PRINT_PROGRESS = true;
                    } else if (!next.equals("false")) {
                        System.err.println("Wrong 'progress (-p)' value: " + next + ", allowed values are 'true' and 'false'");
                    }
                    break;
                case "-debug:":
                case "-debug":
                    if (silentTrue || next.equals("true")){
                        Configuration.DEBUG_PRINT = true;
                    } else if (!next.equals("false")) {
                        System.err.println("Wrong 'debug' value: " + next + ", allowed values are 'true' and 'false'");
                    }
                    break;
                case "-opt:":
                case "-opt":
                    if (next.contains("1")){
                        Configuration.optimisations.add(Optimisation.MOVE_CONSISTENCY_CHECKS);
                    }
                    if (next.contains("2")){
                        Configuration.optimisations.add(Optimisation.SORT_MODEL);
                    }
                    if (next.contains("3")) {
                        Configuration.optimisations.add(Optimisation.REMOVE_NEGATED_PATH);
                    }
                    if (next.contains("4")) {
                        Configuration.optimisations.add(Optimisation.TRIPLE_MXP);
                    }
                    if (next.contains("5")) {
                        Configuration.optimisations.add(Optimisation.FULLY_RANDOM_SET_DIVISION);
                    }
                    else if (next.contains("6")) {
                        Configuration.optimisations.add(Optimisation.EQUAL_SIZE_RANDOM_SET_DIVISION);
                    }
                    break;
                case "-defOpt:":
                case "-defOpt":
                    if (next.equals("false")) {
                        Configuration.IGNORE_DEFAULT_OPTIMISATIONS = true;
                    } else if (!silentTrue && !next.equals("true")) {
                        System.err.println("Wrong 'default optimisations (-defOpt)' value: " + next + ", allowed values are 'true' and 'false'");
                    }
                    break;
                case "-partial:":
                case "-partial":
                    if (next.equals("false")) {
                        Configuration.PARTIAL_LOGS = false;
                    } else if (!silentTrue && !next.equals("true")) {
                        System.err.println("Wrong 'partial logs (-partial)' value: " + next + ", allowed values are 'true' and 'false'");
                    }
                    break;
                case "-stats:":
                case "-stats":
                    if (next.equals("false")) {
                        Configuration.TRACKING_STATS = false;
                    } else if (!silentTrue && !next.equals("true")) {
                        System.err.println("Wrong 'statistics (-stats)' value: " + next + ", allowed values are 'true' and 'false'");
                    }
                    break;
                case "-fast:":
                case "-fast":
                    if (silentTrue || next.equals("true")){
                        Configuration.LOGGING = false;
                        Configuration.DEBUG_PRINT = false;
                        Configuration.TRACKING_STATS = false;
                        Configuration.EVENTS = false;
                    } else if (!next.equals("false")) {
                        System.err.println("Wrong 'fast' value: " + next + ", allowed values are 'true' and 'false'");
                    }
                    break;
                default:
                    String message = "Unknown option " + line[0] + " in input file";
                    throw new RuntimeException(message);
            }
        }
        if (Configuration.INPUT_ONT_FILE.equals("") || Configuration.OBSERVATION.equals("")){
            String message = "Input file -f and observation -o are both required arguments";
            throw new RuntimeException(message);
        }
        if (Configuration.REASONER == null) {
            Configuration.REASONER = ReasonerType.JFACT;
        }
    }

    public void chooseAlgorithm(String argument){
        Algorithm chosenAlg = null;
        String name = argument.toUpperCase();
        try{
            chosenAlg = Algorithm.valueOf(name);
        } catch(IllegalArgumentException e){
            for (Algorithm alg : Algorithm.values()) {
                if (alg.matchesName(name)){
                    chosenAlg = alg;
                    break;
                }
            }
        }

        if (chosenAlg == null)
            throw new RuntimeException("Invalid algorithm name! " + argument);

        Configuration.ALGORITHM = chosenAlg;
    }

    private void add_prefix(String prefix){
        if (!prefix.matches("[a-zA-Z0-9]+: " + DLSyntax.IRI_REGEX)){
            String message = "Prefix '" + prefix + "' does not match the form 'prefix_shortcut: prefix'";
            throw new RuntimeException(message);
        }
        Configuration.PREFIXES.add(prefix);
    }

    private void addAbducible(String abd, boolean axiomBasedAbducibles, boolean isConcept, boolean isRole){
        if (axiomBasedAbducibles)
            Configuration.AXIOM_BASED_ABDUCIBLES.add(abd);
        else if (isConcept)
            Configuration.ABDUCIBLES_CONCEPTS.add(abd);
        else if (isRole)
            Configuration.ABDUCIBLES_ROLES.add(abd);
        else
            Configuration.ABDUCIBLES_INDIVIDUALS.add(abd);
    }

    private void add_axiom_based_abd(String[] abd){
        StringBuilder assertion = new StringBuilder();
        for(String abd1 : abd){
            assertion.append(abd1).append(" ");
        }
        Configuration.AXIOM_BASED_ABDUCIBLES.add(assertion.toString());
    }

    private ArrayList<String[]> read_input_file(String input_file_path) {
        FileInputStream stream = null;
        try {
            stream = new FileInputStream(input_file_path);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        String strLine;
        ArrayList<String[]> lines = new ArrayList<>();
        try {
            while ((strLine = reader.readLine()) != null) {
                if (strLine.equals("")){
                    continue;
                }
                String[] words = strLine.split("\\s+");
                lines.add(words);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lines;
    }

}
