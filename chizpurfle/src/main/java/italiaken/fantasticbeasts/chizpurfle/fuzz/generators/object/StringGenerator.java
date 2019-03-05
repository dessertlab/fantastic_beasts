package italiaken.fantasticbeasts.chizpurfle.fuzz.generators.object;

import java.lang.reflect.Array;
import java.util.Random;

import italiaken.fantasticbeasts.chizpurfle.configuration.ConfigurationManager;
import italiaken.fantasticbeasts.chizpurfle.fuzz.generators.IValueGenerator;
import italiaken.fantasticbeasts.chizpurfle.fuzz.generators.ValueGeneratorException;
import italiaken.fantasticbeasts.chizpurfle.fuzz.generators.ValueGeneratorManager;
import italiaken.fantasticbeasts.chizpurfle.fuzz.generators.primitive.CharacterGenerator;
import italiaken.fantasticbeasts.chizpurfle.fuzz.generators.primitive.IntegerGenerator;
import italiaken.fantasticbeasts.chizpurfle.fuzz.generators.primitive.PrimitiveGenerator;

/**
 * Created by ken on 25/11/17 for fantastic_beasts
 */

class StringGenerator implements IValueGenerator<String> {

    private static String[] knownValues = new String[]{
            null, "", "\0", "A Chizpurfle is a type of very small parasite. Crab-like in " +
            "appearance, they are up to a twentieth of an inch with fangs. Magic attracts " +
            "them and they are commonly found in the fur and feathers of Crups and Augureys. " +
            "They attack magical objects like wands and cauldrons, gnawing through to the " +
            "magical core or gorging on the last remnants of potions. In the absence of " +
            "magic, Chizpurfles attack Muggle items powered by electricity. This explains " +
            "the sudden failure of various new electrical goods."
    };

    @Override
    public String random() {

        int lenght = ValueGeneratorManager.r.nextInt(ConfigurationManager.getStringMaxSize());
        CharacterGenerator charGenerator = new CharacterGenerator();

        StringBuilder stringBuilder = new StringBuilder(lenght);
        for (int i = 0; i<lenght; i++){
            stringBuilder.append(charGenerator.randomValid());
        }

        return stringBuilder.toString();

    }

    @Override
    public String mutate(String mutant) throws ValueGeneratorException {

        try {

            if (mutant == null || mutant.length() == 0)
                return random();

            switch (ValueGeneratorManager.chooseMutation()){
                case KNOWN:
                    return knownValues[ValueGeneratorManager.r.nextInt(knownValues.length)];
                case NEIGHBOR:
                    int index = ValueGeneratorManager.r.nextInt(mutant.length());
                    return mutant.substring(0, index) +
                            new CharacterGenerator().mutate(mutant.charAt(index)) +
                            mutant.substring(index + 1);
                case INVERSE:
                    String result = mutant;

                    if (ValueGeneratorManager.r.nextBoolean()){
                        result = mutant.concat(random());
                        if (result.length() <= ConfigurationManager.getStringMaxSize()){
                            return result;
                        }
                    }

                    int a = ValueGeneratorManager.r.nextInt(result.length());
                    int b = ValueGeneratorManager.r.nextInt(result.length());
                    return result.substring(Math.min(a, b), Math.max(a, b));

            }
        } catch (Exception e) {
            throw new ValueGeneratorException("can't mutate " + mutant, e);
        }

        return null;
    }

    @Override
    public String crossover(String parent1, String parent2) throws ValueGeneratorException {
        try {
            int longestLenght = Math.max(parent1.length(), parent2.length());
            int shortestLenght = Math.min(parent1.length(), parent2.length());
            switch (ValueGeneratorManager.chooseCrossOver()){
                case SINGLE_POINT:
                    int point = ValueGeneratorManager.r.nextInt(shortestLenght);
                    return parent1.substring(0, point) +
                            parent2.substring(point);
                case TWO_POINT:
                    int point1 = ValueGeneratorManager.r.nextInt(shortestLenght);
                    int point2 = ValueGeneratorManager.r.nextInt(shortestLenght);
                    return parent1.substring(0, Math.min(point1, point2)) +
                            parent2.substring(Math.min(point1, point2), Math.max(point1, point2)) +
                            parent1.substring(Math.max(point1,point2));
                case UNIFORM:
                    StringBuilder stringBuilder = new StringBuilder();
                    for (int i = 0; i<shortestLenght; i++){
                        if (ValueGeneratorManager.r.nextBoolean()){
                            stringBuilder.append(parent1.charAt(i));
                        }else {
                            stringBuilder.append(parent2.charAt(i));
                        }
                    }
                    if (parent1.length() > parent2.length()){
                        stringBuilder.append(parent1.substring(parent2.length()));
                    }else{
                        stringBuilder.append(parent2.substring(parent1.length()));
                    }

                    return stringBuilder.toString();
                case ARITHMETIC:
                    String result = (parent1+parent2).substring((longestLenght+shortestLenght)/2);
                    if (result.length() > ConfigurationManager.getStringMaxSize()){
                        result = result.substring(0, ConfigurationManager.getStringMaxSize());
                    }
                    return result;
            }
        }catch (Exception e){
            if (parent1 != null && parent2 == null)
                return parent1;
            if (parent2 != null && parent1 == null)
                return parent2;
            if (parent1 == null && parent2 == null)
                return null;
            int lenght1 = parent1.length();
            int lenght2 = parent2.length();
            if (lenght1 == 0)
                return parent2;
            if (lenght2 == 0)
                return parent1;

            throw new ValueGeneratorException("can't crossover "+parent1+" and "+parent2, e);
        }

        return null;
    }

}
