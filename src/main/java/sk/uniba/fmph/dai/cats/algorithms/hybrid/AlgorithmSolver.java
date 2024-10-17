package sk.uniba.fmph.dai.cats.algorithms.hybrid;

import org.semanticweb.owlapi.model.OWLAxiom;
import sk.uniba.fmph.dai.cats.algorithms.Algorithm;
import sk.uniba.fmph.dai.cats.algorithms.rctree.RcTreeBuilder;
import sk.uniba.fmph.dai.cats.common.Configuration;
import sk.uniba.fmph.dai.cats.common.IPrinter;
import sk.uniba.fmph.dai.cats.common.StringFactory;
import sk.uniba.fmph.dai.cats.data.Explanation;
import sk.uniba.fmph.dai.cats.model.Model;
import sk.uniba.fmph.dai.cats.model.ModelManager;
import sk.uniba.fmph.dai.cats.progress.ProgressManager;
import sk.uniba.fmph.dai.cats.reasoner.Loader;
import sk.uniba.fmph.dai.cats.timer.ThreadTimer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class AlgorithmSolver {

    public Loader loader;
    protected ModelManager modelManager;
    public final ExplanationManager explanationManager;
    protected final ProgressManager progressManager;
    public RuleChecker ruleChecker;
    protected SetDivider setDivider;
    protected IPrinter printer;
    protected final TimeManager timer;
    protected final ConsistencyChecker consistencyChecker;

    // COLLECTIONS
    final public Set<OWLAxiom> path = new HashSet<>();
    protected final Set<Set<OWLAxiom>> pathsInCertainDepth = new HashSet<>();
    protected IAbducibleAxioms abducibleAxioms;

    // INTEGERS
    protected int currentDepth = 0;
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

    public Collection<Explanation> getExplanations() {
        return explanationManager.getFinalExplanations();
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
        }

        else {
            loader.reasonerManager.isOriginalOntologyConsistentWithLiterals(abducibleAxioms.getAxioms());
            try {
                startSolving();
            } catch (Throwable e) {
                makeErrorAndPartialLog(e);
                message = "An error occured: " + e.getMessage();
                throw (e);
            } finally {
                explanationManager.processExplanations(message);
                if (Configuration.DEBUG_PRINT){}
                    //System.out.println("NUMBER OF NODES: " + numberOfNodes);
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
            makeTimeoutPartialLog();
            return;
        }

        while (!treeBuilder.isTreeClosed()) {

            TreeNode node = treeBuilder.getNextNodeFromTree();

            if (node == null)
                continue;

            if(increaseDepth(node)){
                currentDepth++;
            }

            if (depthLimitReached(node) || node.closed) {
                break;
            }

            if(isTimeout()){
                makeTimeoutPartialLog();
                break;
            }

            if (Configuration.DEBUG_PRINT)
                System.out.println("*********\n" + "[TREE] PROCESSING node: "
                        + StringFactory.getRepresentation(node.model.getNegatedData()));

            boolean canIterateNodeChildren = treeBuilder.startIteratingNodeChildren(node);
            if (!canIterateNodeChildren){
                if (Configuration.DEBUG_PRINT)
                    System.out.println("[TREE] NO CHILDREN TO ITERATE!");
                continue;
            }

            while (!treeBuilder.noChildrenLeft()){

                OWLAxiom child = treeBuilder.getNextChild();

                if (child == null) {
                    if (Configuration.DEBUG_PRINT)
                        System.out.println("[!!!] NULL CHILD");
                    continue;
                }

//                if (child.getClassesInSignature().stream().findFirst().orElse(null).toString().contains("Cavity")
//                        && currentDepth == 1){
//                    System.out.println("BREAKPOINT HERE FOR DEBUGGING PURPOSES");
//                }

                if (Configuration.DEBUG_PRINT)
                    System.out.println("[TREE] TRYING EDGE: " + StringFactory.getRepresentation(child));

                if(isTimeout()){
                    makeTimeoutPartialLog();
                    return;
                }

                //ak je axiom negaciou axiomu na ceste k vrcholu, alebo
                //ak axiom nie je v abducibles
                //nepokracujeme vo vetve
                if(treeBuilder.isIncorrectPath(node.label, child)){
                    if (Configuration.DEBUG_PRINT)
                        System.out.println("[PRUNING] INCORRECT PATH!");
                    continue;
                }

                //rovno pridame potencialne vysvetlenie
                Explanation explanation = createPossibleExplanation(node, child);

                path.clear();
                path.addAll(explanation.getAxioms());
                //path = new HashSet<>(explanation.getAxioms());

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

                    if(isTimeout()){
                        makeTimeoutPartialLog();
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

        if (Configuration.DEBUG_PRINT)
            System.out.println("[TREE] Finished iterating the tree.");

        path.clear();

        if(!timer.levelHasTime(currentDepth)){
            makePartialLog();
        }

        currentDepth = 0;

    }

    private Explanation createPossibleExplanation(TreeNode node, OWLAxiom child){
        Explanation explanation = new Explanation();
        explanation.addAxioms(node.label);
        explanation.addAxiom(child);
        explanation.lastAxiom = child;
        explanation.setAcquireTime(timer.getTime());
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
        return new Explanation(axioms, axioms.size(), currentDepth, timer.getTime());
    }

    boolean isPathAlreadyStored(){
        return pathsInCertainDepth.contains(path);
    }

    void storePath(){
        pathsInCertainDepth.add(new HashSet<>(path));
    }

    protected boolean increaseDepth(TreeNode node){
        if (node.depth > currentDepth){
            makePartialLog();
            if (Configuration.PRINT_PROGRESS)
                updateProgress();
            if (Configuration.DEBUG_PRINT){}
                //System.out.println("NUMBER OF NODES: " + numberOfNodes);
            numberOfNodes = 0;
            return true;
        }
        return false;
    }

    protected boolean depthLimitReached(TreeNode node){
        return Configuration.DEPTH > 0 && node.depth.equals(Configuration.DEPTH);
    }

    //TODO TOTO TREBA ODPRATAT DO INEJ TRIEDY
    protected void makeErrorAndPartialLog(Throwable e) {
        explanationManager.logError(e);

        double time = timer.getTime();
        timer.setTimeForLevel(time, currentDepth);

        explanationManager.logExplanationsWithDepth(currentDepth, false, true, time);
        if(Configuration.ALGORITHM.usesMxp()){
            explanationManager.logExplanationsWithDepth(currentDepth + 1, false, true, time);
            explanationManager.logExplanationsWithLevel(currentDepth, false, true, time);
        }
    }

    protected void makePartialLog() {
        Double time = timer.getTime();
        timer.setTimeForLevel(time, currentDepth);
        explanationManager.logExplanationsWithDepth(currentDepth, false, false, time);
        if(Configuration.ALGORITHM.usesMxp()){
            explanationManager.logExplanationsWithLevel(currentDepth, false, false, time);
        }
        pathsInCertainDepth.clear();
    }

    protected void makeTimeoutPartialLog() {
        Double time = timer.getTime();
        timer.setTimeForLevel(time, currentDepth);
        explanationManager.logExplanationsWithDepth(currentDepth, true, false, time);
        if(Configuration.ALGORITHM.usesMxp()){
            explanationManager.logExplanationsWithDepth(currentDepth + 1, true, false, time);
            explanationManager.logExplanationsWithLevel(currentDepth, true,false, time);
        }
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
        progressManager.updateProgress(currentDepth, timer.getTime());
    }


}
