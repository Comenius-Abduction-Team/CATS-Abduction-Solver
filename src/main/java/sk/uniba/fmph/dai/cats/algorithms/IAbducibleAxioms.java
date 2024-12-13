package sk.uniba.fmph.dai.cats.algorithms;

import org.semanticweb.owlapi.model.OWLAxiom;

import java.util.Set;

public interface IAbducibleAxioms {

    Set<OWLAxiom> getAxioms();
    int size();
    boolean contains(OWLAxiom axiom);

}
