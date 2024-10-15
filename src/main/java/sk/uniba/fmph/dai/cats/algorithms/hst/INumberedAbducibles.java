package sk.uniba.fmph.dai.cats.algorithms.hst;

import org.semanticweb.owlapi.model.OWLAxiom;
import sk.uniba.fmph.dai.cats.algorithms.hybrid.IAbducibleAxioms;

public interface INumberedAbducibles extends IAbducibleAxioms {

    boolean shouldBeIndexed(OWLAxiom axiom);

    void addWithIndex(OWLAxiom axiom, Integer index);

    OWLAxiom getAxiomByIndex(int index);

    boolean areAllAbduciblesIndexed();

}
