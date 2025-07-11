package sk.uniba.fmph.dai.cats.algorithms.hst;

import sk.uniba.fmph.dai.cats.algorithms.TreeNode;
import sk.uniba.fmph.dai.cats.common.StringFactory;

public class HstNode extends TreeNode {
    public int min, index;

    @Override
    public String toString() {
        return index + ". " + StringFactory.getRepresentation(model.getNegatedData()) + ", min: " + min;
    }

}
