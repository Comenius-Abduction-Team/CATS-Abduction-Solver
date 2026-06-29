package sk.uniba.fmph.dai.cats.algorithms.lubm;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import sk.uniba.fmph.dai.cats.algorithms.Algorithm;
import sk.uniba.fmph.dai.cats.algorithms.Optimisation;
import sk.uniba.fmph.dai.cats.algorithms.data.LubmInput;
import sk.uniba.fmph.dai.cats.api_implementation.CatsAbducer;
import sk.uniba.fmph.dai.cats.common.Configuration;

import java.io.IOException;
import java.util.*;

public abstract class LubmCountTest extends LubmTestBase {
    private final Map<String, Set<Optimisation>> optimizationCombinations = new HashMap<>();

    private final boolean IGNORE_DEFAULT_OPTIMIZATIONS = false;

    public LubmCountTest(String name) throws OWLOntologyCreationException, IOException {
        super(name);
        initializeOptimizationCombinations();
    }

    private void initializeOptimizationCombinations() {
        Set<Optimisation> opt1 = new HashSet<>();
        Set<Optimisation> opt2 = new HashSet<>();
        Set<Optimisation> opt12 = new HashSet<>();
        Set<Optimisation> opt13 = new HashSet<>();

        opt1.add(Optimisation.MOVE_CONSISTENCY_CHECKS);
        opt2.add(Optimisation.SORT_MODEL);
        opt12.add(Optimisation.MOVE_CONSISTENCY_CHECKS);
        opt12.add(Optimisation.SORT_MODEL);

        opt13.add(Optimisation.MOVE_CONSISTENCY_CHECKS);
        opt13.add(Optimisation.TRIPLE_MXP);

//        optimizationCombinations.put("no-opt", new HashSet<>());
//        optimizationCombinations.put("opt1", opt1);
//        optimizationCombinations.put("opt2", opt2);
//        optimizationCombinations.put("opt12", opt12);
//        optimizationCombinations.put("opt12", opt12);

        optimizationCombinations.put("opt1", opt1);
        optimizationCombinations.put("opt13", opt13);
    }

    protected void executeTest(LubmInput input,
                               Algorithm algorithm,
                               boolean useNoNeg,
                               Integer customDepth,
                               Set<Optimisation> optimisations) {

        try {

            this.observation = parseAxiomsFromString(input.getObservation());

            this.abducer = new CatsAbducer(backgroundKnowledge, this.observation);
            this.abducer.setLogging(false);
            this.abducer.setIgnoreDefaultOptimizations(IGNORE_DEFAULT_OPTIMIZATIONS);
            this.abducer.addOptimisations(optimisations);
            this.abducer.setAlgorithm(algorithm);

            if (useNoNeg) {
                Configuration.INPUT_FILE_NAME += "NoNeg";
                this.abducer.setExplanationConfigurator(noNeg);
            }

            if (customDepth != null) {
                this.abducer.setDepth(customDepth);
            }

            solve();
            testExplanationsFound(input.getExpectedExplanationCount());

        } catch (OWLOntologyCreationException e) {
            throw new RuntimeException("Parsing of observation failed: " + input.getId(), e);
        }
    }

    protected Collection<DynamicTest> generateDynamicTests(
            Algorithm algorithm,
            boolean useNoNeg,
            Integer customDepth) {

        List<DynamicTest> dynamicTests = new ArrayList<>();

        for (LubmInput input : getInputs()) {

            for (String optIndex : optimizationCombinations.keySet()) {
                String testName = input.getId() + "_" + optIndex;

                DynamicTest dTest = DynamicTest.dynamicTest(testName, () -> {
                    executeTest(input, algorithm, useNoNeg, customDepth,
                            optimizationCombinations.get(optIndex));
                });
                dynamicTests.add(dTest);
            }
        }

        return dynamicTests;
    }

    @TestFactory
    public Collection<DynamicTest> multipleMhsMxpNoNeg() {
        return generateDynamicTests(
                Algorithm.MHS_MXP,
                true,
                getDepthLimit());
    }

//    @TestFactory
//    public Collection<DynamicTest> multipleRctMxpNoNeg() {
//        return generateDynamicTests(
//                Algorithm.RCT_MXP,
//                true,
//                getDepthLimit());
//    }

    //    @TestFactory
//    public Collection<DynamicTest> multipleRctMxpNoNeg() {
//        return generateDynamicTests(
//                Algorithm.RCT_MXP,
//                true,
//                getDepthLimit());
//    }

}