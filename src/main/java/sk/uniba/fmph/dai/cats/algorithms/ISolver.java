package sk.uniba.fmph.dai.cats.algorithms;

import sk.uniba.fmph.dai.cats.models.Explanation;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import sk.uniba.fmph.dai.cats.reasoner.ILoader;
import sk.uniba.fmph.dai.cats.reasoner.IReasonerManager;

import java.util.Collection;


public interface ISolver {

    void solve(ILoader loader, IReasonerManager reasonerManager) throws OWLOntologyStorageException, OWLOntologyCreationException;

    Collection<Explanation> getExplanations();

}
