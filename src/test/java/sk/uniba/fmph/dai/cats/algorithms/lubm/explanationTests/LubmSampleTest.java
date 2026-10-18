package sk.uniba.fmph.dai.cats.algorithms.lubm.explanationTests;

import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import sk.uniba.fmph.dai.cats.algorithms.data.*;
import sk.uniba.fmph.dai.cats.algorithms.lubm.LubmExplanationTest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LubmSampleTest extends LubmExplanationTest {

    public LubmSampleTest() throws OWLOntologyCreationException, IOException {
        super("LUBM sample: checking computed explanations");
    }

    @Override
    protected List<LubmInput> getInputs() {
        List<LubmInput> sampleInputs = new ArrayList<>();

        selectRandomElementsFrom(LubmS1Data.LUBM_INPUTS, sampleInputs, 1);
        selectRandomElementsFrom(LubmS2Data.LUBM_INPUTS, sampleInputs, 2);
        selectRandomElementsFrom(LubmS3Data.LUBM_INPUTS, sampleInputs, 2);
        selectRandomElementsFrom(LubmS4Data.LUBM_INPUTS, sampleInputs, 1);

        return sampleInputs;
    }

    private static void selectRandomElementsFrom(List<LubmInput> lubmInputs,
                                                 List<LubmInput> sampleInputs,
                                                 int numberOfElements) {

        List<LubmInput> sample = new ArrayList<>(lubmInputs);
        Collections.shuffle(sample);
        sampleInputs.addAll(sample.subList(0, numberOfElements));
    }

}
