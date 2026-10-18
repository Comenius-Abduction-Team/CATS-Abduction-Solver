package sk.uniba.fmph.dai.cats.model;

import org.semanticweb.owlapi.model.OWLAxiom;

import java.util.Collection;
import java.util.HashSet;
import java.util.stream.Collectors;

public class ModelData extends HashSet<OWLAxiom> implements Comparable<ModelData> {

    public ModelData(){
        super();
    }

    ModelData(Collection<OWLAxiom> collection){
        super(collection);
    }

    @Override
    public int compareTo(ModelData o) {
        if (this.equals(o))
            return 0;

        String firstString = this.stream()
                .map(Object::toString)
                .sorted()
                .collect(Collectors.joining());

        String secondString = o.stream()
                .map(Object::toString)
                .sorted()
                .collect(Collectors.joining());

        return firstString.compareTo(secondString);
    }
}
