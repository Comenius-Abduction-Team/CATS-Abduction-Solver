package sk.uniba.fmph.dai.cats.algorithms;

import org.semanticweb.owlapi.model.OWLAxiom;
import sk.uniba.fmph.dai.cats.algorithms.hst.HstTreeBuilder;
import sk.uniba.fmph.dai.cats.algorithms.mxp.MxpNodeProcessor;
import sk.uniba.fmph.dai.cats.algorithms.mxp.QxpNodeProcessor;
import sk.uniba.fmph.dai.cats.algorithms.mxp.RootOnlyTreeBuilder;
import sk.uniba.fmph.dai.cats.algorithms.rctree.RcTreeBuilder;
import sk.uniba.fmph.dai.cats.common.Configuration;
import sk.uniba.fmph.dai.cats.common.StaticPrinter;
import sk.uniba.fmph.dai.cats.common.StringFactory;
import sk.uniba.fmph.dai.cats.data.Explanation;
import sk.uniba.fmph.dai.cats.data_processing.ExplanationManager;
import sk.uniba.fmph.dai.cats.data_processing.ExplanationLogger;
import sk.uniba.fmph.dai.cats.data_processing.Level;
import sk.uniba.fmph.dai.cats.data_processing.TreeStats;
import sk.uniba.fmph.dai.cats.model.InsertSortModelManager;
import sk.uniba.fmph.dai.cats.model.Model;
import sk.uniba.fmph.dai.cats.model.ModelManager;
import sk.uniba.fmph.dai.cats.progress.ProgressManager;
import sk.uniba.fmph.dai.cats.reasoner.AxiomManager;
import sk.uniba.fmph.dai.cats.reasoner.Loader;
import sk.uniba.fmph.dai.cats.timer.MetricsThread;
import sk.uniba.fmph.dai.cats.timer.MetricsManager;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.*;

public class AlgorithmSolver {

    public Loader loader;
    protected ModelManager modelManager;
    public final ExplanationManager explanationManager;
    private final ExplanationLogger logger;
    protected final ProgressManager progressManager;
    public RuleChecker ruleChecker;
    public final MetricsManager metrics;
    public final ConsistencyChecker consistencyChecker;

    // COLLECTIONS
    final public Set<OWLAxiom> path = new HashSet<>();
    protected final Set<Set<OWLAxiom>> pathsInCertainDepth = new HashSet<>();
    public IAbducibleAxioms abducibleAxioms;

    // INTEGERS
    protected int currentDepth = 0;
    int maxDepth = -1;

    protected ITreeBuilder treeBuilder;
    public INodeProcessor nodeProcessor;

    public final TreeStats stats = new TreeStats();
    public Level currentLevel;

    public String message = "";

    protected AlgorithmSolver(Algorithm algorithm, Loader loader, ExplanationManager explanationManager,
                              ProgressManager progressManager, MetricsThread metricsThread) {

        this.loader = loader;

        metrics = new MetricsManager(metricsThread);
        ruleChecker = new RuleChecker(this);

        this.explanationManager = explanationManager;
        explanationManager.setSolver(this);

        logger = new ExplanationLogger(this);

        this.progressManager = progressManager;

        if (Configuration.SORTED_MODELS)
            modelManager = new InsertSortModelManager(this);
        else
            modelManager = new ModelManager(this);

        consistencyChecker = new ConsistencyChecker(this);

        setAlgorithm(algorithm);

    }

