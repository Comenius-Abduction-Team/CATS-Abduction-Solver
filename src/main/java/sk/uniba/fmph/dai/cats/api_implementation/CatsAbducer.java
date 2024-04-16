package sk.uniba.fmph.dai.cats.api_implementation;

import jdk.nashorn.internal.runtime.regexp.joni.Config;
import sk.uniba.fmph.dai.abduction_api.abducible.IAbducibles;
import sk.uniba.fmph.dai.abduction_api.abducible.IAxiomAbducibles;
import sk.uniba.fmph.dai.abduction_api.abducible.IExplanationConfigurator;
import sk.uniba.fmph.dai.abduction_api.abducible.ISymbolAbducibles;
import sk.uniba.fmph.dai.abduction_api.exception.CommonException;
import sk.uniba.fmph.dai.abduction_api.exception.InvalidObservationException;
import sk.uniba.fmph.dai.abduction_api.exception.InvalidSolverParameterException;
import sk.uniba.fmph.dai.cats.algorithms.Algorithm;
import sk.uniba.fmph.dai.cats.algorithms.hst.HstHybridSolver;
import sk.uniba.fmph.dai.cats.algorithms.hybrid.HybridSolver;
import sk.uniba.fmph.dai.cats.common.Configuration;
import sk.uniba.fmph.dai.cats.logger.FileLogger;
import sk.uniba.fmph.dai.cats.models.Explanation;
import org.semanticweb.owlapi.model.*;
import sk.uniba.fmph.dai.abduction_api.abducer.IExplanation;
import sk.uniba.fmph.dai.abduction_api.abducer.IThreadAbducer;
import sk.uniba.fmph.dai.abduction_api.monitor.AbductionMonitor;

import sk.uniba.fmph.dai.cats.reasoner.ReasonerManager;
import sk.uniba.fmph.dai.cats.reasoner.ReasonerType;
import sk.uniba.fmph.dai.cats.timer.ThreadTimes;

import java.util.*;

public class CatsAbducer implements IThreadAbducer {

    private CatsAbducibles abducibles;
    private CatsExplanationConfigurator configurator = new CatsExplanationConfigurator();
    private final AbductionMonitor abductionMonitor = new AbductionMonitor();

    OWLOntology backgroundKnowledge;
    Set<OWLAxiom> observations = new HashSet<>();

    Set<IExplanation> explanations = new HashSet<>();
    String message = "";
    StringBuilder logs = new StringBuilder();

    double timeout = 0;
    int depth = 0;
    boolean pureMhs = false;
    boolean hst = false;
    Algorithm algorithm = Algorithm.MHS_MXP;
    boolean strictRelevance = true;

    boolean multithread = false;

    HybridSolver solver;
    ApiLoader loader;
    ReasonerManager reasonerManager;
    ThreadTimes timer;

    public boolean isMultithread() {
        return multithread;
    }

    public CatsAbducer(){
        FileLogger.initializeLogger();
    }

    public CatsAbducer(OWLOntology backgroundKnowledge, OWLAxiom observation)
    throws InvalidObservationException {
        setBackgroundKnowledge(backgroundKnowledge);
        setObservation(observation);
    }

    public CatsAbducer(OWLOntology backgroundKnowledge, Set<OWLAxiom> observation)
    throws InvalidObservationException {
        setBackgroundKnowledge(backgroundKnowledge);
        setObservation(observation);
    }

    void setExplanations(Collection<Explanation> explanations){
        this.explanations = new HashSet<>(explanations);
    }

    @Override
    public void setBackgroundKnowledge(OWLOntology ontology) {
        backgroundKnowledge = ontology;
    }

    @Override
    public OWLOntology getBackgroundKnowledge() {
        return backgroundKnowledge;
    }

    @Override
    public void setObservation(OWLAxiom axiom) throws InvalidObservationException {
        if (checkObservationType(axiom))
            observations = Collections.singleton(axiom);
        else
            throwInvalidObservationException(axiom);
    }

    private void throwInvalidObservationException(OWLAxiom axiom){
        throw new InvalidObservationException(axiom);
    }

    private boolean checkObservationType(OWLAxiom axiom){
        return true;
//        AxiomType<?> type = axiom.getAxiomType();
//        return  AxiomType.CLASS_ASSERTION == type ||
//                AxiomType.OBJECT_PROPERTY_ASSERTION == type ||
//                AxiomType.NEGATIVE_OBJECT_PROPERTY_ASSERTION == type;
    }

    @Override
    public void setObservation(Set<OWLAxiom> observation) throws InvalidObservationException {
        Set<OWLAxiom> validObservations = new HashSet<>();
        observation.forEach(axiom -> addObservationToSet(axiom, validObservations));
        this.observations = validObservations;
    }

    private void addObservationToSet(OWLAxiom axiom, Set<OWLAxiom> set) throws InvalidObservationException {
        if (checkObservationType(axiom))
            set.add(axiom);
        else throwInvalidObservationException(axiom);
    }

    @Override
    public Set<OWLAxiom> getObservation() {
        return observations;
    }

    @Override
    public void setTimeout(double seconds) {
        timeout = seconds;
    }

    @Override
    public double getTimeout() {
        return timeout;
    }

