package sk.uniba.fmph.dai.cats.model;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.knowledgeexploration.OWLKnowledgeExplorerReasoner;
import org.semanticweb.owlapi.search.EntitySearcher;
import sk.uniba.fmph.dai.cats.algorithms.IAbducibleAxioms;
import sk.uniba.fmph.dai.cats.common.Configuration;
import sk.uniba.fmph.dai.cats.data.InputAbducibles;
import sk.uniba.fmph.dai.cats.reasoner.AxiomManager;
import sk.uniba.fmph.dai.cats.reasoner.Loader;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toSet;

public class ModelExtractor {

    private final Loader loader;
    private final InputAbducibles inputAbducibles;
    private final Map<OWLAxiom, OWLAxiom> abducibleAxioms;
    private final List<OWLNamedIndividual> abdIndividuals;

    private final List<OWLAxiom> assertionAxioms;
    private final List<OWLAxiom> negAssertionAxioms;
    
    private final OWLOntology originalOntology;

    private final OWLOntologyManager ontologyManager;

    public ModelExtractor(Loader loader, IAbducibleAxioms abducibleAxioms){

        this.loader = loader;
        inputAbducibles = loader.getAbducibles();
        originalOntology = loader.getOriginalOntology();

        this.abducibleAxioms = new HashMap<>();
        for (OWLAxiom axiom : abducibleAxioms.getAxioms()) {
            this.abducibleAxioms.put(axiom, axiom);
        }

        assertionAxioms = abducibleAxioms.getAssertionAxioms();
        negAssertionAxioms = abducibleAxioms.getNegatedAssertionAxioms();

        this.abdIndividuals = getAbdIndividuals(this.abducibleAxioms.keySet());

        this.ontologyManager = OWLManager.createOWLOntologyManager();
    }

    public Model extractModel() {
        Model model = new Model();

        OWLDataFactory dfactory = ontologyManager.getOWLDataFactory();

        for (OWLNamedIndividual ind : abdIndividuals) {
            assignTypesToIndividual(dfactory, ind, model);
        }

        if (Configuration.ROLES_IN_EXPLANATIONS_ALLOWED) {
            for (OWLNamedIndividual ind : abdIndividuals) {
                assignRolesToIndividual(dfactory, ind, abdIndividuals, model);
            }
        }

        return model;
    }

    public void assignTypesToIndividual(OWLDataFactory dfactory, OWLNamedIndividual individual, Model model){
        Set<OWLClassExpression> notTypes = classSet2classExpSet(originalOntology.classesInSignature().collect(toSet()));
        notTypes.remove(dfactory.getOWLThing());

        OWLObjectOneOf individualNominal = ontologyManager.getOWLDataFactory().getOWLObjectOneOf(individual);

        OWLKnowledgeExplorerReasoner.RootNode rootNode = loader.getReasoner().getRoot(individualNominal);
        Set<OWLClassExpression> foundTypes = loader.getReasoner().getObjectLabel(rootNode,false)
                .entities()
                .collect(toSet());

        notTypes.removeAll(foundTypes);

        addAxiomsToModelsAccordingTypes(dfactory, model, foundTypes, notTypes, individual);
    }

