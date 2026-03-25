package sk.uniba.fmph.dai.cats.algorithms.marco;

import org.semanticweb.owlapi.model.OWLAxiom;
import sk.uniba.fmph.dai.cats.algorithms.AlgorithmSolver;
import sk.uniba.fmph.dai.cats.algorithms.INodeProcessor;
import sk.uniba.fmph.dai.cats.algorithms.TransformedAbducibles;
import sk.uniba.fmph.dai.cats.data.Explanation;
import sk.uniba.fmph.dai.cats.data_processing.ExplanationManager;


import java.util.HashSet;
import java.util.Set;

public class MarcoNodeProcessor implements INodeProcessor {

    private final AlgorithmSolver solver;
    private final SubsetMapManager map;
    private final ExplanationManager explanationManager;

    public MarcoNodeProcessor(AlgorithmSolver solver, SubsetMapManager map){
        this.solver = solver;
        this.map = map;
        explanationManager = solver.explanationManager;
    }

    @Override
    public boolean canCreateRoot(boolean extractModel) {
        System.out.print("\ncalling Marco canCreateRoot()");

        return solver.consistencyChecker.checkOntologyConsistency(extractModel);

    }

    @Override
    public boolean shouldPruneBranch(Explanation explanation) {
        return false;
    }

    @Override
    public int findExplanations(Explanation explanation, boolean extractModel) {
        System.out.print("\ncalling Marco findExplanations()");
        Set<OWLAxiom> S = new HashSet<>(explanation.getAxioms());

        if(map.isKnown(S)){
            return 0;
        }

        boolean consistent =
                solver.consistencyChecker.checkOntologyConsistencyWithPath(false, true);

        if(consistent){
            map.markConsistent(S);
            System.out.print("Current map:");
            map.printMapContents();
            return 0;
        }
        else{
            map.markInconsistent(S);
            System.out.print("|||||found inconsistent||||");
            System.out.print("Current map:");
            map.printMapContents();
            solver.explanationManager.addPossibleExplanation(explanation);
            return 1;
        }
    }

    @Override
    public boolean shouldCloseNode(int explanationsFound) {
        return false;
    }

    @Override
    public void postProcessExplanations() {
        System.out.print("\ncalling Marco postProcessExplanations()");
        //explanationManager.finalisePossibleExplanations();
        //explanationManager.filterToMinimalRelevantExplanations();
        //explanationManager.groupFinalExplanationsBySize();
        explanationManager.readyExplanationsToProcess();
        explanationManager.filterToConsistentExplanations();
        explanationManager.filterToMinimalRelevantExplanations();
    }

    @Override
    public void storeAbduciblesIfNeeded(sk.uniba.fmph.dai.cats.algorithms.IAbducibleAxioms abducibles) {}

}