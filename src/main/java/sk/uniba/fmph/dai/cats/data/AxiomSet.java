package sk.uniba.fmph.dai.cats.data;

import org.semanticweb.owlapi.model.OWLAxiom;
import sk.uniba.fmph.dai.cats.algorithms.hybrid.IAbducibleAxioms;
import sk.uniba.fmph.dai.cats.common.StringFactory;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class AxiomSet implements IAbducibleAxioms {

    private final Set<OWLAxiom> axioms;

    public AxiomSet() {
        this.axioms = new HashSet<>();
    }

    public AxiomSet(Set<OWLAxiom> axioms) {
        this.axioms = axioms;
    }

    public AxiomSet(Collection<OWLAxiom> axioms) {
        this.axioms = new HashSet(axioms);
    }

    @Override
    public Set<OWLAxiom> getAxioms() {
        return axioms;
    }

    @Override
    public String toString() {
        return StringFactory.getRepresentation(axioms);
    }

    public void remove(OWLAxiom literal) {
        axioms.remove(literal);
    }

    public void removeAll(Collection<OWLAxiom> literals){
        axioms.removeAll(literals);
    }

    public Set<OWLAxiom> removeAllAndReturn(Collection<OWLAxiom> axioms){
        Set<OWLAxiom> removed = new HashSet<>();
        for (OWLAxiom axiom : axioms){
            if (this.axioms.contains(axiom)){
                this.axioms.remove(axiom);
                removed.add(axiom);
            }
        }
        return removed;
    }

    public void add(OWLAxiom literal) {
        axioms.add(literal);
    }

    public void addAll(Collection<OWLAxiom> literals){
        axioms.addAll(literals);
    }

    @Override
    public int size(){
        return axioms.size();
    }

    public boolean isEmpty() {  return axioms.isEmpty(); }

    @Override
    public boolean contains(OWLAxiom axiom) {
        return axioms.contains(axiom);
    }

}
