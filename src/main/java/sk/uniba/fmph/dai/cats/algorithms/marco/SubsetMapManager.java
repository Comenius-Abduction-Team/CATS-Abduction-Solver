package sk.uniba.fmph.dai.cats.algorithms.marco;
import org.semanticweb.owlapi.model.OWLAxiom;
import sk.uniba.fmph.dai.cats.algorithms.TransformedAbducibles;
import sk.uniba.fmph.dai.cats.algorithms.hst.INumberedAbducibles;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


import org.semanticweb.owlapi.model.OWLAxiom;

import java.util.*;

public class SubsetMapManager {

    public enum Status {
        UNKNOWN,
        CONSISTENT,
        INCONSISTENT
    }

    private final Map<Set<OWLAxiom>, Status> map = new HashMap<>();

    public Status getStatus(Set<OWLAxiom> set){
        return map.getOrDefault(set, Status.UNKNOWN);
    }

    public void markConsistent(Set<OWLAxiom> set){
        map.put(new HashSet<>(set), Status.CONSISTENT);
    }

    public void markInconsistent(Set<OWLAxiom> set){
        map.put(new HashSet<>(set), Status.INCONSISTENT);
    }

    public boolean isKnown(Set<OWLAxiom> set){
        return map.containsKey(set);
    }

    public void printMapContents() {
        map.forEach((axioms, status) -> {
            System.out.println("Status: " + status);
            axioms.forEach(ax -> System.out.println("  - " + ax));
            System.out.println("-----");
        });
    }



}