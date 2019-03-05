package italiaken.fantasticbeasts.chizpurfle;

import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import italiaken.fantasticbeasts.chizpurfle.configuration.ConfigurationException;
import italiaken.fantasticbeasts.chizpurfle.configuration.ConfigurationManager;
import italiaken.fantasticbeasts.chizpurfle.fuzz.blackbox.ServiceFuzzer;
import italiaken.fantasticbeasts.chizpurfle.fuzz.evolutionary.EvoluzionaryFuzzer;
import italiaken.fantasticbeasts.chizpurfle.fuzz.generators.IValueGenerator;
import italiaken.fantasticbeasts.chizpurfle.fuzz.generators.ParametersGenerator;
import italiaken.fantasticbeasts.chizpurfle.fuzz.generators.ValueGeneratorException;
import italiaken.fantasticbeasts.chizpurfle.fuzz.generators.primitive.PrimitiveGenerator;
import italiaken.fantasticbeasts.chizpurfle.infiltration.MethodCallerBuilderFuzzableMethodFilter;
import italiaken.fantasticbeasts.chizpurfle.infiltration.InfiltrationException;
import italiaken.fantasticbeasts.chizpurfle.infiltration.MethodCaller;
import italiaken.fantasticbeasts.chizpurfle.infiltration.MethodCallerBuilder;
import italiaken.fantasticbeasts.chizpurfle.infiltration.ServiceManagerWrapper;
import italiaken.fantasticbeasts.chizpurfle.infiltration.service.BatteryPropertiesWrapper;
import italiaken.fantasticbeasts.chizpurfle.instrumentation.InstrumentationException;
import italiaken.fantasticbeasts.chizpurfle.instrumentation.InstrumentationManager;
import italiaken.fantasticbeasts.chizpurfle.instrumentation.ProcessTracer;
import italiaken.fantasticbeasts.chizpurfle.instrumentation.trace.Block;
import italiaken.fantasticbeasts.chizpurfle.instrumentation.trace.Branch;
import italiaken.fantasticbeasts.chizpurfle.instrumentation.trace.ITrace;

/**
 * Created by ken on 20/11/17 for fantastic_beasts
 */

public class Main {

    public  static  void  main(String[] args) {

        L.setTag("Chizpurfle");
        L.i("Welcome, I am a Chizpurfle!");
        printMemoryInfo();

        try {

            ConfigurationManager.initConfiguration(args);

            if (ConfigurationManager.isExtraction()){
                extractModel();
            }else if (ConfigurationManager.isSingleCall()){
                singleCall();
            }else if (ConfigurationManager.isBlackBoxType())
                ServiceFuzzer.start();
            else if (ConfigurationManager.newFitnessEvaluator() != null)
                EvoluzionaryFuzzer.start();


        } catch (Exception | Error e) {
            L.e("problem with main");
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            L.e(sw.toString());
        }

        L.i("Thank you for feeding me!");

    }

    private static void extractModel() throws InfiltrationException {
        // service interfaces
        File outputFile = new File("/data/local/tmp/all_services_interfaces_in_binder.json");

        String serviceInterfaces = new ServiceManagerWrapper().getAllServiceInterfaces();
        L.d("Saving model file...");
        if (outputFile.exists()) outputFile.delete();
        try {
            outputFile.createNewFile();
            FileOutputStream fOut = new FileOutputStream(outputFile);
            fOut.write(serviceInterfaces.getBytes());
            fOut.close();
        } catch (IOException e) {
            throw new RuntimeException("Unable to save the model file");
        }


    }

    private static void singleCall() throws InfiltrationException {

        String serviceName = ConfigurationManager.getServiceName();
        String methodName = "setEnabledCocktailIds";

        ServiceManagerWrapper serviceManagerWrapper = new ServiceManagerWrapper();
        MethodCaller methodCaller = new MethodCallerBuilder()
                .setCalledObject(serviceManagerWrapper.getServiceObjectByName(serviceName, null))
                .setMethodByName(methodName)
                .createMethodCaller();



        L.i("Calling");
        methodCaller.call(
        /* arguments */
        new int[]{635391817, -384962296, 1575783343, -1209818926, 708535444, 1612416286, -693190087, -234222882, 1147971868, -1197488911, -486571509, -1735288863, 238648844, 428088662, -2002012503, -1592407783, 2062627418, 53899234, -961810917, 1902997577, 916048000, 1337325707, 472086460, -747812860, -1689787535, -1328125554, 70392913, 587549738, -1026289974, -1052876396, -241122468, -725838157, -1902127544, -1655398384, -1927112270, 1753728171, 1129341050, -1902011414, 1919520075, 1379018621, 1653309623, -1000251295, 753283673, -2058205581}

        );
        L.i("Called");
    }

