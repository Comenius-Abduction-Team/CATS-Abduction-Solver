package sk.uniba.fmph.dai.cats.algorithms.lubm.explanationTests;

import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import sk.uniba.fmph.dai.cats.algorithms.data.LubmInput;
import sk.uniba.fmph.dai.cats.algorithms.data.LubmS4Data;
import sk.uniba.fmph.dai.cats.algorithms.lubm.LubmExpanationTest;

import java.io.IOException;
import java.util.List;

public class LubmS4ExplanationTest extends LubmExpanationTest {

    public LubmS4ExplanationTest() throws OWLOntologyCreationException, IOException {
        super("LUBM checking computed explanations: group S4");
    }

    @Override
    protected List<LubmInput> getInputs() {
        return LubmS4Data.LUBM_INPUTS;
    }

    @Override
    protected Integer getDepthLimit() {
        return 5;
    }

}
