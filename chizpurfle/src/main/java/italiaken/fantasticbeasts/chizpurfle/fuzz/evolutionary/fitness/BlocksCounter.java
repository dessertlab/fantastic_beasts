package italiaken.fantasticbeasts.chizpurfle.fuzz.evolutionary.fitness;

import java.util.Map;
import java.util.Set;

import italiaken.fantasticbeasts.chizpurfle.L;
import italiaken.fantasticbeasts.chizpurfle.fuzz.ITest;
import italiaken.fantasticbeasts.chizpurfle.fuzz.evolutionary.IndividualAnalyzer;
import italiaken.fantasticbeasts.chizpurfle.fuzz.evolutionary.Individual;
import italiaken.fantasticbeasts.chizpurfle.instrumentation.trace.Block;
import italiaken.fantasticbeasts.chizpurfle.instrumentation.trace.Branch;
import italiaken.fantasticbeasts.chizpurfle.instrumentation.trace.ITrace;
import italiaken.fantasticbeasts.chizpurfle.instrumentation.trace.TracesMap;

/**
 * Created by ken on 27/11/17 for fantastic_beasts
 */

public class BlocksCounter implements IndividualAnalyzer.IFitnessEvaluator {

    @Override
    public void analyzeForFitness(Individual individual) {

        /*
         * nothing to analyze for this fitness evaluation
         */

    }

    @Override
    public void evaluateFitness(Individual individual) {

        int fitnessValue = 0;

        TracesMap tracesMap = individual.getTraces();

        if (tracesMap != null){

            for (Block block : tracesMap.getBlocks()){
                fitnessValue += block.getCount();
            }

        }

        individual.setFitnessValue(fitnessValue);

    }

}
