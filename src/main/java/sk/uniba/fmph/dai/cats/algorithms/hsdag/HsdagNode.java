package sk.uniba.fmph.dai.cats.algorithms.hsdag;

import org.semanticweb.owlapi.model.OWLAxiom;
import sk.uniba.fmph.dai.cats.algorithms.TreeNode;
import sk.uniba.fmph.dai.cats.common.StringFactory;

import java.util.*;

public class HsdagNode extends TreeNode implements Comparable<HsdagNode>{

    public Set<OWLAxiom> pathSet;
    public ArrayList<HsdagNode> children;
    final int id;

    OWLAxiom labelAxiom;
    List<OWLAxiom> childrenToProcess;
    Set<OWLAxiom> positiveAxioms;

    boolean currentlyRelabeled;
    int referenceCount;

    public boolean modelWasNotReused;


    HsdagNode parent;

    public HsdagNode(int id) {
        this.id = id;
        this.currentlyRelabeled = false;
        referenceCount = 1;
        children = new ArrayList<HsdagNode>();
        childrenToProcess = new ArrayList<OWLAxiom>();
        positiveAxioms = new HashSet<>();
    }

    boolean isSubsetOf(HsdagNode other){
        if (this == other)
            return false;
        if (positiveAxioms.size() >= other.positiveAxioms.size())
            return false;
        return other.positiveAxioms.containsAll(positiveAxioms);
    }

    @Override
    public int compareTo(HsdagNode other) {
        if (this.depth != other.depth) {
            return Integer.compare(this.depth, other.depth);
        }
        return Integer.compare(positiveAxioms.size(), other.positiveAxioms.size());
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;
        HsdagNode that = (HsdagNode) other;
         return id == that.id;
    }

    @Override
    public String toString() {
        if (labelAxiom == null)
            return id + "." + StringFactory.getRepresentation(model.getNegatedData());
        return id + "." + StringFactory.getRepresentation(labelAxiom) + ":" +
                StringFactory.getRepresentation(model.getNegatedData());
    }

    public boolean HaveParent(){
        return referenceCount  > 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }


}
