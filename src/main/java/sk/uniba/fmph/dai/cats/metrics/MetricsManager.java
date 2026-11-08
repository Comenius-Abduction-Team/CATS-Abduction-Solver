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
        startTime = System.currentTimeMillis();
        metricsThread.start();
        metricsThread.awaitInitialization();
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

    public double measureAverageMemory(){
        double averageMemory = metricsThread.getAverageMemory();
        //metricsThread.clearMemoryRecords();
        return averageMemory;
    }


    public double getPeakMemory(){

        double peak = metricsThread.getMemoryPeak();
        //metricsThread.clearMemoryRecords();
        return peak;
    }

    public void clearMemory(){
        metricsThread.clearMemoryRecords();
    }

    public void changeTimeout(){
        metricsThread.setTimeInterval();
    }



}
