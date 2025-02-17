package sk.uniba.fmph.dai.cats.data_processing;

import sk.uniba.fmph.dai.cats.common.ConsolePrinter;
import sk.uniba.fmph.dai.cats.common.StaticPrinter;
import sk.uniba.fmph.dai.cats.data.Explanation;

public class ConsoleExplanationManager extends ExplanationManager {

    public ConsoleExplanationManager(){
        printer = new ConsolePrinter();
    }

    @Override
    public void processExplanations(String message, TreeStats stats) {

        showExplanations(message, stats);
    }

}
