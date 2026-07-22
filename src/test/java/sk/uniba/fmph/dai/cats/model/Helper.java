package sk.uniba.fmph.dai.cats.model;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

public class Helper {

    public static OWLAxiom createClassAssertion(String individualName, String className) {
        OWLDataFactory df = OWLManager.getOWLDataFactory();

        OWLNamedIndividual individual =
                df.getOWLNamedIndividual(IRI.create("http://example.com/" + individualName));

        OWLClass owlClass =
                df.getOWLClass(IRI.create("http://example.com/" + className));

        return df.getOWLClassAssertionAxiom(owlClass, individual);
    }

    public static OWLAxiom createNegativeClassAssertion(String individualName, String className) {
        OWLDataFactory df = OWLManager.getOWLDataFactory();

        OWLNamedIndividual individual =
                df.getOWLNamedIndividual(IRI.create("http://example.com/" + individualName));

        OWLClass owlClass =
                df.getOWLClass(IRI.create("http://example.com/" + className));

        return df.getOWLClassAssertionAxiom(df.getOWLObjectComplementOf(owlClass), individual);
    }

}
