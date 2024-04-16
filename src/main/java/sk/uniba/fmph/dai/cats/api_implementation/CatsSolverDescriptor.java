package sk.uniba.fmph.dai.cats.api_implementation;

import sk.uniba.fmph.dai.abduction_api.factory.ISolverDescriptor;

public class CatsSolverDescriptor implements ISolverDescriptor {
    @Override
    public boolean hasThreadMode() {
        return true;
    }

    @Override
    public boolean hasSymbolAbducibles() {
        return true;
    }

    @Override
    public boolean hasAxiomAbducibles() {
        return true;
    }

    @Override
    public boolean hasConceptSwitch() {
        return false;
    }

    @Override
    public boolean hasComplexConceptSwitch() {
        return false;
    }

    @Override
    public boolean hasComplementConceptSwitch() {
        return true;
    }

    @Override
    public boolean hasRoleSwitch() {
        return true;
    }

    @Override
    public boolean hasLoopSwitch() {
        return true;
    }

    @Override
    public boolean hasSpecificParameters() {
        return true;
    }

    @Override
    public boolean hasTimeLimit() {
        return true;
    }
}
