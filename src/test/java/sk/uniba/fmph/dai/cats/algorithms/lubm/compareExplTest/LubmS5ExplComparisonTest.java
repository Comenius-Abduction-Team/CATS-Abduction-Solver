package sk.uniba.fmph.dai.cats.algorithms.lubm.compareExplTest;

import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import sk.uniba.fmph.dai.cats.algorithms.data.LubmInput;
import sk.uniba.fmph.dai.cats.algorithms.data.LubmS4Data;
import sk.uniba.fmph.dai.cats.algorithms.data.LubmS5Data;
import sk.uniba.fmph.dai.cats.algorithms.lubm.LubmExplComparisonTest;

import java.io.IOException;
import java.util.List;

public class LubmS5ExplComparisonTest extends LubmExplComparisonTest {

    public LubmS5ExplComparisonTest() throws OWLOntologyCreationException, IOException {
        super("LubmS5ExplComparisonTest");
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
