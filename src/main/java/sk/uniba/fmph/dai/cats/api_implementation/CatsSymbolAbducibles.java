package sk.uniba.fmph.dai.cats.api_implementation;

import sk.uniba.fmph.dai.abduction_api.abducible.ISymbolAbducibles;
import sk.uniba.fmph.dai.abduction_api.exception.SymbolAbducibleException;
import sk.uniba.fmph.dai.cats.models.Abducibles;
import org.semanticweb.owlapi.model.*;
import sk.uniba.fmph.dai.cats.reasoner.ILoader;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class CatsSymbolAbducibles extends CatsAbducibles implements ISymbolAbducibles {

    public CatsSymbolAbducibles(){}

    public CatsSymbolAbducibles(Collection<OWLEntity> symbols) throws SymbolAbducibleException {
        addAll(symbols);
    }

    private Set<OWLNamedIndividual> individuals = new HashSet<>();
    private Set<OWLClass> classes = new HashSet<>();
    private Set<OWLObjectProperty> roles = new HashSet<>();

    @Override
    public void setSymbols(Set<OWLEntity> symbols) throws SymbolAbducibleException {

        Set<OWLClass> classes = new HashSet<>();
        Set<OWLNamedIndividual> individuals = new HashSet<>();
        Set<OWLObjectProperty> roles = new HashSet<>();

        symbols.forEach(entity -> addEntityToCorrectSet(entity, individuals, classes, roles));

        this.individuals = individuals;
        this.classes = classes;
        this.roles = roles;

    }

    @Override
    public Set<OWLEntity> getSymbols() {
        Set<OWLEntity> symbols = new HashSet<>(classes);
        symbols.addAll(individuals);
        symbols.addAll(roles);
        return symbols;
    }

    @Override
    public void add(OWLEntity symbol) throws SymbolAbducibleException {
        addEntityToCorrectSet(symbol, individuals, classes, roles);
    }

    private void addEntityToCorrectSet(
            OWLEntity symbol,
            Set<OWLNamedIndividual> individuals,
            Set<OWLClass> classes,
            Set<OWLObjectProperty> roles)
    {
        EntityType<?> type = symbol.getEntityType();
        if (type == EntityType.CLASS){
            classes.add((OWLClass) symbol);
        }
        else if (type == EntityType.NAMED_INDIVIDUAL){
            individuals.add((OWLNamedIndividual) symbol);
        }
        else if (type == EntityType.OBJECT_PROPERTY){
            roles.add((OWLObjectProperty) symbol);
        }
        else throw new SymbolAbducibleException(symbol);
    }

    @Override
    public void addAll(Collection<OWLEntity> symbols) throws SymbolAbducibleException {

        Set<OWLClass> classes = new HashSet<>();
        Set<OWLNamedIndividual> individuals = new HashSet<>();
        Set<OWLObjectProperty> roles = new HashSet<>();

        symbols.forEach(entity -> addEntityToCorrectSet(entity, individuals, classes, roles));

        this.classes.addAll(classes);
        this.individuals.addAll(individuals);
        this.roles.addAll(roles);
    }

    @Override
    public void remove(OWLEntity owlEntity) throws SymbolAbducibleException {

    }

    @Override
    public void removeAll(Collection<OWLEntity> collection) throws SymbolAbducibleException {

    }

    @Override
    public Abducibles exportAbducibles(ILoader loader) {
        return new Abducibles(loader, individuals, classes, roles);
    }

    @Override
    public boolean isEmpty(){
        return classes.isEmpty() && roles.isEmpty();
    }

    @Override
    public void clear() {
        individuals.clear();
        classes.clear();
        roles.clear();
    }

    @Override
    public int size() {
        return 0;
    }

    public Set<OWLNamedIndividual> getIndividuals() {
        return individuals;
    }

    public Set<OWLClass> getClasses() {
        return classes;
    }

    public Set<OWLObjectProperty> getRoles() {
        return roles;
    }
}
