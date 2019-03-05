package italiaken.fantasticbeasts.chizpurfle.fuzz.generators.object;

import android.content.ComponentName;
import android.content.Intent;
import android.view.InputEvent;

import java.util.Set;

import italiaken.fantasticbeasts.chizpurfle.L;
import italiaken.fantasticbeasts.chizpurfle.fuzz.generators.IValueGenerator;
import italiaken.fantasticbeasts.chizpurfle.fuzz.generators.ValueGeneratorException;
import italiaken.fantasticbeasts.chizpurfle.fuzz.generators.primitive.BooleanGenerator;
import italiaken.fantasticbeasts.chizpurfle.fuzz.generators.primitive.ByteGenerator;
import italiaken.fantasticbeasts.chizpurfle.fuzz.generators.primitive.CharacterGenerator;
import italiaken.fantasticbeasts.chizpurfle.fuzz.generators.primitive.DoubleGenerator;
import italiaken.fantasticbeasts.chizpurfle.fuzz.generators.primitive.FloatGenerator;
import italiaken.fantasticbeasts.chizpurfle.fuzz.generators.primitive.IntegerGenerator;
import italiaken.fantasticbeasts.chizpurfle.fuzz.generators.primitive.LongGenerator;
import italiaken.fantasticbeasts.chizpurfle.fuzz.generators.primitive.ShortGenerator;

/**
 * Created by ken on 25/11/17 for fantastic_beasts
 */

public class ObjectGenerator implements IValueGenerator<Object> {

    private final IValueGenerator valueGenerator;

    public ObjectGenerator(Class parameterType) {

        if (parameterType.isPrimitive())
            throw new RuntimeException(parameterType.getName() + " is not an object!");

        if (parameterType.equals(Byte.class))
            valueGenerator = new ByteGenerator();
        else if (parameterType.equals(Short.class))
            valueGenerator = new ShortGenerator();
        else if (parameterType.equals(Integer.class))
            valueGenerator = new IntegerGenerator();
        else if (parameterType.equals(Long.class))
            valueGenerator = new LongGenerator();
        else if (parameterType.equals(Float.class))
            valueGenerator = new FloatGenerator();
        else if (parameterType.equals(Double.class))
            valueGenerator = new DoubleGenerator();
        else if (parameterType.equals(Boolean.class))
            valueGenerator = new BooleanGenerator();
        else if (parameterType.equals(Character.class))
            valueGenerator = new CharacterGenerator();
        else if (parameterType.equals(String.class))
            valueGenerator = new StringGenerator();
        else if (parameterType.equals(ComponentName.class))
            valueGenerator = new ComponentNameGenerator();
        else if (parameterType.equals(Intent.class)){
            valueGenerator = new IntentGenerator();
        } else{
            this.valueGenerator = new GenericObjectGenerator(parameterType);
        }

    }

    public ObjectGenerator(Class parameterType, Set<String> classSetForDeadLock) {
        if (parameterType.isPrimitive())
            throw new RuntimeException(parameterType.getName() + " is not an object!");

        if (parameterType.equals(Byte.class))
            valueGenerator = new ByteGenerator();
        else if (parameterType.equals(Short.class))
            valueGenerator = new ShortGenerator();
        else if (parameterType.equals(Integer.class))
            valueGenerator = new IntegerGenerator();
        else if (parameterType.equals(Long.class))
            valueGenerator = new LongGenerator();
        else if (parameterType.equals(Float.class))
            valueGenerator = new FloatGenerator();
        else if (parameterType.equals(Double.class))
            valueGenerator = new DoubleGenerator();
        else if (parameterType.equals(Boolean.class))
            valueGenerator = new BooleanGenerator();
        else if (parameterType.equals(Character.class))
            valueGenerator = new CharacterGenerator();
        else if (parameterType.equals(String.class))
            valueGenerator = new StringGenerator();
        else if (parameterType.equals(ComponentName.class))
            valueGenerator = new ComponentNameGenerator();
        else if (parameterType.equals(Intent.class)){
            valueGenerator = new IntentGenerator();
        } else{
            this.valueGenerator = new GenericObjectGenerator(parameterType, classSetForDeadLock);
        }

    }

    @Override
    public Object random() {
        return valueGenerator.random();
    }

    @Override
    public Object mutate(Object mutant) throws ValueGeneratorException {
        return valueGenerator.mutate(mutant);
    }

    @Override
    public Object crossover(Object parent1, Object parent2) throws ValueGeneratorException {
        return valueGenerator.crossover(parent1,parent2);
    }

}