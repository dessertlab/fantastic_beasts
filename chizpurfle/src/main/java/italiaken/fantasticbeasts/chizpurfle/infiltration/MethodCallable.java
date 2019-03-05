package italiaken.fantasticbeasts.chizpurfle.infiltration;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;

/**
 * Created by ken on 02/12/17 for fantastic_beasts
 */

public class MethodCallable implements Callable<Object> {

    private final Method method;
    private final Object calledObject;
    private final Object[] effectiveArguments;

    public MethodCallable(Method method, Object calledObject, Object[] effectiveArguments) {
        this.method = method;
        this.calledObject = calledObject;
        this.effectiveArguments = effectiveArguments;
    }

    @Override
    public Object call() throws Exception {
        return method.invoke(calledObject, effectiveArguments);
    }
}
