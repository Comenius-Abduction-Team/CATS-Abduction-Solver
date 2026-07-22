package sk.uniba.fmph.dai.cats.algorithms.lubm.countTests;

import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import sk.uniba.fmph.dai.cats.algorithms.data.LubmInput;
import sk.uniba.fmph.dai.cats.algorithms.data.LubmS1Data;
import sk.uniba.fmph.dai.cats.algorithms.data.LubmS3Data;
import sk.uniba.fmph.dai.cats.algorithms.data.LubmS4Data;
import sk.uniba.fmph.dai.cats.algorithms.lubm.LubmCountTest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LubmAllProblematicInputsTest extends LubmCountTest {

    public LubmAllProblematicInputsTest() throws OWLOntologyCreationException, IOException {
        super("LubmProblematicInputsCountTest");
    }

    @Override
    protected List<LubmInput> getInputs() {
        List<LubmInput> problematicInputs = new ArrayList<>();

        //from S3 (rct-mxp)
        problematicInputs.add(LubmS3Data.LUBM_INPUTS.get(1));
        problematicInputs.add(LubmS3Data.LUBM_INPUTS.get(5));
        problematicInputs.add(LubmS3Data.LUBM_INPUTS.get(7));
        problematicInputs.add(LubmS3Data.LUBM_INPUTS.get(9));

        //from S4 (rct-mxp, hsdag-mxp)
        problematicInputs.add(LubmS4Data.LUBM_INPUTS.get(1));
        problematicInputs.add(LubmS4Data.LUBM_INPUTS.get(2));
        problematicInputs.add(LubmS4Data.LUBM_INPUTS.get(3));
        problematicInputs.add(LubmS4Data.LUBM_INPUTS.get(4));
        problematicInputs.add(LubmS4Data.LUBM_INPUTS.get(6));
        problematicInputs.add(LubmS4Data.LUBM_INPUTS.get(7));
        problematicInputs.add(LubmS4Data.LUBM_INPUTS.get(8));
        problematicInputs.add(LubmS4Data.LUBM_INPUTS.get(9));

        return problematicInputs;
    }

    @Override
    protected Integer getDepthLimit() {
        return 5;
    }

}
