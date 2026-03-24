package sk.uniba.fmph.dai.cats.events;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

import org.semanticweb.owlapi.model.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import sk.uniba.fmph.dai.cats.algorithms.Algorithm;
import sk.uniba.fmph.dai.cats.common.Configuration;
import sk.uniba.fmph.dai.cats.algorithms.AlgorithmSolver;
import sk.uniba.fmph.dai.cats.algorithms.TreeNode;
import sk.uniba.fmph.dai.cats.common.StringFactory;
import sk.uniba.fmph.dai.cats.data.Explanation;

public class JsonExportEventSubscriber implements IEventSubscriber {
    private final AlgorithmSolver solver;
    public JsonExportEventSubscriber(AlgorithmSolver solver) {
        this.solver = solver;

    }

    private final Map<TreeNode, Integer> nodeIds = new IdentityHashMap<>();

    // JSON collections
    private final List<Map<String, Object>> nodes = new ArrayList<>();
    private final List<Map<String, Object>> edges = new ArrayList<>();

    private final List<String> ontologyAxioms = new ArrayList<>();
    private final List<String> tbox = new ArrayList<>();
    private final List<String> observations = new ArrayList<>();
    private boolean ontologyLoaded = false;

    private int stepCounter = 0;
    private int nextStep() {
        return ++stepCounter;
    }
    private Map<String, Object> makeStepEvent(String type) {
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("step", nextStep());
        event.put("type", type);
        return event;
    }

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

    @Override
    public void processEvent(Event event) {

        if (!ontologyLoaded) { loadOntology(); ontologyLoaded = true; }
        //System.out.println("test print: "+event.getEventType());
        switch (event.getEventType()) {
            case LEVEL_STARTED: break;
            case PROCESSING_NODE:
                handleProcessingNode((NodeEvent) event);
                break;
            case EDGE_CREATED:
                lastCreatedEdge = ((EdgeEvent) event).branchLabel;
                break;
            case EDGE_PRUNED:
                handleEdgePruned(currentNode, "PRUNED PATH!", "EDGE_PRUNED");
                break;
            case INVALID_PATH:
                handleEdgePruned(currentNode, "INVALID PATH!", "INVALID_PATH");
                break;
            case MODEL_REUSE:
                //handleEdgePruned(currentNode, "Model was reused.");
                break;
            case INCONSISTENT_EXPLANATION:
                handleEdgePruned(currentNode, "INCONSISTENT EXPLANATION:", "INCONSISTENT_EXPLANATION");
                break;
            case IRELEVANT_EXPLANATION:
                handleEdgePruned(currentNode, "IRRELEVANT EXPLANATION:", "IRELEVANT_EXPLANATION");
                break;
            case NODE_CREATED:
                handleNodeCreated((NodeEvent) event);
                break;
            case CLOSING_NODE:
                markNodeClosed(((NodeEvent) event).node, "closed");
                break;
            case POSSIBLE_EXPLANATION:
                handlePossibleExplanation((ExplanationEvent) event);
                break;
            case NONMINIMAL_EXPLANATION:
                handleEdgePruned(currentNode, "NON-MINIMAL EXPLANATION!", "NONMINIMAL_EXPLANATION");
                break;
            case TREE_FINISHED:
                writeJson();
                break;
            case MXP_CALL:
                break;
            default: break;
        }
    }

// --------------------------------------------------
    // ONTOLOGY LOADING
    // --------------------------------------------------

    private void loadOntology() {
        try {
            OWLOntology ontology = solver.loader.getOntology();
            if (ontology == null) return;

            tbox.clear();
            observations.clear();

            for (OWLAxiom ax : ontology.getAxioms()) {
                String dl = axiomToDL(ax);

                if (ax instanceof OWLSubClassOfAxiom) {
                    tbox.add(dl);
                }
                else if (ax instanceof OWLClassAssertionAxiom) {
                    observations.add(dl);
                }
            }
        } catch (Exception e) {
            System.err.println("[JSON EXPORT] Cannot load original ontology snapshot: " + e.getMessage());
        }
    }

    private String axiomToDL(OWLAxiom ax) {

        if (ax instanceof OWLSubClassOfAxiom) {
            OWLSubClassOfAxiom sub = (OWLSubClassOfAxiom) ax;

            return classExprToDL(sub.getSubClass()) + " ⊑ " +
                    classExprToDL(sub.getSuperClass());
        }

        if (ax instanceof OWLClassAssertionAxiom) {
            OWLClassAssertionAxiom ass = (OWLClassAssertionAxiom) ax;

            String ind = shortName(ass.getIndividual().asOWLNamedIndividual().getIRI());

            return ind + " : " + classExprToDL(ass.getClassExpression());
        }

        return ax.toString();
    }

