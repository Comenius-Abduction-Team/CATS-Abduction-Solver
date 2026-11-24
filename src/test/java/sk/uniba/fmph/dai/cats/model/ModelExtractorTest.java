package sk.uniba.fmph.dai.cats.model;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.knowledgeexploration.OWLKnowledgeExplorerReasoner;
import sk.uniba.fmph.dai.cats.algorithms.IAbducibleAxioms;
import sk.uniba.fmph.dai.cats.common.Configuration;
import sk.uniba.fmph.dai.cats.reasoner.Loader;
import sk.uniba.fmph.dai.cats.reasoner.ReasonerType;
import uk.ac.manchester.cs.jfact.JFactFactory;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ModelExtractorTest {

    private OWLDataFactory df;
    private OWLClass A, B, C;
    private OWLNamedIndividual a, b;
    private OWLObjectProperty r;
    private boolean oldNegation, oldRoles;

    @BeforeEach
    void setUp() {
        oldNegation = Configuration.NEGATION_ALLOWED;
        oldRoles = Configuration.ROLES_IN_EXPLANATIONS_ALLOWED;
        Configuration.NEGATION_ALLOWED = true;
        Configuration.ROLES_IN_EXPLANATIONS_ALLOWED = false;
        df = OWLManager.getOWLDataFactory();
        A = df.getOWLClass(IRI.create("urn:extractor-test#A"));
        B = df.getOWLClass(IRI.create("urn:extractor-test#B"));
        C = df.getOWLClass(IRI.create("urn:extractor-test#C"));
        a = df.getOWLNamedIndividual(IRI.create("urn:extractor-test#a"));
        b = df.getOWLNamedIndividual(IRI.create("urn:extractor-test#b"));
        r = df.getOWLObjectProperty(IRI.create("urn:extractor-test#r"));
    }

    @AfterEach
    void tearDown() {
        Configuration.NEGATION_ALLOWED = oldNegation;
        Configuration.ROLES_IN_EXPLANATIONS_ALLOWED = oldRoles;
    }

    @Test
    void explicitPositiveAssertion() throws OWLOntologyCreationException {
        assertForcedType(true, df.getOWLClassAssertionAxiom(A, a));
    }

    @Test
    void explicitNegativeAssertion() throws OWLOntologyCreationException {
        assertForcedType(false, df.getOWLClassAssertionAxiom(A.getComplementNNF(), a));
    }

    @Test
    void namedClassEquivalence() throws OWLOntologyCreationException {
        assertForcedType(true, df.getOWLEquivalentClassesAxiom(A, B),
                df.getOWLClassAssertionAxiom(B, a));
    }

    @Test
    void intersectionDefinition() throws OWLOntologyCreationException {
        assertForcedType(true, df.getOWLEquivalentClassesAxiom(A, df.getOWLObjectIntersectionOf(B, C)),
                df.getOWLClassAssertionAxiom(B, a), df.getOWLClassAssertionAxiom(C, a));
    }

    @Test
    void unionDefinition() throws OWLOntologyCreationException {
        assertForcedType(true, df.getOWLEquivalentClassesAxiom(A, df.getOWLObjectUnionOf(B, C)),
                df.getOWLClassAssertionAxiom(B, a));
    }

    @Test
    void complementDefinition() throws OWLOntologyCreationException {
        assertForcedType(true, df.getOWLEquivalentClassesAxiom(A, B.getComplementNNF()),
                df.getOWLClassAssertionAxiom(B.getComplementNNF(), a));
    }

    @Test
    void nominalDefinition() throws OWLOntologyCreationException {
        assertForcedType(true, df.getOWLEquivalentClassesAxiom(A, df.getOWLObjectOneOf(a)));
    }

    @Test
    void existentialDefinition() throws OWLOntologyCreationException {
        assertForcedType(true, df.getOWLEquivalentClassesAxiom(A, df.getOWLObjectSomeValuesFrom(r, B)),
                df.getOWLObjectPropertyAssertionAxiom(r, a, b), df.getOWLClassAssertionAxiom(B, b));
    }

    @Test
    void complementDefinitionWithNegativeEntailment() throws OWLOntologyCreationException {
        assertForcedType(false, df.getOWLEquivalentClassesAxiom(A, B.getComplementNNF()),
                df.getOWLClassAssertionAxiom(B, a));
    }

    @Test
    void disjunctionMustHaveJointlyConsistentAssignment() throws OWLOntologyCreationException {
        Set<OWLAxiom> base = set(df.getOWLClassAssertionAxiom(df.getOWLObjectUnionOf(A, B), a));
        Set<OWLAxiom> abducibles = set(positive(A), negative(A), positive(B), negative(B));
        Model model = extract(base, abducibles);
        // Neither A(a) nor B(a) is individually entailed, so don't force one particular model.
        assertValidProjection(base, abducibles, model);
    }

    @Test
    void intersectionDefinitionWithOnlyPositiveAbducibles() throws OWLOntologyCreationException {
        Configuration.NEGATION_ALLOWED = false;
        Set<OWLAxiom> base = set(df.getOWLEquivalentClassesAxiom(A, df.getOWLObjectIntersectionOf(B, C)),
                df.getOWLClassAssertionAxiom(B, a), df.getOWLClassAssertionAxiom(C, a));
        Set<OWLAxiom> abducibles = set(positive(A));
        Model model = extract(base, abducibles);
        assertValidProjection(base, abducibles, model);
        assertEquals(abducibles, model.getData(), "A(a) is entailed and must be true");
    }

    @Test
    void complementDefinitionWithoutForcedType()
            throws OWLOntologyCreationException {

        Set<OWLAxiom> base = set(
                df.getOWLEquivalentClassesAxiom(
                        A, B.getComplementNNF()
                ),
                df.getOWLClassAssertionAxiom(
                        df.getOWLThing(), a
                )
        );

        Set<OWLAxiom> abducibles = set(
                positive(A), negative(A),
                positive(B), negative(B)
        );

        Model model = extract(base, abducibles);

        assertValidProjection(base, abducibles, model);
    }

    @Test
    void complementDefinitionWithMarkerClasses()
            throws OWLOntologyCreationException {

        OWLClass positiveA = df.getOWLClass(
                IRI.create("urn:extractor-test#PositiveA"));
        OWLClass negativeA = df.getOWLClass(
                IRI.create("urn:extractor-test#NegativeA"));

        OWLClass positiveB = df.getOWLClass(
                IRI.create("urn:extractor-test#PositiveB"));
        OWLClass negativeB = df.getOWLClass(
                IRI.create("urn:extractor-test#NegativeB"));

        // Original ontology
        Set<OWLAxiom> base = set(
                df.getOWLEquivalentClassesAxiom(
                        A, B.getComplementNNF()),
                df.getOWLClassAssertionAxiom(
                        df.getOWLThing(), a)
        );

        // Transformed ontology
        Set<OWLAxiom> transformed = new HashSet<>(base);

        transformed.addAll(set(
                df.getOWLSubClassOfAxiom(positiveA, A),
                df.getOWLSubClassOfAxiom(
                        negativeA, A.getComplementNNF()),
                df.getOWLClassAssertionAxiom(
                        df.getOWLObjectUnionOf(positiveA, negativeA),
                        a),

                df.getOWLSubClassOfAxiom(positiveB, B),
                df.getOWLSubClassOfAxiom(
                        negativeB, B.getComplementNNF()),
                df.getOWLClassAssertionAxiom(
                        df.getOWLObjectUnionOf(positiveB, negativeB),
                        a)
        ));

        try (TestLoader loader = new TestLoader(transformed)) {

            OWLKnowledgeExplorerReasoner reasoner =
                    loader.getReasoner();

            assertTrue(reasoner.isConsistent());

            OWLKnowledgeExplorerReasoner.RootNode root =
                    reasoner.getRoot(df.getOWLObjectOneOf(a));

            Set<OWLClassExpression> foundTypes = reasoner
                    .getObjectLabel(root, false)
                    .entities()
                    .collect(Collectors.toSet());

            System.out.println("Found types: " + foundTypes);

            boolean aPositive = foundTypes.contains(positiveA);
            boolean aNegative = foundTypes.contains(negativeA);

            boolean bPositive = foundTypes.contains(positiveB);
            boolean bNegative = foundTypes.contains(negativeB);

            // Exactly one marker must be present for each class.
            assertTrue(aPositive ^ aNegative,
                    "Exactly one marker for A must be present");

            assertTrue(bPositive ^ bNegative,
                    "Exactly one marker for B must be present");

            // Reconstruct the interpretation of original classes.
            Set<OWLAxiom> assignment = new HashSet<>(base);

            assignment.add(
                    aPositive ? positive(A) : negative(A));

            assignment.add(
                    bPositive ? positive(B) : negative(B));

            System.out.println("A(a): " + aPositive);
            System.out.println("B(a): " + bPositive);

            // The reconstructed interpretation must be consistent.
            assertTrue(isConsistent(assignment),
                    "Reconstructed interpretation is inconsistent");
        }
    }

    private void assertForcedType(boolean expected, OWLAxiom... axioms) throws OWLOntologyCreationException {
        Set<OWLAxiom> base = set(axioms);

        OWLAxiom positive = positive(A);
        OWLAxiom negative = negative(A);
        Set<OWLAxiom> abducibles = set(positive, negative);

        // Independently verify the intended test fixture using a fresh reasoner.
        assertTrue(isConsistent(base), "Invalid test fixture: inconsistent background");
        Set<OWLAxiom> opposite = new HashSet<>(base);
        opposite.add(expected ? negative : positive);
        assertFalse(isConsistent(opposite), "Invalid test fixture: expected truth value is not forced");

        Model model = extract(base, abducibles);
        assertValidProjection(base, abducibles, model);
        assertEquals(set(expected ? positive : negative), model.getData(), "Wrong true abducibles");
        assertEquals(set(expected ? negative : positive), model.getNegatedData(), "Wrong false abducibles");
    }

    private void assertValidProjection(Set<OWLAxiom> base, Set<OWLAxiom> abducibles, Model model)
            throws OWLOntologyCreationException {
        Set<OWLAxiom> classified = new HashSet<>(model.getData());
        classified.addAll(model.getNegatedData());
        assertEquals(abducibles, classified, "All and only abducibles must be classified");
        assertTrue(Collections.disjoint(model.getData(), model.getNegatedData()), "Overlapping classifications");

        Set<OWLAxiom> assignment = new HashSet<>(base);
        assignment.addAll(model.getData());
        for (OWLAxiom axiom : model.getNegatedData()) {
            OWLClassAssertionAxiom ca = (OWLClassAssertionAxiom) axiom;
            assignment.add(df.getOWLClassAssertionAxiom(ca.getClassExpression().getComplementNNF(), ca.getIndividual()));
        }
        assertTrue(isConsistent(assignment), "Extracted projection is inconsistent with background. True: "
                + model.getData() + "; false: " + model.getNegatedData());
    }

    private Model extract(Set<OWLAxiom> base, Set<OWLAxiom> abducibles) throws OWLOntologyCreationException {
        try (TestLoader loader = new TestLoader(base)) {
            assertTrue(loader.getReasoner().isConsistent(), "Invalid test fixture");
            return new ModelExtractor(loader, new TestAbducibles(abducibles)).extractModel();
        }
    }

    private boolean isConsistent(Set<OWLAxiom> axioms) throws OWLOntologyCreationException {
        OWLOntology ontology = OWLManager.createOWLOntologyManager().createOntology(axioms);
        OWLReasoner reasoner = new JFactFactory().createReasoner(ontology);

        try {
            return reasoner.isConsistent();
        } finally {
            reasoner.dispose();
        }
    }

    private OWLAxiom positive(OWLClass cls) { return df.getOWLClassAssertionAxiom(cls, a); }
    private OWLAxiom negative(OWLClass cls) { return df.getOWLClassAssertionAxiom(cls.getComplementNNF(), a); }
    private static Set<OWLAxiom> set(OWLAxiom... axioms) { return new HashSet<>(Arrays.asList(axioms)); }

    /** Minimal loader fixture: real ontology and real reasoner, no input files. */
    private static final class TestLoader extends Loader implements AutoCloseable {
        TestLoader(Set<OWLAxiom> axioms) throws OWLOntologyCreationException {
            ontologyManager = OWLManager.createOWLOntologyManager();
            originalOntology = ontologyManager.createOntology(axioms);
            ontology = originalOntology;
            setAxiomBasedAbduciblesOnInput(true);
            changeReasoner(ReasonerType.JFACT);
        }
        @Override public void initialize(ReasonerType reasonerType) { throw new UnsupportedOperationException(); }
        @Override protected void setupOntology() { throw new UnsupportedOperationException(); }
        @Override protected void loadObservation() { throw new UnsupportedOperationException(); }
        @Override protected void loadAbducibles() { throw new UnsupportedOperationException(); }
        @Override public void close() { getReasoner().dispose(); }
    }

    /** Explicit axiom-based abducibles; role assertion lists are unused here. */
    private static final class TestAbducibles implements IAbducibleAxioms {
        private final Set<OWLAxiom> axioms;
        TestAbducibles(Set<OWLAxiom> axioms) { this.axioms = axioms; }
        @Override public Set<OWLAxiom> getAxioms() { return axioms; }
        @Override public int size() { return axioms.size(); }
        @Override public boolean contains(OWLAxiom axiom) { return axioms.contains(axiom); }
        @Override public List<OWLAxiom> getAssertionAxioms() { return Collections.emptyList(); }
        @Override public List<OWLAxiom> getNegatedAssertionAxioms() { return Collections.emptyList(); }
    }

}
