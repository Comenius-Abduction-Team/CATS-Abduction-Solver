package sk.uniba.fmph.dai.cats.data_processing;

import java.util.HashMap;
import java.util.Map;

public class TreeStats {

    public Map<Integer, LevelStats> levels = new HashMap<>();

    public LevelStats getLevelStats(int level){
        if (levels.containsKey(level)){
            return levels.get(level);
        }
        LevelStats stats = new LevelStats();
        levels.put(level, stats);
        return stats;
    }

}
