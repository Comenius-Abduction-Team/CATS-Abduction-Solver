package sk.uniba.fmph.dai.cats.application;

public class Application {

    public static void finish(ExitCode exitCode) {
        System.exit(exitCode.ordinal());
    }
}
