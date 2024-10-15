package sk.uniba.fmph.dai.cats.api_implementation;

import sk.uniba.fmph.dai.cats.algorithms.hybrid.ExplanationManager;
import sk.uniba.fmph.dai.cats.common.Configuration;
import sk.uniba.fmph.dai.cats.common.StringFactory;
import sk.uniba.fmph.dai.cats.data.Explanation;
import sk.uniba.fmph.dai.cats.reasoner.Loader;
import sk.uniba.fmph.dai.cats.reasoner.ReasonerManager;

public class ApiExplanationManager extends ExplanationManager {

    private final CatsAbducer abducer;

    public ApiExplanationManager(CatsAbducer Abducer) {
        this.abducer = Abducer;
        printer = new ApiPrinter(Abducer);
    }

    public void addPossibleExplanation(Explanation explanation) {
        possibleExplanations.add(explanation);
        if (Configuration.DEBUG_PRINT)
            System.out.println("[EXPLANATION] possible explanation added: " +
                    StringFactory.getRepresentation(explanation.getAxioms()));
        try {
            if (abducer.isMultithread())
                abducer.sendExplanation(explanation);
        } catch(InterruptedException ignored){}
    }

    public void processExplanations(String message) {
        if (! (message == null))
            abducer.setMessage(message);
        showExplanations();
        abducer.setExplanations(finalExplanations);
    }
}
