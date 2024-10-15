package sk.uniba.fmph.dai.cats.algorithms.hybrid;

import sk.uniba.fmph.dai.cats.common.Configuration;
import sk.uniba.fmph.dai.cats.common.ConsolePrinter;
import sk.uniba.fmph.dai.cats.data.Explanation;

public class ConsoleExplanationManager extends ExplanationManager {

    public ConsoleExplanationManager(){
        printer = new ConsolePrinter();
    }

    @Override
    public void addPossibleExplanation(Explanation explanation) {
        possibleExplanations.add(explanation);
    }

    @Override
    public void processExplanations(String message) {

        showExplanations();
        logMessages(Configuration.getInfo(), message);

        if (message != null){
            printer.print('\n' + message);
        }
    }

}
