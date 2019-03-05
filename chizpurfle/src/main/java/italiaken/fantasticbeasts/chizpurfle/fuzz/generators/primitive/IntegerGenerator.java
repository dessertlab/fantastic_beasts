package italiaken.fantasticbeasts.chizpurfle.fuzz.generators.primitive;

import italiaken.fantasticbeasts.chizpurfle.fuzz.generators.IValueGenerator;
import italiaken.fantasticbeasts.chizpurfle.fuzz.generators.ValueGeneratorException;
import italiaken.fantasticbeasts.chizpurfle.fuzz.generators.ValueGeneratorManager;

/**
 * Created by ken on 25/11/17 for fantastic_beasts
 */

public class IntegerGenerator implements IValueGenerator<Integer> {

    private static Integer[] knownValues = new Integer[]{
            null, 0, -1, Integer.MAX_VALUE, Integer.MIN_VALUE
    };


    @Override
    public Integer random() {
        return ValueGeneratorManager.r.nextInt();
    }

    @Override
    public Integer mutate(Integer mutant) throws ValueGeneratorException {

        try {
            if (mutant == null)
                return random();

            switch (ValueGeneratorManager.chooseMutation()){
                case KNOWN:
                    return knownValues[ValueGeneratorManager.r.nextInt(knownValues.length)];
                case NEIGHBOR:
                    return (mutant +
                            ValueGeneratorManager.r.nextInt(ValueGeneratorManager.MAX_DELTA*2) -
                            ValueGeneratorManager.MAX_DELTA);
                case INVERSE:
                    if (mutant == 0)
                        return random();
                    if (ValueGeneratorManager.r .nextBoolean())
                        return 1/mutant;
                    else
                        return -mutant;
            }

        }catch (Exception e){
            throw new ValueGeneratorException("can't mutate " + mutant, e);
        }

        return null;
    }

    @Override
    public Integer crossover(Integer parent1, Integer parent2) throws ValueGeneratorException {
        try {
            int mask;
            switch (ValueGeneratorManager.chooseCrossOver()){
                case SINGLE_POINT:
                    int point = ValueGeneratorManager.r.nextInt(Integer.SIZE);
                    mask = (0xffffffff << point);
                    return ((parent1 & mask) | (parent2 & ~mask));
                case TWO_POINT:
                    int point1 = ValueGeneratorManager.r.nextInt(Integer.SIZE);
                    int point2 = ValueGeneratorManager.r.nextInt(Integer.SIZE);
                    mask = ((0xffffffff << point1)^(0xffffffff << point2));
                    return ((parent1 & mask) | (parent2 & ~mask));
                case UNIFORM:
                    mask = random();
                    return ((parent1 & mask) | (parent2 & ~mask));
                case ARITHMETIC:
                    return (parent1^parent2);
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
