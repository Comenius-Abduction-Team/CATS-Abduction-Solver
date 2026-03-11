package sk.uniba.fmph.dai.cats.algorithms.marco;

import org.semanticweb.owlapi.model.OWLAxiom;
import sk.uniba.fmph.dai.cats.algorithms.AlgorithmSolver;
import sk.uniba.fmph.dai.cats.algorithms.INodeProcessor;
import sk.uniba.fmph.dai.cats.data.Explanation;


import java.util.HashSet;
import java.util.Set;

public class MarcoNodeProcessor implements INodeProcessor {

    private final AlgorithmSolver solver;
    private final SubsetMapManager map;

    public MarcoNodeProcessor(AlgorithmSolver solver, SubsetMapManager map){
        this.solver = solver;
        this.map = map;
    }

    @Override
    public boolean canCreateRoot(boolean extractModel) {

        return solver.consistencyChecker.checkOntologyConsistency(extractModel);

    }

    @Override
    public boolean shouldPruneBranch(Explanation explanation) {
        return false;
    }

    @Override
    public int findExplanations(Explanation explanation, boolean extractModel) {

        Set<OWLAxiom> S = new HashSet<>(explanation.getAxioms());

        if(map.isKnown(S)){
            return 0;
        }

        boolean consistent =
                solver.consistencyChecker.checkOntologyConsistencyWithPath(false, true);

        if(consistent){
            map.markConsistent(S);
        }
        else{
            map.markInconsistent(S);
        }

        return 0;
    }

    @Override
    public boolean shouldCloseNode(int explanationsFound) {
        return false;
    }

    @Override
    public void postProcessExplanations() {}

    @Override
    public void storeAbduciblesIfNeeded(sk.uniba.fmph.dai.cats.algorithms.IAbducibleAxioms abducibles) {}

}