package italiaken.fantasticbeasts.chizpurfle.fuzz.generators.primitive;

import java.nio.ByteBuffer;

import italiaken.fantasticbeasts.chizpurfle.fuzz.generators.IValueGenerator;
import italiaken.fantasticbeasts.chizpurfle.fuzz.generators.ValueGeneratorException;
import italiaken.fantasticbeasts.chizpurfle.fuzz.generators.ValueGeneratorManager;

/**
 * Created by ken on 25/11/17 for fantastic_beasts
 */

public class LongGenerator implements IValueGenerator<Long>{

    private static Long[] knownValues = new Long[]{
            null, 0L, (long) -1, Long.MAX_VALUE, Long.MIN_VALUE
    };


    @Override
    public Long random() {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);

        byte[] bytes = new byte[Long.BYTES];
        ValueGeneratorManager.r.nextBytes(bytes);
        buffer.put(bytes);
        buffer.flip();

        return buffer.getLong();

    }

    @Override
    public Long mutate(Long mutant) throws ValueGeneratorException {
        try {
            if (mutant == null)
                return random();

            switch (ValueGeneratorManager.chooseMutation()){
                case KNOWN:
                    return knownValues[ValueGeneratorManager.r.nextInt(knownValues.length)];
                case NEIGHBOR:
                    return (mutant +
                            ValueGeneratorManager.r.nextLong()%ValueGeneratorManager.MAX_DELTA*2 -
                            ValueGeneratorManager.MAX_DELTA);
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
    public Long crossover(Long parent1, Long parent2) throws ValueGeneratorException {
        try {
            long mask;
            switch (ValueGeneratorManager.chooseCrossOver()){
                case SINGLE_POINT:
                    int point = ValueGeneratorManager.r.nextInt(Long.SIZE);
                    mask = (0xffffffffffffffffL << point);
                    return ((parent1 & mask) | (parent2 & ~mask));
                case TWO_POINT:
                    int point1 = ValueGeneratorManager.r.nextInt(Long.SIZE);
                    int point2 = ValueGeneratorManager.r.nextInt(Long.SIZE);
                    mask = ((0xffffffffffffffffL << point1)^(0xffffffffffffffffL << point2));
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
