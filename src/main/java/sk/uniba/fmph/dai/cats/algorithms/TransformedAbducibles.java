package sk.uniba.fmph.dai.cats.algorithms;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import sk.uniba.fmph.dai.cats.common.Configuration;
import sk.uniba.fmph.dai.cats.data.InputAbducibles;
import sk.uniba.fmph.dai.cats.reasoner.AxiomManager;
import sk.uniba.fmph.dai.cats.reasoner.Loader;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * Helper object that extracts collections of axioms from the Abducible object that was created from user input.
 */
public class TransformedAbducibles {

    public final List<OWLAxiom> assertionAxioms = new ArrayList<>();
    public final List<OWLAxiom> negAssertionAxioms = new ArrayList<>();

    public Set<OWLAxiom> abducibleAxioms;

    TransformedAbducibles(Loader loader){

        InputAbducibles inputAbducibles = loader.getAbducibles();

        if(loader.isAxiomBasedAbduciblesOnInput()){
            Set<OWLAxiom> abduciblesWithoutObservation = inputAbducibles.getAxiomBasedAbducibles();
            if (loader.isMultipleObservationOnInput()){
                if (Configuration.STRICT_RELEVANCE) {
                    loader.getObservation().getAxiomsInMultipleObservations()
                            .forEach(abduciblesWithoutObservation::remove);
                }
            } else {
                abduciblesWithoutObservation.remove(loader.getObservationAxiom());
            }
            abducibleAxioms = abduciblesWithoutObservation;
            return;
        }

        for(OWLClass owlClass : inputAbducibles.getClasses()){
            if (owlClass.isTopEntity() || owlClass.isBottomEntity()) continue;
            List<OWLAxiom> classAssertionAxiom = AxiomManager.createClassAssertionAxiom(loader, owlClass);
            for (int i = 0; i < classAssertionAxiom.size(); i++) {
                if (i % 2 == 0) {
                    assertionAxioms.add(classAssertionAxiom.get(i));
                } else {
                    negAssertionAxioms.add(classAssertionAxiom.get(i));
                }
            }
        }

        if(Configuration.ROLES_IN_EXPLANATIONS_ALLOWED){
            for(OWLObjectProperty objectProperty : inputAbducibles.getRoles()){
                if (objectProperty.isTopEntity() || objectProperty.isBottomEntity()) continue;
                List<OWLAxiom> objectPropertyAssertionAxiom = AxiomManager.createObjectPropertyAssertionAxiom(loader, objectProperty);
                for (int i = 0; i < objectPropertyAssertionAxiom.size(); i++) {
                    if (i % 2 == 0) {
                        assertionAxioms.add(objectPropertyAssertionAxiom.get(i));
                    } else {
                        negAssertionAxioms.add(objectPropertyAssertionAxiom.get(i));
                    }
                }
            }
        }

        if (loader.isMultipleObservationOnInput()){
            if (Configuration.STRICT_RELEVANCE) {
                assertionAxioms.removeAll(loader.getObservation().getAxiomsInMultipleObservations());
                negAssertionAxioms.removeAll(loader.getObservation().getAxiomsInMultipleObservations());
            }
        } else {
            assertionAxioms.remove(loader.getObservationAxiom());
            negAssertionAxioms.remove(loader.getObservationAxiom());
        }

        abducibleAxioms = new HashSet<>(assertionAxioms);

        if(Configuration.NEGATION_ALLOWED)
            abducibleAxioms.addAll(negAssertionAxioms);

    }
}
