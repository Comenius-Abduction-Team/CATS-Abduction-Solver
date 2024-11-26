package sk.uniba.fmph.dai.cats.model;

import org.semanticweb.owlapi.model.OWLAxiom;
import sk.uniba.fmph.dai.cats.algorithms.hybrid.AlgorithmSolver;
import sk.uniba.fmph.dai.cats.algorithms.hybrid.ModelExtractor;
import sk.uniba.fmph.dai.cats.data_processing.TreeStats;

import java.util.*;

public class ModelManager {

    ModelExtractor extractor;

    Collection<Model> models;

    Model modelToReuse;

    TreeStats stats;

    protected ModelManager(){}

    public ModelManager(AlgorithmSolver solver){

        models = new ArrayList<>();
        stats = solver.stats;

    }

    public void setExtractor(ModelExtractor extractor) {
        this.extractor = extractor;
    }

    public Model getReusableModel(){
        return modelToReuse;
    }

    public boolean canReuseModel(){
        return modelToReuse != null;
    }

    public void setModelToReuse(Model model){
        modelToReuse = model;
    }

    public void add(Model model){
        models.add(model);
    }

    public boolean findReusableModel(Model model){

        for (int i = models.size() - 1; i >= 0; i--) {
            Model storedModel = ((List<Model>)models).get(i);
            if (storedModel.equals(model)){
                modelToReuse = storedModel;
                return true;
            }
        }
        return false;
    }

    public boolean findReuseModelForPath(Set<OWLAxiom> path){

        for (int i = models.size() - 1; i >= 0; i--) {
            Model model = ((List<Model>)models).get(i);
            if (model.getData().containsAll(path)){
                modelToReuse = model;
                return true;
            }
        }

        modelToReuse = null;
        return false;
    }

    public void storeModelFoundByConsistencyCheck(){

        Model model = extractor.extractModel();
        stats.getCurrentLevelStats().modelExtractions += 1;

        if (model.isEmpty())
            return;

        if (!findReusableModel(model)) {
            add(model);
            setModelToReuse(model);
        }
    }

}