    @Override
    public void setSolverSpecificParameters(String s) {
        if (s.equals(""))
            return;
        String[] arguments = s.split("\\s+");
            for (int i = 0; i < arguments.length; i++){
                try {
                    switch (arguments[i]) {
                        case "-d":
                            int depth = Integer.parseInt(arguments[i + 1]);
                            setDepth(depth);
                            i++;
                            continue;
                        case "-mhs":
                            boolean pureMhs = Boolean.parseBoolean(arguments[i + 1]);
                            setPureMhs(pureMhs);
                            i++;
                            continue;
                        case "-sR":
                            boolean strictRelevance = Boolean.parseBoolean(arguments[i + 1]);
                            setStrictRelevance(strictRelevance);
                            i++;
                            continue;
                        default:
                            throw new InvalidSolverParameterException(arguments[i], "Unknown solver argument");
                    }
                } catch(NumberFormatException e){
                    throw new InvalidSolverParameterException(arguments[i+1], "Invalid integer value");
                } catch(ArrayIndexOutOfBoundsException e){
                    throw new InvalidSolverParameterException(arguments[i], "Missing parameter value");
                }
            }
    }

    @Override
    public void resetSolverSpecificParameters() {
        setDepth(0);
        setPureMhs(false);
        setStrictRelevance(true);
    }

    @Override
    public void solveAbduction() {
        clearResults();
        setupSolver();
        solve();
    }

    private void clearResults() {
        explanations = new HashSet<>();
        message = "";
        logs = new StringBuilder();
        abductionMonitor.clearMonitor();
    }

    private void setupSolver() {

        loader = new ApiLoader(this);
        ApiPrinter printer = new ApiPrinter(this);

        try {
            loader.initialize(ReasonerType.JFACT);
        } catch (Exception e){
            printer.logError("An error occurred while initialising the internal reasoner: ",e);
            return;
        }

        reasonerManager = new ReasonerManager(loader);

        ApiExplanationManager explanationManager = new ApiExplanationManager(loader, reasonerManager, this);
        ApiProgressManager progressManager = new ApiProgressManager(this);

        timer = new ThreadTimes(100);
        timer.start();

        setSolverConfiguration();

        if (algorithm.isHst())
            solver = new HstHybridSolver(timer, explanationManager, progressManager, printer);
        else
            solver = new HybridSolver(timer, explanationManager, progressManager, printer);

    }

    private void solve(){
        try {
            solver.solve(loader, reasonerManager);
        } catch (Throwable e) {
            new ApiPrinter(this).logError("An error occured while solving: ", e);
        }
    }

    @Override
    public Set<IExplanation> getExplanations() {
        return explanations;
    }

    @Override
    public String getOutputMessage() {
        return message;
    }

    @Override
    public String getFullLog() {
        return logs.toString();
    }

    private void setSolverConfiguration(){

        Configuration.STRICT_RELEVANCE = strictRelevance;
        Configuration.PRINT_PROGRESS = true;

        setDepthInConfiguration();
        setTimeoutInConfiguration();

        if (configurator == null)
            return;

        setExplanationConfiguration();
    }

    private void setDepthInConfiguration() {
        Configuration.DEPTH = depth;
    }

    private void setTimeoutInConfiguration() {
        Configuration.TIMEOUT = (long) timeout;
    }

    private void setExplanationConfiguration() {
        Configuration.LOOPING_ALLOWED = configurator.areLoopsAllowed();
        Configuration.ROLES_IN_EXPLANATIONS_ALLOWED = configurator.areRoleAssertionsAllowed();
        Configuration.NEGATION_ALLOWED = configurator.areComplementConceptsAllowed();
    }

    @Override
    public void setAbducibles(IAbducibles abducibles) {

        if (abducibles instanceof CatsAbducibles)
            this.abducibles = (CatsAbducibles) abducibles;

        else if (abducibles instanceof ISymbolAbducibles == abducibles instanceof IAxiomAbducibles)
            throw new CommonException("Abducible container type not compatible with abduction manager!");

        else if (abducibles instanceof ISymbolAbducibles)
            this.abducibles = ApiObjectConverter.convertSymbolAbducibles(abducibles);

        else
            this.abducibles = ApiObjectConverter.convertAxiomAbducibles(abducibles);

    }

    @Override
    public CatsAbducibles getAbducibles() {
        return abducibles;
    }

    @Override
    public IExplanationConfigurator getExplanationConfigurator() {
        return configurator;
    }

    @Override
    public void setExplanationConfigurator(IExplanationConfigurator configurator) {

        if (configurator instanceof CatsExplanationConfigurator){
            this.configurator = (CatsExplanationConfigurator) configurator;
            return;
        }

        else if (ApiObjectConverter.configuratorImplementsIncompatibleInterfaces(configurator))
            throw new CommonException("Explanation configurator type not compatible with abduction manager!");

        this.configurator = ApiObjectConverter.attemptConfiguratorConversion(configurator);

    }

    @Override
    public void run() {
        synchronized (abductionMonitor){
            multithread = true;
            solveAbduction();
            multithread = false;
        }
    }

    @Override
    public AbductionMonitor getAbductionMonitor() {
        return abductionMonitor;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public boolean isPureMhs() {
        return pureMhs;
    }

    public void setAlgorithm(Algorithm algorithm){
        this.algorithm = algorithm;
    }

    public void setPureMhs(boolean pureMhs) {
        this.pureMhs = pureMhs;
    }

    public void setHst(boolean hst){this.hst = hst;}

    public boolean isStrictRelevance() {
        return strictRelevance;
    }

    public void setStrictRelevance(boolean strictRelevance) {
        this.strictRelevance = strictRelevance;
    }

    void appendToLog(String message){
        logs.append(message);
        logs.append('\n');
    }

    public void setMessage(String message){
        this.message = message;
    }
}
