package sk.uniba.fmph.dai.cats.algorithms;

import org.junit.jupiter.api.Test;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import java.io.IOException;

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
    @Override
    void mhs() {

        super.mhs();
        solve();
        testExplanationsFound(7);

    }

    @Test
    @Override
    void hst() {

        super.hst();
        solve();
        testExplanationsFound(7);

    }

    @Test
    @Override
    void mhsMxp() {

        super.mhsMxp();
        solve();
        testExplanationsFound(7);

    }

    @Test
    @Override
    void hstMxp() {

        super.hstMxp();
        solve();
        testExplanationsFound(7);

    }

    @Test
    @Override
    void mhsNoNeg() {

        super.mhsNoNeg();
        solve();
        testExplanationsFound(0);

    }

    @Test
    @Override
    void hstNoNeg() {

        super.hstNoNeg();
        solve();
        testExplanationsFound(0);

    }

    @Test
    @Override
    void mhsMxpNoNeg() {

        super.mhsMxpNoNeg();
        solve();
        testExplanationsFound(0);

    }

    @Test
    @Override
    void hstMxpNoNeg() {

        super.hstMxpNoNeg();
        solve();
        testExplanationsFound(0);

    }

    @Test
    @Override
    void mhsSymbolAbd() {

        super.mhsSymbolAbd();
        solve();
        testExplanationsFound(2);

    }

    @Test
    @Override
    void hstSymbolAbd() {

        super.hstSymbolAbd();
        solve();
        testExplanationsFound(2);

    }

    @Test
    @Override
    void mhsMxpSymbolAbd() {

        super.mhsMxpSymbolAbd();
        solve();
        testExplanationsFound(2);

    }

    @Test
    @Override
    void hstMxpSymbolAbd() {

        super.hstMxpSymbolAbd();
        solve();
        testExplanationsFound(2);

    }

    @Test
    @Override
    void mhsSymbolAbdNoNeg() {

        super.mhsSymbolAbdNoNeg();
        solve();
        testExplanationsFound(0);

    }

    @Test
    @Override
    void mhsMxpSymbolAbdNoNeg() {

        super.mhsMxpSymbolAbdNoNeg();
        solve();
        testExplanationsFound(0);

    }

    @Test
    @Override
    void hstMxpSymbolAbdNoNeg() {

        super.hstMxpSymbolAbdNoNeg();
        solve();
        testExplanationsFound(0);

    }

}
