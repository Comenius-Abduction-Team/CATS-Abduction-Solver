package sk.uniba.fmph.dai.cats.algorithms.hybrid;

import org.semanticweb.owlapi.model.OWLAxiom;
import sk.uniba.fmph.dai.cats.algorithms.Algorithm;
import sk.uniba.fmph.dai.cats.algorithms.rctree.RcTreeBuilder;
import sk.uniba.fmph.dai.cats.common.Configuration;
import sk.uniba.fmph.dai.cats.common.IPrinter;
import sk.uniba.fmph.dai.cats.common.StringFactory;
import sk.uniba.fmph.dai.cats.data.Explanation;
import sk.uniba.fmph.dai.cats.explanation_processing.ExplanationManager;
import sk.uniba.fmph.dai.cats.explanation_processing.ExplanationLogger;
import sk.uniba.fmph.dai.cats.model.Model;
import sk.uniba.fmph.dai.cats.model.ModelManager;
import sk.uniba.fmph.dai.cats.progress.ProgressManager;
import sk.uniba.fmph.dai.cats.reasoner.Loader;
import sk.uniba.fmph.dai.cats.timer.ThreadTimer;
import sk.uniba.fmph.dai.cats.timer.TimeManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class AlgorithmSolver {

    public Loader loader;
    protected ModelManager modelManager;
    public final ExplanationManager explanationManager;
    private final ExplanationLogger logger;
    protected final ProgressManager progressManager;
    public RuleChecker ruleChecker;
    protected SetDivider setDivider;
    protected IPrinter printer;
    public final TimeManager timer;
    protected final ConsistencyChecker consistencyChecker;

    // COLLECTIONS
    final public Set<OWLAxiom> path = new HashSet<>();
    protected final Set<Set<OWLAxiom>> pathsInCertainDepth = new HashSet<>();
    protected IAbducibleAxioms abducibleAxioms;

    // INTEGERS
    protected int currentDepth = 0;
    int maxDepth = -1;
    protected int numberOfNodes = 0;

    protected TreeBuilder treeBuilder;
    public NodeProcessor nodeProcessor;

    protected AlgorithmSolver(Algorithm algorithm, Loader loader, ExplanationManager explanationManager,
                              ProgressManager progressManager, ThreadTimer threadTimer, IPrinter printer) {

        this.loader = loader;
        ruleChecker = new RuleChecker(loader);
        timer = new TimeManager(threadTimer);

        this.explanationManager = explanationManager;
        explanationManager.setSolver(this);
        setDivider = new SetDivider(explanationManager);

        logger = new ExplanationLogger(this);

        this.progressManager = progressManager;

        this.printer = printer;

        modelManager = new ModelManager();

        consistencyChecker = new ConsistencyChecker(this);

        setAlgorithm(algorithm);

    }

    void setAlgorithm(Algorithm algorithm){

        if (algorithm.usesMxp())
            nodeProcessor = new MxpNodeProcessor(this);
        else
            nodeProcessor = new ClassicNodeProcessor(this);

        if (algorithm.isHst())
            treeBuilder = new HstTreeBuilder(this);
        else if (algorithm.isRcTree())
            treeBuilder = new RcTreeBuilder(this);
        else if (algorithm.isTreeOnly())
            treeBuilder = new RootOnlyTreeBuilder(this);
        else
            treeBuilder = new MhsTreeBuilder(this);

    }

    public void solve(){

        printInfo();

        addNegatedObservation();

        if (Configuration.PRINT_PROGRESS)
            progressManager.updateProgress(0, "Initializing abducibles.");
        initializeAbducibles();

        timer.setStartTime();

        if (Configuration.PRINT_PROGRESS)
            progressManager.updateProgress(0, "Initializing abduction.");

        String message = null;

        if (!loader.reasonerManager.isOntologyConsistent()) {
            message = "The observation is already entailed!";
            explanationManager.processExplanations(message);
            logger.logMessage(Configuration.getInfo(), message);
        }

        else {
            loader.reasonerManager.isOriginalOntologyConsistentWithLiterals(abducibleAxioms.getAxioms());
            try {
                startSolving();
            } catch (Throwable e) {
                logger.makeErrorAndPartialLog(currentDepth, e);
                message = "An error occured: " + e.getMessage();
                throw (e);
            } finally {
                timer.setEndTime();
                explanationManager.processExplanations(message);
            }
        }
        if (Configuration.PRINT_PROGRESS)
            progressManager.updateProgress(100, "Abduction finished.");

    }

    protected void printInfo(){

        printer.print("\n");
        printer.print(String.join("\n", Configuration.getInfo()));
        printer.print("\n");

    }

    protected void addNegatedObservation(){
        loader.reasonerManager.addNegatedObservationToOntologies();
    }

    protected void initializeAbducibles() {
        TransformedAbducibles abducibles = new TransformedAbducibles(loader);
        modelManager.setExtractor(new ModelExtractor(loader, abducibles));
        abducibleAxioms = treeBuilder.createAbducibles(abducibles);
    }

    protected void startSolving() {

        currentDepth = 0;

        TreeNode root = treeBuilder.createRoot();
        if (root == null) {
            if (Configuration.DEBUG_PRINT)
                System.out.println("[!!!] ROOT COULD NOT BE CREATED!");
            return;
        }
        treeBuilder.addNodeToTree(root);

        if(isTimeout()) {
            logger.makeTimeoutPartialLog(currentDepth);
            return;
        }

        while (!treeBuilder.isTreeClosed()) {

            TreeNode node = treeBuilder.getNextNodeFromTree();

            if (node == null){
                if (Configuration.DEBUG_PRINT)
                    System.out.println("[!!!] Null node!");
                continue;
            }

            if (increaseDepth(node)){
                if (Configuration.DEBUG_PRINT)
                    System.out.println("[TREE] finished depth " + currentDepth);
                currentDepth++;
                //System.out.println(node);
                //System.out.println("depth: " + currentDepth);
            }


            if (depthLimitReached()) {
                break;
            }

            if(isTimeout()){
                logger.makeTimeoutPartialLog(currentDepth);
                break;
            }

            if (Configuration.DEBUG_PRINT)
                System.out.println("*********\n" + "[TREE] PROCESSING node: " + node);

            if (node.closed) {
                if (Configuration.DEBUG_PRINT)
                    System.out.println("[TREE] Closed node");
                continue;
            }

            boolean canIterateNodeChildren = treeBuilder.startIteratingNodeChildren(node);

            if (!canIterateNodeChildren){
                if (Configuration.DEBUG_PRINT)
                    System.out.println("[TREE] NO CHILDREN TO ITERATE!");
                continue;
            }

            numberOfNodes++;

            while (!treeBuilder.noChildrenLeft()){

                OWLAxiom child = treeBuilder.getNextChild();

                if (child == null) {
                    if (Configuration.DEBUG_PRINT)
                        System.out.println("[!!!] NULL CHILD");
                    continue;
                }

                if (Configuration.DEBUG_PRINT)
                    System.out.println("[TREE] TRYING EDGE: " + StringFactory.getRepresentation(child));

                if(isTimeout()){
                    logger.makeTimeoutPartialLog(currentDepth);
                    return;
                }

                //ak je axiom negaciou axiomu na ceste k vrcholu, alebo
                //ak axiom nie je v abducibles
                //nepokracujeme vo vetve
                if(treeBuilder.isIncorrectPath(node.path, child)){
                    if (Configuration.DEBUG_PRINT)
                        System.out.println("[PRUNING] INCORRECT PATH!");
                    continue;
                }

                //rovno pridame potencialne vysvetlenie
                Explanation explanation = createPossibleExplanation(node, child);

                path.clear();
                path.addAll(explanation.getAxioms());

                boolean pruneThisChild = treeBuilder.pruneTree(node, explanation);

                if (pruneThisChild || node.closed){
                    if (Configuration.DEBUG_PRINT)
                        System.out.println("[PRUNING] NODE CLOSED!");
                    path.clear();
                    continue;
                }

                if (Configuration.REUSE_OF_MODELS)
                    modelManager.findReuseModelForPath(path);

                if (Configuration.REUSE_OF_MODELS && modelManager.canReuseModel())
                    explanationManager.setLengthOneExplanations(new ArrayList<>());
                else {

                    if (Configuration.DEBUG_PRINT){
                        System.out.println("[MODEL] Model was not reused.");
                    }

                    if (isTimeout()){
                        logger.makeTimeoutPartialLog(currentDepth);
                        return;
                    }

                    if (nodeProcessor.cannotAddExplanation(explanation)){
                        path.clear();
                        continue;
                    }
                }

                treeBuilder.addNodeToTree(
                        treeBuilder.createChildNode(node, explanation)
                );

                if (Configuration.DEBUG_PRINT){
                    System.out.println("[TREE] Created node");
                }

                path.clear();

            }
        }

        if (Configuration.DEBUG_PRINT) {
            System.out.println("[TREE] Finished iterating the tree.");
            System.out.println("[TREE] Number of nodes: " + numberOfNodes);
        }

        //System.out.println(numberOfNodes);

        path.clear();

        if(!timer.levelHasTime(currentDepth)){
            logger.makePartialLog(currentDepth);
            pathsInCertainDepth.clear();
        }

        currentDepth = 0;

    }

    private Explanation createPossibleExplanation(TreeNode node, OWLAxiom child){
        Explanation explanation = new Explanation();
        explanation.addAxioms(node.path);
        explanation.addAxiom(child);
        explanation.lastAxiom = child;
        explanation.setAcquireTime(timer.getCurrentTime());
        explanation.setLevel(currentDepth);
        return explanation;
    }


    public boolean isTimeout(){
        return timer.isTimeout();
    }

    void removeNegatedObservationFromPath(){
        path.remove(loader.getNegObservationAxiom());
    }

    Explanation createExplanationFromAxioms(Set<OWLAxiom> axioms){
        return new Explanation(axioms, currentDepth, timer.getCurrentTime());
    }

    boolean isPathAlreadyStored(){
        return pathsInCertainDepth.contains(path);
    }

    void storePath(){
        pathsInCertainDepth.add(new HashSet<>(path));
    }

    protected boolean increaseDepth(TreeNode node){
        if (node.depth > currentDepth){
            logger.makePartialLog(currentDepth);
            if (Configuration.PRINT_PROGRESS)
                updateProgress();
            return true;
        }
        return false;
    }

    protected boolean depthLimitReached(){
        return Configuration.DEPTH > 0 && currentDepth == Configuration.DEPTH;
    }

    private void increaseDepthVoid(TreeNode node){

        if (node.depth != currentDepth) {
            pathsInCertainDepth.clear();
        }
        else return;

        if (currentDepth == maxDepth){
            logger.makePartialLog(currentDepth);
            if (Configuration.PRINT_PROGRESS)
                updateProgress();
        }

        if (node.depth > maxDepth)
            maxDepth = node.depth;

        currentDepth = node.depth;

    }

    public Model findAndGetModelToReuse(){
        boolean modelFound = false;

        if (!modelManager.canReuseModel())
            modelFound = modelManager.findReuseModelForPath(path);

        if (!modelFound && !modelManager.canReuseModel())
            return null;

        return modelManager.getReusableModel();
    }

    public Model removePathAxiomsFromModel(Model model){
        Model copy = new Model(model);
        copy.getNegatedData().removeAll(path);
        return copy;
    }

    void updateProgress(){
        progressManager.updateProgress(currentDepth, timer.getCurrentTime());
    }


}
