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
    protected List<OWLAxiom> lengthOneExplanations = new ArrayList<>();
    protected List<Explanation> finalExplanations;
    protected AlgorithmSolver solver;
    private Loader loader;
    private ReasonerManager reasonerManager;
    private RuleChecker ruleChecker;
    protected IPrinter printer;
    TimeManager timer;

    public ExplanationLogger logger;

    abstract public void addPossibleExplanation(Explanation explanation);

    abstract public void processExplanations(String message);
    
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
        this.lengthOneExplanations = new ArrayList<>(lengthOneExplanations);
    }
    
    public List<OWLAxiom> getLengthOneExplanations() {
        return lengthOneExplanations;
    }
    
    public int getLengthOneExplanationsSize(){
        return lengthOneExplanations.size();
    }
    
    public void showExplanations() {
        List<Explanation> filteredExplanations;
        if(!Configuration.ALGORITHM.usesMxp()){
            filteredExplanations = possibleExplanations;
        } else {
            filteredExplanations = getConsistentExplanations();
        }

        solver.path.clear();
        finalExplanations = new ArrayList<>();

        StringBuilder result = formatExplanationsWithSize(filteredExplanations);
        printer.print(result.toString());
        if (Configuration.LOGGING)
            FileManager.appendToFile(FileManager.FINAL_LOG__PREFIX, timer.getStartTime(), result.toString());

        logger.logExplanationsTimes(finalExplanations);

        if(Configuration.ALGORITHM.usesMxp()){
            result = formatExplanationsWithLevel(new ArrayList<>(finalExplanations));
            FileManager.appendToFile(FileManager.LEVEL_LOG__PREFIX, timer.getStartTime(), result.toString());
        }
    }

    private List<Explanation> getConsistentExplanations() {
        reasonerManager.resetOntology(loader.getInitialOntology().axioms());

        List<Explanation> filteredExplanations = new ArrayList<>();
        for (Explanation explanation : possibleExplanations) {
            if (isExplanation(explanation)) {
                if (reasonerManager.isOntologyWithLiteralsConsistent(explanation.getAxioms(), loader.getInitialOntology())) {
                    filteredExplanations.add(explanation);
                }
            }
        }

        reasonerManager.resetOntology(loader.getOriginalOntology().axioms());
        return filteredExplanations;
    }

    private StringBuilder formatExplanationsWithSize(List<Explanation> explanations) {
        StringBuilder result = new StringBuilder();
        int size = 1;
        while (!explanations.isEmpty()) {
            List<Explanation> currentExplanations = filterExplanationsBySize(explanations, size);
            explanations.removeAll(currentExplanations);
            if(Configuration.ALGORITHM.usesMxp()){
                if(!Configuration.CHECKING_MINIMALITY_BY_QXP){
                    filterIfNotMinimal(currentExplanations);
                }
                filterIfNotRelevant(currentExplanations);
            }
            if (currentExplanations.isEmpty()) {
                size++;
                continue;
            }

            timer.setTimeForLevelIfNotSet(findLevelTime(currentExplanations), size);
            finalExplanations.addAll(currentExplanations);
            String currentExplanationsFormat = StringUtils.join(currentExplanations, ", ");
            String line = String.format("%d; %d; %.2f; { %s }\n", size, currentExplanations.size(),
                    timer.getTimeForLevel(size-1), currentExplanationsFormat);
            result.append(line);
            size++;
        }

        String line = String.format("%.2f", timer.getEndTime());
        result.append(line);

        return result;
    }

    private StringBuilder formatExplanationsWithLevel(List<Explanation> explanations){
        StringBuilder result = new StringBuilder();
        int level = 0;
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

    private boolean isExplanation(Explanation explanation) {

        //ROLY - bude to containsNegation fungovat???

        if (explanation.getAxioms().size() == 1) {
            return true;
        }

        for (OWLAxiom axiom1 : explanation.getAxioms()) {
            String name1 = StringFactory.extractClassName(axiom1);
            boolean negated1 = containsNegation(name1);
            if (negated1) {
                name1 = name1.substring(1);
            }

            for (OWLAxiom axiom2 : explanation.getAxioms()) {
                if (!axiom1.equals(axiom2) && axiom1.getIndividualsInSignature().equals(axiom2.getIndividualsInSignature())) {
                    String name2 = StringFactory.extractClassName(axiom2);

                    boolean negated2 = containsNegation(name2);
                    if (negated2) {
                        name2 = name2.substring(1);
                    }

                    if (name1.equals(name2) && ((!negated1 && negated2) || (negated1 && !negated2))) {
                        return false;
                    }
                }
            }
        }

        return true;
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

    public List<Explanation> getFinalExplanations() {
        return finalExplanations;
    }
}
