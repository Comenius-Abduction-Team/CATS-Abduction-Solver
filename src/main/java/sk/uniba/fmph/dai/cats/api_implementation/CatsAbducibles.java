package sk.uniba.fmph.dai.cats.api_implementation;

import sk.uniba.fmph.dai.cats.models.Abducibles;
import sk.uniba.fmph.dai.cats.reasoner.ILoader;
import sk.uniba.fmph.dai.abduction_api.abducible.IAbducibles;

public abstract class CatsAbducibles implements IAbducibles {
    
    /**
     * Create an instance of the models.Abducibles class containing abducibles from this container
     * @param loader instance of reasoner.ILooader needed to construct the Abducibles instance
     */
    public abstract Abducibles exportAbducibles(ILoader loader);

}
