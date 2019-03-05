package italiaken.fantasticbeasts.chizpurfle.instrumentation.trace;

/**
 * Created by ken on 24/11/17 for fantastic_beasts
 */

public class Branch implements ICounterTrace{

    private Long id;
    private Long count;

    public Branch(String k, long aLong) {

        id = Long.parseLong(k);
        count = aLong;

    }

    public Branch(long id, long count) {

        this.id = id;
        this.count = count;

    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public Long getCount() {
        return count;
    }

    public Branch incrementCountBy(long aLong) {

        this.count += aLong;

        return this;
    }

}
