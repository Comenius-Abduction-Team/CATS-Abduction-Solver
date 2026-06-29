package sk.uniba.fmph.dai.cats.algorithms;

import sk.uniba.fmph.dai.abduction_api.abducer.IExplanation;
import sk.uniba.fmph.dai.cats.api_implementation.CatsAbducer;
import org.junit.jupiter.api.BeforeEach;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.StringDocumentSource;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.DefaultPrefixManager;
import sk.uniba.fmph.dai.cats.api_implementation.CatsExplanationConfigurator;
import sk.uniba.fmph.dai.cats.api_implementation.CatsSymbolAbducibles;
import sk.uniba.fmph.dai.cats.common.Configuration;
import sk.uniba.fmph.dai.cats.common.StringFactory;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Base class for tests that run the algorithms and check the explanations found.
 *
 * Each test class extending this one represents tests over a single ontology/abduction problem.
 *
 * Each test case in an extending class represents a run of the solver over the same abduction problem (the same
 * as all the other test cases in that class), but with different settings (algorithm, abducibles,...).
 *
 */
public abstract class AlgorithmTestBase {

    /** Whether each test should print the explanations found into the console. */
    private final boolean PRINT_EXPLANATIONS = true;
    /** Whether the tests should create logs. */
    private final boolean CREATE_LOGS = false;
    /** Whether the tests should print the debugging prints. */
    private final boolean DEBUG_PRINTING = false;


    /** Path to the file containing the background knowledge ontology. */
    protected String ONTOLOGY_FILE;
    /** String containing the observation axioms in some OWL syntax. */
    protected String OBSERVATION;
    /** Prefix to use for abducible symbols (usually a prefix from the BK ontology). */
    protected String ABDUCIBLE_PREFIX;

    /** The BK ontology extracted from the file. */
    protected OWLOntology backgroundKnowledge;
    /** The observation extracted from the string. */
    protected Set<OWLAxiom> observation;

    protected OWLDataFactory dataFactory;
    protected PrefixManager prefixManager;

    protected CatsAbducer abducer;
    /** Configurator to be used for runs with disabled negations. */
    protected CatsExplanationConfigurator noNeg;
    /** Configurator to be used for runs with symbol abducibles. */
    protected CatsSymbolAbducibles symbolAbd;

    /**
     * Instantiates a new Algorithm test base.
     *
     * @throws OWLOntologyCreationException internal OWL API error
     * @throws IOException                  in case of wrong file path
     */
    protected AlgorithmTestBase(String name) throws OWLOntologyCreationException, IOException {
        setOntologyName(name);
    }

    private void setOntologyName(String name){
        Configuration.INPUT_ONT_FILE = name;
        Configuration.INPUT_FILE_NAME = "";
    }

    /**
     * Method that should be overwritten by the specific test classes to set the BK, the observation and the prefix
     * to be used in all test cases in that class.
     * */
    protected abstract void setUpInput();

    /**
     * Constructs objects necesarry to set up abducibles and run the test cases.
     *
     * @throws OWLOntologyCreationException internal OWL API error
     * @throws IOException                  in case of wrong file path
     */
    private void setUpHelperObjects() throws OWLOntologyCreationException, IOException {

        backgroundKnowledge = parseOntologyFromFile(ONTOLOGY_FILE);
        observation = parseAxiomsFromString(OBSERVATION);

        dataFactory = OWLManager.createOWLOntologyManager().getOWLDataFactory();
        prefixManager = new DefaultPrefixManager(ABDUCIBLE_PREFIX);

        noNeg = new CatsExplanationConfigurator();
        noNeg.allowComplementConcepts(false);
        symbolAbd = new CatsSymbolAbducibles();

    }

    /**
     * Method that should be overriden by the specific test classes to set up the abducibles
     * to be used in all test cases in that class.
     * */
    protected abstract void setUpAbducibles();

    /** Instantiates the abducer before each test case. */
    @BeforeEach
    void setUp() {
        Configuration.DEBUG_PRINT = DEBUG_PRINTING;
        abducer = new CatsAbducer(backgroundKnowledge,observation);
        abducer.setLogging(CREATE_LOGS);
    }
    @BeforeEach
    void init() throws OWLOntologyCreationException, IOException {
        setUpInput();
        setUpHelperObjects();
        setUpAbducibles();
    }

    /**
     * Parses ontology from an OWL file.
     *
     * @param filepath path to the ontology file
     * @return the OWL ontology
     * @throws IOException                  in case of wrong file
     * @throws OWLOntologyCreationException internal OWL API error
     */
    private OWLOntology parseOntologyFromFile(String filepath) throws IOException, OWLOntologyCreationException {
        File file = new File(filepath);
        if (!file.exists())
            throw new IOException("File '" + filepath + "' wasn't found!");
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        return manager.loadOntologyFromOntologyDocument(file);
    }

