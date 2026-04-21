package sk.uniba.fmph.dai.cats.algorithms;

import org.semanticweb.owlapi.model.OWLAxiom;

import java.util.List;
import java.util.Set;

/** A collection of axioms that are allowed to appear in the explanations.
 * Used directly in the solving algorithms (as opposed to other abducible classes which are used in pre-processing). */
public interface IAbducibleAxioms {

    Set<OWLAxiom> getAxioms();

    int size();

    boolean contains(OWLAxiom axiom);

    List<OWLAxiom> getAssertionAxioms();

    List<OWLAxiom> getNegatedAssertionAxioms();

}
