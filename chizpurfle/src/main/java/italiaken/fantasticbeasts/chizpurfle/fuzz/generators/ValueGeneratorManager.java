package italiaken.fantasticbeasts.chizpurfle.fuzz.generators;

import java.util.Random;

import italiaken.fantasticbeasts.chizpurfle.configuration.ConfigurationManager;

/**
 * Created by ken on 24/11/17 for fantastic_beasts
 */

public class ValueGeneratorManager {

    public static final Random r;
    public static final int MAX_DELTA = 10;

    static
    {
        r = new Random(ConfigurationManager.getRandomSeed());
    }

    public static MutationType chooseMutation() {

        int probability = r.nextInt(100);

        if (probability < 5)
            return MutationType.KNOWN;
        else if (probability < 85)
            return MutationType.NEIGHBOR;
        else
            return MutationType.INVERSE;

    }

    public static CrossOverType chooseCrossOver() {

        CrossOverType[] types = CrossOverType.values();

        return types[r.nextInt(types.length)];

    }

    public enum MutationType {
        KNOWN,
        NEIGHBOR,
        INVERSE
    }

    public enum CrossOverType {
        SINGLE_POINT,
        TWO_POINT,
        UNIFORM,
        ARITHMETIC
    }

}