    void setAlgorithm(Algorithm algorithm){

        if (algorithm.usesMxp())
            nodeProcessor = new MxpNodeProcessor(this);
        else if (algorithm.usesQxp())
            nodeProcessor = new QxpNodeProcessor(this);
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

        if (Configuration.PRINT_PROGRESS)
            progressManager.updateProgress(0, "Initializing abduction.");

        loader.reasonerManager.isOriginalOntologyConsistentWithLiterals(abducibleAxioms.getAxioms());

        Future<Void> future = null;
        ExecutorService executor = Executors.newSingleThreadExecutor();

        try {

            Callable<Void> task = this::startSolving;
            future = executor.submit(task);

            if (Configuration.TIMEOUT > 0)
                future.get(Configuration.TIMEOUT, TimeUnit.SECONDS);
            else
                future.get();
        }  catch (Throwable e) {

                if (    (e.getClass() == ExecutionException.class &&
                        e.getCause().getClass() == TimeoutException.class)
                ||
                        e.getClass() == TimeoutException.class
                ){
                    future.cancel(true);
                    message += "Time-out reached! ";
                    currentLevel.message = "time-out";
                    logger.makeTimeoutPartialLog(currentLevel);
                }
                else {
                    if (!(future == null))
                        future.cancel(true);
                    currentLevel.error = true;
                    String errorString = /*e.getClass().getName() + " : " + */e.getMessage();
                    currentLevel.errorMessage = errorString;
                    message += "An error occured: " + errorString;
                    logger.makeErrorAndPartialLog(currentLevel, e);
                    StaticPrinter.logError("An error occurred:", e);
                    e.printStackTrace();
                }

        } finally {

            executor.shutdown();
            if (currentLevel.finish < 0)
                currentLevel.finish = metrics.getRunningTime();
            if (currentLevel.memory == 0)
                currentLevel.memory = metrics.measureAverageMemory();
            if (Configuration.PRINT_PROGRESS)
                progressManager.updateProgress(100, "Abduction finished.");

            stats.getFilteringStats().start = metrics.getRunningTime();
            nodeProcessor.postProcessExplanations();
            stats.getFilteringStats().finish = metrics.getRunningTime();

            metrics.setEndTime();
            explanationManager.processExplanations(message, stats);

            if (!message.isEmpty())
                logger.logMessage(Configuration.getInfo(), message);
        }

    }

    protected void printInfo(){
        StaticPrinter.print("");
        StaticPrinter.print(
                "Started solving at: " + StringFactory.formatTime(System.currentTimeMillis())
        );
        StaticPrinter.print(String.join("\n", Configuration.getInfo()));
        StaticPrinter.print("");
    }

    protected void addNegatedObservation(){
        loader.reasonerManager.addNegatedObservationToOntologies();
    }

    protected void initializeAbducibles() {
        TransformedAbducibles transformedAbduciblesFromInput = new TransformedAbducibles(loader);
        abducibleAxioms = treeBuilder.createAbducibles(transformedAbduciblesFromInput);
        modelManager.setExtractor(new ModelExtractor(loader, abducibleAxioms));
    }

    protected Void startSolving() throws TimeoutException {

        metrics.setStartTime();

        currentDepth = 0;
        currentLevel = stats.getLevelStats(currentDepth);
        currentLevel.start = metrics.getRunningTime();

        TreeNode root = treeBuilder.createRoot();
        if (root == null) {
            StaticPrinter.debugPrint("[!!!] ROOT COULD NOT BE CREATED!");
            return null;
        }
        treeBuilder.addNodeToTree(root);
        currentLevel.createdNodes = 1;

        if(isTimeout()) {
            logger.makeTimeoutPartialLog(currentLevel);
            throw new TimeoutException();
        }

        while (!treeBuilder.isTreeClosed()) {

            TreeNode node = treeBuilder.getNextNodeFromTree();

            if (node == null){
                StaticPrinter.debugPrint("[!!!] Null node!");
                continue;
            }

            updateDepthIfNeeded(node);

            if (depthLimitReached()) {
                currentLevel.message = "max depth";
                break;
            }

            if(isTimeout()){
                logger.makeTimeoutPartialLog(currentLevel);
                throw new TimeoutException();
            }

            StaticPrinter.debugPrint("*********\n" + "[TREE] PROCESSING node: " + node);

            if (!node.processed) {
                node.processed = true;
                currentLevel.processedNodes += 1;
            } else {
                currentLevel.repeatedProcessing += 1;
            }

            boolean canIterateNodeChildren = treeBuilder.startIteratingNodeChildren(node);

            if (!canIterateNodeChildren){
                StaticPrinter.debugPrint("[TREE] NO CHILDREN TO ITERATE!");
                currentLevel.childlessNodes += 1;
                continue;
            }

            StaticPrinter.debugPrint("[TREE] Iterating child edges");

            while (!treeBuilder.noChildrenLeft()){

                OWLAxiom child = treeBuilder.getNextChild();

                if (child == null) {
                    StaticPrinter.debugPrint("[!!!] NULL CHILD");
                    continue;
                }

                StaticPrinter.debugPrint("[TREE] TRYING EDGE: " + StringFactory.getRepresentation(child));

                currentLevel.createdEdges += 1;

                if(isTimeout()){
                    logger.makeTimeoutPartialLog(currentLevel);
                    throw new TimeoutException();
                }

                if(isIncorrectPath(node, child)){
                    StaticPrinter.debugPrint("[PRUNING] INCORRECT PATH!");
                    currentLevel.prunedEdges += 1;
                    continue;
                }

                Explanation explanation = createPossibleExplanation(node, child);

                path.clear();
                path.addAll(explanation.getAxioms());

                boolean pruneThisChild = treeBuilder.shouldPruneChildBranch(node, explanation);

                if (pruneThisChild){
                    StaticPrinter.debugPrint("[PRUNING] NODE CLOSED!");
                    currentLevel.prunedEdges += 1;
                    path.clear();
                    continue;
                }

                if (Configuration.REUSE_OF_MODELS)
                    modelManager.findReuseModelForPath(path);

                boolean canReuseModel = Configuration.REUSE_OF_MODELS && modelManager.canReuseModel();

                if (!canReuseModel) {

                    if (isTimeout()){
                        logger.makeTimeoutPartialLog(currentLevel);
                        throw new TimeoutException();
                    }

                    int explanationsFound = nodeProcessor.findExplanations(explanation, treeBuilder.shouldExtractModel());
                    boolean shouldCloseNode = nodeProcessor.shouldCloseNode(explanationsFound);

                    if (shouldCloseNode){
                        path.clear();
                        continue;
                    }

                } else {

                    currentLevel.reusedModels += 1;
                    StaticPrinter.debugPrint("[MODEL] Model was reused.");

                }

                if (isTimeout()){
                    logger.makeTimeoutPartialLog(currentLevel);
                    throw new TimeoutException();
                }

                treeBuilder.addNodeToTree(treeBuilder.createChildNode(node, explanation));
                currentLevel.createdNodes += 1;

                StaticPrinter.debugPrint("[TREE] Created node");

                path.clear();

            }

        }

        currentLevel.finish = metrics.getRunningTime();

        StaticPrinter.debugPrint("[TREE] Finished iterating the tree.");

        path.clear();
        pathsInCertainDepth.clear();
        logger.makePartialLog(currentLevel);

        return null;

    }

