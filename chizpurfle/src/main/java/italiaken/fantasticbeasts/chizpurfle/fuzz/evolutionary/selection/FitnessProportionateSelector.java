package italiaken.fantasticbeasts.chizpurfle.fuzz.evolutionary.selection;

import italiaken.fantasticbeasts.chizpurfle.fuzz.evolutionary.Individual;
import italiaken.fantasticbeasts.chizpurfle.fuzz.evolutionary.Population;
import italiaken.fantasticbeasts.chizpurfle.fuzz.generators.ValueGeneratorManager;

/**
 * Created by ken on 18/12/17 for fantastic_beasts
 */

public class FitnessProportionateSelector implements Population.IIndividualSelector {

    private Population population;

    @Override
    public Population.IIndividualSelector setPopulation(Population population) {
        this.population = population;
        return this;
    }

    @Override
    public Individual selectOne() {

        if (population.getTotalFitnessValue() == 0)
            return population.getIndividuals().get(
                    ValueGeneratorManager.r.nextInt(population.getIndividuals().size()));

        int chosenInt = ValueGeneratorManager.r.nextInt(population.getTotalFitnessValue());

        for (Individual individual : population.getIndividuals()){

            chosenInt -= individual.getFitnessValue();
            if (chosenInt<=0)
                return individual;

        }

        throw new RuntimeException("can't select one!!!");

    }
}
