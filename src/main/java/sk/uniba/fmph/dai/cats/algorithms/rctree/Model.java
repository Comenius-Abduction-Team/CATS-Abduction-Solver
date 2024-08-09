package sk.uniba.fmph.dai.cats.algorithms.rctree;

import org.semanticweb.owlapi.model.OWLAxiom;

public class Model {

    ModelData data = new ModelData();
    ModelData negatedData = new ModelData();
    boolean valid = true;

    public Model(){}

    public Model(ModelData data, ModelData negatedData) {
        this.data = data;
        this.negatedData = negatedData;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public ModelData getData() {
        return data;
    }

    public ModelData getNegatedData() {
        return negatedData;
    }

    public void add(OWLAxiom axiom){
        data.add(axiom);
    }

    public void addNegated(OWLAxiom axiom){
        negatedData.add(axiom);
    }

    public boolean isEmpty(){
        return negatedData.isEmpty() && data.isEmpty();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Model) {
            Model other = (Model) obj;
            return data.containsAll(other.getData()) && other.getData().containsAll(data);
        }
        return false;
    }
}
