package italiaken.fantasticbeasts.chizpurfle.fuzz.blackbox;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;

import italiaken.fantasticbeasts.chizpurfle.L;
import italiaken.fantasticbeasts.chizpurfle.configuration.ConfigurationManager;
import italiaken.fantasticbeasts.chizpurfle.fuzz.ITest;
import italiaken.fantasticbeasts.chizpurfle.fuzz.TestExecutor;
import italiaken.fantasticbeasts.chizpurfle.fuzz.evolutionary.IndividualAnalyzer;
import italiaken.fantasticbeasts.chizpurfle.fuzz.generators.ParametersGenerator;
import italiaken.fantasticbeasts.chizpurfle.fuzz.generators.ValueGeneratorException;
import italiaken.fantasticbeasts.chizpurfle.infiltration.InfiltrationException;
import italiaken.fantasticbeasts.chizpurfle.infiltration.MethodCaller;
import italiaken.fantasticbeasts.chizpurfle.infiltration.MethodCallerBuilder;
import italiaken.fantasticbeasts.chizpurfle.infiltration.MethodCallerBuilderFuzzableMethodFilter;
import italiaken.fantasticbeasts.chizpurfle.infiltration.ServiceManagerWrapper;
import italiaken.fantasticbeasts.chizpurfle.infiltration.service.BatteryPropertiesWrapper;
import italiaken.fantasticbeasts.chizpurfle.instrumentation.InstrumentationException;

/**
 * Created by ken on 27/11/17 for fantastic_beasts
 */

public class ServiceFuzzer {

    //TODO GESTIRE BENE LE ECCEZIONI
    public static void start() throws InfiltrationException, InstrumentationException {

        L.i("Chizpurfle black box service fuzzer started");

        /*
         * retrieve main parameters
         */
        String serviceName = ConfigurationManager.getServiceName();
        String processName = ConfigurationManager.getProcessName();

        /*
         * the serviceManagerWrapper to get the service object (even in case of death)
         */
        ServiceManagerWrapper serviceManagerWrapper = new ServiceManagerWrapper();

        /*
         * a boolean to share between the death notifier of the service and the test executor
         */
        AtomicBoolean isServiceDead = new AtomicBoolean(false);

        /*
         * initialize the test executor and the test analyzer
         */
        TestExecutor testExecutor = new TestExecutor(processName, isServiceDead);
        BlackBoxTestAnalyzer testAnalyzer = new BlackBoxTestAnalyzer();

        /*
         * build a method caller for each (filtered) method of the service
         */
        List<MethodCaller> methodCallers = new MethodCallerBuilder()
                .setCalledObject(serviceManagerWrapper
                        .getServiceObjectByName(serviceName, isServiceDead))
                .createMethodCallers(new MethodCallerBuilderFuzzableMethodFilter());


        Queue<BlackBoxTest> queue = new ArrayDeque<>();
        for (MethodCaller methodCaller : methodCallers){
            ParametersGenerator generator = new ParametersGenerator(methodCaller.getParameterTypes());
            queue.add(new BlackBoxTest(methodCaller, generator, generator.random()));
        }

        // in black box maxGeneration is the number of experiment
        int numberOfExperiment = ConfigurationManager.getMaxGeneration();

        for (int i = 0; i< numberOfExperiment; i++){
            if (queue.isEmpty()){
                L.w("queue is empty!");
                break;
            }

            if (i % 20 == 0) checkBattery(serviceManagerWrapper);

            BlackBoxTest test = queue.remove();

            try {
                testExecutor.execute(test);
            } catch (TestExecutor.DeadServiceException e) {
                L.e("trying to execute with a dead service", e);
                handleDeath(testExecutor, serviceManagerWrapper, methodCallers);
            }

            testAnalyzer.startAnalysis(test);

            /*
             * if the test killed the service, re-initialize
             */
            if (test.isKiller()){
                handleDeath(testExecutor, serviceManagerWrapper, methodCallers);
            }

            testAnalyzer.startSave(test);

            try {
                queue.add(test.fuzz());
            } catch (ValueGeneratorException e) {
                L.e("problem in fuzzing the blackboxtest", e);
            }

        }

        L.i("End of the fuzzing");

    }

    private static void checkBattery(ServiceManagerWrapper serviceManagerWrapper) throws InfiltrationException {
        BatteryPropertiesWrapper batteryPropertiesWrapper =
                new BatteryPropertiesWrapper(serviceManagerWrapper);

        int capacity = batteryPropertiesWrapper.getBatteryPropertyCapacity();

        while ( capacity <= 40){

            int sleepTimeInMillis = (100 - capacity) * 60 * 1000;

            L.d("battery capacity is "+capacity+", going to sleep for "+sleepTimeInMillis+"ms");

            try {
                Thread.sleep(sleepTimeInMillis);
            } catch (InterruptedException ignored) {}

            capacity = batteryPropertiesWrapper.getBatteryPropertyCapacity();
        }

    }

    /*
     * it handles the death of the service under test by re-initializing the instrumentation and by
     * updating the service object in the method callers
     */
    private static void handleDeath(TestExecutor testExecutor,
                                    ServiceManagerWrapper serviceManagerWrapper,
                                    List<MethodCaller> methodCallers)
            throws InstrumentationException, InfiltrationException {
        L.i("handling the target death...");

        /*
         * retrieve main parameters
         */
        String serviceName = ConfigurationManager.getServiceName();
        String processName = ConfigurationManager.getProcessName();

        /*
         * wait the smartphone to inizialize all its services
         */
        AtomicBoolean isServiceDead = new AtomicBoolean(false);

        int trials = 1;
        do {
            try {
                try {
                    Thread.sleep(trials
                            * ConfigurationManager.getRecoveryTimeInMilliseconds());
                } catch (InterruptedException ignored) {}

                L.d(trials + "^ trial");
                testExecutor.recycle();
                testExecutor.initInstrumentation(processName, isServiceDead);

                break;
            } catch (InstrumentationException e) {
                L.e("can't restore fuzzing... retry");
            }
        } while (trials++ <= ConfigurationManager.getRestoreMaxTrial());

        Object calledObject = serviceManagerWrapper
                .getServiceObjectByName(serviceName, isServiceDead);
        for (MethodCaller methodCaller : methodCallers){
            methodCaller.setCalledObject(calledObject);
        }

        L.i("restored fuzzing");

    }




}
