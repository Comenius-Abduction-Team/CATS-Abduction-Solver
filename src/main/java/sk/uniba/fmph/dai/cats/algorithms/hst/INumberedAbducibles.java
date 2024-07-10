package sk.uniba.fmph.dai.cats.algorithms.hst;

import sk.uniba.fmph.dai.cats.algorithms.hybrid.IAbducibleAxioms;
import org.semanticweb.owlapi.model.OWLAxiom;

public interface INumberedAbducibles extends IAbducibleAxioms {

    boolean shouldBeIndexed(OWLAxiom axiom);

    void addWithIndex(OWLAxiom axiom, Integer index);

    OWLAxiom getAxiomByIndex(int index);

    boolean areAllAbduciblesIndexed();

}