    /**
     * Parses a set of axioms from a string in some OWL syntax.
     *
     * @param string the string
     * @return set of OWL axioms
     * @throws OWLOntologyCreationException internal OWL API error
     */
    protected Set<OWLAxiom> parseAxiomsFromString(String string) throws OWLOntologyCreationException {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology observationOntology = manager.loadOntologyFromOntologyDocument(new StringDocumentSource(string));
        return observationOntology.getAxioms();
    }

    /**
     * Asserts that the last run of the solver has found the given number of explanations.
     * This should be used in tests to verify the expected results.
     *
     * @param expectedCount the expected number of explanations found
     */
    protected void testExplanationsFound(int expectedCount){
        Collection<IExplanation> explanations = abducer.getExplanations();
        if (PRINT_EXPLANATIONS)
            System.out.println(explanations);
        assertEquals(expectedCount, explanations.size());
    }

    /**
     * Asserts that the two sets of explanations are equal.
     *
     * This test should be used to compare the results of different algorithms,
     * two versions of the same algorithm, or a result with the ground truth
     * when we expect them to return the same set of explanations.
     *
     * @param explanations1 set of explanations
     * @param explanations2 set of explanations
     */
    protected void compareExplanationsFound(Set<IExplanation> explanations1, Set<IExplanation> explanations2){

        Map<String, IExplanation> map1 = explanations1.stream()
                .collect(Collectors.toMap(
                        this::normalize,
                        Function.identity(),
                        (a, b) -> a
                ));

        Map<String, IExplanation> map2 = explanations2.stream()
                .collect(Collectors.toMap(
                        this::normalize,
                        Function.identity(),
                        (a, b) -> a
                ));

        Set<String> onlyIn1 = difference(map1.keySet(), map2.keySet());
        Set<String> onlyIn2 = difference(map2.keySet(), map1.keySet());

        if (onlyIn1.size() != onlyIn2.size()) {
            System.out.println("ALG1: " + onlyIn1.size());
            System.out.println("ALG2: " + onlyIn2.size());

            System.out.println("Only in ALG1: " + onlyIn1.size());
            System.out.println("Only in ALG2: " + onlyIn2.size());
        }

        String onlyIn1String = onlyIn1.stream()
                .map(map1::get)
                .map(e -> StringFactory.getRepresentation(e.getAxiomSet()))
                .collect(Collectors.joining("\n"));

        String onlyIn2String = onlyIn2.stream()
                .map(map2::get)
                .map(e -> StringFactory.getRepresentation(e.getAxiomSet()))
                .collect(Collectors.joining("\n"));


        assertTrue(
                onlyIn1.isEmpty() && onlyIn2.isEmpty(),
                "\nOnly in ALG1:\n" + onlyIn1String +
                        "\n\nOnly in ALG2:\n" + onlyIn2String
        );

    }

    private static <T> Set<T> difference(Set<T> first, Set<T> second) {
        Set<T> result = new HashSet<>(first);
        result.removeAll(second);
        return result;
    }

    /**
     * Normalizes the explanation (into a unique string)
     *
     * @param explanation an explanation
     */
    private String normalize(IExplanation explanation) {

        return explanation.getAxiomSet().stream()
                .map(Object::toString)
                .sorted()
                .collect(Collectors.joining("|"));
    }

    /**
     * Asserts that the set of explanations does not contain duplicates.
     *
     * @param explanations set of explanations
     */
    protected void testDuplicateExplanations(Set<IExplanation> explanations) {
        System.out.println("Returned explanations: " + explanations.size());

        Set<String> normalizedSet = explanations.stream()
                .map(this::normalize)
                .collect(Collectors.toSet());

        System.out.println("Unique normalized explanations: "
                + normalizedSet.size());

        Set<String> unique = new HashSet<>();

        for (IExplanation explanation : explanations) {
            String normalized = normalize(explanation);

            assertTrue(
                    unique.add(normalized),
                    "Duplicate explanation found: " + normalized
            );
        }

        assertEquals(normalizedSet.size(), explanations.size());
    }

    /** Runs the solver. * */
    protected void solve(){
        abducer.solveAbduction();
        if (abducer.getOutputMessage() != null && !abducer.getOutputMessage().isEmpty()) {
            System.err.println(abducer.getOutputMessage());
        }
    }

    // The following private methods are meant as shorthands to set the solver's configuration options
    // in the test cases.

    private void setQxp(){
        abducer.setAlgorithm(Algorithm.QXP);
    }
    private void setMxp(){
        abducer.setAlgorithm(Algorithm.MXP);
    }
    
    private void setMhs(){
        abducer.setAlgorithm(Algorithm.MHS);
    }

    private void setMhsMxp(){
        abducer.setAlgorithm(Algorithm.MHS_MXP);
    }

    private void setHst(){
        abducer.setAlgorithm(Algorithm.HST);
    }

    private void setHstMxp(){
        abducer.setAlgorithm(Algorithm.HST_MXP);
    }

    private void setRct(){
        abducer.setAlgorithm(Algorithm.RCT);
    }

    private void setRctMxp(){
        abducer.setAlgorithm(Algorithm.RCT_MXP);
    }

