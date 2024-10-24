package sk.uniba.fmph.dai.cats.logger;

import org.apache.commons.lang3.StringUtils;
import sk.uniba.fmph.dai.cats.algorithms.hybrid.AlgorithmSolver;
import sk.uniba.fmph.dai.cats.algorithms.hybrid.ExplanationManager;

import sk.uniba.fmph.dai.cats.timer.TimeManager;
import sk.uniba.fmph.dai.cats.common.Configuration;
import sk.uniba.fmph.dai.cats.data.Explanation;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

public class ExplanationLogger {

    final AlgorithmSolver solver;

    final ExplanationManager explanationManager;

    final TimeManager timer;

    public ExplanationLogger(AlgorithmSolver solver) {
        this.solver = solver;
        explanationManager = solver.explanationManager;
        timer = solver.timer;
    }

    public void logExplanationsWithSize(int size, boolean timeout, boolean error, Double time) {
        if (!Configuration.LOGGING)
            return;
        List<Explanation> explanations = explanationManager.getExplanationsBySize(size);
        String currentExplanationsFormat = StringUtils.join(explanations, ", ");
        String line = String.format("%d; %d; %.2f%s%s; { %s }\n", size, explanations.size(), time, timeout ? "-TIMEOUT" : "", error ? "-ERROR" : "", currentExplanationsFormat);
        FileManager.appendToFile(FileManager.PARTIAL_LOG__PREFIX, timer.getStartTime(), line);
    }

    public void logExplanationsWithLevel(int level, boolean timeout, boolean error, Double time){
        if (!Configuration.LOGGING)
            return;
        List<Explanation> explanations = explanationManager.getExplanationsByLevel(level);
        String currentExplanationsFormat = StringUtils.join(explanations, ", ");
        String line = String.format("%d; %d; %.2f%s%s; { %s }\n", level, explanations.size(), time, timeout ? "-TIMEOUT" : "", error ? "-ERROR" : "", currentExplanationsFormat);
        FileManager.appendToFile(FileManager.PARTIAL_LEVEL_LOG__PREFIX, timer.getStartTime(), line);
    }

    public void logError(Throwable e) {
        StringWriter result = new StringWriter();
        PrintWriter printWriter = new PrintWriter(result);
        e.printStackTrace(printWriter);

        FileManager.appendToFile(FileManager.ERROR_LOG__PREFIX, timer.getStartTime(), result.toString());
    }

    public void logMessage(List<String> info, String message) {
        StringBuilder result = new StringBuilder();
        result.append(String.join("\n", info));

        if (message != null && !message.isEmpty()) {
            result.append("\n\n").append(message);
        }

        FileManager.appendToFile(FileManager.INFO_LOG__PREFIX, timer.getStartTime(), result.toString());
    }

    public void makeErrorAndPartialLog(int size, Throwable e) {
        logError(e);

        double time = timer.getTime();
        timer.setTimeForLevel(time, size);

        logExplanationsWithSize(size, false, true, time);
        if(Configuration.ALGORITHM.usesMxp()){
            logExplanationsWithSize(size + 1, false, true, time);
            logExplanationsWithLevel(size, false, true, time);
        }
    }

    public void makePartialLog(int size) {
        Double time = timer.getTime();
        timer.setTimeForLevel(time, size);
        logExplanationsWithSize(size, false, false, time);
        if(Configuration.ALGORITHM.usesMxp()){
            logExplanationsWithLevel(size, false, false, time);
        }
    }

    public void makeTimeoutPartialLog(int size) {
        Double time = timer.getTime();
        timer.setTimeForLevel(time, size);
        logExplanationsWithSize(size, true, false, time);
        if(Configuration.ALGORITHM.usesMxp()){
            logExplanationsWithSize(size + 1, true, false, time);
            logExplanationsWithLevel(size, true,false, time);
        }
    }

}
