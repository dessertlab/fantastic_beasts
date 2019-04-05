package italiaken.fantasticbeasts.chizpurfle.infiltration;

import java.lang.reflect.Method;

import italiaken.fantasticbeasts.chizpurfle.configuration.ConfigurationManager;

/**
 * Created by ken on 24/11/17 for fantastic_beasts
 */

public class MethodCallerBuilderFuzzableMethodFilter implements MethodCallerBuilder.IMethodCallerBuilderFilter {
    @Override
    public boolean filter(Method m) {

        if(m.getParameterTypes().length == 0) {
		return false;
	}


	String targetMethod = ConfigurationManager.getMethodName();

	if(targetMethod != null && !m.getName().equals(targetMethod)) {
		return false;
	}

	return true;

    }
}
