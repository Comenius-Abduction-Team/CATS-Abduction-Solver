package sk.uniba.fmph.dai.cats.data;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import sk.uniba.fmph.dai.cats.common.StringFactory;

import java.util.List;

public class Observation {

    private final OWLAxiom axiom;
    private final List<OWLAxiom> axiomsInMultipleObservations;
    private final OWLNamedIndividual reductionIndividual;

    public Observation(OWLAxiom axiom) {
        this.axiom = axiom;
        this.axiomsInMultipleObservations = null;
        this.reductionIndividual = null;
    }

    public Observation(OWLAxiom axiom, List<OWLAxiom> axiomsInMultipleObservations, OWLNamedIndividual reductionIndividual) {
        this.axiom = axiom;
        this.axiomsInMultipleObservations = axiomsInMultipleObservations;
        this.reductionIndividual = reductionIndividual;
    }

    public OWLAxiom getOwlAxiom() {
        return axiom;
    }

    public List<OWLAxiom> getAxiomsInMultipleObservations() {
        return axiomsInMultipleObservations;
    }

    public OWLNamedIndividual getReductionIndividual() {
        return reductionIndividual;
    }

    @Override
    public String toString() {
        if(axiomsInMultipleObservations == null || reductionIndividual == null){
            return StringFactory.getRepresentation(axiom);
        }
        StringBuilder result = new StringBuilder();
        result.append("\n Reduced observation: ");
        result.append(StringFactory.getRepresentation(axiom));
        result.append("\n Observation consist of multiple axioms:");
        for (OWLAxiom a : axiomsInMultipleObservations){
            result.append(" ").append(StringFactory.getRepresentation(a));
        }
        result.append("\n");
        return result.toString();
    }

}
