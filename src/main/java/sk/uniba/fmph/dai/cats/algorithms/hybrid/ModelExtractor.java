package sk.uniba.fmph.dai.cats.algorithms.hybrid;
import sk.uniba.fmph.dai.cats.common.Configuration;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.knowledgeexploration.OWLKnowledgeExplorerReasoner;
import org.semanticweb.owlapi.search.EntitySearcher;
import sk.uniba.fmph.dai.cats.reasoner.AxiomManager;
import sk.uniba.fmph.dai.cats.reasoner.ILoader;
import sk.uniba.fmph.dai.cats.reasoner.IReasonerManager;
import java.util.*;

import static java.util.stream.Collectors.toSet;

public class ModelExtractor {

    private ILoader loader;
    private IReasonerManager reasonerManager;
    private HybridSolver solver;
    private OWLOntologyManager ontologyManager;

    private Set<OWLAxiom> abducibles;
    //private HashMap<Integer,OWLAxiom> abducibles;

    public ModelExtractor(ILoader loader, IReasonerManager reasonerManager, HybridSolver solver){
        this.loader = loader;
        this.reasonerManager = reasonerManager;
        this.solver = solver;
        this.ontologyManager = OWLManager.createOWLOntologyManager();
    }

    public void initialiseAbducibles(){
//        abducibles = new HashMap<>();
//        for (OWLAxiom axiom : solver.abducibleAxioms.getAxioms()){
//            abducibles.put(axiom.hashCode(), axiom);
//        }
        abducibles = solver.abducibleAxioms.getAxioms();
    }

    public ModelNode getNegModelByOntology(){  // mrozek
        OWLDataFactory dfactory = ontologyManager.getOWLDataFactory();
        ModelNode negModelNode = new ModelNode();
        ModelNode modelNode = new ModelNode();
        Set<OWLAxiom> negModelSet = new HashSet<>();
        Set<OWLAxiom> modelSet = new HashSet<>();

        if(!isOntologyConsistentWithPath()){
            modelNode.modelIsValid = false;
            negModelNode.modelIsValid = false;
            return modelNode;
        }

//        System.out.println("MODEL");
        ArrayList<OWLNamedIndividual> individualArray;
        if(loader.isAxiomBasedAbduciblesOnInput()){
            individualArray = new ArrayList<>(loader.getOntology().getIndividualsInSignature());
        } else {
            individualArray = new ArrayList<>(solver.abducibles.getIndividuals());
        }

        for (OWLNamedIndividual ind : individualArray) {
            assignTypesToIndividual(dfactory, ind, negModelSet, modelSet);
        }
        if (Configuration.ROLES_IN_EXPLANATIONS_ALLOWED) {
            for (OWLNamedIndividual ind : individualArray) {
                assignRolesToIndividual(dfactory, ind, individualArray, negModelSet, modelSet);
            }
        }

        deletePathFromOntology();

        if(loader.isAxiomBasedAbduciblesOnInput()){
            modelSet.retainAll(solver.abducibles.getAxiomBasedAbducibles());
            modelNode.data = modelSet;
            negModelSet.retainAll(solver.abducibles.getAxiomBasedAbducibles());
            negModelNode.data = negModelSet;

        } else {
            modelNode.data = modelSet;
            negModelNode.data = negModelSet;
        }

        solver.lastUsableModelIndex = solver.models.indexOf(modelNode);

        if (!negModelNode.data.isEmpty() && solver.lastUsableModelIndex == -1) {
            solver.lastUsableModelIndex = solver.models.size();
            addModel(modelNode, negModelNode);
        }
        return negModelNode;
    }

    public boolean isOntologyConsistentWithPath(){
        if(solver.checkingMinimalityWithQXP) {
            return isOntologyConsistentWithPath(solver.pathDuringCheckingMinimality);
        }
        else {
            return isOntologyConsistentWithPath(solver.path);
        }
    }

    public boolean isOntologyConsistentWithPath(Set<OWLAxiom> path){
        if (path != null) {
            if(loader.isMultipleObservationOnInput()){
                for(OWLAxiom axiom : loader.getObservation().getAxiomsInMultipleObservations()){
                    path.remove(AxiomManager.getComplementOfOWLAxiom(loader, axiom));
                }
            } else {
                path.remove(solver.negObservation);
            }
            reasonerManager.addAxiomsToOntology(path);
            if (!reasonerManager.isOntologyConsistent()){
                solver.resetOntologyToOriginal();
                return false;
            }
        }
        return true;
    }

