package sk.uniba.fmph.dai.cats.algorithms.hst;

import org.semanticweb.owlapi.model.OWLAxiom;
import sk.uniba.fmph.dai.cats.algorithms.TransformedAbducibles;

import java.util.*;

public class MapArrayNumberedAxioms
        implements INumberedAbducibles
{

    private final List<OWLAxiom> assertionAxioms;

    private final List<OWLAxiom> negatedAssertionAxioms;
    public static final Integer DEFAULT_INDEX = -100;

    Map<OWLAxiom, Integer> axiomToIndex = new HashMap<>();

    OWLAxiom[] indexToAxiom;

    private final int max;

    public MapArrayNumberedAxioms(TransformedAbducibles transformedAbducibles) {
        assertionAxioms = transformedAbducibles.assertionAxioms;
        negatedAssertionAxioms = transformedAbducibles.negAssertionAxioms;
        addAll(transformedAbducibles.abducibleAxioms);
        max = transformedAbducibles.abducibleAxioms.size();
        indexToAxiom = new OWLAxiom[max];
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
        Integer index = axiomToIndex.get(axiom);
        if (index == null)
            return;
        axiomToIndex.remove(axiom);
        indexToAxiom[index] = null;
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

    @Override
    public void assignIndex(OWLAxiom axiom, Integer index){
        if (index < 1 || index > max)
            throw new IndexOutOfBoundsException("Index " + index + "out of bounds of the numbered axioms.");
        axiomToIndex.put(axiom,index);
        indexToAxiom[index-1] = axiom;
    }

    @Override
    public String toString() {
        return axiomToIndex.toString();
    }

    public OWLAxiom getAxiomByIndex(int index){
        if (index < 1 || index > max)
            throw new IndexOutOfBoundsException("Index " + index + "out of bounds of the numbered axioms.");
        return indexToAxiom[index-1];
    }

    @Override
    public boolean isIndexed(OWLAxiom axiom) {
        return (DEFAULT_INDEX.equals(getIndex(axiom)));
    }

    @Override
    public int size() {
        return max;
    }

    @Override
    public boolean areAllAbduciblesIndexed() {
        for (int i = 0; i < max; i++) {
            if (indexToAxiom[i] == null)
                return false;
        }
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
