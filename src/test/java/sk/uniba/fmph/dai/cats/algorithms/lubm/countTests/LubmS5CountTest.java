package sk.uniba.fmph.dai.cats.algorithms.lubm.countTests;

import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import sk.uniba.fmph.dai.cats.algorithms.data.LubmInput;
import sk.uniba.fmph.dai.cats.algorithms.data.LubmS5Data;
import sk.uniba.fmph.dai.cats.algorithms.lubm.LubmCountTest;

import java.io.IOException;
import java.util.List;

public class LubmS5CountTest extends LubmCountTest {

    public LubmS5CountTest() throws OWLOntologyCreationException, IOException {
        super("LubmS5OptCountTest");
    }

    @Override
    protected List<LubmInput> getInputs() {
        return LubmS5Data.LUBM_INPUTS;
    }

    @Override
    protected Integer getDepthLimit() {
        return LubmS5Data.DEPTH_LIMIT;
    }

}
