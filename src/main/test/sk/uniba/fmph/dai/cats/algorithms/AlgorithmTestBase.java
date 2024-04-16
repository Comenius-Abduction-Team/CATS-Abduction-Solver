package sk.uniba.fmph.dai.cats.algorithms;

import org.junit.jupiter.api.Test;
import sk.uniba.fmph.dai.abduction_api.abducer.IExplanation;
import sk.uniba.fmph.dai.cats.api_implementation.CatsAbducer;
import org.junit.jupiter.api.BeforeEach;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.StringDocumentSource;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.DefaultPrefixManager;
import sk.uniba.fmph.dai.cats.api_implementation.CatsExplanationConfigurator;
import sk.uniba.fmph.dai.cats.api_implementation.CatsSymbolAbducibles;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The type Algorithm test base.
 */
public abstract class AlgorithmTestBase {

    /** Path to the file containing the background knowledge ontology. */
    static String ONTOLOGY_FILE;
    /** String containing the observation axioms in some OWL syntax. */
    static String OBSERVATION;
    /** Prefix to use for abducible symbols (usually a prefix from the BK ontology). */
    static String ABDUCIBLE_PREFIX;

    /** The BK ontology extracted from the file. */
    protected OWLOntology backgroundKnowledge;
    /** The observation extracted from the string. */
    protected Set<OWLAxiom> observation;

    /**
     * The Data factory.
     */
    protected OWLDataFactory dataFactory;
    /**
     * The Prefix manager.
     */
    protected PrefixManager prefixManager;

    /**
     * The Manager.
     */
    protected CatsAbducer manager;
    /**
     * The No neg.
     */
    protected CatsExplanationConfigurator noNeg;
    /**
     * The Symbol abd.
     */
    protected CatsSymbolAbducibles symbolAbd;

    /**
     * Instantiates a new Algorithm test base.
     *
     * @throws OWLOntologyCreationException internal OWL API error
     * @throws IOException                  in case of wrong file path
     */
    public AlgorithmTestBase() throws OWLOntologyCreationException, IOException {
        setUpInput();
        setUpHelperObjects();
        setUpAbducibles();
    }

    /** Method that should be overwritten by the test to set its BK, observation and prefix to be used in all test cases. */
    abstract void setUpInput();

    /**
     * Constructs objects necesarry to set up abducibles and run the test cases.
     *
     * @throws OWLOntologyCreationException internal OWL API error
     * @throws IOException                  in case of wrong file path
     */
    void setUpHelperObjects() throws OWLOntologyCreationException, IOException {

        backgroundKnowledge = parseOntologyFromFile(ONTOLOGY_FILE);
        observation = parseAxiomsFromString(OBSERVATION);

        dataFactory = OWLManager.createOWLOntologyManager().getOWLDataFactory();
        prefixManager = new DefaultPrefixManager(ABDUCIBLE_PREFIX);

        noNeg = new CatsExplanationConfigurator();
        noNeg.allowComplementConcepts(false);
        symbolAbd = new CatsSymbolAbducibles();

    }

    /** Method that should be overriden by the specific test to set up abducibles used in the abducible test cases. */
    abstract void setUpAbducibles();

    /**
     * Instantiates an abduction manager before each test case.
     */
    @BeforeEach
    void setUp() {
        manager = new CatsAbducer(backgroundKnowledge,observation);
    }

    /**
     * Parses ontology from an OWL file.
     *
     * @param filepath path to the ontology file
     * @return the OWL ontology
     * @throws IOException                  in case of wrong file
     * @throws OWLOntologyCreationException internal OWL API error
     */
    public OWLOntology parseOntologyFromFile(String filepath) throws IOException, OWLOntologyCreationException {
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
    public Set<OWLAxiom> parseAxiomsFromString(String string) throws OWLOntologyCreationException {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology observationOntology = manager.loadOntologyFromOntologyDocument(new StringDocumentSource(string));
        return observationOntology.getAxioms();
    }

    public void testExplanationsFound(int expectedCount){
        Collection<IExplanation> explanations = manager.getExplanations();
        System.out.println(explanations);
        assertEquals(expectedCount, explanations.size());
    }

    public void solve(){
        manager.solveAbduction();
    }

    void mhs(){
        manager.setAlgorithm(Algorithm.MHS);
    }

    void hst(){
        manager.setAlgorithm(Algorithm.HST);
    }

    void mhsMxp(){
        manager.setAlgorithm(Algorithm.MHS_MXP);
    }

    void hstMxp(){
        manager.setAlgorithm(Algorithm.HST_MXP);
    }

    void mhsNoNeg(){
        manager.setAlgorithm(Algorithm.MHS);
        manager.setExplanationConfigurator(noNeg);
    }

    void hstNoNeg(){
        manager.setAlgorithm(Algorithm.HST);
        manager.setExplanationConfigurator(noNeg);
    }

    void mhsMxpNoNeg(){
        manager.setAlgorithm(Algorithm.MHS_MXP);
        manager.setExplanationConfigurator(noNeg);
    }

    void hstMxpNoNeg(){
        manager.setAlgorithm(Algorithm.HST_MXP);
        manager.setExplanationConfigurator(noNeg);
    }

    void mhsSymbolAbd(){
        manager.setAlgorithm(Algorithm.MHS);
        manager.setAbducibles(symbolAbd);
    }

    void hstSymbolAbd(){
        manager.setAlgorithm(Algorithm.HST);
        manager.setAbducibles(symbolAbd);
    }

    void mhsMxpSymbolAbd(){
        manager.setAlgorithm(Algorithm.MHS_MXP);
        manager.setAbducibles(symbolAbd);
    }

    void hstMxpSymbolAbd(){
        manager.setAlgorithm(Algorithm.HST_MXP);
        manager.setAbducibles(symbolAbd);
    }

    void mhsSymbolAbdNoNeg(){
        manager.setAlgorithm(Algorithm.MHS);
        manager.setAbducibles(symbolAbd);
        manager.setExplanationConfigurator(noNeg);
    }

    void hstSymbolAbdNoNeg(){
        manager.setAlgorithm(Algorithm.HST);
        manager.setAbducibles(symbolAbd);
        manager.setExplanationConfigurator(noNeg);
    }

    void mhsMxpSymbolAbdNoNeg(){
        manager.setAlgorithm(Algorithm.MHS_MXP);
        manager.setAbducibles(symbolAbd);
        manager.setExplanationConfigurator(noNeg);
    }

    void hstMxpSymbolAbdNoNeg(){
        manager.setAlgorithm(Algorithm.HST_MXP);
        manager.setAbducibles(symbolAbd);
        manager.setExplanationConfigurator(noNeg);
    }
}
