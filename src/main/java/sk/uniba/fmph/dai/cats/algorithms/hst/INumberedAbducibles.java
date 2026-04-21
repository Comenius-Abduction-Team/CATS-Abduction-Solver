package sk.uniba.fmph.dai.cats.algorithms.hst;

import org.semanticweb.owlapi.model.OWLAxiom;
import sk.uniba.fmph.dai.cats.algorithms.IAbducibleAxioms;

/**
 * A collection of axioms that can be assigned numerical indices as defined in the HST algorithm.
 * */
public interface INumberedAbducibles extends IAbducibleAxioms {

    boolean isIndexed(OWLAxiom axiom);

    void assignIndex(OWLAxiom axiom, Integer index);

    OWLAxiom getAxiomByIndex(int index);

    boolean areAllAbduciblesIndexed();

}
