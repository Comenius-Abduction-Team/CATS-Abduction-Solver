package sk.uniba.fmph.dai.cats.algorithms.lubm;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import sk.uniba.fmph.dai.cats.algorithms.Algorithm;
import sk.uniba.fmph.dai.cats.algorithms.AlgorithmTestBase;
import sk.uniba.fmph.dai.cats.algorithms.data.LubmInput;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

public abstract class LubmTestBase extends AlgorithmTestBase {

    public LubmTestBase(String name) throws OWLOntologyCreationException, IOException {
        super(name);
    }

    protected abstract List<LubmInput> getInputs();

    protected abstract Integer getDepthLimit();

    @Override
    protected void setUpInput() {
        ONTOLOGY_FILE = "ont/lubm-0.owl";
        ABDUCIBLE_PREFIX =
                "http://swat.cse.lehigh.edu/onto/univ-bench.owl#";

        OBSERVATION = getInputs().get(0).getObservation();
    }

    @Override
    protected void setUpAbducibles() {}

}