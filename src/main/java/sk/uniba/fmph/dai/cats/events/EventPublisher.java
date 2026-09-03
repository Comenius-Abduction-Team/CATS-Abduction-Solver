package sk.uniba.fmph.dai.cats.events;

import org.semanticweb.owlapi.model.OWLAxiom;
import sk.uniba.fmph.dai.cats.algorithms.AlgorithmSolver;
import sk.uniba.fmph.dai.cats.algorithms.TreeNode;
import sk.uniba.fmph.dai.cats.common.Configuration;
import sk.uniba.fmph.dai.cats.data.Explanation;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class EventPublisher {

    /**
     * Subscribers are stored per solver because multiple solver instances may exist in one JVM.
     * Entries MUST be removed after a solver finishes; otherwise this static map keeps the whole
     * solver object graph reachable and prevents it from being garbage-collected.
     */
    private static final Map<AlgorithmSolver, CopyOnWriteArrayList<IEventSubscriber>> subscribers =
            new ConcurrentHashMap<>();


    public static void publishGenericEvent(AlgorithmSolver solver, EventType type){

        if (!Configuration.EVENTS)
            return;

        List<IEventSubscriber> solverSubscribers = getSubscribers(solver);

        if (solverSubscribers == null || solverSubscribers.isEmpty())
            return;

        publishEvent(solverSubscribers, new Event(type));

    }

    public static void publishNodeEvent(AlgorithmSolver solver, EventType type, TreeNode node){

        if (!Configuration.EVENTS)
            return;

        List<IEventSubscriber> solverSubscribers = getSubscribers(solver);

        if (solverSubscribers == null || solverSubscribers.isEmpty())
            return;

        publishEvent(solverSubscribers, new NodeEvent(node, type));

    }

    public static void publishEdgeEvent(AlgorithmSolver solver, EventType type, OWLAxiom label){

        if (!Configuration.EVENTS)
            return;

        List<IEventSubscriber> solverSubscribers = getSubscribers(solver);

        if (solverSubscribers == null || solverSubscribers.isEmpty())
            return;

        publishEvent(solverSubscribers, new EdgeEvent(label, type));

    }

    public static void publishExplanationEvent(AlgorithmSolver solver, EventType type, Explanation explanation){

        if (!Configuration.EVENTS)
            return;

        List<IEventSubscriber> solverSubscribers = getSubscribers(solver);

        if (solverSubscribers == null || solverSubscribers.isEmpty())
            return;

        publishEvent(solverSubscribers, new ExplanationEvent(explanation, type));

    }

    private static void publishEvent(List<IEventSubscriber> solverSubscribers, Event event){
        for (IEventSubscriber subscriber : solverSubscribers)
            subscriber.processEvent(event);
    }

    private static List<IEventSubscriber> getSubscribers(AlgorithmSolver solver){
        if (solver == null)
            throw new IllegalArgumentException("Solver must not be null when publishing an event.");

        return subscribers.get(solver);
    }

    public static void registerSubscriber(AlgorithmSolver solver, IEventSubscriber subscriber){
        if (solver == null || subscriber == null)
            throw new IllegalArgumentException("Solver and subscriber must not be null.");

        subscribers.computeIfAbsent(solver, v -> new CopyOnWriteArrayList<>()).add(subscriber);
    }

    public static void unregisterSubscribers(AlgorithmSolver solver){
        if (solver != null)
            subscribers.remove(solver);
    }

}
