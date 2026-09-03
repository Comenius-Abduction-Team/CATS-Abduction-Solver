package sk.uniba.fmph.dai.cats.algorithms;

import org.semanticweb.owlapi.model.OWLAxiom;
import sk.uniba.fmph.dai.cats.common.Configuration;
import sk.uniba.fmph.dai.cats.data.Explanation;
import sk.uniba.fmph.dai.cats.events.EventPublisher;
import sk.uniba.fmph.dai.cats.events.EventType;
import sk.uniba.fmph.dai.cats.reasoner.AxiomManager;
import sk.uniba.fmph.dai.cats.reasoner.Loader;
import sk.uniba.fmph.dai.cats.reasoner.ReasonerManager;

import java.util.HashSet;
import java.util.List;

public class RuleChecker {

    private final AlgorithmSolver solver;
    private final Loader loader;
    private final ReasonerManager reasonerManager;

    RuleChecker(AlgorithmSolver solver) {
        this.solver = solver;
        this.loader = solver.loader;
        this.reasonerManager = loader.reasonerManager;
    }

    public boolean isConsistent(Explanation explanation) {
        reasonerManager.resetOntologyToInitial();
        reasonerManager.addAxiomsToOntology(explanation.getAxioms());
        boolean isConsistent = reasonerManager.isOntologyConsistent();
        EventPublisher.publishGenericEvent(solver, EventType.CONSISTENCY_CHECK);
        reasonerManager.resetOntologyToOriginal();
        return isConsistent;
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

        OntologyWrapper ontology = new OntologyWrapper(solver, explanation.getAxioms());

        if(loader.isMultipleObservationOnInput()){

            for(OWLAxiom obs : loader.getObservation().getAxiomsInMultipleObservations()){
                OWLAxiom negObs = AxiomManager.getComplementOfOWLAxiom(loader, obs);
                ontology.addAxiom(negObs);
                boolean isConsistent = ontology.isConsistent();
                if(Configuration.STRICT_RELEVANCE && !isConsistent){ //strictly relevant
                    return false;
                }
                else if(!Configuration.STRICT_RELEVANCE && isConsistent){ //partially relevant
                    return true;
                }
                ontology.removeAxiom(negObs);
            }
            return true;

        } else {
            ontology.addAxiom(loader.getNegObservation().getOwlAxiom());
            return ontology.isConsistent();
        }
    }
}