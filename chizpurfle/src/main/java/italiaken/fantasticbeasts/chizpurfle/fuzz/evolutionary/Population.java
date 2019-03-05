package italiaken.fantasticbeasts.chizpurfle.fuzz.evolutionary;

import java.util.ArrayList;
import java.util.List;

import italiaken.fantasticbeasts.chizpurfle.L;
import italiaken.fantasticbeasts.chizpurfle.configuration.ConfigurationManager;
import italiaken.fantasticbeasts.chizpurfle.fuzz.generators.ParametersGenerator;
import italiaken.fantasticbeasts.chizpurfle.fuzz.generators.ValueGeneratorException;
import italiaken.fantasticbeasts.chizpurfle.fuzz.generators.ValueGeneratorManager;
import italiaken.fantasticbeasts.chizpurfle.infiltration.MethodCaller;

/**
 * Created by ken on 24/11/17 for fantastic_beasts
 */

public class Population {

    private final MethodCaller methodCaller;
    private final ParametersGenerator generator;

    private int targetSize;

    private List<Individual> individuals;
    private List<Individual> offsprings;

    private int fitnessValue;
    private int totalFitnessValue;

    public Population(MethodCaller methodCaller) {
        this.methodCaller = methodCaller;
        this.generator = new ParametersGenerator(methodCaller.getParameterTypes());

        this.targetSize = ConfigurationManager.getPopulationInitialTargetSize();
        individuals = new ArrayList<>();
        offsprings = new ArrayList<>();
    }

    public void createRandomIndividuals() {
        if (!individuals.isEmpty()){
            L.w("creating random individuals deleting previous generations");
            individuals = new ArrayList<>();
        }
        for (int i = 0; i<targetSize; i++){
            individuals.add(new Individual(0, methodCaller, generator.random()));
            System.gc();
        }
    }

    public List<Individual> getIndividuals() {
        return individuals;
    }

    public void updateFitnessValue() {

        totalFitnessValue = 0;

        for (Individual individual : individuals){
            totalFitnessValue += individual.getFitnessValue();
        }

        fitnessValue = totalFitnessValue/individuals.size();

    }

    public int getFitnessValue() {
        return fitnessValue;
    }

    public int decrementTargetSize(int i){

        int oldTargetSize = targetSize;
        targetSize = oldTargetSize - i;

        if (targetSize < 2)
            targetSize = 2;

        return oldTargetSize - targetSize;

    }

    public void incrementTargetSize() {
        targetSize++;
    }

    public void generateOffspring() {

        L.d("generating offsprings "+ this.methodCaller.getMethodName()+" ("+targetSize+")");

        IIndividualSelector selector = ConfigurationManager
                .getIndividualSelector()
                .setPopulation(this);

        while (offsprings.size() < targetSize) {
            Individual parent1 = selector.selectOne();
            Individual offspring;
            int generationNumber = parent1.getGenerationNumber() + 1;

            if (ValueGeneratorManager.r.nextInt(100)
                    < ConfigurationManager.getCrossOverRate()){

                try {
                    offspring = new Individual(generationNumber, methodCaller, generator.crossover(
                            parent1.getParameters(), selector.selectOne().getParameters()));
                } catch (ValueGeneratorException e) {
                    L.w("can't crosserver, parent 1 survive",e);
                    offspring = new Individual(generationNumber, methodCaller, parent1.getParameters());
                }

            }else {
                offspring = new Individual(generationNumber, methodCaller, parent1.getParameters());
            }


            try {
                if (ValueGeneratorManager.r.nextInt(100)
                        < ConfigurationManager.getMutationRate()){
                    offspring = new Individual(generationNumber, methodCaller, generator.mutate(
                            offspring.getParameters()));
                }
            } catch (ValueGeneratorException e) {
                L.w("problem in mutate the individual "+ offspring);
            }

            offsprings.add(offspring);
        }


    }

    public void goToNextGeneration() {
        individuals = offsprings;
        offsprings = new ArrayList<>();
    }

    public int getTargetSize() {
        return targetSize;
    }

    public int getTotalFitnessValue() {
        return totalFitnessValue;
    }

    public interface IIndividualSelector{

        IIndividualSelector setPopulation(Population population);

        Individual selectOne();

    }
}
