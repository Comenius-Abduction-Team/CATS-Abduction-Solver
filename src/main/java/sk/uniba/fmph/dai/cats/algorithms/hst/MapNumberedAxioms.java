package sk.uniba.fmph.dai.cats.algorithms.hst;

import org.semanticweb.owlapi.model.OWLAxiom;
import sk.uniba.fmph.dai.cats.algorithms.TransformedAbducibles;

import java.util.*;

public class MapNumberedAxioms
        implements INumberedAbducibles
{

    private final List<OWLAxiom> assertionAxioms;

    private final List<OWLAxiom> negatedAssertionAxioms;
    public static final int DEFAULT_INDEX = -100;

    Map<OWLAxiom, Integer> axiomToIndex = new HashMap<>();

    public MapNumberedAxioms(TransformedAbducibles transformedAbducibles) {
        assertionAxioms = transformedAbducibles.assertionAxioms;
        negatedAssertionAxioms = transformedAbducibles.negAssertionAxioms;
        addAll(transformedAbducibles.abducibleAxioms);
    }

//    @Override
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

    public void assignIndex(OWLAxiom axiom, Integer index){
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
    public boolean isIndexed(OWLAxiom axiom) {
        return (DEFAULT_INDEX == getIndex(axiom));
    }

    @Override
    public int size() {
        return axiomToIndex.size();
    }

    @Override
    public boolean areAllAbduciblesIndexed() {
        for (OWLAxiom axiom : axiomToIndex.keySet())
            if (isIndexed(axiom))
                return false;
        return true;
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
