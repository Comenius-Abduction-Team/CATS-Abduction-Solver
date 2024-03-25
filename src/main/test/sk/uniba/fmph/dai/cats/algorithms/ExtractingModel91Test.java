package sk.uniba.fmph.dai.cats.algorithms;

import sk.uniba.fmph.dai.abduction_api.abducer.IExplanation;
import org.junit.jupiter.api.Test;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import java.io.IOException;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ExtractingModel91Test extends AlgorithmTestBase {

    public ExtractingModel91Test() throws OWLOntologyCreationException, IOException {
        super();
    }

    @Override
    void setUpInput() {
        ONTOLOGY_FILE = "files/testExtractingModel9_1.owl";

        OBSERVATION =
                "Prefix: o: <http://www.co-ode.org/ontologies/ont.owl#>"
                        + " Class: o:A Class: o:C Class: o:E"
                        + " Individual: o:a Types: o:A and o:C and o:E";

        ABDUCIBLE_PREFIX = "http://www.co-ode.org/ontologies/ont.owl#";
    }

    @Override
    void setUpAbducibles() {
        OWLClass A = dataFactory.getOWLClass(":A", prefixManager);
        OWLClass B = dataFactory.getOWLClass(":B", prefixManager);
        OWLClass D = dataFactory.getOWLClass(":D", prefixManager);
        OWLClass E = dataFactory.getOWLClass(":E", prefixManager);

        symbolAbd.add(A);
        symbolAbd.add(B);
        symbolAbd.add(D);
        symbolAbd.add(E);
    }

    @Test
    void mhsMxp() {

        manager.solveAbduction();

        Collection<IExplanation> explanations = manager.getExplanations();
        System.out.println(explanations);
        assertEquals(7, explanations.size());
        //String expected = "[{¬B(a),¬F(a),¬D(a)}, {E(a),¬D(a),A(a)}, {E(a),¬D(a),¬B(a)}, {E(a),C(a),¬B(a)}, {¬D(a),A(a),¬F(a)}, {¬F(a),A(a),C(a)}, {¬F(a),¬B(a),C(a)}]";
//        String expected = "[{E(a),¬D(a)}, {¬F(a),¬D(a)}, {C(a),E(a)}, {¬F(a),C(a)}]";
//        assertEquals(expected, explanations.toString());

    }

    @Test
    void mhs() {

        manager.setAlgorithm(Algorithm.MHS);

        manager.solveAbduction();

        Collection<IExplanation> explanations = manager.getExplanations();
        System.out.println(explanations);
        assertEquals(7, explanations.size());

    }

    @Test
    void mhsMxpNoNeg() {

        manager.setExplanationConfigurator(noNeg);

        manager.solveAbduction();

        Collection<IExplanation> explanations = manager.getExplanations();
        System.out.println(explanations);
        assertEquals(0, explanations.size());

    }

    @Test
    void mhsNoNeg() {

        manager.setAlgorithm(Algorithm.MHS);

        manager.setExplanationConfigurator(noNeg);

        manager.solveAbduction();

        Collection<IExplanation> explanations = manager.getExplanations();
        System.out.println(explanations);
        assertEquals(0, explanations.size());

    }

    @Test
    void mhsMxpSymbolAbd() {

        manager.setAbducibles(symbolAbd);

        manager.solveAbduction();

        Collection<IExplanation> explanations = manager.getExplanations();
        System.out.println(explanations);
        assertEquals(2, explanations.size());

    }

    @Test
    void mhsSymbolAbd() {

        manager.setAlgorithm(Algorithm.MHS);

        manager.setAbducibles(symbolAbd);

        manager.solveAbduction();

        Collection<IExplanation> explanations = manager.getExplanations();
        System.out.println(explanations);
        assertEquals(2, explanations.size());

    }

    @Test
    void mhsMxpSymbolAbdNoNeg() {

        manager.setAbducibles(symbolAbd);
        manager.setExplanationConfigurator(noNeg);

        manager.solveAbduction();

        Collection<IExplanation> explanations = manager.getExplanations();
        System.out.println(explanations);
        assertEquals(0, explanations.size());

    }

    @Test
    void mhsSymbolAbdNoNeg() {

        manager.setAlgorithm(Algorithm.MHS);

        manager.setAbducibles(symbolAbd);
        manager.setExplanationConfigurator(noNeg);

        manager.solveAbduction();

        Collection<IExplanation> explanations = manager.getExplanations();
        System.out.println(explanations);
        assertEquals(0, explanations.size());

    }

    @Test
    void hstMxp() {

        manager.setAlgorithm(Algorithm.HST_MXP);
        manager.solveAbduction();

        Collection<IExplanation> explanations = manager.getExplanations();
        System.out.println(explanations);
        assertEquals(7, explanations.size());
        //String expected = "[{¬B(a),¬F(a),¬D(a)}, {E(a),¬D(a),A(a)}, {E(a),¬D(a),¬B(a)}, {E(a),C(a),¬B(a)}, {¬D(a),A(a),¬F(a)}, {¬F(a),A(a),C(a)}, {¬F(a),¬B(a),C(a)}]";
//        String expected = "[{E(a),¬D(a)}, {¬F(a),¬D(a)}, {C(a),E(a)}, {¬F(a),C(a)}]";
//        assertEquals(expected, explanations.toString());

    }

    @Test
    void hst() {

        manager.setAlgorithm(Algorithm.HST);

        manager.solveAbduction();

        Collection<IExplanation> explanations = manager.getExplanations();
        System.out.println(explanations);
        assertEquals(7, explanations.size());

    }

    @Test
    void hstMxpNoNeg() {

        manager.setAlgorithm(Algorithm.HST_MXP);

        manager.setExplanationConfigurator(noNeg);

        manager.solveAbduction();

        Collection<IExplanation> explanations = manager.getExplanations();
        System.out.println(explanations);
        assertEquals(0, explanations.size());

    }

    @Test
    void hstNoNeg() {

        manager.setAlgorithm(Algorithm.HST);
        manager.setExplanationConfigurator(noNeg);

        manager.solveAbduction();

        Collection<IExplanation> explanations = manager.getExplanations();
        System.out.println(explanations);
        assertEquals(0, explanations.size());

    }

    @Test
    void hstMxpSymbolAbd() {

        manager.setAlgorithm(Algorithm.HST_MXP);
        manager.setAbducibles(symbolAbd);

        manager.solveAbduction();

        Collection<IExplanation> explanations = manager.getExplanations();
        System.out.println(explanations);
        assertEquals(2, explanations.size());

    }

    @Test
    void hstSymbolAbd() {

        manager.setAlgorithm(Algorithm.HST);
        manager.setAbducibles(symbolAbd);

        manager.solveAbduction();

        Collection<IExplanation> explanations = manager.getExplanations();
        System.out.println(explanations);
        assertEquals(2, explanations.size());

    }

    @Test
    void hstMxpSymbolAbdNoNeg() {

        manager.setAlgorithm(Algorithm.HST_MXP);
        manager.setAbducibles(symbolAbd);
        manager.setExplanationConfigurator(noNeg);

        manager.solveAbduction();

        Collection<IExplanation> explanations = manager.getExplanations();
        System.out.println(explanations);
        assertEquals(0, explanations.size());

    }

    @Test
    void hstSymbolAbdNoNeg() {

        manager.setAlgorithm(Algorithm.HST);
        manager.setAbducibles(symbolAbd);
        manager.setExplanationConfigurator(noNeg);

        manager.solveAbduction();

        Collection<IExplanation> explanations = manager.getExplanations();
        System.out.println(explanations);
        assertEquals(0, explanations.size());

    }

}