    private void setHsdag(){
        abducer.setAlgorithm(Algorithm.HSDAG);
    }

    private void setHsdagMxp(){abducer.setAlgorithm(Algorithm.HSDAG_MXP);}

    private void setNoNeg(){
        Configuration.INPUT_FILE_NAME += "NoNeg"; // set up artifically to change the file path of created logs
        abducer.setExplanationConfigurator(noNeg);
    }

    private void setSymbolAbd(){
        Configuration.INPUT_FILE_NAME += "SymbolAbd";
        abducer.setAbducibles(symbolAbd);
    }
    
    private void setSymbolAbdNoNeg(){
        setSymbolAbd();
        setNoNeg();
    }

    // The following set of methods each represent a type of test case to be tested:
    // more specifically, each test case is some combination of an algorithm and various configuration options
    // (such as negations and abducibles being used or not).
    // The test cases should cover every possible combination of configurations (which is currently not true).
    //
    // !!! Every test class extending AlgorithmTestBase should override each of these methods. !!!
    // In the overriden method, call super() to set up the configuration as defined here,
    // then run the solver and test the results. (See existing AlgorithmTestBase extensions as examples)
    //
    //

    // ------- QXP -------

    void qxp(){ setQxp(); }

    void qxpNoNeg(){
        setQxp();
        setNoNeg();
    }

    void qxpSymbolAbd(){
        setQxp();
        setSymbolAbd();
    }

    void qxpSymbolAbdNoNeg(){
        setQxp();
        setSymbolAbdNoNeg();
    }

    // ------- MHS -------
    
    void mhs(){
        setMhs();
    }

    void mhsNoNeg(){
        setMhs();
        setNoNeg();
    }

    void mhsSymbolAbd(){
        setMhs();
        setSymbolAbd();
    }

    void mhsSymbolAbdNoNeg(){
        setMhs();
        setSymbolAbdNoNeg();
    }

    // ------- MXP -------

    void mxp(){ setMxp(); }

    void mxpNoNeg(){
        setMxp();
        setNoNeg();
    }

    void mxpSymbolAbd(){
        setMxp();
        setSymbolAbd();
    }

    void mxpSymbolAbdNoNeg(){
        setMxp();
        setSymbolAbdNoNeg();
    }

    // ------- MHS-MXP -------

    void mhsMxp(){
        setMhsMxp();
    }

    void mhsMxpNoNeg(){
        setMhsMxp();
        setNoNeg();
    }

    void mhsMxpSymbolAbd(){
        setMhsMxp();
        setSymbolAbd();
    }

    void mhsMxpSymbolAbdNoNeg(){
        setMhsMxp();
        setSymbolAbdNoNeg();
    }

    // ------- HST -------

    void hst(){
        setHst();
    }

    void hstNoNeg(){
        setHst();
        setNoNeg();
    }

    void hstSymbolAbd(){
        setHst();
        setSymbolAbd();
    }

    void hstSymbolAbdNoNeg(){
        setHst();
        setSymbolAbdNoNeg();
    }

    // ------- HST-MXP -------

    void hstMxp(){
        setHstMxp();
    }

    void hstMxpNoNeg(){
        setHstMxp();
        setNoNeg();
    }

    void hstMxpSymbolAbd(){
        setHstMxp();
        setSymbolAbd();
    }

    void hstMxpSymbolAbdNoNeg(){
        setHstMxp();
        setSymbolAbdNoNeg();
    }

    // ------- RCT -------

    void rct(){
        setRct();
    }

    void rctNoNeg(){
        setRct();
        setNoNeg();
    }

    void rctSymbolAbd(){
        setRct();
        setSymbolAbd();
    }

    void rctSymbolAbdNoNeg(){
        setRct();
        setSymbolAbdNoNeg();
    }

    // ------- RCT-MXP -------

    void rctMxp(){
        setRctMxp();
    }

    void rctMxpNoNeg(){
        setRctMxp();
        setNoNeg();
    }

    void rctMxpSymbolAbd(){
        setRctMxp();
        setSymbolAbd();
    }

    void rctMxpSymbolAbdNoNeg(){
        setRctMxp();
        setSymbolAbdNoNeg();
    }

    // ------- HS-DAG -------

    void hsdag(){
        setHsdag();
    }

    void hsdagNoNeg(){
        setHsdag();
        setNoNeg();
    }

    void hsdagSymbolAbd(){
        setHsdag();
        setSymbolAbd();
    }

    void hsdagSymbolAbdNoNeg(){
        setHsdag();
        setSymbolAbdNoNeg();
    }

    // ------- HS-DAG-MXP -------

    void hsdagMxp(){
        setHsdagMxp();
    }

    void hsdagMxpNoNeg(){
        setHsdagMxp();
        setNoNeg();
    }

    void hsdagMxpSymbolAbd(){
        setHsdagMxp();
        setSymbolAbd();
    }

    void hsdagMxpSymbolAbdNoNeg(){
        setHsdagMxp();
        setSymbolAbdNoNeg();
    }

}
