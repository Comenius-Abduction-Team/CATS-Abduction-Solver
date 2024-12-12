package sk.uniba.fmph.dai.cats.algorithms.hybrid;

import sk.uniba.fmph.dai.cats.algorithms.Algorithm;
import sk.uniba.fmph.dai.cats.api_implementation.*;
import sk.uniba.fmph.dai.cats.common.Configuration;
import sk.uniba.fmph.dai.cats.common.ConsolePrinter;
import sk.uniba.fmph.dai.cats.common.StaticPrinter;
import sk.uniba.fmph.dai.cats.data_processing.ConsoleExplanationManager;
import sk.uniba.fmph.dai.cats.data_processing.ExplanationManager;
import sk.uniba.fmph.dai.cats.progress.ConsoleProgressManager;
import sk.uniba.fmph.dai.cats.progress.ProgressManager;
import sk.uniba.fmph.dai.cats.reasoner.ConsoleLoader;
import sk.uniba.fmph.dai.cats.reasoner.Loader;
import sk.uniba.fmph.dai.cats.reasoner.ReasonerManager;
import sk.uniba.fmph.dai.cats.timer.ThreadTimer;

public class AlgorithmSolverFactory {

    public static AlgorithmSolver createConsoleSolver(ThreadTimer timer, Algorithm algorithm) {

        Loader loader = new ConsoleLoader();
        new ReasonerManager(loader);
        loader.initialize(Configuration.REASONER);

        ExplanationManager explanationManager = new ConsoleExplanationManager();
        ProgressManager progressManager = new ConsoleProgressManager();
        StaticPrinter.setPrinter(new ConsolePrinter());

        return new AlgorithmSolver(algorithm, loader, explanationManager, progressManager, timer);

    }

    public static AlgorithmSolver createApiSolver(ThreadTimer timer, Algorithm algorithm, CatsAbducer abducer) {

        Loader loader = new ApiLoader(abducer);
        new ReasonerManager(loader);
        loader.initialize(Configuration.REASONER);

        ExplanationManager explanationManager = new ApiExplanationManager(abducer);
        ProgressManager progressManager = new ApiProgressManager(abducer);
        StaticPrinter.setPrinter(new ApiPrinter(abducer));

        return new AlgorithmSolver(algorithm, loader, explanationManager, progressManager, timer);

    }

}
