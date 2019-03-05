package italiaken.fantasticbeasts.chizpurfle.fuzz.generators.primitive;

import italiaken.fantasticbeasts.chizpurfle.L;
import italiaken.fantasticbeasts.chizpurfle.fuzz.generators.IValueGenerator;
import italiaken.fantasticbeasts.chizpurfle.fuzz.generators.ValueGeneratorException;

/**
 * Created by ken on 24/11/17 for fantastic_beasts
 */

public class PrimitiveGenerator implements IValueGenerator<Object>{


    private final Class parameterType;
    private final IValueGenerator valueGenerator;

    public PrimitiveGenerator(Class parameterType) {

        if (!parameterType.isPrimitive())
            throw new RuntimeException(parameterType.getName() + " is not primitive!");
        this.parameterType = parameterType;

        if (parameterType.equals(Byte.TYPE))
            this.valueGenerator = new ByteGenerator();
        else if (parameterType.equals(Short.TYPE))
            this.valueGenerator = new ShortGenerator();
        else if (parameterType.equals(Integer.TYPE))
            this.valueGenerator = new IntegerGenerator();
        else if (parameterType.equals(Long.TYPE))
            this.valueGenerator = new LongGenerator();
        else if (parameterType.equals(Float.TYPE))
            this.valueGenerator = new FloatGenerator();
        else if (parameterType.equals(Double.TYPE))
            this.valueGenerator = new DoubleGenerator();
        else if (parameterType.equals(Boolean.TYPE))
            this.valueGenerator = new BooleanGenerator();
        else if (parameterType.equals(Character.TYPE))
            this.valueGenerator = new CharacterGenerator();
        else
            throw new RuntimeException("undefined behaviour for primitive " + parameterType.getName());

    }


    @Override
    public Object random() {

        Object result = null;

        while (result == null){
            result = valueGenerator.random();
        }

        return result;
    }

    @Override
    public Object mutate(Object mutant) throws ValueGeneratorException {

        Object result = null;

        while (result == null){
            result = valueGenerator.mutate(mutant);
        }

        return result;

    }

    @Override
    public Object crossover(Object parent1, Object parent2) throws ValueGeneratorException {

        while (parent1 == null){
            parent1 = valueGenerator.random();
        }
        while (parent2 == null){
            parent2 = valueGenerator.random();
        }

        return valueGenerator.crossover(parent1, parent2);

    }
}
