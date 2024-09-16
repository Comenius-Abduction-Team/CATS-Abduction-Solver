package sk.uniba.fmph.dai.cats.algorithms;

import sk.uniba.fmph.dai.cats.data.Explanation;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import sk.uniba.fmph.dai.cats.reasoner.Loader;
import sk.uniba.fmph.dai.cats.reasoner.ReasonerManager;

import java.util.Collection;


public interface ISolver {

    void solve(Loader loader, ReasonerManager reasonerManager) throws OWLOntologyStorageException, OWLOntologyCreationException;

    Collection<Explanation> getExplanations();

}
