package sk.uniba.fmph.dai.cats.reasoner;

import java.util.*;
import sk.uniba.fmph.dai.cats.models.Abducibles;
import sk.uniba.fmph.dai.cats.models.Individuals;
import sk.uniba.fmph.dai.cats.models.Observation;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.knowledgeexploration.OWLKnowledgeExplorerReasoner;

public interface ILoader {

    void initialize(ReasonerType reasonerType) throws Exception;

    void changeReasoner(ReasonerType reasonerType);

    void initializeReasoner();

    Observation getObservation();

    void setObservation(OWLAxiom observation);

    void setObservation(OWLAxiom observation, List<OWLAxiom> axiomsInMultipleObservations, OWLNamedIndividual reductionIndividual);

    Observation getNegObservation();

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
