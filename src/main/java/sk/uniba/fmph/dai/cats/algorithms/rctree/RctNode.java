package sk.uniba.fmph.dai.cats.algorithms.rctree;

import org.semanticweb.owlapi.model.OWLAxiom;
import sk.uniba.fmph.dai.cats.algorithms.TreeNode;
import sk.uniba.fmph.dai.cats.common.StringFactory;
import sk.uniba.fmph.dai.cats.data.AxiomSet;
import sk.uniba.fmph.dai.cats.metrics.Level;

import java.util.ArrayList;
import java.util.List;

public class RctNode extends TreeNode implements Comparable<RctNode> {

    final int id;

    boolean closed;

    OWLAxiom labelAxiom;

    RctNode parent;

    final AxiomSet childrenToIgnore = new AxiomSet();

    final List<OWLAxiom> childrenToProcess = new ArrayList<>();
    final List<RctNode> children = new ArrayList<>();

    final List<OWLAxiom> usedLabels = new ArrayList<>();

    RctNode(int id){
        this.id = id;
    }

    boolean isProperSubsetOf(RctNode other){
        if (model.getNegatedData().size() >= other.model.getNegatedData().size())
            return false;
        return other.model.getNegatedData().containsAll(model.getNegatedData());
    }

    public void closeNode() {
        closed = true;
    }

    @Override
    public int compareTo(RctNode other) {
        int depthOrder = Integer.compare(depth, other.depth);
        if (depthOrder != 0)
            return depthOrder;

        return Integer.compare(id, other.id);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (obj == null || getClass() != obj.getClass())
            return false;

        RctNode other = (RctNode) obj;

        return id == other.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }

    @Override
    public String toString() {
        if (labelAxiom == null)
            return id + "." + StringFactory.getRepresentation(model.getNegatedData());
        return id + "." + StringFactory.getRepresentation(labelAxiom) + ":" +
                StringFactory.getRepresentation(model.getNegatedData());
    }
}
