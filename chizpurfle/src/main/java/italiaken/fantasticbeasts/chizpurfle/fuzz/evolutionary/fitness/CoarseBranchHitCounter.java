package italiaken.fantasticbeasts.chizpurfle.fuzz.evolutionary.fitness;

import android.util.LongSparseArray;

import italiaken.fantasticbeasts.chizpurfle.L;
import italiaken.fantasticbeasts.chizpurfle.fuzz.evolutionary.Individual;
import italiaken.fantasticbeasts.chizpurfle.fuzz.evolutionary.IndividualAnalyzer;
import italiaken.fantasticbeasts.chizpurfle.instrumentation.trace.Branch;
import italiaken.fantasticbeasts.chizpurfle.instrumentation.trace.TracesMap;

/**
 * Created by ken on 06/12/17 for fantastic_beasts
 */

public class CoarseBranchHitCounter implements IndividualAnalyzer.IFitnessEvaluator {

    /*
     * branchToHitBucketsMap : map a branch.id to the hitBuckets
     * hitBuckets : for each bucket k, there is a counter that counts how many inputs executed that
     *  branch n times, where 2^k <= n < 2^(k+1)
     */
    private LongSparseArray<LongSparseArray<Integer>> branchToHitBucketsMap = new LongSparseArray<>();

    @Override
    public void analyzeForFitness(Individual individual) {

        TracesMap tracesMap = individual.getTraces();

        if (tracesMap != null){

            for (Branch branch : tracesMap.getBranches()){
                long id = branch.getId();

                LongSparseArray<Integer> hitBuckets = branchToHitBucketsMap.get(id);
                if (hitBuckets == null){
                    hitBuckets = new LongSparseArray<>();
                    hitBuckets.append(-1, Integer.MAX_VALUE);
                    branchToHitBucketsMap.append(id, hitBuckets);
                }

                if (branch.getCount() <= 0){
                    L.w("branch count is not positive of branch "+id);
                    continue;
                }
                long coarseCount = (long) Math.floor(Math.log(branch.getCount()));

                int newCount = 1 + hitBuckets.get(coarseCount, 0);

                hitBuckets.put(coarseCount, newCount);
                hitBuckets.put(-1, Math.min(newCount, hitBuckets.get(-1)));

            }

        }

    }

    /*
     * the fittest individual is the input that executed those branches with the least coarse branch
     * hit count (LCBHC), where a coarse branch hit count, for a bucket k, is defined by how many
     * inputs executed that branch n times, where 2^k <= n < 2^(k+1)
     */
    @Override
    public void evaluateFitness(Individual individual) {

        int fitnessValue = 0;

        TracesMap tracesMap = individual.getTraces();

        if (tracesMap != null){

            for (Branch branch : tracesMap.getBranches()){

                long id = branch.getId();

                LongSparseArray<Integer> hitBuckets = branchToHitBucketsMap.get(id);
                if (hitBuckets == null){
                    L.w("countersMap is null for branch "+id);
                    continue;
                }

                long coarseCount = (long) Math.floor(Math.log(branch.getCount()));

                int count = hitBuckets.get(coarseCount, 0);
                if (count == 0){
                    L.w("count is 0 of counter "+coarseCount+" of branch "+id);
                    continue;
                }

                /*
                 * the fitness value is a cumulative value, computed for each branch.
                 * If the branch has the LCBHC, it increases the individual
                 * fitness by 100, else the difference between the LCBHC and the branch count is
                 * condidered.
                 * If this difference is greater then zero, it increases the individual fitness by
                 * the difference value, else by 0.
                 */
                fitnessValue =
                        fitnessValue +
                                Math.max(0,
                                        100 - count + hitBuckets.get(-1));

            }

        }

        individual.setFitnessValue(fitnessValue);


    }


}
