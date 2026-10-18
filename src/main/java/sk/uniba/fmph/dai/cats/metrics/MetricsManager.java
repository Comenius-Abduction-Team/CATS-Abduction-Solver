package sk.uniba.fmph.dai.cats.metrics;

import sk.uniba.fmph.dai.cats.common.Configuration;

import java.util.Locale;

public class MetricsManager {

    final MetricsThread metricsThread;
    private long startTime;
    private double endTime;
    private long wallClockStartNanos;
    private long wallClockEndNanos;
    private double cpuTime = Double.NaN;
    private double userTime = Double.NaN;
    private double clockTime = Double.NaN;

    public MetricsManager(MetricsThread metricsThread){
        this.metricsThread = metricsThread;

    }

    public void setStartTime(){
        wallClockStartNanos = System.nanoTime();
        metricsThread.start();
        metricsThread.awaitInitialization();
        startTime = System.currentTimeMillis();
    }

    public void setEndTime(){
        metricsThread.refreshTimeRecords();
        wallClockEndNanos = System.nanoTime();
        userTime = metricsThread.getTotalUserTimeInSec();
        cpuTime = metricsThread.getTotalCpuTimeInSec();
        clockTime = (wallClockEndNanos - wallClockStartNanos) / 1000000000.0;
        endTime = userTime;
        metricsThread.terminate();
    }

    public double getEndTime(){
        return endTime;
    }

    public double getRunningTime(){
        return getCpuTime();
    }

    public double getClockTime(){
        return Double.isNaN(clockTime) ? (System.nanoTime() - wallClockStartNanos) / 1000000000.0 : clockTime;
    }

    public double getCpuTime(){
        double currentCpuTime = metricsThread.getTotalCpuTimeInSec();
        return Double.isNaN(cpuTime) ? currentCpuTime : cpuTime;
    }

    public double getUserTime(){
        double currentUserTime = metricsThread.getTotalUserTimeInSec();
        return Double.isNaN(userTime) ? currentUserTime : userTime;
    }

    public long getStartTime(){
        return startTime;
    }

    public boolean isTimeout(){
        return Configuration.TIMEOUT > 0 && getRunningTime() > Configuration.TIMEOUT;
    }

    public void refresh(){
        metricsThread.refreshTimeRecords();
    }

    public String buildTimingSummary(Double postProcessingClockTime, Double postProcessingCpuTime, Double postProcessingUserTime) {
        StringBuilder builder = new StringBuilder();
        builder.append("Timing summary:");
        builder.append("\nClock time: ").append(formatSeconds(getClockTime()));
        builder.append("\nCPU time: ").append(formatSeconds(getCpuTime()));
        builder.append("\nCPU user time: ").append(formatSeconds(getUserTime()));
        builder.append("\nCPU system time: ").append(formatSystemTime(getCpuTime(), getUserTime()));

        if (postProcessingClockTime != null) {
            builder.append("\nPost-processing clock time: ").append(formatSeconds(postProcessingClockTime));
        }
        if (postProcessingCpuTime != null) {
            builder.append("\nPost-processing CPU time: ").append(formatSeconds(postProcessingCpuTime));
        }
        if (postProcessingUserTime != null) {
            builder.append("\nPost-processing CPU user time: ").append(formatSeconds(postProcessingUserTime));
        }

        return builder.toString();
    }

    public double measureAverageMemory(){
        double memory = metricsThread.getAverageMemory();
        metricsThread.clearMemoryRecords();
        return memory;
    }

    public void changeTimeout(){
        metricsThread.setTimeInterval();
    }

    private String formatSeconds(double seconds) {
        if (Double.isNaN(seconds)) {
            return "unsupported";
        }
        return String.format(Locale.ROOT, "%.3f s", seconds);
    }

    private String formatSystemTime(double cpuTime, double userTime) {
        if (Double.isNaN(cpuTime) || Double.isNaN(userTime)) {
            return "unsupported";
        }

        return formatSeconds(Math.max(0.0, cpuTime - userTime));
    }

}
