package sk.uniba.fmph.dai.cats.algorithms;

import org.semanticweb.owlapi.model.OWLAxiom;
import sk.uniba.fmph.dai.cats.common.StringFactory;
import sk.uniba.fmph.dai.cats.metrics.Level;
import sk.uniba.fmph.dai.cats.model.Model;

import java.util.ArrayList;
import java.util.List;

public class TreeNode {

    public static final int DEFAULT_DEPTH = 1;

    public List<OWLAxiom> path = new ArrayList<>();
    public Integer depth = DEFAULT_DEPTH;
    public Model model;
    public boolean processed;

    public Level assignedLevel;

    @Override
    public String toString() {
        return StringFactory.getRepresentation(model.getNegatedData());
    }

}