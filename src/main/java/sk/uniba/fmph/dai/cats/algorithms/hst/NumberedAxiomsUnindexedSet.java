package sk.uniba.fmph.dai.cats.algorithms.hst;

import org.semanticweb.owlapi.model.OWLAxiom;
import sk.uniba.fmph.dai.cats.algorithms.TransformedAbducibles;
import sk.uniba.fmph.dai.cats.common.StaticPrinter;
import sk.uniba.fmph.dai.cats.common.StringFactory;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NumberedAxiomsUnindexedSet implements INumberedAbducibles {

    private final Set<OWLAxiom> allAbducibles;

    private final List<OWLAxiom> assertionAxioms;

    private final List<OWLAxiom> negatedAssertionAxioms;
    private final Set<OWLAxiom> unindexed = new HashSet<>();
    private int unindexedSize;

    private boolean allAbduciblesIndexed = false;

    private final OWLAxiom[] indexToAxiom;

    private final int max;

    public NumberedAxiomsUnindexedSet(TransformedAbducibles transformedAbducibles) {
        allAbducibles = transformedAbducibles.abducibleAxioms;
        assertionAxioms = transformedAbducibles.assertionAxioms;
        negatedAssertionAxioms = transformedAbducibles.negAssertionAxioms;
        addAll(transformedAbducibles.abducibleAxioms);
        max = unindexedSize = unindexed.size();
        indexToAxiom = new OWLAxiom[max];
    }

    @Override
    public Set<OWLAxiom> getAxioms() {
        return allAbducibles;
//        if (unindexedSize == max)
//            return new HashSet<>(unindexed);
//        Set<OWLAxiom> result = new HashSet<>(unindexed);
//        for (int i = 0; i < max; i++) {
//            OWLAxiom axiom = indexToAxiom[i];
//            if (axiom != null){
//                result.add(axiom);
//            }
//        }
//        return result;
    }

    private void addAll(Collection<OWLAxiom> axioms) {
        unindexed.addAll(axioms);
    }

    @Override
    public void addWithIndex(OWLAxiom axiom, Integer index){
        if (!unindexed.contains(axiom))
            throw new RuntimeException("Axiom " + StringFactory.getRepresentation(axiom) + " is not in the abducibles!");
        if (index < 1 || index > max)
            throw new IndexOutOfBoundsException("Index " + index + "out of bounds of the numbered axioms.");
        indexToAxiom[index-1] = axiom;
        unindexed.remove(axiom);
        unindexedSize--;
        if (unindexedSize == 0)
            allAbduciblesIndexed = true;
        StaticPrinter.debugPrint("[HST] New numbering: " + StringFactory.getRepresentation(axiom) + " = " + index);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("{ ");
        for (int i = 0; i < max; i++) {
            OWLAxiom axiom = indexToAxiom[i];
            if (axiom == null)
                continue;

            builder.append(i);
            builder.append(" -> ");
            builder.append(StringFactory.getRepresentation(axiom));
            builder.append("; ");
        }
        for (OWLAxiom axiom : unindexed){
            builder.append(StringFactory.getRepresentation(axiom));
            builder.append("; ");
        }
        builder.append(" }");

        return builder.toString();
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
            if (axiom.equals(indexToAxiom[i]))
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
    public boolean areAllAbduciblesIndexed() {
        return allAbduciblesIndexed;
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
