package italiaken.fantasticbeasts.chizpurfle.fuzz.generators.primitive;

import italiaken.fantasticbeasts.chizpurfle.fuzz.generators.IValueGenerator;
import italiaken.fantasticbeasts.chizpurfle.fuzz.generators.ValueGeneratorException;
import italiaken.fantasticbeasts.chizpurfle.fuzz.generators.ValueGeneratorManager;

/**
 * Created by ken on 25/11/17 for fantastic_beasts
 */

public class BooleanGenerator implements IValueGenerator<Boolean>{

    private static Boolean[] knownValues = new Boolean[]{
            null, Boolean.TRUE, Boolean.FALSE
    };


    @Override
    public Boolean random() {
        return ValueGeneratorManager.r.nextBoolean();
    }

    @Override
    public Boolean mutate(Boolean mutant) throws ValueGeneratorException{

        try {
            if (mutant == null)
                return random();

            switch (ValueGeneratorManager.chooseMutation()){
                case KNOWN:
                    return knownValues[ValueGeneratorManager.r.nextInt(knownValues.length)];
                case NEIGHBOR:
                case INVERSE:
                    return !mutant;
            }
        } catch (Exception e) {
            throw new ValueGeneratorException("can't mutate "+mutant, e);
        }

        return null;
    }

    @Override
    public Boolean crossover(Boolean parent1, Boolean parent2) throws ValueGeneratorException{
        try{

            switch (ValueGeneratorManager.chooseCrossOver()){
                case SINGLE_POINT:
                case TWO_POINT:
                case UNIFORM:
                    return parent1^parent2;
                case ARITHMETIC:
                    return parent1&parent2;
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
