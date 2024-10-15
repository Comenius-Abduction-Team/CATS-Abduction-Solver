package sk.uniba.fmph.dai.cats.reasoner;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

import java.util.Collection;
import java.util.stream.Stream;

public class ReasonerManager {

    private final Loader loader;

    public ReasonerManager(Loader loader) {
        this.loader = loader;
        loader.reasonerManager = this;
    }

    public void addAxiomToOntology(OWLAxiom axiom) {
        loader.getOntologyManager().addAxiom(loader.getOntology(), axiom);
        loader.initializeReasoner();
    }

    public void addAxiomToOriginalOntology(OWLAxiom axiom) {
        loader.getOntologyManager().addAxiom(loader.getOriginalOntology(), axiom);
        loader.initializeReasoner();
    }

    public void addNegatedObservationToOntologies(){
        OWLAxiom negObservation = loader.getNegObservationAxiom();
        addAxiomToOriginalOntology(negObservation);
        addAxiomToOntology(negObservation);
    }

    public void addAxiomsToOntology(Collection<OWLAxiom> axioms) {
        loader.getOntologyManager().addAxioms(loader.getOntology(), axioms);
        loader.initializeReasoner();
    }

    public void removeAxiomFromOntology(OWLAxiom axiom) {
        loader.getOntologyManager().removeAxiom(loader.getOntology(), axiom);
        loader.initializeReasoner();
    }

    public void resetOntology(Stream<OWLAxiom> axioms) {
        loader.getOntologyManager().removeAxioms(loader.getOntology(), loader.getOntology().axioms());
        loader.initializeReasoner();
        loader.getOntologyManager().addAxioms(loader.getOntology(), axioms);
        loader.initializeReasoner();
    }

    public void resetOntologyToOriginal(){
        resetOntology(loader.getOriginalOntology().axioms());
    }

    public boolean isOntologyConsistent() {
        loader.initializeReasoner();
        return loader.getReasoner().isConsistent();
    }

    public boolean isOntologyWithLiteralsConsistent(Collection<OWLAxiom> axioms, OWLOntology ontology) {
        addAxiomsToOntology(axioms);
        boolean isConsistent = isOntologyConsistent();
        resetOntology(ontology.axioms());
        return isConsistent;
    }

    public boolean isOriginalOntologyConsistentWithLiterals(Collection<OWLAxiom> axioms){
        return isOntologyWithLiteralsConsistent(axioms, loader.getOriginalOntology());
    }

}
