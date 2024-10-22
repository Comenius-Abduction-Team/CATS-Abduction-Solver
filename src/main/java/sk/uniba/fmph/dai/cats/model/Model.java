package sk.uniba.fmph.dai.cats.model;

import org.semanticweb.owlapi.model.OWLAxiom;
import sk.uniba.fmph.dai.cats.common.StringFactory;

public class Model implements Comparable<Model>{

    ModelData data = new ModelData();
    ModelData negatedData = new ModelData();

    public Model(){}

    public Model(Model other){
        data = new ModelData(other.data);
        negatedData = new ModelData(other.negatedData);
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
            return data.containsAll(other.data) && other.data.containsAll(data);
        }
        return false;
    }

    @Override
    public int compareTo(Model o) {
        int compare = Integer.compare(negatedData.size(), o.negatedData.size());
        if (compare == 0)
            return 1;
        return compare;
        //return 1 * Integer.compare(negatedData.size(), o.negatedData.size());
    }

    @Override
    public String toString() {
        return StringFactory.getRepresentation(negatedData);
    }
}
