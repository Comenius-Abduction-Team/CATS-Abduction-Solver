package sk.uniba.fmph.dai.cats.algorithms.lubm.explanationTests;

import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import sk.uniba.fmph.dai.cats.algorithms.data.LubmInput;
import sk.uniba.fmph.dai.cats.algorithms.data.LubmS2Data;
import sk.uniba.fmph.dai.cats.algorithms.lubm.LubmExplanationTest;

import java.io.IOException;
import java.util.List;

public class LubmS2ExplanationTest extends LubmExplanationTest {

    public LubmS2ExplanationTest() throws OWLOntologyCreationException, IOException {
        super("LUBM checking computed explanations: group S2");
    }

    @Override
    protected List<LubmInput> getInputs() {
        return LubmS2Data.LUBM_INPUTS;
    }

}
