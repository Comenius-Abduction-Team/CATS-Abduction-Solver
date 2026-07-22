package sk.uniba.fmph.dai.cats.model;

import org.semanticweb.owlapi.model.OWLAxiom;
import sk.uniba.fmph.dai.cats.common.StringFactory;

import java.util.Objects;

public class Model implements Comparable<Model>{

    ModelData data = new ModelData();
    ModelData negatedData = new ModelData();

    private final String illegalStateMessage =
            "There should be no intersection between data and negated data of a model.";

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
        if (negatedData.contains(axiom))
            throw new IllegalStateException(illegalStateMessage);

        data.add(axiom);
    }

    public void addNegated(OWLAxiom axiom){
        if (data.contains(axiom))
            throw new IllegalStateException(illegalStateMessage);

        negatedData.add(axiom);
    }

    public boolean isEmpty(){
        return negatedData.isEmpty() && data.isEmpty();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (obj instanceof Model) {
            Model other = (Model) obj;
            return data.equals(other.data)
                    && negatedData.equals(other.negatedData);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(data, negatedData);
    }

    //TODO fix
    @Override
    public int compareTo(Model o) {
        // when sorting models, they are sorted according to their negated data length
        int compare = Integer.compare(negatedData.size(), o.negatedData.size());
        // if equal, compare model data and negatedData
        // (sorted set uses this method's result to also determine equality of two models)
        if (compare != 0)
            return compare;

        compare = this.negatedData.compareTo(o.negatedData);

        if (compare != 0)
            return compare;

        return this.data.compareTo(o.data);
    }

    @Override
    public String toString() {
        return StringFactory.getRepresentation(negatedData);
    }
}
