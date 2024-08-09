package sk.uniba.fmph.dai.cats.algorithms.hybrid;
import sk.uniba.fmph.dai.cats.algorithms.rctree.Model;
import sk.uniba.fmph.dai.cats.algorithms.rctree.ModelManager;
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

    private final ILoader loader;
    private final HybridSolver solver;
    private final OWLOntologyManager ontologyManager;

    private Set<OWLAxiom> abducibles;
    //private HashMap<Integer,OWLAxiom> abducibles;

    public ModelExtractor(HybridSolver solver, IAbducibleAxioms abducibles){
        this.solver = solver;
        this.loader = solver.loader;
        initialiseAbducibles(abducibles.getAxioms());
        this.ontologyManager = OWLManager.createOWLOntologyManager();
    }

    public void initialiseAbducibles(Set<OWLAxiom> abducibles){
//        abducibles = new HashMap<>();
//        for (OWLAxiom axiom : solver.abducibleAxioms.getAxioms()){
//            abducibles.put(axiom.hashCode(), axiom);
//        }
        this.abducibles = abducibles;
    }

    public Model extractModel() {  // mrozek

        Model model = new Model();
        ArrayList<OWLNamedIndividual> individualArray;

        if(loader.isAxiomBasedAbduciblesOnInput()){
            individualArray = new ArrayList<>(loader.getOntology().getIndividualsInSignature());
        } else {
            individualArray = new ArrayList<>(solver.abducibles.getIndividuals());
        }

        OWLDataFactory dfactory = ontologyManager.getOWLDataFactory();

        for (OWLNamedIndividual ind : individualArray) {
            assignTypesToIndividual(dfactory, ind, model);
        }
        if (Configuration.ROLES_IN_EXPLANATIONS_ALLOWED) {
            for (OWLNamedIndividual ind : individualArray) {
                assignRolesToIndividual(dfactory, ind, individualArray, model);
            }
        }

        deletePathFromOntology();

        if(loader.isAxiomBasedAbduciblesOnInput()){

            model.getData().retainAll(solver.abducibles.getAxiomBasedAbducibles());
            model.getNegatedData().retainAll(solver.abducibles.getAxiomBasedAbducibles());

        }

        return model;

    }



    public void assignTypesToIndividual(OWLDataFactory dfactory, OWLNamedIndividual ind, Model model){
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

        addAxiomsToModelsAccordingTypes(dfactory, model, foundTypes, newNotTypes, ind);
    }

    public void assignRolesToIndividual(OWLDataFactory dfactory, OWLNamedIndividual ind, ArrayList<OWLNamedIndividual> individuals, Model model) {
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
        addAxiomsToModelsAccordingTypes(model, found, newNot);
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
        return new HashSet<>(classSet);
    }

    public void addAxiomsToModelsAccordingTypes(OWLDataFactory factory, Model model, Set<OWLClassExpression> foundTypes, Set<OWLClassExpression> newNotTypes, OWLNamedIndividual ind){

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
                    model.add(axiom);
                }

                OWLAxiom negatedAxiom = factory.getOWLClassAssertionAxiom(classExpression.getComplementNNF(), ind);
                if (!Configuration.NEGATION_ALLOWED)
                    continue;
                negatedAxiom = getFromAbducibles(negatedAxiom);
                if (Objects.nonNull(negatedAxiom)){
                    model.addNegated(negatedAxiom);
                }

            }
            else {

                OWLAxiom axiom = factory.getOWLClassAssertionAxiom(classExpression, ind);
                if (solver.abducibleAxioms.contains(axiom))
                    model.add(axiom);

                OWLAxiom negatedAxiom = factory.getOWLClassAssertionAxiom(classExpression.getComplementNNF(), ind);
                if (Configuration.NEGATION_ALLOWED && solver.abducibleAxioms.contains(negatedAxiom))
                    model.addNegated(negatedAxiom);

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
                    model.addNegated(axiom);
                }

                OWLAxiom negatedAxiom = factory.getOWLClassAssertionAxiom(classExpression.getComplementNNF(), ind);
                if (!Configuration.NEGATION_ALLOWED)
                    continue;
                negatedAxiom = getFromAbducibles(negatedAxiom);
                if (Objects.nonNull(negatedAxiom)){
                    model.add(negatedAxiom);
                }

            }

            else {

                OWLAxiom axiom = factory.getOWLClassAssertionAxiom(classExpression, ind);
                if (solver.abducibleAxioms.contains(axiom))
                    model.addNegated(axiom);

                OWLAxiom negatedAxiom = factory.getOWLClassAssertionAxiom(classExpression.getComplementNNF(), ind);
                if (Configuration.NEGATION_ALLOWED && solver.abducibleAxioms.contains(negatedAxiom))
                    model.add(negatedAxiom);

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

    public void addAxiomsToModelsAccordingTypes(Model model, Set<OWLObjectPropertyAssertionAxiom> foundTypes, Set<OWLAxiom> newNotTypes){

        for (OWLObjectPropertyAssertionAxiom axiom : foundTypes) {
            if(!loader.isAxiomBasedAbduciblesOnInput()){
                if (!solver.abducibles.getRoles().contains(axiom.getProperty().getNamedProperty())) {
                    continue;
                }
            }
            OWLAxiom neg = AxiomManager.getComplementOfOWLAxiom(loader, axiom);

            model.add(axiom);
            model.addNegated(neg);

        }

        for (OWLAxiom axiom : newNotTypes) {
            if (!loader.isAxiomBasedAbduciblesOnInput()) {
                if (!solver.abducibles.getRoles().contains(((OWLObjectPropertyAssertionAxiom)axiom).getProperty().getNamedProperty())) {
                    continue;
                }
            }
            OWLAxiom neg = AxiomManager.getComplementOfOWLAxiom(loader, axiom);

            model.addNegated(axiom);
            model.add(neg);
        }

    }

    public void deletePathFromOntology() {
        solver.resetOntologyToOriginal();
    }

}