    private String classExprToDL(OWLClassExpression ce) {

        if (ce instanceof OWLClass) {
            OWLClass c = (OWLClass) ce;
            return shortName(c.getIRI());
        }

        if (ce instanceof OWLObjectIntersectionOf) {
            OWLObjectIntersectionOf inter = (OWLObjectIntersectionOf) ce;

            List<String> parts = new ArrayList<>();
            for (OWLClassExpression op : inter.getOperands()) {
                parts.add(classExprToDL(op));
            }

            return String.join(" ⊓ ", parts);
        }

        if (ce instanceof OWLObjectComplementOf) {
            OWLObjectComplementOf comp = (OWLObjectComplementOf) ce;
            return "¬" + classExprToDL(comp.getOperand());
        }

        return ce.toString();
    }

    private String shortName(IRI iri){
        String s = iri.toString();
        int i = s.indexOf("#");
        return (i >= 0) ? s.substring(i + 1) : s;
    }


    // -----------------------
    //  PROCESSING_NODE
    // -----------------------
    // Prechádza pendingEdges a hľadá, ktoré hrany patrí tomuto uzlu
    /*
    //original
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
    */
    private void handleProcessingNode(NodeEvent ev) {
        TreeNode node = ev.node;
        currentNode = node;

        nodeIds.computeIfAbsent(node, k -> idCounter++);
        addNodeIfNotExist(node);

        Integer id = nodeIds.get(node);
        if (id == null) return;

        for (Map<String, Object> jsonNode : nodes) {
            if (((Integer) jsonNode.get("id")).intValue() == id.intValue()) {
                if (!jsonNode.containsKey("processed")) {
                    jsonNode.put("processed", makeStepEvent("PROCESSING_NODE"));
                }
                break;
            }
        }
    }

    /*
    // original
    private void handleNodeCreated(NodeEvent ev) {
        TreeNode parent = ev.node;
        OWLAxiom label = lastCreatedEdge;
        if (label != null) {
            pendingEdges.add(new PendingEdge(parent, label));
        }
        lastCreatedEdge = null;
    }
    */
    private void handleNodeCreated(NodeEvent ev) {
        TreeNode newNode = ev.node;

        nodeIds.computeIfAbsent(newNode, k -> idCounter++);
        addNodeIfNotExist(newNode);

        Integer id = nodeIds.get(newNode);
        if (id != null) {
            for (Map<String, Object> jsonNode : nodes) {
                if (((Integer) jsonNode.get("id")).intValue() == id.intValue()) {
                    if (!jsonNode.containsKey("created")) {
                        jsonNode.put("created", makeStepEvent("NODE_CREATED"));
                    }
                    break;
                }
            }
        }

        if (lastCreatedEdge != null) {
            TreeNode parent = findParent(newNode);
            if (parent != null) {
                createEdge(parent, newNode, lastCreatedEdge);
            }
        }

        lastCreatedEdge = null;
    }
    private TreeNode findParent(TreeNode child) {

        if (child.path == null || child.path.isEmpty())
            return null;

        int parentDepth = child.depth - 1;

        for (TreeNode n : nodeIds.keySet()) {

            if (n.depth != parentDepth)
                continue;

            if (isPrefix(n.path, child.path))
                return n;
        }

        return null;
    }
    private boolean isPrefix(List<OWLAxiom> parent, List<OWLAxiom> child) {

        if (parent == null)
            return child == null || child.isEmpty();

        if (child == null)
            return false;

        if (parent.size() + 1 != child.size())
            return false;

        for (int i = 0; i < parent.size(); i++) {

            String p = StringFactory.getRepresentation(parent.get(i));
            String c = StringFactory.getRepresentation(child.get(i));

            if (!p.equals(c))
                return false;
        }

        return true;
    }

    private Map<String, Object> makeBooleanStepField(String fieldName, boolean value, String type) {
        Map<String, Object> obj = new LinkedHashMap<>();
        obj.put(fieldName, value);
        obj.put("step", nextStep());
        obj.put("type", type);
        return obj;
    }

    private Map<String, Object> makeBooleanField(String fieldName, boolean value) {
        Map<String, Object> obj = new LinkedHashMap<>();
        obj.put(fieldName, value);
        return obj;
    }

    private Map<String, Object> makeStringStepField(String fieldName, String value, String type) {
        Map<String, Object> obj = new LinkedHashMap<>();
        obj.put(fieldName, value);
        obj.put("step", nextStep());
        obj.put("type", type);
        return obj;
    }

