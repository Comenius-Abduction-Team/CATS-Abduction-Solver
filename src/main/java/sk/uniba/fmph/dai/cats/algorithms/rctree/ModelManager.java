package sk.uniba.fmph.dai.cats.algorithms.rctree;

import org.semanticweb.owlapi.model.OWLAxiom;
import sk.uniba.fmph.dai.cats.algorithms.hybrid.ModelExtractor;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ModelManager {

    ModelExtractor extractor;

    final List<Model> models = new ArrayList<>();

    int lastUsableModelIndex;

    public void setExtractor(ModelExtractor extractor) {
        this.extractor = extractor;
    }

    public Model getReusableModel(){
        if (!canReuseModel())
            return null;
        return models.get(lastUsableModelIndex);
    }

    public boolean canReuseModel(){
        return lastUsableModelIndex >= 0;
    }

    public void setLastModelAsReusable(){
        lastUsableModelIndex = models.size();
    }

    public void add(Model model){
        models.add(model);
    }

    public void findReusableModel(Model model){
        lastUsableModelIndex = models.indexOf(model);
    }

    public int findReuseIndexForPath(Set<OWLAxiom> path){

        for (int i = models.size()-1; i >= 0 ; i--){
            if (models.get(i).getData().containsAll(path)){
                lastUsableModelIndex = i;
                return i;
            }
        }
        lastUsableModelIndex = -1;
        return -1;
    }

    public void storeModelFoundByConsistencyCheck(){

        Model model = extractor.extractModel();

        findReusableModel(model);

        if (!model.isEmpty() && !canReuseModel()) {
            setLastModelAsReusable();
            add(model);
        }
    }

}
