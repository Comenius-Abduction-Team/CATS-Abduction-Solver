package sk.uniba.fmph.dai.cats.algorithms.marco;
import org.semanticweb.owlapi.model.OWLAxiom;
import sk.uniba.fmph.dai.cats.algorithms.*;
import sk.uniba.fmph.dai.cats.algorithms.hst.HstNode;
import sk.uniba.fmph.dai.cats.algorithms.hst.INumberedAbducibles;
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
    private List<OWLAxiom> orderedAbducibles;

    public MarcoTreeBuilder(AlgorithmSolver solver){
        this.solver = solver;
    }

    @Override
    public IAbducibleAxioms createAbducibles(TransformedAbducibles abducibles) {
        IAbducibleAxioms abd = new AxiomSetAbducibles(abducibles);

        orderedAbducibles = new ArrayList<>(abd.getAxioms());

        return abd;
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
        System.out.print("\ncalling Marco createRoot()");

        TreeNode root = new TreeNode();
        root.path = new ArrayList<>();
        root.depth = TreeNode.DEFAULT_DEPTH;

        return root;
    }

    @Override
    public TreeNode createChildNode(TreeNode parent, Explanation label){
        System.out.print("\ncalling Marco createChildNode()");
        TreeNode node = new TreeNode();

        node.path = new ArrayList<>(label.getAxioms());
        node.depth = parent.depth + 1;

        return node;

    }

    @Override
    public void addNodeToTree(TreeNode node){
        System.out.print("\ncalling Marco addNodeToTree()");
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

        parentNode = node;

        iteratedChildren = new ArrayList<>();

        int startIndex = 0;

        if(node.path != null && !node.path.isEmpty()){

            OWLAxiom last = null;

            for(OWLAxiom ax : node.path)
                last = ax;

            startIndex = orderedAbducibles.indexOf(last) + 1;
        }

        for(int i = startIndex; i < orderedAbducibles.size(); i++){
            iteratedChildren.add(orderedAbducibles.get(i));
        }

        return true;
    }

    @Override
    public boolean noChildrenLeft(){
        return iteratedChildren.isEmpty();
    }

    @Override
    public OWLAxiom getNextChild(){
        System.out.print("\ncalling Marco getNextChild()");
        OWLAxiom ax = iteratedChildren.get(0);

        iteratedChildren.remove(0);

        return ax;
    }

    @Override
    public void resetLevel(){}

}