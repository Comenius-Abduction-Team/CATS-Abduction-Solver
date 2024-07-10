package sk.uniba.fmph.dai.cats.algorithms.hybrid;

import sk.uniba.fmph.dai.cats.algorithms.ISolver;
import com.google.common.collect.Iterables;
import sk.uniba.fmph.dai.cats.common.Configuration;
import sk.uniba.fmph.dai.cats.common.IPrinter;
import sk.uniba.fmph.dai.cats.common.StringFactory;
import sk.uniba.fmph.dai.cats.models.*;
import org.semanticweb.owlapi.model.*;
import sk.uniba.fmph.dai.cats.progress.IProgressManager;
import sk.uniba.fmph.dai.cats.reasoner.AxiomManager;
import sk.uniba.fmph.dai.cats.reasoner.ILoader;
import sk.uniba.fmph.dai.cats.reasoner.IReasonerManager;
import sk.uniba.fmph.dai.cats.timer.ThreadTimes;

import java.util.*;

/**
 * Base = knowledgeBase + negObservation
 * Literals = set of all literals / concepts with named individual except observation
 */

public class HybridSolver implements ISolver {

    protected ILoader loader;
    protected IReasonerManager reasonerManager;
    protected IAbducibleAxioms abducibleAxioms;
    protected ModelExtractor modelExtractor;
    protected final IExplanationManager explanationManager;
    protected final IProgressManager progressManager;
    protected SetDivider setDivider;
    protected Set<Set<OWLAxiom>> pathsInCertainDepth = new HashSet<>();

    public OWLOntology ontology;
    public List<ModelNode> models;
    public List<ModelNode> negModels;
    public List<OWLAxiom> assertionsAxioms;
    public List<OWLAxiom> negAssertionsAxioms;
    public Set<OWLAxiom> path = new HashSet<>();
    public Set<OWLAxiom> pathDuringCheckingMinimality;
    public Abducibles abducibles;
    protected int lastUsableModelIndex;
    public OWLAxiom negObservation;
    public ThreadTimes threadTimes;
    public long startTime;
    public Map<Integer, Double> levelTimes = new HashMap<>();
    public boolean checkingMinimalityWithQXP = false;
    protected IRuleChecker ruleChecker;
    protected int currentDepth = 0;
    protected int globalMin;

    protected int numberOfNodes = 0;

    public HybridSolver(ThreadTimes threadTimes,
                        IExplanationManager explanationManager, IProgressManager progressManager, IPrinter printer) {

        String info = String.join("\n", getInfo());
        printer.print("");
        printer.print(info);
        printer.print("");

        this.explanationManager = explanationManager;
        explanationManager.setSolver(this);

        this.progressManager = progressManager;

        this.threadTimes = threadTimes;
        this.startTime = System.currentTimeMillis();
    }

    public IExplanationManager getExplanationManager(){
        return explanationManager;
    }

    public List<String> getInfo() {
//        String optimizationQXP = "Optimization QXP: " + Configuration.CHECKING_MINIMALITY_BY_QXP;
//        String optimizationLongestConf = "Optimization Cached Conflicts - The Longest Conflict: " + Configuration.CACHED_CONFLICTS_LONGEST_CONFLICT;
//        String optimizationMedian = "Optimization Cached Conflicts - Median: " + Configuration.CACHED_CONFLICTS_MEDIAN;
        String roles = "Roles: " + Configuration.ROLES_IN_EXPLANATIONS_ALLOWED;
        String looping = "Looping allowed: " + Configuration.LOOPING_ALLOWED;
        String negation = "Negation: " +  Configuration.NEGATION_ALLOWED;
        String mhs_mode = "Algorithm: " + Configuration.ALGORITHM;
        String caching = "Abducible caching: " + Configuration.CACHE_ABDUCIBLES;
        String relevance = "Strict relevance: " + Configuration.STRICT_RELEVANCE;
        String depth = "Depth limit: ";
        if (Configuration.DEPTH > 0) depth += Configuration.DEPTH; else depth += "none";
        String timeout = "Timeout: ";
        if (Configuration.TIMEOUT > 0) timeout += Configuration.TIMEOUT; else timeout += "none";

        return Arrays.asList(
                roles, looping, negation, mhs_mode, caching, relevance, depth, timeout);
    }

