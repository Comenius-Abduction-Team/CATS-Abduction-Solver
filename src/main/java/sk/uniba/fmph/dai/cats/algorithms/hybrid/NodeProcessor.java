package sk.uniba.fmph.dai.cats.algorithms.hybrid;

import sk.uniba.fmph.dai.cats.data.Explanation;

interface NodeProcessor {

    boolean canCreateRoot();

    boolean isInvalidExplanation(Explanation explanation);

    boolean cannotAddExplanation(Explanation explanation);

}
