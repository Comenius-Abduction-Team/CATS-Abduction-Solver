package sk.uniba.fmph.dai.cats.algorithms.hybrid;

import org.semanticweb.owlapi.model.OWLAxiom;
import sk.uniba.fmph.dai.cats.algorithms.Algorithm;
import sk.uniba.fmph.dai.cats.algorithms.rctree.RcTreeBuilder;
import sk.uniba.fmph.dai.cats.common.Configuration;
import sk.uniba.fmph.dai.cats.common.IPrinter;
import sk.uniba.fmph.dai.cats.common.StringFactory;
import sk.uniba.fmph.dai.cats.data.Explanation;
import sk.uniba.fmph.dai.cats.data_processing.ExplanationManager;
import sk.uniba.fmph.dai.cats.data_processing.ExplanationLogger;
import sk.uniba.fmph.dai.cats.data_processing.LevelStats;
import sk.uniba.fmph.dai.cats.data_processing.TreeStats;
import sk.uniba.fmph.dai.cats.model.InsertSortModelManager;
import sk.uniba.fmph.dai.cats.model.Model;
import sk.uniba.fmph.dai.cats.model.ModelManager;
import sk.uniba.fmph.dai.cats.progress.ProgressManager;
import sk.uniba.fmph.dai.cats.reasoner.AxiomManager;
import sk.uniba.fmph.dai.cats.reasoner.Loader;
import sk.uniba.fmph.dai.cats.timer.ThreadTimer;
import sk.uniba.fmph.dai.cats.timer.TimeManager;

import java.util.ArrayList;
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

    protected TreeBuilder treeBuilder;
    public NodeProcessor nodeProcessor;

    public final TreeStats stats;
    public LevelStats currentLevelStats;

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

        stats = new TreeStats();

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
            explanationManager.processExplanations(message, stats);
            logger.logMessage(Configuration.getInfo(), message);
            if (Configuration.PRINT_PROGRESS)
                progressManager.updateProgress(100, "Abduction finished.");
        }


        loader.reasonerManager.isOriginalOntologyConsistentWithLiterals(abducibleAxioms.getAxioms());

