package sk.uniba.fmph.dai.cats.reasoner;

import sk.uniba.fmph.dai.cats.common.Configuration;
import sk.uniba.fmph.dai.cats.common.ConsolePrinter;
import sk.uniba.fmph.dai.cats.data.Individuals;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import sk.uniba.fmph.dai.cats.parser.*;

import java.io.File;

public class ConsoleLoader extends Loader {

    public ConsoleLoader(){
        super();
        printer = new ConsolePrinter();
    }

    @Override
    public void initialize(ReasonerType reasonerType) throws Exception {
        loadReasoner(reasonerType);
        loadObservation();
        loadPrefixes();
        loadAbducibles();
    }

    @Override
    protected void setupOntology() throws OWLOntologyCreationException {
        File ontologyFile = new File(Configuration.INPUT_ONT_FILE);
        ontology = ontologyManager.loadOntologyFromOntologyDocument(ontologyFile);
        originalOntology = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(ontologyFile);
        initialOntology = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(ontologyFile);
    }

    @Override
    protected void loadObservation() throws Exception {
        namedIndividuals = new Individuals();

        IObservationParser observationParser = new ConsoleObservationParser(this);
        observationParser.parse();
    }

    @Override
    protected void loadAbducibles(){
        AbduciblesParser abduciblesParser = new AbduciblesParser(this);
        abducibles = abduciblesParser.parse();
    }

}
