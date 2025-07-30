package sk.uniba.fmph.dai.cats.algorithms.mxp;

import org.semanticweb.owlapi.model.OWLAxiom;
import sk.uniba.fmph.dai.cats.algorithms.AlgorithmSolver;
import sk.uniba.fmph.dai.cats.algorithms.INodeProcessor;
import sk.uniba.fmph.dai.cats.algorithms.RuleChecker;
import sk.uniba.fmph.dai.cats.common.Configuration;
import sk.uniba.fmph.dai.cats.common.LogMessage;
import sk.uniba.fmph.dai.cats.common.StaticPrinter;
import sk.uniba.fmph.dai.cats.data.AxiomSet;
import sk.uniba.fmph.dai.cats.data.Explanation;
import sk.uniba.fmph.dai.cats.reasoner.AxiomManager;
import sk.uniba.fmph.dai.cats.reasoner.ReasonerManager;

import java.util.*;

public class MxpNodeProcessor extends QxpNodeProcessor implements INodeProcessor {

    private final RuleChecker ruleChecker;
    private final ReasonerManager reasonerManager;

    final public Set<OWLAxiom> path;

    private boolean explanationLargerThanOne = false;

    public MxpNodeProcessor(AlgorithmSolver solver){
        super(solver);
        ruleChecker = solver.ruleChecker;
        reasonerManager = solver.loader.reasonerManager;
        path = solver.path;
    }

    @Override
    public boolean shouldPruneBranch(Explanation explanation) {

        if (Configuration.MOVE_CHECKS_AFTER_MODEL_REUSE)
            return false;

        if (!consistencyChecker.checkOntologyConsistencyWithPath(false, true)){

            solver.stats.getCurrentLevelStats().explanationEdges += 1;

            if(Configuration.CONTINUOUS_HYBRID_RELEVANCE_CHECKS && !ruleChecker.isRelevant(explanation)){
                solver.stats.getCurrentLevelStats().originalExplanations += 1;
                solver.stats.getCurrentLevelStats().filteredExplanations += 1;
                StaticPrinter.debugPrint("[CLOSING] IRRELEVANT EXPLANATION!");
                return true;
            }

            addToExplanations(explanation);
            StaticPrinter.debugPrint("[CLOSING] IS EXPLANATION!");
            return true;
        }

        return false;
    }

    @Override
    public int findExplanations(Explanation explanation, boolean extractModel) {

        explanationLargerThanOne = false;

        if (Configuration.MOVE_CHECKS_AFTER_MODEL_REUSE){

            if (!consistencyChecker.checkOntologyConsistencyWithPath(false, true)){

                solver.stats.getCurrentLevelStats().explanationEdges += 1;

                if(Configuration.CONTINUOUS_HYBRID_RELEVANCE_CHECKS && !ruleChecker.isRelevant(explanation)){
                    solver.stats.getCurrentLevelStats().originalExplanations += 1;
                    solver.stats.getCurrentLevelStats().filteredExplanations += 1;
                    StaticPrinter.debugPrint("[CLOSING] IRRELEVANT EXPLANATION!");
                    return 0;
                }

                addToExplanations(explanation);
                StaticPrinter.debugPrint("[CLOSING] IS EXPLANATION!");
                return 0; //explanation WAS found, but it was the path itself, i.e., no explanation larger than |path| can be found
            }
        }

        StaticPrinter.debugPrint("[MXP] Calling MXP");
        return addExplanationsFoundByMxp(Configuration.ALWAYS_EXTRACT_MODELS_IN_MXP || extractModel);

    }

    @Override
    public boolean shouldCloseNode(int explanationsFound) {
        if (explanationLargerThanOne)
            return false;
        boolean result = explanationsFound == 0;
        if (result)
            StaticPrinter.debugPrint("[CLOSING] Closed by MXP!");
        return result;
    }

