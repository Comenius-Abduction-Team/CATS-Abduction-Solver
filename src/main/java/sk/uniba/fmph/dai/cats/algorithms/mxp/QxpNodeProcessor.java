package sk.uniba.fmph.dai.cats.algorithms.mxp;

import org.semanticweb.owlapi.model.OWLAxiom;
import sk.uniba.fmph.dai.cats.algorithms.AlgorithmSolver;
import sk.uniba.fmph.dai.cats.algorithms.ConsistencyChecker;
import sk.uniba.fmph.dai.cats.algorithms.NodeProcessor;
import sk.uniba.fmph.dai.cats.common.LogMessage;
import sk.uniba.fmph.dai.cats.common.StaticPrinter;
import sk.uniba.fmph.dai.cats.data.AxiomSet;
import sk.uniba.fmph.dai.cats.data.Explanation;
import sk.uniba.fmph.dai.cats.data_processing.ExplanationManager;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class QxpNodeProcessor  implements NodeProcessor {

    protected final AlgorithmSolver solver;
    protected final SetDivider setDivider;
    protected final ConsistencyChecker consistencyChecker;
    protected final ExplanationManager explanationManager;

    public QxpNodeProcessor(AlgorithmSolver solver) {

        this.solver = solver;
        this.consistencyChecker = solver.consistencyChecker;
        this.explanationManager = solver.explanationManager;
        setDivider = new SetDivider(explanationManager);
    }

    @Override
    public boolean canCreateRoot(boolean extractModel) {

        if (!consistencyChecker.checkOntologyConsistency(extractModel)) {
            solver.message = LogMessage.INFO_NOTHING_TO_EXPLAIN;
            solver.currentLevel.message = "nothing to explain";
            return false;
        }

        StaticPrinter.debugPrint("[QXP] Initial QXP");
        solver.removeNegatedObservationFromPath();
        Explanation explanation = getConflict(solver.path, solver.path, solver.abducibleAxioms.getAxioms(), false);
        explanationManager.addPossibleExplanation(explanation);
        return true;
    }

    //                                              B                          D                     C
    protected Explanation getConflict(Set<OWLAxiom> path, Collection<OWLAxiom> axioms, Set<OWLAxiom> literals, boolean extractModel) {

//        if (solver.isTimeout()) {
//            return new Explanation();
//        }

        // if D != ∅ ∧ ¬isConsistent(B) then return ∅;
        if (!axioms.isEmpty() && !consistencyChecker.checkOntologyConsistencyWithPath(extractModel, false)) {
            return new Explanation();
        }

        // if |C| = 1 then return C;
        if (literals.size() == 1) {
            return solver.createExplanationFromAxioms(literals);
        }

        // Split C into disjoint, non-empty sets
        List<AxiomSet> sets = setDivider.divideIntoSetsWithoutCondition(literals);

        //B ∪ C1
        path.addAll(sets.get(0).getAxioms());
        //D2 ← GETCONFLICT (B ∪ C1, C1, C2)
        Explanation D2 = getConflict(path, sets.get(0).getAxioms(), sets.get(1).getAxioms(), extractModel);
        //B ∪ C1 back to B
        path.removeAll(sets.get(0).getAxioms());

        //B ∪ D2
        path.addAll(D2.getAxioms());
        //D1 ← GETCONFLICT (B ∪ D2, D2, C1)
        Explanation D1 = getConflict(path, D2.getAxioms(), sets.get(0).getAxioms(), extractModel);
        //B ∪ D2 back to B
        D2.getAxioms().forEach(path::remove);

        Set<OWLAxiom> conflicts = new HashSet<>();
        conflicts.addAll(D1.getAxioms());
        conflicts.addAll(D2.getAxioms());

        return solver.createExplanationFromAxioms(conflicts);
    }

    @Override
    public boolean isInvalidExplanation(Explanation explanation) {
        return false;
    }

    @Override
    public boolean findExplanations(Explanation explanation, boolean extractModel) {
        return false;
    }

    @Override
    public void postProcessExplanations() {
        explanationManager.readyExplanationsToProcess();
        explanationManager.filterToConsistentExplanations();
        explanationManager.filterToMinimalRelevantExplanations();
    }



}