    @Override
    public Collection<Explanation> getExplanations() {
        return explanationManager.getFinalExplanations();
    }

    @Override
    public void solve(ILoader loader, IReasonerManager reasonerManager) throws OWLOntologyStorageException, OWLOntologyCreationException {
        this.loader = loader;
        this.reasonerManager = reasonerManager;
        this.ontology = this.loader.getOriginalOntology();
        this.modelExtractor = new ModelExtractor(loader, reasonerManager, this);
        this.setDivider = new SetDivider(this);
        this.ruleChecker = new RuleChecker(loader, reasonerManager);

        negObservation = loader.getNegObservation().getOwlAxiom();
        this.abducibles = loader.getAbducibles();

        initialize();

        String message;

        if (!reasonerManager.isOntologyConsistent()) {
            message = "The observation is already entailed!";
            explanationManager.processExplanations(message);
        }

        else {
            reasonerManager.isOntologyWithLiteralsConsistent(abducibleAxioms.getAxioms(), ontology);
            trySolve();
        }
        if (Configuration.PRINT_PROGRESS)
            progressManager.updateProgress(100, "Abduction finished.");

    }

    protected void trySolve() throws OWLOntologyStorageException, OWLOntologyCreationException {
        String message = null;
        try {
            startSolving();
        } catch (Throwable e) {
            makeErrorAndPartialLog(e);
            message = "An error occured: " + e.getMessage();
            throw e;
        } finally {
            explanationManager.processExplanations(message);
            if (Configuration.DEBUG_PRINT)
                System.out.println("NUMBER OF NODES: " + numberOfNodes);
        }
    }

    protected void makeErrorAndPartialLog(Throwable e) {
        explanationManager.logError(e);

        Double time = threadTimes.getTotalUserTimeInSec();
        levelTimes.put(currentDepth, time);
        explanationManager.logExplanationsWithDepth(currentDepth, false, true, time);
        if(Configuration.ALGORITHM.usesMxp()){
            explanationManager.logExplanationsWithDepth(currentDepth + 1, false, true, time);
            explanationManager.logExplanationsWithLevel(currentDepth, false, true, time);
        }
    }

    protected void initialize() {

        setupCollections();
        addNegatedObservation();

        abducibleAxioms = new AxiomSet(createAbducibleAxioms());
        modelExtractor.initialiseAbducibles();
    }

    protected void setupCollections(){
        models = new ArrayList<>();
        negModels = new ArrayList<>();

        assertionsAxioms = new ArrayList<>();
        negAssertionsAxioms = new ArrayList<>();
    }

    protected void addNegatedObservation(){
        loader.getOntologyManager().addAxiom(ontology, loader.getNegObservation().getOwlAxiom());
        reasonerManager.addAxiomToOntology(loader.getNegObservation().getOwlAxiom());
    }

