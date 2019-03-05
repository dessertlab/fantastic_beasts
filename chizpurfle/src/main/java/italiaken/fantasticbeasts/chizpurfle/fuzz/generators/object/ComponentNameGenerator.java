package italiaken.fantasticbeasts.chizpurfle.fuzz.generators.object;

import android.content.ComponentName;
import android.content.pm.ComponentInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;

import italiaken.fantasticbeasts.chizpurfle.fuzz.generators.IValueGenerator;
import italiaken.fantasticbeasts.chizpurfle.fuzz.generators.ValueGeneratorException;
import italiaken.fantasticbeasts.chizpurfle.fuzz.generators.ValueGeneratorManager;
import italiaken.fantasticbeasts.chizpurfle.infiltration.InfiltrationException;
import italiaken.fantasticbeasts.chizpurfle.infiltration.InfiltrationManager;

/**
 * Created by ken on 26/11/17 for fantastic_beasts
 */

public class ComponentNameGenerator implements IValueGenerator<ComponentName> {

    private static List<ComponentInfo> allComponentInfo;

    public ComponentNameGenerator() {
        try {
            allComponentInfo = InfiltrationManager.getInstalledComponentInfo();
        } catch (InfiltrationException e) {
            throw new RuntimeException("can't initialize ComponentNameGenerator", e);
        }
    }

    @Override
    public ComponentName random() {

        if (ValueGeneratorManager.r.nextBoolean()) {
            StringGenerator stringGenerator = new StringGenerator();
            try {
                return new ComponentName(
                        stringGenerator.random(),
                        stringGenerator.random());
            } catch (Exception e) {
                return randomValid();
            }
        } else {
            return randomValid();
        }

    }

    public ComponentName randomValid() {

        ComponentInfo componentInfo = allComponentInfo
                .get(ValueGeneratorManager.r.nextInt(allComponentInfo.size()));

        return new ComponentName(componentInfo.packageName, componentInfo.name);
    }



    @Override
    public ComponentName mutate(ComponentName mutant) throws ValueGeneratorException {
        try {

            switch (ValueGeneratorManager.chooseMutation()){
                case KNOWN:
                    return randomValid();
                case NEIGHBOR:
                    if (mutant == null)
                        return random();
                    StringGenerator stringGenerator = new StringGenerator();
                    try {
                        if (ValueGeneratorManager.r.nextBoolean()){
                            return new ComponentName(
                                    mutant.getPackageName(),
                                    stringGenerator.mutate(mutant.getClassName()));
                        }else {
                            return new ComponentName(
                                    stringGenerator.mutate(mutant.getPackageName()),
                                    mutant.getClassName());
                        }
                    } catch (NullPointerException e) {
                        return random();
                    }
                case INVERSE:
                    return randomValid();
            }
        } catch (Exception e) {
            throw new ValueGeneratorException("can't mutate " + mutant, e);
        }

        return null;
    }

    @Override
    public ComponentName crossover(ComponentName parent1, ComponentName parent2) throws ValueGeneratorException {

        try {
            switch (ValueGeneratorManager.chooseCrossOver()){
                case SINGLE_POINT:
                case TWO_POINT:
                case UNIFORM:
                case ARITHMETIC:
                    return new ComponentName(parent1.getPackageName(), parent2.getClassName());
            }
        }catch (Exception e){
            if (parent1 != null && parent2 == null)
                return parent1;
            if (parent2 != null && parent1 == null)
                return parent2;
            if (parent1 == null && parent2 == null)
                return null;
            throw new ValueGeneratorException("can't crossover "+parent1+" and "+parent2, e);
        }

        return null;


    }
}
