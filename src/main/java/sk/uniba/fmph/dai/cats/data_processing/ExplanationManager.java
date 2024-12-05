package sk.uniba.fmph.dai.cats.data_processing;

import org.apache.commons.lang3.StringUtils;
import org.semanticweb.owlapi.model.OWLAxiom;
import sk.uniba.fmph.dai.cats.algorithms.hybrid.AlgorithmSolver;
import sk.uniba.fmph.dai.cats.algorithms.hybrid.RuleChecker;
import sk.uniba.fmph.dai.cats.common.Configuration;
import sk.uniba.fmph.dai.cats.common.DLSyntax;
import sk.uniba.fmph.dai.cats.common.IPrinter;
import sk.uniba.fmph.dai.cats.common.StringFactory;
import sk.uniba.fmph.dai.cats.data.Explanation;
import sk.uniba.fmph.dai.cats.reasoner.Loader;
import sk.uniba.fmph.dai.cats.reasoner.ReasonerManager;
import sk.uniba.fmph.dai.cats.timer.TimeManager;

import java.util.*;
import java.util.stream.Collectors;

public abstract class ExplanationManager {

    protected List<Explanation> possibleExplanations = new ArrayList<>();

    protected List<Explanation> explanationsToProcess = new ArrayList<>();
    protected Set<OWLAxiom> lengthOneExplanations = new HashSet<>();
    protected List<Explanation> finalExplanations = new ArrayList<>();
    protected AlgorithmSolver solver;
    private Loader loader;
    private ReasonerManager reasonerManager;
    private RuleChecker ruleChecker;
    protected IPrinter printer;
    TimeManager timer;

    Map<Integer, List<Explanation>> explanationsBySize = new HashMap<>();

    Map<Integer, List<Explanation>> explanationsByLevel = new HashMap<>();

    public ExplanationLogger logger;

    abstract public void addPossibleExplanation(Explanation explanation);

    abstract public void processExplanations(String message, TreeStats stats);
    
    public void setSolver(AlgorithmSolver solver) {
        this.solver = solver;
        loader = solver.loader;
        reasonerManager = loader.reasonerManager;
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
    
    public void showExplanations(TreeStats stats) {

        groupExplanations(possibleExplanations.isEmpty(), stats);

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

    private StringBuilder formatExplanationsWithSize() {
        StringBuilder result = new StringBuilder();
        int size = 1;
        while (!possibleExplanations.isEmpty()) {
            List<Explanation> currentExplanations = filterExplanationsBySize(possibleExplanations, size);
            possibleExplanations.removeAll(currentExplanations);
            if(Configuration.ALGORITHM.usesMxp()){
                if(!Configuration.CHECKING_MINIMALITY_BY_QXP){
                    filterIfNotMinimal(currentExplanations);
                }
                filterIfNotRelevant(currentExplanations);
            }
            explanationsBySize.put(size, currentExplanations);
            if (currentExplanations.isEmpty()) {
                size++;
                continue;
            }

            timer.setTimeForLevelIfNotSet(findLevelTime(currentExplanations), size);
            finalExplanations.addAll(currentExplanations);
            String currentExplanationsFormat = StringUtils.join(currentExplanations, ", ");
            String line = String.format("%d; %d; %.2f; { %s }\n", size, currentExplanations.size(),
                    timer.getTimeForLevel(size), currentExplanationsFormat);
            result.append(line);
            size++;
        }

        String line = String.format("%.2f", timer.getEndTime());
        result.append(line);

        return result;
    }

    private StringBuilder formatExplanationsWithLevel(List<Explanation> explanations){
        StringBuilder result = new StringBuilder();
        int level = -1;
        while (!explanations.isEmpty()) {
            List<Explanation> filteredExplanations = filterExplanationsByLevel(explanations, level);
            explanations.removeAll(filteredExplanations);
            timer.setTimeForLevelIfNotSet(findLevelTime(filteredExplanations), level);
            String currentExplanationsFormat = StringUtils.join(filteredExplanations, ", ");
            String line = String.format("%d; %d; %.2f; { %s }\n", level, filteredExplanations.size(),
                    timer.getTimeForLevel(level), currentExplanationsFormat);
            result.append(line);
            level++;
        }
        String line = String.format("%.2f", timer.getEndTime());
        result.append(line);
        return result;
    }

    private void filterIfNotMinimal(List<Explanation> explanations){
        List<Explanation> notMinimalExplanations = new LinkedList<>();
        for (Explanation e: explanations){
            for (Explanation m: finalExplanations){
                if (new HashSet<>(e.getAxioms()).containsAll(m.getAxioms())){
                    notMinimalExplanations.add(e);
                }
            }
        }
        explanations.removeAll(notMinimalExplanations);
    }

    private void filterIfNotRelevant(List<Explanation> explanations) {
        List<Explanation> notRelevantExplanations = new LinkedList<>();
        for(Explanation e : explanations){
            if(!ruleChecker.isRelevant(e)){
                notRelevantExplanations.add(e);
            }
        }
        //System.out.println(notRelevantExplanations);
        explanations.removeAll(notRelevantExplanations);
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

    private double findLevelTime(List<Explanation> explanations){
        double time = 0;
        for (Explanation exp: explanations){
            if (exp.getAcquireTime() > time){
                time = exp.getAcquireTime();
            }
        }
        return time;
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
        explanationsToProcess = getConsistentExplanations();
    }

    public void filterToMinimalRelevantExplanations(){
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

    public void groupExplanations(boolean bySize, TreeStats stats){
        for (Explanation e : finalExplanations){

            int level = e.getLevel();

            LevelStats levelStats = stats.getLevelStats(level);
            levelStats.explanations++;

            double time = e.getAcquireTime();

            if (time < levelStats.firstExplanation) {
                levelStats.firstExplanation = time;
                System.out.println(time + " < " + levelStats.firstExplanation);
            }
            else System.out.println(time + " > " + levelStats.firstExplanation);

            if (time > levelStats.lastExplanation)
                levelStats.lastExplanation = time;

            putExplanationIntoMap(e,level,explanationsByLevel);

            if (bySize){
                putExplanationIntoMap(e,e.size(),explanationsBySize);
            }

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
