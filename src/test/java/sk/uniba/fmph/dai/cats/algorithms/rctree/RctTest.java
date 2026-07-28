package sk.uniba.fmph.dai.cats.algorithms.rctree;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sk.uniba.fmph.dai.cats.algorithms.AlgorithmSolver;
import sk.uniba.fmph.dai.cats.common.Configuration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

//TODO
public class RctTest {

    private AlgorithmSolver solver;

    RctTreeBuilder builder;

    @BeforeEach
    void setUp() {
        Configuration.EVENTS = false;
        solver = mock(AlgorithmSolver.class);
        builder = new RctTreeBuilder(solver);
    }

    @Test
    void shouldReturnNodesInBreadthFirstOrder() {
        RctNode root = new RctNode(0);
        root.depth = 0;

        RctNode n1 = new RctNode(1);
        n1.depth = 1;

        RctNode n2 = new RctNode(2);
        n2.depth = 1;

        RctNode n3 = new RctNode(3);
        n3.depth = 2;

        RctNode n4 = new RctNode(4);
        n4.depth = 1;

        builder.addNodeToTree(n2);
        builder.addNodeToTree(root);
        builder.addNodeToTree(n1);
        builder.addNodeToTree(n3);
        builder.addNodeToTree(n4);


        assertSame(root, builder.getNextNodeFromTree());
        assertSame(n1, builder.getNextNodeFromTree());
        assertSame(n2, builder.getNextNodeFromTree());
        assertSame(n4, builder.getNextNodeFromTree());
        assertSame(n3, builder.getNextNodeFromTree());
    }



}
