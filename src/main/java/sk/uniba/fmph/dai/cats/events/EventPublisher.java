package sk.uniba.fmph.dai.cats.events;

import org.semanticweb.owlapi.model.OWLAxiom;
import sk.uniba.fmph.dai.cats.algorithms.AlgorithmSolver;
import sk.uniba.fmph.dai.cats.algorithms.TreeNode;
import sk.uniba.fmph.dai.cats.common.Configuration;
import sk.uniba.fmph.dai.cats.data.Explanation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class EventPublisher {

    private static HashMap<AlgorithmSolver, List<IEventSubscriber>> subscribers;

    public static void publishGenericEvent(AlgorithmSolver solver, EventType type){

        if (!Configuration.EVENTS)
            return;

        List<IEventSubscriber> solverSubscribers = subscribers.get(solver);

        if (solverSubscribers == null || solverSubscribers.isEmpty())
            return;

        publishEvent(solverSubscribers, new Event(type));

    }

    public static void publishNodeEvent(AlgorithmSolver solver, EventType type, TreeNode node){

        if (!Configuration.EVENTS)
            return;

        List<IEventSubscriber> solverSubscribers = subscribers.get(solver);

        if (solverSubscribers == null || solverSubscribers.isEmpty())
            return;

        publishEvent(solverSubscribers, new NodeEvent(node, type));

    }

    public static void publishEdgeEvent(AlgorithmSolver solver, EventType type, OWLAxiom label){

        if (!Configuration.EVENTS)
            return;

        List<IEventSubscriber> solverSubscribers = subscribers.get(solver);

        if (solverSubscribers == null || solverSubscribers.isEmpty())
            return;

        publishEvent(solverSubscribers, new EdgeEvent(label, type));

    }

    public static void publishExplanationEvent(AlgorithmSolver solver, EventType type, Explanation explanation){

        if (!Configuration.EVENTS)
            return;

        List<IEventSubscriber> solverSubscribers = subscribers.get(solver);

        if (solverSubscribers == null || solverSubscribers.isEmpty())
            return;

        publishEvent(solverSubscribers, new ExplanationEvent(explanation, type));

    }

    private static void publishEvent(List<IEventSubscriber> solverSubscribers, Event event){
        for (IEventSubscriber subscriber : solverSubscribers)
            subscriber.processEvent(event);
    }

    public static void registerSubscriber(AlgorithmSolver solver, IEventSubscriber subscriber){

        if (subscribers == null){
            subscribers = new HashMap<>();
        }
        subscribers.computeIfAbsent(solver, v -> new ArrayList<>(1));
        subscribers.get(solver).add(subscriber);

    }


}
