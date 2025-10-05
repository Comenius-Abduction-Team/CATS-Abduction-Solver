package sk.uniba.fmph.dai.cats.events;

import org.semanticweb.owlapi.model.OWLAxiom;

public class EdgeEvent extends Event {

    OWLAxiom branchLabel;

    public EdgeEvent(OWLAxiom branchLabel, EventType type) {
        super(type);
        this.branchLabel = branchLabel;
    }
}
