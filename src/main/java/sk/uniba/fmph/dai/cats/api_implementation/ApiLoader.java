package sk.uniba.fmph.dai.cats.api_implementation;

import sk.uniba.fmph.dai.abduction_api.abducible.IAxiomAbducibles;
import sk.uniba.fmph.dai.cats.data.Abducibles;
import sk.uniba.fmph.dai.cats.data.Individuals;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import sk.uniba.fmph.dai.cats.parser.ObservationParser;
import sk.uniba.fmph.dai.cats.reasoner.Loader;
import sk.uniba.fmph.dai.cats.reasoner.ReasonerType;

import java.util.stream.Collectors;

public class ApiLoader extends Loader {

    private final CatsAbducer Abducer;

    public ApiLoader(CatsAbducer Abducer){
        this.Abducer = Abducer;
        printer = new ApiPrinter(Abducer);
    }

    @Override
    public void initialize(ReasonerType reasonerType) throws Exception {
        loadReasoner(reasonerType);
        loadObservation();
        loadAbducibles();
    }

    @Override
    protected void setupOntology() throws OWLOntologyCreationException {

        ontology = this.Abducer.getBackgroundKnowledge();
        ontologyManager = ontology.getOWLOntologyManager();

        observationOntologyFormat = ontology.getFormat();
        ontologyIRI = ontology.getOntologyID().getOntologyIRI().toString();

        originalOntology = ontologyManager.createOntology();
        copyOntology(ontology, originalOntology);

        initialOntology = ontologyManager.createOntology();
        copyOntology(ontology, initialOntology);
    }

    private void copyOntology(OWLOntology oldOntology, OWLOntology newOntology){
        ontologyManager.addAxioms(newOntology, oldOntology.getAxioms());
    }

    @Override
    protected void loadObservation() throws Exception {
        namedIndividuals = new Individuals();
        ObservationParser observationParser = new ApiObservationParser(this, Abducer);
        observationParser.parse();
    }

    @Override
    protected void loadAbducibles() {
        CatsAbducibles container = Abducer.getAbducibles();

        if (container == null || container.isEmpty()) {
            abducibles = new Abducibles(this);
            return;
        }

        if (container instanceof IAxiomAbducibles)
            isAxiomBasedAbduciblesOnInput = true;

        abducibles = container.exportAbducibles(this);

        if (container instanceof CatsSymbolAbducibles) {
            CatsSymbolAbducibles converted = (CatsSymbolAbducibles) container;
            if (converted.getIndividuals().isEmpty()) {
                abducibles.addIndividuals(ontology.individualsInSignature().collect(Collectors.toList()));
            }
        }
    }
}
