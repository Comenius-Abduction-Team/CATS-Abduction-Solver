package sk.uniba.fmph.dai.cats.events;

import sk.uniba.fmph.dai.cats.algorithms.AlgorithmSolver;

public interface IEventSubscriber {

    void processEvent(Event event);

}
