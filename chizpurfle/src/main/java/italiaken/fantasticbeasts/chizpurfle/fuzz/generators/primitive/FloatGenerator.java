package italiaken.fantasticbeasts.chizpurfle.fuzz.generators.primitive;

import italiaken.fantasticbeasts.chizpurfle.fuzz.generators.IValueGenerator;
import italiaken.fantasticbeasts.chizpurfle.fuzz.generators.ValueGeneratorException;
import italiaken.fantasticbeasts.chizpurfle.fuzz.generators.ValueGeneratorManager;

/**
 * Created by ken on 25/11/17 for fantastic_beasts
 */

public class FloatGenerator implements IValueGenerator<Float>{

    private static Float[] knownValues = new Float[]{
            null, 0f, -1f, Float.MAX_VALUE, Float.MIN_NORMAL,
            Float.MIN_VALUE, Float.NaN, Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY
    };


    @Override
    public Float random() {
        Float result = ValueGeneratorManager.r.nextFloat() * Float.MAX_VALUE;
        if (ValueGeneratorManager.r.nextBoolean())
            return result;
        else
            return -result;
    }

    @Override
    public Float mutate(Float mutant) throws ValueGeneratorException {

        try {
            if (mutant == null)
                return random();

            switch (ValueGeneratorManager.chooseMutation()){
                case KNOWN:
                    return knownValues[ValueGeneratorManager.r.nextInt(knownValues.length)];
                case NEIGHBOR:
                    return mutant +
                            random() % (ValueGeneratorManager.MAX_DELTA * 2) -
                            ValueGeneratorManager.MAX_DELTA;
                case INVERSE:
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
    public Float crossover(Float parent1, Float parent2) throws ValueGeneratorException {
        try {
            int p1 = Float.floatToRawIntBits(parent1);
            int p2 = Float.floatToRawIntBits(parent2);
            int mask;
            switch (ValueGeneratorManager.chooseCrossOver()){
                case SINGLE_POINT:
                    int point = ValueGeneratorManager.r.nextInt(Float.SIZE);
                    mask = (0xffffffff << point);
                    return Float.intBitsToFloat((p1 & mask) | (p2 & ~mask));
                case TWO_POINT:
                    int point1 = ValueGeneratorManager.r.nextInt(Double.SIZE);
                    int point2 = ValueGeneratorManager.r.nextInt(Double.SIZE);
                    mask = ((0xffffffff << point1)^(0xffffffff << point2));
                    return Float.intBitsToFloat((p1 & mask) | (p2 & ~mask));
                case UNIFORM:
                    mask = Float.floatToRawIntBits(random());
                    return Float.intBitsToFloat((p1 & mask) | (p2 & ~mask));
                case ARITHMETIC:
                    return Float.intBitsToFloat(p1^p2);
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
