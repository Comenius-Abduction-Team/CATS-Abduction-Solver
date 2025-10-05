package sk.uniba.fmph.dai.cats.events;

import sk.uniba.fmph.dai.cats.data.Explanation;

public class ExplanationEvent extends Event {

    Explanation explanation;

    public ExplanationEvent(Explanation explanation, EventType type) {
        super(type);
        this.explanation = explanation;
    }
}
