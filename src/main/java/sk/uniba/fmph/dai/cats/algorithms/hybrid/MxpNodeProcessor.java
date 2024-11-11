package sk.uniba.fmph.dai.cats.algorithms.hybrid;

import com.google.common.collect.Iterables;
import org.semanticweb.owlapi.model.OWLAxiom;
import sk.uniba.fmph.dai.cats.common.Configuration;
import sk.uniba.fmph.dai.cats.data.AxiomSet;
import sk.uniba.fmph.dai.cats.data.Explanation;
import sk.uniba.fmph.dai.cats.data_processing.ExplanationManager;
import sk.uniba.fmph.dai.cats.reasoner.ReasonerManager;

import java.util.*;

public class MxpNodeProcessor implements NodeProcessor {

    private final AlgorithmSolver solver;
    private final SetDivider setDivider;
    private final RuleChecker ruleChecker;
    private final ConsistencyChecker consistencyChecker;
    private final ReasonerManager reasonerManager;
    private final ExplanationManager explanationManager;

    final public Set<OWLAxiom> path;
    MxpNodeProcessor(AlgorithmSolver solver){
        this.solver = solver;
        setDivider = solver.setDivider;
        ruleChecker = solver.ruleChecker;
        consistencyChecker = solver.consistencyChecker;
        reasonerManager = solver.loader.reasonerManager;
        explanationManager = solver.explanationManager;
        path = solver.path;
    }

    @Override
    public boolean isInvalidExplanation(Explanation explanation) {

        if(Configuration.CONTINUOUS_RELEVANCE_CHECKS && !ruleChecker.isRelevant(explanation)){
            if (Configuration.DEBUG_PRINT)
                System.out.println("[PRUNING] IRRELEVANT EXPLANATION!");
            return true;
        }

        if (ruleChecker.isExplanation(explanation)){
            addToExplanations(explanation);
            return true;
        }
        return false;
    }

    @Override
    public boolean cannotAddExplanation(Explanation explanation, boolean extractModel) {
        boolean result = !addNewExplanations();
        if (result && Configuration.DEBUG_PRINT)
            System.out.println("[PRUNING] Pruned by MXP!");
        return result;
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
        Explanation newExplanation = getConflict(new HashSet<>(), potentialExplanations, minimalityPath);
        consistencyChecker.turnMinimalityCheckingOff();

        return newExplanation;
    }

    @Override
    public boolean canCreateRoot() {
        runMxpInRoot();
        return true;
    }

    void runMxpInRoot(){
        Conflict conflict = getMergeConflict();
        explanationManager.setPossibleExplanations(conflict.getExplanations());
    }

    private Conflict getMergeConflict() {
        return findConflicts(solver.abducibleAxioms.getAxioms());
    }

