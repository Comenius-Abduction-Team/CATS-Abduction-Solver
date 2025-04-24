package sk.uniba.fmph.dai.cats.api_implementation;

import sk.uniba.fmph.dai.cats.common.IPrinter;

import java.util.Arrays;

public class ApiPrinter implements IPrinter {

    private final CatsAbducer Abducer;

    public ApiPrinter(CatsAbducer Abducer){
        this.Abducer = Abducer;
    }

    @Override
    public void logInfo(String message) {
        Abducer.appendToLog(message);
    }

    @Override
    public void logError(String message, Throwable exception) {
        Abducer.appendToLog(message);
        Abducer.appendToLog(exception.getMessage());
        Abducer.appendToLog(Arrays.toString(exception.getStackTrace()));
        Abducer.setMessage(exception.getMessage());
    }

    @Override
    public void print(String message) {
        Abducer.appendToLog(message);
    }
}
