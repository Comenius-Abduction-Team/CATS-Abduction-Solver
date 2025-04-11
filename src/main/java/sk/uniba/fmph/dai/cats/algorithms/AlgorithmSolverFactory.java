package sk.uniba.fmph.dai.cats.algorithms;

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
import sk.uniba.fmph.dai.cats.timer.MetricsThread;

public class AlgorithmSolverFactory {

    public static AlgorithmSolver createConsoleSolver(MetricsThread metrics, Algorithm algorithm) {

        StaticPrinter.setPrinter(new ConsolePrinter());

        Loader loader = new ConsoleLoader();
        loader.initialize(Configuration.REASONER);

        ExplanationManager explanationManager = new ConsoleExplanationManager();
        ProgressManager progressManager = new ConsoleProgressManager();

        return new AlgorithmSolver(algorithm, loader, explanationManager, progressManager, metrics);

    }

    public static AlgorithmSolver createApiSolver(MetricsThread metrics, Algorithm algorithm, CatsAbducer abducer) {

        StaticPrinter.setPrinter(new ApiPrinter(abducer));

        Loader loader = new ApiLoader(abducer);
        loader.initialize(Configuration.REASONER);

        ExplanationManager explanationManager = new ApiExplanationManager(abducer);
        ProgressManager progressManager = new ApiProgressManager(abducer);

        return new AlgorithmSolver(algorithm, loader, explanationManager, progressManager, metrics);

    }

}
