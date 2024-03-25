package sk.uniba.fmph.dai.cats;

import sk.uniba.fmph.dai.abduction_api.abducer.IAbducer;
import sk.uniba.fmph.dai.abduction_api.abducible.ISymbolAbducibles;
import sk.uniba.fmph.dai.abduction_api.abducer.IExplanation;
import sk.uniba.fmph.dai.abduction_api.abducer.IThreadAbducer;
import sk.uniba.fmph.dai.abduction_api.monitor.AbductionMonitor;
import sk.uniba.fmph.dai.abduction_api.monitor.Percentage;
import sk.uniba.fmph.dai.cats.algorithms.ISolver;
import sk.uniba.fmph.dai.cats.algorithms.hst.HstHybridSolver;
import sk.uniba.fmph.dai.cats.algorithms.hybrid.ConsoleExplanationManager;
import sk.uniba.fmph.dai.cats.algorithms.hybrid.HybridSolver;
import sk.uniba.fmph.dai.cats.api_implementation.CatsAbductionFactory;
import sk.uniba.fmph.dai.cats.application.Application;
import sk.uniba.fmph.dai.cats.application.ExitCode;
import sk.uniba.fmph.dai.cats.common.Configuration;
import sk.uniba.fmph.dai.cats.common.ConsolePrinter;
import sk.uniba.fmph.dai.cats.logger.FileLogger;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.DefaultPrefixManager;
import sk.uniba.fmph.dai.cats.parser.ArgumentParser;
import sk.uniba.fmph.dai.cats.progress.ConsoleProgressManager;
import sk.uniba.fmph.dai.cats.reasoner.*;
import sk.uniba.fmph.dai.cats.timer.ThreadTimes;

import java.io.*;
import java.util.Collections;
import java.util.Set;
public class Main {

    /** whether the solver is being run from an IDE*/
    private static boolean TESTING = true;
    /** whether the solver is being run from an IDE through the API*/
    private static final boolean API = false;

    //private static final String INPUT_FILE = "in/eval/lubm-0_2_0_noNeg.in";
    private static final String INPUT_FILE = "in/testExtractingModels/pokus9_2.in";

    public static void main(String[] args) throws Exception {

        FileLogger.initializeLogger();

        if (TESTING){
            if (API){
                runApiTestingMain();
                return;
            }

            args = new String[]{INPUT_FILE};
        }

        Logger logger = Logger.getRootLogger();
        logger.setLevel(Level.OFF);
        BasicConfigurator.configure();

        ThreadTimes threadTimes = new ThreadTimes(100);

        try{
            runSolving(args, threadTimes, logger);
        } catch(Throwable e) {
            e.printStackTrace();
            Application.finish(ExitCode.ERROR);
        } finally {
            threadTimes.interrupt();
        }

    }

    private static void runApiTestingMain() throws OWLOntologyCreationException {

        Configuration.PRINT_PROGRESS = true;

        CatsAbductionFactory factory = CatsAbductionFactory.getFactory();

        OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager();
        File file = new File("files/testExtractingModel9_1.owl");
        OWLOntology ont = ontologyManager.loadOntologyFromOntologyDocument(file);

        OWLDataFactory dataFactory = ontologyManager.getOWLDataFactory();
        String prefix = "http://www.co-ode.org/ontologies/ont.owl#";
        PrefixManager pm = new DefaultPrefixManager(prefix);
        OWLClass A = dataFactory.getOWLClass(":A", pm);
        OWLClass C = dataFactory.getOWLClass(":C", pm);
        OWLClass E = dataFactory.getOWLClass(":E", pm);
        OWLNamedIndividual a = dataFactory.getOWLNamedIndividual(":a", pm);
        OWLClassAssertionAxiom classAssertion = dataFactory.getOWLClassAssertionAxiom(
                dataFactory.getOWLObjectIntersectionOf(A,C,E), a);

        ISymbolAbducibles abducibles = factory.getSymbolAbducibles();
        abducibles.add(A);
        abducibles.add(C);

        IAbducer abducer = factory.getAbducer(ont, Collections.singleton(classAssertion));
        abducer.setSolverSpecificParameters("");

        IThreadAbducer threadAbducer = (IThreadAbducer) abducer;
        AbductionMonitor monitor = threadAbducer.getAbductionMonitor();
        monitor.setWaitLimit(1000);

        Thread thread = new Thread(threadAbducer);
        thread.start();

        while(true){
            try{
                synchronized (monitor){
                    monitor.wait();

                    if (monitor.areNewExplanationsAvailable()){
                        Set<IExplanation> expl = monitor.getUnprocessedExplanations();
                        System.out.println(expl);
                        monitor.markExplanationsAsProcessed();
                        monitor.clearExplanations();
                    }

                    if (monitor.isNewProgressAvailable()){
                        Percentage progress = monitor.getProgress();
                        String message = monitor.getStatusMessage();
                        System.out.println(progress + "//" + message);
                        monitor.markProgressAsProcessed();
                    }

                    if (monitor.getProgress().getValue() >= 100){
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
        System.out.println("EXPLANATIONS FOUND: " + threadAbducer.getExplanations());

        System.out.println("-----------------------------------------");
        System.out.println("OUTPUT MESSAGE: " + threadAbducer.getOutputMessage());
        System.out.println("-----------------------------------------");
        System.out.println("FULL LOG:");
        System.out.println(threadAbducer.getFullLog());
    }

    public static ISolver runSolving(String[] args, ThreadTimes threadTimes, Logger logger) throws Exception {

        ISolver solver = null;

        try{

            ArgumentParser argumentParser = new ArgumentParser();
            argumentParser.parse(args);

            threadTimes.start();

            ILoader loader = new ConsoleLoader();
            loader.initialize(Configuration.REASONER);

            IReasonerManager reasonerManager = new ReasonerManager(loader);

            solver = createSolver(threadTimes, loader, reasonerManager, logger);
            solver.solve(loader, reasonerManager);

        } catch(Throwable e){
            new ConsolePrinter(logger).logError("An error occurred: ", e);
            throw e;
        }

        return solver;
    }

    private static ISolver createSolver(ThreadTimes threadTimes, ILoader loader, IReasonerManager reasonerManager, Logger logger) {

        ConsoleExplanationManager explanationManager = new ConsoleExplanationManager(loader, reasonerManager);
        ConsoleProgressManager progressManager = new ConsoleProgressManager();

        if (Configuration.ALGORITHM.isHst())
            return new HstHybridSolver(threadTimes, explanationManager, progressManager, new ConsolePrinter(logger));
        else
            return new HybridSolver(threadTimes, explanationManager, progressManager, new ConsolePrinter(logger));
    }
}
