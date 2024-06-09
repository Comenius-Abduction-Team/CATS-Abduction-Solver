package sk.uniba.fmph.dai.cats.algorithms.hybrid;

import sk.uniba.fmph.dai.cats.common.IPrinter;
import sk.uniba.fmph.dai.cats.progress.IProgressManager;
import sk.uniba.fmph.dai.cats.timer.ThreadTimes;

public class MxpSolver extends HybridSolver {
    public MxpSolver(ThreadTimes threadTimes, IExplanationManager explanationManager, IProgressManager progressManager, IPrinter printer) {
        super(threadTimes, explanationManager, progressManager, printer);
    }

    @Override
    protected void startSolving() {
        runMxpInRoot();
    }
}
