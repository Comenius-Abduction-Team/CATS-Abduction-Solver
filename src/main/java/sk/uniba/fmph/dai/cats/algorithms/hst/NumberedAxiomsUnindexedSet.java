package sk.uniba.fmph.dai.cats.algorithms.hst;

import org.semanticweb.owlapi.model.OWLAxiom;
import sk.uniba.fmph.dai.cats.models.AxiomSet;

import java.util.*;

public class NumberedAxiomsUnindexedSet implements INumberedAbducibles {

    Set<OWLAxiom> unindexed = new HashSet<>();

    OWLAxiom[] indexToAxiom;

    private final int max;

    public NumberedAxiomsUnindexedSet(Collection<OWLAxiom> owlAxioms) {
        addAll(owlAxioms);
        max = owlAxioms.size();
        indexToAxiom = new OWLAxiom[max];
    }

    @Override
    public Set<OWLAxiom> getAxioms() {
        if (unindexed.size() == max)
            return Collections.unmodifiableSet(unindexed);
        Set<OWLAxiom> result = new HashSet<>(unindexed);
        for (int i = 0; i < max; i++) {
            OWLAxiom axiom = indexToAxiom[i];
            if (axiom != null){
                result.add(axiom);
            }
        }
        return result;
    }

    public void add(OWLAxiom axiom) {
        unindexed.add(axiom);
    }

    public void addAll(Collection<OWLAxiom> axioms) {
        unindexed.addAll(axioms);
    }

    @Override
    public void addWithIndex(OWLAxiom axiom, Integer index){
        if (index < 1 || index > max)
            throw new IndexOutOfBoundsException("Index " + index + "out of bounds of the numbered axioms.");
        indexToAxiom[index-1] = axiom;
        unindexed.remove(axiom);
    }

    @Override
    public String toString() {
        return unindexed.toString();
    }

    @Override
    public OWLAxiom getAxiomByIndex(int index){
        if (index < 1 || index > max)
            throw new IndexOutOfBoundsException("Index " + index + "out of bounds of the numbered axioms.");
        return indexToAxiom[index-1];
    }

    @Override
    public boolean contains(OWLAxiom axiom) {
        return unindexed.contains(axiom) || indexedContains(axiom);
    }

    private boolean indexedContains(OWLAxiom axiom){
        for (int i = 0; i < max; i++) {
            if (indexToAxiom[i] != null && indexToAxiom[i].equals(axiom))
                return true;
        }
        return false;
    }

    @Override
    public boolean shouldBeIndexed(OWLAxiom axiom) {
        return unindexed.contains(axiom);
    }

    @Override
    public int size() {
        return max;
    }

    @Override
    public AxiomSet getAsAxiomSet() {
        return new AxiomSet(getAxioms());
    }
}
