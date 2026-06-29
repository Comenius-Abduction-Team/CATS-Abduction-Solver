package sk.uniba.fmph.dai.cats.algorithms.lubm.compareExplTest;

import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import sk.uniba.fmph.dai.cats.algorithms.data.LubmInput;
import sk.uniba.fmph.dai.cats.algorithms.data.LubmS3Data;
import sk.uniba.fmph.dai.cats.algorithms.data.LubmS4Data;
import sk.uniba.fmph.dai.cats.algorithms.lubm.LubmExplComparisonTest;

import java.io.IOException;
import java.util.List;

public class LubmS4ExplComparisonTest extends LubmExplComparisonTest {

    public LubmS4ExplComparisonTest() throws OWLOntologyCreationException, IOException {
        super("LubmS4ExplComparisonTest");
    }

    @Override
    protected List<LubmInput> getInputs() {
        return LubmS4Data.LUBM_INPUTS;
    }

    @Override
    protected Integer getDepthLimit() {
        return LubmS4Data.DEPTH_LIMIT;
    }

}
