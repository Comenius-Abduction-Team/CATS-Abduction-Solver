package sk.uniba.fmph.dai.cats.algorithms;

import org.semanticweb.owlapi.model.OWLAxiom;
import sk.uniba.fmph.dai.cats.data.AxiomSet;

import java.util.List;
import java.util.Set;

public class AxiomSetAbducibles implements IAbducibleAxioms {

    private final AxiomSet axioms;

    private final List<OWLAxiom> assertionAxioms;

    private final List<OWLAxiom> negatedAssertionAxioms;

    public AxiomSetAbducibles(TransformedAbducibles transformedAbducibles){
        axioms = new AxiomSet(transformedAbducibles.abducibleAxioms);
        assertionAxioms = transformedAbducibles.assertionAxioms;
        negatedAssertionAxioms = transformedAbducibles.negAssertionAxioms;
    }

    @Override
    public Set<OWLAxiom> getAxioms() {
        return axioms.getAxioms();
    }

    @Override
    public int size() {
        return axioms.size();
    }

    @Override
    public boolean contains(OWLAxiom axiom) {
        return axioms.contains(axiom);
    }

    @Override
    public List<OWLAxiom> getAssertionAxioms() {
        return assertionAxioms;
    }

    @Override
    public List<OWLAxiom> getNegatedAssertionAxioms() {
        return negatedAssertionAxioms;
    }
}
