package sk.uniba.fmph.dai.cats.algorithms;

import sk.uniba.fmph.dai.cats.data.Explanation;

public interface INodeProcessor {

    /**
     * Checks whether the root node can be created.
     *
     * Implementations performs initialization required before root creation,
     * such as consistency checking, model extraction, or initial QXP/MXP
     * computation.
     * This method should be called exactly once (currently before TreeBuilder.createRoot()).
     */
    boolean canCreateRoot(boolean extractModel);

    /**
     * Determines whether the current child branch should be pruned.
     *
     * If this method returns true, the implementation is responsible
     * for publishing the event that accounts for the pruned edge.
     */
    boolean shouldPruneBranch(Explanation explanation);

    int findExplanations(Explanation explanation, boolean extractModel);

    boolean shouldCloseNode(int explanationsFound);

    void postProcessExplanations();

    void storeAbduciblesIfNeeded(IAbducibleAxioms abducibles);

}
