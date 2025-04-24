package sk.uniba.fmph.dai.cats.model;

import org.semanticweb.owlapi.model.OWLAxiom;
import sk.uniba.fmph.dai.cats.algorithms.AlgorithmSolver;
import sk.uniba.fmph.dai.cats.algorithms.ModelExtractor;
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

    private void setModelToReuse(Model model){
        modelToReuse = model;
    }

    private void add(Model model){
        models.add(model);
        stats.getCurrentLevelStats().storedModels++;
    }

    protected boolean findReusableModel(Model model){

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

        if (model.isEmpty())
            return;

        if (!findReusableModel(model)) {
            add(model);
            setModelToReuse(model);
        }
    }

}
