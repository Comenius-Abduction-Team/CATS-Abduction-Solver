package sk.uniba.fmph.dai.cats.model;

import org.semanticweb.owlapi.model.OWLAxiom;
import sk.uniba.fmph.dai.cats.algorithms.hybrid.ModelExtractor;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ModelManager {

    ModelExtractor extractor;

    final List<Model> models = new ArrayList<>();

    Model modelToReuse;

    int lastUsableModelIndex;

    public void setExtractor(ModelExtractor extractor) {
        this.extractor = extractor;
    }

//    public Model getReusableModel(){
//        if (!canReuseModel())
//            return null;
//        return models.get(lastUsableModelIndex);
//    }

    public Model getReusableModel(){
        return modelToReuse;
    }

//    public boolean canReuseModel(){
//        return lastUsableModelIndex >= 0;
//    }

    public boolean canReuseModel(){
        return modelToReuse != null;
    }

    public void setLastModelAsReusable(){
        lastUsableModelIndex = models.size();
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

        for (int i = models.size() - 1; i >= 0; i--) {
            Model storedModel = models.get(i);
            if (storedModel.equals(model)){
                modelToReuse = storedModel;
                return true;
            }
        }
        modelToReuse = null;
        return false;
    }

//    public int findReuseIndexForPath(Set<OWLAxiom> path){
//
//        for (int i = models.size()-1; i >= 0 ; i--){
//            if (models.get(i).getData().containsAll(path)){
//                lastUsableModelIndex = i;
//                return i;
//            }
//        }
//        lastUsableModelIndex = -1;
//        return -1;
//    }

    public boolean findReuseModelForPath(Set<OWLAxiom> path){

        for (int i = models.size() - 1; i >= 0; i--) {
            Model model = models.get(i);
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
