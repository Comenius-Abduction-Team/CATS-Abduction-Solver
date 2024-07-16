package sk.uniba.fmph.dai.cats.algorithms.hst;

import org.semanticweb.owlapi.model.OWLAxiom;

import java.util.*;

public class NumberedAxiomsSingleMap implements INumberedAbducibles {

    public static final int DEFAULT_INDEX = -100;

    Map<OWLAxiom, Integer> axiomToIndex = new HashMap<>();

    public NumberedAxiomsSingleMap(Collection<OWLAxiom> owlAxioms) {
        addAll(owlAxioms);
    }

    @Override
    public Set<OWLAxiom> getAxioms() {
        return Collections.unmodifiableSet(axiomToIndex.keySet());
    }

    public void add(OWLAxiom axiom) {
        axiomToIndex.put(axiom, DEFAULT_INDEX);
    }

    public void addAll(Collection<OWLAxiom> axioms) {
        axioms.forEach(this::add);
    }

    public void remove(OWLAxiom axiom) {
        axiomToIndex.remove(axiom);
    }

    public boolean contains(OWLAxiom axiom) {
        return axiomToIndex.containsKey(axiom);
    }

    public Integer getIndex(OWLAxiom axiom){
        return axiomToIndex.get(axiom);
    }

    public void addWithIndex(OWLAxiom axiom, Integer index){
        axiomToIndex.put(axiom,index);
    }

    @Override
    public String toString() {
        return axiomToIndex.toString();
    }

    public OWLAxiom getAxiomByIndex(int index){
        for (OWLAxiom axiom : axiomToIndex.keySet()){
            if (axiomToIndex.get(axiom).equals(index))
                return axiom;
        }
        return null;
    }

    @Override
    public boolean shouldBeIndexed(OWLAxiom axiom) {
        return (DEFAULT_INDEX == getIndex(axiom));
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