    private int addExplanationsFoundByMxp(boolean extractModel){

        List<Explanation> newExplanations = findExplanationsWithMxp(extractModel);
        for (Explanation explanation : newExplanations){
            if (!explanationLargerThanOne && explanation.getAxioms().size() > 1){
                explanationLargerThanOne = true;
            }

            explanation.addAxioms(path);

            if(Configuration.CONTINUOUS_HYBRID_MINIMALITY_CHECKS) {
                if (!ruleChecker.isMinimal(explanationManager.getPossibleExplanations(), explanation)){
                    solver.stats.getCurrentLevelStats().originalExplanations += 1;
                    solver.stats.getCurrentLevelStats().filteredExplanations += 1;
                    continue;
                }
            }
            else if(Configuration.CHECKING_MINIMALITY_BY_QXP){
                explanation = getMinimalExplanationByCallingQXP(explanation);
                if (explanationManager.getPossibleExplanations().contains(explanation)){
                    solver.stats.getCurrentLevelStats().originalExplanations += 1;
                    solver.stats.getCurrentLevelStats().filteredExplanations += 1;
                    continue;
                }
            }

            explanationManager.addPossibleExplanation(explanation);
            if(Configuration.CACHED_CONFLICTS_MEDIAN){
                setDivider.addPairsOfLiteralsToTable(explanation);
            }

        }

        return newExplanations.size();
    }

    private List<Explanation> findExplanationsWithMxp(boolean extractModel){

        Set<OWLAxiom> abduciblesCopy = new HashSet<>();

        for (OWLAxiom a : abducibleAxioms){
            if (path.contains(a))
                continue;
            if (Configuration.REMOVE_COMPLEMENTS_FROM_MXP && Configuration.NEGATION_ALLOWED
                    && path.contains(AxiomManager.getComplementOfOWLAxiom(solver.loader, a)))
                continue;
            abduciblesCopy.add(a);
        }

        if(Configuration.CACHED_CONFLICTS_LONGEST_CONFLICT){
            setDivider.setIndexesOfExplanations(explanationManager.getPossibleExplanationsSize());
        }
        Conflict conflict = runMxp(abduciblesCopy, true, extractModel);

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

        if (!consistencyChecker.checkOntologyConsistency(extractModel)) {
            solver.message = LogMessage.INFO_NOTHING_TO_EXPLAIN;
            solver.currentLevel.message = "nothing to explain";
            return false;
        }

        StaticPrinter.debugPrint("[MXP] Initial MXP");
        Conflict conflict = runMxp(abducibleAxioms, false, extractModel);
        explanationManager.setPossibleExplanations(conflict.getExplanations());
        return true;
    }

    private Conflict runMxp(Set<OWLAxiom> axioms, boolean initialConsistencyAlreadyChecked, boolean extractModel){

        solver.removeNegatedObservationFromPath();

        if (solver.isTimeout()) {
            return new Conflict();
        }

        reasonerManager.addAxiomsToOntology(path);

        if (!initialConsistencyAlreadyChecked && !consistencyChecker.checkOntologyConsistency(extractModel))
            return new Conflict();

        // if isConsistent(B ∪ C) then return [C, ∅];
        if (consistencyChecker.checkOntologyConsistencyWithAddedAxioms(axioms, extractModel)) {
            return new Conflict();
        }

        return findConflicts(axioms, true, extractModel);
    }

    private Conflict findConflicts(Set<OWLAxiom> axioms, boolean firstIteration, boolean extractModel) {

        // if isConsistent(B ∪ C) then return [C, ∅];
        if (!firstIteration && consistencyChecker.checkOntologyConsistencyWithAddedAxioms(axioms, extractModel)) {
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
        Conflict conflictC1 = findConflicts(sets.get(0).getAxioms(), false, extractModel);
        if (Configuration.CACHED_CONFLICTS_LONGEST_CONFLICT){
            setDivider.addIndexToIndexesOfExplanations(indexOfExplanation);
        } else if (Configuration.CACHED_CONFLICTS_MEDIAN){
            setDivider.setMedian(median);
        }

        //[C'2, Γ2] ← FINDCONFLICTS(B, C2)
        Conflict conflictC2 = findConflicts(sets.get(1).getAxioms(), false, extractModel);
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
