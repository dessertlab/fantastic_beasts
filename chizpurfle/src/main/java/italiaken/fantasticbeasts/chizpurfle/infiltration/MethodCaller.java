package italiaken.fantasticbeasts.chizpurfle.infiltration;

import java.lang.reflect.Method;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import italiaken.fantasticbeasts.chizpurfle.L;
import italiaken.fantasticbeasts.chizpurfle.configuration.ConfigurationManager;

/**
 * Created by ken on 24/11/17 for fantastic_beasts
 */

public class MethodCaller {

    private final ExecutorService executor;
    private Object calledObject;
    private Method method;

    MethodCaller(Object calledObject, Method method) {
        this.calledObject = calledObject;
        this.method = method;

        executor = Executors.newFixedThreadPool(1);

    }

    public Class[] getParameterTypes(){
        return this.method.getParameterTypes();
    }

    public Object callWithTimeout(long timeoutInSeconds, Object...effectiveArguments)throws MethodCallerException {

        if (effectiveArguments == null && method.getParameterTypes().length != 0){
            effectiveArguments = new Object[]{null};
        }

        Future<Object> future = executor
                .submit(new MethodCallable(method, calledObject, effectiveArguments));

        try {
            L.d("invoking method " + method.getName());
            Object result = future.get(timeoutInSeconds,
                    TimeUnit.SECONDS);
            L.d("returned from method "+method.getName());
            return result;
        } catch (TimeoutException | InterruptedException | ExecutionException e) {
            future.cancel(true);
            L.d("cancelling method "+method.getName());
            throw new MethodCallerException(e);
        }

    }

    public Object call(Object...effectiveArguments)throws MethodCallerException {

        return callWithTimeout(ConfigurationManager.getCallTimeoutInSeconds(), effectiveArguments);

    }


    public Object callNoParameters()throws MethodCallerException {

        Future<Object> future = executor
                .submit(new MethodCallable(method, calledObject, new Object[]{}));

        try {
            L.d("invoking method " + method.getName());
            Object result = future.get(ConfigurationManager.getCallTimeoutInSeconds(),
                    TimeUnit.SECONDS);
            L.d("returned from method "+method.getName());
            return result;
        } catch (TimeoutException | InterruptedException | ExecutionException e) {
            future.cancel(true);
            L.d("cancelling method "+method.getName());
            throw new MethodCallerException(e);
        }

    }

    public void setCalledObject(Object calledObject) {
        this.calledObject = calledObject;
    }

    public String getMethodName(){
        return method.getName();
    }
}
