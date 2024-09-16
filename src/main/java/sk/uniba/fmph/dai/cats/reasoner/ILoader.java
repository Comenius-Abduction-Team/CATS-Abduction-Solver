package sk.uniba.fmph.dai.cats.reasoner;

import java.util.*;
import sk.uniba.fmph.dai.cats.data.Abducibles;
import sk.uniba.fmph.dai.cats.data.Individuals;
import sk.uniba.fmph.dai.cats.data.Observation;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.knowledgeexploration.OWLKnowledgeExplorerReasoner;

public interface ILoader {

    void initialize(ReasonerType reasonerType) throws Exception;

    void changeReasoner(ReasonerType reasonerType);

    void initializeReasoner();

    Observation getObservation();

    OWLAxiom getObservationAxiom();

    void setObservation(OWLAxiom observation);

    void setObservation(OWLAxiom observation, List<OWLAxiom> axiomsInMultipleObservations, OWLNamedIndividual reductionIndividual);

    Observation getNegObservation();

    OWLAxiom getNegObservationAxiom();

    void setNegObservation(OWLAxiom negObservation);

    OWLOntologyManager getOntologyManager();

    OWLOntology getOntology();

    OWLKnowledgeExplorerReasoner getReasoner();

    void setOWLReasonerFactory(OWLReasonerFactory reasonerFactory);

    String getOntologyIRI();

    OWLDataFactory getDataFactory();

    Individuals getIndividuals();

    void addNamedIndividual(OWLNamedIndividual namedIndividual);

    OWLOntology getOriginalOntology();

    OWLOntology getInitialOntology();

    Abducibles getAbducibles();

    boolean isMultipleObservationOnInput();

    boolean isAxiomBasedAbduciblesOnInput();
}
