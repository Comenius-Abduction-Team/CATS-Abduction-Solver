package sk.uniba.fmph.dai.cats.algorithms.hst;

import sk.uniba.fmph.dai.cats.algorithms.hybrid.TreeNode;
import sk.uniba.fmph.dai.cats.common.StringFactory;

public class HstTreeNode extends TreeNode {
    public int min, index;

    @Override
    public String toString() {
        String data = (model == null) ? "{}" : StringFactory.getRepresentation(model.getNegatedData());
        return index + ". " + data + ", min: " + min;
    }

}
