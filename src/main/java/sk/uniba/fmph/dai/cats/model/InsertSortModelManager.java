package sk.uniba.fmph.dai.cats.model;

import org.semanticweb.owlapi.model.OWLAxiom;
import sk.uniba.fmph.dai.cats.algorithms.hybrid.AlgorithmSolver;

import java.util.Set;
import java.util.TreeSet;

public class InsertSortModelManager extends ModelManager {

    public InsertSortModelManager(AlgorithmSolver solver){
        stats = solver.stats;
        models = new TreeSet<>();
    }

    @Override
    public boolean findReusableModel(Model model){

//        Model foundModel = models.stream().filter(model::equals).findFirst().orElse(null);
//        if (foundModel == null)
//            return false;
//        modelToReuse = foundModel;
//        return true;

        for (Model storedModel : models) {

            if (storedModel.equals(model)){
                modelToReuse = storedModel;
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean findReuseModelForPath(Set<OWLAxiom> path){

        for (Model model : models) {

            if (model.getData().containsAll(path)){
                modelToReuse = model;
                return true;
            }
        }

        modelToReuse = null;
        return false;
    }

}
