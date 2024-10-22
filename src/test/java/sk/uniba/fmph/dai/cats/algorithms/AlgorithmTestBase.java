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
    protected CatsAbducer abducer;
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
        Configuration.DEBUG_PRINT = false;
        abducer = new CatsAbducer(backgroundKnowledge,observation);
        abducer.setLogging(false);
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
        Collection<IExplanation> explanations = abducer.getExplanations();
        //System.out.println(explanations);
        assertEquals(expectedCount, explanations.size());
    }

    public void solve(){
        abducer.solveAbduction();
        if (abducer.getOutputMessage() != null && !abducer.getOutputMessage().isEmpty())
            System.err.println(abducer.getOutputMessage());
    }

    // ------- MHS -------
    
    void mhs(){
        abducer.setAlgorithm(Algorithm.MHS);
    }

    void mhsNoNeg(){
        abducer.setAlgorithm(Algorithm.MHS);
        abducer.setExplanationConfigurator(noNeg);
    }

    void mhsSymbolAbd(){
        abducer.setAlgorithm(Algorithm.MHS);
        abducer.setAbducibles(symbolAbd);
    }

    void mhsSymbolAbdNoNeg(){
        abducer.setAlgorithm(Algorithm.MHS);
        abducer.setAbducibles(symbolAbd);
        abducer.setExplanationConfigurator(noNeg);
    }

    // ------- MXP -------

    void mxp(){ abducer.setAlgorithm(Algorithm.MXP); }

    void mxpNoNeg(){
        abducer.setAlgorithm(Algorithm.MXP);
        abducer.setExplanationConfigurator(noNeg);
    }

    void mxpSymbolAbd(){
        abducer.setAlgorithm(Algorithm.MXP);
        abducer.setAbducibles(symbolAbd);
    }

    void mxpSymbolAbdNoNeg(){
        abducer.setAlgorithm(Algorithm.MXP);
        abducer.setAbducibles(symbolAbd);
        abducer.setExplanationConfigurator(noNeg);
    }

    // ------- MHS-MXP -------

    void mhsMxp(){
        abducer.setAlgorithm(Algorithm.MHS_MXP);
    }

    void mhsMxpNoNeg(){
        abducer.setAlgorithm(Algorithm.MHS_MXP);
        abducer.setExplanationConfigurator(noNeg);
    }

    void mhsMxpSymbolAbd(){
        abducer.setAlgorithm(Algorithm.MHS_MXP);
        abducer.setAbducibles(symbolAbd);
    }

    void mhsMxpSymbolAbdNoNeg(){
        abducer.setAlgorithm(Algorithm.MHS_MXP);
        abducer.setAbducibles(symbolAbd);
        abducer.setExplanationConfigurator(noNeg);
    }

    // ------- HST -------

    void hst(){
        abducer.setAlgorithm(Algorithm.HST);
    }

    void hstNoNeg(){
        abducer.setAlgorithm(Algorithm.HST);
        abducer.setExplanationConfigurator(noNeg);
    }

    void hstSymbolAbd(){
        abducer.setAlgorithm(Algorithm.HST);
        abducer.setAbducibles(symbolAbd);
    }

    void hstSymbolAbdNoNeg(){
        abducer.setAlgorithm(Algorithm.HST);
        abducer.setAbducibles(symbolAbd);
        abducer.setExplanationConfigurator(noNeg);
    }

    // ------- HST-MXP -------

    void hstMxp(){
        abducer.setAlgorithm(Algorithm.HST_MXP);
    }

    void hstMxpNoNeg(){
        abducer.setAlgorithm(Algorithm.HST_MXP);
        abducer.setExplanationConfigurator(noNeg);
    }

    void hstMxpSymbolAbd(){
        abducer.setAlgorithm(Algorithm.HST_MXP);
        abducer.setAbducibles(symbolAbd);
    }

    void hstMxpSymbolAbdNoNeg(){
        abducer.setAlgorithm(Algorithm.HST_MXP);
        abducer.setAbducibles(symbolAbd);
        abducer.setExplanationConfigurator(noNeg);
    }

    // ------- RCT -------

    void rct(){
        abducer.setAlgorithm(Algorithm.RCT);
    }

    void rctNoNeg(){
        abducer.setAlgorithm(Algorithm.RCT);
        abducer.setExplanationConfigurator(noNeg);
    }

    void rctSymbolAbd(){
        abducer.setAlgorithm(Algorithm.RCT);
        abducer.setAbducibles(symbolAbd);
    }

    void rctSymbolAbdNoNeg(){
        abducer.setAlgorithm(Algorithm.RCT);
        abducer.setAbducibles(symbolAbd);
        abducer.setExplanationConfigurator(noNeg);
    }

    // ------- RCT-MXP -------

    void rctMxp(){
        abducer.setAlgorithm(Algorithm.RCT_MXP);
    }

    void rctMxpNoNeg(){
        abducer.setAlgorithm(Algorithm.RCT_MXP);
        abducer.setExplanationConfigurator(noNeg);
    }

    void rctMxpSymbolAbd(){
        abducer.setAlgorithm(Algorithm.RCT_MXP);
        abducer.setAbducibles(symbolAbd);
    }

    void rctMxpSymbolAbdNoNeg(){
        abducer.setAlgorithm(Algorithm.RCT_MXP);
        abducer.setAbducibles(symbolAbd);
        abducer.setExplanationConfigurator(noNeg);
    }

    

    

    

    



    

    

    

    

    

    

    

    

    

    

    

    

    

    
}
