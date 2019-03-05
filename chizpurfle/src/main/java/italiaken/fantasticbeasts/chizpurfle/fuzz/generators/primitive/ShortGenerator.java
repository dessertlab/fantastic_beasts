package italiaken.fantasticbeasts.chizpurfle.fuzz.generators.primitive;

import italiaken.fantasticbeasts.chizpurfle.fuzz.generators.IValueGenerator;
import italiaken.fantasticbeasts.chizpurfle.fuzz.generators.ValueGeneratorException;
import italiaken.fantasticbeasts.chizpurfle.fuzz.generators.ValueGeneratorManager;

/**
 * Created by ken on 25/11/17 for fantastic_beasts
 */

public class ShortGenerator implements IValueGenerator<Short> {

    private static Short[] knownValues = new Short[]{
            0, -1, Short.MAX_VALUE, Short.MIN_VALUE, Short.SIZE
    };


    @Override
    public Short random() {
        return (short) ValueGeneratorManager.r.nextInt();
    }

    @Override
    public Short mutate(Short mutant) throws ValueGeneratorException {
        try {
            if (mutant == null)
                return random();

            switch (ValueGeneratorManager.chooseMutation()){
                case KNOWN:
                    return knownValues[ValueGeneratorManager.r.nextInt(knownValues.length)];
                case NEIGHBOR:
                    return (short) (mutant +
                            ValueGeneratorManager.r.nextInt(ValueGeneratorManager.MAX_DELTA*2) -
                            ValueGeneratorManager.MAX_DELTA);
                case INVERSE:
                    if (ValueGeneratorManager.r .nextBoolean())
                        return (short) (1 / mutant);
                    else
                        return (short) (-mutant);
            }

        }catch (Exception e){
            throw new ValueGeneratorException("can't mutate " + mutant, e);
        }

        return null;
    }

    @Override
    public Short crossover(Short parent1, Short parent2) throws ValueGeneratorException {
        try {
            short mask;
            switch (ValueGeneratorManager.chooseCrossOver()){
                case SINGLE_POINT:
                    int point = ValueGeneratorManager.r.nextInt(Short.SIZE);
                    mask = (short) (0xffff << point);
                    return (short)((parent1 & mask) | (parent2 & ~mask));
                case TWO_POINT:
                    int point1 = ValueGeneratorManager.r.nextInt(Short.SIZE);
                    int point2 = ValueGeneratorManager.r.nextInt(Short.SIZE);
                    mask = (short)((0xffff << point1)^(0xffff << point2));
                    return (short)((parent1 & mask) | (parent2 & ~mask));
                case UNIFORM:
                    mask = random();
                    return (short)((parent1 & mask) | (parent2 & ~mask));
                case ARITHMETIC:
                    return (short)(parent1^parent2);
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
