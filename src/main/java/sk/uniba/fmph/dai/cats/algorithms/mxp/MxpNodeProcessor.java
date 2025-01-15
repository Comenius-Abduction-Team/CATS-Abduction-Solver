package sk.uniba.fmph.dai.cats.algorithms.mxp;

import com.google.common.collect.Iterables;
import org.semanticweb.owlapi.model.OWLAxiom;
import sk.uniba.fmph.dai.cats.algorithms.AlgorithmSolver;
import sk.uniba.fmph.dai.cats.algorithms.NodeProcessor;
import sk.uniba.fmph.dai.cats.algorithms.RuleChecker;
import sk.uniba.fmph.dai.cats.common.Configuration;
import sk.uniba.fmph.dai.cats.common.StaticPrinter;
import sk.uniba.fmph.dai.cats.data.AxiomSet;
import sk.uniba.fmph.dai.cats.data.Explanation;
import sk.uniba.fmph.dai.cats.reasoner.ReasonerManager;

import java.util.*;

public class MxpNodeProcessor extends QxpNodeProcessor implements NodeProcessor {

    private final RuleChecker ruleChecker;
    private final ReasonerManager reasonerManager;

    final public Set<OWLAxiom> path;

    public MxpNodeProcessor(AlgorithmSolver solver){
        super(solver);
        ruleChecker = solver.ruleChecker;
        reasonerManager = solver.loader.reasonerManager;
        path = solver.path;
    }

    @Override
    public boolean isInvalidExplanation(Explanation explanation) {

        if(Configuration.CONTINUOUS_RELEVANCE_CHECKS && !ruleChecker.isRelevant(explanation)){
            StaticPrinter.debugPrint("[PRUNING] IRRELEVANT EXPLANATION!");
            return true;
        }

        if (isSupersetOfExistingExplanation(explanation)) {
            StaticPrinter.debugPrint("[PRUNING] SUPERSET OF EXPLANATION");
            return true;
        }

        if (!consistencyChecker.checkOntologyConsistencyWithPath(false, true)){
        //if (ruleChecker.isExplanation(explanation)){
            addToExplanations(explanation);
            StaticPrinter.debugPrint("[PRUNING] IS EXPLANATION!");
            solver.stats.getCurrentLevelStats().explanation_edges += 1;
            return true;
        }
        return false;
    }

    private boolean isSupersetOfExistingExplanation(Explanation explanation){
        for (Explanation existingExplanation : explanationManager.getPossibleExplanations()){
            if (explanation.containsAll(existingExplanation))
                return true;
        }
        return false;
    }

    @Override
    public boolean findExplanations(Explanation explanation, boolean extractModel) {
        StaticPrinter.debugPrint("[MXP] Calling MXP");
        boolean result = !addExplanationsFoundByMxp();
        if (result)
            StaticPrinter.debugPrint("[PRUNING] Pruned by MXP!");
        return result;
    }

    private boolean addExplanationsFoundByMxp(){
        List<Explanation> newExplanations = findExplanationsWithMxp();
        explanationManager.getLengthOneExplanations().clear();
        for (Explanation explanation : newExplanations){
            if (explanation.getAxioms().size() == 1){
                explanationManager.addLengthOneExplanation(Iterables.get(explanation.getAxioms(), 0));
            }

            explanation.addAxioms(path);
            if (!ruleChecker.isMinimal(explanationManager.getPossibleExplanations(), explanation)){
                continue;
            }

            if(Configuration.CHECKING_MINIMALITY_BY_QXP){
                explanation = getMinimalExplanationByCallingQXP(explanation);
            }
            explanationManager.addPossibleExplanation(explanation);
            if(Configuration.CACHED_CONFLICTS_MEDIAN){
                setDivider.addPairsOfLiteralsToTable(explanation);
            }

        }
        if (newExplanations.size() == explanationManager.getLengthOneExplanationsSize()){
            return false;
        }
        return !newExplanations.isEmpty();
    }

    private List<Explanation> findExplanationsWithMxp(){

        Set<OWLAxiom> abduciblesCopy = new HashSet<>();

        Set<OWLAxiom> lengthOne = explanationManager.getLengthOneExplanations();

        for (OWLAxiom a : solver.abducibleAxioms.getAxioms()){
            if (path.contains(a))
                continue;
            if (lengthOne.contains(a)) {
                continue;
            }
            abduciblesCopy.add(a);
        }

        if(Configuration.CACHED_CONFLICTS_LONGEST_CONFLICT){
            setDivider.setIndexesOfExplanations(explanationManager.getPossibleExplanationsSize());
        }
        Conflict conflict = findConflicts(abduciblesCopy, true);

        return conflict.getExplanations();
    }

    protected void addToExplanations(Explanation explanation){
        if(Configuration.CHECKING_MINIMALITY_BY_QXP){
            Explanation newExplanation = getMinimalExplanationByCallingQXP(explanation);
            explanationManager.addPossibleExplanation(newExplanation);
            if(Configuration.CACHED_CONFLICTS_MEDIAN){
                setDivider.addPairsOfLiteralsToTable(newExplanation);
            }
        } else {
            explanationManager.addPossibleExplanation(explanation);
            if(Configuration.CACHED_CONFLICTS_MEDIAN){
                setDivider.addPairsOfLiteralsToTable(explanation);
            }
        }
    }

