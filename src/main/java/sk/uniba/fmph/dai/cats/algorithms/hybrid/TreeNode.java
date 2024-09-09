package sk.uniba.fmph.dai.cats.algorithms.hybrid;

import org.semanticweb.owlapi.model.OWLAxiom;
import sk.uniba.fmph.dai.cats.algorithms.ITreeNode;
import sk.uniba.fmph.dai.cats.model.Model;

import java.util.Collection;

public class TreeNode implements ITreeNode {

    public Collection<OWLAxiom> label;
    public Integer depth;
    public Model model;
}