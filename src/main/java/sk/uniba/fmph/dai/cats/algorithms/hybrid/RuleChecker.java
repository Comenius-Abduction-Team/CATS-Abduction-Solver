package sk.uniba.fmph.dai.cats.algorithms.hybrid;

import openllet.owlapi.OpenlletReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import sk.uniba.fmph.dai.cats.common.Configuration;
import sk.uniba.fmph.dai.cats.data.Explanation;
import sk.uniba.fmph.dai.cats.reasoner.AxiomManager;
import sk.uniba.fmph.dai.cats.reasoner.Loader;
import sk.uniba.fmph.dai.cats.reasoner.ReasonerManager;

import java.util.HashSet;
import java.util.List;

public class RuleChecker {

    private final Loader loader;
    private final ReasonerManager reasonerManager;

    RuleChecker(Loader loader) {
        this.loader = loader;
        this.reasonerManager = loader.reasonerManager;
    }

    public boolean isConsistent(Explanation explanation) {
        reasonerManager.resetOntology(loader.getInitialOntology().axioms());
        reasonerManager.addAxiomsToOntology(explanation.getAxioms());
        boolean isConsistent = reasonerManager.isOntologyConsistent();
        reasonerManager.resetOntology(loader.getOriginalOntology().axioms());
        return isConsistent;
    }

    public boolean isExplanation(Explanation explanation) {
        reasonerManager.addAxiomsToOntology(explanation.getAxioms());
        boolean isConsistent = reasonerManager.isOntologyConsistent();
        reasonerManager.resetOntology(loader.getOriginalOntology().axioms());
        return !isConsistent;
    }

    public boolean isMinimal(List<Explanation> explanationList, Explanation explanation) {
        if (explanation == null || explanation.getAxioms() == null) {
            return false;
        }

        for (Explanation minimalExplanation : explanationList) {
            if (new HashSet<>(explanation.getAxioms()).containsAll(minimalExplanation.getAxioms())) {
                return false;
            }
        }
        return true;
    }

    public boolean isRelevant(Explanation explanation) {
        OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager();

        OWLOntology ontology;

        try {
            ontology = ontologyManager.createOntology(explanation.getAxioms());
        } catch(OWLOntologyCreationException e){
            throw new RuntimeException("Could not create ontology while checking relevancy: " + e.getMessage());
        }

        OWLReasoner reasoner = new OpenlletReasonerFactory().createNonBufferingReasoner(ontology);
        //OWLReasoner reasoner = new ReasonerFactory().createNonBufferingReasoner(ontology);

        if(loader.isMultipleObservationOnInput()){
            for(OWLAxiom obs : loader.getObservation().getAxiomsInMultipleObservations()){
                OWLAxiom negObs = AxiomManager.getComplementOfOWLAxiom(loader, obs);
                ontologyManager.addAxiom(ontology, negObs);
                if(Configuration.STRICT_RELEVANCE && !reasoner.isConsistent()){ //strictly relevant
                    return false;
                }
                else if(!Configuration.STRICT_RELEVANCE && reasoner.isConsistent()){ //partially relevant
                    return true;
                }
                ontologyManager.removeAxiom(ontology, negObs);
            }
            return true;
        } else {
            ontologyManager.addAxiom(ontology, loader.getNegObservation().getOwlAxiom());
            return reasoner.isConsistent();
        }
    }

    public boolean checkConsistencyUsingNewReasoner(Explanation explanation) {
        OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager();

        OWLOntology ontology;

        try {
            ontology = ontologyManager.createOntology(loader.getInitialOntology().axioms());
        } catch(OWLOntologyCreationException e){
            throw new RuntimeException("Could not create ontology while checking relevancy: " + e.getMessage());
        }

        ontologyManager.addAxioms(ontology, explanation.getAxioms());

        OWLReasoner reasoner = new OpenlletReasonerFactory().createNonBufferingReasoner(ontology);
        return reasoner.isConsistent();

    }
}