    protected Set<OWLAxiom> createAbducibleAxioms(){
        if(loader.isAxiomBasedAbduciblesOnInput()){
            Set<OWLAxiom> abduciblesWithoutObservation = abducibles.getAxiomBasedAbducibles();
            if (loader.isMultipleObservationOnInput()){
                if (Configuration.STRICT_RELEVANCE) {
                    loader.getObservation().getAxiomsInMultipleObservations().forEach(abduciblesWithoutObservation::remove);
                }
            } else {
                abduciblesWithoutObservation.remove(loader.getObservation().getOwlAxiom());
            }
            return abduciblesWithoutObservation;
        }

        for(OWLClass owlClass : abducibles.getClasses()){
            if (owlClass.isTopEntity() || owlClass.isBottomEntity()) continue;
            List<OWLAxiom> classAssertionAxiom = AxiomManager.createClassAssertionAxiom(loader, owlClass);
            for (int i = 0; i < classAssertionAxiom.size(); i++) {
                if (i % 2 == 0) {
                    assertionsAxioms.add(classAssertionAxiom.get(i));
                } else {
                    negAssertionsAxioms.add(classAssertionAxiom.get(i));
                }
            }
        }

        if(Configuration.ROLES_IN_EXPLANATIONS_ALLOWED){
            for(OWLObjectProperty objectProperty : abducibles.getRoles()){
                if (objectProperty.isTopEntity() || objectProperty.isBottomEntity()) continue;
                List<OWLAxiom> objectPropertyAssertionAxiom = AxiomManager.createObjectPropertyAssertionAxiom(loader, objectProperty);
                for (int i = 0; i < objectPropertyAssertionAxiom.size(); i++) {
                    if (i % 2 == 0) {
                        assertionsAxioms.add(objectPropertyAssertionAxiom.get(i));
                    } else {
                        negAssertionsAxioms.add(objectPropertyAssertionAxiom.get(i));
                    }
                }
            }
        }

        if (loader.isMultipleObservationOnInput()){
            if (Configuration.STRICT_RELEVANCE) {
                assertionsAxioms.removeAll(loader.getObservation().getAxiomsInMultipleObservations());
                negAssertionsAxioms.removeAll(loader.getObservation().getAxiomsInMultipleObservations());
            }
        } else {
            assertionsAxioms.remove(loader.getObservation().getOwlAxiom());
            negAssertionsAxioms.remove(loader.getObservation().getOwlAxiom());
        }

        Set<OWLAxiom> toAbd = new HashSet<>(assertionsAxioms);

        if(Configuration.NEGATION_ALLOWED)
            toAbd.addAll(negAssertionsAxioms);

        return toAbd;
    }

    protected void startSolving() throws OWLOntologyCreationException {
        if (Configuration.PRINT_PROGRESS)
            progressManager.updateProgress(0, "Abduction initialized.");

        currentDepth = 0;

        Queue<TreeNode> queue = new ArrayDeque<>();
        ModelNode root = createRoot();
        if (root == null)
            return;

        queue.add(root);

        if(isTimeout()) {
            makeTimeoutPartialLog();
            return;
        }

        while (!queue.isEmpty()) {
            TreeNode node = queue.poll();

            if(increaseDepth(node)){
                currentDepth++;
            }
            if(isTimeout()){
                makeTimeoutPartialLog();
                break;
            }

            ModelNode model = (ModelNode) node;

            if (Configuration.DEBUG_PRINT)
                System.out.println("*********\n" + "PROCESSING node: " + StringFactory.getRepresentation(model.data));

            if (depthLimitReached(model)) {
                break;
            }

            for (OWLAxiom child : model.data){

                if (child == null)
                    continue;

                if (Configuration.DEBUG_PRINT)
                    System.out.println("TRYING EDGE: " + StringFactory.getRepresentation(child));

                if(isTimeout()){
                    makeTimeoutPartialLog();
                    return;
                }

                //ak je axiom negaciou axiomu na ceste k vrcholu, alebo
                //ak axiom nie je v abducibles
                //nepokracujeme vo vetve
                if(isIncorrectPath(model, child)){
                    if (Configuration.DEBUG_PRINT)
                        System.out.println("INCORRECT PATH!");
                    continue;
                }

                //rovno pridame potencialne vysvetlenie
                Explanation explanation = createPossibleExplanation(model, child);

                path = new HashSet<>(explanation.getAxioms());

                if(canBePruned(explanation)){
                    if (Configuration.DEBUG_PRINT){
                        System.out.println("CAN BE PRUNED!");
                    }
                    path.clear();
                    continue;
                }

                int reuseIndex = -1;

                if (Configuration.REUSE_OF_MODELS)
                    reuseIndex = findReuseIndex();

                if (reuseIndex == -1) {

                    if (Configuration.DEBUG_PRINT){
                        System.out.println("Model was not reused.");
                    }

                    if(isTimeout()){
                        makeTimeoutPartialLog();
                        return;
                    }
                    if(!Configuration.ALGORITHM.usesMxp()){
                        if(!isOntologyConsistent()){

                            if (Configuration.DEBUG_PRINT){
                                System.out.println("inconsistent ontology!");
                            }

                            explanation.setDepth(explanation.getAxioms().size());
                            explanationManager.addPossibleExplanation(explanation);
                            path.clear();
                            continue;
                        }
                    } else {
                        if (!addNewExplanations()){
                            path.clear();
                            if(isTimeout()){
                                makeTimeoutPartialLog();
                                return;
                            }
                            if (Configuration.DEBUG_PRINT)
                                System.out.println("pruned by MXP");
                            continue;
                        }
                        if(isTimeout()){
                            makeTimeoutPartialLog();
                            return;
                        }
                    }
                }
                else {
                    explanationManager.setLengthOneExplanations(new ArrayList<>());
                }

                addNodeToTree(queue, explanation, model, reuseIndex);
            }
        }
        path.clear();

        if(!levelTimes.containsKey(currentDepth)){
            makePartialLog();
        }
        currentDepth = 0;
    }

