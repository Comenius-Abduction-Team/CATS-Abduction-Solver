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

    private final boolean CREATE_LOGS = false;
    private final boolean DEBUG_PRINTING = false;
    private final boolean PRINT_EXPLANATIONS = true;

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
    public AlgorithmTestBase(String name) throws OWLOntologyCreationException, IOException {
        setOntologyName(name);
        setUpInput();
        setUpHelperObjects();
        setUpAbducibles();
    }

    private void setOntologyName(String name){
        Configuration.INPUT_ONT_FILE = name;
        Configuration.INPUT_FILE_NAME = "";
    }

    /** Method that should be overwritten by the test to set its BK, observation and prefix to be used in all test cases. */
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

    /** Method that should be overriden by the specific test to set up abducibles used in the abducible test cases. */
    protected abstract void setUpAbducibles();

    /**
     * Instantiates an abduction manager before each test case.
     */
    @BeforeEach
    void setUp() {
        Configuration.DEBUG_PRINT = DEBUG_PRINTING;
        abducer = new CatsAbducer(backgroundKnowledge,observation);
        abducer.setLogging(CREATE_LOGS);
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
        if (PRINT_EXPLANATIONS)
            System.out.println(explanations);
        assertEquals(expectedCount, explanations.size());
    }

    public void solve(){
        abducer.solveAbduction();
        if (abducer.getOutputMessage() != null && !abducer.getOutputMessage().isEmpty()) {
            System.err.println(abducer.getOutputMessage());
            System.err.println(abducer.getFullLog());
        }
    }

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
    
    private void setNoNeg(){
        Configuration.INPUT_FILE_NAME += "NoNeg";
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

    

    

    

    



    

    

    

    

    

    

    

    

    

    

    

    

    

    
}
