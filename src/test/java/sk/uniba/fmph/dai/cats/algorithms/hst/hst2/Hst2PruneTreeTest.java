package sk.uniba.fmph.dai.cats.algorithms.hst.hst2;

import org.junit.jupiter.api.Test;
import sk.uniba.fmph.dai.cats.algorithms.AlgorithmSolver;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class Hst2PruneTreeTest {

    private final Hst2TreeBuilder builder = new Hst2TreeBuilder(mock(AlgorithmSolver.class));

    private static Hst2Node addChild(Hst2Node parent) {
        Hst2Node child = new Hst2Node();
        child.parent = parent;
        parent.children.add(child);
        return child;
    }

    private void prune(Hst2Node node) {
        try {
            Method method = Hst2TreeBuilder.class.getDeclaredMethod("pruneTree", Hst2Node.class);
            method.setAccessible(true);
            method.invoke(builder, node);

        } catch (InvocationTargetException e) {
            throw new AssertionError("pruneTree threw an exception", e.getCause());
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("Could not invoke pruneTree", e);
        }
    }

    @Test
    void removesClosedLeafFromParent() {
        Hst2Node root = new Hst2Node();
        Hst2Node leaf = addChild(root);

        prune(leaf);

        assertTrue(root.children.isEmpty(), "The closed leaf must be detached");
    }

    @Test
    void keepsParentWithAnotherChild() {
        Hst2Node root = new Hst2Node();
        Hst2Node parent = addChild(root);
        Hst2Node closedLeaf = addChild(parent);
        Hst2Node survivingChild = addChild(parent);

        prune(closedLeaf);

        assertEquals(1, parent.children.size());
        assertSame(survivingChild, parent.children.get(0));
        assertEquals(1, root.children.size());
        assertSame(parent, root.children.get(0), "Parent still has a live subtree");
    }

    @Test
    void removesEmptyAncestorsUntilRoot() {
        Hst2Node root = new Hst2Node();
        Hst2Node parent = addChild(root);
        Hst2Node middle = addChild(parent);
        Hst2Node leaf = addChild(middle);

        prune(leaf);

        assertTrue(middle.children.isEmpty(), "Leaf should be detached");
        assertTrue(parent.children.isEmpty(), "Empty middle node should be detached");
        assertTrue(root.children.isEmpty(), "Empty parent should be detached");
    }

    @Test
    void keepsAncestorThatHasExplanationEdge() {
        Hst2Node root = new Hst2Node();
        Hst2Node parent = addChild(root);
        Hst2Node closedLeaf = addChild(parent);
        parent.hasExplanationEdgeChild = true;

        prune(closedLeaf);

        assertTrue(parent.children.isEmpty(), "The closed leaf should be removed");
        assertEquals(1, root.children.size());
        assertSame(parent, root.children.get(0),
                "The ancestor must survive because it has an explanation edge");
    }

    @Test
    void doesNotPruneBeforeAllChildIndicesAreProcessed() {
        Hst2Node root = new Hst2Node();
        Hst2Node parent = addChild(root);
        parent.index = 5;
        builder.parentNode = parent;
        builder.nextChildIndex = 3;

        assertFalse(builder.noChildrenLeft(), "Child indices 3 and 4 are still pending");
        assertEquals(1, root.children.size(),
                "The parent must not be detached during child generation");
        assertSame(parent, root.children.get(0));

        // Simulate reaching the end of the child-index range without
        // creating any surviving child nodes.
        builder.nextChildIndex = parent.index;

        assertTrue(builder.noChildrenLeft());
        assertTrue(root.children.isEmpty(),
                "The childless parent may be pruned once iteration is finished");
    }
}
