package sk.uniba.fmph.dai.cats.data_processing;

import org.semanticweb.owlapi.model.OWLAxiom;
import sk.uniba.fmph.dai.cats.algorithms.AlgorithmSolver;
import sk.uniba.fmph.dai.cats.algorithms.RuleChecker;
import sk.uniba.fmph.dai.cats.common.*;
import sk.uniba.fmph.dai.cats.data.Explanation;
import sk.uniba.fmph.dai.cats.metrics.Level;
import sk.uniba.fmph.dai.cats.metrics.MetricsManager;
import sk.uniba.fmph.dai.cats.metrics.TreeStats;

import java.util.*;
import java.util.stream.Collectors;

public abstract class ExplanationManager {

    protected List<Explanation> possibleExplanations = new ArrayList<>();

    protected List<Explanation> explanationsToProcess = new ArrayList<>();
    public List<Explanation> finalExplanations = new ArrayList<>();
    protected AlgorithmSolver solver;

    private RuleChecker ruleChecker;
    protected IPrinter printer;
    MetricsManager timer;

    Map<Integer, List<Explanation>> explanationsBySize = new HashMap<>();

    public ExplanationLogger logger;

    public void addPossibleExplanation(Explanation explanation){
        possibleExplanations.add(explanation);
        StaticPrinter.debugPrint("[EXPLANATION] " + explanation + " at time: " + explanation.getAcquireTime() );
        solver.currentLevel.originalExplanations++;
    }

    abstract public void processExplanations(String message, TreeStats stats);
    
    public void setSolver(AlgorithmSolver solver) {
        this.solver = solver;
        ruleChecker = solver.ruleChecker;
        timer = solver.metrics;
    }
    
    public void setPossibleExplanations(Collection<Explanation> possibleExplanations) {
        this.possibleExplanations = new ArrayList<>();
        possibleExplanations.forEach(this::addPossibleExplanation);
    }
    
    public List<Explanation> getPossibleExplanations() {
        return possibleExplanations;
    }
    
    public int getPossibleExplanationsSize(){
        return possibleExplanations.size();
    }
    
    public void showExplanations(String message, TreeStats stats) {

        groupFinalExplanationsByLevel();

        StringBuilder bySize = formatExplanationsBySize();

        bySize.insert(0,"Total explanations: " + finalExplanations.size());
        bySize.append("Time: ").append(stats.getFilteringStats().finish);
        if (message != null && !message.isEmpty())
            bySize.append("\n").append(message);

        StaticPrinter.print(bySize.toString());

        logger.createFinalLogs(bySize.toString(), stats.buildCsvTable());
        logger.logExplanationsTimes(finalExplanations);
        logger.clearPartialLog();

    }

    private List<Explanation> getConsistentExplanations() {

        List<Explanation> filteredExplanations = new ArrayList<>();
        for (Explanation explanation : explanationsToProcess) {
            if (!containsContradictoryAxioms(explanation)
                    && ruleChecker.checkConsistencyUsingNewReasoner(explanation)) {
                filteredExplanations.add(explanation);
            } else {
                solver.stats.getFilteringStats().filteredExplanations += 1;
            }
        }

        return filteredExplanations;
    }

    private StringBuilder formatExplanationsBySize(){
        StringBuilder result = new StringBuilder("\n");

        for (Integer size : explanationsBySize.keySet()){

            if (explanationsBySize.get(size).isEmpty())
                continue;

            List<Explanation> explanations = explanationsBySize.get(size);

            result.append(StringFactory.buildCsvRow(
                    true, size, explanations.size(), StringFactory.getExplanationsRepresentation(explanations)
            ));
            result.append("\n");

        }

        return result;
    }

    private void filterIfNotMinimal(List<Explanation> explanations){
        List<Explanation> notMinimalExplanations = new ArrayList<>();
        for (Explanation e : explanations){
            for (Explanation m : finalExplanations){
                if (new HashSet<>(e.getAxioms()).containsAll(m.getAxioms())){
                    notMinimalExplanations.add(e);
                }
            }
        }
        explanations.removeAll(notMinimalExplanations);
    }

