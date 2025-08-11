package sk.uniba.fmph.dai.cats.metrics;

import sk.uniba.fmph.dai.cats.common.StringFactory;

import java.util.ArrayList;
import java.util.List;

public class TreeStats {

    public List<Level> levels = new ArrayList<>();

    private Level currentLevel;
    private final Level filteringLevel = new Level(-1);


//    public Map<Level, LevelStats> levels = new HashMap<>();
//
//    private Level currentLevel;

//    public LevelStats startNewLevel(int depth){
//        LevelStats newStats = new LevelStats();
//        levels.put(new Level(depth), newStats);
//        return newStats;
//    }

    public Level getCurrentLevelStats(){
        return currentLevel;
    }

    public Level getFilteringStats(){
        return filteringLevel;
    }

    public Level getLevelStats(int level){
        if (levels.size() > level){
            currentLevel = levels.get(level);
            return currentLevel;
        }
        currentLevel = new Level(level);
        levels.add(currentLevel);
        return currentLevel;
    }

    public Level getNewLevelStats(int level){
        currentLevel = new Level(level);
        levels.add(currentLevel);
        return currentLevel;
    }

    public Level getLevelStatsNoSetting(int level){
        if (levels.size() > level){
            return levels.get(level);
        }
        Level levelStats = new Level(level);
        levels.add(levelStats);
        return levelStats;
    }

    public int getTotalNodeCount(){
        int result = 0;
        for (Level level: levels){
            result += level.createdNodes;
        }
        return result;
    }

    public int getTotalPrunedCount(){
        int result = 0;
        for (Level level: levels){
            result += level.prunedEdges;
        }
        return result;
    }

    @Override
    public String toString() {
        return "TreeStats{" +
                "levels\n=" + levels +
                '}';
    }

    public static String getCsvHeader(boolean addCommas){
        return StringFactory.buildCsvRow(addCommas,
                "level",
                "processed nodes", "childless nodes", "(RCT) repeated node processing", "(RCT) deleted processed nodes",
                "created edges", "pruned edges", "explanation edges", "created nodes", "(RCT) deleted created nodes",
                "reused models", "model extractions", "stored models", "consistency checks", "QXP calls", "MXP calls",
                "(HST) largest unassigned index",
                "explanations", "filtered explanations", "final explanations",
                "average memory",
                "start time", "finish time", "duration", "first explanation time", "last explanation time",
                "message", "error", "error message", "explanations");
    }

    public String buildCsvTable(){

        StringBuilder builder = new StringBuilder();
        builder.append(getCsvHeader(false));
        builder.append('\n');

        for (Level level : levels){
            buildCsvRow(builder, level);
        }

        builder.append("f;");
        filteringLevel.buildCsvRow(builder, false);

        return builder.toString();
    }

    public void buildCsvRow(StringBuilder builder, Level level){

        builder.append(level.depth);
        builder.append(';');
        level.buildCsvRow(builder, false);
        builder.append(';');
        builder.append(StringFactory.getExplanationsRepresentation(level.explanations));
        builder.append('\n');

    }
}
