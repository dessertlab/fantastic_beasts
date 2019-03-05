package italiaken.fantasticbeasts.chizpurfle.fuzz.evolutionary;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

import italiaken.fantasticbeasts.chizpurfle.L;
import italiaken.fantasticbeasts.chizpurfle.Utils;
import italiaken.fantasticbeasts.chizpurfle.fuzz.TestOutputAnalyzer;
import italiaken.fantasticbeasts.chizpurfle.fuzz.generators.primitive.BooleanGenerator;
import italiaken.fantasticbeasts.chizpurfle.fuzz.generators.primitive.ByteGenerator;
import italiaken.fantasticbeasts.chizpurfle.fuzz.generators.primitive.CharacterGenerator;
import italiaken.fantasticbeasts.chizpurfle.fuzz.generators.primitive.DoubleGenerator;
import italiaken.fantasticbeasts.chizpurfle.fuzz.generators.primitive.FloatGenerator;
import italiaken.fantasticbeasts.chizpurfle.fuzz.generators.primitive.IntegerGenerator;
import italiaken.fantasticbeasts.chizpurfle.fuzz.generators.primitive.LongGenerator;
import italiaken.fantasticbeasts.chizpurfle.fuzz.generators.primitive.ShortGenerator;

/**
 * Created by ken on 27/11/17 for fantastic_beasts
 */

public class IndividualAnalyzer extends TestOutputAnalyzer<Individual>{

    private final IFitnessEvaluator fitnessEvaluator;

    IndividualAnalyzer(IFitnessEvaluator fitnessEvaluator) {
        this.fitnessEvaluator = fitnessEvaluator;
    }

    public void analyze(Individual individual) {

        fitnessEvaluator.analyzeForFitness(individual);

    }

    public void evaluateFitness(Community community) {

        for (Individual individual : community.getAllIndividuals()){
            fitnessEvaluator.evaluateFitness(individual);
        }

        community.updateFitnessValues();
        community.updateTargetSizes();

    }

    @Override
    public void save(Individual individual, File testFolder) {

        /* INDIVIDUAL INFO */
        saveIndividualInfo(individual, testFolder);

        /* EXCEPTION INFO */
        saveExceptionInfo(individual, testFolder);

        /* LOGS */
        saveLogs(individual, testFolder);

        /* TRACES */
        if (individual.getTraces() != null) {
            saveTraces(testFolder, individual.getTraces().getJsonString());
        }


    }

    private void saveIndividualInfo(Individual individual, File testFolder) {
        try {
            JSONObject individualInfo = new JSONObject()
                    .put("method", individual.getMethodCaller().getMethodName());

            JSONArray parameter_types = new JSONArray();
            for (Class k : individual.getMethodCaller().getParameterTypes()){
                parameter_types.put(k.getName());
            }
            individualInfo.put("parameter_types", parameter_types);

            JSONArray parameter_values = new JSONArray();
            for (Object o : individual.getParameters()){
                if (o == null){
                    parameter_values.put("null");
                } else if (o.getClass().isArray()) {
                    if (o.getClass().getComponentType().equals(Byte.TYPE))
                        parameter_values.put(Arrays.toString((byte[])o));
                    else if (o.getClass().getComponentType().equals(Short.TYPE))
                        parameter_values.put(Arrays.toString((short[])o));
                    else if (o.getClass().getComponentType().equals(Integer.TYPE))
                        parameter_values.put(Arrays.toString((int[])o));
                    else if (o.getClass().getComponentType().equals(Long.TYPE))
                        parameter_values.put(Arrays.toString((long[])o));
                    else if (o.getClass().getComponentType().equals(Float.TYPE))
                        parameter_values.put(Arrays.toString((float[])o));
                    else if (o.getClass().getComponentType().equals(Double.TYPE))
                        parameter_values.put(Arrays.toString((double[])o));
                    else if (o.getClass().getComponentType().equals(Boolean.TYPE))
                        parameter_values.put(Arrays.toString((boolean[])o));
                    else if (o.getClass().getComponentType().equals(Character.TYPE))
                        parameter_values.put(Arrays.toString((char[])o));
                    else
                        parameter_values.put(Arrays.toString((Object[]) o));
                } else{
                    try {
                        parameter_values.put(o.toString());
                    } catch (NullPointerException npe) {
                        L.w("NullPointerException when calling toString on a not null object",
                                npe);
                        parameter_values.put("null");
                    }
                }
            }
            individualInfo.put("parameter_values", parameter_values);

            individualInfo.put("generation_number", individual.getGenerationNumber())
                    .put("fitness_value", individual.getFitnessValue());

            Utils.saveJsonToFile(individualInfo, testFolder, "individual");

        } catch (JSONException e) {
            L.e("can't save individual info", e);
        }
    }

    private void saveExceptionInfo(Individual individual, File testFolder) {
        try {
            JSONObject exceptionInfo = new JSONObject();

            Throwable throwable;

            throwable = individual.getMethodCallerException();
            JSONArray methodCallerException = new JSONArray();
            while (throwable != null){
                methodCallerException.put(throwable.toString());
                throwable = throwable.getCause();
            }
            exceptionInfo.put("method_caller_exception", methodCallerException);

            throwable = individual.getInstrumentationException();
            JSONArray instrumentationException = new JSONArray();
            while (throwable != null){
                instrumentationException.put(throwable.toString());
                throwable = throwable.getCause();
            }
            exceptionInfo.put("instrumentation_exception", instrumentationException);

            Utils.saveJsonToFile(exceptionInfo, testFolder, "exceptions");

        } catch (JSONException e) {
            L.e("can't save instrumentation info", e);
        }
    }

    private void saveLogs(Individual individual, File testFolder) {
        try {
            JSONObject logs = new JSONObject();

            JSONArray logcat = new JSONArray();
            for (String log : individual.getLogcat()){
                logcat.put(log);
            }
            logs.put("logcat", logcat);

            Utils.saveJsonToFile(logs, testFolder, "logs");

        } catch (JSONException e) {
            L.e("can't save logs", e);
        }
    }

    private void saveTraces(File testFolder, JSONObject jsonString) {
        Utils.saveJsonToFile(jsonString, testFolder, "traces");
    }

    void saveCommunityInfo(Community community) {

        String fileName = campaignFolder.getPath() + File.separator + "generation.info";
        try (PrintWriter out = new PrintWriter( new BufferedWriter(
                new FileWriter(fileName, true)))){

            StringBuilder stringBuilder = new StringBuilder("generation ")
                    .append(community.getAllIndividuals().get(0).getGenerationNumber())
                    .append(": ");

            //for (int i = 0; i<community.getPopulations().size(); i++){
            for (Population population : community.getPopulations()){
                stringBuilder
                        .append(population.getIndividuals().get(0).getMethodCaller().getMethodName())
                        .append(".")
                        .append(population.getFitnessValue())
                        .append(".")
                        .append(population.getTargetSize())
                        .append(" ");
            }

            String communityInfo = stringBuilder.toString();
            L.i(communityInfo);
            out.println(communityInfo);
        } catch (IOException e) {
            L.e("can't save community info", e);
        }

    }

    public void saveIndividualsInfo(Community community) {
        for (Individual individual : community.getAllIndividuals()){
            startSave(individual);
        }
    }

    public interface IFitnessEvaluator {

        /*
         * analyze the individual of the current generation and extract the data necessary to the
         * fitness evaluation
         */
        void analyzeForFitness(Individual individual);

        /*
         * evaluate the fitness of an individual
         */
        void evaluateFitness(Individual individual);
    }

}
