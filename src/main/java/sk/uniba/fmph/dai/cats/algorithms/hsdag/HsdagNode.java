package sk.uniba.fmph.dai.cats.algorithms.hsdag;

import org.semanticweb.owlapi.model.OWLAxiom;
import sk.uniba.fmph.dai.cats.algorithms.TreeNode;
import sk.uniba.fmph.dai.cats.common.StringFactory;

import java.util.*;

public class HsdagNode extends TreeNode implements Comparable<HsdagNode>{

    public List<HsdagNode> children;
    final int id;
    Map<HsdagNode, OWLAxiom> incomingLabels = new HashMap<>();

    public HsdagNode(int id) {
        this.id = id;
        children = new ArrayList<>();
    }

    boolean isProperSubsetOf(HsdagNode other){
        if (this == other)
            return false;
        if (model.getNegatedData().size() >= other.model.getNegatedData().size())
            return false;
        return other.model.getNegatedData().containsAll(model.getNegatedData());
    }

    @Override
    public int compareTo(HsdagNode other) {
        int depthOrder = Integer.compare(depth, other.depth);
        if (depthOrder != 0)
            return depthOrder;

        int negatedDataSizeOrder = Integer.compare(
                model.getNegatedData().size(),
                other.model.getNegatedData().size()
        );
        if (negatedDataSizeOrder != 0)
            return negatedDataSizeOrder;

        return Integer.compare(id, other.id);
    }

    @Override
    public String toString() {
        if (getAllLabelAxioms().isEmpty())
            return id + "." + StringFactory.getRepresentation(model.getNegatedData());
        return id + "." + StringFactory.getRepresentation(getAllLabelAxioms()) + ":" +
                StringFactory.getRepresentation(model.getNegatedData());
    }

    public void addParent(HsdagNode parent, OWLAxiom label){
        if (!incomingLabels.containsKey(parent)){
            incomingLabels.put(parent, label);
        }
    }

    public void removeParent(HsdagNode parent){
        OWLAxiom removed = incomingLabels.remove(parent);

        if (removed == null){
            throw new IllegalStateException(
                    "Trying to remove non-existing parent " + parent
            );
        }
    }

    public OWLAxiom getLabelFrom(HsdagNode parent){
        return incomingLabels.get(parent);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (obj == null || getClass() != obj.getClass())
            return false;

        HsdagNode other = (HsdagNode) obj;

        return id == other.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }

    public boolean hasParent(){
        return !incomingLabels.isEmpty();
    }

    private Collection<OWLAxiom> getAllLabelAxioms(){
        if (incomingLabels.isEmpty())
            return new HashSet<>();
        return incomingLabels.values();
    }

}
