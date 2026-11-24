package sk.uniba.fmph.dai.cats.algorithms.lubm.explanationTests;

import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import sk.uniba.fmph.dai.cats.algorithms.data.LubmInput;
import sk.uniba.fmph.dai.cats.algorithms.data.LubmS5Data;
import sk.uniba.fmph.dai.cats.algorithms.lubm.LubmExplanationTest;

import java.io.IOException;
import java.util.List;

public class LubmS5ExplanationTest extends LubmExplanationTest {

    public LubmS5ExplanationTest() throws OWLOntologyCreationException, IOException {
        super("LUBM_S5");
    }

    @Override
    protected List<LubmInput> getInputs() {
        return LubmS5Data.LUBM_INPUTS;
    }

}