    public void assignTypesToIndividual(OWLDataFactory dfactory, OWLNamedIndividual ind, Set<OWLAxiom> negModelSet, Set<OWLAxiom> modelSet){
        //complex concepts from original ontology
        Set<OWLClassExpression> ontologyTypes = EntitySearcher.getTypes(ind, solver.ontology).collect(toSet());

        Set<OWLClassExpression> knownTypes = new HashSet<>(); //concepts assigned to ind from original ontology
        Set<OWLClassExpression> knownNotTypes = new HashSet<>(); //neg concepts assigned to ind from original ontology
        divideTypesAccordingOntology(ontologyTypes, knownTypes, knownNotTypes);

        Set<OWLClassExpression> newNotTypes = classSet2classExpSet(solver.ontology.classesInSignature().collect(toSet()));
        newNotTypes.remove(dfactory.getOWLThing());
        newNotTypes.removeAll(knownNotTypes);

        OWLObjectOneOf individual = ontologyManager.getOWLDataFactory().getOWLObjectOneOf(ind);

//        System.out.println(ind);
//        System.out.println(individual);
//        System.out.println();

        OWLKnowledgeExplorerReasoner.RootNode rootNode = loader.getReasoner().getRoot(individual);
        Set<OWLClassExpression> foundTypes = loader.getReasoner().getObjectLabel(rootNode,false)
                .entities()
                .collect(toSet());

        newNotTypes.removeAll(foundTypes);
        foundTypes.removeAll(knownTypes);

        addAxiomsToModelsAccordingTypes(dfactory, negModelSet, modelSet, foundTypes, newNotTypes, ind);
    }

    public void assignRolesToIndividual(OWLDataFactory dfactory, OWLNamedIndividual ind, ArrayList<OWLNamedIndividual> individuals, Set<OWLAxiom> negModelSet, Set<OWLAxiom> modelSet) {
        Set<OWLAxiom> ontologyPropertyAxioms = solver.ontology.axioms()
                .filter(a -> a.isOfType(AxiomType.OBJECT_PROPERTY_ASSERTION)
                        && ((OWLObjectPropertyAssertionAxiom)a).getSubject() == ind)
                .collect(toSet()); //object properties where ind is a subject -> objectProperty(ind,x)

        ontologyPropertyAxioms.addAll(solver.ontology.axioms()
                .filter(a -> a.isOfType(AxiomType.NEGATIVE_OBJECT_PROPERTY_ASSERTION)
                        && ((OWLNegativeObjectPropertyAssertionAxiom)a).getSubject() == ind)
                .collect(toSet())); //add neg object properties where ind is a subject -> not(objectProperty(ind,x))

        Set<OWLObjectPropertyAssertionAxiom> known = new HashSet<>();
        Set<OWLObjectPropertyAssertionAxiom> knownNot = new HashSet<>();
        dividePropertyAxiomsAccordingOntology(ontologyPropertyAxioms, known, knownNot);
        Set<OWLAxiom> newNot = getAllRolesAssertionWithIndividual(ind);

        newNot.removeAll(knownNot);

        Set<OWLObjectPropertyAssertionAxiom> found = new HashSet<>();
        List<OWLKnowledgeExplorerReasoner.RootNode> nodes = new ArrayList<>();

        for (OWLNamedIndividual n : individuals) {
            OWLObjectOneOf i = ontologyManager.getOWLDataFactory().getOWLObjectOneOf(n);
            nodes.add(loader.getReasoner().getRoot(i));
        }
        OWLObjectOneOf individual = ontologyManager.getOWLDataFactory().getOWLObjectOneOf(ind);
        OWLKnowledgeExplorerReasoner.RootNode rootNode = loader.getReasoner().getRoot(individual);
//        System.out.println("IND " + ind);
//        System.out.println("NOMINAL " + individual);
//        System.out.println("NODE " + rootNode);
//        System.out.println("NODE v2 " + rootNode.getNode() + "");
        Set<OWLObjectPropertyExpression> roles = loader.getReasoner().getObjectNeighbours(rootNode, false)
                .entities()
                .collect(toSet());

        for (OWLObjectPropertyExpression role : roles) {
            if (role.isOWLObjectProperty()) {
                Collection<OWLKnowledgeExplorerReasoner.RootNode> nodes2 = loader.getReasoner()
                        .getObjectNeighbours(rootNode, role.getNamedProperty());
//                System.out.println("ROLES " + role);
                for (OWLKnowledgeExplorerReasoner.RootNode r : nodes2) {
                    if (nodes.stream().anyMatch(p -> p.getNode().equals(r.getNode()))) {
                        OWLKnowledgeExplorerReasoner.RootNode n = nodes.stream()
                                .filter(p -> p.getNode().equals(r.getNode())).findFirst().get();
                        OWLNamedIndividual object = individuals.get(nodes.indexOf(n));
//                        System.out.println("OBJECT " + object);
                        found.add(dfactory.getOWLObjectPropertyAssertionAxiom(role, ind, object));
                    }
                }
            }
        }

        newNot.removeAll(found);
        found.removeAll(known);
//        System.out.println(known);
//        System.out.println(found);
        addAxiomsToModelsAccordingTypes(negModelSet, modelSet, found, newNot);
    }

