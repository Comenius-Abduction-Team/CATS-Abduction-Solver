package sk.uniba.fmph.dai.cats.algorithms.lubm.countTests;

import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import sk.uniba.fmph.dai.cats.algorithms.data.LubmInput;
import sk.uniba.fmph.dai.cats.algorithms.data.LubmS2Data;
import sk.uniba.fmph.dai.cats.algorithms.lubm.LubmCountTest;

import java.io.IOException;
import java.util.List;

public class LubmS2CountTest extends LubmCountTest {

    public LubmS2CountTest() throws OWLOntologyCreationException, IOException {
        super("LubmS2OptCountTest");
    }

    @Override
    protected List<LubmInput> getInputs() {
        return LubmS2Data.LUBM_INPUTS;
    }

    @Override
    protected Integer getDepthLimit() {
        return 2;
    }

}
