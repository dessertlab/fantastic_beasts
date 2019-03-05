package italiaken.fantasticbeasts.chizpurfle.fuzz.evolutionary.fitness;

import android.annotation.SuppressLint;
import android.util.LongSparseArray;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import italiaken.fantasticbeasts.chizpurfle.L;
import italiaken.fantasticbeasts.chizpurfle.fuzz.evolutionary.Individual;
import italiaken.fantasticbeasts.chizpurfle.fuzz.evolutionary.IndividualAnalyzer;
import italiaken.fantasticbeasts.chizpurfle.instrumentation.trace.Branch;
import italiaken.fantasticbeasts.chizpurfle.instrumentation.trace.TracesMap;

/**
 * Created by ken on 06/12/17 for fantastic_beasts
 */

public class BranchExecutionCount implements IndividualAnalyzer.IFitnessEvaluator {

    @SuppressLint("UseSparseArrays")
    private HashMap<Long, Branch> map = new HashMap<>();
    private BranchList branchList;

    @Override
    public void analyzeForFitness(Individual individual) {

        TracesMap tracesMap = individual.getTraces();

        if (tracesMap != null){
            branchList = null;

            for (Branch branch : tracesMap.getBranches()){

                long id = branch.getId();

                Branch tmp;

                tmp = map.get(id);

                if (tmp == null)
                    tmp = new Branch(id, 0L);

                tmp.incrementCountBy(branch.getCount());

                map.put(id, tmp);

            }

        }


    }

    @Override
    public void evaluateFitness(Individual individual) {

        int fitnessValue = 0;

        TracesMap tracesMap = individual.getTraces();

        if (tracesMap != null) {

            if (branchList == null) {
                branchList = new BranchList(map.values());

                Collections.sort(branchList, new Comparator<Branch>() {
                    @Override
                    public int compare(Branch b1, Branch b2) {
                        return (int) (b2.getCount() - b1.getCount());
                    }
                });
            }

            for (Branch branch : tracesMap.getBranches()){

                long id = branch.getId();

                fitnessValue = Math.max(fitnessValue,
                        branchList.indexOfBlockWithId(id));

            }

        }

        individual.setFitnessValue(fitnessValue);

    }

    private class BranchList extends ArrayList<Branch>{

        BranchList(Collection<Branch> values) {
            super(values);
        }

        int indexOfBlockWithId(long id) {

            for (int i = 0; i<this.size(); i++){
                if (this.get(i).getId() == id)
                    return i;
            }

            return -1;
        }
    }


}
