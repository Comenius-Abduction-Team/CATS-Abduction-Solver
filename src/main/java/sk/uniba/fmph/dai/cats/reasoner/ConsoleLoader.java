package sk.uniba.fmph.dai.cats.reasoner;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import sk.uniba.fmph.dai.cats.common.Configuration;
import sk.uniba.fmph.dai.cats.common.ConsolePrinter;
import sk.uniba.fmph.dai.cats.data.Individuals;
import sk.uniba.fmph.dai.cats.parser.AbduciblesParser;
import sk.uniba.fmph.dai.cats.parser.ConsoleObservationParser;
import sk.uniba.fmph.dai.cats.parser.ObservationParser;

import java.io.File;

public class ConsoleLoader extends Loader {

    public ConsoleLoader(){
        super();
        printer = new ConsolePrinter();
    }

    @Override
    public void initialize(ReasonerType reasonerType) {
        loadReasoner(reasonerType);
        loadObservation();
        loadPrefixes();
        loadAbducibles();
    }

    @Override
    protected void setupOntology() throws OWLOntologyCreationException {
        File ontologyFile = new File(Configuration.INPUT_ONT_FILE);
        ontology = ontologyManager.loadOntologyFromOntologyDocument(ontologyFile);
        ontology = filterOntology(ontology);
        originalOntology = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(ontologyFile);
        initialOntology = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(ontologyFile);
    }

    @Override
    protected void loadObservation() {
        namedIndividuals = new Individuals();

        ObservationParser observationParser = new ConsoleObservationParser(this);
        observationParser.parse();
    }

    @Override
    protected void loadAbducibles(){
        AbduciblesParser abduciblesParser = new AbduciblesParser(this);
        abducibles = abduciblesParser.parse();
    }

}
