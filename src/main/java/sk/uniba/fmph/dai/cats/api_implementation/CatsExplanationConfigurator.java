package sk.uniba.fmph.dai.cats.api_implementation;

import sk.uniba.fmph.dai.abduction_api.abducible.IComplexConceptConfigurator;
import sk.uniba.fmph.dai.abduction_api.abducible.IRoleConfigurator;
import sk.uniba.fmph.dai.abduction_api.exception.NotSupportedException;

public class CatsExplanationConfigurator implements IRoleConfigurator, IComplexConceptConfigurator {

    private boolean complementConcepts, roles, loops;

    public CatsExplanationConfigurator(){
        setDefaultConfiguration();
    }

    @Override
    public void allowComplementConcepts(Boolean allowComplementConcepts) {
        complementConcepts = allowComplementConcepts;
    }

    @Override
    public boolean areComplementConceptsAllowed() {
        return complementConcepts;
    }

    @Override
    public boolean getDefaultComplementConceptsAllowed() {
        return true;
    }

    @Override
    public void allowRoleAssertions(Boolean allowRoleAssertions) throws NotSupportedException {
        roles = allowRoleAssertions;
    }

    @Override
    public boolean areRoleAssertionsAllowed() throws NotSupportedException {
        return roles;
    }

    @Override
    public boolean getDefaultRoleAssertionsAllowed() throws NotSupportedException {
        return false;
    }

    @Override
    public void allowLoops(Boolean allowLoops) throws NotSupportedException {
        loops = allowLoops;
    }

    @Override
    public boolean areLoopsAllowed() {
        return loops;
    }

    @Override
    public boolean getDefaultLoopsAllowed() {
        return true;
    }

    @Override
    public void setDefaultConfiguration() {
        allowComplementConcepts(getDefaultComplementConceptsAllowed());
        allowRoleAssertions(getDefaultRoleAssertionsAllowed());
        allowLoops(getDefaultLoopsAllowed());
    }
}
