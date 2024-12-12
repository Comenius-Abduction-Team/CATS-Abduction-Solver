package sk.uniba.fmph.dai.cats.data_processing;

import sk.uniba.fmph.dai.cats.common.ConsolePrinter;
import sk.uniba.fmph.dai.cats.common.StaticPrinter;
import sk.uniba.fmph.dai.cats.data.Explanation;

public class ConsoleExplanationManager extends ExplanationManager {

    public ConsoleExplanationManager(){
        printer = new ConsolePrinter();
    }

    @Override
    public void addPossibleExplanation(Explanation explanation) {
        possibleExplanations.add(explanation);
        StaticPrinter.debugPrint("[EXPLANATION] " + explanation + " at time: " + explanation.getAcquireTime() );
    }

    @Override
    public void processExplanations(String message, TreeStats stats) {

        showExplanations(stats);

        if (message != null){
            printer.print('\n' + message);
        }
    }

}
