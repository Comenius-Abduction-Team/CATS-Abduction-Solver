package sk.uniba.fmph.dai.cats.algorithms.hybrid;

import sk.uniba.fmph.dai.cats.common.Configuration;
import sk.uniba.fmph.dai.cats.data.Explanation;
import openllet.owlapi.OpenlletReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import sk.uniba.fmph.dai.cats.reasoner.AxiomManager;
import sk.uniba.fmph.dai.cats.reasoner.ILoader;
import sk.uniba.fmph.dai.cats.reasoner.IReasonerManager;

import java.util.List;

public class RuleChecker implements IRuleChecker {

    private final ILoader loader;
    private final IReasonerManager reasonerManager;

    RuleChecker(ILoader loader, IReasonerManager reasonerManager) {
        this.loader = loader;
        this.reasonerManager = reasonerManager;
    }

    @Override
    public boolean isConsistent(Explanation explanation) {
        reasonerManager.resetOntology(loader.getInitialOntology().axioms());
        reasonerManager.addAxiomsToOntology(explanation.getAxioms());
        boolean isConsistent = reasonerManager.isOntologyConsistent();
        reasonerManager.resetOntology(loader.getOriginalOntology().axioms());
        return isConsistent;
    }

    @Override
    public boolean isExplanation(Explanation explanation) {
        reasonerManager.addAxiomsToOntology(explanation.getAxioms());
        boolean isConsistent = reasonerManager.isOntologyConsistent();
        reasonerManager.resetOntology(loader.getOriginalOntology().axioms());
        return !isConsistent;
    }

    @Override
    public boolean isMinimal(List<Explanation> explanationList, Explanation explanation) {
        if (explanation == null || !(explanation.getAxioms() instanceof List)) {
            return false;
        }

        for (Explanation minimalExplanation : explanationList) {
            if (explanation.getAxioms().containsAll(minimalExplanation.getAxioms())) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isRelevant(Explanation explanation) throws OWLOntologyCreationException {
        OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager();
        OWLOntology ontology = ontologyManager.createOntology(explanation.getAxioms());

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
}