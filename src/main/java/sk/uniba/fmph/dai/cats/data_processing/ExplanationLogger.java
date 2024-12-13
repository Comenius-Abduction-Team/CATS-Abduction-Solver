package sk.uniba.fmph.dai.cats.data_processing;

import org.apache.commons.lang3.StringUtils;
import sk.uniba.fmph.dai.cats.algorithms.AlgorithmSolver;

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
        explanationManager.logger = this;
        timer = solver.timer;
    }

    public void logMessage(List<String> info, String message) {
        StringBuilder result = new StringBuilder();
        result.append(String.join("\n", info));

        if (message != null && !message.isEmpty()) {
            result.append("\n\n").append(message);
        }

        FileManager.appendToFile(FileManager.INFO_LOG__PREFIX, timer.getStartTime(), result.toString());
    }

    public void makeErrorAndPartialLog(int level, Throwable e) {
        logError(e);

        double time = timer.getCurrentTime();
        timer.setTimeForLevelIfNotSet(time, level);

        time = timer.getTimeForLevel(level);

        logExplanationsWithSize(level, false, true, time);
        if(Configuration.ALGORITHM.usesMxp()){
            logExplanationsWithSize(level + 1, false, true, time);
            logExplanationsWithLevel(level, false, true, time);
        }
    }

    public void makePartialLog(int level) {
        double time = timer.getCurrentTime();
        timer.setTimeForLevelIfNotSet(time, level);

        time = timer.getTimeForLevel(level);
        logExplanationsWithSize(level, false, false, time);
        if(Configuration.ALGORITHM.usesMxp()){
            logExplanationsWithLevel(level, false, false, time);
        }
    }

    public void makeTimeoutPartialLog(int level) {
        double time = timer.getCurrentTime();
        timer.setTimeForLevelIfNotSet(time, level);

        time = timer.getTimeForLevel(level);
        logExplanationsWithSize(level, true, false, time);
        if(Configuration.ALGORITHM.usesMxp()){
            logExplanationsWithSize(level + 1, true, false, time);
            logExplanationsWithLevel(level, true,false, time);
        }
    }

    private void logExplanationsWithSize(int size, boolean timeout, boolean error, double time) {
        if (!Configuration.LOGGING)
            return;
        List<Explanation> explanations = explanationManager.getExplanationsBySize(size);
        String explanationsFormat = StringUtils.join(explanations, ", ");
        String line = String.format("%d; %d; %.2f%s%s; { %s }\n", size, explanations.size(), time,
                timeout ? "-TIMEOUT" : "", error ? "-ERROR" : "", explanationsFormat);
        FileManager.appendToFile(FileManager.PARTIAL_LOG__PREFIX, timer.getStartTime(), line);
    }

    private void logExplanationsWithLevel(int level, boolean timeout, boolean error, double time){
        if (!Configuration.LOGGING)
            return;
        List<Explanation> explanations = explanationManager.getExplanationsByLevel(level);
        String explanationsFormat = StringUtils.join(explanations, ", ");
        String line = String.format("%d; %d; %.2f%s%s; { %s }\n", level, explanations.size(), time,
                timeout ? "-TIMEOUT" : "", error ? "-ERROR" : "", explanationsFormat);
        FileManager.appendToFile(FileManager.PARTIAL_LEVEL_LOG__PREFIX, timer.getStartTime(), line);
    }

    private void logError(Throwable e) {
        StringWriter result = new StringWriter();
        PrintWriter printWriter = new PrintWriter(result);
        e.printStackTrace(printWriter);

        FileManager.appendToFile(FileManager.ERROR_LOG__PREFIX, timer.getStartTime(), result.toString());
    }

    void logExplanationsTimes(List<Explanation> explanations){
        if (!Configuration.LOGGING)
            return;
        StringBuilder result = new StringBuilder();
        for (Explanation exp: explanations){
            String line = String.format("%.2f; %s\n", exp.getAcquireTime(), exp);
            result.append(line);
        }
        FileManager.appendToFile(FileManager.EXP_TIMES_LOG__PREFIX, timer.getStartTime(), result.toString());
    }

}
