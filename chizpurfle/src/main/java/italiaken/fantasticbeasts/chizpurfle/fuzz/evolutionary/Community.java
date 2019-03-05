package italiaken.fantasticbeasts.chizpurfle.fuzz.evolutionary;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import italiaken.fantasticbeasts.chizpurfle.configuration.ConfigurationManager;
import italiaken.fantasticbeasts.chizpurfle.infiltration.MethodCaller;

/**
 * Created by ken on 24/11/17 for fantastic_beasts
 */

public class Community {

    private List<Population> populations;
    private int totalFitnessValue;

    public Community(List<MethodCaller> methodCallers) {
        populations = new ArrayList<>();

        for (MethodCaller methodCaller : methodCallers){
            populations.add(new Population(methodCaller));
        }


    }

    public void createRandomIndividuals() {
        for (Population population : populations){
            population.createRandomIndividuals();
        }
    }

    public List<Individual> getAllIndividuals() {
        List<Individual> result = new ArrayList<>();

        for (Population population : populations){
            result.addAll(population.getIndividuals());
        }

        return result;
    }

    public void updateFitnessValues() {

        totalFitnessValue = 0;
        for (Population population : populations){
            population.updateFitnessValue();
            totalFitnessValue += population.getTotalFitnessValue();
        }

    }

    public void updateTargetSizes() {

        /*
         * TODO aggiungere il concetto ESTINZIONI per rimuovere definitivamente i metodi inutili
         */

        Collections.sort(populations, new Comparator<Population>() {
            @Override
            public int compare(Population p1, Population p2) {
                //return (p2.getFitnessValue() - p1.getFitnessValue());
                int i = p2.getFitnessValue() - p1.getFitnessValue();
                return i==0 ? p1.getTargetSize() - p2.getTargetSize() : i;

            }
        });

        int communitySize = 0;
        for (Population p : populations){
            communitySize += p.getTargetSize();
        }

        int numberToSubtractToCommunitySize = Math.max(1,
                (communitySize - ConfigurationManager.getMaxCommunitySize()) / 2);


        for (int i = populations.size()-1; i >= 1 && numberToSubtractToCommunitySize > 0; i--){

            numberToSubtractToCommunitySize -= populations.get(i)
                    .decrementTargetSize(numberToSubtractToCommunitySize);

        }

        populations.get(0).incrementTargetSize();

    }


    public List<Population> getPopulations() {
        return populations;
    }

    public int getTotalFitnessValue() {
        return totalFitnessValue;
    }

}
