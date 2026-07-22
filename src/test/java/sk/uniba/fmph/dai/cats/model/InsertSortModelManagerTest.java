package sk.uniba.fmph.dai.cats.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.semanticweb.owlapi.model.OWLAxiom;
import sk.uniba.fmph.dai.cats.algorithms.AlgorithmSolver;
import sk.uniba.fmph.dai.cats.common.Configuration;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

//TODO
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
    void shouldStoreModelsInSortedOrder() {
        Model first = new Model();
        first.add(PERSON_JOHN_AXIOM);

        Model second = new Model();
        second.add(PERSON_MARY_AXIOM);

        Model third = new Model();
        third.add(PERSON_JANE_AXIOM);

        manager.models.add(second);
        manager.models.add(third);
        manager.models.add(first);

        List<Model> sorted = new ArrayList<>(manager.models);

        assertEquals(3, sorted.size());

        assertTrue(sorted.get(0).compareTo(sorted.get(1)) <= 0);
        assertTrue(sorted.get(1).compareTo(sorted.get(2)) <= 0);
    }

    @Test
    void shouldNotStoreDuplicateModelsInTreeSet() {
        Model first = new Model();
        first.add(PERSON_JOHN_AXIOM);

        Model second = new Model();
        second.add(PERSON_JOHN_AXIOM);

        manager.models.add(first);
        manager.models.add(second);

        assertEquals(1, manager.models.size());
    }

    @Test
    void shouldFindReusableModel() {
        Model model = new Model();
        model.add(PERSON_JOHN_AXIOM);

        manager.models.add(model);

        boolean found = manager.findReusableModel(model);

        assertTrue(found);
        assertSame(model, manager.modelToReuse);
    }

    @Test
    void shouldFindModelUsingTreeSetOrder() {
        Model first = new Model();
        first.add(PERSON_JOHN_AXIOM);

        Model second = new Model();
        second.add(PERSON_JOHN_AXIOM);

        manager.models.add(first);
        manager.models.add(second);

        assertTrue(manager.findReusableModel(first));

        assertNotNull(manager.modelToReuse);
    }

    @Test
    void shouldFindModelContainingPath() {
        Model first = new Model();
        first.add(PERSON_JOHN_AXIOM);
        first.add(PERSON_MARY_AXIOM);

        Model second = new Model();
        second.add(PERSON_JANE_AXIOM);

        manager.models.add(first);
        manager.models.add(second);

        Set<OWLAxiom> path = new HashSet<>();
        path.add(PERSON_JOHN_AXIOM);

        boolean found =
                manager.findReuseModelForPath(path);

        assertTrue(found);
        assertSame(first, manager.modelToReuse);
    }

    @Test
    void shouldReturnFalseWhenPathIsNotFound() {
        Model model = new Model();
        model.add(PERSON_JOHN_AXIOM);

        manager.models.add(model);

        Set<OWLAxiom> path = new HashSet<>();
        path.add(PERSON_MARY_AXIOM);

        boolean found =
                manager.findReuseModelForPath(path);

        assertFalse(found);
        assertNull(manager.modelToReuse);
    }

    @Test
    void shouldKeepCollectionSortedAfterAddingModels() {
        Model a = new Model();
        a.add(PERSON_JANE_AXIOM);

        Model b = new Model();
        b.add(PERSON_JOHN_AXIOM);

        Model c = new Model();
        c.add(PERSON_MARY_AXIOM);

        manager.models.add(a);
        manager.models.add(b);
        manager.models.add(c);

        Model previous = null;

        for (Model current : manager.models) {
            if (previous != null) {
                assertTrue(previous.compareTo(current) <= 0);
            }
            previous = current;
        }
    }

    //TODO, toto ale naopak .. chceme prave ten mensi ziskat
    @Test
    void shouldFindNewestModelContainingPath() {
        Model oldModel = new Model();
        oldModel.add(PERSON_JOHN_AXIOM);

        Model newestModel = new Model();
        newestModel.add(PERSON_JOHN_AXIOM);
        newestModel.add(PERSON_MARY_AXIOM);

        manager.models.add(oldModel);
        manager.models.add(newestModel);

        Set<OWLAxiom> path = new HashSet<>();
        path.add(PERSON_JOHN_AXIOM);

        boolean found = manager.findReuseModelForPath(path);

        assertTrue(found);
        assertSame(newestModel, manager.modelToReuse);
    }


}
