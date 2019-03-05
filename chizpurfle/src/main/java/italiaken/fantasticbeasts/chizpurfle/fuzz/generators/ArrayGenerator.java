package italiaken.fantasticbeasts.chizpurfle.fuzz.generators;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Set;

import italiaken.fantasticbeasts.chizpurfle.configuration.ConfigurationManager;
import italiaken.fantasticbeasts.chizpurfle.fuzz.generators.object.ObjectGenerator;
import italiaken.fantasticbeasts.chizpurfle.fuzz.generators.primitive.CharacterGenerator;
import italiaken.fantasticbeasts.chizpurfle.fuzz.generators.primitive.IntegerGenerator;

import italiaken.fantasticbeasts.chizpurfle.L;
import italiaken.fantasticbeasts.chizpurfle.fuzz.generators.primitive.PrimitiveGenerator;
import italiaken.fantasticbeasts.chizpurfle.infiltration.MethodCaller;
import italiaken.fantasticbeasts.chizpurfle.infiltration.MethodCallerBuilder;

/**
 * Created by ken on 25/11/17 for fantastic_beasts
 */

public class ArrayGenerator implements IValueGenerator<Object>{

    private final Class componentType;
    private final IValueGenerator valueGenerator;

    public ArrayGenerator(Class componentType) {

        if (componentType.isArray()){
            L.w("Array generator for componentType "
                    + componentType.getName()
                    + " that is a component type in turn");
        }
        this.componentType = componentType;
        if (componentType.isPrimitive())
            this.valueGenerator = new PrimitiveGenerator(componentType);
        else if (componentType.isArray())
            this.valueGenerator = new ArrayGenerator(componentType.getComponentType());
        else
            this.valueGenerator = new ObjectGenerator(componentType);
    }

    public ArrayGenerator(Class componentType, Set<String> classSetForDeadLock) {
        if (componentType.isArray()){
            L.w("Array generator for componentType "
                    + componentType.getName()
                    + " that is a component type in turn");
        }
        this.componentType = componentType;
        if (componentType.isPrimitive())
            this.valueGenerator = new PrimitiveGenerator(componentType);
        else if (componentType.isArray())
            this.valueGenerator = new ArrayGenerator(componentType.getComponentType());
        else
            this.valueGenerator = new ObjectGenerator(componentType, classSetForDeadLock);

    }

    @Override
    public Object random() {

        int lenght = ValueGeneratorManager.r.nextInt(ConfigurationManager.getArrayMaxSize());

        Object o = Array.newInstance(componentType, lenght);

        for (int i = 0; i< lenght; i++){
            Array.set(o, i, valueGenerator.random());
        }

        return o;
    }

    @Override
    public Object mutate(Object mutant) throws ValueGeneratorException {

        try {

            Object result;
            if (mutant == null ||  Array.getLength(mutant) == 0)
                return random();
            int lenght = Array.getLength(mutant);

            switch (ValueGeneratorManager.chooseMutation()){
                case KNOWN:
                    if (ValueGeneratorManager.r.nextBoolean())
                        return null;
                    else
                        return Array.newInstance(componentType, 0);
                case NEIGHBOR:
                    int index = ValueGeneratorManager.r.nextInt(lenght);
                    result = Array.newInstance(componentType, lenght);

                    for (int i = 0; i< lenght; i++){
                        if (i != index)
                            Array.set(result, i, Array.get(mutant, i));
                        else
                            Array.set(result, i, valueGenerator.mutate(Array.get(mutant, i)));
                    }

                    return result;
                case INVERSE:
                    if (ValueGeneratorManager.r.nextBoolean()){
                        int increase = ValueGeneratorManager.r.nextInt(lenght) + 1;
                        increase = Math.min(increase, ConfigurationManager.getArrayMaxSize() - lenght);
                        result = Array.newInstance(componentType, lenght + increase);
                        for (int i = 0; i< Array.getLength(result); i++){
                            if (i < Array.getLength(mutant))
                                Array.set(result, i, Array.get(mutant, i));
                            else
                                Array.set(result, i, valueGenerator.random());
                        }
                    }else {
                        int a = ValueGeneratorManager.r.nextInt(lenght);
                        int b = ValueGeneratorManager.r.nextInt(lenght);

                        result = Array.newInstance(componentType, Math.max(a, b) - Math.min(a, b));

                        for (int i = Math.min(a, b); i < Math.max(a, b); i++){
                            Array.set(result, i - Math.min(a, b), Array.get(mutant, i));
                        }

                        return result;
                    }
            }
        } catch (Exception e) {
            throw new ValueGeneratorException("can't mutate " + mutant, e);
        }

        return null;
    }

    @Override
    public Object crossover(Object parent1, Object parent2) throws ValueGeneratorException {
        try {
            Object longest = (Array.getLength(parent1) > Array.getLength(parent2))? parent1:parent2;
            Object shortest = (longest == parent1)?parent2:parent1;
            int longestLenght = Array.getLength(longest);
            int shortestLenght = Array.getLength(shortest);
            Object result = null;

            switch (ValueGeneratorManager.chooseCrossOver()){
                case SINGLE_POINT:
                    int point = ValueGeneratorManager.r.nextInt(shortestLenght);
                    result = Array.newInstance(componentType, longestLenght);
                    for (int i = 0; i < point; i++){
                        Array.set(result, i, Array.get(shortest, i));
                    }
                    for (int i = point; i< longestLenght; i++){
                        Array.set(result, i, Array.get(longest, i));
                    }
                    return result;
                case TWO_POINT:
                    int point1 = ValueGeneratorManager.r.nextInt(shortestLenght);
                    int point2 = ValueGeneratorManager.r.nextInt(shortestLenght);

                    result = Array.newInstance(componentType, longestLenght);
                    for (int i = 0; i < point1; i++){
                        Array.set(result, i, Array.get(longest, i));
                    }
                    for (int i = point1; i< point2; i++){
                        Array.set(result, i, Array.get(shortest, i));
                    }
                    for (int i = point2; i< longestLenght; i++){
                        Array.set(result, i, Array.get(longest, i));
                    }
                    return result;
                case UNIFORM:
                    result = Array.newInstance(componentType, longestLenght);
                    for (int i = 0; i < shortestLenght; i++){
                        if (ValueGeneratorManager.r.nextBoolean()) {
                            Array.set(result, i, Array.get(shortest, i));
                        }else {
                            Array.set(result, i, Array.get(longest, i));
                        }
                    }

                    for (int i = shortestLenght; i<longestLenght; i++){
                        Array.set(result, i, Array.get(longest, i));
                    }

                    return result;
                case ARITHMETIC:
                    int resultLenght = Math.min((longestLenght+shortestLenght)/2,
                            ConfigurationManager.getArrayMaxSize());

                    result = Array.newInstance(componentType, resultLenght);
                    for (int i = 0; i < resultLenght; i++){
                        if (i < shortestLenght) {
                            Array.set(result, i, Array.get(shortest, i));
                        }else {
                            Array.set(result, i, Array.get(longest, i));
                        }
                    }
                    return result;
            }

            return result;

        }catch (Exception e){
            if (parent1 != null && parent2 == null)
                return parent1;
            if (parent2 != null && parent1 == null)
                return parent2;
            if (parent1 == null && parent2 == null)
                return null;
            int lenght1 = Array.getLength(parent1);
            int lenght2 = Array.getLength(parent2);
            if (lenght1 == 0)
                return parent2;
            if (lenght2 == 0)
                return parent1;


            throw new ValueGeneratorException("can't crossover "+parent1+" and "+parent2, e);
        }

    }
}
