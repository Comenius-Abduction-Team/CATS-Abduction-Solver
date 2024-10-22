package sk.uniba.fmph.dai.cats.algorithms.rctree;

import org.semanticweb.owlapi.model.OWLAxiom;
import sk.uniba.fmph.dai.cats.algorithms.hybrid.TreeNode;
import sk.uniba.fmph.dai.cats.common.StringFactory;
import sk.uniba.fmph.dai.cats.data.AxiomSet;

import java.util.ArrayList;
import java.util.List;

public class RcTreeNode extends TreeNode implements Comparable<RcTreeNode> {

    final int index;

    OWLAxiom labelAxiom;

    RcTreeNode parent;

    final AxiomSet childrenToIgnore = new AxiomSet();

    final List<OWLAxiom> childrenToProcess = new ArrayList<>();
    final List<RcTreeNode> children = new ArrayList<>();

    final List<OWLAxiom> usedLabels = new ArrayList<>();

    RcTreeNode(int index){
        this.index = index;
    }

    boolean isSubsetOf(RcTreeNode other){
        if (this == other)
            return false;
        if (model == other.model)
            return false;
        return other.model.getNegatedData().containsAll(model.getNegatedData());
    }

    @Override
    public int compareTo(RcTreeNode o) {
        //return depth.compareTo(o.depth);
        return Integer.compare(index, o.index);
    }

    @Override
    public String toString() {
        if (labelAxiom == null)
            return index + "." + StringFactory.getRepresentation(model.getNegatedData());
        return index + "." + StringFactory.getRepresentation(labelAxiom) + ":" +
                StringFactory.getRepresentation(model.getNegatedData());
    }
}
