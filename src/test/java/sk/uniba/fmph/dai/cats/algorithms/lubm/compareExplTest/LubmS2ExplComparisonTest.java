package sk.uniba.fmph.dai.cats.algorithms.lubm.compareExplTest;

import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import sk.uniba.fmph.dai.cats.algorithms.data.LubmInput;
import sk.uniba.fmph.dai.cats.algorithms.data.LubmS1Data;
import sk.uniba.fmph.dai.cats.algorithms.data.LubmS2Data;
import sk.uniba.fmph.dai.cats.algorithms.lubm.LubmExplComparisonTest;

import java.io.IOException;
import java.util.List;

public class LubmS2ExplComparisonTest extends LubmExplComparisonTest {

    public LubmS2ExplComparisonTest() throws OWLOntologyCreationException, IOException {
        super("LubmS2ExplComparisonTest");
    }

    @Override
    protected List<LubmInput> getInputs() {
        return LubmS2Data.LUBM_INPUTS;
    }

    @Override
    protected Integer getDepthLimit() {
        return LubmS2Data.DEPTH_LIMIT;
    }

}
