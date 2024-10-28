package sk.uniba.fmph.dai.cats.algorithms;

import org.junit.jupiter.api.Test;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import java.io.IOException;

public class ToothacheTest extends AlgorithmTestBase {

    public ToothacheTest() throws OWLOntologyCreationException, IOException {
        super("ToothacheTest");
    }

    @Override
    protected void setUpInput() {
        ONTOLOGY_FILE = "files/toothache.rdf";

        OBSERVATION =
                "Prefix: o: <http://www.semanticweb.org/janbo/ontologies/2024/4/toothache#>"
                        + " Class: o:Toothache"
                        + " Individual: o:John Types: o:Toothache";

        ABDUCIBLE_PREFIX = "http://www.semanticweb.org/janbo/ontologies/2024/4/toothache#";
    }

    @Override
    protected void setUpAbducibles() {
        OWLClass cold = dataFactory.getOWLClass(":DrankColdDrink", prefixManager);
        OWLClass sensitive = dataFactory.getOWLClass(":SensitiveTeeth", prefixManager);
        OWLClass cavity = dataFactory.getOWLClass(":Cavity", prefixManager);

        symbolAbd.add(cold);
        symbolAbd.add(sensitive);
        symbolAbd.add(cavity);
    }

    @Test
    @Override
    void mhs() {

        super.mhs();
        solve();
        testExplanationsFound(3);

    }

    @Test
    @Override
    void mhsNoNeg() {

        super.mhsNoNeg();
        solve();
        testExplanationsFound(3);

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
    void mhsSymbolAbdNoNeg() {

        super.mhsSymbolAbdNoNeg();
        solve();
        testExplanationsFound(2);

    }

    @Test
    @Override
    void mxp() {

        super.mxp();
        solve();
        testExplanationsFound(3);

    }

    @Test
    @Override
    void mxpNoNeg() {

        super.mxpNoNeg();
        solve();
        testExplanationsFound(2);

    }

    @Test
    @Override
    void mxpSymbolAbd() {

        super.mxpSymbolAbd();
        solve();
        testExplanationsFound(1);

    }

    @Test
    @Override
    void mxpSymbolAbdNoNeg() {

        super.mxpSymbolAbdNoNeg();
        solve();
        testExplanationsFound(2);

    }

    @Test
    @Override
    void mhsMxp() {

        super.mhsMxp();
        solve();
        testExplanationsFound(3);

    }

    @Test
    @Override
    void mhsMxpNoNeg() {

        super.mhsMxp();
        solve();
        testExplanationsFound(3);

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
    void mhsMxpSymbolAbdNoNeg() {

        super.mhsMxpSymbolAbdNoNeg();
        solve();
        testExplanationsFound(2);

    }

    @Test
    @Override
    void hst() {

        super.hst();
        solve();
        testExplanationsFound(3);

    }

    @Test
    @Override
    void hstNoNeg() {

        super.hstNoNeg();
        solve();
        testExplanationsFound(3);

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
    void hstSymbolAbdNoNeg() {

        super.hstSymbolAbdNoNeg();
        solve();
        testExplanationsFound(2);

    }

    @Test
    @Override
    void hstMxp() {

        super.hstMxp();
        solve();
        testExplanationsFound(3);

    }

    @Test
    @Override
    void hstMxpNoNeg() {

        super.hstMxpNoNeg();
        solve();
        testExplanationsFound(3);

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
    void hstMxpSymbolAbdNoNeg() {

        super.hstMxpSymbolAbdNoNeg();
        solve();
        testExplanationsFound(2);

    }

    @Test
    @Override
    void rct() {

        super.rct();
        solve();
        testExplanationsFound(3);

    }

    @Test
    @Override
    void rctNoNeg() {

        super.rctNoNeg();
        solve();
        testExplanationsFound(3);

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
    void rctSymbolAbdNoNeg() {

        super.rctSymbolAbdNoNeg();
        solve();
        testExplanationsFound(2);

    }

    @Test
    @Override
    void rctMxp() {

        super.rctMxp();
        solve();
        testExplanationsFound(3);

    }

    @Test
    @Override
    void rctMxpNoNeg() {

        super.rctMxpNoNeg();
        solve();
        testExplanationsFound(3);

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
    void rctMxpSymbolAbdNoNeg() {

        super.rctMxpSymbolAbdNoNeg();
        solve();
        testExplanationsFound(2);

    }

}
