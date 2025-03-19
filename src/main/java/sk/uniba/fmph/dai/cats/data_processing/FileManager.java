package sk.uniba.fmph.dai.cats.data_processing;

import sk.uniba.fmph.dai.cats.common.Configuration;
import sk.uniba.fmph.dai.cats.common.DLSyntax;
import sk.uniba.fmph.dai.cats.common.LogTypes;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class FileManager {

    public static final Map<LogTypes, String> LOG_PREFIXES;

    static {
        Map<LogTypes, String> map = new HashMap<>();
        map.put(LogTypes.INFO, "info");
        map.put(LogTypes.FINAL, "final");
        map.put(LogTypes.LEVEL, "level");
        map.put(LogTypes.PARTIAL, "partial_level");
        map.put(LogTypes.TIMES, "explanation_times");
        map.put(LogTypes.ERROR, "error");
        LOG_PREFIXES = Collections.unmodifiableMap(map);
    }

    public static final String INFO_LOG__PREFIX = "info";
    public static final String ERROR_LOG__PREFIX = "error";
    public static final String FINAL_LOG__PREFIX = "final";
    public static final String LEVEL_LOG__PREFIX = "level";
    public static final String EXP_TIMES_LOG__PREFIX = "explanation_times";
    public static final String PARTIAL_LOG__PREFIX = "partial_explanations";
    public static final String PARTIAL_LEVEL_LOG__PREFIX = "partial_level_explanations";
    public static final String LOG_FILE__POSTFIX = ".log";
    private static String FILE_DIRECTORY = "";

    static void appendToFile(LogTypes type, String time, String log) {
        if (!Configuration.LOGGING)
            return;
        FILE_DIRECTORY = "logs" + File.separator + Configuration.ALGORITHM;

        String fileName = LOG_PREFIXES.get(type);

        createFileIfNotExists(fileName, time);
        try {
            String file_path = getFilePath(fileName, time);
            Files.write(Paths.get(file_path), log.getBytes(), StandardOpenOption.APPEND);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    private static void createFileIfNotExists(String fileName, String time) {
        File file = new File(getFilePath(fileName, time));
        try {
            file.createNewFile();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    private static String getFilePath(String fileName, String time) {
        String directoryPath;

        if (Configuration.OUTPUT_PATH.isEmpty()) {
            directoryPath = getDefaultOutputPath();
        }
        else {
            directoryPath = FILE_DIRECTORY
                    .concat(File.separator)
                    .concat(Configuration.OUTPUT_PATH);
        }
        File directory = new File(directoryPath);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        return directoryPath.concat(File.separator).concat(time + "_").concat(Configuration.INPUT_FILE_NAME + "_").concat(fileName).concat(LOG_FILE__POSTFIX);
    }

    private static String getDefaultOutputPath() {
        String directoryPath;
        String[] inputFile;

        inputFile = Configuration.INPUT_ONT_FILE.split("[/\\\\]");
        //if (inputFile.length <= 1) inputFile = Configuration.INPUT_ONT_FILE.split(File.separator);

        String input = inputFile[inputFile.length - 1];
        String ontologyName = input;
        String[] inputFileParts = input.split("\\.");
        if (inputFileParts.length > 0) {
            ontologyName = inputFileParts[0];
        }

        directoryPath = FILE_DIRECTORY
                .concat(File.separator)
                .concat(ontologyName)
                .concat(File.separator)
                .concat(Configuration.INPUT_FILE_NAME);
        return directoryPath;
    }

    private static String observationToFilePath(){
        String[] observation = Configuration.OBSERVATION.substring(0, Configuration.OBSERVATION.length()-1).split("\\"+ DLSyntax.LEFT_PARENTHESES);
        for (int i = 0; i < observation.length; i++){
            if (observation[i].contains(DLSyntax.DELIMITER_ONTOLOGY)){
                observation[i] = observation[i].substring(observation[i].indexOf(DLSyntax.DELIMITER_ONTOLOGY)+1);
            }
            else {
                observation[i] = observation[i].substring(observation[i].indexOf(DLSyntax.DELIMITER_ASSERTION)+1);
            }
        }
        return String.join("_", observation);
    }
}


