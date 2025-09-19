package sk.uniba.fmph.dai.cats.model;

import org.semanticweb.owlapi.model.OWLAxiom;
import sk.uniba.fmph.dai.cats.algorithms.AlgorithmSolver;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

public class InsertSortModelManager extends ModelManager {

    public InsertSortModelManager(AlgorithmSolver solver){
        super(solver);
    }

    @Override
    protected Collection<Model> createModelCollection() {
        return new TreeSet<>();
    }

    @Override
    protected boolean findReusableModel(Model model){

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
