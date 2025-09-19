package sk.uniba.fmph.dai.cats.events;

import sk.uniba.fmph.dai.cats.algorithms.TreeNode;

public class NodeEvent extends Event {

    public TreeNode node;

    public NodeEvent(TreeNode node,EventType type){
        super(type);
        this.node = node;
    }

}
