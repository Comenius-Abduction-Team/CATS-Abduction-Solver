package sk.uniba.fmph.dai.cats.algorithms.hst;

import org.semanticweb.owlapi.model.OWLAxiom;

import java.util.*;

public class NumberedAxiomsMap implements INumberedAbducibles {

    public static final Integer DEFAULT_INDEX = -100;

    Map<OWLAxiom, Integer> axiomToIndex = new HashMap<>();

    Map<Integer, OWLAxiom> indexToAxiom = new HashMap<>();

    public NumberedAxiomsMap(Collection<OWLAxiom> owlAxioms) {
        addAll(owlAxioms);
    }

    @Override
    public Set<OWLAxiom> getAxioms() {
        return Collections.unmodifiableSet(axiomToIndex.keySet());
    }

    public void add(OWLAxiom axiom) {
        if (!axiomToIndex.containsKey(axiom))
            axiomToIndex.put(axiom, DEFAULT_INDEX);
    }

    public void addAll(Collection<OWLAxiom> axioms) {
        axioms.forEach(this::add);
    }

    public void remove(OWLAxiom axiom) {
        Integer index = axiomToIndex.get(axiom);
        if (index == null)
            return;
        axiomToIndex.remove(axiom);
        indexToAxiom.remove(index);
    }

    public void removeAll(Collection<OWLAxiom> axioms) {
        axioms.forEach(this::remove);
    }

    public boolean contains(OWLAxiom axiom) {
        return axiomToIndex.containsKey(axiom);
    }

    public Integer getIndex(OWLAxiom axiom){
        return axiomToIndex.get(axiom);
    }

    public void addWithIndex(OWLAxiom axiom, Integer index){
        axiomToIndex.put(axiom,index);
        indexToAxiom.put(index,axiom);
    }

    @Override
    public String toString() {
        return axiomToIndex.toString();
    }

    @Override
    public OWLAxiom getAxiomByIndex(int index){
        return indexToAxiom.get(index);
    }

    @Override
    public boolean shouldBeIndexed(OWLAxiom axiom) {
        return (DEFAULT_INDEX.equals(getIndex(axiom)));
    }

    @Override
    public int size() {
        return axiomToIndex.size();
    }

    @Override
    public boolean areAllAbduciblesIndexed() {
        for (OWLAxiom axiom : axiomToIndex.keySet())
            if (shouldBeIndexed(axiom))
                return false;
        return true;
    }
}
