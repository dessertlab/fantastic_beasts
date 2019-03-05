package italiaken.fantasticbeasts.chizpurfle.fuzz.evolutionary.selection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

import italiaken.fantasticbeasts.chizpurfle.fuzz.evolutionary.Individual;
import italiaken.fantasticbeasts.chizpurfle.fuzz.evolutionary.Population;
import italiaken.fantasticbeasts.chizpurfle.fuzz.generators.ValueGeneratorManager;

/**
 * Created by ken on 18/12/17 for fantastic_beasts
 */

public class TournamentSelector implements Population.IIndividualSelector {

    private List<Individual> individuals;
    private static final int TOURNAMENT_SIZE = 10; //http://www.geatbx.com/docu/algindex-02.html#P244_16021

    @Override
    public Population.IIndividualSelector setPopulation(Population population) {

        individuals = population.getIndividuals();

        return this;
    }

    @Override
    public Individual selectOne() {

        List<Individual> tournamentList = new ArrayList<>();

        int targetTounamentSize = TOURNAMENT_SIZE;
        if (individuals.size() <= targetTounamentSize)
            targetTounamentSize = individuals.size()-1;

        tournamentList.addAll(individuals);

        while (tournamentList.size() == targetTounamentSize){
            tournamentList
                    .remove(
                            tournamentList.get(
                                    ValueGeneratorManager.r.nextInt(tournamentList.size())));
        }

        Collections.sort(tournamentList, new Comparator<Individual>() {
            @Override
            public int compare(Individual individual, Individual theOther) {
                return theOther.getFitnessValue() - individual.getFitnessValue();
            }
        });

        return tournamentList.get(0);
    }

}
