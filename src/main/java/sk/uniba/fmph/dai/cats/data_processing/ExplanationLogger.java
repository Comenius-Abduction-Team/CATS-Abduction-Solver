package sk.uniba.fmph.dai.cats.data_processing;

import org.apache.commons.lang3.StringUtils;
import sk.uniba.fmph.dai.cats.algorithms.AlgorithmSolver;

import sk.uniba.fmph.dai.cats.common.LogTypes;
import sk.uniba.fmph.dai.cats.common.StringFactory;
import sk.uniba.fmph.dai.cats.timer.MetricsManager;
import sk.uniba.fmph.dai.cats.common.Configuration;
import sk.uniba.fmph.dai.cats.data.Explanation;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ExplanationLogger {

    final AlgorithmSolver solver;

    final ExplanationManager explanationManager;

    final MetricsManager timer;

    private String startTimeText;

    public ExplanationLogger(AlgorithmSolver solver) {
        this.solver = solver;
        explanationManager = solver.explanationManager;
        explanationManager.logger = this;
        timer = solver.metrics;
    }

    private String getStartTime(){
        if (startTimeText == null){
            startTimeText = StringFactory.formatTimeWithDate(timer.getStartTime());
        }
        return startTimeText;
    }

    public void logMessage(List<String> info, String message) {
        StringBuilder result = new StringBuilder();
        result.append(String.join("\n", info));

        if (message != null && !message.isEmpty()) {
            result.append("\n\n").append(message);
        }
        log(LogTypes.INFO, result);
    }

    public void makeErrorAndPartialLog(Level level, Throwable e) {
        logError(e);
        createPartialLevelLog(level);

//        logExplanationsWithSize(level, false, true, time);
//        if(Configuration.ALGORITHM.usesMxp()){
//            logExplanationsWithSize(level + 1, false, true, time);
//            createPartialLevelLog(level);
//        }
    }

    public void makePartialLog(Level level) {
        createPartialLevelLog(level);
//        time = timer.getTimeForLevel(level);
//        logExplanationsWithSize(level, false, false, time);
    }

    public void makeTimeoutPartialLog(Level level) {
        createPartialLevelLog(level);
//        time = timer.getTimeForLevel(level);
//        logExplanationsWithSize(level, true, false, time);
//        if(Configuration.ALGORITHM.usesMxp()){
//            logExplanationsWithSize(level + 1, true, false, time);
//        }
    }

    private void logExplanationsWithSize(int size, boolean timeout, boolean error, double time) {
        if (!Configuration.LOGGING)
            return;
        List<Explanation> explanations = explanationManager.getExplanationsBySize(size);
        String explanationsFormat = StringUtils.join(explanations, ", ");
        String line = String.format("%d; %d; %.2f%s%s; { %s }\n", size, explanations.size(), time,
                timeout ? "-TIMEOUT" : "", error ? "-ERROR" : "", explanationsFormat);
        //FileManager.appendToFile(FileManager.PARTIAL_LOG__PREFIX, getStartTime(), line);
    }

    private void createPartialLevelLog(Level level){
        if (!Configuration.LOGGING)
            return;

        StringBuilder builder = new StringBuilder();
        if (level.depth == 0){
            builder.append(TreeStats.getCsvHeader(false));
            builder.append('\n');
        }

        solver.stats.buildCsvRow(builder,level);
        log(LogTypes.PARTIAL, builder);
    }

    private void logError(Throwable e) {
        StringWriter result = new StringWriter();
        PrintWriter printWriter = new PrintWriter(result);
        e.printStackTrace(printWriter);
        log(LogTypes.ERROR, result);
    }

    void logExplanationsTimes(List<Explanation> explanations){
        if (!Configuration.LOGGING)
            return;
        StringBuilder result = new StringBuilder();
        for (Explanation e: explanations){
            result.append(StringFactory.buildCsvRow(false, e.getAcquireTime(), e));
            result.append('\n');
        }
        log(LogTypes.TIMES, result);
    }

    void log(LogTypes type, Object data){
        FileManager.appendToFile(type, getStartTime(), data.toString());
    }

}
