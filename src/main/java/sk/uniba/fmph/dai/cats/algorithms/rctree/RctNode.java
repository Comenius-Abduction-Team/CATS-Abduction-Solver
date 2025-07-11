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

    Level level;

    RctNode(int id){
        this.id = id;
    }

    boolean isSubsetOf(RctNode other){
        if (this == other)
            return false;
        if (model.getNegatedData().size() >= other.model.getNegatedData().size())
            return false;
        return other.model.getNegatedData().containsAll(model.getNegatedData());
    }

    public void closeNode() {
        closed = true;
    }

    @Override
    public int compareTo(RctNode other) {
        //return depth.compareTo(other.depth);
        return Integer.compare(id, other.id);
    }

    @Override
    public String toString() {
        if (labelAxiom == null)
            return id + "." + StringFactory.getRepresentation(model.getNegatedData());
        return id + "." + StringFactory.getRepresentation(labelAxiom) + ":" +
                StringFactory.getRepresentation(model.getNegatedData());
    }
}
