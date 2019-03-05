package italiaken.fantasticbeasts.chizpurfle.infiltration;

import java.lang.reflect.Method;

/**
 * Created by ken on 24/11/17 for fantastic_beasts
 */

public class MethodCallerBuilderFuzzableMethodFilter implements MethodCallerBuilder.IMethodCallerBuilderFilter {
    @Override
    public boolean filter(Method m) {

        return m.getParameterTypes().length != 0;

    }
}
