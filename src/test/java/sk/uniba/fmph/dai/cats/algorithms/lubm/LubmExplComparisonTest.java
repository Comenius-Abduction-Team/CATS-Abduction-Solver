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
import sk.uniba.fmph.dai.cats.data.Explanation;

import java.io.IOException;
import java.util.*;

public abstract class LubmExplComparisonTest extends LubmTestBase {

    private List<Algorithm> algorithms = new ArrayList<>();
    private Map<String, Set<Optimisation>> optimizationCombinations = new HashMap<>();

    private final boolean IGNORE_DEFAULT_OPTIMIZATIONS = false;
    private final boolean NO_NEG = true;

    public LubmExplComparisonTest(String name) throws OWLOntologyCreationException, IOException {
        super(name);
        initializeAlgorithms();
        initializeOptimizationCombinations();
    }

    private void initializeAlgorithms() {
//        algorithms.add(Algorithm.MHS);
//        algorithms.add(Algorithm.RCT);
        algorithms.add(Algorithm.MHS_MXP);
        algorithms.add(Algorithm.RCT_MXP);
    }

    private void initializeOptimizationCombinations() {
        Set<Optimisation> opt1 = new HashSet<>();
        Set<Optimisation> opt2 = new HashSet<>();
        Set<Optimisation> opt12 = new HashSet<>();

        opt1.add(Optimisation.MOVE_CONSISTENCY_CHECKS);
        opt2.add(Optimisation.SORT_MODEL);
        opt12.add(Optimisation.MOVE_CONSISTENCY_CHECKS);
        opt12.add(Optimisation.SORT_MODEL);

        optimizationCombinations.put("no-opt", new HashSet<>());
//        optimizationCombinations.put("opt1", opt1);
//        optimizationCombinations.put("opt2", opt2);
//        optimizationCombinations.put("opt12", opt12);
    }

    protected void executeTest(LubmInput input,
                               AlgorithmConfiguration algorithmConfiguration1,
                               AlgorithmConfiguration algorithmConfiguration2) {

            Set<IExplanation> explanationsALG1 =
                    executeTest(input, algorithmConfiguration1);

            Set<IExplanation> explanationsALG2 =
                    executeTest(input, algorithmConfiguration2);

            compareExplanationsFound(explanationsALG1, explanationsALG2);
    }

    protected Set<IExplanation> executeTest(LubmInput input,
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

            if (getDepthLimit() != null) {
                this.abducer.setDepth(getDepthLimit());
            }

            solve();
            return new HashSet<>(this.abducer.getExplanations());

        } catch (OWLOntologyCreationException e) {
            throw new RuntimeException("Parsing of observation failed: " + input.getId(), e);
        }
    }

    protected Collection<DynamicTest> generateDynamicTestsForAlgorithmVariations() {

        List<DynamicTest> dynamicTests = new ArrayList<>();
        List<Map.Entry<String, Set<Optimisation>>> optsEntries =
                new ArrayList<>(optimizationCombinations.entrySet());

        for (LubmInput input : getInputs()) {
            for (Algorithm algorithm : algorithms) {

                for (int indexOpts1 = 0; indexOpts1 < optsEntries.size(); indexOpts1++) {
                    for (int indexOpts2 = indexOpts1 + 1; indexOpts2 < optsEntries.size(); indexOpts2++) {

                        Map.Entry<String, Set<Optimisation>> opts1 = optsEntries.get(indexOpts1);
                        Map.Entry<String, Set<Optimisation>> opts2 = optsEntries.get(indexOpts2);

                        String testName = algorithm.name() + "_" + input.getId()
                                + "_" + opts1.getKey() + "_vs_" + opts2.getKey();

                        AlgorithmConfiguration algorithmConfiguration1 = new AlgorithmConfiguration(
                                algorithm,
                                opts1.getValue(),
                                IGNORE_DEFAULT_OPTIMIZATIONS,
                                NO_NEG
                        );

                        AlgorithmConfiguration algorithmConfiguration2 = new AlgorithmConfiguration(
                                algorithm,
                                opts2.getValue(),
                                IGNORE_DEFAULT_OPTIMIZATIONS,
                                NO_NEG
                        );

                        DynamicTest dTest = DynamicTest.dynamicTest(testName, () -> {
                            executeTest(input, algorithmConfiguration1, algorithmConfiguration2);
                        });
                        dynamicTests.add(dTest);


                    }
                }

            }
        }

        return dynamicTests;
    }

    protected Collection<DynamicTest> generateDynamicTestsForDifferentAlgorithms() {

        List<DynamicTest> dynamicTests = new ArrayList<>();

        for (LubmInput input : getInputs()) {
            for (int indexAlg1 = 0; indexAlg1 < algorithms.size(); indexAlg1++) {
                for (int indexAlg2 = indexAlg1 + 1; indexAlg2 < algorithms.size(); indexAlg2++) {

                    Algorithm alg1 = algorithms.get(indexAlg1);
                    Algorithm alg2 = algorithms.get(indexAlg2);

                    for (String optIndex : optimizationCombinations.keySet()) {
                        String testName = alg1.name()
                                + "_vs_" + alg2.name()
                                + input.getId() + "_" + optIndex;

                        AlgorithmConfiguration algorithmConfiguration1 = new AlgorithmConfiguration(
                                alg1,
                                optimizationCombinations.get(optIndex),
                                IGNORE_DEFAULT_OPTIMIZATIONS,
                                NO_NEG
                        );

                        AlgorithmConfiguration algorithmConfiguration2 = new AlgorithmConfiguration(
                                alg2,
                                optimizationCombinations.get(optIndex),
                                IGNORE_DEFAULT_OPTIMIZATIONS,
                                NO_NEG
                        );

                        DynamicTest dTest = DynamicTest.dynamicTest(testName, () -> {
                            executeTest(input, algorithmConfiguration1, algorithmConfiguration2);
                        });
                        dynamicTests.add(dTest);
                    }

                }
            }
        }

        return dynamicTests;
    }

//    @TestFactory
//    public Collection<DynamicTest> compareAlgorithmVariations() {
//        return generateDynamicTestsForAlgorithmVariations();
//    }

    @TestFactory
    public Collection<DynamicTest> compareDifferentAlgorithms() {
        return generateDynamicTestsForDifferentAlgorithms();
    }
}