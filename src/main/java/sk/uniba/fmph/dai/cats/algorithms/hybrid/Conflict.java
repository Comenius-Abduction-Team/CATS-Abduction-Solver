package sk.uniba.fmph.dai.cats.algorithms.hybrid;

import org.semanticweb.owlapi.model.OWLAxiom;
import sk.uniba.fmph.dai.cats.data.Explanation;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class Conflict {

    private Set<OWLAxiom> axioms;
    private List<Explanation> explanations;

    Conflict() {
        this.axioms = new HashSet<>();
        this.explanations = new LinkedList<>();
    }

    Conflict(Set<OWLAxiom>axioms, List<Explanation> explanations) {
        this.axioms = axioms;
        this.explanations = explanations;
    }

    Set<OWLAxiom> getAxioms() {
        if (axioms == null) {
            axioms = new HashSet<>();
        }
        return axioms;
    }

    public List<Explanation> getExplanations() {
        if (explanations == null) {
            explanations = new LinkedList<>();
        }
        return explanations;
    }
}
