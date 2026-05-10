package sk.uniba.fmph.dai.cats.algorithms.marco;
import org.semanticweb.owlapi.model.OWLAxiom;
import sk.uniba.fmph.dai.cats.algorithms.*;
import sk.uniba.fmph.dai.cats.algorithms.hst.HstNode;
import sk.uniba.fmph.dai.cats.algorithms.hst.INumberedAbducibles;
import sk.uniba.fmph.dai.cats.algorithms.hst.MapArrayNumberedAxioms;
import sk.uniba.fmph.dai.cats.data.Explanation;


import java.util.*;

import org.semanticweb.owlapi.model.OWLAxiom;

import sk.uniba.fmph.dai.cats.algorithms.*;
import sk.uniba.fmph.dai.cats.data.Explanation;
import sk.uniba.fmph.dai.cats.reasoner.Loader;

import java.util.*;

public class MarcoTreeBuilder implements ITreeBuilder {

    final AlgorithmSolver solver;

    final Queue<TreeNode> queue = new ArrayDeque<>();

    TreeNode parentNode;

    List<OWLAxiom> iteratedChildren;
    final MarcoNodeProcessor nodeProcessor;


    private MapArrayNumberedAxioms numberedAbducibles;

    public MarcoTreeBuilder(AlgorithmSolver solver, MarcoNodeProcessor nodeProcessor){
        this.solver = solver;
        this.nodeProcessor = nodeProcessor;
    }

    @Override
    public IAbducibleAxioms createAbducibles(TransformedAbducibles abducibles) {

        numberedAbducibles = new MapArrayNumberedAxioms(abducibles);

        int index = 1;
        for (OWLAxiom ax : abducibles.abducibleAxioms) {
            numberedAbducibles.assignIndex(ax, index++);
        }

        return numberedAbducibles;
    }


    @Override
    public boolean shouldPruneChildBranch(TreeNode node, Explanation explanation) {
        return false;
    }

    @Override
    public boolean shouldExtractModel() {
        return false;
    }

    @Override
    public TreeNode createRoot(){
        //System.out.print("\ncalling Marco createRoot()");

        TreeNode root = new TreeNode();
        root.path = new ArrayList<>();
        root.depth = TreeNode.DEFAULT_DEPTH;

        return root;


    }

    @Override
    public TreeNode createChildNode(TreeNode parent, Explanation label){
        TreeNode node = new TreeNode();

        node.path = new ArrayList<>(label.getAxioms());
        node.depth = parent.depth + 1;

        return node;

    }

    @Override
    public void addNodeToTree(TreeNode node){
        //System.out.print("\ncalling Marco addNodeToTree()");
        queue.add(node);
    }

    @Override
    public boolean isTreeClosed(){
        return queue.isEmpty();
    }

    @Override
    public TreeNode getNextNodeFromTree(){
        return queue.poll();
    }

    @Override
    public boolean startIteratingNodeChildren(TreeNode node){
        /*
        parentNode = node;

        iteratedChildren = new ArrayList<>();

        int lastIndex = 0;

        if(node.path != null && !node.path.isEmpty()){
            for(OWLAxiom ax : node.path){
                int idx = numberedAbducibles.getIndex(ax);
                if(idx > lastIndex){
                    lastIndex = idx;
                    }
                }
            }

        for(int i = lastIndex + 1; i <= numberedAbducibles.size(); i++){
            OWLAxiom ax = numberedAbducibles.getAxiomByIndex(i);
            if(ax != null){
                iteratedChildren.add(ax);
                }
            }

        return true;*/
        parentNode = node;
        iteratedChildren = new ArrayList<>();

        int lastIndex = 0;

        if (node.path != null && !node.path.isEmpty()) {
            for (OWLAxiom ax : node.path) {
                int idx = numberedAbducibles.getIndex(ax);
                if (idx > lastIndex) {
                    lastIndex = idx;
                }
            }
        }

        // generates children, but prunes them before adding
        for (int i = lastIndex + 1; i <= numberedAbducibles.size(); i++) {

            OWLAxiom ax = numberedAbducibles.getAxiomByIndex(i);
            if (ax == null) continue;

            // builds the potential explanation for this child
            Set<OWLAxiom> newSet = new HashSet<>(node.path);
            newSet.add(ax);
            BitSet mask = nodeProcessor.toMask(newSet);

            // pruning
            if (nodeProcessor.map.isKnown(mask)) continue;
            if (nodeProcessor.map.hasInconsistentSubset(mask)) continue;
            if (nodeProcessor.map.hasConsistentSuperset(mask)) continue;


            // only adds children that survive pruning
            iteratedChildren.add(ax);
        }

        return true;

    }

    @Override
    public boolean noChildrenLeft(){
        return iteratedChildren.isEmpty();
    }

    @Override
    public OWLAxiom getNextChild(){
        //System.out.print("\ncalling Marco getNextChild()");
        OWLAxiom ax = iteratedChildren.get(0);

        iteratedChildren.remove(0);

        return ax;
    }

    @Override
    public void resetLevel(){}

}