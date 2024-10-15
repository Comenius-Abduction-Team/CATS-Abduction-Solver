package sk.uniba.fmph.dai.cats.parser;

import sk.uniba.fmph.dai.cats.common.Configuration;
import sk.uniba.fmph.dai.cats.common.IPrinter;
import org.semanticweb.owlapi.model.*;
import sk.uniba.fmph.dai.cats.reasoner.Loader;

import java.util.*;

public abstract class ObservationParser {

    protected Loader loader;
    protected IPrinter printer;

    public ObservationParser(Loader loader){
        this.loader = loader;
    }

    protected abstract void createOntologyFromObservation() throws OWLOntologyCreationException, OWLOntologyStorageException;

    public void parse() {
        try{
            createOntologyFromObservation();
        } catch (OWLOntologyCreationException | OWLOntologyStorageException e){
            throw new RuntimeException("Probem while creating ontology observation");
        }
        printer.logInfo("Observation: ".concat(Configuration.OBSERVATION));
    }

    protected void processAxiomsFromObservation(OWLOntology observationOntology){
        Set<OWLAxiom> set = observationOntology.getAxioms();
        List<OWLAxiom> resultingObservation = new ArrayList<>();

        for (OWLAxiom axiom : set){
            AxiomType<?> type = axiom.getAxiomType();
            if(AxiomType.CLASS_ASSERTION == type || AxiomType.OBJECT_PROPERTY_ASSERTION == type || AxiomType.NEGATIVE_OBJECT_PROPERTY_ASSERTION == type) {
                resultingObservation.add(axiom);
            }
        }

        if(resultingObservation.size() > 0){
            chooseProcessingObservationAccordingType(resultingObservation);
        } else {
            String message = "In -o is not founded any correct observation - class assertion, object property assertion or negative object property assertion.";
            throw new RuntimeException(message);
        }
    }

    private void chooseProcessingObservationAccordingType(List<OWLAxiom> observationsAxioms){
        //single obs / complex obs
        if(observationsAxioms.size() == 1){
            loader.setObservation(observationsAxioms.get(0));

            if(observationsAxioms.get(0).getAxiomType() == AxiomType.CLASS_ASSERTION){
                processClassAssertionAxiom( (OWLClassAssertionAxiom) observationsAxioms.get(0));

            } else if (observationsAxioms.get(0).getAxiomType() == AxiomType.OBJECT_PROPERTY_ASSERTION){
                processObjectPropertyAssertionAxiom( (OWLObjectPropertyAssertionAxiom) observationsAxioms.get(0));

            } else if (observationsAxioms.get(0).getAxiomType() == AxiomType.NEGATIVE_OBJECT_PROPERTY_ASSERTION){
                processNegativeObjectPropertyAssertionAxiom( (OWLNegativeObjectPropertyAssertionAxiom) observationsAxioms.get(0));
            }

        //multiple obs
        } else {
            loader.setMultipleObservationOnInput(true);

            OWLNamedIndividual reductionIndividual = loader.getDataFactory().getOWLNamedIndividual("http://www.co-ode.org/ontologies/ont.owl#s" + System.currentTimeMillis());
            OWLClassExpression reductionClass = createReducedClassFromMultipleObservations(observationsAxioms);
            processMultipleObservations(reductionClass, observationsAxioms, reductionIndividual);
        }

        loader.initializeReasoner();
    }

    private void processClassAssertionAxiom(OWLClassAssertionAxiom axiom){
        OWLClassExpression axiomClass = axiom.getClassExpression();
        OWLNamedIndividual axiomIndividual = axiom.getIndividual().asOWLNamedIndividual();

        addIndividualToLoader(axiomIndividual);

        OWLAxiom negAxiom = loader.getDataFactory().getOWLClassAssertionAxiom(axiomClass.getComplementNNF(), axiomIndividual);
        loader.setNegObservation(negAxiom);
    }

    private void processObjectPropertyAssertionAxiom(OWLObjectPropertyAssertionAxiom axiom){
        OWLObjectPropertyExpression axiomProperty = axiom.getProperty();
        OWLNamedIndividual axiomSubject = axiom.getSubject().asOWLNamedIndividual();
        OWLNamedIndividual axiomObject = axiom.getObject().asOWLNamedIndividual();

        addIndividualToLoader(axiomSubject);
        addIndividualToLoader(axiomObject);

        OWLAxiom negAxiom = loader.getDataFactory().getOWLNegativeObjectPropertyAssertionAxiom(axiomProperty, axiomSubject, axiomObject);
        loader.setNegObservation(negAxiom);
    }

    private void processNegativeObjectPropertyAssertionAxiom(OWLNegativeObjectPropertyAssertionAxiom axiom){
        OWLObjectPropertyExpression axiomProperty = axiom.getProperty();
        OWLNamedIndividual axiomSubject = axiom.getSubject().asOWLNamedIndividual();
        OWLNamedIndividual axiomObject = axiom.getObject().asOWLNamedIndividual();

        addIndividualToLoader(axiomSubject);
        addIndividualToLoader(axiomObject);

        OWLAxiom negAxiom = loader.getDataFactory().getOWLObjectPropertyAssertionAxiom(axiomProperty, axiomSubject, axiomObject);
        loader.setNegObservation(negAxiom);
    }


