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

/*public class SubsetMapManager {

    public enum Status {
        UNKNOWN,
        CONSISTENT,
        INCONSISTENT
    }

    private final Map<Set<OWLAxiom>, Status> map = new HashMap<>();

    public Status getStatus(Set<OWLAxiom> set){
        return map.getOrDefault(set, Status.UNKNOWN);
    }
    public int size(){
        return map.size();
    }
    public boolean isDuplicate(Set<OWLAxiom> set){
        return map.containsKey(set);
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



}*/
public class SubsetMapManager {

    public enum Status {
        UNKNOWN,
        CONSISTENT,
        INCONSISTENT
    }

    private final Map<Long, Status> map = new HashMap<>();

    private final Set<Long> consistentMasks = new HashSet<>();
    private final Set<Long> inconsistentMasks = new HashSet<>();

    public boolean isKnown(long mask){
        return map.containsKey(mask);
    }

    public void markConsistent(long mask){
        map.put(mask, Status.CONSISTENT);
        consistentMasks.add(mask);
    }

    public void markInconsistent(long mask){
        map.put(mask, Status.INCONSISTENT);
        inconsistentMasks.add(mask);
    }

    public boolean hasConsistentSubset(long mask){
        for(long known : consistentMasks){
            //if((mask & known) == known){
            if((mask & known) == mask)//known je podmnozinou mask, (chceme maximalny)
                return true;
            }
        return false;
    }
    public boolean hasInconsistentSuperset(long mask){
        for(long known : inconsistentMasks){
            if((mask & known) == known){  //known je nadmnozinou mask, (chceme minimalny)
                return true;
            }
        }
        return false;
    }
}