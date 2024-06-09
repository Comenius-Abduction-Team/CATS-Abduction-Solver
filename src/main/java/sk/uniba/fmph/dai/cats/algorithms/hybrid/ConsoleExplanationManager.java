package sk.uniba.fmph.dai.cats.algorithms.hybrid;

import sk.uniba.fmph.dai.cats.common.ConsolePrinter;
import sk.uniba.fmph.dai.cats.models.Explanation;
import org.semanticweb.owlapi.model.*;
import sk.uniba.fmph.dai.cats.reasoner.ILoader;
import sk.uniba.fmph.dai.cats.reasoner.IReasonerManager;

public class ConsoleExplanationManager extends ExplanationManager {

    public ConsoleExplanationManager(ILoader loader, IReasonerManager reasonerManager){
        super(loader, reasonerManager);
        printer = new ConsolePrinter();
    }

    @Override
    public void addPossibleExplanation(Explanation explanation) {
        possibleExplanations.add(explanation);
    }

    @Override
    public void processExplanations(String message) throws OWLOntologyCreationException, OWLOntologyStorageException {
        try{
            showExplanations();
        } catch (OWLOntologyCreationException | OWLOntologyStorageException e) {
            e.printStackTrace();
        }
        logMessages(solver.getInfo(), message);

        if (message != null){
            printer.print('\n' + message);
        }
    }

}
