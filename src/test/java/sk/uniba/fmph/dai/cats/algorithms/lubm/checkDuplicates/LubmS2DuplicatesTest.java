package sk.uniba.fmph.dai.cats.algorithms.lubm.checkDuplicates;

import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import sk.uniba.fmph.dai.cats.algorithms.data.LubmInput;
import sk.uniba.fmph.dai.cats.algorithms.data.LubmS2Data;
import sk.uniba.fmph.dai.cats.algorithms.lubm.LubmCheckDuplicates;

import java.io.IOException;
import java.util.List;

public class LubmS2DuplicatesTest extends LubmCheckDuplicates {

    public LubmS2DuplicatesTest() throws OWLOntologyCreationException, IOException {
        super("LubmS2DuplicatesTest");
    }

    @Override
    protected List<LubmInput> getInputs() {
        return LubmS2Data.LUBM_INPUTS;
    }

}
