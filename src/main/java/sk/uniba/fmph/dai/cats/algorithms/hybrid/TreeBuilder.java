package sk.uniba.fmph.dai.cats.algorithms.hybrid;

import org.semanticweb.owlapi.model.OWLAxiom;
import sk.uniba.fmph.dai.cats.data.Explanation;

/**
 * The interface Tree builder.
 */
public interface TreeBuilder {

    /**
     * Create abducibles abducible axioms.
     *
     * @param abducibles the abducibles
     * @return the abducible axioms
     */
    IAbducibleAxioms createAbducibles(TransformedAbducibles abducibles);

    /**
     * Returns true if the given path (from the root to a node),
     * extended by the given axiom (that would label a child edge starting in the node),
     * would be invalid for some reason.
     *
     * @param node the path
     * @return the boolean
     */
    boolean hasIncorrectPath(TreeNode node);

    /**
     * Prune tree.
     *
     * @param node        the node
     * @param explanation the explanation
     */
    boolean pruneNode(TreeNode node, Explanation explanation);

    boolean closeExplanation(Explanation explanation);

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
     * @param edge axiom labeling the edge from the parent to the created node
     * @return the tree node
     */
    TreeNode createChildNode(TreeNode parent, OWLAxiom edge);

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

    void assignModel(TreeNode node);

}
