package sk.uniba.fmph.dai.cats;

import sk.uniba.fmph.dai.abduction_api.abducer.IExplanation;
import sk.uniba.fmph.dai.abduction_api.monitor.AbductionMonitor;
import sk.uniba.fmph.dai.abduction_api.monitor.Percentage;
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
import sk.uniba.fmph.dai.cats.timer.ThreadTimes;

import java.io.*;
import java.util.Set;
public class Main {

    /** whether the solver is being run from an IDE*/
    private static final boolean TESTING = true;
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

        ThreadTimes threadTimes = new ThreadTimes(100);

        try{
            runSolving(args, threadTimes);
        } catch(Throwable e) {
            e.printStackTrace();
            Application.finish(ExitCode.ERROR);
        } finally {
            threadTimes.interrupt();
        }

    }

    private static void runApiTestingMain() throws OWLOntologyCreationException {

        Configuration.PRINT_PROGRESS = true;

        OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager();
        File file = new File("files/toothache.rdf");
        OWLOntology backgroundKnowledge = ontologyManager.loadOntologyFromOntologyDocument(file);

        OWLDataFactory dataFactory = ontologyManager.getOWLDataFactory();
        String prefix = "http://www.semanticweb.org/janbo/ontologies/2024/4/toothache#";

        PrefixManager pm = new DefaultPrefixManager(prefix);

        OWLClass toothache = dataFactory.getOWLClass(":Toothache", pm);
        OWLNamedIndividual john = dataFactory.getOWLNamedIndividual(":john", pm);

        OWLClassAssertionAxiom observation = dataFactory.getOWLClassAssertionAxiom(toothache, john);

        CatsAbducer abducer = new CatsAbducer(backgroundKnowledge, observation);
        abducer.setAlgorithm(Algorithm.MXP);

        AbductionMonitor monitor = abducer.getAbductionMonitor();
        monitor.setWaitLimit(1000);

        Thread thread = new Thread(abducer);
        thread.start();

        while(true){
            try{
                synchronized (monitor){
                    monitor.wait();

                    if (monitor.areNewExplanationsAvailable()){
                        Set<IExplanation> newExplanations = monitor.getUnprocessedExplanations();
                        System.out.println(newExplanations);
                        monitor.markExplanationsAsProcessed();
                        monitor.clearExplanations();
                    }

                    if (monitor.isNewProgressAvailable()){
                        Percentage progress = monitor.getProgressPercentage();
                        String message = monitor.getStatusMessage();
                        System.out.println(progress + " | " + message);
                        monitor.markProgressAsProcessed();
                    }

                    if (monitor.getProgressPercentage().getValue() >= 100){
                        thread.interrupt();
                        monitor.notify();
                        break;
                    }

                    monitor.notify();
                }
            } catch(InterruptedException e){
                e.printStackTrace();
            }

        }
        System.out.println("EXPLANATIONS FOUND: " + abducer.getExplanations());

//        System.out.println("-----------------------------------------");
//        System.out.println("OUTPUT MESSAGE: " + abducer.getOutputMessage());
//        System.out.println("-----------------------------------------");
//        System.out.println("FULL LOG:");
//        System.out.println(abducer.getFullLog());
    }

    public static ISolver runSolving(String[] args, ThreadTimes threadTimes) throws Exception {

        ISolver solver = null;

        try{

            ArgumentParser argumentParser = new ArgumentParser();
            argumentParser.parse(args);

            threadTimes.start();

            ILoader loader = new ConsoleLoader();
            loader.initialize(Configuration.REASONER);

            IReasonerManager reasonerManager = new ReasonerManager(loader);

            solver = createSolver(threadTimes, loader, reasonerManager);
            solver.solve(loader, reasonerManager);

        } catch(Throwable e){
            new ConsolePrinter().logError("An error occurred:", e);
            throw e;
        }

        return solver;
    }

    private static ISolver createSolver(ThreadTimes threadTimes, ILoader loader, IReasonerManager reasonerManager) {

        ConsoleExplanationManager explanationManager = new ConsoleExplanationManager(loader, reasonerManager);
        ConsoleProgressManager progressManager = new ConsoleProgressManager();

        if (Configuration.ALGORITHM == Algorithm.MXP)
            return new MxpSolver(threadTimes, explanationManager, progressManager, new ConsolePrinter());
        else if (Configuration.ALGORITHM.isHst())
            return new HstHybridSolver(threadTimes, explanationManager, progressManager, new ConsolePrinter());
        else
            return new HybridSolver(threadTimes, explanationManager, progressManager, new ConsolePrinter());
    }
}