    private Map<String, Object> makeStringField(String fieldName, String value) {
        Map<String, Object> obj = new LinkedHashMap<>();
        obj.put(fieldName, value);
        return obj;
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

        exNode.put("isExplanation",
                makeBooleanStepField("isExplanation", true, "POSSIBLE_EXPLANATION"));
        exNode.put("closed",
                makeBooleanStepField("closed", true, "CLOSING_NODE"));

        // CONFLICT SET (len pre MHS-MXP)

        if (Configuration.ALGORITHM.toString().contains("MXP")) {

            List<String> conflictSet = new ArrayList<>();

            if (currentNode != null && currentNode.path != null) {

                Set<String> pathSet = new HashSet<>();
                for (OWLAxiom ax : currentNode.path) {
                    pathSet.add(StringFactory.getRepresentation(ax));
                }

                for (OWLAxiom ax : ex.getAxioms()) {
                    String repr = StringFactory.getRepresentation(ax);
                    if (!pathSet.contains(repr)) {
                        conflictSet.add(repr);
                    }
                }
            }

            if (!conflictSet.isEmpty()) {
                exNode.put("conflictSet", conflictSet);
            }
        }
        // =========================

        nodes.add(exNode);

        if (currentNode != null) {
            Map<String, Object> edgeObj = new LinkedHashMap<>();
            edgeObj.put("parent", nodeIds.get(currentNode));
            edgeObj.put("child", exId);
            OWLAxiom last = ex.lastAxiom;
            edgeObj.put("label", last == null ? null : StringFactory.getRepresentation(last));
            edgeObj.put("created", makeStepEvent("EDGE_CREATED"));
            edgeObj.put("pruned", makeStringField("pruned", ""));

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

        List<String> pathStrings = new ArrayList<>();
        if (n.path != null) {
            for (OWLAxiom ax : n.path) {
                pathStrings.add(StringFactory.getRepresentation(ax));
            }
        }
        nodeObj.put("path", pathStrings);

        List<String> modelStrings = new ArrayList<>();
        if (n.model != null && n.model.getNegatedData() != null) {
            n.model.getNegatedData().forEach(ax ->
                    modelStrings.add(StringFactory.getRepresentation(ax))
            );
        }
        nodeObj.put("label", modelStrings);

        nodeObj.put("isExplanation", makeBooleanField("isExplanation", false));
        nodeObj.put("closed", makeBooleanField("closed", false));

        nodes.add(nodeObj);
    }

    private void markNodeClosed(TreeNode n, String type) {
        Integer id = nodeIds.get(n);
        if (id == null) return;

        for (Map<String, Object> jsonNode : nodes) {
            if (((Integer) jsonNode.get("id")).intValue() == id.intValue()) {
                jsonNode.put("closed",
                        makeBooleanStepField("closed", true, "CLOSING_NODE"));
                break;
            }
        }
    }

    private void createEdge(TreeNode parent, TreeNode child, OWLAxiom label) {
        Map<String, Object> edgeObj = new LinkedHashMap<>();
        edgeObj.put("parent", nodeIds.get(parent));
        edgeObj.put("child", child == null ? null : nodeIds.get(child));
        edgeObj.put("label", label == null ? null : StringFactory.getRepresentation(label));
        edgeObj.put("created", makeStepEvent("EDGE_CREATED"));
        edgeObj.put("pruned", makeStringField("pruned", ""));

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

    private void handleEdgePruned(TreeNode node, String reason, String eventType) {
        if (node == null) return;

        Map<String, Object> edgeObj = new LinkedHashMap<>();
        edgeObj.put("parent", nodeIds.get(node));
        edgeObj.put("child", null);
        edgeObj.put("label", lastCreatedEdge == null ? null : StringFactory.getRepresentation(lastCreatedEdge));
        edgeObj.put("pruned", makeStringStepField("pruned", reason, eventType));

        edges.add(edgeObj);
    }

    // -----------------------
    // JSON export
    // -----------------------

    private String buildExportPath() {
        Algorithm  algorithm = Configuration.ALGORITHM;

        String[] parts = Configuration.INPUT_ONT_FILE.split("[/\\\\]");
        String ontology = parts[parts.length - 1].split("\\.")[0];

        String input = Configuration.INPUT_FILE_NAME;
        String timestamp = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date());

        String directoryPath = "exports"
                + File.separator + algorithm
                + File.separator + ontology
                + File.separator + input;

        new File(directoryPath).mkdirs();

        String fileName = timestamp + "_" + input + "_HStree.json";

        return directoryPath + File.separator + fileName;
    }

    private void writeJson() {
        try {
            Map<String, Object> root = new LinkedHashMap<>();
            //root.put("ontology", ontologyAxioms);
            Algorithm algorithm = Configuration.ALGORITHM;
            Map<String, Object> algorithmName = new LinkedHashMap<>();
            root.put("algorithm",algorithm);

            Map<String, Object> ontology = new LinkedHashMap<>();
            ontology.put("tbox", tbox);
            ontology.put("observations", observations);

            root.put("ontology", ontology);
            root.put("nodes", nodes);
            root.put("edges", edges);

            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            String exportPath = buildExportPath();
            mapper.writeValue(new File(exportPath), root);

            System.out.println("[JSON EXPORT] HS-tree exported to " + exportPath);

        } catch (Exception e) {
            System.err.println("[JSON EXPORT] Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
