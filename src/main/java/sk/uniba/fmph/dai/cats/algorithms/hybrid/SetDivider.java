package sk.uniba.fmph.dai.cats.algorithms.hybrid;

import sk.uniba.fmph.dai.cats.common.Configuration;
import sk.uniba.fmph.dai.cats.data.*;
import org.semanticweb.owlapi.model.OWLAxiom;

import java.util.*;

public class SetDivider {

    ExplanationManager explanationManager;
    private Map<AxiomPair, Integer> tableOfAxiomPairOccurance;
    private List<Integer> numberOfAxiomPairOccurance;
    private double median = 0;
    public Set<Integer> notUsedExplanations;
    private int lastUsedIndex;

    public SetDivider(HybridSolver hybridSolver){
        this.explanationManager = hybridSolver.getExplanationManager();
        tableOfAxiomPairOccurance = new HashMap<>();
        numberOfAxiomPairOccurance = new ArrayList<>();
        notUsedExplanations = new HashSet<>();
        lastUsedIndex = -1;
    }

    public void decreaseMedian(){
        median /= 2;
        if(median < 1){
            median = 0;
        }
    }

    public void setMedian(double median){
        this.median = median;
    }

    public double getMedian(){
        return median;
    }

    public void setIndexesOfExplanations(int sizeOfCollection){
        for(int i = 0; i < sizeOfCollection; i++){
            notUsedExplanations.add(i);
        }
    }

    public void addIndexToIndexesOfExplanations(int index){
        if(index != -1){
            notUsedExplanations.add(index);
        }
    }

    public List<AxiomSet> divideIntoSets(Set<OWLAxiom> literals) {
        if(Configuration.CACHED_CONFLICTS_LONGEST_CONFLICT && explanationManager.getPossibleExplanationsSize() > 0 && lastUsedIndex != -1){
            return divideIntoSetsAccordingTheLongestConflict(literals);
        } else if (Configuration.CACHED_CONFLICTS_MEDIAN && explanationManager.getPossibleExplanationsSize() > 0){
            return divideIntoSetsAccordingTableOfLiteralsPairOccurrence(literals);
        }
        return divideIntoSetsWithoutCondition(literals);
    }

    public List<AxiomSet> divideIntoSetsWithoutCondition(Set<OWLAxiom> literals){
        List<AxiomSet> dividedLiterals = new ArrayList<>();

        dividedLiterals.add(new AxiomSet());
        dividedLiterals.add(new AxiomSet());

        int count = 0;

        for (OWLAxiom owlAxiom : literals) {
            dividedLiterals.get(count % 2).add(owlAxiom);
            count++;
        }
        return dividedLiterals;
    }

    private List<AxiomSet> divideIntoSetsAccordingTheLongestConflict(Set<OWLAxiom> literals){
        Explanation theLongestExplanation = explanationManager.getPossibleExplanations().get(lastUsedIndex);
        Set<OWLAxiom> axiomsFromExplanation = new HashSet<>(theLongestExplanation.getAxioms());

        List<AxiomSet> dividedLiterals = new ArrayList<>();
        dividedLiterals.add(new AxiomSet());
        dividedLiterals.add(new AxiomSet());

        int count = 0;
        for(OWLAxiom owlAxiom : axiomsFromExplanation){
            if(literals.contains(owlAxiom)){
                dividedLiterals.get(count % 2).add(owlAxiom);
                count++;
            }
        }

        for(OWLAxiom owlAxiom : literals) {
            if(!axiomsFromExplanation.contains(owlAxiom)){
                dividedLiterals.get(count % 2).add(owlAxiom);
                count++;
            }
        }
        return dividedLiterals;
    }

    public int getIndexOfTheLongestAndNotUsedConflict(){
        int indexOfLongestExp = -1;
        int length = 0;

        for(Integer i : notUsedExplanations){
            if(explanationManager.getPossibleExplanations().get(i).getDepth() > length){
                indexOfLongestExp = i;
            }
        }

        lastUsedIndex = indexOfLongestExp;
        if(indexOfLongestExp == -1){
            return -1;
        }
        notUsedExplanations.remove(indexOfLongestExp);
        return indexOfLongestExp;
    }

    private List<AxiomSet> divideIntoSetsAccordingTableOfLiteralsPairOccurrence(Set<OWLAxiom> literals){
        Set<OWLAxiom> axiomsFromLiterals = new HashSet<>(literals);
        List<AxiomSet> dividedLiterals = new ArrayList<>();
        dividedLiterals.add(new AxiomSet());
        dividedLiterals.add(new AxiomSet());

        for(AxiomPair key : tableOfAxiomPairOccurance.keySet()){
            if(axiomsFromLiterals.contains(key.first) && axiomsFromLiterals.contains(key.second)){
                if(tableOfAxiomPairOccurance.get(key) >= median){
                    dividedLiterals.get(0).add(key.first);
                    dividedLiterals.get(1).add(key.second);
                    axiomsFromLiterals.remove(key.first);
                    axiomsFromLiterals.remove(key.second);
                }
            }
        }

        int count = 0;
        for (OWLAxiom owlAxiom : axiomsFromLiterals) {
            dividedLiterals.get(count % 2).add(owlAxiom);
            count++;
        }

        decreaseMedian();
        return dividedLiterals;
    }

    public void addPairsOfLiteralsToTable(Explanation explanation){
        LinkedList<OWLAxiom> expAxioms;
        if (explanation.getAxioms() != null)
            expAxioms = (LinkedList<OWLAxiom>) explanation.getAxioms();
        else
            expAxioms = new LinkedList<>(explanation.getAxioms());

        for(int i = 0; i < expAxioms.size(); i++){
            for(int j = i + 1; j < expAxioms.size(); j++){
                AxiomPair axiomPair = new AxiomPair(expAxioms.get(i), expAxioms.get(j));
                Integer value = tableOfAxiomPairOccurance.getOrDefault(axiomPair, 0) + 1;
                tableOfAxiomPairOccurance.put(axiomPair, value);
                addToListOfAxiomPairOccurance(value);
            }
        }
        setMedianFromListOfAxiomPairOccurance();
    }

    public void addToListOfAxiomPairOccurance(Integer value){
        int index = 0;
        for (Integer integer : numberOfAxiomPairOccurance) {
            if (integer > value) {
                break;
            }
            index++;
        }
        numberOfAxiomPairOccurance.add(index, value);
    }

    private void setMedianFromListOfAxiomPairOccurance(){
        if(numberOfAxiomPairOccurance.isEmpty()){
            return;
        }
        if(numberOfAxiomPairOccurance.size() % 2 == 0){
            int index = numberOfAxiomPairOccurance.size()/2;
            median = (numberOfAxiomPairOccurance.get(index - 1) + numberOfAxiomPairOccurance.get(index)) / 2.0;
        } else {
            int index = (numberOfAxiomPairOccurance.size() - 1)/2;
            median = numberOfAxiomPairOccurance.get(index);
        }
    }

}
