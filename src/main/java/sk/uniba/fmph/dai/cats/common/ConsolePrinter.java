package sk.uniba.fmph.dai.cats.common;

public class ConsolePrinter implements IPrinter {

    @Override
    public void logInfo(String message) {
        print(message);
    }

    @Override
    public void logError(String message, Throwable exception) {
        if (exception == null)
            System.err.println(message);
        else
            System.err.println(message + " " + exception);
    }

    @Override
    public void print(String message) {
        System.out.println(message);
    }
}
