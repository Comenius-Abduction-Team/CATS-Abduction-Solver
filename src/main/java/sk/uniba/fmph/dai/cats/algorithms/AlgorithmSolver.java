package sk.uniba.fmph.dai.cats.algorithms;

import org.semanticweb.owlapi.model.OWLAxiom;
import sk.uniba.fmph.dai.cats.algorithms.hst.HstTreeBuilder;
import sk.uniba.fmph.dai.cats.algorithms.marco.MarcoNodeProcessor;
import sk.uniba.fmph.dai.cats.algorithms.marco.MarcoTreeBuilder;
import sk.uniba.fmph.dai.cats.algorithms.marco.SubsetMapManager;
import sk.uniba.fmph.dai.cats.algorithms.mhs.MhsTreeBuilder;
import sk.uniba.fmph.dai.cats.algorithms.mxp.MxpNodeProcessor;
import sk.uniba.fmph.dai.cats.algorithms.mxp.QxpNodeProcessor;
import sk.uniba.fmph.dai.cats.algorithms.mxp.RootOnlyTreeBuilder;
import sk.uniba.fmph.dai.cats.algorithms.mxp.TripleMxpNodeProcessor;
import sk.uniba.fmph.dai.cats.algorithms.rctree.RctTreeBuilder;
import sk.uniba.fmph.dai.cats.common.Configuration;
import sk.uniba.fmph.dai.cats.common.StaticPrinter;
import sk.uniba.fmph.dai.cats.common.StringFactory;
import sk.uniba.fmph.dai.cats.data.Explanation;
import sk.uniba.fmph.dai.cats.data_processing.ExplanationManager;
import sk.uniba.fmph.dai.cats.data_processing.ExplanationLogger;
import sk.uniba.fmph.dai.cats.events.*;
import sk.uniba.fmph.dai.cats.metrics.*;
import sk.uniba.fmph.dai.cats.model.InsertSortModelManager;
import sk.uniba.fmph.dai.cats.model.Model;
import sk.uniba.fmph.dai.cats.model.ModelExtractor;
import sk.uniba.fmph.dai.cats.model.ModelManager;
import sk.uniba.fmph.dai.cats.progress.ProgressManager;
import sk.uniba.fmph.dai.cats.reasoner.AxiomManager;
import sk.uniba.fmph.dai.cats.reasoner.Loader;