    private int filterIfNotRelevant(List<Explanation> explanations) {
        List<Explanation> notRelevantExplanations = new LinkedList<>();
        for(Explanation e : explanations){
            if(!ruleChecker.isRelevant(e)){
                notRelevantExplanations.add(e);
                solver.stats.getFilteringStats().filteredExplanations += 1;
            }
        }
        explanations.removeAll(notRelevantExplanations);
        return notRelevantExplanations.size();
    }

    private List<Explanation> filterExplanationsBySize(List<Explanation> explanations, int size) {
        return explanations
                .stream()
                .filter(explanation -> size == explanation.size())
                .collect(Collectors.toList());
    }

    private List<Explanation> filterExplanationsByLevel(List<Explanation> filteredExplanations, int level) {
        return filteredExplanations
                .stream()
                .filter(explanation -> level == explanation.getDepth())
                .collect(Collectors.toList());
    }

    private boolean containsContradictoryAxioms(Explanation explanation) {

        if (explanation.getAxioms().size() == 1) {
            return false;
        }

        for (int i = 0; i < explanation.getAxioms().size(); i++) {
            OWLAxiom axiom1 = explanation.getAxioms().get(i);
            String name1 = StringFactory.extractClassName(axiom1);
            boolean negated1 = DLSyntax.containsNegation(name1);
            if (negated1) {
                name1 = name1.substring(1);
            }

            for (int j = i+1; j < explanation.getAxioms().size(); j++) {
                OWLAxiom axiom2 = explanation.getAxioms().get(j);
                if (!axiom1.equals(axiom2) && axiom1.getIndividualsInSignature().equals(axiom2.getIndividualsInSignature())) {
                    String name2 = StringFactory.extractClassName(axiom2);

                    boolean negated2 = DLSyntax.containsNegation(name2);
                    if (negated2) {
                        name2 = name2.substring(1);
                    }

                    if (name1.equals(name2) && ((!negated1 && negated2) || (negated1 && !negated2))) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public List<Explanation> getExplanationsBySize(int size) {
        return filterExplanationsBySize(possibleExplanations, size);
    }

    public void finalisePossibleExplanations(){
        finalExplanations.addAll(possibleExplanations);
    }

    public void readyExplanationsToProcess(){
        explanationsToProcess.addAll(possibleExplanations);
    }

    public void filterToConsistentExplanations(){
        if (!explanationsToProcess.isEmpty())
            explanationsToProcess = getConsistentExplanations();
    }

    public void filterToMinimalRelevantExplanations(){

        if (explanationsToProcess.size() == 1){
            int removed = filterIfNotRelevant(explanationsToProcess);
            if (removed == 1)
                return;
            explanationsBySize.put(explanationsToProcess.get(0).size(), explanationsToProcess);
            finalExplanations.addAll(explanationsToProcess);
            return;
        }

        int size = 1;
        while (!explanationsToProcess.isEmpty()) {
            List<Explanation> currentExplanations = filterExplanationsBySize(explanationsToProcess, size);
            explanationsToProcess.removeAll(currentExplanations);

            if (!Configuration.CHECKING_MINIMALITY_BY_QXP) {
                filterIfNotMinimal(currentExplanations);
            }
            filterIfNotRelevant(currentExplanations);

            explanationsBySize.put(size, currentExplanations);
            finalExplanations.addAll(currentExplanations);
            size++;
        }
    }

    public void groupFinalExplanationsByLevel(){
        for (Explanation e : finalExplanations){

            Level level = e.level;
            level.finalExplanations++;

            double time = e.getAcquireTime();

            if (level.firstExplanationTime == null || time < level.firstExplanationTime) {
                level.firstExplanationTime = time;
            }

            if (level.lastExplanationTime == null || time > level.lastExplanationTime)
                level.lastExplanationTime = time;

            level.addFinalExplanation(e);
        }
    }

    public void groupFinalExplanationsBySize(){
        for (Explanation e : finalExplanations){
            int size = e.size();
            List<Explanation> explanations = explanationsBySize.computeIfAbsent(size, _k -> new ArrayList<>());
            explanations.add(e);
        }
    }

    private void putExplanationIntoMap(Explanation explanation, int key, Map<Integer,List<Explanation>> map){
        List<Explanation> listToInsert;
        List<Explanation> existingList = map.get(key);
        if (existingList == null){
            listToInsert = new ArrayList<>();
            map.put(key, listToInsert);
        }
        else {
            listToInsert = existingList;
        }
        listToInsert.add(explanation);
    }
}
