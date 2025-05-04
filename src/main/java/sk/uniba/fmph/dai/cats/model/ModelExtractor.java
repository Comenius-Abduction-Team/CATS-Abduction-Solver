package sk.uniba.fmph.dai.cats.model;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.knowledgeexploration.OWLKnowledgeExplorerReasoner;
import org.semanticweb.owlapi.search.EntitySearcher;
import sk.uniba.fmph.dai.cats.algorithms.IAbducibleAxioms;
import sk.uniba.fmph.dai.cats.common.Configuration;
import sk.uniba.fmph.dai.cats.data.InputAbducibles;
import sk.uniba.fmph.dai.cats.model.Model;
import sk.uniba.fmph.dai.cats.reasoner.AxiomManager;
import sk.uniba.fmph.dai.cats.reasoner.Loader;

import java.util.*;

import static java.util.stream.Collectors.toSet;

public class ModelExtractor {

    private final Loader loader;
    private final InputAbducibles inputAbducibles;
    private final Set<OWLAxiom> abducibleAxioms;

    private final List<OWLAxiom> assertionAxioms;
    private final List<OWLAxiom> negAssertionAxioms;
    
    private final OWLOntology originalOntology;

    private final OWLOntologyManager ontologyManager;

    public ModelExtractor(Loader loader, IAbducibleAxioms abducibleAxioms){

        this.loader = loader;
        inputAbducibles = loader.getAbducibles();
        originalOntology = loader.getOriginalOntology();

        this.abducibleAxioms = abducibleAxioms.getAxioms();
        assertionAxioms = abducibleAxioms.getAssertionAxioms();
        negAssertionAxioms = abducibleAxioms.getNegatedAssertionAxioms();

        this.ontologyManager = OWLManager.createOWLOntologyManager();
    }

