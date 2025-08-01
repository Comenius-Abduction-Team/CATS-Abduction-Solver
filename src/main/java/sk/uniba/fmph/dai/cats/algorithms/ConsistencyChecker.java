package sk.uniba.fmph.dai.cats.algorithms;

import org.semanticweb.owlapi.model.OWLAxiom;
import sk.uniba.fmph.dai.cats.metrics.TreeStats;
import sk.uniba.fmph.dai.cats.model.ModelManager;
import sk.uniba.fmph.dai.cats.reasoner.AxiomManager;
import sk.uniba.fmph.dai.cats.reasoner.Loader;
import sk.uniba.fmph.dai.cats.reasoner.ReasonerManager;

import java.util.Collection;
import java.util.Set;

public class ConsistencyChecker {

    final ModelManager modelManager;
    final ReasonerManager reasonerManager;
    final Loader loader;
    final Set<OWLAxiom> path;

    public boolean checkingMinimalityWithQXP = false;
    public Set<OWLAxiom> pathDuringCheckingMinimality;

    TreeStats stats;

    ConsistencyChecker(AlgorithmSolver solver){
        modelManager = solver.modelManager;
        loader = solver.loader;
        reasonerManager = loader.reasonerManager;
        path = solver.path;
        stats = solver.stats;
    }

    public boolean checkOntologyConsistency(boolean extractModel){

        boolean isConsistent = reasonerManager.isOntologyConsistent();
        stats.getCurrentLevelStats().consistencyChecks += 1;

        if (!isConsistent)
            return false;

        if (extractModel){
            modelManager.storeModelFoundByConsistencyCheck();
            stats.getCurrentLevelStats().modelExtractions += 1;
        }

        reasonerManager.resetOntologyToOriginal();

        return true;

    }

    public boolean checkOntologyConsistencyWithAddedAxioms(Collection<OWLAxiom> axioms, boolean extractModel){
        path.addAll(axioms);
        boolean isConsistent = checkOntologyConsistencyWithPath(extractModel, false);
        path.removeAll(axioms);
        return isConsistent;
    }

    public boolean checkOntologyConsistencyWithPath(boolean extractModel, boolean simple){

        if(checkingMinimalityWithQXP) {
            return checkOntologyConsistencyWithPath(pathDuringCheckingMinimality, extractModel, simple);
        }
        else {
            return checkOntologyConsistencyWithPath(path, extractModel, simple);
        }
    }

    private boolean checkOntologyConsistencyWithPath(Set<OWLAxiom> path, boolean extractModel, boolean simple){
        if (path.isEmpty())
            return true;

        if (!simple) {
            if (loader.isMultipleObservationOnInput()) {
                for (OWLAxiom axiom : loader.getObservation().getAxiomsInMultipleObservations()) {
                    path.remove(AxiomManager.getComplementOfOWLAxiom(loader, axiom));
                }
            } else {
                path.remove(loader.getNegObservationAxiom());
            }
        }

        reasonerManager.addAxiomsToOntology(path);
        if (!checkOntologyConsistency(extractModel)){
            reasonerManager.resetOntologyToOriginal();
            return false;
        }

        return true;
    }

    public void turnMinimalityCheckingOn(Set<OWLAxiom> path){

        pathDuringCheckingMinimality = path;
        checkingMinimalityWithQXP = true;

    }

    public void turnMinimalityCheckingOff(){

        checkingMinimalityWithQXP = false;

    }




}
