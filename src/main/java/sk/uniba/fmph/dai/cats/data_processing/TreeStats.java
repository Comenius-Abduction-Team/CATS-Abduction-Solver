package sk.uniba.fmph.dai.cats.data_processing;

import java.util.HashMap;
import java.util.Map;

public class TreeStats {

    public Map<Integer, LevelStats> levels = new HashMap<>();

    private int currentLevel = -1;

    public double filteringStart, filteringEnd;

//    public Map<Level, LevelStats> levels = new HashMap<>();
//
//    private Level currentLevel;

//    public LevelStats startNewLevel(int depth){
//        LevelStats newStats = new LevelStats();
//        levels.put(new Level(depth), newStats);
//        return newStats;
//    }

    public LevelStats getCurrentLevelStats(){
        return levels.get(currentLevel);
    }

    public LevelStats getLevelStats(int level){
        currentLevel = level;
        if (levels.containsKey(level)){
            return levels.get(level);
        }
        LevelStats levelStats = new LevelStats();
        levels.put(level, levelStats);
        return levelStats;
    }

    public LevelStats getLevelStatsNoSetting(int level){
        if (levels.containsKey(level)){
            return levels.get(level);
        }
        LevelStats levelStats = new LevelStats();
        levels.put(level, levelStats);
        return levelStats;
    }

    public int getTotalNodeCount(){
        int result = 0;
        for (LevelStats level: levels.values()){
            result += level.created_nodes;
        }
        return result;
    }

    public int getTotalPrunedCount(){
        int result = 0;
        for (LevelStats level: levels.values()){
            result += level.pruned_edges;
        }
        return result;
    }

    @Override
    public String toString() {
        return "TreeStats{" +
                "levels\n=" + levels +
                ", filtering_start=" + filteringStart +
                ", filtering_end=" + filteringEnd +
                '}';
    }
}
