package sk.uniba.fmph.dai.cats.api_implementation;

import sk.uniba.fmph.dai.abduction_api.abducible.*;
import sk.uniba.fmph.dai.abduction_api.exception.CommonException;
import sk.uniba.fmph.dai.abduction_api.exception.NotSupportedException;

public class ApiObjectConverter {

    static CatsExplanationConfigurator attemptConfiguratorConversion(IExplanationConfigurator configurator){

        CatsExplanationConfigurator newConfigurator = new CatsExplanationConfigurator();
        copyComplexConceptConfiguration(configurator, newConfigurator);
        copyRoleConfiguration(configurator, newConfigurator);
        return newConfigurator;

    }

    private static void copyRoleConfiguration(IExplanationConfigurator oldConfigurator, CatsExplanationConfigurator newConfigurator) {
        try {
            IRoleConfigurator convertedConfigurator = (IRoleConfigurator) oldConfigurator;
            newConfigurator.allowRoleAssertions(convertedConfigurator.areRoleAssertionsAllowed());
            newConfigurator.allowLoops(convertedConfigurator.areLoopsAllowed());
        } catch(NotSupportedException e){
            throw new CommonException("Explanation configurator type not compatible with abduction manager!");
        }
    }

    private static void copyComplexConceptConfiguration(IExplanationConfigurator oldConfigurator,
                                                 CatsExplanationConfigurator newConfigurator) {

        IComplexConceptConfigurator convertedConfigurator = (IComplexConceptConfigurator) oldConfigurator;

        try{
            if (!convertedConfigurator.areComplexConceptsAllowed())
                throw new CommonException(
                        "Explanation configurator type not compatible with abduction manager! - MHS-MXP can't disallow complex concepts");
        } catch(NotSupportedException ignored){}

        try {
            newConfigurator.allowComplementConcepts(convertedConfigurator.areComplementConceptsAllowed());
        } catch(NotSupportedException e){
            throw new CommonException("Explanation configurator type not compatible with abduction manager!");
        }
    }

    static CatsSymbolAbducibles convertSymbolAbducibles(IAbducibles abducibles) {
        ISymbolAbducibles symbolAbducibles = (ISymbolAbducibles) abducibles;
        return new CatsSymbolAbducibles(symbolAbducibles.getSymbols());
    }

    static CatsAxiomAbducibles convertAxiomAbducibles(IAbducibles abducibles) {
        IAxiomAbducibles symbolAbducibles = (IAxiomAbducibles) abducibles;
        return new CatsAxiomAbducibles(symbolAbducibles.getAxioms());
    }

    static boolean configuratorImplementsIncompatibleInterfaces(IExplanationConfigurator configurator){
        return configurator instanceof IConceptConfigurator ||
                !(configurator instanceof IComplexConceptConfigurator) ||
                !(configurator instanceof IRoleConfigurator);
    }

}
