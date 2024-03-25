package sk.uniba.fmph.dai.cats.algorithms.hybrid;

import org.semanticweb.owlapi.model.OWLAxiom;
import sk.uniba.fmph.dai.cats.models.AxiomSet;

import java.util.Set;

public interface IAbducibleAxioms {

    Set<OWLAxiom> getAxioms();
    int size();

    boolean contains(OWLAxiom axiom);

    AxiomSet getAsAxiomSet();

}
