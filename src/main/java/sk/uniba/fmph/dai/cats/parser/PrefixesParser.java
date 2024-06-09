package sk.uniba.fmph.dai.cats.parser;

import sk.uniba.fmph.dai.cats.common.Prefixes;
import org.semanticweb.owlapi.model.*;

public class

PrefixesParser {

    private final OWLDocumentFormat observationOntologyFormat;

    public PrefixesParser(OWLDocumentFormat observationOntologyFormat) {
        this.observationOntologyFormat = observationOntologyFormat;
    }

    public void parse() {
        if (observationOntologyFormat.isPrefixOWLDocumentFormat()) {
            Prefixes.prefixes = observationOntologyFormat.asPrefixOWLDocumentFormat().getPrefixName2PrefixMap();
        }
}
}
