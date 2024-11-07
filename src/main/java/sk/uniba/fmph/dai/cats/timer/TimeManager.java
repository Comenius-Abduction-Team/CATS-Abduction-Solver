package sk.uniba.fmph.dai.cats.timer;

import sk.uniba.fmph.dai.cats.common.Configuration;

import java.util.HashMap;
import java.util.Map;

public class TimeManager {

    final ThreadTimer timer;
    private long startTime;
    private double endTime;
    Map<Integer, Double> levelTimes = new HashMap<>();

    public TimeManager(ThreadTimer timer){
        this.timer = timer;

    }

    public void setStartTime(){
        timer.start();
        startTime = System.currentTimeMillis();
    }

    public void setEndTime(){
        endTime = timer.getTotalUserTimeInSec();
    }

    public double getEndTime(){
        return endTime;
    }

    public double getCurrentTime(){
        return timer.getTotalUserTimeInSec();
    }

    public long getStartTime(){
        return startTime;
    }

    public void setTimeForLevel(double time, int level){
        levelTimes.put(level, time);
    }

    public double getTimeForLevel(int level){
        //System.out.println(level + ": " + levelTimes.get(level));
        return levelTimes.get(level);
    }

    public boolean levelHasTime(int depth){
        return levelTimes.containsKey(depth);
    }

    public boolean isTimeout(){
        return Configuration.TIMEOUT > 0 && getCurrentTime() > Configuration.TIMEOUT;
    }

    public void setTimeForLevelIfNotSet(double time, int depth){
        if (!levelHasTime(depth)) {
            setTimeForLevel(time, depth);
           //System.out.println(depth + ": " + time);
        }
    }

}
