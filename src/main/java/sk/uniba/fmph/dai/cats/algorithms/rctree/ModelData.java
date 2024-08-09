package sk.uniba.fmph.dai.cats.algorithms.rctree;

import org.semanticweb.owlapi.model.OWLAxiom;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

public class ModelData extends HashSet<OWLAxiom> {

    private Iterator<OWLAxiom> iterator;

    public ModelData(){
        super();
    }

    ModelData(Iterator<OWLAxiom> iterator){
        this.iterator = iterator;
    }

    ModelData(Collection<OWLAxiom> collection){
        super(collection);
    }

    ModelData(Collection<OWLAxiom> collection, Iterator<OWLAxiom> iterator){
        super(collection);
        this.iterator = iterator;
    }

    @Override
    public Iterator<OWLAxiom> iterator() {
        if (iterator == null)
            return super.iterator();
        else
            return iterator;
    }

    public void setIterator(Iterator<OWLAxiom> iterator) {
        this.iterator = iterator;
    }
}
