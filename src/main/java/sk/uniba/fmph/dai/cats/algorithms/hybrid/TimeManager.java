package sk.uniba.fmph.dai.cats.algorithms.hybrid;

import sk.uniba.fmph.dai.cats.common.Configuration;
import sk.uniba.fmph.dai.cats.timer.ThreadTimer;

import java.util.HashMap;
import java.util.Map;

public class TimeManager {

    final ThreadTimer timer;
    private long startTime;
    Map<Integer, Double> levelTimes = new HashMap<>();

    public TimeManager(ThreadTimer timer){
        this.timer = timer;

    }

    public void setStartTime(){
        startTime = System.currentTimeMillis();
    }

    double getTime(){
        return timer.getTotalUserTimeInSec();
    }

    long getStartTime(){
        return startTime;
    }

    void setTimeForLevel(double time, int depth){
        levelTimes.put(depth, time);
    }

    double getTimeForLevel(int depth){
        return levelTimes.get(depth);
    }

    boolean levelHasTime(int depth){
        return levelTimes.containsKey(depth);
    }

    boolean isTimeout(){
        return Configuration.TIMEOUT > 0 && getTime() > Configuration.TIMEOUT;
    }

    void setTimeForLevelIfNotSet(double time, int depth){
        if (!levelHasTime(depth))
            setTimeForLevel(time, depth);
    }

}
