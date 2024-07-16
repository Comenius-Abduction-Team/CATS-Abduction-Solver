package sk.uniba.fmph.dai.cats.algorithms.hybrid;

import sk.uniba.fmph.dai.cats.common.IPrinter;
import sk.uniba.fmph.dai.cats.progress.IProgressManager;
import sk.uniba.fmph.dai.cats.timer.ThreadTimer;

public class MxpSolver extends HybridSolver {
    public MxpSolver(ThreadTimer timer, IExplanationManager explanationManager, IProgressManager progressManager, IPrinter printer) {
        super(timer, explanationManager, progressManager, printer);
    }

    @Override
    protected void startSolving() {
        runMxpInRoot();
    }
}
