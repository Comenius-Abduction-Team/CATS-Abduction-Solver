package sk.uniba.fmph.dai.cats;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.DefaultPrefixManager;
import sk.uniba.fmph.dai.abduction_api.abducer.IExplanation;
import sk.uniba.fmph.dai.cats.algorithms.Algorithm;
import sk.uniba.fmph.dai.cats.algorithms.AlgorithmSolver;
import sk.uniba.fmph.dai.cats.algorithms.AlgorithmSolverFactory;
import sk.uniba.fmph.dai.cats.algorithms.RuleChecker;
import sk.uniba.fmph.dai.cats.api_implementation.CatsAbducer;
import sk.uniba.fmph.dai.cats.application.Application;
import sk.uniba.fmph.dai.cats.application.ExitCode;
import sk.uniba.fmph.dai.cats.common.Configuration;
import sk.uniba.fmph.dai.cats.common.ConsolePrinter;
import sk.uniba.fmph.dai.cats.parser.ArgumentParser;
import sk.uniba.fmph.dai.cats.metrics.MetricsThread;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {

    /** whether the solver is being run from an IDE*/
    private static final boolean TESTING = true;
    /** whether the solver is being run from an IDE through the API*/
    private static final boolean API = false;

    // files often used for manual testing
    private static final String FAMILY_MULTI = "in/multiple_obs/family.in";
    //private static final String TOOTHACHE = "in/toothache.in";
    private static final String TOOTHACHE = "in/toothache.in";

    private static final String TEST1 = "D:\\studium\\diplom\\CATS_ver2\\CATS-Abduction-Solver\\in\\lubm_mhsmxp_testing_mykyta\\lubm_1.in";

    private static final String INPUT_FILE = "D:\\studium\\diplom\\CATS_ver2\\CATS-Abduction-Solver\\in\\dl2025\\S2\\lubm-0_2_1_mhs-mxp_noNeg.in";

    public static void main(String[] args) throws Exception {

        if (TESTING){
            if (API){
                runApiTestingMain();
                return;
            }

            args = new String[]{INPUT_FILE};
        }

        MetricsThread metrics = new MetricsThread(2);

        try{
            runSolving(args, metrics);
//            System.out.println(
//                    "isMinimal was called " +
//                            RuleChecker.getIsMinimalCalls() +
//                            " times"
//            );
        } catch(Throwable e) {
            e.printStackTrace();
            Application.finish(ExitCode.ERROR);
        } finally {
            metrics.terminate();
        }
        Application.finish(ExitCode.SUCCESS);

    }

    private static void runApiTestingMain() throws OWLOntologyCreationException {

        OWLOntology backgroundKnowledge;
        OWLAxiom observation;

        OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager();

        File file = new File("files/toothache_ontology.rdf");
        backgroundKnowledge = ontologyManager.loadOntologyFromOntologyDocument(file);

        OWLDataFactory dataFactory = ontologyManager.getOWLDataFactory();
        String prefix = "http://www.semanticweb.org/janbo/ontologies/2024/4/toothache#";
        PrefixManager pm = new DefaultPrefixManager(prefix);
        OWLNamedIndividual john = dataFactory.getOWLNamedIndividual(":john", pm);
        OWLClass toothache = dataFactory.getOWLClass(":Toothache", pm);
        observation = dataFactory.getOWLClassAssertionAxiom(toothache, john);

        CatsAbducer abducer = new CatsAbducer(backgroundKnowledge, observation);
        abducer.setAlgorithm(Algorithm.MHS_MXP);
        abducer.solveAbduction();
        Set<IExplanation> explanations = abducer.getExplanations();
        explanations.forEach(System.out::println);

    }

    public static void runSolving(String[] args, MetricsThread metrics) {

        try{
            FileWriter fw = null;
            PrintWriter pw = null;



            ArgumentParser argumentParser = new ArgumentParser();
            argumentParser.parse(args);

            AlgorithmSolver solver = AlgorithmSolverFactory.createConsoleSolver(metrics, Configuration.ALGORITHM);

            Runtime runtime = Runtime.getRuntime();
            long usedMemoryBefore = runtime.totalMemory() - runtime.freeMemory();
            //System.out.println("Used Memory before" + usedMemoryBefore);
            solver.solve();
//            long usedMemoryAfter = runtime.totalMemory() - runtime.freeMemory();
//            try {
//                fw = new FileWriter("output.txt", true);
//                pw = new PrintWriter(fw);
//
//                pw.write("Used memory: " + (usedMemoryAfter - usedMemoryBefore) / (1024 * 1024)
//                        + " MB\n");
//                pw.close();
//                fw.close();
//            } catch (IOException ex) {
//                //pass
//            }



        } catch(Throwable e){
            new ConsolePrinter().logError("An error occurred:", e);
            throw e;
        }

    }
}
