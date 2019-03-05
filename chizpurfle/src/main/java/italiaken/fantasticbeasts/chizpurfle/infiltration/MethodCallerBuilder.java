package italiaken.fantasticbeasts.chizpurfle.infiltration;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import italiaken.fantasticbeasts.chizpurfle.L;

public class MethodCallerBuilder {

    private Object calledObject;
    private Method method;

    public MethodCallerBuilder setCalledObject(Object calledObject) {
        this.calledObject = calledObject;
        return this;
    }

    public MethodCallerBuilder setMethod(Method method) throws MethodCallerException {

        if (this.calledObject == null)
            throw new MethodCallerException("MethodCallerBuinder needs calledObject first");

        if (!Arrays.asList(this.calledObject.getClass().getDeclaredMethods()).contains(method))
            throw new MethodCallerException("MethodCallerBuinder uses a calledObject without specified method");

        this.method = method;
        return this;
    }

    public MethodCallerBuilder setMethodByName(String name) throws MethodCallerException {

        List<Method> results = new ArrayList<>();

        for (Method m : this.calledObject.getClass().getDeclaredMethods())
        {
            if (m.getName().equals(name))
                results.add(m);
        }

        if (results.isEmpty())
        {
            String msg = calledObject.getClass().getName() + "does not have a method with name "+
                    name;
            throw new MethodCallerException(msg);
        }
        else if (results.size() > 1)
        {
            String msg = calledObject.getClass().getName() + "has more than one method with name="+
                    name +"(using the first one)";
            L.w(msg);
        }

        this.method = results.get(0);

        return this;
    }

    public MethodCaller createMethodCaller() throws MethodCallerException {

        if (this.calledObject == null || this.method == null)
            throw new MethodCallerException("MethodCallerBuinder can't build");

        StringBuilder stringBuilder =
                new StringBuilder("building a MethodCaller on an instance of ");
        stringBuilder.append(this.calledObject.getClass().getName());
        stringBuilder.append(" - ");

        stringBuilder.append(this.method.getReturnType().getName());
        stringBuilder.append(" ");
        stringBuilder.append(this.method.getName());
        stringBuilder.append("( ");
        for ( Class c : this.method.getParameterTypes()){
            stringBuilder.append(c.getName());
            stringBuilder.append(" ");
        }
        stringBuilder.append(")");

        L.d(stringBuilder.toString());

        return new MethodCaller(calledObject, method);
    }

    public List<MethodCaller> createMethodCallers() throws MethodCallerException {
        return createMethodCallers(new IMethodCallerBuilderFilter(){

            @Override
            public boolean filter(Method m) {
                return true;
            }
        });

    }

    public List<MethodCaller> createMethodCallers(IMethodCallerBuilderFilter filter) throws MethodCallerException {

        List<MethodCaller> result = new ArrayList<>();
        MethodCallerBuilder inner = new MethodCallerBuilder().setCalledObject(this.calledObject);

        for (Method m : this.calledObject.getClass().getDeclaredMethods())
        {
            if (filter.filter(m))
                result.add(inner.setMethod(m).createMethodCaller());
        }

        return result;

    }

    // TODO SUBSTITUTE WITH PREDICATE
    public interface IMethodCallerBuilderFilter {

        boolean filter(Method m);

    }
}