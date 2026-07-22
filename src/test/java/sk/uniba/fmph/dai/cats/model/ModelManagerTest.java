package sk.uniba.fmph.dai.cats.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.semanticweb.owlapi.model.OWLAxiom;
import sk.uniba.fmph.dai.cats.algorithms.AlgorithmSolver;
import sk.uniba.fmph.dai.cats.common.Configuration;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ModelManagerTest {

    private AlgorithmSolver solver;

    private ModelExtractor extractor;

    public static final OWLAxiom PERSON_JOHN_AXIOM = Helper.createClassAssertion("john", "Person");
    public static final OWLAxiom PERSON_JOHN_AXIOM2 = Helper.createClassAssertion("john", "Person");

    public static final OWLAxiom PERSON_MARY_AXIOM = Helper.createClassAssertion("mary", "Person");
    public static final OWLAxiom PERSON_MARY_AXIOM2 = Helper.createClassAssertion("mary", "Person");

    public static final OWLAxiom PERSON_JANE_AXIOM = Helper.createClassAssertion("jane", "Person");

    public static final OWLAxiom PERSON_EVE_AXIOM = Helper.createClassAssertion("eve", "Person");

    private ModelManager manager;

    @BeforeEach
    void setUp() {
        Configuration.EVENTS = false;

        solver = mock(AlgorithmSolver.class);
        extractor = mock(ModelExtractor.class);

        manager = new ModelManager(solver);
        manager.setExtractor(extractor);
    }

    @Test
    void constructorCreatesEmptyCollection() {
        assertTrue(manager.models.isEmpty());
    }

    @Test
    void shouldStoreNewModel() {
        Model model = new Model();
        model.add(PERSON_JOHN_AXIOM);

        when(extractor.extractModel()).thenReturn(model);

        manager.storeModelFoundByConsistencyCheck();

        assertFalse(model.isEmpty());
        assertEquals(1, manager.models.size());
        assertTrue(manager.models.contains(model));
    }

    @Test
    void shouldNotStoreEmptyModel() {
        Model model = new Model();

        when(extractor.extractModel()).thenReturn(model);

        manager.storeModelFoundByConsistencyCheck();

        assertTrue(model.isEmpty());
        assertEquals(0, manager.models.size());
    }

    @Test
    void shouldNotStoreSameModelTwice() {
        Model model = new Model();
        model.add(PERSON_JOHN_AXIOM);
        model.addNegated(PERSON_MARY_AXIOM);

        when(extractor.extractModel())
                .thenReturn(model)
                .thenReturn(model);

        manager.storeModelFoundByConsistencyCheck();
        manager.storeModelFoundByConsistencyCheck();

        assertEquals(1, manager.models.size());
    }

    @Test
    void shouldNotStoreDifferentInstanceWithSameData() {
        Model first = new Model();
        first.add(PERSON_JOHN_AXIOM);
        first.addNegated(PERSON_MARY_AXIOM);

        Model second = new Model();
        second.add(PERSON_JOHN_AXIOM2);
        second.addNegated(PERSON_MARY_AXIOM2);

        when(extractor.extractModel())
                .thenReturn(first)
                .thenReturn(second);

        manager.storeModelFoundByConsistencyCheck();
        manager.storeModelFoundByConsistencyCheck();

        assertEquals(1, manager.models.size());
    }

    @Test
    void shouldStoreDifferentModels() {
        Model first = new Model();
        first.add(PERSON_JOHN_AXIOM);
        first.addNegated(PERSON_MARY_AXIOM);

        Model second = new Model();
        second.add(PERSON_JOHN_AXIOM);

        when(extractor.extractModel())
                .thenReturn(first)
                .thenReturn(second);

        manager.storeModelFoundByConsistencyCheck();
        manager.storeModelFoundByConsistencyCheck();

        assertEquals(2, manager.models.size());
    }

    @Test
    void shouldSetNewlyStoredModelAsModelToReuse() {
        Model model = new Model();
        model.add(PERSON_JOHN_AXIOM);

        when(extractor.extractModel()).thenReturn(model);

        manager.storeModelFoundByConsistencyCheck();

        assertSame(model, manager.modelToReuse);
    }

    @Test
    void shouldFindStoredModelAsReusable() {
        Model model = new Model();
        model.add(PERSON_JOHN_AXIOM);

        manager.models.add(model);

        assertTrue(manager.findReusableModel(model));
        assertSame(model, manager.modelToReuse);
    }

    @Test
    void shouldFindReuseModelForEmptyPath() {
        Model model = new Model();
        model.add(PERSON_JOHN_AXIOM);

        manager.models.add(model);

        Model reused = manager.findAndGetModelToReuse(new HashSet<>());

        assertSame(model, reused);
    }

    @Test
    void shouldFindReuseModelForAPath() {
        Model model = new Model();
        model.add(PERSON_JOHN_AXIOM);
        model.add(PERSON_MARY_AXIOM);

        manager.models.add(model);

        assertNull(manager.modelToReuse);

        Set<OWLAxiom> path = new HashSet<>();
        path.add(PERSON_JOHN_AXIOM2);

        Model reused = manager.findAndGetModelToReuse(path);
        assertSame(model, reused);
    }

    @Test
    void shouldSetModelToReuseWhenDuplicateModelIsFound() {
        Model storedModel = new Model();
        storedModel.add(PERSON_JOHN_AXIOM);
        storedModel.addNegated(PERSON_MARY_AXIOM);

        Model duplicateModel = new Model();
        duplicateModel.add(PERSON_JOHN_AXIOM2);
        duplicateModel.addNegated(PERSON_MARY_AXIOM2);

        when(extractor.extractModel())
                .thenReturn(storedModel)
                .thenReturn(duplicateModel);

        manager.storeModelFoundByConsistencyCheck();
        manager.storeModelFoundByConsistencyCheck();

        assertEquals(1, manager.models.size());
        assertSame(storedModel, manager.modelToReuse);
        assertNotSame(duplicateModel, manager.modelToReuse);
    }

    @Test
    void shouldReturnExistingModelForReuseAfterDuplicateIsFound() {
        Model storedModel = new Model();
        storedModel.add(PERSON_JOHN_AXIOM);
        storedModel.addNegated(PERSON_MARY_AXIOM);

        Model duplicateModel = new Model();
        duplicateModel.add(PERSON_JOHN_AXIOM2);
        duplicateModel.addNegated(PERSON_MARY_AXIOM2);

        when(extractor.extractModel())
                .thenReturn(storedModel)
                .thenReturn(duplicateModel);

        manager.storeModelFoundByConsistencyCheck();
        manager.storeModelFoundByConsistencyCheck();

        Model reused = manager.findAndGetModelToReuse(new HashSet<>());
        assertTrue(manager.findReusableModel(storedModel));
        assertSame(storedModel, reused);
    }

    @Test
    void shouldKeepModelsInInsertionOrder() {
        Model first = new Model();
        first.add(PERSON_JOHN_AXIOM);
        first.addNegated(PERSON_MARY_AXIOM);

        Model second = new Model();
        second.add(PERSON_JANE_AXIOM);
        second.addNegated(PERSON_EVE_AXIOM);

        Model third = new Model();
        third.add(PERSON_MARY_AXIOM);
        third.addNegated(PERSON_JOHN_AXIOM);

        when(extractor.extractModel())
                .thenReturn(first)
                .thenReturn(second)
                .thenReturn(third);

        manager.storeModelFoundByConsistencyCheck();
        manager.storeModelFoundByConsistencyCheck();
        manager.storeModelFoundByConsistencyCheck();

        assertEquals(3, manager.models.size());

        assertSame(first, ((List<Model>) manager.models).get(0));
        assertSame(second, ((List<Model>) manager.models).get(1));
        assertSame(third, ((List<Model>) manager.models).get(2));
    }

    @Test
    void shouldFindLastMatchingModel() {
        Model first = new Model();
        first.add(PERSON_JOHN_AXIOM);
        first.addNegated(PERSON_MARY_AXIOM);

        Model second = new Model();
        second.add(PERSON_JANE_AXIOM);

        Model third = new Model();
        third.add(PERSON_JOHN_AXIOM);
        third.addNegated(PERSON_MARY_AXIOM);

        manager.models.add(first);
        manager.models.add(second);
        manager.models.add(third);

        boolean found = manager.findReusableModel(first);

        assertTrue(found);
        assertSame(third, manager.modelToReuse);
    }

    @Test
    void shouldReturnFalseWhenReusableModelDoesNotExist() {
        Model stored = new Model();
        stored.add(PERSON_JOHN_AXIOM);

        manager.models.add(stored);

        Model searched = new Model();
        searched.add(PERSON_MARY_AXIOM);

        boolean found = manager.findReusableModel(searched);

        assertFalse(found);
    }

    @Test
    void shouldFindModelContainingPath() {
        Model first = new Model();
        first.add(PERSON_JOHN_AXIOM);
        first.add(PERSON_MARY_AXIOM);

        Model second = new Model();
        second.add(PERSON_JOHN_AXIOM);
        second.add(PERSON_JANE_AXIOM);

        manager.models.add(first);
        manager.models.add(second);

        Set<OWLAxiom> path = new HashSet<>();
        path.add(PERSON_MARY_AXIOM);

        boolean found = manager.findReuseModelForPath(path);

        assertTrue(found);
        assertSame(first, manager.modelToReuse);
    }

    @Test
    void shouldNotFindModelWhenPathIsNotFullyContained() {
        Model model = new Model();
        model.add(PERSON_JOHN_AXIOM);

        manager.models.add(model);

        Set<OWLAxiom> path = new HashSet<>();
        path.add(PERSON_JOHN_AXIOM);
        path.add(PERSON_MARY_AXIOM);

        boolean found = manager.findReuseModelForPath(path);

        assertFalse(found);
        assertNull(manager.modelToReuse);
    }

    @Test
    void shouldFindNewestModelContainingPath() {
        Model older = new Model();
        older.add(PERSON_JOHN_AXIOM);

        Model newer = new Model();
        newer.add(PERSON_JOHN_AXIOM);
        newer.add(PERSON_MARY_AXIOM);

        manager.models.add(older);
        manager.models.add(newer);

        Set<OWLAxiom> path = new HashSet<>();
        path.add(PERSON_JOHN_AXIOM);

        boolean found = manager.findReuseModelForPath(path);

        assertTrue(found);
        assertSame(newer, manager.modelToReuse);
    }

    @Test
    void shouldClearModelToReuseWhenNoModelMatchesPath() {
        Model toReuse = new Model();
        toReuse.add(PERSON_MARY_AXIOM);

        Model model = new Model();
        model.add(PERSON_JOHN_AXIOM);

        manager.modelToReuse = toReuse;

        manager.models.add(model);
        manager.models.add(toReuse);

        Set<OWLAxiom> path = new HashSet<>();
        path.add(PERSON_MARY_AXIOM);
        path.add(PERSON_JANE_AXIOM);

        boolean found = manager.findReuseModelForPath(path);

        assertFalse(found);
        assertNull(manager.modelToReuse);
    }

    @Test
    void shouldReturnAlreadySetModelToReuseWhenItContainsPath() {
        Model storedModel = new Model();
        storedModel.add(PERSON_JOHN_AXIOM);
        storedModel.add(PERSON_JANE_AXIOM);

        Model anotherModel = new Model();
        anotherModel.add(PERSON_JOHN_AXIOM);
        anotherModel.add(PERSON_MARY_AXIOM);

        manager.models.add(storedModel);
        manager.models.add(anotherModel);

        manager.modelToReuse = storedModel;

        Set<OWLAxiom> path = new HashSet<>();
        path.add(PERSON_JOHN_AXIOM);

        Model result = manager.findAndGetModelToReuse(path);

        assertSame(storedModel, result);
    }

    @Test
    void shouldFindAnotherModelWhenCurrentModelToReuseDoesNotContainPath() {
        Model storedModel = new Model();
        storedModel.add(PERSON_JOHN_AXIOM);

        Model matchingModel = new Model();
        matchingModel.add(PERSON_MARY_AXIOM);

        manager.models.add(storedModel);
        manager.models.add(matchingModel);

        manager.modelToReuse = storedModel;

        Set<OWLAxiom> path = new HashSet<>();
        path.add(PERSON_MARY_AXIOM);

        Model result = manager.findAndGetModelToReuse(path);

        assertSame(matchingModel, result);
    }

    @Test
    void shouldReturnNullWhenNoModelContainsPath() {
        Model storedModel = new Model();
        storedModel.add(PERSON_JOHN_AXIOM);

        manager.models.add(storedModel);

        manager.modelToReuse = storedModel;

        Set<OWLAxiom> path = new HashSet<>();
        path.add(PERSON_MARY_AXIOM);

        Model result = manager.findAndGetModelToReuse(path);

        assertNull(result);
    }

    @Test
    void shouldReturnNullWhenNoReusableModelExists() {
        Model model = new Model();
        model.add(PERSON_JOHN_AXIOM);

        manager.models.add(model);

        Set<OWLAxiom> path = new HashSet<>();
        path.add(PERSON_MARY_AXIOM);

        assertNull(manager.modelToReuse);

        Model result = manager.findAndGetModelToReuse(path);

        assertNull(result);
    }

//    @Test
//    void shouldReturnModelWithoutSpecifiedNegatedAxioms() {
//        Model original = new Model();
//        original.add(PERSON_JOHN_AXIOM);
//        original.addNegated(PERSON_MARY_AXIOM);
//        original.addNegated(PERSON_JANE_AXIOM);
//
//        Collection<OWLAxiom> axiomsToRemove = new HashSet<>();
//        axiomsToRemove.add(PERSON_MARY_AXIOM);
//
//        Model result = manager.getModelWithoutAxioms(
//                original,
//                axiomsToRemove
//        );
//
//        assertFalse(result.getNegatedData().contains(PERSON_MARY_AXIOM));
//        assertTrue(result.getNegatedData().contains(PERSON_JANE_AXIOM));
//    }
//
//    @Test
//    void shouldNotModifyOriginalModelWhenRemovingAxioms() {
//        Model original = new Model();
//        original.add(PERSON_JOHN_AXIOM);
//        original.addNegated(PERSON_MARY_AXIOM);
//        original.addNegated(PERSON_JANE_AXIOM);
//
//        Collection<OWLAxiom> axiomsToRemove = new HashSet<>();
//        axiomsToRemove.add(PERSON_MARY_AXIOM);
//
//        Model result = manager.getModelWithoutAxioms(
//                original,
//                axiomsToRemove
//        );
//
//        assertTrue(original.getNegatedData().contains(PERSON_MARY_AXIOM));
//        assertFalse(result.getNegatedData().contains(PERSON_MARY_AXIOM));
//    }

}