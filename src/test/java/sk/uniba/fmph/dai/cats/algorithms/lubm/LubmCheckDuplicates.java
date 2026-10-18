package sk.uniba.fmph.dai.cats.algorithms.lubm;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import sk.uniba.fmph.dai.abduction_api.abducer.IExplanation;
import sk.uniba.fmph.dai.cats.algorithms.Algorithm;
import sk.uniba.fmph.dai.cats.algorithms.Optimisation;
import sk.uniba.fmph.dai.cats.algorithms.data.LubmInput;
import sk.uniba.fmph.dai.cats.api_implementation.CatsAbducer;
import sk.uniba.fmph.dai.cats.common.Configuration;

import java.io.IOException;
import java.util.*;

public abstract class LubmCheckDuplicates extends LubmTestBase {

    private final Map<String, Set<Optimisation>> optimizationCombinations = new HashMap<>();
    private final boolean IGNORE_DEFAULT_OPTIMIZATIONS = true;
    private final boolean NO_NEG = true;

    public LubmCheckDuplicates(String name) throws OWLOntologyCreationException, IOException {
        super(name);
        initializeOptimizationCombinations();
    }

    private void initializeOptimizationCombinations() {
        Set<Optimisation> opt1 = new HashSet<>();
        Set<Optimisation> opt2 = new HashSet<>();
        Set<Optimisation> opt12 = new HashSet<>();

        opt1.add(Optimisation.MOVE_CONSISTENCY_CHECKS);
        opt2.add(Optimisation.SORT_MODEL);
        opt12.add(Optimisation.MOVE_CONSISTENCY_CHECKS);
        opt12.add(Optimisation.SORT_MODEL);

//        optimizationCombinations.put("no-opt", new HashSet<>());
//        optimizationCombinations.put("opt1", opt1);
//        optimizationCombinations.put("opt2", opt2);
        optimizationCombinations.put("opt12", opt12);
    }

    protected void executeTest(LubmInput input,
                               AlgorithmConfiguration algorithmConfiguration) {

        try {

            this.observation = parseAxiomsFromString(input.getObservation());

            this.abducer = new CatsAbducer(backgroundKnowledge, this.observation);
            this.abducer.setLogging(false);
            this.abducer.setIgnoreDefaultOptimizations(
                    algorithmConfiguration.ignoreDefaultOptimisations
            );
            this.abducer.addOptimisations(algorithmConfiguration.optimisations);
            this.abducer.setAlgorithm(algorithmConfiguration.algorithm);

            if (algorithmConfiguration.noNeg) {
                Configuration.INPUT_FILE_NAME += "NoNeg";
                this.abducer.setExplanationConfigurator(noNeg);
            }

            this.abducer.setDepth(input.getRequiredDepthLimit());
            //Configuration.REUSE_OF_MODELS = false;

            solve();
            printUsedOptimizations();

            testDuplicateExplanations(this.abducer.getExplanations());


        } catch (OWLOntologyCreationException e) {
            throw new RuntimeException("Parsing of observation failed: " + input.getId(), e);
        }
    }

    protected Collection<DynamicTest> generateDynamicTests(Algorithm algorithm) {

        List<DynamicTest> dynamicTests = new ArrayList<>();

        for (LubmInput input : getInputs()) {

            for (String optIndex : optimizationCombinations.keySet()) {
                String testName = input.getId() + "_" + optIndex;

                AlgorithmConfiguration algorithmConfiguration = new AlgorithmConfiguration(
                        algorithm,
                        optimizationCombinations.get(optIndex),
                        IGNORE_DEFAULT_OPTIMIZATIONS,
                        NO_NEG
                );

                DynamicTest dTest = DynamicTest.dynamicTest(testName, () -> {
                    executeTest(input, algorithmConfiguration);
                });

                dynamicTests.add(dTest);
            }
        }

        return dynamicTests;
    }

    @TestFactory
    public Collection<DynamicTest> multipleMhsMxpNoNeg() {
        return generateDynamicTests(Algorithm.MHS_MXP);
    }

    @TestFactory
    public Collection<DynamicTest> multipleHsDagMxpNoNeg() {
        return generateDynamicTests(Algorithm.HSDAG_MXP);
    }

    @TestFactory
    public Collection<DynamicTest> multipleRctMxpNoNeg() {
        return generateDynamicTests(Algorithm.RCT_MXP);
    }

//    @TestFactory
//    public Collection<DynamicTest> multipleHstMxpNoNeg() {
//        return generateDynamicTests(Algorithm.HST_MXP);
//    }


}