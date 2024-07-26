package sk.uniba.fmph.dai.cats.algorithms.hybrid;

import org.semanticweb.owlapi.model.OWLAxiom;

import java.util.Set;

public class ModelNode extends TreeNode {

    public Set<OWLAxiom> data;
    public boolean modelIsValid = true;
    public int min, index, parentIndex = 0;

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ModelNode) {
            ModelNode node = (ModelNode) obj;
            return data.containsAll(node.data) && node.data.containsAll(data);
        }
        return false;
    }

}
