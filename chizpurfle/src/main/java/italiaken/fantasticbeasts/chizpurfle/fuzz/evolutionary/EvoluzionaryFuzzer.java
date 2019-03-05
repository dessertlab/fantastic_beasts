package italiaken.fantasticbeasts.chizpurfle.fuzz.evolutionary;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import italiaken.fantasticbeasts.chizpurfle.L;
import italiaken.fantasticbeasts.chizpurfle.configuration.ConfigurationManager;
import italiaken.fantasticbeasts.chizpurfle.fuzz.TestExecutor;
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

public class EvoluzionaryFuzzer {

    //TODO GESTIRE BENE LE ECCEZIONI
    public static void start() throws InfiltrationException, InstrumentationException {

        L.i("Chizpurfle evolutionary fuzzer started");

        /*
         * retrieve main parameters
         */
        String serviceName = ConfigurationManager.getServiceName();
        int maxGeneration = ConfigurationManager.getMaxGeneration();
        String processName = ConfigurationManager.getProcessName();
        int maxIncubatingGeneration = ConfigurationManager.getMaxIncubatingGeneration();

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
        IndividualAnalyzer individualAnalyzer =
                new IndividualAnalyzer(ConfigurationManager.newFitnessEvaluator());

        /*
         * build a method caller for each (filtered) method of the service
         */
        List<MethodCaller> methodCallers = new MethodCallerBuilder()
                .setCalledObject(serviceManagerWrapper
                        .getServiceObjectByName(serviceName, isServiceDead))
                .createMethodCallers(new MethodCallerBuilderFuzzableMethodFilter());;
        L.i("community of service "
                + serviceName
                + " has "
                + methodCallers.size()
                + " populations of "
                + ConfigurationManager.getPopulationInitialTargetSize()
                + " individuals each");

        /*
         * initialize a community
         */
        Community community = new Community(methodCallers);

        /*
         * generate at most maxGeneration generations
         */
        int generationNumber = 0;
        while (generationNumber != maxGeneration){

            checkBattery(serviceManagerWrapper);

            if (generationNumber == 0){
                L.i("creating generation 0");
                community.createRandomIndividuals();
            }else{
                /*
                * every population in the community generates their own offspring (next generation)
                */
                L.i("generating " + generationNumber + "th community");
                for (Population population : community.getPopulations()){
                    population.generateOffspring();
                    population.goToNextGeneration();
                }
            }

            L.i("observing community of generation "+ generationNumber);

            /*
             * execute, analyze, and save every individual
             */
            for (Individual individual : community.getAllIndividuals()){

                while (true) {
                    try {
                        testExecutor.execute(individual);
                        break;
                    } catch (TestExecutor.DeadServiceException e) {
                        L.e("trying to execute with a dead service", e);
                        handleDeath(testExecutor, serviceManagerWrapper, methodCallers);
                    }
                }

                individualAnalyzer.startAnalysis(individual);

                /*
                 * if the individual killed the service, re-initialize
                 */
                if (individual.isKiller()){
                    handleDeath(testExecutor, serviceManagerWrapper, methodCallers);
                }

            }

            /*
             * evaluate the fitness for the whole community and save the individuals and community
             * info
             */
            individualAnalyzer.evaluateFitness(community);
            individualAnalyzer.saveIndividualsInfo(community);
            individualAnalyzer.saveCommunityInfo(community);

            /*
             * exit the generation loop if there are no improvements over
             * ConfigurationManager.getMaxIncubatingGeneration generations
             */
            if (community.getTotalFitnessValue() == 0){
                L.w("The community of generation "
                        + generationNumber
                        + "provided zero total fitness");
                if (maxIncubatingGeneration-- == 0)
                    break;
            }else {
                maxIncubatingGeneration = ConfigurationManager.getMaxIncubatingGeneration();
            }

            generationNumber++;

        }

        L.i("End of the generations");

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