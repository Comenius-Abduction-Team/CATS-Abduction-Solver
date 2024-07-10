package sk.uniba.fmph.dai.cats;

import sk.uniba.fmph.dai.abduction_api.abducer.IExplanation;
import sk.uniba.fmph.dai.cats.algorithms.Algorithm;
import sk.uniba.fmph.dai.cats.algorithms.ISolver;
import sk.uniba.fmph.dai.cats.algorithms.hst.HstHybridSolver;
import sk.uniba.fmph.dai.cats.algorithms.hybrid.ConsoleExplanationManager;
import sk.uniba.fmph.dai.cats.algorithms.hybrid.HybridSolver;
import sk.uniba.fmph.dai.cats.algorithms.hybrid.MxpSolver;
import sk.uniba.fmph.dai.cats.api_implementation.CatsAbducer;
import sk.uniba.fmph.dai.cats.application.Application;
import sk.uniba.fmph.dai.cats.application.ExitCode;
import sk.uniba.fmph.dai.cats.common.Configuration;
import sk.uniba.fmph.dai.cats.common.ConsolePrinter;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.DefaultPrefixManager;
import sk.uniba.fmph.dai.cats.parser.ArgumentParser;
import sk.uniba.fmph.dai.cats.progress.ConsoleProgressManager;
import sk.uniba.fmph.dai.cats.reasoner.*;
import sk.uniba.fmph.dai.cats.timer.ThreadTimer;

import java.io.*;
import java.util.Set;
public class Main {

    /** whether the solver is being run from an IDE*/
    private static final boolean TESTING = false;
    /** whether the solver is being run from an IDE through the API*/
    private static final boolean API = false;

    private static final String INPUT_FILE = "in/toothache.in";

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

    public static ISolver runSolving(String[] args, ThreadTimer timer) throws Exception {

        ISolver solver = null;

        try{

            ArgumentParser argumentParser = new ArgumentParser();
            argumentParser.parse(args);

            timer.start();

            ILoader loader = new ConsoleLoader();
            loader.initialize(Configuration.REASONER);

            IReasonerManager reasonerManager = new ReasonerManager(loader);

            solver = createSolver(timer, loader, reasonerManager);
            solver.solve(loader, reasonerManager);

        } catch(Throwable e){
            new ConsolePrinter().logError("An error occurred:", e);
            throw e;
        }

        return solver;
    }

    private static ISolver createSolver(ThreadTimer timer, ILoader loader, IReasonerManager reasonerManager) {

        ConsoleExplanationManager explanationManager = new ConsoleExplanationManager(loader, reasonerManager);
        ConsoleProgressManager progressManager = new ConsoleProgressManager();

        if (Configuration.ALGORITHM == Algorithm.MXP)
            return new MxpSolver(timer, explanationManager, progressManager, new ConsolePrinter());
        else if (Configuration.ALGORITHM.isHst())
            return new HstHybridSolver(timer, explanationManager, progressManager, new ConsolePrinter());
        else
            return new HybridSolver(timer, explanationManager, progressManager, new ConsolePrinter());
    }
}