    private void addIndividualToLoader(OWLNamedIndividual individual){
        loader.addNamedIndividual(individual);
        loader.getOntologyManager().addAxiom(loader.getOntology(), loader.getDataFactory().getOWLDeclarationAxiom(individual));
        loader.getOntologyManager().addAxiom(loader.getOriginalOntology(), loader.getDataFactory().getOWLDeclarationAxiom(individual));
    }

    private OWLClassExpression createReducedClassFromMultipleObservations(List<OWLAxiom> resultingObservation){
        List<OWLClassExpression> unions = new ArrayList<>();

        for(OWLAxiom axiom : resultingObservation){
            OWLClassExpression union = createOneUnion(axiom);
            unions.add(union);
        }

        return loader.getDataFactory().getOWLObjectIntersectionOf(unions);
    }

    private OWLClassExpression createOneUnion(OWLAxiom axiom){
        OWLClassExpression union = null;

        if(axiom.getAxiomType() == AxiomType.CLASS_ASSERTION){
            OWLClassAssertionAxiom classAssertionAxiom = (OWLClassAssertionAxiom) axiom;
            //urcim ze to nie je rovnaky individual
            OWLObjectOneOf individualFromAxiom = loader.getDataFactory().getOWLObjectOneOf(classAssertionAxiom.getIndividual());
            OWLClassExpression differentIndividuals = individualFromAxiom.getObjectComplementOf();

            union = loader.getDataFactory().getOWLObjectUnionOf(classAssertionAxiom.getClassExpression(), differentIndividuals);

        } else if (axiom.getAxiomType() == AxiomType.OBJECT_PROPERTY_ASSERTION){
            OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiom = (OWLObjectPropertyAssertionAxiom) axiom;

            OWLObjectOneOf individualFromAxiom = loader.getDataFactory().getOWLObjectOneOf(objectPropertyAssertionAxiom.getSubject());
            OWLClassExpression differentIndividuals = individualFromAxiom.getObjectComplementOf();

            OWLObjectHasValue existentialRestriction = loader.getDataFactory().getOWLObjectHasValue(objectPropertyAssertionAxiom.getProperty(), objectPropertyAssertionAxiom.getObject());
            union = loader.getDataFactory().getOWLObjectUnionOf(existentialRestriction, differentIndividuals);

        } else if (axiom.getAxiomType() == AxiomType.NEGATIVE_OBJECT_PROPERTY_ASSERTION){
            OWLNegativeObjectPropertyAssertionAxiom negativeObjectPropertyAssertionAxiom = (OWLNegativeObjectPropertyAssertionAxiom) axiom;

            OWLObjectOneOf individualFromAxiom = loader.getDataFactory().getOWLObjectOneOf(negativeObjectPropertyAssertionAxiom.getSubject());
            OWLClassExpression differentIndividuals = individualFromAxiom.getObjectComplementOf();

            OWLObjectOneOf objectFromAxiom = loader.getDataFactory().getOWLObjectOneOf(negativeObjectPropertyAssertionAxiom.getObject());
            OWLClassExpression differentObject = objectFromAxiom.getObjectComplementOf();

            OWLObjectAllValuesFrom valueRestriction = loader.getDataFactory().getOWLObjectAllValuesFrom(negativeObjectPropertyAssertionAxiom.getProperty(), differentObject);
            union = loader.getDataFactory().getOWLObjectUnionOf(valueRestriction, differentIndividuals);
        }

        if(union == null){
            String message = "In -o one of the multiple observations is different from class assertion, object property assertion or negative object property assertion axiom.";
            throw new RuntimeException(message);
        }
        return union;
    }

    private void processMultipleObservations(OWLClassExpression reductionClass, List<OWLAxiom> observationsAxioms, OWLNamedIndividual reductionIndividual){
        OWLAxiom observationAfterReduction = loader.getDataFactory().getOWLClassAssertionAxiom(reductionClass, reductionIndividual);
        loader.setObservation(observationAfterReduction, observationsAxioms, reductionIndividual);
        processClassAssertionAxiom( (OWLClassAssertionAxiom) observationAfterReduction);

        Set<OWLNamedIndividual> individualsUsedAsNominal = reductionClass.getIndividualsInSignature();
        for(OWLNamedIndividual i : individualsUsedAsNominal){
            addIndividualToLoader(i);
        }

        //asi to tu nemusi byt
        addIndividualToLoader(reductionIndividual);

        OWLAxiom negAxiom = loader.getDataFactory().getOWLClassAssertionAxiom(reductionClass.getComplementNNF(), reductionIndividual);
        loader.setNegObservation(negAxiom);
    }
}

