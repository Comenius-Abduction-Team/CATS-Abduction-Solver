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

    protected boolean isOntologyWithLiteralsConsistent(Collection<OWLAxiom> axioms){
        path.addAll(axioms);
        boolean isConsistent = checkConsistencyWithModelExtraction();
        path.removeAll(axioms);
        return isConsistent;
    }

    protected boolean checkConsistencyWithModelExtraction(){

        boolean isConsistent = checkConsistency();

        if (!isConsistent)
            return false;

        modelManager.storeModelFoundByConsistencyCheck();

        return true;
    }

    public boolean checkConsistency(){
        if(checkingMinimalityWithQXP) {
            return checkConsistency(pathDuringCheckingMinimality);
        }
        else {
            return checkConsistency(path);
        }
    }

    private boolean checkConsistency(Set<OWLAxiom> path){
        if (path != null && !path.isEmpty()) {
            if(loader.isMultipleObservationOnInput()){
                for(OWLAxiom axiom : loader.getObservation().getAxiomsInMultipleObservations()){
                    path.remove(AxiomManager.getComplementOfOWLAxiom(loader, axiom));
                }
            } else {
                path.remove(loader.getNegObservation().getOwlAxiom());
            }
            reasonerManager.addAxiomsToOntology(path);
            if (!reasonerManager.isOntologyConsistent()){
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
