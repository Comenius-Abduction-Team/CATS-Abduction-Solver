package sk.uniba.fmph.dai.cats.api_implementation;

import sk.uniba.fmph.dai.cats.algorithms.hybrid.ExplanationManager;
import sk.uniba.fmph.dai.cats.data.Explanation;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import sk.uniba.fmph.dai.cats.reasoner.ILoader;
import sk.uniba.fmph.dai.cats.reasoner.IReasonerManager;

public class ApiExplanationManager extends ExplanationManager {

    private final CatsAbducer Abducer;

    public ApiExplanationManager(ILoader loader, IReasonerManager reasonerManager, CatsAbducer Abducer) {
        super(loader, reasonerManager);
        this.Abducer = Abducer;
        printer = new ApiPrinter(Abducer);
    }

    public void addPossibleExplanation(Explanation explanation) {
        possibleExplanations.add(explanation);
        try {
            if (Abducer.isMultithread())
                Abducer.sendExplanation(explanation);
        } catch(InterruptedException ignored){}
    }

    public void processExplanations(String message) throws OWLOntologyCreationException, OWLOntologyStorageException {
        if (! (message == null))
            Abducer.setMessage(message);
        showExplanations();
        Abducer.setExplanations(finalExplanations);
    }
}