    private static void testBatteryPropertiesWrapper() {
        try {
            ServiceManagerWrapper serviceManagerWrapper = new ServiceManagerWrapper();

            BatteryPropertiesWrapper batteryPropertiesWrapper = new BatteryPropertiesWrapper(serviceManagerWrapper);

            L.i(batteryPropertiesWrapper.getBatteryPropertyCapacity()+ " olè");

        } catch (InfiltrationException e) {
            L.e("problem with main");

            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            L.e(sw.toString());
        }
    }

    private static void printMemoryInfo() {
        // Get current size of heap in bytes
        long heapSize = Runtime.getRuntime().totalMemory();

        // Get maximum size of heap in bytes. The heap cannot grow beyond this size.// Any attempt will result in an OutOfMemoryException.
        long heapMaxSize = Runtime.getRuntime().maxMemory();

        // Get amount of free memory within the heap in bytes. This size will increase // after garbage collection and decrease as new objects are created.
        long heapFreeSize = Runtime.getRuntime().freeMemory();

        L.d(heapSize + ", " + heapMaxSize + ", " + heapFreeSize);
    }

    private static void testGenerators() {
        try {
            Object[] p1;
            Object[] p2;

            int[] a = new int[10];
            IValueGenerator valueGenerator = new ParametersGenerator(
                    Intent.class,
                    a.getClass(),
                    ComponentName.class,
                    String.class,
                    Character.class,
                    boolean.class);

            for (int i = 0; i< 100; i++){
                p1 = (Object[]) valueGenerator.random();
                p2 = (Object[]) valueGenerator.random();
                for (int j = 0; j<1000; j++){
                    L.i("p1:" + Arrays.deepToString(p1));
                    L.i("p2:" + Arrays.deepToString(p2));
                    p1 = (Object[]) valueGenerator.mutate(p1);
                    p2 = (Object[]) valueGenerator.mutate(p2);
                    p1 = (Object[]) valueGenerator.crossover(p1, p2);
                    p2 = (Object[]) valueGenerator.crossover(p2, p1);
                }
            }

            L.i(a.getClass().getName());
        } catch (ValueGeneratorException e) {
            L.e("problem main", e);
        }
    }

    private static void testInfiltrationWithSomeFuzz() {
        try {
            ServiceManagerWrapper serviceManagerWrapper = new ServiceManagerWrapper();

            Object serviceObject = serviceManagerWrapper
                    .getServiceObjectByName("package", null);

            MethodCaller methodCaller = new MethodCallerBuilder()
                    .setCalledObject(serviceObject)
                    .setMethodByName("flushPackageRestrictionsAsUser")
                    .createMethodCaller();

            Class k = methodCaller.getParameterTypes()[0];

            Object o = new PrimitiveGenerator(k).random();

            methodCaller.call(null);

            L.i(o.getClass().isPrimitive()?"yes":"no");

        } catch (InfiltrationException e) {
            L.e("problem main", e);
        }
    }

    // TODO white/black list with configurations
    private static void testIntrumentationModule() {

        try {
            ConfigurationManager.initConfiguration(new String[]{"spengestureservice"});
        } catch (ConfigurationException e) {
            e.printStackTrace();
            return;
        }

        InstrumentationManager instrumentationManager = new InstrumentationManager();
        ProcessTracer processTracer;

        try {
            processTracer = instrumentationManager.instrumentProcess("system_server");
            Thread.sleep(100);

            L.i("PRIMA MANCHE");
            processTracer.startTracing();
            Thread.sleep(5*1000);
            testInfiltrationModule();
            Thread.sleep(5*1000);
            Map<Class, Set<? extends ITrace>> m = processTracer.stopTracing().getMap();

            L.i("result size = " + m.size());
            L.i("blocks:" + m.get(Block.class).size());
            L.i("branches:" + m.get(Branch.class).size());

            instrumentationManager.reset();
        } catch (InstrumentationException | InterruptedException e) {
            L.e("problem main", e);
        }
    }

    // TODO check logcat with first experiment
    private static void testInfiltrationModule(){
        try {
            ServiceManagerWrapper serviceManagerWrapper = new ServiceManagerWrapper();

            Object serviceObject = serviceManagerWrapper
                    .getServiceObjectByName("spengestureservice", null);

            List<MethodCaller> methodCallers = new MethodCallerBuilder()
                    .setCalledObject(serviceObject)
                    .createMethodCallers(new MethodCallerBuilderFuzzableMethodFilter());

            methodCallers.get(0).call(null, null);

            //List<String> logcatLines = LogcatWrapper.dumpLines();

            //L.i(logcatLines != null ? logcatLines.get(0) : "empty logcat...");

        } catch (InfiltrationException e) {
            L.e("problem main", e);
        }
    }

}