    public void assignRolesToIndividual(OWLDataFactory dfactory, OWLNamedIndividual ind, List<OWLNamedIndividual> individuals, Model model) {
        Set<OWLAxiom> ontologyPropertyAxioms = originalOntology.axioms()
                .filter(a -> a.isOfType(AxiomType.OBJECT_PROPERTY_ASSERTION)
                        && ((OWLObjectPropertyAssertionAxiom)a).getSubject().equals(ind))
                .collect(toSet()); //object properties where ind is a subject -> objectProperty(ind,x)

        ontologyPropertyAxioms.addAll(originalOntology.axioms()
                .filter(a -> a.isOfType(AxiomType.NEGATIVE_OBJECT_PROPERTY_ASSERTION)
                        && ((OWLNegativeObjectPropertyAssertionAxiom)a).getSubject().equals(ind))
                .collect(toSet())); //add neg object properties where ind is a subject -> not(objectProperty(ind,x))

        Set<OWLAxiom> notProperties = getAllRolesAssertionWithIndividual(ind);

        Set<OWLObjectPropertyAssertionAxiom> foundProperties = new HashSet<>();
        Map<OWLObject, OWLNamedIndividual> nodeToIndividual = new HashMap<>();

        for (OWLNamedIndividual n : individuals) {
            OWLObjectOneOf i = ontologyManager.getOWLDataFactory().getOWLObjectOneOf(n);
            OWLKnowledgeExplorerReasoner.RootNode node = loader.getReasoner().getRoot(i);
            nodeToIndividual.put(node.getNode(), n);
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

                    OWLNamedIndividual object = nodeToIndividual.get(r.getNode());
                    if (object != null) {
                        foundProperties.add(dfactory.getOWLObjectPropertyAssertionAxiom(role, ind, object));
                    }

                }
            }
        }

        notProperties.removeAll(foundProperties);

        addAxiomsToModelsAccordingTypes(model, foundProperties, notProperties);
    }

    private List<OWLNamedIndividual> getAbdIndividuals(Set<OWLAxiom> axioms) {
        if (!loader.isAxiomBasedAbduciblesOnInput()) {
            return new ArrayList<>(inputAbducibles.getIndividuals());
        }

        Set<OWLNamedIndividual> individuals = new HashSet<>();
        for (OWLAxiom axiom : axioms) {
            individuals.addAll(axiom.getIndividualsInSignature());
        }

        return new ArrayList<>(individuals);
    }

    private Set<OWLAxiom> getAllRolesAssertionWithIndividual(OWLNamedIndividual individual) {
        Set<OWLAxiom> roleAssertions = assertionAxioms.stream()
                .filter(a -> a.isOfType(AxiomType.OBJECT_PROPERTY_ASSERTION)
                        && ((OWLObjectPropertyAssertionAxiom)a).getSubject().equals(individual))
                .collect(toSet());

        Set<OWLAxiom> negativeRoleAssertions = negAssertionAxioms.stream()
                .filter(a -> a.isOfType(AxiomType.NEGATIVE_OBJECT_PROPERTY_ASSERTION)
                        && ((OWLNegativeObjectPropertyAssertionAxiom)a).getSubject().equals(individual))
                .collect(toSet());

        for (OWLAxiom axiom : negativeRoleAssertions) {
            if (axiom.isOfType(AxiomType.NEGATIVE_OBJECT_PROPERTY_ASSERTION)) {
                roleAssertions.add(AxiomManager.getComplementOfOWLAxiom(loader, axiom));
            }
        }

        return roleAssertions;
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

            OWLAxiom axiom = factory.getOWLClassAssertionAxiom(classExpression, ind);
            axiom = getFromAbducibles(axiom);
            if (Objects.nonNull(axiom)){
                model.add(axiom);
            }

            if (!Configuration.NEGATION_ALLOWED)
                continue;
            OWLAxiom negatedAxiom = factory.getOWLClassAssertionAxiom(classExpression.getComplementNNF(), ind);
            negatedAxiom = getFromAbducibles(negatedAxiom);
            if (Objects.nonNull(negatedAxiom)){
                model.addNegated(negatedAxiom);
            }

        }

        for (OWLClassExpression classExpression : newNotTypes) {
            if (!loader.isAxiomBasedAbduciblesOnInput()) {
                if (!inputAbducibles.getClasses().contains(classExpression)) {
                    continue;
                }
            }

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
    }

    private OWLAxiom getFromAbducibles(OWLAxiom axiom){
        return abducibleAxioms.get(axiom);
    }

    public void addAxiomsToModelsAccordingTypes(Model model, Set<OWLObjectPropertyAssertionAxiom> newTypes, Set<OWLAxiom> newNotTypes){

        for (OWLObjectPropertyAssertionAxiom axiom : newTypes) {
            if(!loader.isAxiomBasedAbduciblesOnInput()){
                if (!inputAbducibles.getRoles().contains(axiom.getProperty().getNamedProperty())) {
                    continue;
                }
            }

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

        for (OWLAxiom axiom : newNotTypes) {
            if (!loader.isAxiomBasedAbduciblesOnInput()) {
                if (!inputAbducibles.getRoles().contains(((OWLObjectPropertyAssertionAxiom)axiom).getProperty().getNamedProperty())) {
                    continue;
                }
            }

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

    }

}
