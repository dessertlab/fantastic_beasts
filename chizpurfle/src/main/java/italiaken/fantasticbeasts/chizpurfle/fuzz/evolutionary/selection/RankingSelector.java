package italiaken.fantasticbeasts.chizpurfle.fuzz.evolutionary.selection;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import italiaken.fantasticbeasts.chizpurfle.fuzz.evolutionary.Individual;
import italiaken.fantasticbeasts.chizpurfle.fuzz.evolutionary.Population;
import italiaken.fantasticbeasts.chizpurfle.fuzz.generators.ValueGeneratorManager;

/**
 * Created by ken on 18/12/17 for fantastic_beasts
 */

public class RankingSelector implements Population.IIndividualSelector {

    private static final double SELECTIVE_PRESSURE = 1.5; //value must be between 1.0 and 2.0
    private List<Individual> individuals;
    private double maxValue;

    @Override
    public Population.IIndividualSelector setPopulation(Population population) {

        individuals = population.getIndividuals();

        Collections.sort(individuals, new Comparator<Individual>() {
            @Override
            public int compare(Individual individual, Individual theOther) {
                return individual.getFitnessValue() - theOther.getFitnessValue();
            }
        });

        maxValue = 0;
        for (int i = 0; i<individuals.size(); i++)
            maxValue += getRankingFitnessValue(i, individuals.size());

        return this;
    }

    @Override
    public Individual selectOne() {
        if (individuals == null)
            throw new RuntimeException("need to setup the population first!!!");

        double chosenDouble = ValueGeneratorManager.r.nextDouble() * maxValue;

        for (int i = 0; i<individuals.size(); i++){
            chosenDouble -= getRankingFitnessValue(i, individuals.size());
            if (chosenDouble<=0)
                return individuals.get(i);
        }

        throw new RuntimeException("can't select one!!!");


    }

    private double getRankingFitnessValue(int position, int size){

        return 2 -
                SELECTIVE_PRESSURE +
                2 * (SELECTIVE_PRESSURE - 1) * ((position - 1) / (size - 1));

    }

}
