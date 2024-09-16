//package sk.uniba.fmph.dai.cats.algorithms.hybrid;
//
//import sk.uniba.fmph.dai.cats.data.Explanation;
//import org.semanticweb.owlapi.model.OWLAxiom;
//import org.semanticweb.owlapi.model.OWLOntologyCreationException;
//import org.semanticweb.owlapi.model.OWLOntologyStorageException;
//
//import java.util.Collection;
//import java.util.List;
//
//public interface ExplanationManager {
//
//    void setSolver(HybridSolver solver);
//
//    void addPossibleExplanation(Explanation explanation);
//
//    void setPossibleExplanations(Collection<Explanation> possibleExplanations);
//
//    List<Explanation> getPossibleExplanations();
//
//    int getPossibleExplanationsSize();
//
//    void addLengthOneExplanation(OWLAxiom explanation);
//
//    void setLengthOneExplanations(Collection<OWLAxiom> lengthOneExplanations);
//
//    List<OWLAxiom> getLengthOneExplanations();
//
//    int getLengthOneExplanationsSize();
//
//    void processExplanations(String message) throws OWLOntologyCreationException, OWLOntologyStorageException;
//
//    void showExplanations() throws OWLOntologyStorageException, OWLOntologyCreationException;
//
//    void logError(Throwable e);
//
//    void logMessages(List<String> info, String message);
//
//    void logExplanationsWithDepth(Integer depth, boolean timeout, boolean error, Double time);
//
//    void logExplanationsWithLevel(Integer level, boolean timeout, boolean error, Double time);
//
//    Collection<Explanation> getFinalExplanations();
//
//}
