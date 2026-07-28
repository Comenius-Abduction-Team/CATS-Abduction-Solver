package sk.uniba.fmph.dai.cats.algorithms.lubm.checkDuplicates;

import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import sk.uniba.fmph.dai.cats.algorithms.data.LubmInput;
import sk.uniba.fmph.dai.cats.algorithms.data.LubmS4Data;
import sk.uniba.fmph.dai.cats.algorithms.lubm.LubmCheckDuplicates;

import java.io.IOException;
import java.util.List;

public class LubmS4DuplicatesTest extends LubmCheckDuplicates {

    public LubmS4DuplicatesTest() throws OWLOntologyCreationException, IOException {
        super("LubmS4DuplicatesTest");
    }

    @Override
    protected List<LubmInput> getInputs() {
        return LubmS4Data.LUBM_INPUTS;
    }

}
