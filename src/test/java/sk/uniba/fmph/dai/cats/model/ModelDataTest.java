package sk.uniba.fmph.dai.cats.model;

import org.junit.jupiter.api.Test;
import org.semanticweb.owlapi.model.OWLAxiom;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;


class ModelDataTest {

    public static final OWLAxiom PERSON_JOHN_AXIOM = Helper.createClassAssertion("john", "Person");
    public static final OWLAxiom PERSON_JOHN_AXIOM2 = Helper.createClassAssertion("john", "Person");

    public static final OWLAxiom PERSON_MARY_AXIOM = Helper.createClassAssertion("mary", "Person");
    public static final OWLAxiom PERSON_MARY_AXIOM2 = Helper.createClassAssertion("mary", "Person");

    @Test
    void newModelDataShouldBeEmpty() {
        ModelData data = new ModelData();

        assertTrue(data.isEmpty());
        assertEquals(0, data.size());
    }

    @Test
    void shouldBehaveLikeHashSetWhenAddingAxiom() {
        ModelData data = new ModelData();

        boolean added = data.add(PERSON_JOHN_AXIOM);

        assertTrue(added);
        assertTrue(data.contains(PERSON_JOHN_AXIOM));
        assertEquals(1, data.size());
    }


    @Test
    void shouldNotAddSameAxiomTwice() {
        ModelData data = new ModelData();
        data.add(PERSON_JOHN_AXIOM);
        data.add(PERSON_JOHN_AXIOM2);
        data.add(PERSON_JOHN_AXIOM);

        assertNotSame(PERSON_JOHN_AXIOM, PERSON_JOHN_AXIOM2);
        assertEquals(PERSON_JOHN_AXIOM, PERSON_JOHN_AXIOM2);

        assertEquals(1, data.size());
    }


    @Test
    void shouldRemoveAxiom() {
        ModelData data = new ModelData();
        data.add(PERSON_JOHN_AXIOM);

        boolean removed = data.remove(PERSON_JOHN_AXIOM);

        assertTrue(removed);
        assertFalse(data.contains(PERSON_JOHN_AXIOM));
        assertTrue(data.isEmpty());
    }


    @Test
    void collectionConstructorShouldCopyData() {
        Collection<OWLAxiom> collection = new HashSet<>();
        collection.add(PERSON_JOHN_AXIOM);

        ModelData data = new ModelData(collection);

        assertEquals(1, data.size());
        assertTrue(data.contains(PERSON_JOHN_AXIOM2));
    }

    @Test
    void collectionConstructorShouldCopyCollection() {
        Collection<OWLAxiom> collection = new HashSet<>();
        collection.add(PERSON_JOHN_AXIOM);

        ModelData data = new ModelData(collection);

        collection.clear();

        assertTrue(data.contains(PERSON_JOHN_AXIOM2));
    }

    @Test
    void emptyModelDataShouldCompareAsEqual() {
        ModelData first = new ModelData();
        ModelData second = new ModelData();

        assertEquals(0, first.compareTo(second));
    }

    @Test
    void modelDataWithSameAxiomsShouldCompareAsEqual() {
        ModelData first = new ModelData();
        first.add(PERSON_JOHN_AXIOM);

        ModelData second = new ModelData();
        second.add(PERSON_JOHN_AXIOM2);

        assertEquals(0, first.compareTo(second));
    }

    @Test
    void differentModelDataShouldNotCompareAsEqual() {
        ModelData first = new ModelData();
        first.add(PERSON_JOHN_AXIOM);

        ModelData second = new ModelData();
        second.add(PERSON_MARY_AXIOM);

        assertNotEquals(0, first.compareTo(second));
    }

    @Test
    void modelDataShouldCompareAsEqualRegardlessOfOrder() {
        ModelData first = new ModelData();
        first.add(PERSON_JOHN_AXIOM);
        first.add(PERSON_MARY_AXIOM);

        ModelData second = new ModelData();
        second.add(PERSON_MARY_AXIOM2);
        second.add(PERSON_JOHN_AXIOM2);

        assertEquals(0, first.compareTo(second));
    }

    @Test
    void compareToShouldBeConsistentWithEquals() {
        ModelData first = new ModelData();
        first.add(PERSON_JOHN_AXIOM);

        ModelData second = new ModelData();
        second.add(PERSON_JOHN_AXIOM2);

        assertEquals(first, second);
        assertEquals(0, first.compareTo(second));
    }

}