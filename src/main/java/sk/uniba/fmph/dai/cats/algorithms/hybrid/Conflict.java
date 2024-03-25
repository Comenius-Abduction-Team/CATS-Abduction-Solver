package sk.uniba.fmph.dai.cats.algorithms.hybrid;

import sk.uniba.fmph.dai.cats.models.AxiomSet;
import sk.uniba.fmph.dai.cats.models.Explanation;

import java.util.LinkedList;
import java.util.List;

public class Conflict {

    private AxiomSet axioms;
    private List<Explanation> explanations;

    Conflict() {
        this.axioms = new AxiomSet();
        this.explanations = new LinkedList<>();
    }

    Conflict(AxiomSet axioms, List<Explanation> explanations) {
        this.axioms = axioms;
        this.explanations = explanations;
    }

    Conflict(Conflict conflict) {
        this.axioms = new AxiomSet();
        this.axioms.addAll(conflict.getAxioms().getAxioms());

        this.explanations = new LinkedList<>();
        this.explanations.addAll(conflict.getExplanations());
    }

    AxiomSet getAxioms() {
        if (axioms == null) {
            axioms = new AxiomSet();
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