    protected boolean depthLimitReached(ModelNode model){
        return Configuration.DEPTH > 0 && model.depth.equals(Configuration.DEPTH);
    }

    protected Explanation createPossibleExplanation(ModelNode model, OWLAxiom child){
        Explanation explanation = new Explanation();
        explanation.addAxioms(model.label);
        explanation.addAxiom(child);
        explanation.setAcquireTime(threadTimes.getTotalUserTimeInSec());
        explanation.setLevel(currentDepth);
        return explanation;
    }

    protected void makePartialLog() {
        Double time = threadTimes.getTotalUserTimeInSec();
        levelTimes.put(currentDepth, time);
        explanationManager.logExplanationsWithDepth(currentDepth, false, false, time);
        if(Configuration.ALGORITHM.usesMxp()){
            explanationManager.logExplanationsWithLevel(currentDepth, false, false, time);
        }
        pathsInCertainDepth = new HashSet<>();
    }

    protected ModelNode createRoot() {
        if( Configuration.ALGORITHM.usesMxp() ){

            runMxpInRoot();

        } else {

            if(!isOntologyConsistent()){
                return null;
            }

        }

        return createModelNodeFromExistingModel(true, null, null, findReuseIndex());
    }

    protected void runMxpInRoot(){
        Conflict conflict = getMergeConflict();
        for (Explanation e: conflict.getExplanations()){
            e.setDepth(e.getAxioms().size());
        }
        explanationManager.setPossibleExplanations(conflict.getExplanations());
    }

    protected void addToExplanations(Explanation explanation){
        explanation.setDepth(explanation.getAxioms().size());
        if(Configuration.CHECKING_MINIMALITY_BY_QXP){
            Explanation newExplanation = getMinimalExplanationByCallingQXP(explanation);
            explanationManager.addPossibleExplanation(newExplanation);
            if(Configuration.CACHED_CONFLICTS_MEDIAN){
                setDivider.addPairsOfLiteralsToTable(newExplanation);
            }
        } else {
            explanationManager.addPossibleExplanation(explanation);
            if(Configuration.CACHED_CONFLICTS_MEDIAN){
                setDivider.addPairsOfLiteralsToTable(explanation);
            }
        }
    }

    protected void addNodeToTree(Queue<TreeNode> queue, Explanation explanation, ModelNode parent, int reuseIndex){
        ModelNode newNode = createModelNodeFromExistingModel(false, explanation, parent.depth + 1, reuseIndex);
        if(newNode == null){
            path.clear();
            return;
        }
        if(Configuration.ALGORITHM.usesMxp()){
            newNode.addLengthOneExplanationsFromNode(parent);
            newNode.addLengthOneExplanations(explanationManager.getLengthOneExplanations());
        }

        queue.add(newNode);
        numberOfNodes++;

        if (Configuration.DEBUG_PRINT){
            System.out.println("Created node");
        }

        path.clear();
    }

    protected ModelNode createModelNodeFromExistingModel(boolean isRoot, Explanation explanation, Integer depth, int reuseIndex){

        if (reuseIndex < 0)
            reuseIndex = findReuseIndex();

        ModelNode modelNode = new ModelNode();
        if (reuseIndex >= 0){
            if(isRoot){
                modelNode.data = negModels.get(reuseIndex).data;
                modelNode.label = new ArrayList<>();
                modelNode.depth = 0;
            } else {
                modelNode.label = explanation.getAxioms();
                modelNode.data = negModels.get(reuseIndex).data;
                modelNode.data.removeAll(path);
                modelNode.depth = depth;
            }
        }
        if(modelNode.data == null || modelNode.data.isEmpty()){
            return null;
        }
        return modelNode;
    }

