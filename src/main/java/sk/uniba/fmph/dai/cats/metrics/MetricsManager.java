package sk.uniba.fmph.dai.cats.metrics;

import sk.uniba.fmph.dai.cats.common.Configuration;

public class MetricsManager {

    final MetricsThread metricsThread;
    private long startTime;
    private double endTime;

    public MetricsManager(MetricsThread metricsThread){
        this.metricsThread = metricsThread;

    }

    public void setStartTime(){
        metricsThread.start();
        startTime = System.currentTimeMillis();
    }

    public void setEndTime(){
        endTime = metricsThread.getTotalUserTimeInSec();
        metricsThread.terminate();
    }

    public double getEndTime(){
        return endTime;
    }

    public double getRunningTime(){
        return metricsThread.getTotalUserTimeInSec();
    }

    public long getStartTime(){
        return startTime;
    }

    public boolean isTimeout(){
        return Configuration.TIMEOUT > 0 && getRunningTime() > Configuration.TIMEOUT;
    }

    public double measureAverageMemory(){
        double memory = metricsThread.getAverageMemory();
        metricsThread.clearMemoryRecords();
        return memory;
    }

}
