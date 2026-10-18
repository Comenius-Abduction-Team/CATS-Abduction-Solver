package sk.uniba.fmph.dai.cats.model;

import org.semanticweb.owlapi.model.OWLAxiom;
import sk.uniba.fmph.dai.cats.algorithms.AlgorithmSolver;
import sk.uniba.fmph.dai.cats.application.EmptyModelException;
import sk.uniba.fmph.dai.cats.common.LogMessage;
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

    private void add(Model model){
        models.add(model);
        EventPublisher.publishGenericEvent(solver, EventType.MODEL_STORED);
    }

    protected boolean findReusableModel(Model model){
        List<Model> modelList = (List<Model>) models;

        for (int i = models.size() - 1; i >= 0; i--) {
            Model storedModel = modelList.get(i);
            if (storedModel.equals(model)){
                modelToReuse = storedModel;
                return true;
            }
        }
        return false;
    }

    public boolean findReuseModelForPath(Set<OWLAxiom> path){
        List<Model> modelList = (List<Model>) models;

        for (int i = models.size() - 1; i >= 0; i--) {
            Model model =  modelList.get(i);
            if (model.getData().containsAll(path)){
                modelToReuse = model;
                return true;
            }
        }

        modelToReuse = null;
        return false;
    }

    public void storeModelFoundByConsistencyCheck() {

        Model model = extractor.extractModel();
        EventPublisher.publishGenericEvent(solver, EventType.MODEL_EXTRACTION);

        if (model.isEmpty())
            throw new EmptyModelException(LogMessage.INFO_EMPTY_MODEL_FOUND_NO_EXPLANATIONS);

        if (!findReusableModel(model)) {
            add(model);
            modelToReuse = model;
        }
    }

    public Model findAndGetModelToReuse(Set<OWLAxiom> path){
        if (modelToReuse == null || !modelToReuse.getData().containsAll(path))
            findReuseModelForPath(path);

        return modelToReuse;
    }
}
