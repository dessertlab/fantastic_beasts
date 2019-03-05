package italiaken.fantasticbeasts.chizpurfle.fuzz.evolutionary;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import italiaken.fantasticbeasts.chizpurfle.L;
import italiaken.fantasticbeasts.chizpurfle.fuzz.ITest;
import italiaken.fantasticbeasts.chizpurfle.fuzz.TestExecutor;
import italiaken.fantasticbeasts.chizpurfle.infiltration.MethodCaller;
import italiaken.fantasticbeasts.chizpurfle.infiltration.MethodCallerException;
import italiaken.fantasticbeasts.chizpurfle.infiltration.logcat.LogcatWrapper;
import italiaken.fantasticbeasts.chizpurfle.instrumentation.InstrumentationException;
import italiaken.fantasticbeasts.chizpurfle.instrumentation.ProcessTracer;
import italiaken.fantasticbeasts.chizpurfle.instrumentation.trace.TracesMap;

/**
 * Created by ken on 24/11/17 for fantastic_beasts
 */

public class Individual implements ITest {

    private final MethodCaller methodCaller;
    private final Object[] parameters;

    private boolean executed;

    /* TEST OUTPUT */
    private long id;
    private boolean isKiller;
    private MethodCallerException callerException;
    private InstrumentationException instrumentationException;
    private TracesMap traces;
    private List<String> logcat;

    private int fitnessValue;

    private long executionTime;
    private long analysisTime;
    private int generationNumber;

    public Individual(int generationNumber, MethodCaller methodCaller, Object...parameters) {
        this.generationNumber = generationNumber;
        this.methodCaller = methodCaller;
        this.parameters = parameters;

        executed = false;
    }

    @Override
    public void execute(ProcessTracer tracer, AtomicBoolean isServiceDead) throws TestExecutor.DeadServiceException {

        if (executed)
            L.w("re-evaluating test "+id);

        executed = true;
        isKiller = false;

        /* a bit of clearance :P */
        Runtime.getRuntime().gc();

        /* clear logcat */
        LogcatWrapper.clearLogcat();

        try {
            /* start branch tracing */
            tracer.startTracing();

            /* actually call the service */
            try {
                L.i("Executing call for test "+id);
                methodCaller.call(parameters);
            } catch (MethodCallerException e) {
                /* save the caller exception */
                callerException = e;
            }

            /* stop the tracing */
            traces = tracer.stopTracing();

        } catch (InstrumentationException e) {
            L.e("MyTracer stops working", e);
            instrumentationException = e;
        }

        /* save logcat */
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            L.e("problem while waiting before dumpLines", e);
        }
        logcat = LogcatWrapper.dumpLines();

        /* check if service is dead */
        isKiller = isServiceDead.get();
    }

    @Override
    public boolean executed() {
        return executed;
    }

    @Override
    public void setExecutionTime(long executionTime) {
        this.executionTime = executionTime;
    }

    @Override
    public long getExecutionTime() {
        return executionTime;
    }

    public void setFitnessValue(int fitnessValue) {
        this.fitnessValue = fitnessValue;
    }

    public TracesMap getTraces() {
        return traces;
    }

    public int getFitnessValue() {
        return fitnessValue;
    }

    public MethodCaller getMethodCaller() {
        return methodCaller;
    }

    public Object[] getParameters() {
        return parameters;
    }

    public boolean isKiller() {
        return isKiller;
    }

    @Override
    public long getAnalysisTime() {
        return analysisTime;
    }

    @Override
    public void setNumericId(long id) {
        this.id = id;
    }

    @Override
    public long getNumericId() {
        return id;
    }

    @Override
    public void setAnalysisTime(long analysisTime) {
        this.analysisTime = analysisTime;
    }

    public int getGenerationNumber() {
        return generationNumber;
    }

    public MethodCallerException getMethodCallerException() {
        return callerException;
    }

    public InstrumentationException getInstrumentationException() {
        return instrumentationException;
    }

    public List<String> getLogcat() {
        return logcat;
    }
}
