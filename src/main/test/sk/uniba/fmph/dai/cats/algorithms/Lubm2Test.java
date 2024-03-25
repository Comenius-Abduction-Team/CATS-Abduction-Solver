//package sk.uniba.fmph.dai.cats.algorithms;
//
//import sk.uniba.fmph.dai.abduction_api.abducer.IExplanation;
//import org.junit.jupiter.api.Test;
//import org.semanticweb.owlapi.model.OWLOntologyCreationException;
//
//import java.io.IOException;
//import java.util.*;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertFalse;
//
//public class Lubm2Test extends AlgorithmTestBase {
//
//    private final int DEPTH_LEVEL = 3;
//
//    public Lubm2Test() throws OWLOntologyCreationException, IOException {
//        super();
//    }
//
//    @Override
//    void setUpInput() {
//        ONTOLOGY_FILE = "files/lubm-0.owl";
//
//        OBSERVATION =
//                "Prefix: p: <http://swat.cse.lehigh.edu/onto/univ-bench.owl#>"
//                + "Class: p:Publication Class: p:Employee "
//                + "Individual: p:a Types: p:Publication and p:Employee";
//
//        ABDUCIBLE_PREFIX = "http://swat.cse.lehigh.edu/onto/univ-bench.owl#";
//    }
//
//    @Override
//    void setUpAbducibles() {
//        List<String> names = Arrays.asList(":Dean",":AssociateProfessor",":Lecturer",":FullProfessor", ":Professor",
//                ":Article",":Software",":Manual",":TechnicalReport");
//        names.forEach(n -> symbolAbd.add(dataFactory.getOWLClass(n, prefixManager)));
//    }
//
//    @Test
//    void defaultMode() {
//
//        manager.setDepth(DEPTH_LEVEL);
//
//        manager.solveAbduction();
//
//        Collection<IExplanation> explanations = manager.getExplanations();
//        System.out.println(explanations);
//        assertEquals(159, explanations.size());
//
//    }
//
//    @Test
//    void mhs() {
//
//        manager.setPureMhs(true);
//        manager.setDepth(DEPTH_LEVEL);
//
//        manager.solveAbduction();
//
//        Collection<IExplanation> explanations = manager.getExplanations();
//        System.out.println(explanations);
//        assertEquals(159, explanations.size());
//
//    }
//
//    @Test
//    void noNeg() {
//
//        manager.setExplanationConfigurator(noNeg);
//        manager.setDepth(DEPTH_LEVEL);
//
//        manager.solveAbduction();
//
//        Collection<IExplanation> explanations = manager.getExplanations();
//        System.out.println(explanations);
//        assertEquals(159, explanations.size());
//
//    }
//
//    @Test
//    void mhsNoNeg() {
//
//        manager.setExplanationConfigurator(noNeg);
//        manager.setDepth(DEPTH_LEVEL);
//        manager.setPureMhs(true);
//
//        manager.solveAbduction();
//
//        Collection<IExplanation> explanations = manager.getExplanations();
//        System.out.println(explanations);
//        assertEquals(159, explanations.size());
//
//    }
//
//    @Test
//    void symbolAbd() {
//
//        manager.setAbducibles(symbolAbd);
//        manager.setDepth(DEPTH_LEVEL);
//
//        manager.solveAbduction();
//
//        Collection<IExplanation> explanations = manager.getExplanations();
//        System.out.println(explanations);
//        assertEquals(159, explanations.size());
//
//    }
//
//    @Test
//    void mhsSymbolAbd() {
//
//        manager.setAbducibles(symbolAbd);
//        manager.setPureMhs(true);
//        manager.setDepth(DEPTH_LEVEL);
//
//        manager.solveAbduction();
//
//        Collection<IExplanation> explanations = manager.getExplanations();
//        System.out.println(explanations);
//        assertEquals(159, explanations.size());
//
//    }
//
//    @Test
//    void symbolAbdNoNeg() {
//
//        manager.setAbducibles(symbolAbd);
//        manager.setExplanationConfigurator(noNeg);
//        manager.setDepth(DEPTH_LEVEL);
//
//        manager.solveAbduction();
//
//        Collection<IExplanation> explanations = manager.getExplanations();
//        System.out.println(explanations);
//        assertEquals(159, explanations.size());
//
//    }
//
//    @Test
//    void mhsSymbolAbdNoNeg() {
//
//        manager.setAbducibles(symbolAbd);
//        manager.setExplanationConfigurator(noNeg);
//        manager.setDepth(DEPTH_LEVEL);
//        manager.setPureMhs(true);
//
//        manager.solveAbduction();
//
//        Collection<IExplanation> explanations = manager.getExplanations();
//        System.out.println(explanations);
//        assertEquals(159, explanations.size());
//
//    }
//
//}