    protected boolean increaseDepth(TreeNode node){
        if (node.depth > currentDepth){
            makePartialLog();
            if (Configuration.PRINT_PROGRESS)
                progressManager.updateProgress(currentDepth, threadTimes.getTotalUserTimeInSec());
            if (Configuration.DEBUG_PRINT)
                System.out.println("NUMBER OF NODES: " + numberOfNodes);
            numberOfNodes = 0;
            return true;
        }
        return false;
    }

    protected boolean isTimeout(){
        if (Configuration.TIMEOUT > 0 && threadTimes.getTotalUserTimeInSec() > Configuration.TIMEOUT) {
            return true;
        }
        return false;
    }

    protected void makeTimeoutPartialLog() {
        Double time = threadTimes.getTotalUserTimeInSec();
        levelTimes.put(currentDepth, time);
        explanationManager.logExplanationsWithDepth(currentDepth, true, false, time);
        if(Configuration.ALGORITHM.usesMxp()){
            explanationManager.logExplanationsWithDepth(currentDepth + 1, true, false, time);
            explanationManager.logExplanationsWithLevel(currentDepth, true,false, time);
        }
    }

    protected boolean canBePruned(Explanation explanation) throws OWLOntologyCreationException {
        if (!ruleChecker.isMinimal(explanationManager.getPossibleExplanations(), explanation)){
            return true;
        }

        if(pathsInCertainDepth.contains(path)){
            return true;
        }
        pathsInCertainDepth.add(new HashSet<>(path));

        if(Configuration.CHECK_RELEVANCE_DURING_BUILDING_TREE_IN_MHS_MXP){
            if(!ruleChecker.isRelevant(explanation)){
                return true;
            }
        }

        if(!Configuration.ALGORITHM.usesMxp()){
            if(!ruleChecker.isRelevant(explanation)){
                return true;
            }
            if(!ruleChecker.isConsistent(explanation)){
                return true;
            }
        }

        if(Configuration.ALGORITHM.usesMxp()){
            if (ruleChecker.isExplanation(explanation)){
                addToExplanations(explanation);
                if (Configuration.DEBUG_PRINT)
                    System.out.println("EXPLANATION FOUND: " + explanation);
                return true;
            }
        }
        return false;
    }

    protected boolean isIncorrectPath(ModelNode model, OWLAxiom child){
        if (model.label.contains(AxiomManager.getComplementOfOWLAxiom(loader, child))){
            return true;
        }

        if (child.equals(loader.getObservation().getOwlAxiom())){
            return true;
        }

        return false;
    }

    protected Conflict getMergeConflict() {
        return findConflicts(abducibleAxioms.getAxioms());
    }

    protected List<Explanation> findExplanations(){

        Set<OWLAxiom> copy = new HashSet<>();

        List<OWLAxiom> lengthOne = explanationManager.getLengthOneExplanations();

        for (OWLAxiom a : abducibleAxioms.getAxioms()){
            if (path.contains(a))
                continue;
            if (lengthOne.contains(a))
                continue;
            copy.add(a);
        }

       if(Configuration.CACHED_CONFLICTS_LONGEST_CONFLICT){
           setDivider.setIndexesOfExplanations(explanationManager.getPossibleExplanationsSize());
       }
       Conflict conflict = findConflicts(copy);

       return conflict.getExplanations();
    }

