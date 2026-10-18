package sk.uniba.fmph.dai.cats.reasoner;

import org.semanticweb.owlapi.model.OWLAxiom;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.stream.Stream;

public class ReasonerManager {

    private final Loader loader;

    ReasonerManager(Loader loader) {
        this.loader = loader;
    }

    public void addAxiomToOntology(OWLAxiom axiom) {
        loader.getOntologyManager().addAxiom(loader.getOntology(), axiom);
        loader.flushReasoner();
    }

    public void addAxiomToOriginalOntology(OWLAxiom axiom) {
        loader.getOntologyManager().addAxiom(loader.getOriginalOntology(), axiom);
        loader.flushReasoner();
    }

    public void addNegatedObservationToOntologies(){
        OWLAxiom negObservation = loader.getNegObservationAxiom();
        addAxiomToOriginalOntology(negObservation);
        addAxiomToOntology(negObservation);
    }

    public void addAxiomsToOntology(Collection<OWLAxiom> axioms) {
        loader.getOntologyManager().addAxioms(loader.getOntology(), axioms);
        loader.flushReasoner();
    }

    public void removeAxiomFromOntology(OWLAxiom axiom) {
        loader.getOntologyManager().removeAxiom(loader.getOntology(), axiom);
        loader.flushReasoner();
    }

    public void resetOntology(Stream<OWLAxiom> axioms) {
        loader.getOntologyManager().removeAxioms(loader.getOntology(), loader.getOntology().axioms());
        loader.flushReasoner();
        loader.getOntologyManager().addAxioms(loader.getOntology(), axioms);
        loader.flushReasoner();
    }

    public void resetOntologyToOriginal(){
        resetOntology(loader.getOriginalOntology().axioms());
    }

    public void resetOntologyToInitial(){
        resetOntology(loader.getInitialOntology().axioms());
    }

    public boolean isOntologyConsistent() {

        Runtime runtime = Runtime.getRuntime();
        long usedMemoryBefore = (runtime.totalMemory() - runtime.freeMemory());

        loader.flushReasoner();
        boolean isConsistent = loader.getReasoner().isConsistent();

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
