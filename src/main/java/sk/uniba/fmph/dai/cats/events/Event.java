package sk.uniba.fmph.dai.cats.events;

public class Event {

    EventType type;

    public Event(EventType type) {
        this.type = type;
    }

    public EventType getEventType() {
        return type;
    }

}
