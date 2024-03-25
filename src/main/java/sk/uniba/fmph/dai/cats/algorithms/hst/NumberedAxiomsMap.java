package sk.uniba.fmph.dai.cats.algorithms.hst;

import org.semanticweb.owlapi.model.OWLAxiom;
import sk.uniba.fmph.dai.cats.models.AxiomSet;

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

//    @Override
//    public void addAll(IAxioms axioms) {
//        axioms.toSet().forEach(this::add);
//    }

//    @Override
//    public void set(Collection<OWLAxiom> axioms) {
//        this.axioms = new HashMap<>();
//        addAll(axioms);
//    }

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

//    @Override
//    public void removeAll(IAxioms axioms) {
//        this.axioms.keySet().removeAll(axioms.toSet());
//    }

    public boolean contains(OWLAxiom axiom) {
        return axiomToIndex.containsKey(axiom);
    }

    @Override
    public AxiomSet getAsAxiomSet() {
        return new AxiomSet(getAxioms());
    }

//    @Override
//    public boolean containsAll(Collection<OWLAxiom> axioms) {
//        return this.axioms.keySet().containsAll(axioms);
//    }

//    @Override
//    public boolean containsAll(IAxioms axioms) {
//        return this.axioms.keySet().containsAll(axioms.toSet());
//    }

//    @Override
//    public int size() {
//        return axioms.size();
//    }
//
//    @Override
//    public boolean isEmpty() {
//        return axioms.isEmpty();
//    }

//    @Override
//    public IAxioms copy() {
//        NumberedAxioms copy = new NumberedAxioms();
//        axioms.forEach(copy::addAxiom);
//        return copy;
//    }

//    @Override
//    public Stream<OWLAxiom> stream() {
//        return axioms.keySet().stream();
//    }

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
}
