package sk.uniba.fmph.dai.cats.algorithms;

import sk.uniba.fmph.dai.cats.data.Explanation;

public interface INodeProcessor {

    boolean canCreateRoot(boolean extractModel);

    boolean shouldPruneBranch(Explanation explanation);

    int findExplanations(Explanation explanation, boolean extractModel);

    boolean shouldCloseNode(int explanationsFound);

    void postProcessExplanations();

}
