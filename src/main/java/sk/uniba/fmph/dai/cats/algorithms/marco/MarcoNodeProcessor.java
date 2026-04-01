package sk.uniba.fmph.dai.cats.algorithms.marco;

import org.semanticweb.owlapi.model.OWLAxiom;
import sk.uniba.fmph.dai.cats.algorithms.AlgorithmSolver;
import sk.uniba.fmph.dai.cats.algorithms.INodeProcessor;
import sk.uniba.fmph.dai.cats.algorithms.TransformedAbducibles;
import sk.uniba.fmph.dai.cats.algorithms.hst.MapArrayNumberedAxioms;
import sk.uniba.fmph.dai.cats.data.Explanation;
import sk.uniba.fmph.dai.cats.data_processing.ExplanationManager;


import java.util.HashSet;
import java.util.Set;

public class MarcoNodeProcessor implements INodeProcessor {

    private final AlgorithmSolver solver;
    private final SubsetMapManager map;
    private final ExplanationManager explanationManager;
    private MapArrayNumberedAxioms numbered;

    public MarcoNodeProcessor(AlgorithmSolver solver, SubsetMapManager map){
        this.solver = solver;
        this.map = map;
        explanationManager = solver.explanationManager;
    }

    @Override
    public boolean canCreateRoot(boolean extractModel) {
        //System.out.print("\ncalling Marco canCreateRoot()");

        return solver.consistencyChecker.checkOntologyConsistency(extractModel);

    }

    @Override
    public boolean shouldPruneBranch(Explanation explanation) {
        return false;
    }

    @Override
    public int findExplanations(Explanation explanation, boolean extractModel) {
        /*//System.out.print("\ncalling Marco findExplanations()");
        Set<OWLAxiom> S = new HashSet<>(explanation.getAxioms());

        //System.out.println("MAP SIZE: " + map.size());

        if(map.isKnown(S)){
            return 0;
        }

        boolean consistent =
                solver.consistencyChecker.checkOntologyConsistencyWithPath(false, true);

        if(consistent){
            map.markConsistent(S);
            //System.out.print("Current map:");
            //map.printMapContents();
            return 0;
        }
        else{
            map.markInconsistent(S);
            //System.out.print("|||||found inconsistent||||");
            //System.out.print("Current map:");
            //map.printMapContents();
            solver.explanationManager.addPossibleExplanation(explanation);
            return 1;
        }*/
        Set<OWLAxiom> S = new HashSet<>(explanation.getAxioms());
        long mask = toMask(S);
        System.out.println("MASK: " + Long.toBinaryString(mask));

        if(map.isKnown(mask)){
            return 0;
        }

        //tu by sme najprv chceli najst maximalny consistent
        if(map.hasConsistentSubset(mask)){
            map.markConsistent(mask);
            return 0;
        }


        if(map.hasInconsistentSuperset(mask)){
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
        numbered = (MapArrayNumberedAxioms) abducibles;

        int i = 1;
        for(OWLAxiom ax : numbered.getAxioms()){
            numbered.assignIndex(ax, i++);
        }
    }

    // z mnoziny axiom vytvori bitmasku, kde kazdy bit reprezentuje jeden axiom.
    //long moze byt problem
    private long toMask(Set<OWLAxiom> set){
        long mask = 0L;

        for(OWLAxiom ax : set){
            int index = numbered.getIndex(ax); // 1-based
            mask |= (1L << (index - 1)); //  posunie a nastavi bit 1 na spravnu poziciu

        }

        return mask;
    }

}