package sk.uniba.fmph.dai.cats.algorithms.hybrid;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import sk.uniba.fmph.dai.cats.common.Configuration;
import sk.uniba.fmph.dai.cats.data.Abducibles;
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

    final List<OWLAxiom> assertionAxioms = new ArrayList<>();
    final List<OWLAxiom> negAssertionAxioms = new ArrayList<>();

    Set<OWLAxiom> abducibleAxioms;

    TransformedAbducibles(Loader loader){

        Abducibles abducibles = loader.getAbducibles();

        if(loader.isAxiomBasedAbduciblesOnInput()){
            Set<OWLAxiom> abduciblesWithoutObservation = abducibles.getAxiomBasedAbducibles();
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

        for(OWLClass owlClass : abducibles.getClasses()){
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
            for(OWLObjectProperty objectProperty : abducibles.getRoles()){
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

    public Set<OWLAxiom> getAbducibleAxioms() {
        return abducibleAxioms;
    }
}
