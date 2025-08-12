package sk.uniba.fmph.dai.cats.reasoner;

import org.semanticweb.owlapi.model.*;
import sk.uniba.fmph.dai.cats.common.Configuration;
import sk.uniba.fmph.dai.cats.common.DLSyntax;
import sk.uniba.fmph.dai.cats.common.StringFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class AxiomManager {

    public static List<OWLAxiom> createClassAssertionAxiom(Loader loader, OWLClass owlClass) {
        List<OWLAxiom> owlAxioms = new LinkedList<>();

        if (owlClass != null) {
            for (OWLNamedIndividual namedIndividual : loader.getAbducibles().getIndividuals()) {
                if(!loader.isMultipleObservationOnInput() || loader.getObservationReductionIndividual() != namedIndividual){
                    owlAxioms.add(loader.getDataFactory().getOWLClassAssertionAxiom(owlClass, namedIndividual));
                    owlAxioms.add(loader.getDataFactory().getOWLClassAssertionAxiom(owlClass.getComplementNNF(), namedIndividual));
                }
            }
        }
        return owlAxioms;
    }

    public static List<OWLAxiom> createObjectPropertyAssertionAxiom(Loader loader, OWLObjectProperty objectProperty) {
        List<OWLAxiom> owlAxioms = new LinkedList<>();

        if (objectProperty != null) {
            for (OWLNamedIndividual subject : loader.getAbducibles().getIndividuals()) {
                if(!loader.isMultipleObservationOnInput() || subject != loader.getObservationReductionIndividual()){
                    for (OWLNamedIndividual object : loader.getAbducibles().getIndividuals()) {
                        if (Configuration.LOOPING_ALLOWED || !subject.equals(object)) {
                            if(!loader.isMultipleObservationOnInput() || object != loader.getObservationReductionIndividual()){
                                owlAxioms.add(loader.getDataFactory().getOWLObjectPropertyAssertionAxiom(objectProperty, subject, object));
                                owlAxioms.add(loader.getDataFactory().getOWLNegativeObjectPropertyAssertionAxiom(objectProperty, subject, object));
                            }
                        }
                    }
                }
            }
        }
        return owlAxioms;
    }

    public static OWLAxiom getComplementOfOWLAxiom(Loader loader, OWLAxiom owlAxiom) {
        OWLAxiom complement = null;
        if(owlAxiom.getAxiomType() == AxiomType.CLASS_ASSERTION){
            OWLClassExpression owlClassExpression = ((OWLClassAssertionAxiom) owlAxiom).getClassExpression();
            complement = loader.getDataFactory().getOWLClassAssertionAxiom(owlClassExpression.getComplementNNF(), ((OWLClassAssertionAxiom) owlAxiom).getIndividual());
        } else if (owlAxiom.getAxiomType() == AxiomType.OBJECT_PROPERTY_ASSERTION){
            OWLObjectPropertyExpression owlObjectProperty = ((OWLObjectPropertyAssertionAxiom) owlAxiom).getProperty();
            complement = loader.getDataFactory().getOWLNegativeObjectPropertyAssertionAxiom(owlObjectProperty, ((OWLObjectPropertyAssertionAxiom) owlAxiom).getSubject(), ((OWLObjectPropertyAssertionAxiom) owlAxiom).getObject());
        } else if (owlAxiom.getAxiomType() == AxiomType.NEGATIVE_OBJECT_PROPERTY_ASSERTION){
            OWLObjectPropertyExpression owlObjectProperty = ((OWLNegativeObjectPropertyAssertionAxiom) owlAxiom).getProperty();
            complement = loader.getDataFactory().getOWLObjectPropertyAssertionAxiom(owlObjectProperty, ((OWLNegativeObjectPropertyAssertionAxiom) owlAxiom).getSubject(), ((OWLNegativeObjectPropertyAssertionAxiom) owlAxiom).getObject());
        }
        return complement;
    }

    public static boolean isNegatedClassAssertion(OWLAxiom axiom){

        if (!axiom.isOfType(AxiomType.CLASS_ASSERTION))
            return false;

        return DLSyntax.containsNegation(StringFactory.extractClassName(axiom));
    }

    public static boolean isNegatedRoleAssertion(OWLAxiom axiom){

        return axiom.isOfType(AxiomType.NEGATIVE_OBJECT_PROPERTY_ASSERTION);

    }
}
