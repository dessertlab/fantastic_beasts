package italiaken.fantasticbeasts.chizpurfle.instrumentation.trace;

/**
 * Created by ken on 24/11/17 for fantastic_beasts
 */

public class Block implements ICounterTrace{

    private Long id;
    private Long count;

    public Block(String k, long aLong) {

        id = Long.parseLong(k);
        count = aLong;

    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public Long getCount() {
        return count;
    }

}
