package italiaken.fantasticbeasts.chizpurfle.fuzz.generators;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import italiaken.fantasticbeasts.chizpurfle.L;
import italiaken.fantasticbeasts.chizpurfle.configuration.ConfigurationManager;
import italiaken.fantasticbeasts.chizpurfle.fuzz.generators.object.ObjectGenerator;
import italiaken.fantasticbeasts.chizpurfle.fuzz.generators.primitive.PrimitiveGenerator;

/**
 * Created by ken on 24/11/17 for fantastic_beasts
 */

public class ParametersGenerator implements IValueGenerator<Object[]> {


    private final Class[] parameterTypes;
    private IValueGenerator[] valueGenerators;

    public ParametersGenerator(Class...parameterTypes) {
        this.parameterTypes = parameterTypes;
        if (parameterTypes == null || parameterTypes.length == 0){
            throw new RuntimeException("parameterTypes is null or has no elements");
        }

        StringBuilder stringBuilder = new StringBuilder("new parameters generator: ");
        for (Class klass : parameterTypes){
            stringBuilder.append(klass.getName());
            stringBuilder.append(" ");
        }
        L.d(stringBuilder.toString());

        valueGenerators = new IValueGenerator[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++)
        {
            if (parameterTypes[i].isPrimitive())
                valueGenerators[i] = new PrimitiveGenerator(parameterTypes[i]);
            else if (parameterTypes[i].isArray())
                valueGenerators[i] = new ArrayGenerator(parameterTypes[i].getComponentType());
            else
                valueGenerators[i] = new ObjectGenerator(parameterTypes[i]);
        }


    }

    public ParametersGenerator(Class[] parameterTypes, Set<String> classSetForDeadLock) {
        this.parameterTypes = parameterTypes;
        if (parameterTypes == null || parameterTypes.length == 0){
            throw new RuntimeException("parameterTypes is null or has no elements");
        }

        StringBuilder stringBuilder = new StringBuilder("new parameters generator: ");
        for (Class klass : parameterTypes){
            stringBuilder.append(klass.getName());
            stringBuilder.append(" ");
        }
        L.d(stringBuilder.toString());

        valueGenerators = new IValueGenerator[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++)
        {
            if (parameterTypes[i].isPrimitive())
                valueGenerators[i] = new PrimitiveGenerator(parameterTypes[i]);
            else if (parameterTypes[i].isArray())
                valueGenerators[i] = new ArrayGenerator(parameterTypes[i].getComponentType(), classSetForDeadLock);
            else
                valueGenerators[i] = new ObjectGenerator(parameterTypes[i], classSetForDeadLock);
        }

    }

    @Override
    public Object[] random() {

        Object[] parameters = new Object[parameterTypes.length];

        for (int i = 0; i < parameterTypes.length; i++)
        {
            parameters[i] = valueGenerators[i].random();
        }

        return parameters;
    }

    @Override
    public Object[] mutate(Object[] mutant) throws ValueGeneratorException {

        try {

            if (mutant == null)
                return random();

            Object[] result = new Object[parameterTypes.length];
            int index = -1;

            switch (ValueGeneratorManager.chooseMutation()){
                case KNOWN:

                    //TODO
                    for (int i = 0; i < parameterTypes.length; i++) {
                        if (parameterTypes[i].isPrimitive()){
                            if (parameterTypes[i].equals(Boolean.TYPE))
                                result[i] = false;
                            else
                                result[i] = 0;
                        }
                        else
                            result[i] = null;
                    }
                    return result;

                case NEIGHBOR:
                    index = ValueGeneratorManager.r.nextInt(parameterTypes.length);
                case INVERSE:

                    for (int i = 0; i< parameterTypes.length; i++){
                        if (index == -1 || index ==i)
                            result[i] = valueGenerators[i].mutate(mutant[i]);
                        else
                            result[i] = mutant[i];
                    }
                    return result;

            }
        } catch (Exception e) {
            throw new ValueGeneratorException("can't mutate " + mutant, e);
        }

        return random();
    }

    @Override
    public Object[] crossover(Object[] parent1, Object[] parent2) throws ValueGeneratorException {

        try {

            List<Object> result = new ArrayList<>(parameterTypes.length);

            switch (ValueGeneratorManager.chooseCrossOver()){
                case SINGLE_POINT:
                    int point = ValueGeneratorManager.r.nextInt(parameterTypes.length);

                    for (int i = 0; i<point; i++){
                        result.add(i, parent1[i]);
                    }
                    result.add(point, valueGenerators[point].crossover(parent1[point], parent2[point]));
                    for (int i = point+1; i<parameterTypes.length; i++){
                        result.add(i, parent2[i]);
                    }

                    return result.toArray();
                case TWO_POINT:
                    int point1 = ValueGeneratorManager.r.nextInt(parameterTypes.length);
                    int point2 = ValueGeneratorManager.r.nextInt(parameterTypes.length);

                    for (int i = 0; i<point1; i++){
                        result.add(i, parent1[i]);
                    }
                    result.add(point1, valueGenerators[point1].crossover(parent1[point1], parent2[point1]));
                    for (int i = point1+1; i<point2; i++){
                        result.add(i, parent2[i]);
                    }
                    result.add(point2, valueGenerators[point2].crossover(parent2[point2], parent1[point2]));
                    for (int i = point2+1; i<parameterTypes.length; i++){
                        result.add(i, parent1[i]);
                    }

                    return result.toArray();
                case UNIFORM:
                    for (int i = 0; i<parameterTypes.length; i++){
                        result.add(i, valueGenerators[i].crossover(parent1[i], parent2[i]));
                    }
                    return result.toArray();
                case ARITHMETIC:
                    for (int i = 0; i<parameterTypes.length; i++){
                        if (ValueGeneratorManager.r.nextBoolean()) {
                            result.add(i, parent1[i]);
                        } else {
                            result.add(i, parent2[i]);
                        }
                    }
                    return result.toArray();
            }
        }catch (Exception e){
            if (parent1 != null && parent2 == null)
                return parent1;
            if (parent2 != null && parent1 == null)
                return parent2;
            if (parent1 == null && parent2 == null)
                return random();
            throw new ValueGeneratorException("can't crossover "+parent1+" and "+parent2, e);
        }

        return random();
    }

}
