package italiaken.fantasticbeasts.chizpurfle.fuzz.generators.primitive;

import italiaken.fantasticbeasts.chizpurfle.fuzz.generators.IValueGenerator;
import italiaken.fantasticbeasts.chizpurfle.fuzz.generators.ValueGeneratorException;
import italiaken.fantasticbeasts.chizpurfle.fuzz.generators.ValueGeneratorManager;


/**
 * Created by ken on 25/11/17 for fantastic_beasts
 */

public class DoubleGenerator implements IValueGenerator<Double>{

    private static Double[] knownValues = new Double[]{
            null, 0d, (double) -1,  Double.MAX_VALUE, Double.MIN_NORMAL, Double.MIN_VALUE,
            Double.NaN, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY
    };


    @Override
    public Double random() {
        Double result =  ValueGeneratorManager.r.nextDouble() * Double.MAX_VALUE;
        if (ValueGeneratorManager.r.nextBoolean())
            return result;
        else
            return -result;
    }

    @Override
    public Double mutate(Double mutant) throws ValueGeneratorException {

        try {
            if (mutant == null)
                return random();

            switch (ValueGeneratorManager.chooseMutation()){
                case KNOWN:
                    return knownValues[ValueGeneratorManager.r.nextInt(knownValues.length)];
                case NEIGHBOR:
                    return mutant +
                            ( random() % (ValueGeneratorManager.MAX_DELTA * 2) ) -
                            ValueGeneratorManager.MAX_DELTA;
                case INVERSE:
                    if (ValueGeneratorManager.r .nextBoolean())
                        return 1/mutant;
                    else
                        return -mutant;
            }
        } catch (Exception e) {
            throw new ValueGeneratorException("can't mutate "+mutant, e);
        }


        return null;
    }

    @Override
    public Double crossover(Double parent1, Double parent2) throws ValueGeneratorException {
        try {
            long p1 = Double.doubleToRawLongBits(parent1);
            long p2 = Double.doubleToRawLongBits(parent2);
            long mask;
            switch (ValueGeneratorManager.chooseCrossOver()){
                case SINGLE_POINT:
                    int point = ValueGeneratorManager.r.nextInt(Double.SIZE);
                    mask = (0xffffffffffffffffL << point);
                    return Double.longBitsToDouble((p1 & mask) | (p2 & ~mask));
                case TWO_POINT:
                    int point1 = ValueGeneratorManager.r.nextInt(Double.SIZE);
                    int point2 = ValueGeneratorManager.r.nextInt(Double.SIZE);
                    mask = ((0xffffffffffffffffL << point1)^(0xffffffffffffffffL << point2));
                    return Double.longBitsToDouble((p1 & mask) | (p2 & ~mask));
                case UNIFORM:
                    mask = Double.doubleToRawLongBits(random());
                    return Double.longBitsToDouble((p1 & mask) | (p2 & ~mask));
                case ARITHMETIC:
                    return Double.longBitsToDouble(p1^p2);
            }
        }catch (Exception e){
            if (parent1 != null && parent2 == null)
                return parent1;
            if (parent2 != null && parent1 == null)
                return parent2;
            if (parent1 == null && parent2 == null)
                return null;

            throw new ValueGeneratorException("can't crossover "+parent1+" and "+parent2, e);
        }

        return null;
    }
}
