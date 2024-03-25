//package sk.uniba.fmph.dai.cats.algorithms;
//
//import sk.uniba.fmph.dai.abduction_api.abducer.IExplanation;
//import org.junit.jupiter.api.Test;
//import org.semanticweb.owlapi.model.OWLClass;
//import org.semanticweb.owlapi.model.OWLOntologyCreationException;
//
//import java.io.IOException;
//import java.util.Collection;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//public class FamilyTest extends AlgorithmTestBase {
//
//    public FamilyTest() throws OWLOntologyCreationException, IOException {
//        super();
//    }
//
//    @Override
//    void setUpInput() {
//        ONTOLOGY_FILE = "files/family2.owl";
//
//        OBSERVATION =
//                "Prefix: prefix1: <http://www.semanticweb.org/chrumka/ontologies/2020/4/untitled-ontology-13#>"
//                + "Class: prefix1:Father Class: prefix1:Mother "
//                + "Individual: prefix1:jack Types: prefix1:Father Individual: prefix1:jane Types: prefix1:Mother";
//
//        ABDUCIBLE_PREFIX = "http://www.semanticweb.org/chrumka/ontologies/2020/4/untitled-ontology-13#";
//    }
//
//    @Override
//    void setUpAbducibles() {
//        OWLClass grandfather = dataFactory.getOWLClass(":Grandfather", prefixManager);
//        OWLClass grandmother = dataFactory.getOWLClass(":Grandmother", prefixManager);
//
//        symbolAbd.add(grandfather);
//        symbolAbd.add(grandmother);
//    }
//
//    @Test
//    void mhsMxp() {
//
//        manager.setDepth(3);
//
//        manager.solveAbduction();
//
//        Collection<IExplanation> explanations = manager.getExplanations();
//        System.out.println(explanations.size()); //3
//        System.out.println(explanations);
//        assertFalse(explanations.isEmpty());
//
//    }
//
//    @Test
//    void mhs() {
//
//        manager.setPureMhs(true);
//        manager.setDepth(3);
//
//        manager.solveAbduction();
//
//        Collection<IExplanation> explanations = manager.getExplanations();
//        System.out.println(explanations.size()); //3
//        System.out.println(explanations);
//        assertFalse(explanations.isEmpty());
//
//    }
//
//    @Override
//    void mhsMxpNoNeg() {
//
//    }
//
//    @Test
//    void hybridNoNeg() {
//
//        manager.setExplanationConfigurator(noNeg);
//
//        manager.solveAbduction();
//
//        Collection<IExplanation> explanations = manager.getExplanations();
//        System.out.println(explanations);
//        assertEquals(4, explanations.size());
//
//    }
//
//    @Test
//    void mhsNoNeg() {
//
//        manager.setExplanationConfigurator(noNeg);
//        manager.setPureMhs(true);
//
//        manager.solveAbduction();
//
//        Collection<IExplanation> explanations = manager.getExplanations();
//        System.out.println(explanations);
//        assertEquals(4, explanations.size());
//
//    }
//
//    @Test
//    void mhsMxpSymbolAbd() {
//
//        manager.setAbducibles(symbolAbd);
//
//        manager.solveAbduction();
//
//        Collection<IExplanation> explanations = manager.getExplanations();
//        System.out.println(explanations);
//        assertEquals(1, explanations.size());
//
//    }
//
//    @Test
//    void mhsSymbolAbd() {
//
//        manager.setAbducibles(symbolAbd);
//        manager.setPureMhs(true);
//
//        manager.solveAbduction();
//
//        Collection<IExplanation> explanations = manager.getExplanations();
//        System.out.println(explanations);
//        assertEquals(1, explanations.size());
//
//    }
//
//    @Test
//    void mhsMxpSymbolAbdNoNeg() {
//
//        manager.setAbducibles(symbolAbd);
//        manager.setExplanationConfigurator(noNeg);
//
//        manager.solveAbduction();
//
//        Collection<IExplanation> explanations = manager.getExplanations();
//        System.out.println(explanations);
//        assertEquals(1, explanations.size());
//
//    }
//
//    @Test
//    void mhsSymbolAbdNoNeg() {
//
//        manager.setAbducibles(symbolAbd);
//        manager.setExplanationConfigurator(noNeg);
//        manager.setPureMhs(true);
//
//        manager.solveAbduction();
//
//        Collection<IExplanation> explanations = manager.getExplanations();
//        System.out.println(explanations);
//        assertEquals(1, explanations.size());
//
//    }
//
//}
