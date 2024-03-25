package sk.uniba.fmph.dai.cats.api_implementation;

import sk.uniba.fmph.dai.cats.common.Configuration;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import sk.uniba.fmph.dai.cats.parser.ObservationParser;

public class ApiObservationParser extends ObservationParser {

    private final CatsAbducer Abducer;

    public ApiObservationParser(ApiLoader loader, CatsAbducer abdctionManager){
        super(loader);
        this.Abducer = abdctionManager;
        printer = new ApiPrinter(abdctionManager);
    }

    @Override
    protected void createOntologyFromObservation() throws OWLOntologyCreationException {

        OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager();
        OWLOntology observationOntology = ontologyManager.createOntology(Abducer.getObservation());
        Configuration.OBSERVATION = observationOntology.toString();
        processAxiomsFromObservation(observationOntology);

    }

}
