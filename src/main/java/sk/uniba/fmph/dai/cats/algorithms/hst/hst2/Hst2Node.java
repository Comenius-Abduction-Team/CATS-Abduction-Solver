package sk.uniba.fmph.dai.cats.algorithms.hst.hst2;

import sk.uniba.fmph.dai.cats.algorithms.hst.HstNode;

import java.util.ArrayList;
import java.util.List;

public class Hst2Node extends HstNode {

    Hst2Node parent;

    final List<Hst2Node> children = new ArrayList<>();

    boolean hasExplanationEdgeChild = false;

}
