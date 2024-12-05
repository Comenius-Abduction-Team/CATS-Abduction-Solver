package sk.uniba.fmph.dai.cats;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.DefaultPrefixManager;
import sk.uniba.fmph.dai.abduction_api.abducer.IExplanation;
import sk.uniba.fmph.dai.cats.algorithms.Algorithm;
import sk.uniba.fmph.dai.cats.algorithms.hybrid.AlgorithmSolver;
import sk.uniba.fmph.dai.cats.algorithms.hybrid.AlgorithmSolverFactory;
import sk.uniba.fmph.dai.cats.api_implementation.CatsAbducer;
import sk.uniba.fmph.dai.cats.application.Application;
import sk.uniba.fmph.dai.cats.application.ExitCode;
import sk.uniba.fmph.dai.cats.common.Configuration;
import sk.uniba.fmph.dai.cats.common.ConsolePrinter;
import sk.uniba.fmph.dai.cats.parser.ArgumentParser;
import sk.uniba.fmph.dai.cats.reasoner.ConsoleLoader;
import sk.uniba.fmph.dai.cats.reasoner.Loader;
import sk.uniba.fmph.dai.cats.timer.ThreadTimer;

import java.io.File;
import java.util.Set;
public class Main {

    /** whether the solver is being run from an IDE*/
    private static final boolean TESTING = true;
    /** whether the solver is being run from an IDE through the API*/
    private static final boolean API = false;

    //"in/multiple_obs/family.in"
    //"in/toothache.in"
    private static final String INPUT_FILE = "in/multiple_obs/family.in";

    public static void main(String[] args) throws Exception {

        if (TESTING){
            if (API){
                runApiTestingMain();
                return;
            }

            args = new String[]{INPUT_FILE};
        }

        ThreadTimer timer = new ThreadTimer(100);

        try{
            runSolving(args, timer);
        } catch(Throwable e) {
            e.printStackTrace();
            Application.finish(ExitCode.ERROR);
        } finally {
            timer.interrupt();
        }

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

//        OWLClass A = dataFactory.getOWLClass(":A", pm);
//        OWLClass C = dataFactory.getOWLClass(":C", pm);
//        OWLClass E = dataFactory.getOWLClass(":E", pm);
//        OWLNamedIndividual a = dataFactory.getOWLNamedIndividual(":a", pm);
//        OWLClassAssertionAxiom classAssertion = dataFactory.getOWLClassAssertionAxiom(
//                dataFactory.getOWLObjectIntersectionOf(A,C,E), a);
//
//        ISymbolAbducibles abducibles = factory.getSymbolAbducibles();
//        abducibles.add(A);
//        abducibles.add(C);
//
//        IAbducer abducer = factory.getAbducer(ont, Collections.singleton(classAssertion));
//        abducer.setSolverSpecificParameters("");
//
//        IThreadAbducer threadAbducer = (IThreadAbducer) abducer;
//        AbductionMonitor monitor = threadAbducer.getAbductionMonitor();
//        monitor.setWaitLimit(1000);
//
//        Thread thread = new Thread(threadAbducer);
//        thread.start();
//
//        while(true){
//            try{
//                synchronized (monitor){
//                    monitor.wait();
//
//                    if (monitor.areNewExplanationsAvailable()){
//                        Set<IExplanation> expl = monitor.getUnprocessedExplanations();
//                        System.out.println(expl);
//                        monitor.markExplanationsAsProcessed();
//                        monitor.clearExplanations();
//                    }
//
//                    if (monitor.isNewProgressAvailable()){
//                        Percentage progress = monitor.getProgressPercentage();
//                        String message = monitor.getStatusMessage();
//                        System.out.println(progress + "//" + message);
//                        monitor.markProgressAsProcessed();
//                    }
//
//                    if (monitor.getProgressPercentage().getValue() >= 100){
//                        thread.interrupt();
//                        monitor.notify();
//                        break;
//                    }
//
//                    monitor.notify();
//                }
//            } catch(InterruptedException e){
//                e.printStackTrace();
//            }
//
//        }
//        System.out.println("EXPLANATIONS FOUND: " + threadAbducer.getExplanations());
//
//        System.out.println("-----------------------------------------");
//        System.out.println("OUTPUT MESSAGE: " + threadAbducer.getOutputMessage());
//        System.out.println("-----------------------------------------");
//        System.out.println("FULL LOG:");
//        System.out.println(threadAbducer.getFullLog());
    }

    public static void runSolving(String[] args, ThreadTimer timer) {

        try{

            ArgumentParser argumentParser = new ArgumentParser();
            argumentParser.parse(args);

            Loader loader = new ConsoleLoader();
            loader.initialize(Configuration.REASONER);

            AlgorithmSolver solver = AlgorithmSolverFactory.createConsoleSolver(timer, Configuration.ALGORITHM);
            solver.solve();
            Application.finish(ExitCode.SUCCESS);

        } catch(Throwable e){
            new ConsolePrinter().logError("An error occurred:", e);
            throw e;
        }

    }
}
