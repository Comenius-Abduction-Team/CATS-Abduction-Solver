package sk.uniba.fmph.dai.cats.algorithms;

import openllet.owlapi.OpenlletReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import sk.uniba.fmph.dai.cats.events.EventPublisher;
import sk.uniba.fmph.dai.cats.events.EventType;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collection;

public class OntologyWrapper {

    private final AlgorithmSolver solver;
    private final OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager();
    private OWLOntology ontology;
    private OWLReasoner reasoner;

    OntologyWrapper(AlgorithmSolver solver, Collection<OWLAxiom> axioms) {
        this.solver = solver;
        try {
            ontology = ontologyManager.createOntology(axioms);
        } catch(OWLOntologyCreationException e){
            throw new RuntimeException("Could not create ontology while checking relevancy: " + e.getMessage());
        }
    }

    public void addAxiom(OWLAxiom axiom) {
        ontologyManager.addAxiom(ontology, axiom);
    }

    public void addAxioms(Collection<OWLAxiom> axioms) {
        ontologyManager.addAxioms(ontology, axioms);
    }

    public void removeAxiom(OWLAxiom axiom) {
        ontologyManager.removeAxiom(ontology, axiom);
    }

    public void removeAxioms(Collection<OWLAxiom> axioms) {
        ontologyManager.removeAxioms(ontology, axioms);
    }

    public boolean isConsistent() {
        if (reasoner == null) {
            reasoner = new OpenlletReasonerFactory().createNonBufferingReasoner(ontology);
        }

        Runtime runtime = Runtime.getRuntime();
        long usedMemoryBefore = (runtime.totalMemory() - runtime.freeMemory());


        boolean isConsistent = reasoner.isConsistent();
        EventPublisher.publishGenericEvent(solver, EventType.CONSISTENCY_CHECK);


        long usedMemoryAfter = (runtime.totalMemory() - runtime.freeMemory());

        long memoryIncreased = (usedMemoryAfter-usedMemoryBefore) / (1024*1024);

        Path file = Paths.get("consistencyCalls.txt");

        long oldValue = 0;

        try {
            if (Files.exists(file)) {
                String content = Files.readString(file).trim();

                if (!content.isEmpty()) {
                    oldValue = Long.parseLong(content);
                }
            }

            long newValue = oldValue + Math.toIntExact(memoryIncreased);

            Files.writeString(
                    file,
                    String.valueOf(newValue),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING
            );

        } catch (IOException | NumberFormatException ex) {
            ex.printStackTrace();
        }


        return isConsistent;
    }

}
