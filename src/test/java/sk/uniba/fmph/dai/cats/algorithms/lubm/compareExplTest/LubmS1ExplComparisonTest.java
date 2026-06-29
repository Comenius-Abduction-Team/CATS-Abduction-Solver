package sk.uniba.fmph.dai.cats.algorithms.lubm.compareExplTest;

import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import sk.uniba.fmph.dai.cats.algorithms.data.LubmInput;
import sk.uniba.fmph.dai.cats.algorithms.data.LubmS1Data;
import sk.uniba.fmph.dai.cats.algorithms.lubm.LubmCountTest;
import sk.uniba.fmph.dai.cats.algorithms.lubm.LubmExplComparisonTest;

import java.io.IOException;
import java.util.List;

public class LubmS1ExplComparisonTest extends LubmExplComparisonTest {

    public LubmS1ExplComparisonTest() throws OWLOntologyCreationException, IOException {
        super("LubmS1ExplComparisonTest");
    }

    @Override
    protected List<LubmInput> getInputs() {
        return LubmS1Data.LUBM_INPUTS;
    }

    @Override
    protected Integer getDepthLimit() {
        return LubmS1Data.DEPTH_LIMIT;
    }

}
