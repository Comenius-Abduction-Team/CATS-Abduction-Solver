package sk.uniba.fmph.dai.cats.parser;

import sk.uniba.fmph.dai.cats.algorithms.Algorithm;
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

            if (new_line.equals("//"))
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
            String next = line[1];
            switch(new_line) {
                case "-f:":
                    if (!(new File(next).exists())){
                        String message = "Could not open -f file " + next;
                        throw new RuntimeException(message);
                    }
                    Configuration.INPUT_ONT_FILE = next;
                    break;
                case "-o:":
                    String observation = String.join(" ", line).replace("-o: ", "");
                    Configuration.OBSERVATION = observation;
                    break;
                case "-out:":
                    String path = String.join(" ", line).replace("-out: ", "");
                    if (path.matches("^[\\w\\\\/]*[\\w]+$")) {
                        Configuration.OUTPUT_PATH = path;
                    } else {
                        String message = "Wrong output path -out " + path + "\nOutput path should contain only alphanumeric symbols, _ and separators (\\,/) and cannot end with a separator";
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
                    try {
                        Configuration.DEPTH = Integer.valueOf(next);
                    }
                    catch (NumberFormatException e) {
                        String message = "Wrong tree depth -d " + next + ", choose a whole number value";
                        throw new RuntimeException(message);
                    }
                    break;
                case "-t:":
                    try {
                        Configuration.TIMEOUT = Long.valueOf(next);
                    }
                    catch (NumberFormatException e) {
                        String message = "Wrong timeout value -t " + next + ", choose a whole number value";
                        throw new RuntimeException(message);
                    }
                    break;
                case "-aI:":
                    if (next.equals("{")){
                        read_individuals = true;
                    } else {
                        addAbducible(next, false, false, false);
                    }
                    break;
                case "-aC:":
                    if (next.equals("{")){
                        read_concepts = true;
                    } else {
                        addAbducible(next, false,true, false);
                    }
                    break;
                case "-aR:":
                    if (next.equals("{")){
                        read_roles = true;
                    } else {
                        addAbducible(next, false,false, true);
                    }
                    break;
                case "-abd:":
                    if (next.equals("{")){
                        read_abducibles = true;
                    } else {
                        addAbducible(next, true,false, false);
                    }
                    break;
                case "-l:":
                    if (next.equals("false")) {
                        Configuration.LOOPING_ALLOWED = false;
                    } else if (!next.equals("true")) {
                        System.err.println("Wrong looping allowed value -l" + next + ", allowed values are 'true' and 'false'");
                    }
                    break;
                case "-r:":
                    if (next.equals("true")) {
                        Configuration.ROLES_IN_EXPLANATIONS_ALLOWED = true;
                    } else if (!next.equals("false")) {
                        System.err.println("Wrong roles in explanations allowed value -r" + next + ", allowed values are 'true' and 'false'");
                    }
                    break;
                case "-sR:":
                    if (next.equals("false")) {
                        Configuration.STRICT_RELEVANCE = false;
                    } else if (!next.equals("true")) {
                        System.err.println("Wrong strict relevance value -sR" + next + ", allowed values are 'true' and 'false'");
                }
                case "-n:":
                    if (next.equals("false")) {
                        Configuration.NEGATION_ALLOWED = false;
                    } else if (!next.equals("true")) {
                        System.err.println("Wrong negation allowed value -n" + next + ", allowed values are 'true' and 'false'");
                    }
                    break;
                case "-log:":
                    if (next.equals("false")) {
                        Configuration.LOGGING = false;
                    } else if (!next.equals("true")) {
                        System.err.println("Wrong logging value -log" + next + ", allowed values are 'true' and 'false'");
                    }
                    break;
                case "-abdF:":
                    if (!(new File(next).exists())){
                        String message = "Could not open -abdF file " + next;
                        throw new RuntimeException(message);
                    }
                    Configuration.ABDUCIBLES_FILE_NAME = next;
                    break;
                case "-alg:":
                    chooseAlgorithm(next);
                    break;
                case "-p:":
                    if (next.equals("true")) {
                        Configuration.PRINT_PROGRESS = true;
                    } else if (!next.equals("false")) {
                        System.err.println("Wrong progress value -p" + next + ", allowed values are 'true' and 'false'");
                    }
                    break;
                case "-debug:":
                    if (next.equals("true")){
                        Configuration.DEBUG_PRINT = true;
                    } else if (!next.equals("false")) {
                        System.err.println("Wrong progress value -d" + next + ", allowed values are 'true' and 'false'");
                    }
                    break;
                default:
                    String message = "Unknown option " + line[0] + " in input file";
                    throw new RuntimeException(message);
            }
        }
        if (Configuration.INPUT_ONT_FILE.equals("") || Configuration.OBSERVATION.equals("")){
            String message = "Input file -f and observation -o are both required argument";
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
        String assertion = "";
        for(String abd1 : abd){
            assertion += abd1 + " ";
        }
        Configuration.AXIOM_BASED_ABDUCIBLES.add(assertion);
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
