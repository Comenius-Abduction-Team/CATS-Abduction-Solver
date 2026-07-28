package sk.uniba.fmph.dai.cats.algorithms.lubm.checkDuplicates;

import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import sk.uniba.fmph.dai.cats.algorithms.data.LubmInput;
import sk.uniba.fmph.dai.cats.algorithms.data.LubmS5Data;
import sk.uniba.fmph.dai.cats.algorithms.lubm.LubmCheckDuplicates;

import java.io.IOException;
import java.util.List;

public class LubmS5DuplicatesTest extends LubmCheckDuplicates {

    public LubmS5DuplicatesTest() throws OWLOntologyCreationException, IOException {
        super("LubmS5DuplicatesTest");
    }

    @Override
    protected List<LubmInput> getInputs() {
        return LubmS5Data.LUBM_INPUTS;
    }

}
