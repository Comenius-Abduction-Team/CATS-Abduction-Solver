package sk.uniba.fmph.dai.cats.data_processing;

import sk.uniba.fmph.dai.cats.common.Configuration;
import sk.uniba.fmph.dai.cats.common.DLSyntax;

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
        map.put(LogTypes.PARTIAL, "partial-level");
        map.put(LogTypes.TIMES, "explanation-times");
        map.put(LogTypes.ERROR, "error");
        LOG_PREFIXES = Collections.unmodifiableMap(map);
    }

    public static final String LOG_FILE__POSTFIX = ".log";
    private static String FILE_DIRECTORY = "";

    static void appendToFile(String fileName, String log) {
        if (!Configuration.LOGGING)
            return;

        createFileIfNotExists(fileName);
        try {
            Files.write(Paths.get(fileName), log.getBytes(), StandardOpenOption.APPEND);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    static void appendToFile(LogTypes type, String time, String log) {
        if (!Configuration.LOGGING)
            return;

        String fileName = createLogFileName(type, time, true);

        createFileIfNotExists(fileName);
        try {
            Files.write(Paths.get(fileName), log.getBytes(), StandardOpenOption.APPEND);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    static boolean checkIfFileExists(String fileName){

        if (fileName.equals(""))
            return false;

        File file = new File(fileName);

        return file.exists();
    }

    static void deleteFile(String fileName){
        new File(fileName).delete();
    }

    /**
     * Builds a full relative path to a log of the given type, including the file name.
     * {@param createDirectory} forces the method to also create the required hierarchy of directories.
     * If set to false and the directories don't already exist, the method does not create them and instead returns
     * an empty string.
     *
     * @param  type the type of log file
     * @param  time time when the solver run started
     * @param  createDirectory whether required directories should be created
     */
    static String createLogFileName(LogTypes type, String time, boolean createDirectory){
        if (FILE_DIRECTORY.equals(""))
            FILE_DIRECTORY = "logs" + File.separator + Configuration.ALGORITHM;

        String directoryPath = getDirectoryPath();
        File directory = new File(directoryPath);

        if (!directory.exists()) {
            if (createDirectory)
                directory.mkdirs();
            else
                return "";
        }

        return directoryPath.concat(File.separator).concat(time + "_")
                .concat(Configuration.INPUT_FILE_NAME + "_")
                .concat(LOG_PREFIXES.get(type))
                .concat(LOG_FILE__POSTFIX);
    }

    private static String getDirectoryPath(){
        if (Configuration.OUTPUT_PATH.isEmpty()) {
            return getDefaultOutputPath();
        }
        else {
            return FILE_DIRECTORY
                    .concat(File.separator)
                    .concat(Configuration.OUTPUT_PATH);
        }
    }

    private static void createFileIfNotExists(String fileName) {
        File file = new File(fileName);
        if (file.exists())
            return;
        try {
            file.createNewFile();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
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