//        try {
//            startSolving();
//        } catch (Throwable e) {
//            logger.makeErrorAndPartialLog(currentDepth, e);
//            message = "An error occured: " + e.getMessage();
//            throw (e);
//        } finally {
//            timer.setEndTime();
//            explanationManager.processExplanations(message);
//        }


        Future<Void> future = null;
        ExecutorService executor = Executors.newSingleThreadExecutor();

        try {

            Callable<Void> task = this::startSolving;
            future = executor.submit(task);

            if (Configuration.TIMEOUT > 0)
                future.get(Configuration.TIMEOUT, TimeUnit.SECONDS);
            else
                future.get();
        }  catch (TimeoutException e) {

                future.cancel(true);
                logger.makeTimeoutPartialLog(currentDepth);

        }   catch (Throwable e){

                if (!(future == null))
                    future.cancel(true);
                message = "An error occured: " + e.getMessage();
                logger.makeErrorAndPartialLog(currentDepth, e);
                printer.logError("An error occurred:", e);
                e.printStackTrace();

        } finally {

            executor.shutdown();
            if (stats.getCurrentLevelStats().finish == 0){
                stats.getCurrentLevelStats().finish = timer.getCurrentTime();
            }
            if (Configuration.PRINT_PROGRESS)
                progressManager.updateProgress(100, "Abduction finished.");

//            try {
//                while (executor.awaitTermination(1, TimeUnit.MILLISECONDS) == false) {}
//            } catch (InterruptedException ignored){}

            stats.filteringStart = timer.getCurrentTime();
            nodeProcessor.postProcessExplanations();
            stats.filteringEnd = timer.getCurrentTime();

            timer.setEndTime();
            explanationManager.processExplanations(message, stats);
            System.out.println(stats);
        }

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

    protected Void startSolving() {

        currentDepth = 0;
        currentLevelStats = stats.getLevelStats(currentDepth);
        currentLevelStats.start = timer.getCurrentTime();

        TreeNode root = treeBuilder.createRoot();
        if (root == null) {
            if (Configuration.DEBUG_PRINT)
                System.out.println("[!!!] ROOT COULD NOT BE CREATED!");
            return null;
        }
        treeBuilder.addNodeToTree(root);
        currentLevelStats.created_nodes = 1;

        if(isTimeout()) {
            logger.makeTimeoutPartialLog(currentDepth);
            return null;
        }

//        currentLevelStats.start = timer.getCurrentTime();
//        currentLevelStats.finish = timer.getCurrentTime();
//        currentDepth = 1

        while (!treeBuilder.isTreeClosed()) {

            TreeNode node = treeBuilder.getNextNodeFromTree();

            if (node == null){
                if (Configuration.DEBUG_PRINT)
                    System.out.println("[!!!] Null node!");
                continue;
            }

            updateDepthIfNeeded(node);

            if (depthLimitReached()) {
                break;
            }

            if(isTimeout()){
                logger.makeTimeoutPartialLog(currentDepth);
                break;
            }

            if (Configuration.DEBUG_PRINT)
                System.out.println("*********\n" + "[TREE] PROCESSING node: " + node);

            boolean canIterateNodeChildren = treeBuilder.startIteratingNodeChildren(node);

            if (!canIterateNodeChildren){
                if (Configuration.DEBUG_PRINT)
                    System.out.println("[TREE] NO CHILDREN TO ITERATE!");
                continue;
            }

            if (Configuration.DEBUG_PRINT)
                System.out.println("[TREE] Iterating child edges");

            node.processed = true;
            stats.getCurrentLevelStats().processed_nodes += 1;

            while (!treeBuilder.noChildrenLeft()){

                OWLAxiom child = treeBuilder.getNextChild();

                if (child == null) {
                    if (Configuration.DEBUG_PRINT)
                        System.out.println("[!!!] NULL CHILD");
                    continue;
                }

                if (Configuration.DEBUG_PRINT)
                    System.out.println("[TREE] TRYING EDGE: " + StringFactory.getRepresentation(child));

                stats.getCurrentLevelStats().created_edges += 1;

                if(isTimeout()){
                    logger.makeTimeoutPartialLog(currentDepth);
                    return null;
                }

                //ak je axiom negaciou axiomu na ceste k vrcholu, alebo
                //ak axiom nie je v abducibles
                //nepokracujeme vo vetve
                if(isIncorrectPath(node, child)){
                    if (Configuration.DEBUG_PRINT)
                        System.out.println("[PRUNING] INCORRECT PATH!");
                    continue;
                }

                //rovno pridame potencialne vysvetlenie
                Explanation explanation = createPossibleExplanation(node, child);

                path.clear();
                path.addAll(explanation.getAxioms());

                boolean pruneThisChild = treeBuilder.pruneNode(node, explanation);

                if (pruneThisChild){
                    if (Configuration.DEBUG_PRINT)
                        System.out.println("[PRUNING] NODE CLOSED!");
                    stats.getLevelStats(currentDepth).pruned_edges += 1;
                    path.clear();
                    continue;
                }

                if (Configuration.REUSE_OF_MODELS)
                    modelManager.findReuseModelForPath(path);

                if (Configuration.REUSE_OF_MODELS && modelManager.canReuseModel()) {
                    explanationManager.setLengthOneExplanations(new ArrayList<>());
                    stats.getLevelStats(currentDepth).reused += 1;
                    if (Configuration.DEBUG_PRINT)
                        System.out.println("[MODEL] Model was reused.");
                }
                else {

                    if (Configuration.DEBUG_PRINT){
                        System.out.println("[MODEL] Model was not reused.");
                    }

//                    boolean pruneThisChild = treeBuilder.pruneNode(node, explanation);
//
//                    if (pruneThisChild){
//                        if (Configuration.DEBUG_PRINT)
//                            System.out.println("[PRUNING] NODE CLOSED!");
//                        stats.getLevelStats(currentDepth).pruned += 1;
//                        path.clear();
//                        continue;
//                    }

                    if (isTimeout()){
                        logger.makeTimeoutPartialLog(currentDepth);
                        return null;
                    }

                    if (treeBuilder.closeExplanation(explanation)){
                        //System.out.println(currentDepth);
                        path.clear();
                        continue;
                    }
                }

                treeBuilder.addNodeToTree(treeBuilder.createChildNode(node, explanation));
                stats.getLevelStats(currentDepth).created_nodes += 1;

                if (Configuration.DEBUG_PRINT){
                    System.out.println("[TREE] Created node");
                }

                path.clear();

            }

        }

        currentLevelStats.finish = timer.getCurrentTime();

        if (Configuration.DEBUG_PRINT) {
            System.out.println("[TREE] Finished iterating the tree.");
        }

        //System.out.println(stats.getTotalNodeCount());
        //System.out.println(stats);
        //System.out.println(stats.getTotalPrunedCount());


        path.clear();

        if(!timer.levelHasTime(currentDepth)){
            logger.makePartialLog(currentDepth);
            pathsInCertainDepth.clear();
        }

        currentDepth = 1;

        return null;

    }

    private boolean isIncorrectPath(TreeNode parent, OWLAxiom edgeLabel){
        return parent.path.contains(AxiomManager.getComplementOfOWLAxiom(loader, edgeLabel)) ||
                edgeLabel.equals(loader.getObservationAxiom());
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
        return false;
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

    protected boolean depthLimitReached(){
        return Configuration.DEPTH > 0 && currentDepth == Configuration.DEPTH;
    }

    private void updateDepthIfNeeded(TreeNode node){

        if (node.depth != currentDepth) {
            pathsInCertainDepth.clear();
        }
        else return;

        if (node.depth > maxDepth) {
            maxDepth = node.depth;
            currentLevelStats.finish = timer.getCurrentTime();
            logger.makePartialLog(currentDepth);
            if (Configuration.PRINT_PROGRESS)
                updateProgress();
        }

        currentDepth = node.depth;
        currentLevelStats = stats.getLevelStats(currentDepth);
        currentLevelStats.start = timer.getCurrentTime();

        if (Configuration.DEBUG_PRINT)
            System.out.println("[TREE] entering depth " + node.depth);
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
