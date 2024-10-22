package sk.uniba.fmph.dai.cats.model;

import org.semanticweb.owlapi.model.OWLAxiom;
import sk.uniba.fmph.dai.cats.algorithms.hybrid.ModelExtractor;

import java.util.*;

public class ModelManager {

    ModelExtractor extractor;

    Collection<Model> models;

    Model modelToReuse;

    int lastUsableModelIndex;

    public ModelManager(){
        models = new ArrayList<>();
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

//    public boolean findReusableModel(Model model){
//        lastUsableModelIndex = models.indexOf(model);
//        return lastUsableModelIndex >= 0;
//    }

    public boolean findReusableModel(Model model){

//        List<Model> modelsList = (List<Model>) models;
//        int index = modelsList.indexOf(model);
//        if (index < 0){
//            return false;
//        }
//        modelToReuse = modelsList.get(index);
//        return true;

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
