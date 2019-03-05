package italiaken.fantasticbeasts.chizpurfle.fuzz.generators.primitive;

import italiaken.fantasticbeasts.chizpurfle.fuzz.generators.IValueGenerator;
import italiaken.fantasticbeasts.chizpurfle.fuzz.generators.ValueGeneratorException;
import italiaken.fantasticbeasts.chizpurfle.fuzz.generators.ValueGeneratorManager;

/**
 * Created by ken on 25/11/17 for fantastic_beasts
 */

public class CharacterGenerator implements IValueGenerator<Character>{

    private static Character[] knownValues = new Character[]{
            null, 0, Character.MAX_HIGH_SURROGATE, Character.MAX_LOW_SURROGATE, Character.MAX_SURROGATE,
            Character.MAX_VALUE, Character.MIN_HIGH_SURROGATE, Character.MIN_LOW_SURROGATE,
            Character.MIN_SURROGATE, Character.MIN_VALUE, '!', '\\', '|', '"', '£', '$', '%', '&',
            '/', '(', ')', '=', '?', '\'', '^', '*', '+', '@', '#', '.', ',', ';', '-', '_'
    };

    private final static String validCharacters =
            "1234567890qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM ,.-;:_!\"£$%&/()=?''";

    @Override
    public Character random() {
        return (char) ValueGeneratorManager.r.nextInt(2^ Character.SIZE);
    }

    public Character randomValid() {
        return validCharacters.charAt(ValueGeneratorManager.r.nextInt(validCharacters.length()));
    }

    @Override
    public Character mutate(Character mutant) throws ValueGeneratorException {

        try {
            if (mutant == null)
                return random();

            switch (ValueGeneratorManager.chooseMutation()){
                case KNOWN:
                    return knownValues[ValueGeneratorManager.r.nextInt(knownValues.length)];
                case NEIGHBOR:
                    return (char) (mutant +
                            ValueGeneratorManager.r.nextInt(ValueGeneratorManager.MAX_DELTA*2) -
                            ValueGeneratorManager.MAX_DELTA);
                case INVERSE:
                    return (char) ~mutant;
            }
        } catch (Exception e) {
            throw new ValueGeneratorException("can't mutate " + mutant, e);
        }

        return null;
    }

    @Override
    public Character crossover(Character parent1, Character parent2) throws ValueGeneratorException {
        try {
            byte mask;
            switch (ValueGeneratorManager.chooseCrossOver()){
                case SINGLE_POINT:
                    int point = ValueGeneratorManager.r.nextInt(8);
                    mask = (byte) (0xff << point);
                    return (char)((parent1 & mask) | (parent2 & ~mask));
                case TWO_POINT:
                    int point1 = ValueGeneratorManager.r.nextInt(8);
                    int point2 = ValueGeneratorManager.r.nextInt(8);
                    mask = (byte) ((0xff << point1)^(0xff << point2));
                    return (char)((parent1 & mask) | (parent2 & ~mask));
                case UNIFORM:
                    mask = (byte) random().charValue();
                    return (char)((parent1 & mask) | (parent2 & ~mask));
                case ARITHMETIC:
                    return (char)(parent1^parent2);
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
