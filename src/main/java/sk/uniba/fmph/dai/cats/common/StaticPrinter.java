package sk.uniba.fmph.dai.cats.common;

public class StaticPrinter {

    static private IPrinter printer;

    public static void setPrinter(IPrinter printer){
        StaticPrinter.printer = printer;
    }

    public static void logInfo(String message){
        printer.logInfo(message);
    }

    public static void logError(String message, Throwable exception){
        printer.logError(message, exception);
    }

    public static void print(String message){
        printer.print(message);
    }

    public static void debugPrint(String message){
        if (!Configuration.DEBUG_PRINT)
            return;
        print(message);
    }

}
