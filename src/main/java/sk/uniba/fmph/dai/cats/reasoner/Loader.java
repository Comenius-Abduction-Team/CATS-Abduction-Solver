package sk.uniba.fmph.dai.cats.reasoner;

import sk.uniba.fmph.dai.cats.common.LogMessage;
import sk.uniba.fmph.dai.cats.common.IPrinter;
import sk.uniba.fmph.dai.cats.data.Abducibles;
import sk.uniba.fmph.dai.cats.data.Individuals;
import sk.uniba.fmph.dai.cats.data.Observation;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.knowledgeexploration.OWLKnowledgeExplorerReasoner;
import sk.uniba.fmph.dai.cats.parser.PrefixesParser;
import uk.ac.manchester.cs.jfact.JFactFactory;

import java.util.List;

public abstract class Loader {

    protected OWLOntologyManager ontologyManager;
    protected OWLReasonerFactory reasonerFactory;
    protected OWLOntology ontology;
    protected OWLKnowledgeExplorerReasoner reasoner;

    protected Observation observation;
    protected Observation negObservation;
    protected String ontologyIRI;
    protected Individuals namedIndividuals;
    protected OWLOntology originalOntology;
    protected OWLOntology initialOntology; // initial ontology without negated observation
    protected Abducibles abducibles;

    protected OWLDocumentFormat observationOntologyFormat;
    protected boolean isMultipleObservationOnInput = false;
    protected boolean isAxiomBasedAbduciblesOnInput = false;

    protected IPrinter printer;

    public ReasonerManager reasonerManager;

    abstract public void initialize(ReasonerType reasonerType);

    protected void loadReasoner(ReasonerType reasonerType) {
        try {
            ontologyManager = OWLManager.createOWLOntologyManager();
            setupOntology();
            changeReasoner(reasonerType);
            initializeReasoner();

            if (reasoner.isConsistent()) {
                printer.logInfo(LogMessage.INFO_ONTOLOGY_CONSISTENCY);
            } else {
                reasoner.dispose();
                throw new RuntimeException(LogMessage.ERROR_ONTOLOGY_CONSISTENCY);
            }

        } catch (OWLOntologyCreationException exception) {
            throw new RuntimeException(LogMessage.ERROR_CREATING_ONTOLOGY);
        }
    }

    protected abstract void setupOntology() throws OWLOntologyCreationException;

    
    public void changeReasoner(ReasonerType reasonerType) {
        // Note: we only use JFact for now

//        switch (reasonerType) {
//            case PELLET:
//                setOWLReasonerFactory(new OpenlletReasonerFactory());
//                break;
//
//            case HERMIT:
//                setOWLReasonerFactory(new ReasonerFactory());
//                break;
//
//            case JFACT:
//                setOWLReasonerFactory(new JFactFactory());
//                break;
//        }

        setOWLReasonerFactory(new JFactFactory());
        reasoner = (OWLKnowledgeExplorerReasoner) reasonerFactory.createReasoner(ontology);
        printer.logInfo(LogMessage.INFO_ONTOLOGY_LOADED);
    }

    
    public void initializeReasoner() {
        reasoner.flush();
    }

    protected abstract void loadObservation() throws Exception;

    protected abstract void loadAbducibles();

    protected void loadPrefixes(){
        PrefixesParser prefixesParser = new PrefixesParser(observationOntologyFormat);
        prefixesParser.parse();
    }

    public Abducibles getAbducibles(){
        return abducibles;
    }

    
    public Observation getObservation() {
        return observation;
    }

    
    public OWLAxiom getObservationAxiom() {
        return observation.getOwlAxiom();
    }

    public OWLIndividual getObservationReductionIndividual(){
        return observation.getReductionIndividual();
    }

    
    public void setObservation(OWLAxiom observation) {
        this.observation = new Observation(observation);
    }

    
    public void setObservation(OWLAxiom observation, List<OWLAxiom> axiomsInMultipleObservations, OWLNamedIndividual reductionIndividual){
        this.observation = new Observation(observation, axiomsInMultipleObservations, reductionIndividual);
    }

    
    public Observation getNegObservation() {
        return negObservation;
    }

    
    public OWLAxiom getNegObservationAxiom() {
        return negObservation.getOwlAxiom();
    }

    
    public void setNegObservation(OWLAxiom negObservation) {
        this.negObservation = new Observation(negObservation);
    }

    
    public OWLOntologyManager getOntologyManager() {
        return ontologyManager;
    }

    
    public OWLOntology getOntology() {
        return ontology;
    }

    
    public OWLKnowledgeExplorerReasoner getReasoner() {
        return reasoner;
    }

    
    public void setOWLReasonerFactory(OWLReasonerFactory reasonerFactory) {
        this.reasonerFactory = reasonerFactory;
    }

    
    public String getOntologyIRI() {
        if (ontologyIRI == null) {
            ontologyIRI = ontology.getOntologyID().getOntologyIRI().get().toString();
        }
        return ontologyIRI;
    }

    
    public OWLDataFactory getDataFactory() {
        return ontologyManager.getOWLDataFactory();
    }

    
    public Individuals getIndividuals() {
        return namedIndividuals;
    }

    
    public void addNamedIndividual(OWLNamedIndividual namedIndividual) {
        namedIndividuals.addNamedIndividual(namedIndividual);
    }

    
    public OWLOntology getOriginalOntology() {
        return originalOntology;
    }

    
    public OWLOntology getInitialOntology() {
        return initialOntology;
    }


    public void setObservationOntologyFormat(OWLDocumentFormat observationOntologyFormat) {
        this.observationOntologyFormat = observationOntologyFormat;
    }

    public boolean isMultipleObservationOnInput() {
        return isMultipleObservationOnInput;
    }

    public void setMultipleObservationOnInput(boolean multipleObservationOnInput) {
        isMultipleObservationOnInput = multipleObservationOnInput;
    }

    public boolean isAxiomBasedAbduciblesOnInput() {
        return isAxiomBasedAbduciblesOnInput;
    }

    public void setAxiomBasedAbduciblesOnInput(boolean axiomBasedAbduciblesOnInput) {
        isAxiomBasedAbduciblesOnInput = axiomBasedAbduciblesOnInput;
    }
}
