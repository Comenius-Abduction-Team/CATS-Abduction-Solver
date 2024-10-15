package sk.uniba.fmph.dai.cats.algorithms.hybrid;

import org.semanticweb.owlapi.model.OWLAxiom;
import sk.uniba.fmph.dai.cats.algorithms.ITreeNode;
import sk.uniba.fmph.dai.cats.model.Model;

import java.util.ArrayList;
import java.util.List;

public class TreeNode implements ITreeNode {

    public List<OWLAxiom> label = new ArrayList<>();
    public Integer depth = 0;
    public Model model;
    public boolean closed;

    @Override
    public void closeNode() {
        closed = true;
    }
}