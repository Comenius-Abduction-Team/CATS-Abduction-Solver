package sk.uniba.fmph.dai.cats.algorithms.lubm.checkDuplicates;

import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import sk.uniba.fmph.dai.cats.algorithms.data.LubmInput;
import sk.uniba.fmph.dai.cats.algorithms.data.LubmS3Data;
import sk.uniba.fmph.dai.cats.algorithms.lubm.LubmCheckDuplicates;

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

}
