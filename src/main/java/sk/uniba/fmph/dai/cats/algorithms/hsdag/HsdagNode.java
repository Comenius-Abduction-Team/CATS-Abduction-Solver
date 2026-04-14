package sk.uniba.fmph.dai.cats.algorithms.hsdag;

import org.semanticweb.owlapi.model.OWLAxiom;
import sk.uniba.fmph.dai.cats.algorithms.TreeNode;
import sk.uniba.fmph.dai.cats.common.StringFactory;

import java.util.*;

public class HsdagNode extends TreeNode implements Comparable<HsdagNode>{

    public List<HsdagNode> children;
    final int id;

    OWLAxiom labelAxiom;

    // number of parents this node currently has
    int referenceCount;

    //public boolean modelWasNotReused;

    HsdagNode parent;

    public HsdagNode(int id) {
        this.id = id;
        referenceCount = 1;
        children = new ArrayList<>();
    }

    boolean isSubsetOf(HsdagNode other){
        if (this == other)
            return false;
        if (model.getNegatedData().size() >= other.model.getNegatedData().size())
            return false;
        return other.model.getNegatedData().containsAll(model.getNegatedData());
    }

    @Override
    public int compareTo(HsdagNode other) {
        if (!Objects.equals(this.depth, other.depth)) {
            return Integer.compare(this.depth, other.depth);
        }
        return Integer.compare(model.getNegatedData().size(), other.model.getNegatedData().size());
    }

    @Override
    public String toString() {
        if (labelAxiom == null)
            return id + "." + StringFactory.getRepresentation(model.getNegatedData());
        return id + "." + StringFactory.getRepresentation(labelAxiom) + ":" +
                StringFactory.getRepresentation(model.getNegatedData());
    }

    public boolean hasParent(){
        return referenceCount  > 0;
    }

}