    public Explanation getMinimalExplanationByCallingQXP(Explanation explanation){
        Set<OWLAxiom> potentialExplanations = new HashSet<>(explanation.getAxioms());
        if(path != null){
            potentialExplanations.addAll(path);
        }

        Set<OWLAxiom> minimalityPath = new HashSet<>();

        consistencyChecker.turnMinimalityCheckingOn(minimalityPath);
        Explanation newExplanation = getConflict(minimalityPath, new HashSet<>(), potentialExplanations, false);
        consistencyChecker.turnMinimalityCheckingOff();

        return newExplanation;
    }

    @Override
    public boolean canCreateRoot(boolean extractModel) {
        StaticPrinter.debugPrint("[MXP] Initial MXP");
        Conflict conflict = findConflicts(solver.abducibleAxioms.getAxioms(), extractModel);
        explanationManager.setPossibleExplanations(conflict.getExplanations());
        return true;
    }

    private Conflict findConflicts(Set<OWLAxiom> axioms, boolean extractModel) {

        solver.removeNegatedObservationFromPath();

        if (solver.isTimeout()) {
            return new Conflict(new HashSet<>(), new ArrayList<>());
        }

        reasonerManager.addAxiomsToOntology(path);

        // if isConsistent(B ∪ C) then return [C, ∅];
        if (consistencyChecker.checkOntologyConsistencyWithAddedAxioms(axioms, extractModel)) {
            return new Conflict(axioms, new ArrayList<>());
        }

        reasonerManager.resetOntologyToOriginal();

        // if |C| = 1 then return [∅, {C}];
        if (axioms.size() == 1) {
            List<Explanation> explanations = new ArrayList<>();
            explanations.add(solver.createExplanationFromAxioms(axioms));
            return new Conflict(new HashSet<>(), explanations);
        }

        int indexOfExplanation = -1;
        if(Configuration.CACHED_CONFLICTS_LONGEST_CONFLICT){
            indexOfExplanation = setDivider.getIndexOfTheLongestAndNotUsedConflict();
        }

        // Split C into disjoint, non-empty sets C1 and C2
        List<AxiomSet> sets = setDivider.divideIntoSets(axioms);

        double median = setDivider.getMedian();

        //[C'1, Γ1] ← FINDCONFLICTS(B, C1)
        Conflict conflictC1 = findConflicts(sets.get(0).getAxioms(), extractModel);
        if (Configuration.CACHED_CONFLICTS_LONGEST_CONFLICT){
            setDivider.addIndexToIndexesOfExplanations(indexOfExplanation);
        } else if (Configuration.CACHED_CONFLICTS_MEDIAN){
            setDivider.setMedian(median);
        }

        //[C'2, Γ2] ← FINDCONFLICTS(B, C2)
        Conflict conflictC2 = findConflicts(sets.get(1).getAxioms(), extractModel);
        if (Configuration.CACHED_CONFLICTS_LONGEST_CONFLICT){
            setDivider.addIndexToIndexesOfExplanations(indexOfExplanation);
        } else if (Configuration.CACHED_CONFLICTS_MEDIAN){
            setDivider.setMedian(median);
        }

        // Γ ← Γ1 ∪ Γ2;
        List<Explanation> explanations = new LinkedList<>();
        explanations.addAll(conflictC1.getExplanations());
        explanations.addAll(conflictC2.getExplanations());

        // C'1 ∪ C'2
        Set<OWLAxiom> conflictLiterals = new HashSet<>();
        conflictLiterals.addAll(conflictC1.getAxioms());
        conflictLiterals.addAll(conflictC2.getAxioms());

        // while ¬isConsistent(C'1 ∪ C'2 ∪ B) do
        while (!consistencyChecker.checkOntologyConsistencyWithAddedAxioms(conflictLiterals, extractModel)) {

            if ((Configuration.DEPTH < 1) && Configuration.TIMEOUT > 0)
                if (Configuration.PRINT_PROGRESS)
                    solver.updateProgress();

            if (solver.isTimeout()) break;

            // X ← GETCONFLICT(B ∪ C'2, C'2, C'1)
            path.addAll(conflictC2.getAxioms());
            Explanation X = getConflict(path, conflictC2.getAxioms(), conflictC1.getAxioms(), extractModel);
            path.removeAll(conflictC2.getAxioms());

            // temp ← GETCONFLICT(B ∪ X, X, C'2)
            path.addAll(X.getAxioms());
            Explanation CS = getConflict(path, X.getAxioms(), conflictC2.getAxioms(), extractModel);
            // removeAll(X) is inefficient if X is a list and its size is larger than the set itself
            X.getAxioms().forEach(path::remove);

            // CS ← X ∪ temp
            CS.getAxioms().addAll(X.getAxioms());

            // C'1 ← C'1 \ {α} where α ∈ X
            Set<OWLAxiom> c1Axioms = conflictC1.getAxioms();
            conflictLiterals.removeAll(c1Axioms);

            // toto funguje na 100% iba ak je C'1 nadmnozinou X
            //X.getAxioms().stream().findAny().ifPresent(axiom -> conflictC1.getAxioms().remove(axiom));

            for (OWLAxiom a : X.getAxioms()){
                if (c1Axioms.contains(a)){
                    c1Axioms.remove(a);
                    break;
                }
            }

            conflictLiterals.addAll(c1Axioms);

            if (explanations.contains(CS) || solver.isTimeout()) {
                break;
            }

            if(Configuration.CHECKING_MINIMALITY_BY_QXP){
                CS = getMinimalExplanationByCallingQXP(CS);
            }
            explanations.add(CS);
            if(Configuration.CACHED_CONFLICTS_MEDIAN){
                setDivider.addPairsOfLiteralsToTable(CS);
            }
        }

        return new Conflict(conflictLiterals, explanations);
    }
}
