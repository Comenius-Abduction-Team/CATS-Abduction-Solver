package sk.uniba.fmph.dai.cats.algorithms.hybrid;

import org.semanticweb.owlapi.model.OWLAxiom;
import sk.uniba.fmph.dai.cats.common.StringFactory;
import sk.uniba.fmph.dai.cats.model.Model;

import java.util.ArrayList;
import java.util.List;

public class TreeNode {

    public List<OWLAxiom> path = new ArrayList<>();
    public OWLAxiom labelAxiom;
    public Integer depth = 0;
    public Model model;
    public boolean closed;

    public void closeNode() {
        closed = true;
    }

    public void setPath(List<OWLAxiom> parentPath, OWLAxiom edge){
        path.addAll(parentPath);
        path.add(edge);
        labelAxiom = edge;
    }

    @Override
    public String toString() {
        String data = (model == null) ? "{}" : StringFactory.getRepresentation(model.getNegatedData());
        return StringFactory.getRepresentation(labelAxiom) + ". " + data;
    }
}