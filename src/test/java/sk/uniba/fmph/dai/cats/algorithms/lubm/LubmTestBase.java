package sk.uniba.fmph.dai.cats.algorithms.lubm;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import sk.uniba.fmph.dai.cats.algorithms.Algorithm;
import sk.uniba.fmph.dai.cats.algorithms.AlgorithmTestBase;
import sk.uniba.fmph.dai.cats.algorithms.data.LubmInput;
import sk.uniba.fmph.dai.cats.common.Configuration;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public abstract class LubmTestBase extends AlgorithmTestBase {

    public LubmTestBase(String name) throws OWLOntologyCreationException, IOException {
        super(name);
    }

    protected abstract List<LubmInput> getInputs();

    protected abstract Integer getDepthLimit();

    @Override
    protected void setUpInput() {
        ONTOLOGY_FILE = "ont/lubm.owl";
        ABDUCIBLE_PREFIX =
                "http://swat.cse.lehigh.edu/onto/univ-bench.owl#";

        OBSERVATION = getInputs().get(0).getObservation();
    }

    @Override
    protected void setUpAbducibles() {}

    protected static void printUsedOptimizations() {
        System.out.println(
                "OPTIMIZATIONS: " +
                        Configuration.optimisations.stream()
                                .map(opt -> opt.name().toLowerCase())
                                .collect(Collectors.joining(", "))
        );
    }

}