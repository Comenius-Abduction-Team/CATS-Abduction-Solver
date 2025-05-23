package sk.uniba.fmph.dai.cats.algorithms.hst;

import org.semanticweb.owlapi.model.OWLAxiom;
import sk.uniba.fmph.dai.cats.algorithms.IAbducibleAxioms;

public interface INumberedAbducibles extends IAbducibleAxioms {

    boolean isIndexed(OWLAxiom axiom);

    void assignIndex(OWLAxiom axiom, Integer index);

    OWLAxiom getAxiomByIndex(int index);

    boolean areAllAbduciblesIndexed();

}
