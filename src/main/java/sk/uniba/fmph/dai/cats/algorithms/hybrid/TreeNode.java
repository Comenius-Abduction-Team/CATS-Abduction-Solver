package sk.uniba.fmph.dai.cats.algorithms.hybrid;

import org.semanticweb.owlapi.model.OWLAxiom;
import sk.uniba.fmph.dai.cats.model.Model;

import java.util.ArrayList;
import java.util.List;

public class TreeNode {

    public List<OWLAxiom> label = new ArrayList<>();
    public Integer depth = 0;
    public Model model;
    public boolean closed;

    public void closeNode() {
        closed = true;
    }
}