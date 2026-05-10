package sk.uniba.fmph.dai.cats.algorithms.marco;

import org.semanticweb.owlapi.model.OWLAxiom;
import sk.uniba.fmph.dai.cats.algorithms.AlgorithmSolver;
import sk.uniba.fmph.dai.cats.algorithms.INodeProcessor;
import sk.uniba.fmph.dai.cats.algorithms.TransformedAbducibles;
import sk.uniba.fmph.dai.cats.algorithms.hst.MapArrayNumberedAxioms;
import sk.uniba.fmph.dai.cats.data.Explanation;
import sk.uniba.fmph.dai.cats.data_processing.ExplanationManager;


import java.util.BitSet;
import java.util.HashSet;
import java.util.Set;

public class MarcoNodeProcessor implements INodeProcessor {

    private final AlgorithmSolver solver;
    final SubsetMapManager map;
    private final ExplanationManager explanationManager;
    private MapArrayNumberedAxioms numbered;


    public MarcoNodeProcessor(AlgorithmSolver solver){
        this.solver = solver;
        this.map = new SubsetMapManager();;
        explanationManager = solver.explanationManager;
    }

    @Override
    public boolean canCreateRoot(boolean extractModel) {
        //System.out.print("\ncalling Marco canCreateRoot()");

        return solver.consistencyChecker.checkOntologyConsistency(extractModel);

    }

    /*@Override
    public boolean shouldPruneBranch(Explanation explanation) {
        return false;
    }*/

    @Override
    public boolean shouldPruneBranch(Explanation explanation) {
        // convert explanation to BitSet
        Set<OWLAxiom> S = new HashSet<>(explanation.getAxioms());
        BitSet mask = toMask(S);

        // if we already know the status of this set -> prune
        if (map.isKnown(mask)) {
            return true;
        }

        // if mask contains a minimal inconsistent subset -> prune
        //because explanation would not be minimal
        if (map.hasInconsistentSubset(mask)) {
            return true;
        }

        // if mask is contained in a maximal consistent superset -> prune
        // consistent set is not an explanation
        if (map.hasConsistentSuperset(mask)) {
            return true;
        }
        //o
        // therwise keep this child
        return false;
    }


    @Override
    public int findExplanations(Explanation explanation, boolean extractModel) {
        //the function works with possible explanations, checks their
        //consistency with the KB, and marks them as consistent/inconsistent
        /* VERSION 1
        Set<OWLAxiom> S = new HashSet<>(explanation.getAxioms()); //set containing axioms from the possible explanation


        if(map.isKnown(S)){  //if the consistency is already known, skip the exlanation
            return 0;
        }

        boolean consistent =
                solver.consistencyChecker.checkOntologyConsistencyWithPath(false, true); //check the consistency

        if(consistent){
            map.markConsistent(S);  //mark the the set of axioms as consistent
            return 0;
        }
        else{
            map.markInconsistent(S); //mark the set of axioms as inconsistent
            solver.explanationManager.addPossibleExplanation(explanation); //add the explanation to the set of possible explanations
            return 1;
        }*/
        /* VERSION 2
        Set<OWLAxiom> S = new HashSet<>(explanation.getAxioms()); // //set containing axioms from the possible explanation
        long mask = toMask(S); //create mask based on set S

        if(map.isKnown(mask)){  //if the consistency is already known, skip the explanation
            return 0;
        }

        if(map.hasConsistentSubset(mask)){ //if S has a consistent subset, mark S as also consistent
            map.markConsistent(mask);
            return 0;
        }


        if(map.hasInconsistentSuperset(mask)){ //if S has an inconsistent superset, mark S as also inconsistent
            map.markInconsistent(mask);
            return 0;
        }

        boolean consistent =
                solver.consistencyChecker.checkOntologyConsistencyWithPath(false, true);

        if(consistent){
            map.markConsistent(mask);
        }
        else{
            map.markInconsistent(mask);

            solver.explanationManager.addPossibleExplanation(
                    solver.createExplanationFromAxioms(S)
            );
        }

        return 0;*/

        /*
        Set<OWLAxiom> S = new HashSet<>(explanation.getAxioms()); //set of axioms that form the possible explanation
        BitSet mask = toMask(S);  //the bitmap representation of S

        //processing one subset of the set of abducibles - marking its consistency and possibly adding it to the possible explanations

        if(map.isKnown(mask)){ //skip the explanation if its status is already known
            return 0;
            }

        if(map.hasInconsistentSubset(mask)){ //if the explanation has an inconsistent subset -> mark the explanation as inconsistent (no need to check its consistency)
            map.markInconsistent(mask);      //and dont add it to the possible explanations because it is not minimal
            return 0;
            }

        // check if explanation is consistent with the KB and mark it based on the result
        boolean consistent = solver.consistencyChecker.checkOntologyConsistencyWithPath(false, true);


        if(consistent){ //if consistent -> mark it as consistent
            map.markConsistent(mask);

        }
        else{  //if inconsistent -> mark it inconsistent and add it to the possible explanations
            map.markInconsistent(mask);

            solver.explanationManager.addPossibleExplanation(
                        solver.createExplanationFromAxioms(S)
                );
            }

        return 0;
         */
        Set<OWLAxiom> S = new HashSet<>(explanation.getAxioms()); //set of axioms that form the possible explanation
        BitSet mask = toMask(S); //bitmask representation of S

        if (map.isKnown(mask)) { //status of S already known, skip it
            return 0;
        }

        // pruning using minimal inconsistent subsets
        // if mask contains an inconsistent subset, the mask (S) is inconsistent
        if (map.hasInconsistentSubset(mask)) {
            map.markInconsistent(mask);
            return 0;
        }

        // pruning using maximal consistent supersets
        // if mask is contained in a known consistent superset, the mask (S) is consistent
        if (map.hasConsistentSuperset(mask)) {
            map.markConsistent(mask);
            return 0;
        }

        // no pruning possible, call the reasoner to check consistency
        boolean consistent = solver.consistencyChecker
                .checkOntologyConsistencyWithPath(false, true);

        // store the result (mark S as consistent/inconsistent) and add S to the set of explanations
        if (consistent) {
            map.markConsistent(mask);
        } else {
            map.markInconsistent(mask);

            solver.explanationManager.addPossibleExplanation(
                    solver.createExplanationFromAxioms(S)
            );
        }

        return 0;
    }


    @Override
    public boolean shouldCloseNode(int explanationsFound) {
        return false;
    }

    @Override
    public void postProcessExplanations() {
        //System.out.print("\ncalling Marco postProcessExplanations()");
        explanationManager.readyExplanationsToProcess();
        explanationManager.filterToConsistentExplanations();
        explanationManager.filterToMinimalRelevantExplanations();
    }

    @Override
    public void storeAbduciblesIfNeeded(sk.uniba.fmph.dai.cats.algorithms.IAbducibleAxioms abducibles) {
        //mozno netrba tuto funkciu??
        numbered = (MapArrayNumberedAxioms) abducibles;

        int i = 1;
        for(OWLAxiom ax : numbered.getAxioms()){
            numbered.assignIndex(ax, i++);
        }
    }


    BitSet toMask(Set<OWLAxiom> set) {
        //convert a set to its bitmask representation
        //used in findExplanations

        BitSet mask = new BitSet();

        for (OWLAxiom ax : set) {
            int index = numbered.getIndex(ax); // 1-based
            mask.set(index - 1);               // set the bit at (index-1)
        }

        return mask;
    }

}