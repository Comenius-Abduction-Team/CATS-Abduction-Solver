package sk.uniba.fmph.dai.cats.algorithms.hybrid;

import org.semanticweb.owlapi.model.OWLAxiom;

import java.util.Collection;

public abstract class TreeNode {

    public Collection<OWLAxiom> label;
    public Integer depth;
}