    public Model extractModel() {

        Model model = new Model();
        ArrayList<OWLNamedIndividual> individualArray;

        if(loader.isAxiomBasedAbduciblesOnInput()){
            individualArray = new ArrayList<>(loader.getOntology().getIndividualsInSignature());
        } else {
            individualArray = new ArrayList<>(loader.getAbducibles().getIndividuals());
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

        if(loader.isAxiomBasedAbduciblesOnInput()){

            model.getData().retainAll(inputAbducibles.getAxiomBasedAbducibles());
            model.getNegatedData().retainAll(inputAbducibles.getAxiomBasedAbducibles());

        }

        return model;

    }

    public void assignTypesToIndividual(OWLDataFactory dfactory, OWLNamedIndividual ind, Model model){
        //complex concepts from original ontology
        Set<OWLClassExpression> ontologyTypes = EntitySearcher.getTypes(ind, originalOntology).collect(toSet());

        Set<OWLClassExpression> knownTypes = new HashSet<>(); //concepts assigned to ind from original ontology
        Set<OWLClassExpression> knownNotTypes = new HashSet<>(); //neg concepts assigned to ind from original ontology
        divideTypesAccordingOntology(ontologyTypes, knownTypes, knownNotTypes);

        Set<OWLClassExpression> newNotTypes = classSet2classExpSet(originalOntology.classesInSignature().collect(toSet()));
        newNotTypes.remove(dfactory.getOWLThing());
        newNotTypes.removeAll(knownNotTypes);

        OWLObjectOneOf individual = ontologyManager.getOWLDataFactory().getOWLObjectOneOf(ind);

        OWLKnowledgeExplorerReasoner.RootNode rootNode = loader.getReasoner().getRoot(individual);
        Set<OWLClassExpression> foundTypes = loader.getReasoner().getObjectLabel(rootNode,false)
                .entities()
                .collect(toSet());

        newNotTypes.removeAll(foundTypes);
        foundTypes.removeAll(knownTypes);

        addAxiomsToModelsAccordingTypes(dfactory, model, foundTypes, newNotTypes, ind);
    }

    public void assignRolesToIndividual(OWLDataFactory dfactory, OWLNamedIndividual ind, ArrayList<OWLNamedIndividual> individuals, Model model) {
        Set<OWLAxiom> ontologyPropertyAxioms = originalOntology.axioms()
                .filter(a -> a.isOfType(AxiomType.OBJECT_PROPERTY_ASSERTION)
                        && ((OWLObjectPropertyAssertionAxiom)a).getSubject() == ind)
                .collect(toSet()); //object properties where ind is a subject -> objectProperty(ind,x)

        ontologyPropertyAxioms.addAll(originalOntology.axioms()
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
        Set<OWLObjectPropertyExpression> roles = loader.getReasoner().getObjectNeighbours(rootNode, false)
                .entities()
                .collect(toSet());

        for (OWLObjectPropertyExpression role : roles) {
            if (role.isOWLObjectProperty()) {
                Collection<OWLKnowledgeExplorerReasoner.RootNode> nodes2 = loader.getReasoner()
                        .getObjectNeighbours(rootNode, role.getNamedProperty());
                for (OWLKnowledgeExplorerReasoner.RootNode r : nodes2) {
                    if (nodes.stream().anyMatch(p -> p.getNode().equals(r.getNode()))) {
                        OWLKnowledgeExplorerReasoner.RootNode n = nodes.stream()
                                .filter(p -> p.getNode().equals(r.getNode())).findFirst().get();
                        OWLNamedIndividual object = individuals.get(nodes.indexOf(n));
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
        Set<OWLAxiom> roleAssertions = assertionAxioms.stream()
                .filter(a -> a.isOfType(AxiomType.OBJECT_PROPERTY_ASSERTION)
                        && ((OWLObjectPropertyAssertionAxiom)a).getSubject() == individual)
                .collect(toSet());

        Set<OWLAxiom> negativeRoleAssertions = negAssertionAxioms.stream()
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
                if (!inputAbducibles.getClasses().contains(classExpression)){
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
                if (abducibleAxioms.contains(axiom))
                    model.add(axiom);

                OWLAxiom negatedAxiom = factory.getOWLClassAssertionAxiom(classExpression.getComplementNNF(), ind);
                if (Configuration.NEGATION_ALLOWED && abducibleAxioms.contains(negatedAxiom))
                    model.addNegated(negatedAxiom);

            }

        }

        for (OWLClassExpression classExpression : newNotTypes) {
            if (!loader.isAxiomBasedAbduciblesOnInput()) {
                if (!inputAbducibles.getClasses().contains(classExpression)) {
                    continue;
                }
            }

            if (Configuration.CACHE_ABDUCIBLES) {

                OWLAxiom axiom = factory.getOWLClassAssertionAxiom(classExpression, ind);
                axiom = getFromAbducibles(axiom);
                if (Objects.nonNull(axiom)){
                    model.addNegated(axiom);
                }

                if (!Configuration.NEGATION_ALLOWED)
                    continue;
                OWLAxiom negatedAxiom = factory.getOWLClassAssertionAxiom(classExpression.getComplementNNF(), ind);
                negatedAxiom = getFromAbducibles(negatedAxiom);
                if (Objects.nonNull(negatedAxiom)){
                    model.add(negatedAxiom);

                }

            }

            else {

                OWLAxiom axiom = factory.getOWLClassAssertionAxiom(classExpression, ind);
                if (abducibleAxioms.contains(axiom))
                    model.addNegated(axiom);

                OWLAxiom negatedAxiom = factory.getOWLClassAssertionAxiom(classExpression.getComplementNNF(), ind);
                if (Configuration.NEGATION_ALLOWED && abducibleAxioms.contains(negatedAxiom))
                    model.add(negatedAxiom);

            }
        }
    }

    private OWLAxiom getFromAbducibles(OWLAxiom axiom){
        OWLAxiom fromAbducibles = null;
        for (OWLAxiom abducible : abducibleAxioms){
            if (axiom.equals(abducible)){
                fromAbducibles = abducible;
                break;
            }
        }
        return fromAbducibles;
    }

    public void addAxiomsToModelsAccordingTypes(Model model, Set<OWLObjectPropertyAssertionAxiom> newTypes, Set<OWLAxiom> newNotTypes){

        for (OWLObjectPropertyAssertionAxiom axiom : newTypes) {
            if(!loader.isAxiomBasedAbduciblesOnInput()){
                if (!inputAbducibles.getRoles().contains(axiom.getProperty().getNamedProperty())) {
                    continue;
                }
            }

            if (Configuration.CACHE_ABDUCIBLES){

                axiom = (OWLObjectPropertyAssertionAxiom) getFromAbducibles(axiom);
                if (Objects.nonNull(axiom)){
                    model.add(axiom);
                }

                if (!Configuration.NEGATION_ALLOWED)
                    continue;
                OWLAxiom negatedAxiom = AxiomManager.getComplementOfOWLAxiom(loader, axiom);
                negatedAxiom = getFromAbducibles(negatedAxiom);
                if (Objects.nonNull(negatedAxiom)){
                    model.addNegated(negatedAxiom);

                }

            }

            else {

                OWLAxiom neg = AxiomManager.getComplementOfOWLAxiom(loader, axiom);
                model.add(axiom);
                model.addNegated(neg);

            }


        }

        for (OWLAxiom axiom : newNotTypes) {
            if (!loader.isAxiomBasedAbduciblesOnInput()) {
                if (!inputAbducibles.getRoles().contains(((OWLObjectPropertyAssertionAxiom)axiom).getProperty().getNamedProperty())) {
                    continue;
                }
            }

            if (Configuration.CACHE_ABDUCIBLES){

                OWLAxiom negatedAxiom = AxiomManager.getComplementOfOWLAxiom(loader, axiom);
                negatedAxiom = getFromAbducibles(negatedAxiom);
                if (Objects.nonNull(negatedAxiom)){
                    model.add(negatedAxiom);
                }

                if (!Configuration.NEGATION_ALLOWED)
                    continue;
                axiom = getFromAbducibles(axiom);
                if (Objects.nonNull(axiom)){
                    model.addNegated(axiom);
                }

            }

            else {

                OWLAxiom neg = AxiomManager.getComplementOfOWLAxiom(loader, axiom);
                model.addNegated(axiom);
                model.add(neg);

            }


        }

    }

}
