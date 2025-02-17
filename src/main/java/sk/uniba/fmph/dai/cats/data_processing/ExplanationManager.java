package sk.uniba.fmph.dai.cats.data_processing;

import org.semanticweb.owlapi.model.OWLAxiom;
import sk.uniba.fmph.dai.cats.algorithms.AlgorithmSolver;
import sk.uniba.fmph.dai.cats.algorithms.RuleChecker;
import sk.uniba.fmph.dai.cats.common.*;
import sk.uniba.fmph.dai.cats.data.Explanation;
import sk.uniba.fmph.dai.cats.timer.TimeManager;

import java.util.*;
import java.util.stream.Collectors;

public abstract class ExplanationManager {

    protected List<Explanation> possibleExplanations = new ArrayList<>();

    protected List<Explanation> explanationsToProcess = new ArrayList<>();
    protected Set<OWLAxiom> lengthOneExplanations = new HashSet<>();
    public List<Explanation> finalExplanations = new ArrayList<>();
    protected AlgorithmSolver solver;

    private RuleChecker ruleChecker;
    protected IPrinter printer;
    TimeManager timer;

    Map<Integer, List<Explanation>> explanationsBySize = new HashMap<>();

    Map<Integer, List<Explanation>> explanationsByLevel = new HashMap<>();

    public ExplanationLogger logger;

    public void addPossibleExplanation(Explanation explanation){
        possibleExplanations.add(explanation);
        StaticPrinter.debugPrint("[EXPLANATION] " + explanation + " at time: " + explanation.getAcquireTime() );
        solver.currentLevelStats.explanations++;
    }

    abstract public void processExplanations(String message, TreeStats stats);
    
    public void setSolver(AlgorithmSolver solver) {
        this.solver = solver;
        ruleChecker = solver.ruleChecker;
        timer = solver.timer;
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
    
    public void addLengthOneExplanation(OWLAxiom explanation){
        lengthOneExplanations.add(explanation);
    }
    
    public void setLengthOneExplanations(Collection<OWLAxiom> lengthOneExplanations) {
        this.lengthOneExplanations = new HashSet<>(lengthOneExplanations);
    }
    
    public Set<OWLAxiom> getLengthOneExplanations() {
        return lengthOneExplanations;
    }
    
    public int getLengthOneExplanationsSize(){
        return lengthOneExplanations.size();
    }
    
    public void showExplanations(String message, TreeStats stats) {

        groupExplanations(stats);

        StringBuilder bySize = formatExplanationsBySize();

        bySize.insert(0,"\nTotal explanations: " + finalExplanations.size());
        bySize.append("Time: ").append(stats.filteringEnd);
        if (message != null && !message.isEmpty())
            bySize.append("\n").append(message);

        StaticPrinter.print(bySize.toString());

        FileManager.appendToFile(FileManager.FINAL_LOG__PREFIX, timer.getStartTime(), bySize.toString());

        //System.out.println("EXPLANATIONS: " + finalExplanations);

//        StringBuilder result = formatExplanationsWithSize();
//        printer.print(result.toString());
//        if (Configuration.LOGGING)
//            FileManager.appendToFile(FileManager.FINAL_LOG__PREFIX, timer.getStartTime(), result.toString());
//
//        logger.logExplanationsTimes(finalExplanations);
//
//        if(Configuration.ALGORITHM.usesMxp()){
//            result = formatExplanationsWithLevel(new ArrayList<>(finalExplanations));
//            FileManager.appendToFile(FileManager.LEVEL_LOG__PREFIX, timer.getStartTime(), result.toString());
//        }
    }

    private List<Explanation> getConsistentExplanations() {

        List<Explanation> filteredExplanations = new ArrayList<>();
        for (Explanation explanation : explanationsToProcess) {
            if (!containsContradictoryAxioms(explanation)
                    && ruleChecker.checkConsistencyUsingNewReasoner(explanation)) {
                filteredExplanations.add(explanation);
            }
        }

        return filteredExplanations;
    }

    private StringBuilder formatExplanationsBySize(){
        StringBuilder result = new StringBuilder("\n");

        for (Integer size : explanationsBySize.keySet()){

            if (explanationsBySize.get(size).isEmpty())
                continue;

            result.append(size);
            result.append("; ");

            List<Explanation> explanations = explanationsBySize.get(size);

            result.append(explanations.size());
            result.append("; ");

            result.append(StringFactory.getExplanationsRepresentation(explanations));
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
                .filter(explanation -> level == explanation.getLevel())
                .collect(Collectors.toList());
    }

    private boolean containsContradictoryAxioms(Explanation explanation) {

        if (explanation.getAxioms().size() == 1) {
            return false;
        }

        for (int i = 0; i < explanation.getAxioms().size(); i++) {
            OWLAxiom axiom1 = explanation.getAxioms().get(i);
            String name1 = StringFactory.extractClassName(axiom1);
            boolean negated1 = containsNegation(name1);
            if (negated1) {
                name1 = name1.substring(1);
            }

            for (int j = i+1; j < explanation.getAxioms().size(); j++) {
                OWLAxiom axiom2 = explanation.getAxioms().get(j);
                if (!axiom1.equals(axiom2) && axiom1.getIndividualsInSignature().equals(axiom2.getIndividualsInSignature())) {
                    String name2 = StringFactory.extractClassName(axiom2);

                    boolean negated2 = containsNegation(name2);
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

    private boolean containsNegation(String name) {
        return name.contains(DLSyntax.DISPLAY_NEGATION);
    }

    public List<Explanation> getExplanationsBySize(int size) {
        return filterExplanationsBySize(possibleExplanations, size);
    }

    public List<Explanation> getExplanationsByLevel(int level) {
        return filterExplanationsByLevel(possibleExplanations, level);
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

    public void groupExplanations(TreeStats stats){
        for (Explanation e : finalExplanations){

            int level = e.getLevel();

            LevelStats levelStats = stats.getLevelStats(level);
            levelStats.finalExplanations++;

            double time = e.getAcquireTime();

            if (levelStats.firstExplanation == null || time < levelStats.firstExplanation) {
                levelStats.firstExplanation = time;
            }

            if (levelStats.lastExplanation == null || time > levelStats.lastExplanation)
                levelStats.lastExplanation = time;

            putExplanationIntoMap(e,level,explanationsByLevel);
            putExplanationIntoMap(e,e.size(),explanationsBySize);

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
