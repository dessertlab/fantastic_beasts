package italiaken.fantasticbeasts.chizpurfle.fuzz.generators;

/**
 * Created by ken on 24/11/17 for fantastic_beasts
 */

public interface IValueGenerator<T> {

    public T random();

    public T mutate(T mutant) throws ValueGeneratorException;

    public T crossover(T parent1, T parent2) throws ValueGeneratorException;

}
