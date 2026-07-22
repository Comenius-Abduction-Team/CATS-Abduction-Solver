package sk.uniba.fmph.dai.cats.model;

import org.junit.jupiter.api.Test;
import org.semanticweb.owlapi.model.OWLAxiom;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.*;

class ModelTest {

    public static final OWLAxiom PERSON_JOHN_AXIOM = Helper.createClassAssertion("john", "Person");
    public static final OWLAxiom PERSON_JOHN_AXIOM2 = Helper.createClassAssertion("john", "Person");

    public static final OWLAxiom PERSON_MARY_AXIOM = Helper.createClassAssertion("mary", "Person");
    public static final OWLAxiom PERSON_MARY_AXIOM2 = Helper.createClassAssertion("mary", "Person");

    public static final OWLAxiom PERSON_JANE_AXIOM = Helper.createClassAssertion("jane", "Person");

    @Test
    void newModelShouldBeEmpty() {
        Model model = new Model();

        assertTrue(model.isEmpty());
        assertTrue(model.getData().isEmpty());
        assertTrue(model.getNegatedData().isEmpty());
    }

    @Test
    void emptyModelsShouldCompareAsEqual() {
        Model first = new Model();
        Model second = new Model();

        assertEquals(0, first.compareTo(second));
        assertEquals(first, second);
    }

    @Test
    void shouldAddPositiveAxiom() {
        Model model = new Model();
        model.add(PERSON_JOHN_AXIOM);

        assertTrue(model.getData().contains(PERSON_JOHN_AXIOM));
        assertFalse(model.isEmpty());
    }


    @Test
    void shouldAddNegatedAxiom() {
        Model model = new Model();
        model.addNegated(PERSON_JOHN_AXIOM);

        assertTrue(model.getNegatedData().contains(PERSON_JOHN_AXIOM));
        assertFalse(model.isEmpty());
    }

    //TODO ZAMYSLIET SA ako s tymto pracovat...
    // mozno nechat toto celkove empty a urobit isNegDataEmpty a pouzit na danom mieste kde treba
    @Test
    void shouldNotBeEmptyWhenOnlyPositiveDataExists() {
        Model model = new Model();
        model.add(PERSON_JOHN_AXIOM);

        assertFalse(model.isEmpty());
    }


    @Test
    void shouldNotBeEmptyWhenOnlyNegatedDataExists() {
        Model model = new Model();
        model.addNegated(PERSON_JOHN_AXIOM);

        assertFalse(model.isEmpty());
    }


    @Test
    void copyConstructorShouldCreateEqualModel() {
        Model original = new Model();
        original.add(PERSON_JOHN_AXIOM);
        original.addNegated(PERSON_MARY_AXIOM);

        Model copy = new Model(original);

        assertEquals(original, copy);
        assertEquals(original.getData(), copy.getData());
        assertEquals(original.getNegatedData(), copy.getNegatedData());
        assertNotSame(original.getData(), copy.getData());
        assertNotSame(original.getNegatedData(), copy.getNegatedData());
    }


    @Test
    void copyShouldBeIndependentFromOriginal() {
        Model original = new Model();
        original.add(PERSON_JOHN_AXIOM);

        Model copy = new Model(original);
        copy.add(PERSON_JANE_AXIOM);

        assertTrue(copy.getData().contains(PERSON_JANE_AXIOM));
        assertFalse(original.getData().contains(PERSON_JANE_AXIOM));
    }

    @Test
    void modifyingOriginalShouldNotModifyCopy() {
        Model original = new Model();
        original.add(PERSON_JOHN_AXIOM);

        Model copy = new Model(original);

        original.add(PERSON_JANE_AXIOM);

        assertFalse(copy.getData().contains(PERSON_JANE_AXIOM));
    }

    @Test
    void shouldConsiderDifferentAxiomObjectsWithSameContentAsEqual() {
        assertNotSame(PERSON_JOHN_AXIOM, PERSON_JOHN_AXIOM2);
        assertEquals(PERSON_JOHN_AXIOM, PERSON_JOHN_AXIOM2);

        Model first = new Model();
        first.add(PERSON_JOHN_AXIOM);

        Model second = new Model();
        second.add(PERSON_JOHN_AXIOM2);

        assertEquals(first.getData(), second.getData());
        assertEquals(first, second);
    }

    @Test
    void shouldNotContainDuplicateAxiomsWithSameContent() {
        assertNotSame(PERSON_JOHN_AXIOM, PERSON_JOHN_AXIOM2);
        assertEquals(PERSON_JOHN_AXIOM, PERSON_JOHN_AXIOM2);

        Model model = new Model();
        model.add(PERSON_JOHN_AXIOM);
        model.add(PERSON_JOHN_AXIOM2);

        assertEquals(1, model.getData().size());
    }


