package italiaken.fantasticbeasts.chizpurfle.fuzz.generators.object;

import android.content.ComponentName;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import italiaken.fantasticbeasts.chizpurfle.L;
import italiaken.fantasticbeasts.chizpurfle.fuzz.generators.IValueGenerator;
import italiaken.fantasticbeasts.chizpurfle.fuzz.generators.ParametersGenerator;
import italiaken.fantasticbeasts.chizpurfle.fuzz.generators.ValueGeneratorException;
import italiaken.fantasticbeasts.chizpurfle.fuzz.generators.ValueGeneratorManager;
import italiaken.fantasticbeasts.chizpurfle.fuzz.generators.primitive.CharacterGenerator;

import static italiaken.fantasticbeasts.chizpurfle.fuzz.generators.ValueGeneratorManager.r;

/**
 * Created by ken on 25/11/17 for fantastic_beasts
 */

public class GenericObjectGenerator implements IValueGenerator {

    private final Class klass;
    private Set<String> classSetForDeadLock;

    public GenericObjectGenerator(Class klass) {
        L.d("uses a genericObjectGenerator for "+ klass);
        this.klass = klass;
        this.classSetForDeadLock = new HashSet<>();
    }

    public GenericObjectGenerator(Class klass, Set<String> classSetForDeadLock) {
        L.d("uses a genericObjectGenerator for "+ klass);
        this.klass = klass;
        this.classSetForDeadLock = classSetForDeadLock;
    }

    @Override
    public Object random() {

        classSetForDeadLock.add(klass.getName());
        L.d(klass.getName()+" added to set "+classSetForDeadLock.toString());

        List<Constructor> constructors = Arrays.asList(klass.getConstructors());
        Collections.shuffle(constructors, r);

        Object result = null;

        for (Constructor init: constructors)
        {
            try {

                if (!Modifier.isPublic(init.getModifiers()))
                    continue;

                init.setAccessible(true);

                Class[] parameterTypes = init.getParameterTypes();

                boolean potentialDeadlock = false;
                for (Class parameterType: parameterTypes){
                    L.d(parameterType.getName()+" is init type");
                    if (classSetForDeadLock.contains(parameterType.getName())){
                        potentialDeadlock = true;
                        L.w("breaking from a potential deadlock");
                        break;
                    }
                }
                if (potentialDeadlock)
                    continue;

                StringBuilder msg = new StringBuilder("new instance of ");
                msg.append(klass.getName());
                msg.append("(");

                Object[] parameters = new ParametersGenerator(parameterTypes, classSetForDeadLock)
                        .random();
                for (int i = 0; i < parameterTypes.length; i++){
                    msg.append(parameters[i]);
                    msg.append(", ");
                }

                msg.setLength(msg.length()-", ".length());
                msg.append(")");

                result = init.newInstance(parameters);

                L.d(msg.toString());

            } catch (Exception | StackOverflowError ignored ) {}
        }

        if (result == null)
            L.d("null instance of "+ klass.getName());

        classSetForDeadLock.remove(klass.getName());
        return result;

    }

    @Override
    public Object mutate(Object mutant) throws ValueGeneratorException {

        if (mutant == null)
            return random();


        boolean mutated = false;

        for(Method method : klass.getMethods()){

            try {
                if(method.getName().startsWith("set")){

                    method.setAccessible(true);
                    Class[] ptt = method.getParameterTypes();
                    Object[] pp = new ParametersGenerator(ptt).random();

                    method.invoke(mutant, pp);

                    StringBuilder msg = new StringBuilder("called setter of ");
                    msg.append(klass.getName());
                    msg.append(" - ");
                    msg.append(method.getName());
                    msg.append("(");
                    for (int i = 0; i < ptt.length; i++){
                        msg.append(pp[i]);
                        msg.append(", ");
                    }
                    msg.setLength(msg.length()-", ".length());
                    msg.append(")");
                    L.i(msg.toString());

                    mutated = true;
                }
            } catch (Exception ignored) {}

        }


        if (!mutated) {
            L.w("can't mutate " + mutant);
        }

        return mutant;
    }

    @Override
    public Object crossover(Object parent1, Object parent2) throws ValueGeneratorException {
        try {
            if (ValueGeneratorManager.r.nextBoolean()){
                return parent1;
            }else {
                return parent2;
            }
        }catch (Exception e){
            throw new ValueGeneratorException("can't crossover "+parent1+" and "+parent2, e);
        }
    }
}
