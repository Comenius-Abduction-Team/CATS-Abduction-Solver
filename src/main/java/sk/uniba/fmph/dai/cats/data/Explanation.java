package sk.uniba.fmph.dai.cats.data;

import org.semanticweb.owlapi.model.OWLAxiom;
import sk.uniba.fmph.dai.abduction_api.abducer.IExplanation;
import sk.uniba.fmph.dai.cats.common.StringFactory;

import java.util.*;

public class Explanation implements IExplanation {

    private List<OWLAxiom> axioms = new ArrayList<>();

    public OWLAxiom lastAxiom;

    private double acquireTime;

    private int level = -1;

    public Explanation(List<OWLAxiom> axioms) {
        this.axioms = axioms;
    }

    public Explanation(Collection<OWLAxiom> axioms, int level, double acquireTime) {
        this.axioms = new ArrayList<>(axioms);
        this.acquireTime = acquireTime;
        this.level = level;
    }

    public Explanation() {}

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

    public double getAcquireTime() { return acquireTime; }

    public void setAcquireTime(double time) { this.acquireTime = time; }

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