    private Set<OWLAxiom> getAllRolesAssertionWithIndividual(OWLNamedIndividual individual) {
        Set<OWLAxiom> roleAssertions = solver.assertionsAxioms.stream()
                .filter(a -> a.isOfType(AxiomType.OBJECT_PROPERTY_ASSERTION)
                        && ((OWLObjectPropertyAssertionAxiom)a).getSubject() == individual)
                .collect(toSet());

        Set<OWLAxiom> negativeRoleAssertions = solver.negAssertionsAxioms.stream()
                .filter(a -> a.isOfType(AxiomType.NEGATIVE_OBJECT_PROPERTY_ASSERTION)
                        && ((OWLNegativeObjectPropertyAssertionAxiom)a).getSubject() == individual)
                .collect(toSet());

        for (OWLAxiom axiom : negativeRoleAssertions) {
            if (axiom.isOfType(AxiomType.NEGATIVE_OBJECT_PROPERTY_ASSERTION)) {
                roleAssertions.add(AxiomManager.getComplementOfOWLAxiom(loader, axiom));
            }
        }

        return roleAssertions;
    }

    public void divideTypesAccordingOntology(Set<OWLClassExpression> ontologyTypes, Set<OWLClassExpression> knownTypes, Set<OWLClassExpression> knownNotTypes){
        for (OWLClassExpression exp : ontologyTypes) {
            //System.out.println(exp.getClassExpressionType());
            //assert (exp.isClassExpressionLiteral());
            if (exp.isOWLClass()) {
                knownTypes.add((exp));
            } else {
                knownNotTypes.add(exp.getComplementNNF());
            }
        }
    }

    private void dividePropertyAxiomsAccordingOntology(Set<OWLAxiom> ontologyAxioms, Set<OWLObjectPropertyAssertionAxiom> known, Set<OWLObjectPropertyAssertionAxiom> knownNot) {
        for (OWLAxiom assertionAxiom : ontologyAxioms) {
            if (assertionAxiom.isOfType(AxiomType.OBJECT_PROPERTY_ASSERTION)) {
                known.add((OWLObjectPropertyAssertionAxiom) assertionAxiom);
            }
            else if (assertionAxiom.isOfType(AxiomType.NEGATIVE_OBJECT_PROPERTY_ASSERTION)) {
                knownNot.add((OWLObjectPropertyAssertionAxiom) AxiomManager.getComplementOfOWLAxiom(loader, assertionAxiom));
            }
        }
    }

    public static Set<OWLClassExpression> classSet2classExpSet(Set<OWLClass> classSet) {
        Set<OWLClassExpression> toReturn = new HashSet<>();
        toReturn.addAll(classSet);
        return toReturn;
    }

