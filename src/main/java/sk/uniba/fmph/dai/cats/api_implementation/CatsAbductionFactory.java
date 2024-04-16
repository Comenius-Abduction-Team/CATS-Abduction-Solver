package sk.uniba.fmph.dai.cats.api_implementation;

import sk.uniba.fmph.dai.abduction_api.abducible.*;
import sk.uniba.fmph.dai.abduction_api.exception.AxiomAbducibleException;
import sk.uniba.fmph.dai.abduction_api.exception.InvalidObservationException;
import sk.uniba.fmph.dai.abduction_api.exception.NotSupportedException;
import sk.uniba.fmph.dai.abduction_api.exception.SymbolAbducibleException;
import sk.uniba.fmph.dai.abduction_api.factory.IAbductionFactory;
import sk.uniba.fmph.dai.abduction_api.abducer.IAbducer;
import sk.uniba.fmph.dai.abduction_api.abducer.IThreadAbducer;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;
import sk.uniba.fmph.dai.abduction_api.factory.ISolverDescriptor;

import java.util.Collection;
import java.util.Set;

public class CatsAbductionFactory implements IAbductionFactory {

    private static final CatsAbductionFactory instance = new CatsAbductionFactory();

    private CatsAbductionFactory(){}

    public static CatsAbductionFactory getFactory(){
        return instance;
    }

    @Override
    public IAbducer getAbducer() {
        return new CatsAbducer();
    }

    @Override
    public IAbducer getAbducer(OWLOntology backgroundKnowledge, OWLAxiom observation)
            throws InvalidObservationException {
        return new CatsAbducer(backgroundKnowledge, observation);
    }

    @Override
    public IAbducer getAbducer(OWLOntology backgroundKnowledge, Set<OWLAxiom> observation)
            throws InvalidObservationException {
        return new CatsAbducer(backgroundKnowledge, observation);
    }

    @Override
    public IThreadAbducer getThreadAbducer() {
        return new CatsAbducer();
    }

    @Override
    public IThreadAbducer getThreadAbducer(OWLOntology backgroundKnowledge, OWLAxiom observation)
            throws InvalidObservationException {
        return new CatsAbducer(backgroundKnowledge, observation);
    }

    @Override
    public IThreadAbducer getThreadAbducer(OWLOntology backgroundKnowledge, Set<OWLAxiom> observation)
            throws InvalidObservationException {
        return new CatsAbducer(backgroundKnowledge, observation);
    }

    @Override
    public IAbducibles getAbducibles() {
        return new CatsAxiomAbducibles();
    }

    @Override
    public IAxiomAbducibles getAxiomAbducibles() {
        return new CatsAxiomAbducibles();
    }

    @Override
    public IAxiomAbducibles getAxiomAbducibles(Set<OWLAxiom> axioms)
            throws AxiomAbducibleException {
        return new CatsAxiomAbducibles(axioms);
    }

    @Override
    public ISymbolAbducibles getSymbolAbducibles() {
        return new CatsSymbolAbducibles();
    }

    @Override
    public ISymbolAbducibles getSymbolAbducibles(Set<OWLEntity> symbols) throws SymbolAbducibleException {
        return new CatsSymbolAbducibles(symbols);
    }

    @Override
    public IExplanationConfigurator getExplanationConfigurator() {
        return new CatsExplanationConfigurator();
    }

    @Override
    public IComplexConceptConfigurator getComplexConcepConfigurator() {
        return new CatsExplanationConfigurator();
    }

    @Override
    public IRoleConfigurator getRoleConfigurator() throws NotSupportedException {
        return new CatsExplanationConfigurator();
    }

    @Override
    public ISolverDescriptor getDescriptor() {
        return new CatsSolverDescriptor();
    }
    
}