    private Conflict findConflicts(Set<OWLAxiom> axioms) {

        solver.removeNegatedObservationFromPath();

        if (solver.isTimeout()) {
            return new Conflict(new HashSet<>(), new ArrayList<>());
        }

        reasonerManager.addAxiomsToOntology(path);

        // if isConsistent(B ∪ C) then return [C, ∅];
        if (consistencyChecker.isOntologyWithLiteralsConsistent(axioms)) {
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
        Conflict conflictC1 = findConflicts(sets.get(0).getAxioms());
        if (Configuration.CACHED_CONFLICTS_LONGEST_CONFLICT){
            setDivider.addIndexToIndexesOfExplanations(indexOfExplanation);
        } else if (Configuration.CACHED_CONFLICTS_MEDIAN){
            setDivider.setMedian(median);
        }

        //[C'2, Γ2] ← FINDCONFLICTS(B, C2)
        Conflict conflictC2 = findConflicts(sets.get(1).getAxioms());
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
        while (!consistencyChecker.isOntologyWithLiteralsConsistent(conflictLiterals)) {

            if ((Configuration.DEPTH_LIMIT < 1) && Configuration.TIMEOUT > 0)
                if (Configuration.PRINT_PROGRESS)
                    solver.updateProgress();

            if (solver.isTimeout()) break;

            // X ← GETCONFLICT(B ∪ C'2, C'2, C'1)
            path.addAll(conflictC2.getAxioms());
            Explanation X = getConflict(conflictC2.getAxioms(), conflictC1.getAxioms(), path);
            path.removeAll(conflictC2.getAxioms());

            // temp ← GETCONFLICT(B ∪ X, X, C'2)
            path.addAll(X.getAxioms());
            Explanation CS = getConflict(X.getAxioms(), conflictC2.getAxioms(), path);
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

            Explanation newExplanation = CS;
            if(Configuration.CHECKING_MINIMALITY_BY_QXP){
                newExplanation = getMinimalExplanationByCallingQXP(CS);
            }
            explanations.add(newExplanation);
            if(Configuration.CACHED_CONFLICTS_MEDIAN){
                setDivider.addPairsOfLiteralsToTable(newExplanation);
            }
        }

        return new Conflict(conflictLiterals, explanations);
    }

    protected Explanation getConflict(Collection<OWLAxiom> axioms, Set<OWLAxiom> literals, Set<OWLAxiom> actualPath) {

        if (solver.isTimeout()) {
            return new Explanation();
        }

        // if D != ∅ ∧ ¬isConsistent(B) then return ∅;
        if (!axioms.isEmpty() && !consistencyChecker.checkConsistencyWithModelExtraction()) {
            return new Explanation();
        }

        // if |C| = 1 then return C;
        if (literals.size() == 1) {
            return solver.createExplanationFromAxioms(literals);
        }

        // Split C into disjoint, non-empty sets
        List<AxiomSet> sets = setDivider.divideIntoSetsWithoutCondition(literals);

        actualPath.addAll(sets.get(0).getAxioms());
        Explanation D2 = getConflict(sets.get(0).getAxioms(), sets.get(1).getAxioms(), actualPath);
        actualPath.removeAll(sets.get(0).getAxioms());

        actualPath.addAll(D2.getAxioms());
        Explanation D1 = getConflict(D2.getAxioms(), sets.get(0).getAxioms(), actualPath);
        actualPath.removeAll(D2.getAxioms());

        Set<OWLAxiom> conflicts = new HashSet<>();
        conflicts.addAll(D1.getAxioms());
        conflicts.addAll(D2.getAxioms());

        return solver.createExplanationFromAxioms(conflicts);
    }

    protected boolean addNewExplanations(){
        List<Explanation> newExplanations = findExplanations();
        explanationManager.setLengthOneExplanations(new ArrayList<>());
        for (Explanation explanation : newExplanations){
            if (explanation.getAxioms().size() == 1){
                explanationManager.addLengthOneExplanation(Iterables.get(explanation.getAxioms(), 0));
            }
            explanation.addAxioms(path);
            if (ruleChecker.isMinimal(explanationManager.getPossibleExplanations(), explanation)){
                Explanation newExplanation = explanation;
                if(Configuration.CHECKING_MINIMALITY_BY_QXP){
                    newExplanation = getMinimalExplanationByCallingQXP(explanation);
                }
                explanationManager.addPossibleExplanation(newExplanation);
                if(Configuration.CACHED_CONFLICTS_MEDIAN){
                    setDivider.addPairsOfLiteralsToTable(newExplanation);
                }
            }
        }
        if (newExplanations.size() == explanationManager.getLengthOneExplanationsSize()){
            return false;
        }
        return !newExplanations.isEmpty();
    }

    protected List<Explanation> findExplanations(){

        Set<OWLAxiom> copy = new HashSet<>();

        List<OWLAxiom> lengthOne = explanationManager.getLengthOneExplanations();

        for (OWLAxiom a : solver.abducibleAxioms.getAxioms()){
            if (path.contains(a))
                continue;
            if (lengthOne.contains(a))
                continue;
            copy.add(a);
        }

        if(Configuration.CACHED_CONFLICTS_LONGEST_CONFLICT){
            setDivider.setIndexesOfExplanations(explanationManager.getPossibleExplanationsSize());
        }
        Conflict conflict = findConflicts(copy);

        return conflict.getExplanations();
    }
}
