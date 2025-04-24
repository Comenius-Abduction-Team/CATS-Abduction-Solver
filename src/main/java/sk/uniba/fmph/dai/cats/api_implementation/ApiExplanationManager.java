package sk.uniba.fmph.dai.cats.api_implementation;

import sk.uniba.fmph.dai.cats.data_processing.ExplanationManager;
import sk.uniba.fmph.dai.cats.data.Explanation;
import sk.uniba.fmph.dai.cats.data_processing.TreeStats;

public class ApiExplanationManager extends ExplanationManager {

    private final CatsAbducer abducer;

    public ApiExplanationManager(CatsAbducer Abducer) {
        this.abducer = Abducer;
        printer = new ApiPrinter(Abducer);
    }

    @Override
    public void addPossibleExplanation(Explanation explanation) {
        super.addPossibleExplanation(explanation);
        try {
            if (abducer.isMultithread())
                abducer.sendExplanation(explanation);
        } catch(InterruptedException ignored){}
    }

    public void processExplanations(String message, TreeStats stats) {
        if (! (message == null))
            abducer.setMessage(message);
        showExplanations(message, stats);
        abducer.setExplanations(finalExplanations);
    }
}
