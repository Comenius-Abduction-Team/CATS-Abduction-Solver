package sk.uniba.fmph.dai.cats.models;

import sk.uniba.fmph.dai.abduction_api.abducer.IExplanation;
import sk.uniba.fmph.dai.cats.common.StringFactory;
import org.semanticweb.owlapi.model.OWLAxiom;

import java.util.*;

public class Explanation implements IExplanation {

    private final List<OWLAxiom> axioms;

    private Integer depth;

    private double acquireTime;

    private Integer level;

    public Explanation(List<OWLAxiom> axioms) {
        this.axioms = axioms;
    }

    public Explanation(Collection<OWLAxiom> axioms, Integer depth, Integer level, double acquireTime) {
        this.axioms = new ArrayList<>(axioms);
        this.depth = depth;
        this.acquireTime = acquireTime;
        this.level = level;
    }

    public Explanation() {
        this.axioms = new ArrayList<>();
        this.depth = 0;
        this.level = -1;
    }

    public List<OWLAxiom> getAxioms() {
        return axioms;
    }

    @Override
    public Set<OWLAxiom> getAxiomSet() {
        return new HashSet<>(axioms);
    }

    @Override
    public String getTextRepresentation() {
        return toString();
    }

    @Override
    public int size() {
        return axioms.size();
    }

    public Integer getDepth() {
        return depth;
    }

    public double getAcquireTime() { return acquireTime; }

    public void setAcquireTime(double time) { this.acquireTime = time; }

    public void setDepth(Integer depth) {
        this.depth = depth;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public void addAxioms(Collection<OWLAxiom> axioms) {
        this.axioms.addAll(axioms);
    }

    public void addAxiom(OWLAxiom axiom) {
        this.axioms.add(axiom);
    }

    @Override
    public String toString() {
        return StringFactory.getRepresentation(axioms);
    }

    @Override
    public int hashCode() {
        return axioms.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Explanation) {
            Explanation exp = (Explanation) obj;
            return exp.getAxioms().equals(axioms);
        }

        return false;
    }
}
