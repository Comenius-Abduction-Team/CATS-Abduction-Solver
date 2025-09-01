package sk.uniba.fmph.dai.cats.data_processing;

import sk.uniba.fmph.dai.cats.algorithms.AlgorithmSolver;

import sk.uniba.fmph.dai.cats.common.StringFactory;
import sk.uniba.fmph.dai.cats.metrics.Level;
import sk.uniba.fmph.dai.cats.metrics.MetricsManager;
import sk.uniba.fmph.dai.cats.common.Configuration;
import sk.uniba.fmph.dai.cats.data.Explanation;
import sk.uniba.fmph.dai.cats.metrics.TreeStats;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

public class ExplanationLogger {

    final AlgorithmSolver solver;

    final ExplanationManager explanationManager;

    final MetricsManager timer;

    private String startTimeText, partialLog, levelLog;

    public ExplanationLogger(AlgorithmSolver solver) {
        this.solver = solver;
        explanationManager = solver.explanationManager;
        explanationManager.logger = this;
        timer = solver.metrics;
    }

    void logExplanationsTimes(List<Explanation> explanations){
        if (!Configuration.LOGGING)
            return;
        StringBuilder result = new StringBuilder();
        for (Explanation e: explanations){
            result.append(StringFactory.buildCsvRow(false, e.getAcquireTime(), e.size(), e));
            result.append('\n');
        }
        log(LogTypes.TIMES, result);
    }

    void createFinalLogs(String finalLogContent, String levelLogContent){
        if (!Configuration.LOGGING)
            return;
        log(LogTypes.FINAL, finalLogContent);
        log(getLevelLogFileName(true), levelLogContent);
    }

    void clearPartialLog(){
        if (FileManager.checkIfFileExists(getPartialLogFileName(false))
                && FileManager.checkIfFileExists(getLevelLogFileName(false))){
            FileManager.deleteFile(partialLog);
        }
    }

    private String getStartTime(){
        if (startTimeText == null){
            startTimeText = StringFactory.formatTimeWithDate(timer.getStartTime());
        }
        return startTimeText;
    }

    private String getPartialLogFileName(boolean createDirectory){
        if (partialLog == null)
            partialLog = FileManager.createLogFileName(LogTypes.PARTIAL, getStartTime(), createDirectory);
        return partialLog;
    }

    private String getLevelLogFileName(boolean createDirectory){
        if (levelLog == null)
            levelLog = FileManager.createLogFileName(LogTypes.LEVEL, getStartTime(), createDirectory);
        return levelLog;
    }

    public void logInfo(List<String> info, String message) {
        if (!Configuration.LOGGING)
            return;
        StringBuilder result = new StringBuilder();
        result.append(String.join("\n", info));

        if (message != null && !message.isEmpty()) {
            result.append("\n\n").append(message);
        }
        log(LogTypes.INFO, result);
    }

    public void makeErrorAndPartialLog(Level level, Throwable e) {
        if (!Configuration.LOGGING)
            return;
        logError(e);
        addLevelToPartialLog(level);

    }

    public void addLevelToPartialLog(Level level) {
        if (!Configuration.LOGGING)
            return;
        String content = buildLevelContent(level);
        log(getPartialLogFileName(true), content);
    }

    private String buildLevelContent(Level level){
        StringBuilder builder = new StringBuilder();
        if (level.depth == 0){
            builder.append(TreeStats.getCsvHeader(false));
            builder.append('\n');
        }
        solver.stats.buildCsvRow(builder,level);
        return builder.toString();
    }

    private void logError(Throwable e) {
        StringWriter result = new StringWriter();
        PrintWriter printWriter = new PrintWriter(result);
        e.printStackTrace(printWriter);
        log(LogTypes.ERROR, result);
    }

    /**
     * Appends content to a log file of a given type. If the log file does not exist, it is created.
     * This should be used for logs that should be created by this method call and not reused again, i.e., their file
     * name does not need to be reused elsewhere in code.
     *
     * @param  type the type of log file
     * @param  data content that is converted into string and appended into the log file
     */
    private void log(LogTypes type, Object data){
        String fileName = FileManager.createLogFileName(type, getStartTime(), true);
        FileManager.appendToFile(fileName, data.toString());
    }

    /**
     * Appends content to a log file of a given type. If the log file does not exist, it is created.
     * This should be used for logs that are reused elsewhere.
     *
     * @param  fileName name of the log file
     * @param  data content that is converted into string and appended into the log file
     */
    private void log(String fileName, Object data){
        FileManager.appendToFile(fileName, data.toString());
    }

}
