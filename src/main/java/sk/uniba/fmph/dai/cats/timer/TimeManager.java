package sk.uniba.fmph.dai.cats.timer;

import sk.uniba.fmph.dai.cats.common.Configuration;

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
        timer.start();
        startTime = System.currentTimeMillis();
    }

    public double getTime(){
        return timer.getTotalUserTimeInSec();
    }

    public long getStartTime(){
        return startTime;
    }

    public void setTimeForLevel(double time, int depth){
        levelTimes.put(depth, time);
    }

    public double getTimeForLevel(int depth){
        return levelTimes.get(depth);
    }

    public boolean levelHasTime(int depth){
        return levelTimes.containsKey(depth);
    }

    public boolean isTimeout(){
        return Configuration.TIMEOUT > 0 && getTime() > Configuration.TIMEOUT;
    }

    public void setTimeForLevelIfNotSet(double time, int depth){
        if (!levelHasTime(depth))
            setTimeForLevel(time, depth);
    }

}