    @Test
    void modelsWithSameDataShouldBeEqual() {
        Model first = new Model();
        first.add(PERSON_JOHN_AXIOM);
        first.addNegated(PERSON_MARY_AXIOM);

        Model second = new Model();
        second.add(PERSON_JOHN_AXIOM2);
        second.addNegated(PERSON_MARY_AXIOM2);

        assertEquals(first, second);
    }


    @Test
    void modelsWithDifferentPositiveDataShouldNotBeEqual() {
        Model first = new Model();
        first.add(PERSON_JOHN_AXIOM);

        Model second = new Model();
        second.add(PERSON_MARY_AXIOM);

        assertNotEquals(first, second);
    }

    @Test
    void modelsWithDifferentNegatedDataShouldNotBeEqual() {
        Model first = new Model();
        first.add(PERSON_JOHN_AXIOM);

        Model second = new Model();
        second.add(PERSON_JOHN_AXIOM);
        second.addNegated(PERSON_MARY_AXIOM);

        assertNotEquals(first, second);
    }

    @Test
    void equalModelsShouldHaveSameHashCode() {
        Model first = new Model();
        first.add(PERSON_JOHN_AXIOM);
        first.addNegated(PERSON_MARY_AXIOM);

        Model second = new Model();
        second.add(PERSON_JOHN_AXIOM2);
        second.addNegated(PERSON_MARY_AXIOM2);

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void compareToShouldOrderModelsByNegatedDataSize() {
        Model smaller = new Model();
        smaller.addNegated(PERSON_JOHN_AXIOM);

        Model bigger = new Model();
        bigger.addNegated(PERSON_JOHN_AXIOM);
        bigger.addNegated(PERSON_MARY_AXIOM);

        assertTrue(smaller.compareTo(bigger) < 0);
        assertTrue(bigger.compareTo(smaller) > 0);
    }

    @Test
    void modelsWithTheSameDataShouldCompareAsEqual() {
        Model first = new Model();
        first.addNegated(PERSON_JOHN_AXIOM);
        first.add(PERSON_MARY_AXIOM);

        Model second = new Model();
        second.addNegated(PERSON_JOHN_AXIOM2);
        second.add(PERSON_MARY_AXIOM2);

        assertEquals(0, first.compareTo(second));
    }

    @Test
    void differentModelsWithSamePriorityShouldNotCompareAsEqual() {
        Model first = new Model();
        first.addNegated(PERSON_JOHN_AXIOM);

        Model second = new Model();
        second.addNegated(PERSON_MARY_AXIOM);

        assertNotEquals(0, first.compareTo(second));
    }


    @Test
    void toStringShouldContainStoredAxioms() {
        Model model = new Model();
        model.addNegated(PERSON_JOHN_AXIOM);

        assertTrue(model.toString().contains("john"));
    }

    @Test
    void shouldSortModelsAccordingToNumberOfNegatedAxioms() {
        Model smallest = new Model();
        smallest.addNegated(PERSON_JOHN_AXIOM);

        Model middle = new Model();
        middle.addNegated(PERSON_JOHN_AXIOM2);
        middle.addNegated(PERSON_MARY_AXIOM);

        Model biggest = new Model();
        biggest.addNegated(PERSON_JOHN_AXIOM);
        biggest.addNegated(PERSON_MARY_AXIOM2);
        biggest.addNegated(PERSON_JANE_AXIOM);

        List<Model> models = new ArrayList<>();
        models.add(middle);
        models.add(biggest);
        models.add(smallest);

        Collections.sort(models);

        assertSame(smallest, models.get(0));
        assertSame(middle, models.get(1));
        assertSame(biggest, models.get(2));
    }

    @Test
    void treeSetShouldKeepDifferentModelsWithSamePriority() {
        TreeSet<Model> models = new TreeSet<>();

        Model first = new Model();
        first.addNegated(PERSON_JOHN_AXIOM);

        Model second = new Model();
        second.addNegated(PERSON_MARY_AXIOM);

        models.add(first);
        models.add(second);

        assertEquals(2, models.size());
    }

    @Test
    void sameAxiomCannotExistInPositiveAndNegativeData() {
        Model model = new Model();
        model.add(PERSON_JOHN_AXIOM);

        assertThrows(IllegalStateException.class, () -> model.addNegated(PERSON_JOHN_AXIOM));
    }

    @Test
    void sameAxiomCannotExistInPositiveAndNegativeData2() {
        Model model = new Model();
        model.addNegated(PERSON_JOHN_AXIOM);

        assertThrows(IllegalStateException.class, () -> model.add(PERSON_JOHN_AXIOM));
    }

    @Test
    void equivalentAxiomCannotExistInPositiveAndNegativeData() {
        Model model = new Model();
        model.add(PERSON_JOHN_AXIOM);

        assertThrows(IllegalStateException.class, () -> model.addNegated(PERSON_JOHN_AXIOM2));
    }
}