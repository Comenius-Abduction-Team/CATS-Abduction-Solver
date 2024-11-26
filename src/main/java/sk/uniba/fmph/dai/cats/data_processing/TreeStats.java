package sk.uniba.fmph.dai.cats.data_processing;

import java.util.HashMap;
import java.util.Map;

public class TreeStats {

    public Map<Integer, LevelStats> levels = new HashMap<>();

    private int currentLevel = -1;

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

    @Override
    public String toString() {
        return "TreeStats{" +
                "levels=" + levels +
                '}';
    }
}
