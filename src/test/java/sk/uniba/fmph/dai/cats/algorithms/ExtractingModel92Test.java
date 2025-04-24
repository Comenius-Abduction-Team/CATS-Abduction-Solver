package sk.uniba.fmph.dai.cats.algorithms;

import org.junit.jupiter.api.Test;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import java.io.IOException;


public class ExtractingModel92Test extends AlgorithmTestBase {

    public ExtractingModel92Test() throws OWLOntologyCreationException, IOException {
        super("ExtractingModel92Test");
    }

    @Override
    protected void setUpInput() {
        ONTOLOGY_FILE = "ont/testExtractingModel9_2.owl";

        OBSERVATION =
                "Prefix: o: <http://www.co-ode.org/ontologies/ont.owl#>"
                        + " Class: o:A Class: o:C Class: o:E"
                        + " Individual: o:a Types: o:A and o:C and o:E";

        ABDUCIBLE_PREFIX = "http://www.co-ode.org/ontologies/ont.owl#";
    }

    @Override
    protected void setUpAbducibles() {
        OWLClass D = dataFactory.getOWLClass(":D", prefixManager);
        OWLClass E = dataFactory.getOWLClass(":E", prefixManager);
        OWLClass F = dataFactory.getOWLClass(":F", prefixManager);

        symbolAbd.add(D);
        symbolAbd.add(E);
        symbolAbd.add(F);
    }

    // ------- QXP -------

    @Test
    @Override
    void qxp() {

        super.qxp();
        solve();
        testExplanationsFound(0);

    }

    @Test
    @Override
    void qxpNoNeg() {

        super.qxpNoNeg();
        solve();
        testExplanationsFound(0);

    }

    @Test
    @Override
    void qxpSymbolAbd() {

        super.qxpSymbolAbd();
        solve();
        testExplanationsFound(1);

    }

    @Test
    @Override
    void qxpSymbolAbdNoNeg() {

        super.qxpSymbolAbdNoNeg();
        solve();
        testExplanationsFound(1);

    }

    // ------- MHS -------

    @Test
    @Override
    void mhs() {

        super.mhs();
        solve();
        testExplanationsFound(4);

    }

    @Test
    @Override
    void hst() {

        super.hst();
        solve();
        testExplanationsFound(4);

    }

    @Test
    @Override
    void rct() {

        super.rct();
        solve();
        testExplanationsFound(4);

    }

    @Test
    @Override
    void mxp() {

        super.mxp();
        solve();
        testExplanationsFound(2);

    }

    @Test
    @Override
    void mhsMxp() {

        super.mhsMxp();
        solve();
        testExplanationsFound(4);

    }

    @Test
    @Override
    void hstMxp() {

        super.hstMxp();
        solve();
        testExplanationsFound(4);

    }

    @Test
    @Override
    void rctMxp() {

        super.rctMxp();
        solve();
        testExplanationsFound(4);

    }

    @Test
    @Override
    void mhsNoNeg() {

        super.mhsNoNeg();
        solve();
        testExplanationsFound(1);

    }

    @Test
    @Override
    void hstNoNeg() {

        super.hstNoNeg();
        solve();
        testExplanationsFound(1);

    }

    @Test
    @Override
    void rctNoNeg() {

        super.rctNoNeg();
        solve();
        testExplanationsFound(1);

    }

    @Test
    @Override
    void mxpNoNeg() {

        super.mxpNoNeg();
        solve();
        testExplanationsFound(1);

    }

    @Test
    @Override
    void mhsMxpNoNeg() {

        super.mhsMxpNoNeg();
        solve();
        testExplanationsFound(1);

    }

    @Test
    @Override
    void hstMxpNoNeg() {

        super.hstMxpNoNeg();
        solve();
        testExplanationsFound(1);

    }

    @Test
    @Override
    void rctMxpNoNeg() {

        super.rctMxpNoNeg();
        solve();
        testExplanationsFound(1);

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
    void rctSymbolAbd() {

        super.rctSymbolAbd();
        solve();
        testExplanationsFound(2);

    }

    @Test
    @Override
    void mxpSymbolAbd() {

        super.mxpSymbolAbd();
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
    void rctMxpSymbolAbd() {

        super.rctMxpSymbolAbd();
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
    void hstSymbolAbdNoNeg() {

        super.hstSymbolAbdNoNeg();
        solve();
        testExplanationsFound(0);

    }

    @Test
    @Override
    void rctSymbolAbdNoNeg() {

        super.rctSymbolAbdNoNeg();
        solve();
        testExplanationsFound(0);

    }

    @Test
    @Override
    void mxpSymbolAbdNoNeg() {

        super.mxpSymbolAbdNoNeg();
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

    @Test
    @Override
    void rctMxpSymbolAbdNoNeg() {

        super.rctMxpSymbolAbdNoNeg();
        solve();
        testExplanationsFound(0);

    }

}
