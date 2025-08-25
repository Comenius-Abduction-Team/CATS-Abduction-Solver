package sk.uniba.fmph.dai.cats.algorithms.mxp;

import org.semanticweb.owlapi.model.OWLAxiom;
import sk.uniba.fmph.dai.cats.algorithms.AlgorithmSolver;
import sk.uniba.fmph.dai.cats.common.Configuration;
import sk.uniba.fmph.dai.cats.data.Explanation;
import sk.uniba.fmph.dai.cats.reasoner.AxiomManager;

import java.util.*;

public class TripleMxpNodeProcessor extends MxpNodeProcessor {

    public TripleMxpNodeProcessor(AlgorithmSolver solver) {
        super(solver);
    }

    protected Collection<Explanation> findExplanationsWithMxp(boolean extractModel){

        Set<OWLAxiom> positiveAbducibles = new HashSet<>();
        Set<OWLAxiom> negativeAbducibles = new HashSet<>();
        Set<Explanation> explanationsFound = new HashSet<>();

        for (OWLAxiom a : abducibleAxioms){
            if (path.contains(a))
                continue;
            if (Configuration.REMOVE_COMPLEMENTS_FROM_MXP
                    && path.contains(AxiomManager.getComplementOfOWLAxiom(solver.loader, a)))
                continue;
            if (AxiomManager.isNegatedClassAssertion(a)
                    || (Configuration.ROLES_IN_EXPLANATIONS_ALLOWED && AxiomManager.isNegatedRoleAssertion(a)))
                negativeAbducibles.add(a);
            else
                positiveAbducibles.add(a);
        }

        if(Configuration.CACHED_CONFLICTS_LONGEST_CONFLICT){
            setDivider.setIndexesOfExplanations(explanationManager.getPossibleExplanationsSize());
        }

        explanationsFound.addAll(
                runMxp(positiveAbducibles, true, extractModel).getExplanations());
        explanationsFound.addAll(
                runMxp(negativeAbducibles, true, extractModel, true).getExplanations());
        positiveAbducibles.addAll(negativeAbducibles);

        explanationsFound.addAll(
                runMxp(positiveAbducibles, true, extractModel, true).getExplanations());

        return explanationsFound;
    }
}
