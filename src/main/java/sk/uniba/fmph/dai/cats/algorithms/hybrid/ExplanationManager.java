package sk.uniba.fmph.dai.cats.algorithms.hybrid;

import sk.uniba.fmph.dai.cats.common.Configuration;
import sk.uniba.fmph.dai.cats.common.DLSyntax;
import sk.uniba.fmph.dai.cats.common.IPrinter;
import sk.uniba.fmph.dai.cats.common.StringFactory;
import sk.uniba.fmph.dai.cats.logger.FileLogger;
import sk.uniba.fmph.dai.cats.data.Explanation;
import org.apache.commons.lang3.StringUtils;
import org.semanticweb.owlapi.model.OWLAxiom;
import sk.uniba.fmph.dai.cats.reasoner.Loader;
import sk.uniba.fmph.dai.cats.reasoner.ReasonerManager;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
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

    abstract public void addPossibleExplanation(Explanation explanation);

    abstract public void processExplanations(String message);

    public ExplanationManager(){}

    
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
        finalExplanations = new LinkedList<>();

        StringBuilder result = showExplanationsAccordingToLength(filteredExplanations);
        printer.print(result.toString());
        if (Configuration.LOGGING)
            FileLogger.appendToFile(FileLogger.FINAL_LOG__PREFIX, timer.getStartTime(), result.toString());

        logExplanationsTimes(finalExplanations);

        if(Configuration.ALGORITHM.usesMxp()){
            StringBuilder resultLevel = showExplanationsAccordingToLevel(new ArrayList<>(finalExplanations));
            FileLogger.appendToFile(FileLogger.LEVEL_LOG___PREFIX, timer.getStartTime(), resultLevel.toString());
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

    
    public void logError(Throwable e) {
        StringWriter result = new StringWriter();
        PrintWriter printWriter = new PrintWriter(result);
        e.printStackTrace(printWriter);

        FileLogger.appendToFile(FileLogger.ERROR_LOG__PREFIX, timer.getStartTime(), result.toString());
    }

    
    public void logMessages(List<String> info, String message) {
        StringBuilder result = new StringBuilder();
        result.append(String.join("\n", info));

        if (message != null && !message.isEmpty()) {
            result.append("\n\n").append(message);
        }

        FileLogger.appendToFile(FileLogger.INFO_LOG__PREFIX, timer.getStartTime(), result.toString());
    }


    private StringBuilder showExplanationsAccordingToLength(List<Explanation> filteredExplanations) {
        StringBuilder result = new StringBuilder();
        int depth = 1;
        while (!filteredExplanations.isEmpty()) {
            List<Explanation> currentExplanations = removeExplanationsWithDepth(filteredExplanations, depth);
            if(Configuration.ALGORITHM.usesMxp()){
                if(!Configuration.CHECKING_MINIMALITY_BY_QXP){
                    filterIfNotMinimal(currentExplanations);
                }
                filterIfNotRelevant(currentExplanations);
            }
            if (currentExplanations.isEmpty()) {
                depth++;
                continue;
            }

            timer.setTimeForLevelIfNotSet(findLevelTime(currentExplanations), depth);
            finalExplanations.addAll(currentExplanations);
            String currentExplanationsFormat = StringUtils.join(currentExplanations, ", ");
            String line = String.format("%d; %d; %.2f; { %s }\n", depth, currentExplanations.size(),
                    timer.getTimeForLevel(depth), currentExplanationsFormat);
            result.append(line);
            depth++;
        }

        String line = String.format("%.2f", timer.getTime());
        printer.print(line);
        result.append(line);

        return result;
    }

    private StringBuilder showExplanationsAccordingToLevel(List<Explanation> filteredExplanations){
        StringBuilder result = new StringBuilder();
        int level = 0;
        while (!filteredExplanations.isEmpty()) {
            List<Explanation> currentExplanations = removeExplanationsWithLevel(filteredExplanations, level);
            timer.setTimeForLevelIfNotSet(findLevelTime(currentExplanations), level);
            String currentExplanationsFormat = StringUtils.join(currentExplanations, ", ");
            String line = String.format("%d; %d; %.2f; { %s }\n", level, currentExplanations.size(),
                    timer.getTimeForLevel(level), currentExplanationsFormat);
            result.append(line);
            level++;
        }
        String line = String.format("%.2f", timer.getTime());
        result.append(line);
        return result;
    }

    private void filterIfNotMinimal(List<Explanation> explanations){
        List<Explanation> notMinimalExplanations = new LinkedList<>();
        for (Explanation e: explanations){
            for (Explanation m: finalExplanations){
                if (e.getAxioms().containsAll(m.getAxioms())){
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

    private List<Explanation> removeExplanationsWithDepth(List<Explanation> filteredExplanations, Integer depth) {
        List<Explanation> currentExplanations = filteredExplanations.stream().filter(explanation -> explanation.getDepth().equals(depth)).collect(Collectors.toList());
        filteredExplanations.removeAll(currentExplanations);
        return currentExplanations;
    }

    private List<Explanation> removeExplanationsWithLevel(List<Explanation> filteredExplanations, Integer level) {
        List<Explanation> currentExplanations = filteredExplanations.stream().filter(explanation -> explanation.getLevel().equals(level)).collect(Collectors.toList());
        filteredExplanations.removeAll(currentExplanations);
        return currentExplanations;
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

    private void logExplanationsTimes(List<Explanation> explanations){
        if (!Configuration.LOGGING)
            return;
        StringBuilder result = new StringBuilder();
        for (Explanation exp: explanations){
            String line = String.format("%.2f; %s\n", exp.getAcquireTime(), exp);
            result.append(line);
        }
        FileLogger.appendToFile(FileLogger.EXP_TIMES_LOG__PREFIX, timer.getStartTime(), result.toString());
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

    
    public void logExplanationsWithDepth(Integer depth, boolean timeout, boolean error, Double time) {
        if (!Configuration.LOGGING)
            return;
        List<Explanation> currentExplanations = possibleExplanations.stream().filter(explanation -> explanation.getDepth().equals(depth)).collect(Collectors.toList());
        String currentExplanationsFormat = StringUtils.join(currentExplanations, ", ");
        String line = String.format("%d; %d; %.2f%s%s; { %s }\n", depth, currentExplanations.size(), time, timeout ? "-TIMEOUT" : "", error ? "-ERROR" : "", currentExplanationsFormat);
        FileLogger.appendToFile(FileLogger.PARTIAL_LOG__PREFIX, timer.getStartTime(), line);
    }

    
    public void logExplanationsWithLevel(Integer level, boolean timeout, boolean error, Double time){
        if (!Configuration.LOGGING)
            return;
        List<Explanation> currentExplanations = possibleExplanations.stream().filter(explanation -> explanation.getLevel().equals(level)).collect(Collectors.toList());
        String currentExplanationsFormat = StringUtils.join(currentExplanations, ", ");
        String line = String.format("%d; %d; %.2f%s%s; { %s }\n", level, currentExplanations.size(), time, timeout ? "-TIMEOUT" : "", error ? "-ERROR" : "", currentExplanationsFormat);
        FileLogger.appendToFile(FileLogger.PARTIAL_LEVEL_LOG__PREFIX, timer.getStartTime(), line);
    }

    public List<Explanation> getFinalExplanations() {
        return finalExplanations;
    }
}