    protected Conflict findConflicts(Set<OWLAxiom> axioms) {

        path.remove(negObservation);

        if (isTimeout()) {
            return new Conflict(new HashSet<>(), new ArrayList<>());
        }

        reasonerManager.addAxiomsToOntology(path);

        // if isConsistent(B ∪ C) then return [C, ∅];
        if (isOntologyWithLiteralsConsistent(axioms)) {
            return new Conflict(axioms, new ArrayList<>());
        }

        resetOntologyToOriginal();

        // if |C| = 1 then return [∅, {C}];
        if (axioms.size() == 1) {
            List<Explanation> explanations = new ArrayList<>();
            explanations.add(new Explanation(axioms, axioms.size(), currentDepth, threadTimes.getTotalUserTimeInSec()));
            return new Conflict(new HashSet<>(), explanations);
        }

        int indexOfExplanation = -1;
        if(Configuration.CACHED_CONFLICTS_LONGEST_CONFLICT){
            indexOfExplanation = setDivider.getIndexOfTheLongestAndNotUsedConflict();
        }

        // Split C into disjoint, non-empty sets C1 and C2
        List<AxiomSet> sets = setDivider.divideIntoSets(axioms);

        double median = setDivider.getMedian();

        //[C'1, Γ1] ← FINDCONFLICTS(B, C1)
        Conflict conflictC1 = findConflicts(sets.get(0).getAxioms());
        if (Configuration.CACHED_CONFLICTS_LONGEST_CONFLICT){
            setDivider.addIndexToIndexesOfExplanations(indexOfExplanation);
        } else if (Configuration.CACHED_CONFLICTS_MEDIAN){
            setDivider.setMedian(median);
        }

        //[C'2, Γ2] ← FINDCONFLICTS(B, C2)
        Conflict conflictC2 = findConflicts(sets.get(1).getAxioms());
        if (Configuration.CACHED_CONFLICTS_LONGEST_CONFLICT){
            setDivider.addIndexToIndexesOfExplanations(indexOfExplanation);
        } else if (Configuration.CACHED_CONFLICTS_MEDIAN){
            setDivider.setMedian(median);
        }

        // Γ ← Γ1 ∪ Γ2;
        List<Explanation> explanations = new LinkedList<>();
        explanations.addAll(conflictC1.getExplanations());
        explanations.addAll(conflictC2.getExplanations());

        // C'1 ∪ C'2
        Set<OWLAxiom> conflictLiterals = new HashSet<>();
        conflictLiterals.addAll(conflictC1.getAxioms());
        conflictLiterals.addAll(conflictC2.getAxioms());

        // while ¬isConsistent(C'1 ∪ C'2 ∪ B) do
        while (!isOntologyWithLiteralsConsistent(conflictLiterals)) {

            if ((Configuration.DEPTH < 1) && Configuration.TIMEOUT > 0)
                if (Configuration.PRINT_PROGRESS)
                    progressManager.updateProgress(currentDepth, threadTimes.getTotalUserTimeInSec());

            if (isTimeout()) break;

            // X ← GETCONFLICT(B ∪ C'2, C'2, C'1)
            path.addAll(conflictC2.getAxioms());
            Explanation X = getConflict(conflictC2.getAxioms(), conflictC1.getAxioms(), path);
            path.removeAll(conflictC2.getAxioms());

            // temp ← GETCONFLICT(B ∪ X, X, C'2)
            path.addAll(X.getAxioms());
            Explanation CS = getConflict(X.getAxioms(), conflictC2.getAxioms(), path);
            // removeAll(X) is inefficient if X is a list and its size is larger than the set itself
            X.getAxioms().forEach(path::remove);

            // CS ← X ∪ temp
            CS.getAxioms().addAll(X.getAxioms());

            // C'1 ← C'1 \ {α} where α ∈ X
            Set<OWLAxiom> c1Axioms = conflictC1.getAxioms();
            conflictLiterals.removeAll(c1Axioms);

            // toto funguje na 100% iba ak je C'1 nadmnozinou X
            //X.getAxioms().stream().findAny().ifPresent(axiom -> conflictC1.getAxioms().remove(axiom));

            for (OWLAxiom a : X.getAxioms()){
                if (c1Axioms.contains(a)){
                    c1Axioms.remove(a);
                    break;
                }
            }

            conflictLiterals.addAll(c1Axioms);

            if (explanations.contains(CS) || isTimeout()) {
                break;
            }

            Explanation newExplanation = CS;
            if(Configuration.CHECKING_MINIMALITY_BY_QXP){
                newExplanation = getMinimalExplanationByCallingQXP(CS);
            }
            explanations.add(newExplanation);
            if(Configuration.CACHED_CONFLICTS_MEDIAN){
                setDivider.addPairsOfLiteralsToTable(newExplanation);
            }
        }

        return new Conflict(conflictLiterals, explanations);
    }

