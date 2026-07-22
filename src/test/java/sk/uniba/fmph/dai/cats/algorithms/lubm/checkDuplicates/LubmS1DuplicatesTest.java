package sk.uniba.fmph.dai.cats.algorithms.lubm.checkDuplicates;

import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import sk.uniba.fmph.dai.cats.algorithms.data.LubmInput;
import sk.uniba.fmph.dai.cats.algorithms.data.LubmS1Data;
import sk.uniba.fmph.dai.cats.algorithms.lubm.LubmCheckDuplicates;

import java.io.IOException;
import java.util.List;

public class LubmS1DuplicatesTest extends LubmCheckDuplicates {

    public LubmS1DuplicatesTest() throws OWLOntologyCreationException, IOException {
        super("LubmS1DuplicatesTest");
    }

    @Override
    protected List<LubmInput> getInputs() {
        return LubmS1Data.LUBM_INPUTS;
    }

    @Override
    protected Integer getDepthLimit() {
        return 1;
    }

}
