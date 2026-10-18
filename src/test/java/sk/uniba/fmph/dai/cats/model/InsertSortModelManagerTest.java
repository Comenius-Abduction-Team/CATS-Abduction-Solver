package sk.uniba.fmph.dai.cats.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.semanticweb.owlapi.model.OWLAxiom;
import sk.uniba.fmph.dai.cats.algorithms.AlgorithmSolver;
import sk.uniba.fmph.dai.cats.common.Configuration;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class InsertSortModelManagerTest {

    private AlgorithmSolver solver;
    private ModelExtractor extractor;

    private InsertSortModelManager manager;

    public static final OWLAxiom PERSON_JOHN_AXIOM = Helper.createClassAssertion("john", "Person");
    public static final OWLAxiom PERSON_JOHN_AXIOM2 = Helper.createClassAssertion("john", "Person");

    public static final OWLAxiom PERSON_MARY_AXIOM = Helper.createClassAssertion("mary", "Person");
    public static final OWLAxiom PERSON_MARY_AXIOM2 = Helper.createClassAssertion("mary", "Person");

    public static final OWLAxiom PERSON_JANE_AXIOM = Helper.createClassAssertion("jane", "Person");

    public static final OWLAxiom PERSON_EVE_AXIOM = Helper.createClassAssertion("eve", "Person");

    @BeforeEach
    void setUp() {
        Configuration.EVENTS = false;

        solver = mock(AlgorithmSolver.class);
        extractor = mock(ModelExtractor.class);

        manager = new InsertSortModelManager(solver);
        manager.setExtractor(extractor);
    }

    @Test
    void shouldCreateTreeSetAsModelCollection() {
        assertTrue(manager.models instanceof TreeSet);
    }

    @Test
    void shouldStoreModelsInSpecificOrder() {
        Model first = new Model();
        first.add(PERSON_JANE_AXIOM);
        first.add(PERSON_MARY_AXIOM);
        first.addNegated(PERSON_JOHN_AXIOM);

        Model second = new Model();
        second.addNegated(PERSON_JOHN_AXIOM);
        second.addNegated(PERSON_JANE_AXIOM);

        Model third = new Model();
        third.addNegated(PERSON_JOHN_AXIOM);
        third.addNegated(PERSON_MARY_AXIOM);
        third.addNegated(PERSON_JANE_AXIOM);

        manager.models.add(second);
        manager.models.add(third);
        manager.models.add(first);


        assertEquals(3, manager.models.size());

        assertSame(first, new ArrayList<>(manager.models).get(0));
        assertSame(second, new ArrayList<>(manager.models).get(1));
        assertSame(third, new ArrayList<>(manager.models).get(2));
    }

    @Test
    void shouldNotStoreDuplicateModelsInTreeSet() {
        Model first = new Model();
        first.add(PERSON_JOHN_AXIOM);

        Model second = new Model();
        second.add(PERSON_JOHN_AXIOM2);

        manager.models.add(first);
        manager.models.add(second);

        assertEquals(1, manager.models.size());
    }

    @Test
    void shouldFindReusableModel() {
        Model model = new Model();
        model.add(PERSON_JOHN_AXIOM);

        Model correspondingModel = new Model();
        correspondingModel.add(PERSON_JOHN_AXIOM);

        manager.models.add(model);

        boolean found = manager.findReusableModel(correspondingModel);

        assertTrue(found);
        assertSame(model, manager.modelToReuse);
    }

    @Test
    void shouldNotFindReusableModel() {
        Model first = new Model();
        first.add(PERSON_JOHN_AXIOM);
        first.addNegated(PERSON_JANE_AXIOM);

        Model second = new Model();
        second.add(PERSON_JOHN_AXIOM);

        Model third = new Model();
        third.addNegated(PERSON_JANE_AXIOM);

        Model fourth = new Model();
        fourth.add(PERSON_JOHN_AXIOM);
        fourth.addNegated(PERSON_JANE_AXIOM);
        fourth.addNegated(PERSON_MARY_AXIOM);

        Model fifth = new Model();
        fifth.add(PERSON_JOHN_AXIOM);
        fifth.add(PERSON_MARY_AXIOM);
        fifth.addNegated(PERSON_JANE_AXIOM);

        manager.models.add(second);
        manager.models.add(third);
        manager.models.add(fourth);
        manager.models.add(fifth);

        boolean found = manager.findReusableModel(first);

        assertFalse(found);
        assertNull(manager.modelToReuse);
    }

    @Test
    void shouldFindModelContainingPath() {
        Model first = new Model();
        first.add(PERSON_JANE_AXIOM);

        Model second = new Model();
        second.add(PERSON_JOHN_AXIOM);
        second.add(PERSON_MARY_AXIOM);

        manager.models.add(first);
        manager.models.add(second);

        Set<OWLAxiom> path = new HashSet<>();
        path.add(PERSON_JOHN_AXIOM);

        boolean found = manager.findReuseModelForPath(path);

        assertTrue(found);
        assertSame(second, manager.modelToReuse);
    }

    @Test
    void shouldReturnFalseWhenPathIsNotFound() {
        Model model = new Model();
        model.add(PERSON_JOHN_AXIOM);

        manager.models.add(model);

        Set<OWLAxiom> path = new HashSet<>();
        path.add(PERSON_MARY_AXIOM);
        path.add(PERSON_JOHN_AXIOM);

        boolean found = manager.findReuseModelForPath(path);

        assertFalse(found);
        assertNull(manager.modelToReuse);
    }

    @Test
    void shouldFindModelWithTheSmallestNegDataContainingPath() {
        Model first = new Model();
        first.add(PERSON_JOHN_AXIOM);
        first.addNegated(PERSON_JANE_AXIOM);

        Model second = new Model();
        second.add(PERSON_JOHN_AXIOM);

        Model third = new Model();
        third.addNegated(PERSON_JANE_AXIOM);

        Model fourth = new Model();
        fourth.add(PERSON_JOHN_AXIOM);
        fourth.add(PERSON_MARY_AXIOM);
        fourth.addNegated(PERSON_JANE_AXIOM);
        fourth.addNegated(PERSON_EVE_AXIOM);

        Model fifth = new Model();
        fifth.add(PERSON_JOHN_AXIOM);
        fifth.add(PERSON_MARY_AXIOM);
        fifth.addNegated(PERSON_JANE_AXIOM);

        manager.models.add(first);
        manager.models.add(second);
        manager.models.add(third);
        manager.models.add(fourth);
        manager.models.add(fifth);

        Set<OWLAxiom> path = new HashSet<>();
        path.add(PERSON_JOHN_AXIOM);
        path.add(PERSON_MARY_AXIOM);

        boolean found = manager.findReuseModelForPath(path);

        assertTrue(found);
        assertSame(fifth, manager.modelToReuse);
    }


}