    private boolean isIncorrectPath(TreeNode parent, OWLAxiom edgeLabel){
        return parent.path.contains(AxiomManager.getComplementOfOWLAxiom(loader, edgeLabel)) ||
                edgeLabel.equals(loader.getObservationAxiom());
    }

    private Explanation createPossibleExplanation(TreeNode node, OWLAxiom child){
        Explanation explanation = createExplanationFromAxioms(node.path);
        explanation.addAxiom(child);
        explanation.lastAxiom = child;
        return explanation;
    }


    public boolean isTimeout(){
        return metrics.isTimeout();
    }

    public void removeNegatedObservationFromPath(){
        path.remove(loader.getNegObservationAxiom());
    }

    public Explanation createExplanationFromAxioms(Collection<OWLAxiom> axioms){
        return new Explanation(axioms, currentLevel, metrics.getRunningTime());
    }

    boolean isPathAlreadyStored(){
        return pathsInCertainDepth.contains(path);
    }

    void storePath(){
        pathsInCertainDepth.add(new HashSet<>(path));
    }

    protected boolean depthLimitReached(){
        return Configuration.DEPTH > 0 && currentDepth == Configuration.DEPTH;
    }

    private void updateDepthIfNeeded(TreeNode node){

        if (node.depth == currentDepth) {
            node.assignedLevel = currentLevel;
            return;
        }

        pathsInCertainDepth.clear();
        currentLevel.finish = metrics.getRunningTime();
        currentLevel.memory = metrics.measureAverageMemory();

        if (node.depth > maxDepth) {
            maxDepth = node.depth;
            logger.makePartialLog(currentLevel);
            if (Configuration.PRINT_PROGRESS)
                updateProgress();
        }

        currentDepth = node.depth;

        // if (node.assignedLevel == null) {
            currentLevel = stats.getNewLevelStats(currentDepth);
            currentLevel.start = metrics.getRunningTime();
            node.assignedLevel = currentLevel;
//        }
//        else {
//            currentLevel = node.assignedLevel;
//        }



        StaticPrinter.debugPrint("[TREE] entering depth " + node.depth);
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

    public void updateProgress(){
        progressManager.updateProgress(currentDepth, metrics.getRunningTime());
    }

}
