package sk.uniba.fmph.dai.cats.algorithms.hybrid;

import org.semanticweb.owlapi.model.OWLAxiom;
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

    ConsistencyChecker(AlgorithmSolver solver){
        modelManager = solver.modelManager;
        loader = solver.loader;
        reasonerManager = loader.reasonerManager;
        path = solver.path;
    }

    public boolean checkOntologyConsistency(boolean extractModel){

        boolean isConsistent = reasonerManager.isOntologyConsistent();

        if (!isConsistent)
            return false;

        if (extractModel)
            modelManager.storeModelFoundByConsistencyCheck();

        reasonerManager.resetOntologyToOriginal();

        return true;

    }

    protected boolean checkOntologyConsistencyWithAddedAxioms(Collection<OWLAxiom> axioms, boolean extractModel){
        path.addAll(axioms);
        boolean isConsistent = checkOntologyConsistencyWithPath(extractModel);
        path.removeAll(axioms);
        return isConsistent;
    }

    public boolean checkOntologyConsistencyWithPath(boolean extractModel){

        if(checkingMinimalityWithQXP) {
            return checkOntologyConsistencyWithPath(pathDuringCheckingMinimality, extractModel);
        }
        else {
            return checkOntologyConsistencyWithPath(path, extractModel);
        }
    }

    private boolean checkOntologyConsistencyWithPath(Set<OWLAxiom> path, boolean extractModel){
        if (path != null && !path.isEmpty()) {
            if(loader.isMultipleObservationOnInput()){
                for(OWLAxiom axiom : loader.getObservation().getAxiomsInMultipleObservations()){
                    path.remove(AxiomManager.getComplementOfOWLAxiom(loader, axiom));
                }
            } else {
                path.remove(loader.getNegObservationAxiom());
            }
            reasonerManager.addAxiomsToOntology(path);
            if (!checkOntologyConsistency(extractModel)){
                reasonerManager.resetOntologyToOriginal();
                return false;
            }
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
