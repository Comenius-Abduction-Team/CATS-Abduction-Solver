package sk.uniba.fmph.dai.cats.events;

import java.io.File;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.semanticweb.owlapi.model.OWLAxiom;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import sk.uniba.fmph.dai.cats.algorithms.TreeNode;
import sk.uniba.fmph.dai.cats.common.StringFactory;
import sk.uniba.fmph.dai.cats.data.Explanation;

public class JsonExportEventSubscriber implements IEventSubscriber {
    private final Map<TreeNode, Integer> nodeIds = new IdentityHashMap<>();

    // JSON collections
    private final List<Map<String, Object>> nodes = new ArrayList<>();
    private final List<Map<String, Object>> edges = new ArrayList<>();

    // hrana ktorú ešte nemôžeme spojiť s dieťaťom
    // solver najpr vztvorí rodiča až potom pridáva dieťa keď vytvorí node
    private static class PendingEdge {
        final TreeNode parent;
        final OWLAxiom label;
        PendingEdge(TreeNode parent, OWLAxiom label){
            this.parent = parent;
            this.label = label;
        }
    }
    private final List<PendingEdge> pendingEdges = new ArrayList<>();

    private TreeNode currentNode = null; // práve spracovávaný uzol
    private OWLAxiom lastCreatedEdge = null; // label poslednej hrany
    private int idCounter = 0;
    private static final String OUTPUT = "logs/hs_tree_export.json";

    @Override
    public void processEvent(Event event) {

        switch (event.getEventType()) {
            case LEVEL_STARTED: break;
            case PROCESSING_NODE:
                handleProcessingNode((NodeEvent) event);
                break;
            case EDGE_CREATED:
                lastCreatedEdge = ((EdgeEvent) event).branchLabel;
                break;
            case EDGE_PRUNED:break;
            case NODE_CREATED:
                handleNodeCreated((NodeEvent) event);
                break;
            case CLOSING_NODE:
                markNodeClosed(((NodeEvent) event).node, "closed");
                break;
            case POSSIBLE_EXPLANATION:
                handlePossibleExplanation((ExplanationEvent) event);
                break;
            case TREE_FINISHED:
                writeJson();
                break;
            default: break;
        }
    }

    // -----------------------
    //  PROCESSING_NODE
    // -----------------------
    // Prechádza pendingEdges a hľadá, ktoré hrany patrí tomuto uzlu
    private void handleProcessingNode(NodeEvent ev) {
        TreeNode node = ev.node;
        currentNode = node;

        nodeIds.computeIfAbsent(node, k -> idCounter++);
        addNodeIfNotExist(node);

        Iterator<PendingEdge> it = pendingEdges.iterator();
        while (it.hasNext()) {
            PendingEdge p = it.next();
            if (p.parent == null) continue;
            if (node.depth == p.parent.depth + 1) {
                if (axInPath(node, p.label)) {
                    createEdge(p.parent, node, p.label);
                    it.remove();
                }
            }
        }
    }

    private void handleNodeCreated(NodeEvent ev) {
        TreeNode parent = ev.node;
        OWLAxiom label = lastCreatedEdge;
        if (label != null) {
            pendingEdges.add(new PendingEdge(parent, label));
        }
        lastCreatedEdge = null;
    }
    // vytvorenie nového uzla, 
    private void handlePossibleExplanation(ExplanationEvent ev) {
        Explanation ex = ev.explanation;
        if (ex == null) return;

        int exId = idCounter++;
        Map<String, Object> exNode = new LinkedHashMap<>();
        exNode.put("id", exId);

        int depth = (currentNode == null) ? TreeNode.DEFAULT_DEPTH : (currentNode.depth + 1);
        exNode.put("depth", depth);

        // explanation axioms
        List<String> pathStrings = new ArrayList<>();
        for (OWLAxiom a : ex.getAxioms()) {
            pathStrings.add(StringFactory.getRepresentation(a));
        }
        exNode.put("path", pathStrings);

        // LABEL
        exNode.put("label", pathStrings);

        exNode.put("isExplanation", true);
        exNode.put("closed", "closed");

        nodes.add(exNode);

        if (currentNode != null) {
            Map<String, Object> edgeObj = new LinkedHashMap<>();
            edgeObj.put("parent", nodeIds.get(currentNode));
            edgeObj.put("child", exId);
            OWLAxiom last = ex.lastAxiom;
            edgeObj.put("label", last == null ? null : StringFactory.getRepresentation(last));
            edges.add(edgeObj);
        }
    }

    // -----------------------
    // Node/edge functions
    // -----------------------

    private boolean nodeExists(TreeNode n) {
        Integer id = nodeIds.get(n);
        if (id == null) return false;
        for (Map<String, Object> m : nodes) {
            if (((Integer) m.get("id")).intValue() == id.intValue()) return true;
        }
        return false;
    }

    // Pridanie uzlu NODE do JSON
    private void addNodeIfNotExist(TreeNode n) {
        if (nodeExists(n)) return;

        Map<String, Object> nodeObj = new LinkedHashMap<>();
        int id = nodeIds.get(n);

        nodeObj.put("id", id);
        nodeObj.put("depth", n.depth);

        // PATH H(n)
        List<String> pathStrings = new ArrayList<>();
        if (n.path != null) {
            for (OWLAxiom ax : n.path) {
                pathStrings.add(StringFactory.getRepresentation(ax));
            }
        }
        nodeObj.put("path", pathStrings);

        // LABEL S(n)
        List<String> modelStrings = new ArrayList<>();
        if (n.model != null && n.model.getNegatedData() != null) {
            n.model.getNegatedData().forEach(ax ->
                    modelStrings.add(StringFactory.getRepresentation(ax))
            );
        }
        nodeObj.put("label", modelStrings);

        nodeObj.put("isExplanation", false);
        nodeObj.put("closed", "none");

        nodes.add(nodeObj);
    }

    private void markNodeClosed(TreeNode n, String type) {
        Integer id = nodeIds.get(n);
        if (id == null) return;
        for (Map<String, Object> jsonNode : nodes) {
            if (((Integer) jsonNode.get("id")).intValue() == id.intValue()) {
                jsonNode.put("closed", type);
            }
        }
    }

    private void createEdge(TreeNode parent, TreeNode child, OWLAxiom label) {
        Map<String, Object> edgeObj = new LinkedHashMap<>();
        edgeObj.put("parent", nodeIds.get(parent));
        edgeObj.put("child", child == null ? null : nodeIds.get(child));
        edgeObj.put("label", label == null ? null : StringFactory.getRepresentation(label));
        edges.add(edgeObj);
    }

    // Kontrola či je axiom prítomný v node.path
    private boolean axInPath(TreeNode node, OWLAxiom ax) {
        if (node == null || node.path == null || ax == null) return false;
        String target = StringFactory.getRepresentation(ax);
        for (OWLAxiom p : node.path) {
            if (target.equals(StringFactory.getRepresentation(p))) return true;
        }
        return false;
    }

    // -----------------------
    // JSON export
    // -----------------------

    private void writeJson() {
        try {
            Map<String, Object> root = new LinkedHashMap<>();
            root.put("nodes", nodes);
            root.put("edges", edges);

            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            mapper.writeValue(new File(OUTPUT), root);

            System.out.println("[JSON EXPORT] HS-tree exported to " + OUTPUT);

        } catch (Exception e) {
            System.err.println("[JSON EXPORT] Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