    protected Explanation getConflict(Collection<OWLAxiom> axioms, Set<OWLAxiom> literals, Set<OWLAxiom> actualPath) {

        if (isTimeout()) {
            return new Explanation();
        }

        // if D != ∅ ∧ ¬isConsistent(B) then return ∅;
        if (!axioms.isEmpty() && !isOntologyConsistent()) {
            return new Explanation();
        }

        // if |C| = 1 then return C;
        if (literals.size() == 1) {
            return new Explanation(literals, 1, currentDepth, threadTimes.getTotalUserTimeInSec());
        }

        // Split C into disjoint, non-empty sets
        List<AxiomSet> sets = setDivider.divideIntoSetsWithoutCondition(literals);

        actualPath.addAll(sets.get(0).getAxioms());
        Explanation D2 = getConflict(sets.get(0).getAxioms(), sets.get(1).getAxioms(), actualPath);
        actualPath.removeAll(sets.get(0).getAxioms());

        actualPath.addAll(D2.getAxioms());
        Explanation D1 = getConflict(D2.getAxioms(), sets.get(0).getAxioms(), actualPath);
        actualPath.removeAll(D2.getAxioms());

        Set<OWLAxiom> conflicts = new HashSet<>();
        conflicts.addAll(D1.getAxioms());
        conflicts.addAll(D2.getAxioms());

        return new Explanation(conflicts, conflicts.size(), currentDepth, threadTimes.getTotalUserTimeInSec());
    }

    protected int findReuseIndex(){

        for (int i = models.size()-1; i >= 0 ; i--){
            if (models.get(i).data.containsAll(path)){
                lastUsableModelIndex = i;
                return i;
            }
        }
        return -1;
    }

    protected boolean addNewExplanations(){
        List<Explanation> newExplanations = findExplanations();
        explanationManager.setLengthOneExplanations(new ArrayList<>());
        for (Explanation explanation : newExplanations){
            if (explanation.getAxioms().size() == 1){
                //explanationManager.addLengthOneExplanation(explanation.getOwlAxioms().stream().findFirst().orElse(null));
                explanationManager.addLengthOneExplanation(Iterables.get(explanation.getAxioms(), 0));
            }
            explanation.addAxioms(path);
            if (ruleChecker.isMinimal(explanationManager.getPossibleExplanations(), explanation)){
                Explanation newExplanation = explanation;
                if(Configuration.CHECKING_MINIMALITY_BY_QXP){
                    newExplanation = getMinimalExplanationByCallingQXP(explanation);
                }
                newExplanation.setDepth(newExplanation.getAxioms().size());
                explanationManager.addPossibleExplanation(newExplanation);
                if(Configuration.CACHED_CONFLICTS_MEDIAN){
                    setDivider.addPairsOfLiteralsToTable(newExplanation);
                }
            }
        }
        if (newExplanations.size() == explanationManager.getLengthOneExplanationsSize()){
            return false;
        }
        return !newExplanations.isEmpty();
    }

    protected boolean isOntologyWithLiteralsConsistent(Collection<OWLAxiom> axioms){
        path.addAll(axioms);
        boolean isConsistent = isOntologyConsistent();
        path.removeAll(axioms);
        return isConsistent;
    }

    protected boolean isOntologyConsistent(){
        ModelNode node = modelExtractor.getNegModelByOntology();
        return node.modelIsValid;
    }

    public Explanation getMinimalExplanationByCallingQXP(Explanation explanation){
        Set<OWLAxiom> potentialExplanations = new HashSet<>(explanation.getAxioms());
        if(path != null){
            potentialExplanations.addAll(path);
        }

        checkingMinimalityWithQXP = true;
        pathDuringCheckingMinimality = new HashSet<>();
        Explanation newExplanation = getConflict(new HashSet<>(), potentialExplanations, pathDuringCheckingMinimality);
        checkingMinimalityWithQXP = false;
        pathDuringCheckingMinimality = new HashSet<>();

        return newExplanation;
    }

    public void resetOntologyToOriginal(){
        reasonerManager.resetOntology(loader.getOriginalOntology().axioms());
    }

}