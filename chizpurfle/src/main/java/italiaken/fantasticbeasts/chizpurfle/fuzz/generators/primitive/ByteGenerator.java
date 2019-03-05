package italiaken.fantasticbeasts.chizpurfle.fuzz.generators.primitive;

import java.util.BitSet;

import italiaken.fantasticbeasts.chizpurfle.L;
import italiaken.fantasticbeasts.chizpurfle.fuzz.generators.IValueGenerator;
import italiaken.fantasticbeasts.chizpurfle.fuzz.generators.ValueGeneratorException;
import italiaken.fantasticbeasts.chizpurfle.fuzz.generators.ValueGeneratorManager;

/**
 * Created by ken on 24/11/17 for fantastic_beasts
 */

public class ByteGenerator implements IValueGenerator<Byte> {

    private static Byte[] knownValues = new Byte[]{
            null, 0, 1, -1, Byte.MAX_VALUE, Byte.MIN_VALUE
    };

    @Override
    public Byte random() {
        byte[] bytes = new byte[1];
        ValueGeneratorManager.r.nextBytes(bytes);
        return bytes[0];
    }


    @Override
    public Byte mutate(Byte mutant) throws ValueGeneratorException {

        try {
            if (mutant == null)
                return random();

            switch (ValueGeneratorManager.chooseMutation()){
                case KNOWN:
                    return knownValues[ValueGeneratorManager.r.nextInt(knownValues.length)];
                case NEIGHBOR:
                    return (byte) (mutant ^ (1 << ValueGeneratorManager.r.nextInt(8)));
                case INVERSE:
                    return (byte) ~mutant;
            }
        } catch (Exception e) {
            throw new ValueGeneratorException("can't mutate "+mutant, e);
        }

        return null;
    }

    @Override
    public Byte crossover(Byte parent1, Byte parent2) throws ValueGeneratorException {
        try {
            byte mask;
            switch (ValueGeneratorManager.chooseCrossOver()){
                case SINGLE_POINT:
                    int point = ValueGeneratorManager.r.nextInt(Byte.SIZE);
                    mask = (byte) (0xff << point);
                    return (byte)((parent1 & mask) | (parent2 & ~mask));
                case TWO_POINT:
                    int point1 = ValueGeneratorManager.r.nextInt(Byte.SIZE);
                    int point2 = ValueGeneratorManager.r.nextInt(Byte.SIZE);
                    mask = (byte) ((0xff << point1)^(0xff << point2));
                    return (byte)((parent1 & mask) | (parent2 & ~mask));
                case UNIFORM:
                    mask = random();
                    return (byte)((parent1 & mask) | (parent2 & ~mask));
                case ARITHMETIC:
                    return (byte)(parent1^parent2);
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
