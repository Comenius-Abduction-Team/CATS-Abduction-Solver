package sk.uniba.fmph.dai.cats.algorithms.lubm.checkDuplicates;

import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import sk.uniba.fmph.dai.cats.algorithms.data.LubmInput;
import sk.uniba.fmph.dai.cats.algorithms.data.LubmS3Data;
import sk.uniba.fmph.dai.cats.algorithms.lubm.LubmCheckDuplicates;
import sk.uniba.fmph.dai.cats.algorithms.lubm.LubmCountTest;

import java.io.IOException;
import java.util.List;

public class LubmS3DuplicatesTest extends LubmCheckDuplicates {

    public LubmS3DuplicatesTest() throws OWLOntologyCreationException, IOException {
        super("LubmS3DuplicatesTest");
    }

    @Override
    protected List<LubmInput> getInputs() {
        return LubmS3Data.LUBM_INPUTS;
    }

    @Override
    protected Integer getDepthLimit() {
        return LubmS3Data.DEPTH_LIMIT;
    }

}