    public void addAxiomsToModelsAccordingTypes(OWLDataFactory factory, Set<OWLAxiom> negModelSet, Set<OWLAxiom> modelSet, Set<OWLClassExpression> foundTypes, Set<OWLClassExpression> newNotTypes, OWLNamedIndividual ind){

        for (OWLClassExpression classExpression : foundTypes) {
            if(!loader.isAxiomBasedAbduciblesOnInput()){
                if (!solver.abducibles.getClasses().contains(classExpression)){
                    continue;
                }
            }

            if (Configuration.CACHE_ABDUCIBLES){

                OWLAxiom axiom = factory.getOWLClassAssertionAxiom(classExpression, ind);
                axiom = getFromAbducibles(axiom);
                if (Objects.nonNull(axiom)){
                    modelSet.add(axiom);
                }

                OWLAxiom negatedAxiom = factory.getOWLClassAssertionAxiom(classExpression.getComplementNNF(), ind);
                if (!Configuration.NEGATION_ALLOWED)
                    continue;
                negatedAxiom = getFromAbducibles(negatedAxiom);
                if (Objects.nonNull(negatedAxiom)){
                    negModelSet.add(negatedAxiom);
                }

            }
            else {

                OWLAxiom axiom = factory.getOWLClassAssertionAxiom(classExpression, ind);
                if (solver.abducibleAxioms.contains(axiom))
                    modelSet.add(axiom);

                OWLAxiom negatedAxiom = factory.getOWLClassAssertionAxiom(classExpression.getComplementNNF(), ind);
                if (Configuration.NEGATION_ALLOWED && solver.abducibleAxioms.contains(negatedAxiom))
                    negModelSet.add(negatedAxiom);

            }

        }

        for (OWLClassExpression classExpression : newNotTypes) {
            if (!loader.isAxiomBasedAbduciblesOnInput()) {
                if (!solver.abducibles.getClasses().contains(classExpression)) {
                    continue;
                }
            }

            if (Configuration.CACHE_ABDUCIBLES) {

                OWLAxiom axiom = factory.getOWLClassAssertionAxiom(classExpression, ind);
                axiom = getFromAbducibles(axiom);
                if (Objects.nonNull(axiom)){
                    negModelSet.add(axiom);
                }

                OWLAxiom negatedAxiom = factory.getOWLClassAssertionAxiom(classExpression.getComplementNNF(), ind);
                if (!Configuration.NEGATION_ALLOWED)
                    continue;
                negatedAxiom = getFromAbducibles(negatedAxiom);
                if (Objects.nonNull(negatedAxiom)){
                    modelSet.add(negatedAxiom);
                }

            }

            else {

                OWLAxiom axiom = factory.getOWLClassAssertionAxiom(classExpression, ind);
                if (solver.abducibleAxioms.contains(axiom))
                    negModelSet.add(axiom);

                OWLAxiom negatedAxiom = factory.getOWLClassAssertionAxiom(classExpression.getComplementNNF(), ind);
                if (Configuration.NEGATION_ALLOWED && solver.abducibleAxioms.contains(negatedAxiom))
                    modelSet.add(negatedAxiom);

            }
        }
    }

    private OWLAxiom getFromAbducibles(OWLAxiom axiom){
        OWLAxiom fromAbducibles = null;
        for (OWLAxiom abducible : abducibles){
            if (axiom.equals(abducible)){
                fromAbducibles = abducible;
                break;
            }
        }
        return fromAbducibles;
    }

    public void addAxiomsToModelsAccordingTypes(Set<OWLAxiom> negModelSet, Set<OWLAxiom> modelSet, Set<OWLObjectPropertyAssertionAxiom> foundTypes, Set<OWLAxiom> newNotTypes){

        for (OWLObjectPropertyAssertionAxiom axiom : foundTypes) {
            if(!loader.isAxiomBasedAbduciblesOnInput()){
                if (!solver.abducibles.getRoles().contains(axiom.getProperty().getNamedProperty())) {
                    continue;
                }
            }
            OWLAxiom neg = AxiomManager.getComplementOfOWLAxiom(loader, axiom);
            negModelSet.add(neg);
            modelSet.add(axiom);
        }

        for (OWLAxiom axiom : newNotTypes) {
            if (!loader.isAxiomBasedAbduciblesOnInput()) {
                if (!solver.abducibles.getRoles().contains(((OWLObjectPropertyAssertionAxiom)axiom).getProperty().getNamedProperty())) {
                    continue;
                }
            }
            OWLAxiom neg = AxiomManager.getComplementOfOWLAxiom(loader, axiom);
            negModelSet.add(axiom);
            modelSet.add(neg);
        }

    }

    public void deletePathFromOntology() {
        solver.resetOntologyToOriginal();
    }

    public void addModel(ModelNode model, ModelNode negModel){
        solver.lastUsableModelIndex = solver.models.indexOf(model);
        if (solver.lastUsableModelIndex != -1 || (negModel.data.isEmpty() && model.data.isEmpty())){
            return;
        }
        solver.lastUsableModelIndex = solver.models.size();
        solver.models.add(model);
        solver.negModels.add(negModel);
    }

}