import java.util.Arrays;
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
    public IAbducibleAxioms abducibleAxioms;

    // INTEGERS
    public int currentDepth = 0;
    int maxDepth = -1;

    protected ITreeBuilder treeBuilder;
    public INodeProcessor nodeProcessor;

    public final TreeStats stats = new TreeStats();
    public Level currentLevel;

    public String message = "";

    protected AlgorithmSolver(Algorithm algorithm, Loader loader, ExplanationManager explanationManager,
                              ProgressManager progressManager, MetricsThread metricsThread) {
        /*inicializacia novych instancii*/
        this.loader = loader;

        metrics = new MetricsManager(metricsThread);
        ruleChecker = new RuleChecker(this);

        this.explanationManager = explanationManager;
        explanationManager.setSolver(this);

        logger = new ExplanationLogger(this);

        this.progressManager = progressManager;

        consistencyChecker = new ConsistencyChecker(this);

        setOptimisations(algorithm);
        registerSubscribersFromConfiguration();
        setAlgorithm(algorithm);

        if (Configuration.optimisations.contains(Optimisation.SORT_MODEL))
            modelManager = new InsertSortModelManager(this);
        else
            modelManager = new ModelManager(this);

        consistencyChecker.modelManager = modelManager;

    }

    private void setOptimisations(Algorithm algorithm){
        /*nastavenie nejakych optimalizacii*/
        if (Configuration.IGNORE_DEFAULT_OPTIMISATIONS)
            return;

        Optimisation[] optimisations;

        if (Configuration.NEGATION_ALLOWED)
            optimisations = algorithm.getDefaultOptimisationsWithNegations();
        else
            optimisations = algorithm.getDefaultOptimisationsWithoutNegations();
      
        Configuration.optimisations.addAll(Arrays.asList(optimisations));
    }

    private void registerSubscribersFromConfiguration() {
        /*ci treba vypisat debug a zaznamenavat nejake statistiky*/
        if (Configuration.TRACKING_STATS)
            EventPublisher.registerSubscriber(this, new StatEventSubscriber(this));
        if (Configuration.DEBUG_PRINT)
            EventPublisher.registerSubscriber(this, new DebugPrintEventSubscriber(this));
    }

    private void setAlgorithm(Algorithm algorithm){
        if (algorithm.usesMxp()){
            if (Configuration.NEGATION_ALLOWED && Configuration.optimisations.contains(Optimisation.TRIPLE_MXP))
                nodeProcessor = new TripleMxpNodeProcessor(this);
            else
                nodeProcessor = new MxpNodeProcessor(this);
        }

        else if (algorithm.usesQxp())
            nodeProcessor = new QxpNodeProcessor(this);
        else if (algorithm.isMarco()) {
            nodeProcessor = new MarcoNodeProcessor(this);
        }
        else
            nodeProcessor = new ClassicNodeProcessor(this);


        if (algorithm.isHst())
            treeBuilder = new HstTreeBuilder(this);
        else if (algorithm.isRcTree())
            treeBuilder = new RctTreeBuilder(this);
        else if (algorithm.isRootOnly())
            treeBuilder = new RootOnlyTreeBuilder(this);
        else if (algorithm.isMarco())
            treeBuilder = new MarcoTreeBuilder(this, (MarcoNodeProcessor) nodeProcessor);
        else
            treeBuilder = new MhsTreeBuilder(this);
    }

    public void solve(){

        printInfo();

        addNegatedObservation();

        if (Configuration.PRINT_PROGRESS)
            progressManager.updateProgress(0, "Initializing abducibles.");
        initializeAbducibles();

        if (Configuration.PRINT_PROGRESS)/*k vypisu*/
            progressManager.updateProgress(0, "Initializing abduction.");

        loader.reasonerManager.isOriginalOntologyConsistentWithLiterals(abducibleAxioms.getAxioms());
        /*ci je ontologia konzistentna?*/
        Future<Void> future = null;
        ExecutorService executor = Executors.newSingleThreadExecutor();

        try {

            Callable<Void> task = this::startSolving; /*referencia na metodu startSolving, aby sa neskor ho bolo mozne volat*/
            future = executor.submit(task); /*nevola startSolving hned, ale naplanuje jeho vykonanie na inom threade?*/

            if (Configuration.TIMEOUT > 0)
                future.get(Configuration.TIMEOUT, TimeUnit.SECONDS);
            else
                future.get(); /*caka, kym startSolving() je dokonceny*/
        }  catch (Throwable e) { /*ak bol error, vypise*/

                if (    (e.getClass() == ExecutionException.class &&
                        e.getCause().getClass() == TimeoutException.class)
                ||
                        e.getClass() == TimeoutException.class
                ){
                    future.cancel(true);
                    message += "Time-out reached! ";
                    currentLevel.message = "time-out";
                    logger.addLevelToPartialLog(currentLevel);
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
                currentLevel.finish = metrics.getRunningTime(); /*zaznamenanie dlzky runtomu*/
            if (currentLevel.memory == 0)
                currentLevel.memory = metrics.measureAverageMemory(); /*zaznamenanie memory*/
            if (Configuration.PRINT_PROGRESS)
                progressManager.updateProgress(100, "Abduction finished.");

            stats.getFilteringStats().start = metrics.getRunningTime();
            nodeProcessor.postProcessExplanations(); /*postprocessing v node-och?*/
            stats.getFilteringStats().finish = metrics.getRunningTime();

            metrics.setEndTime();
            explanationManager.processExplanations(message, stats); /*processing vysvetleni?*/

            logger.logInfo(Configuration.getInfo(), message);
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
        nodeProcessor.storeAbduciblesIfNeeded(abducibleAxioms);
    }

    protected Void startSolving() throws TimeoutException {

        metrics.setStartTime();

        currentDepth = 0; /*hlbka stromu je zatial 0*/
        currentLevel = stats.getLevelStats(0); /*aktualny level podla nejakej funkcie*/
        currentLevel.start = metrics.getRunningTime();

        TreeNode root = treeBuilder.createRoot(); /*vytvori root*/

        if (root == null) {
            EventPublisher.publishGenericEvent(this, EventType.ROOT_NOT_CREATED);
            return null;
        }
        treeBuilder.addNodeToTree(root); /*prida root do stromu*/
        currentLevel.createdNodes = 1; /**/


        if(isTimeout()) { /*ak je timeout, vyhodi error*/
            logger.addLevelToPartialLog(currentLevel);
            throw new TimeoutException();
        }

        while (!treeBuilder.isTreeClosed()) {
            /*kym je strom otvoreny (asi kym su not processed nodes)?*/

            TreeNode node = treeBuilder.getNextNodeFromTree(); /*ziska nasledujuci node zo stromu*/


            if (node == null){
                StaticPrinter.debugPrint("[!!!] Null node!");
                continue;
            }

            assignLevelToNode(node); /*k nodu spaja level?*/

            if (depthLimitReached()) { /*max depyh error*/
                EventPublisher.publishGenericEvent(this, EventType.MAX_DEPTH_REACHED);
                break;
            }

            if(isTimeout()){ /*timeout error*/
                logger.addLevelToPartialLog(currentLevel);
                throw new TimeoutException();
            }

            EventPublisher.publishNodeEvent(this, EventType.PROCESSING_NODE, node);
            /*spracuje aktualny node*/
            node.processed = true; /*aktualny node je spracovany*/

            boolean canIterateNodeChildren = treeBuilder.startIteratingNodeChildren(node);
            /*ci sa daju deti nodu iterovat*/

            if (!canIterateNodeChildren){
                /*ak nie, zaznamenaj a continue na dalsi node*/
                EventPublisher.publishNodeEvent(this, EventType.CHILDLESS_NODE, node);
                continue;
            }

            //StaticPrinter.debugPrint("[TREE] Iterating child edges");

            while (!treeBuilder.noChildrenLeft()){
                /*kym ma node deti*/
                OWLAxiom child = treeBuilder.getNextChild(); /*get nasledujuce dieta*/

                /*if (child == null) {
                    StaticPrinter.debugPrint("[!!!] NULL CHILD");
                    continue;
                }*/

                EventPublisher.publishEdgeEvent(this, EventType.EDGE_CREATED, child);

                if(isTimeout()){
                    logger.addLevelToPartialLog(currentLevel);
                    throw new TimeoutException();
                }

                if(isInvalidPath(node, child)){
                    /*invalid path*/
                    EventPublisher.publishEdgeEvent(this, EventType.INVALID_PATH, child);
                    continue;
                }

                Explanation explanation = createPossibleExplanation(node, child);
                /*vytvor possible explanation*/

                path.clear(); /*vymaz cestu*/
                path.addAll(explanation.getAxioms()); /*pridaj explanations k ceste*/

                boolean pruneThisChild = treeBuilder.shouldPruneChildBranch(node, explanation);
                /*ci treba prune*/

                if (pruneThisChild){
                    path.clear();
                    continue;
                }

                boolean canReuseModel = Configuration.REUSE_OF_MODELS && modelManager.findReuseModelForPath(path);
                /*ci sa da znovu pouzit model*/
                if (!canReuseModel) {

                    if (isTimeout()){
                        logger.addLevelToPartialLog(currentLevel);
                        throw new TimeoutException();
                    }

                    int explanationsFound = nodeProcessor.findExplanations(
                            explanation, treeBuilder.shouldExtractModel()
                        );
                    //System.out.print("Number of explanations found = ");
                    //System.out.print(explanationsFound);
                    /*kolko vysvetleni nasiel*/
                    boolean shouldCloseNode = nodeProcessor.shouldCloseNode(explanationsFound);
                    /*ci treba uzavriet node*/
                    if (shouldCloseNode){
                        EventPublisher.publishNodeEvent(this, EventType.CLOSING_NODE, node);
                        path.clear();
                        continue;
                    }

                } else {

                    EventPublisher.publishGenericEvent(this, EventType.MODEL_REUSE);

                }

                if (isTimeout()){
                    logger.addLevelToPartialLog(currentLevel);
                    throw new TimeoutException();
                }
                //System.out.print("CHECkPOINT1");
                TreeNode childNode = treeBuilder.createChildNode(node, explanation);
                treeBuilder.addNodeToTree(childNode);
                //System.out.print("CHECkPOINT2");
                EventPublisher.publishNodeEvent(this, EventType.NODE_CREATED, childNode);
                path.clear();
                /*vytvor novy node a pridaj k stromu*/

            }

        }

        EventPublisher.publishGenericEvent(this, EventType.TREE_FINISHED);
        /*presiel cely strom*/
        path.clear();
        treeBuilder.resetLevel();
        logger.addLevelToPartialLog(currentLevel);

        return null;

    }

    private boolean isInvalidPath(TreeNode parent, OWLAxiom edgeLabel){
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

    protected boolean depthLimitReached(){
        return Configuration.DEPTH > 0 && currentDepth == Configuration.DEPTH;
    }

    private void assignLevelToNode(TreeNode node){

        if (node.depth != currentDepth) {

            treeBuilder.resetLevel();
            EventPublisher.publishGenericEvent(this, EventType.LEVEL_FINISHED);
            updateDepthIfNeeded(node);
            EventPublisher.publishGenericEvent(this, EventType.LEVEL_STARTED);

        }

        node.assignedLevel = currentLevel;

    }

    private void updateDepthIfNeeded(TreeNode node){
        if (node.depth > maxDepth) {
            maxDepth = node.depth;
            logger.addLevelToPartialLog(currentLevel);
            if (Configuration.PRINT_PROGRESS)
                updateProgress();
        }
        currentDepth = node.depth;
    }

    public Model findAndGetModelToReuse(){
        if (!modelManager.canReuseModel())
            modelManager.findReuseModelForPath(path);

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
