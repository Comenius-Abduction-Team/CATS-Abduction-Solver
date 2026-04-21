package sk.uniba.fmph.dai.cats.algorithms;

import org.semanticweb.owlapi.model.OWLAxiom;
import sk.uniba.fmph.dai.cats.data.Explanation;

/**
 * The interface Tree builder.
 */
public interface ITreeBuilder {

    /**
     * Create abducibles abducible axioms.
     *
     * @param abducibles the abducibles
     * @return the abducible axioms
     */
    IAbducibleAxioms createAbducibles(TransformedAbducibles abducibles);

    /**
     * Prune tree.
     *
     * @param node        the node
     * @param explanation the explanation
     */
    boolean shouldPruneChildBranch(TreeNode node, Explanation explanation);

    boolean shouldExtractModel();

    /**
     * Creates the root tree node.
     *
     * @return the root
     */
    TreeNode createRoot();

    /**
     * Creates a new node as a child of the given parent node.
     *
     * @param parent the parent node
     * @param label  label of the path from the tree's root to the created node
     * @return the new node
     */
    TreeNode createChildNode(TreeNode parent, Explanation label);

    /**
     * Adds the given node to the tree. The node may already be a part of the tree in the topological sense
     * (i.e., it may already be attached to a parent node), this however queues the node to be processed.
     *
     * @param node the node
     */
    void addNodeToTree(TreeNode node);

    /**
     * Returns true if there are no more nodes in the tree left to process.
     *
     * @return the boolean
     */
    boolean isTreeClosed();

    /**
     * Gets the next node from the tree that should be processed.
     *
     * @return the next node from tree
     */
    TreeNode getNextNodeFromTree();

    /**
     * Initializes iterating over child edges that stem from the current node.
     *
     * @param parent the parent node
     * @return whether there are child edges that can be iterated
     */
    boolean startIteratingNodeChildren(TreeNode parent);

    /**
     * Returns true if there are no child edges stemming from the current node left to iterate over.
     *
     * @return the boolean
     */
    boolean noChildrenLeft();

    /**
     * Gets the next edge starting in the current node.
     *
     * @return the axiom labeling the edge
     */
    OWLAxiom getNextChild();

    /**
     * When overriden, performs actions that are required when the end of the tree's level is reached.
     * This is currently used only by the MHS tree builder, which needs to forget the paths that were used in the level.
     * The other algorithms don't store this information and thus don't need to override this method.
     */
    default void resetLevel(){}

}
