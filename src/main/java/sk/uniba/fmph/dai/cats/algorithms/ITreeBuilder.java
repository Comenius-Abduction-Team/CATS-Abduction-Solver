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
    boolean pruneNode(TreeNode node, Explanation explanation);

    boolean shouldExtractModel();

    /**
     * Create root tree node.
     *
     * @return the tree node
     */
    TreeNode createRoot();

    /**
     * Create child node tree node.
     *
     * @param parent parent of the node
     * @param label  label of the path from the root to the created node
     * @return the tree node
     */
    TreeNode createChildNode(TreeNode parent, Explanation label);

    /**
     * Add node to tree.
     *
     * @param node the node
     */
    void addNodeToTree(TreeNode node);

    /**
     * Is tree closed boolean.
     *
     * @return the boolean
     */
    boolean isTreeClosed();

    /**
     * Gets next node from tree.
     *
     * @return the next node from tree
     */
    TreeNode getNextNodeFromTree();

    /**
     * Initialise iterating over the child edges starting in the current node.
     *
     * @param parent the parent node
     * @return whether there are child edges that can be iterated
     */
    boolean startIteratingNodeChildren(TreeNode parent);

    /**
     * Returns true if all child edges of the current node have already been iterated.
     *
     * @return the boolean
     */
    boolean noChildrenLeft();

    /**
     * Get next edge starting in the current node.
     *
     * @return the axiom labeling the edge
     */
    OWLAxiom getNextChild();

    void labelNodeWithModel(TreeNode node);

}
