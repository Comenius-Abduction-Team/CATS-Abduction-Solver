package sk.uniba.fmph.dai.cats;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.DefaultPrefixManager;
import sk.uniba.fmph.dai.abduction_api.abducer.IExplanation;
import sk.uniba.fmph.dai.cats.algorithms.Algorithm;
import sk.uniba.fmph.dai.cats.algorithms.AlgorithmSolver;
import sk.uniba.fmph.dai.cats.algorithms.AlgorithmSolverFactory;
import sk.uniba.fmph.dai.cats.api_implementation.CatsAbducer;
import sk.uniba.fmph.dai.cats.application.Application;
import sk.uniba.fmph.dai.cats.application.ExitCode;
import sk.uniba.fmph.dai.cats.common.Configuration;
import sk.uniba.fmph.dai.cats.common.ConsolePrinter;
import sk.uniba.fmph.dai.cats.parser.ArgumentParser;
import sk.uniba.fmph.dai.cats.timer.MetricsThread;

import java.io.File;
import java.util.Set;
public class Main {

    /** whether the solver is being run from an IDE*/
    private static final boolean TESTING = false;
    /** whether the solver is being run from an IDE through the API*/
    private static final boolean API = false;

    //"in/multiple_obs/family.in"
    //"in/toothache.in"
    //"in/ore_ont_8666_obs04_ont01_1729028695588_mxp_.in"
    private static final String INPUT_FILE = "in/multiple_obs/family.in";

    public static void main(String[] args) throws Exception {

        if (TESTING){
            if (API){
                runApiTestingMain();
                return;
            }

            args = new String[]{INPUT_FILE};
        }

        MetricsThread metrics = new MetricsThread(10);

        try{
            runSolving(args, metrics);
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

            ArgumentParser argumentParser = new ArgumentParser();
            argumentParser.parse(args);

            AlgorithmSolver solver = AlgorithmSolverFactory.createConsoleSolver(metrics, Configuration.ALGORITHM);
            solver.solve();

        } catch(Throwable e){
            new ConsolePrinter().logError("An error occurred:", e);
            throw e;
        }

    }
}
