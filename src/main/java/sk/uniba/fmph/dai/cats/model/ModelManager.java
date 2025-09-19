package sk.uniba.fmph.dai.cats.model;

import org.semanticweb.owlapi.model.OWLAxiom;
import sk.uniba.fmph.dai.cats.algorithms.AlgorithmSolver;
import sk.uniba.fmph.dai.cats.events.EventPublisher;
import sk.uniba.fmph.dai.cats.events.EventType;

import java.util.*;

public class ModelManager {

    ModelExtractor extractor;

    protected final Collection<Model> models;

    protected Model modelToReuse;

    private final AlgorithmSolver solver;

    public ModelManager(AlgorithmSolver solver){

        this.solver = solver;
        models = createModelCollection();

    }

    protected Collection<Model> createModelCollection(){
        return new ArrayList<>();
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
        EventPublisher.publishGenericEvent(solver, EventType.MODEL_STORED);
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
