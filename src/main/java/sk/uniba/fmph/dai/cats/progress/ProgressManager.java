package sk.uniba.fmph.dai.cats.progress;

import sk.uniba.fmph.dai.cats.common.Configuration;

import java.text.DecimalFormat;

public abstract class ProgressManager {

    private static final DecimalFormat formatter = new DecimalFormat("0.00");

    protected double currentPercentage = 0;
    protected String message;

    public void updateProgress(int depth, double time) {
        updateProgressAccordingToCorrectFactor(depth, time);
        processProgress();
    }

    public void updateProgress(double newPercentage, String message) {
        currentPercentage = newPercentage;
        this.message = message;
        processProgress();
    }

    protected void updateProgressAccordingToCorrectFactor(int depth, double time){

        if (Configuration.DEPTH_LIMIT > 0){
            updateProgressAccordingToDepthLimit(depth);
        }
        else if (Configuration.TIMEOUT > 0){
            updateProgressAccordingToTimeLimit(time);
            updateMessageAccordingToTimeLimit(time);
            return;
        }
        updateMessageAccordingToDepth(depth);
    }

    abstract protected void processProgress();

    private void updateProgressAccordingToDepthLimit(int depth){
        double remainingPercentage = 99 - currentPercentage;
        int maxDepth = Configuration.DEPTH_LIMIT;
        double percentageToFill = remainingPercentage / Math.pow(3, maxDepth - depth - 1);
        increaseProgress(percentageToFill);
    }

    private void updateMessageAccordingToDepth(int depth){
        message = "Finished tree depth: " + depth;
    }

    protected void updateProgressAccordingToTimeLimit(double time){
        currentPercentage = time / (double) Configuration.TIMEOUT * 99;
    }

    private void updateMessageAccordingToTimeLimit(double time){
        message = "Seconds left until time-out: " + formatter.format(Math.max((Configuration.TIMEOUT - time), 0.0));
    }

    protected void increaseProgress(double percentageToAdd){
        currentPercentage += percentageToAdd;
    }

}
