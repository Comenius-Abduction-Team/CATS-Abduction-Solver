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
/*public class SubsetMapManager {

    public enum Status {
        UNKNOWN,
        CONSISTENT,
        INCONSISTENT
    }

    //private final Map<Long, Status> map = new HashMap<>();

    private final Set<Long> consistentMasks = new HashSet<>();
    private final Set<Long> inconsistentMasks = new HashSet<>();
    //BitSet/BigInteger namiesto Long

    public boolean isKnown(long mask){
        if (consistentMasks.contains(mask)){
            return true;
        }
        else return inconsistentMasks.contains(mask);
    }

    public void markConsistent(long mask){
        //map.put(mask, Status.CONSISTENT);
        consistentMasks.add(mask);
    }

    public void markInconsistent(long mask){
        //map.put(mask, Status.INCONSISTENT);
        inconsistentMasks.add(mask);
    }

    public boolean hasConsistentSubset(long mask){
        for(long known : consistentMasks){
            if((mask & known) == mask)
                return true;
            }
        return false;
    }
    public boolean hasInconsistentSuperset(long mask){
        for(long known : inconsistentMasks){
            if((mask & known) == known){
                return true;
            }
        }
        return false;
    }
}*/

public class SubsetMapManager {

    public enum Status {
        UNKNOWN,
        CONSISTENT,
        INCONSISTENT
    }

    private final Map<BitSet, Status> map = new HashMap<>();

    private final Set<BitSet> minimalInconsistent = new HashSet<>();
    private final Set<BitSet> knownConsistent = new HashSet<>();


    public boolean isKnown(BitSet mask){
        return map.containsKey(mask);
    }


    //marks mask as consistent and stores only maximal supersets
    public void markConsistent(BitSet mask) {
        map.put(mask, Status.CONSISTENT);

        // if a superset already exists, mask is not maximal, do nothing
        for (BitSet c : knownConsistent) {
            BitSet tmp = (BitSet) c.clone();
            tmp.and(mask);
            if (tmp.equals(mask)) {
                return;
            }
        }

        // remove all subsets of mask (because mask is larger and more useful)
        knownConsistent.removeIf(c -> {
            BitSet tmp = (BitSet) mask.clone();
            tmp.and(c);
            return tmp.equals(c); // c ⊆ mask
        });

        // add mask as a new maximal consistent set
        knownConsistent.add((BitSet) mask.clone());
    }


     //marks mask as inconsistent and stores only minimal subsets
     public void markInconsistent(BitSet mask) {
         map.put(mask, Status.INCONSISTENT);

         // if a subset already exists, this is not minimal
         for (BitSet m : minimalInconsistent) {
             BitSet tmp = (BitSet) mask.clone();
             tmp.and(m);
             if (tmp.equals(m)) {
                 return;
             }
         }

         // remove all supersets of the new mask
         minimalInconsistent.removeIf(m -> {
             BitSet tmp = (BitSet) m.clone();
             tmp.and(mask);
             return tmp.equals(mask);
         });

         // add this as a new minimal inconsistent set
         minimalInconsistent.add((BitSet) mask.clone());
     }


     //checks whether the mask already has a stored inconsistent subset -> if yes, the mask is inconsistent too
    public boolean hasInconsistentSubset(BitSet mask) {  //stored ⊆ mask
        for (BitSet stored : minimalInconsistent) {
            BitSet tmp = (BitSet) mask.clone();  //pre Java 21+ existuje contains
            tmp.and(stored);
            if (tmp.equals(stored)) {
                return true;
            }
        }
        return false;
    }

    //checks whether the mask already has a stored consistent superset -> if yes, the mask is consistent too
    public boolean hasConsistentSuperset(BitSet mask) { //stored ⊇ mask
        for (BitSet stored : knownConsistent) {
            BitSet tmp = (BitSet) stored.clone();
            tmp.and(mask);
            if (tmp.equals(mask)) {
                return true;
            }
        }
        return false;
    }


    public int inconsistentBasisSize(){
        return minimalInconsistent.size();
    }

    public int mapSize(){
        return map.size();
    }

}