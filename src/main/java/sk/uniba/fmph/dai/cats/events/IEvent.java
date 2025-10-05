package sk.uniba.fmph.dai.cats.events;

import sk.uniba.fmph.dai.cats.algorithms.AlgorithmSolver;

public interface IEvent {

    public EventType getEventType();

    public AlgorithmSolver getSolver();

}
