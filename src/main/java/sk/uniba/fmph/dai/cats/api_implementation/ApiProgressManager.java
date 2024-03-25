package sk.uniba.fmph.dai.cats.api_implementation;

import sk.uniba.fmph.dai.abduction_api.monitor.Percentage;
import sk.uniba.fmph.dai.cats.progress.ProgressManager;

public class ApiProgressManager extends ProgressManager {

    CatsAbducer Abducer;

    public ApiProgressManager(CatsAbducer Abducer){
        this.Abducer = Abducer;
    }

    @Override
    protected void processProgress() {
        int percentage = (int) Math.round(currentPercentage);
        try {
            if (Abducer.isMultithread())
                Abducer.updateProgress(new Percentage(percentage), message);
        } catch(InterruptedException ignored) {}
    }

}
