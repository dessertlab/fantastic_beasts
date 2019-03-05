package italiaken.fantasticbeasts.chizpurfle.infiltration.service;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import italiaken.fantasticbeasts.chizpurfle.L;
import italiaken.fantasticbeasts.chizpurfle.infiltration.InfiltrationException;
import italiaken.fantasticbeasts.chizpurfle.infiltration.MethodCaller;
import italiaken.fantasticbeasts.chizpurfle.infiltration.MethodCallerBuilder;
import italiaken.fantasticbeasts.chizpurfle.infiltration.ServiceManagerWrapper;

/**
 * Created by ken on 15/12/17 for fantastic_beasts
 */

public class BatteryPropertiesWrapper {

    private final Object serviceObject;
    private final MethodCaller getPropertyMethodCaller;

    public BatteryPropertiesWrapper(ServiceManagerWrapper serviceManagerWrapper) throws InfiltrationException {

        serviceObject = serviceManagerWrapper
                .getServiceObjectByName("batteryproperties", null);

        getPropertyMethodCaller = new MethodCallerBuilder()
                .setCalledObject(serviceObject)
                .setMethodByName("getProperty")
                .createMethodCaller();
    }

    public int getBatteryPropertyCapacity() throws InfiltrationException {

        try {

            Class batteryPropertyClass = Class
                    .forName("android.os.BatteryProperty",
                            false,
                            ClassLoader.getSystemClassLoader());

            Object batteryPropertyObject = batteryPropertyClass
                    .getConstructors()[0]
                    .newInstance();

            int result = (int) getPropertyMethodCaller
                    .call(  4 , batteryPropertyObject);

            if (result != 0){
                throw new InfiltrationException("getProperty returned "+result);
            }

            MethodCaller anotherCaller = new MethodCallerBuilder()
                    .setCalledObject(batteryPropertyObject)
                    .setMethodByName("getLong")
                    .createMethodCaller();

            Long capacity =  (Long) anotherCaller.callNoParameters();

            L.d("Smartphone has a battery capacity of "+ capacity);

            return capacity.intValue();

        } catch (ClassNotFoundException | IllegalAccessException |
                InstantiationException | InvocationTargetException e) {
            throw new InfiltrationException(e);
        }



    }

}
