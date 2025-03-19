package sk.uniba.fmph.dai.cats.data_processing;

import sk.uniba.fmph.dai.cats.common.StringFactory;
import sk.uniba.fmph.dai.cats.data.Explanation;

import java.util.HashMap;
import java.util.List;
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

    public static String getCsvHeader(boolean addCommas){
        return StringFactory.buildCsvRow(addCommas,
                "level",
                "processed nodes", "deleted unprocessed nodes", "deleted processed nodes",
                "created edges", "pruned edges", "explanation edges", "created nodes",
                "reused models", "model extractions", "consistency checks",
                "explanations", "filtered explanations", "final explanations",
                "start time", "finish time", "duration", "first explanation time", "last explanation time",
                "explanations");
    }

    public String buildCsvTable(Map<Integer, List<Explanation>> explanationsByLevel){

        StringBuilder builder = new StringBuilder();
        builder.append(getCsvHeader(false));
        builder.append('\n');

        for (int level : levels.keySet()){
            buildCsvRow(builder, level, explanationsByLevel.get(level));
        }

        LevelStats filteringStats = new LevelStats();
        filteringStats.start = filteringStart;
        filteringStats.finish = filteringEnd;

        builder.append("f;");
        filteringStats.buildCsvRow(builder, false);

        return builder.toString();
    }

    public void buildCsvRow(StringBuilder builder, int level, List<Explanation> explanations){

        builder.append(level);
        builder.append(';');
        levels.get(level).buildCsvRow(builder, false);
        builder.append(';');
        builder.append(StringFactory.getExplanationsRepresentation(explanations));
        builder.append('\n');

    }

    public String buildCsvTablePartialRow(Map<Integer, List<Explanation>> explanationsByLevel){

        StringBuilder builder = new StringBuilder();
        builder.append(getCsvHeader(false));
        builder.append('\n');

        for (int level : levels.keySet()){
            buildCsvRow(builder, level, explanationsByLevel.get(level));
        }

        LevelStats filteringStats = new LevelStats();
        filteringStats.start = filteringStart;
        filteringStats.finish = filteringEnd;

        builder.append("f;");
        filteringStats.buildCsvRow(builder, false);

        return builder.toString();
    }
}
