package sk.uniba.fmph.dai.cats.api_implementation;

import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAxiom;
import sk.uniba.fmph.dai.abduction_api.abducible.IAxiomAbducibles;
import sk.uniba.fmph.dai.abduction_api.exception.AxiomAbducibleException;
import sk.uniba.fmph.dai.cats.data.Abducibles;
import sk.uniba.fmph.dai.cats.reasoner.Loader;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class CatsAxiomAbducibles extends CatsAbducibles implements IAxiomAbducibles {

    Set<OWLAxiom> axioms = new HashSet<>();

    public CatsAxiomAbducibles(){}

    public CatsAxiomAbducibles(Collection<OWLAxiom> axioms) throws AxiomAbducibleException {
        addAll(axioms);
    }

    @Override
    public void setAxioms(Set<OWLAxiom> axioms) throws AxiomAbducibleException {
        Set<OWLAxiom> newAxioms = new HashSet<>();
        axioms.forEach(axiom -> addAxiomToSet(axiom, newAxioms));
        this.axioms = newAxioms;
    }

    private void addAxiomToSet(OWLAxiom axiom, Set<OWLAxiom> set) {
        AxiomType<?> type = axiom.getAxiomType();
        if (
                type == AxiomType.CLASS_ASSERTION
                        || type == AxiomType.NEGATIVE_OBJECT_PROPERTY_ASSERTION
                        || type == AxiomType.OBJECT_PROPERTY_ASSERTION
        )
            set.add(axiom);
        else
            throw new AxiomAbducibleException(axiom);
    }

    @Override
    public Set<OWLAxiom> getAxioms() {
        return axioms;
    }

    @Override
    public void add(OWLAxiom axiom) throws AxiomAbducibleException {
        addAxiomToSet(axiom, axioms);
    }

    @Override
    public void addAll(Collection<OWLAxiom> axioms) throws AxiomAbducibleException {
        Set<OWLAxiom> newAxioms = new HashSet<>();
        for (OWLAxiom axiom : axioms)
            addAxiomToSet(axiom, newAxioms);
        this.axioms.addAll(newAxioms);
    }

    @Override
    public void remove(OWLAxiom owlAxiom) throws AxiomAbducibleException {
        axioms.remove(owlAxiom);
    }

    @Override
    public void removeAll(Collection<OWLAxiom> collection) throws AxiomAbducibleException {
        axioms.removeAll(collection);
    }

    @Override
    public Abducibles exportAbducibles(Loader loader) {
        return new Abducibles(loader, axioms);
    }

    @Override
    public boolean isEmpty(){
        return axioms.isEmpty();
    }

    @Override
    public void clear() {
        axioms.clear();
    }

    @Override
    public int size() {
        return axioms.size();
    }
}
