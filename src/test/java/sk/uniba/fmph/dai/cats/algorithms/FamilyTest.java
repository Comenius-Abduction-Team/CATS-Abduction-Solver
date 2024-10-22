package sk.uniba.fmph.dai.cats.algorithms;

import org.junit.jupiter.api.Test;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import java.io.IOException;

public class FamilyTest extends AlgorithmTestBase {

    public FamilyTest() throws OWLOntologyCreationException, IOException {
        super();
    }

    @Override
    void setUpInput() {
        ONTOLOGY_FILE = "files/family2.owl";

        OBSERVATION =
                "Prefix: prefix1: <http://www.semanticweb.org/chrumka/ontologies/2020/4/untitled-ontology-13#>"
                + "Class: prefix1:Father Class: prefix1:Mother "
                + "Individual: prefix1:jack Types: prefix1:Father Individual: prefix1:jane Types: prefix1:Mother";

        ABDUCIBLE_PREFIX = "http://www.semanticweb.org/chrumka/ontologies/2020/4/untitled-ontology-13#";
    }

    @Override
    void setUpAbducibles() {
        OWLClass grandfather = dataFactory.getOWLClass(":Grandfather", prefixManager);
        OWLClass grandmother = dataFactory.getOWLClass(":Grandmother", prefixManager);

        symbolAbd.add(grandfather);
        symbolAbd.add(grandmother);
    }

    @Test
    @Override
    void mhs() {

        super.mhs();
        abducer.setDepth(3);
        solve();
        testExplanationsFound(3);

    }

    @Test
    @Override
    void mhsNoNeg() {

        super.mhsNoNeg();
        solve();
        testExplanationsFound(4);

    }

    @Test
    @Override
    void mhsSymbolAbd() {

        super.mhsSymbolAbd();
        solve();
        testExplanationsFound(1);

    }

    @Test
    @Override
    void mhsSymbolAbdNoNeg() {

        super.mhsSymbolAbdNoNeg();
        solve();
        testExplanationsFound(1);

    }

    @Test
    @Override
    void mxp() {

        super.mxp();
        solve();
        testExplanationsFound(1);

    }

    @Test
    @Override
    void mxpNoNeg() {

        super.mxpNoNeg();
        solve();
        testExplanationsFound(0);

    }

    @Test
    @Override
    void mxpSymbolAbd() {

        super.mxpSymbolAbd();
        solve();
        testExplanationsFound(0);

    }

    @Test
    @Override
    void mxpSymbolAbdNoNeg() {

        super.mxpSymbolAbdNoNeg();
        solve();
        testExplanationsFound(1);

    }

    @Test
    @Override
    void mhsMxp() {

        super.mhsMxp();
        abducer.setDepth(3);
        solve();
        testExplanationsFound(3);

    }

    @Test
    @Override
    void mhsMxpNoNeg() {

        super.mhsMxpNoNeg();
        solve();
        testExplanationsFound(4);

    }

    @Test
    @Override
    void mhsMxpSymbolAbd() {

        super.mhsMxpSymbolAbd();
        solve();
        testExplanationsFound(1);

    }

    @Test
    @Override
    void mhsMxpSymbolAbdNoNeg() {

        super.mhsMxpSymbolAbdNoNeg();
        solve();
        testExplanationsFound(1);

    }

    @Test
    @Override
    void hst() {

        super.hst();
        abducer.setDepth(3);
        solve();
        testExplanationsFound(3);

    }

    @Test
    @Override
    void hstNoNeg() {

        super.hstNoNeg();
        solve();
        testExplanationsFound(4);

    }

    @Test
    @Override
    void hstSymbolAbd() {

        super.hstSymbolAbd();
        solve();
        testExplanationsFound(1);

    }

    @Test
    @Override
    void hstSymbolAbdNoNeg() {

        super.hstSymbolAbdNoNeg();
        solve();
        testExplanationsFound(1);

    }

    @Test
    @Override
    void hstMxp() {

        super.hstMxp();
        abducer.setDepth(3);
        solve();
        testExplanationsFound(3);

    }

    @Test
    @Override
    void hstMxpNoNeg() {

        super.hstMxpNoNeg();
        solve();
        testExplanationsFound(4);

    }

    @Test
    @Override
    void hstMxpSymbolAbd() {

        super.hstMxpSymbolAbd();
        solve();
        testExplanationsFound(1);

    }

    @Test
    @Override
    void hstMxpSymbolAbdNoNeg() {

        super.hstMxpSymbolAbdNoNeg();
        solve();
        testExplanationsFound(1);

    }

    @Test
    @Override
    void rct() {

        super.rct();
        abducer.setDepth(3);
        solve();
        testExplanationsFound(3);

    }

    @Test
    @Override
    void rctNoNeg() {

        super.rctNoNeg();
        solve();
        testExplanationsFound(4);

    }

    @Test
    @Override
    void rctSymbolAbd() {

        super.rctSymbolAbd();
        solve();
        testExplanationsFound(1);

    }

    @Test
    @Override
    void rctSymbolAbdNoNeg() {

        super.rctSymbolAbdNoNeg();
        solve();
        testExplanationsFound(1);

    }

    @Test
    @Override
    void rctMxp() {

        super.rctMxp();
        abducer.setDepth(3);
        solve();
        testExplanationsFound(3);

    }

    @Test
    @Override
    void rctMxpNoNeg() {

        super.rctMxpNoNeg();
        solve();
        testExplanationsFound(4);

    }

    @Test
    @Override
    void rctMxpSymbolAbd() {

        super.rctMxpSymbolAbd();
        solve();
        testExplanationsFound(1);

    }

    @Test
    @Override
    void rctMxpSymbolAbdNoNeg() {

        super.rctMxpSymbolAbdNoNeg();
        solve();
        testExplanationsFound(1);

    }



}
