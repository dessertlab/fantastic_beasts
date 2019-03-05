package italiaken.fantasticbeasts.chizpurfle.infiltration;

import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import italiaken.fantasticbeasts.chizpurfle.L;

/**
 * Created by ken on 21/11/17 for fantastic_beasts
 */

public class ServiceManagerWrapper {

    private Object theServiceManager;

    public ServiceManagerWrapper() throws InfiltrationException {

        try {
            initializeReference();
        } catch (ClassNotFoundException |
                NoSuchMethodException |
                InvocationTargetException |
                IllegalAccessException e) {
            throw new InfiltrationException(e);
        }

    }

    private void initializeReference() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Class serviceManagerNativeKlass = Class.forName("android.os.ServiceManagerNative", false, ClassLoader.getSystemClassLoader());
        Class binderInternalKlass = Class.forName("com.android.internal.os.BinderInternal", false, ClassLoader.getSystemClassLoader());

        if (serviceManagerNativeKlass == null || binderInternalKlass == null) {
            throw new NullPointerException();
        }

        Method m = binderInternalKlass.getDeclaredMethod("getContextObject", null);
        Object contextObject = m.invoke(null, null);

        Class[] cArg = new Class[1];
        cArg[0] = IBinder.class;
        Method m2 = serviceManagerNativeKlass.getDeclaredMethod("asInterface", cArg);

        Object[] oArg = new Object[1];
        oArg[0] = contextObject;
        theServiceManager = m2.invoke(null, oArg);
    }

    public Object getServiceObjectByName(String serviceName, final AtomicBoolean isServiceDead) throws InfiltrationException {

        try {

            Class[] cArg = new Class[1];
            cArg[0] = String.class;
            Method m1 = theServiceManager.getClass().getDeclaredMethod("getService", cArg);

            Object[] oArg = new Object[1];
            oArg[0] = serviceName;
            Object service = m1.invoke(theServiceManager, oArg);

            if (service == null)
                throw new InfiltrationException("theServiceManager.getService(" + serviceName + ") returned null");

            IBinder serviceBinder = (IBinder) service;

            if (isServiceDead != null) {
                isServiceDead.set(false);
                ((IBinder) service).linkToDeath(new IBinder.DeathRecipient() {
                    @Override
                    public void binderDied() {
                        isServiceDead.set(true);
                    }
                }, 0);
            }

            return getServiceInstance(serviceBinder);

        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | RemoteException | ClassNotFoundException e) {
            L.e("problem getting service with name " + serviceName, e);
            throw new InfiltrationException(e);
        }
    }

    private Object getServiceInstance(IBinder serviceBinder) throws RemoteException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        String descriptor = serviceBinder.getInterfaceDescriptor();
//        Class<?> enclosingClass = Class.forName(descriptor);
//        Object enclosingInstance = enclosingClass.newInstance();

        Class<?> innerClass = Class.forName(descriptor + "$Stub");
//        Constructor<?> ctor = innerClass.getDeclaredConstructor(enclosingClass);
//        Object innerInstance = ctor.newInstance(enclosingInstance);

        Class[] cArg = new Class[1];
        cArg[0] = IBinder.class;
        Method m = innerClass.getDeclaredMethod("asInterface", cArg);

        Object[] oArg = new Object[1];
        oArg[0] = serviceBinder;

        return m.invoke(null, oArg);

    }


    public String getAllServiceInterfaces() throws InfiltrationException {
        List<String> allServiceName = new ArrayList<String>();

        try {

            Method m3 = theServiceManager.getClass().getDeclaredMethod("listServices", null);
            String[] serviceStrings = (String[]) m3.invoke(theServiceManager, null);

            allServiceName = Arrays.asList(serviceStrings);

        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new InfiltrationException("problem while calling listServices", e);
        }
        
        JSONArray jsonArray = new JSONArray();
        for (String interfaceName : allServiceName){
            jsonArray.put(getServiceInterface(interfaceName));
        }

        return jsonArray.toString();
    }

    private JSONObject getServiceInterface(String serviceName) {
        JSONObject serviceJSON = new JSONObject();

        try {
            serviceJSON.put("name", serviceName);

            Class[] cArg = new Class[1];
            cArg[0] = new String().getClass();
            Method m1 = theServiceManager.getClass().getDeclaredMethod("getService", cArg);

            Object[] oArg = new Object[1];
            oArg[0] = new String(serviceName);
            IBinder service = (IBinder) m1.invoke(theServiceManager, oArg);

            if (service == null)
                throw new NullPointerException();

            Method m4 = service.getClass().getDeclaredMethod("getInterfaceDescriptor", null);
            String interfaceName = (String)m4.invoke(service, null);
            serviceJSON.put("interface", interfaceName);

            Class serviceInterface = Class.forName(interfaceName);
            serviceJSON.put("type","java");
            JSONArray methodsJSONArray = new JSONArray();
            for (Method m5 : serviceInterface.getDeclaredMethods()){
                JSONObject methodJSON = new JSONObject();
                methodJSON.put("name", m5.getName());
                JSONArray parametersJSONArray = new JSONArray();
                for (Class c5: m5.getParameterTypes()){
                    parametersJSONArray.put(c5.getName());
                }
                methodJSON.put("parameters", parametersJSONArray);
                methodsJSONArray.put(methodJSON);
            }
            serviceJSON.put("methods", methodsJSONArray);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | JSONException | NullPointerException e) {
            Log.w("unable to extract for service "+ serviceName, e);
        } catch (ClassNotFoundException e) {
            try {
                serviceJSON.put("type", "c");
            } catch (JSONException e1) {
                Log.w("unable to extract for service "+ serviceName, e);
            }
            Log.w("service "+ serviceName+ " should be a C/C++ service", e);
        }


        return serviceJSON;

    }

}
