package sk.uniba.fmph.dai.cats.algorithms.hybrid;

import sk.uniba.fmph.dai.cats.common.ConsolePrinter;
import sk.uniba.fmph.dai.cats.data.Explanation;
import org.semanticweb.owlapi.model.*;
import sk.uniba.fmph.dai.cats.reasoner.Loader;
import sk.uniba.fmph.dai.cats.reasoner.ReasonerManager;

public class ConsoleExplanationManager extends ExplanationManager {

    public ConsoleExplanationManager(Loader loader, ReasonerManager reasonerManager){
        super(loader, reasonerManager);
        printer = new ConsolePrinter();
    }

    @Override
    public void addPossibleExplanation(Explanation explanation) {
        possibleExplanations.add(explanation);
    }

    @Override
    public void processExplanations(String message) {

        showExplanations();
        logMessages(solver.getInfo(), message);

        if (message != null){
            printer.print('\n' + message);
        }
    }

}
