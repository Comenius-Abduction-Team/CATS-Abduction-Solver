package sk.uniba.fmph.dai.cats.algorithms.hybrid;

import sk.uniba.fmph.dai.cats.data.Explanation;

public interface NodeProcessor {

    boolean canCreateRoot();

    boolean isInvalidExplanation(Explanation explanation);

    boolean cannotAddExplanation(Explanation explanation, boolean extractModel);

}
