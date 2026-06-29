package sk.uniba.fmph.dai.cats.algorithms.lubm;

import sk.uniba.fmph.dai.cats.algorithms.Algorithm;
import sk.uniba.fmph.dai.cats.algorithms.Optimisation;

import java.util.Set;

public class AlgorithmConfiguration {

    public final Algorithm algorithm;
    public final Set<Optimisation> optimisations;
    public final boolean ignoreDefaultOptimisations;
    public final boolean noNeg;

    public AlgorithmConfiguration(Algorithm algorithm,
                                  Set<Optimisation> optimisations,
                                  boolean ignoreDefaultOptimisations,
                                  boolean noNeg) {

        this.algorithm = algorithm;
        this.optimisations = optimisations;
        this.ignoreDefaultOptimisations = ignoreDefaultOptimisations;
        this.noNeg = noNeg;
    }
}
