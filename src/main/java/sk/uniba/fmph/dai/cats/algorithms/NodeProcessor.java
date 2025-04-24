package sk.uniba.fmph.dai.cats.algorithms;

import sk.uniba.fmph.dai.cats.data.Explanation;

public interface NodeProcessor {

    boolean canCreateRoot(boolean extractModel);

    boolean isInvalidExplanation(Explanation explanation);

    boolean findExplanations(Explanation explanation, boolean extractModel);

    void postProcessExplanations();

}
