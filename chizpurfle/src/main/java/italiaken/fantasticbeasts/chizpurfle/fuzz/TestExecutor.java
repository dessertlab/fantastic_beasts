package italiaken.fantasticbeasts.chizpurfle.fuzz;

import java.util.concurrent.atomic.AtomicBoolean;

import italiaken.fantasticbeasts.chizpurfle.instrumentation.InstrumentationException;
import italiaken.fantasticbeasts.chizpurfle.instrumentation.InstrumentationManager;

/**
 * Created by ken on 27/11/17 for fantastic_beasts
 */

public class TestExecutor {

    private AtomicBoolean isServiceDead;
    private InstrumentationManager instrumentationManager;
    private long id = 0;

    public TestExecutor(String processName, AtomicBoolean isServiceDead) throws InstrumentationException {
        initInstrumentation(processName, isServiceDead);
    }

    public void initInstrumentation(String processName, AtomicBoolean isServiceDead) throws InstrumentationException {
        instrumentationManager = new InstrumentationManager();
        instrumentationManager.instrumentProcess(processName);

        this.isServiceDead = isServiceDead;
    }

    public void recycle() throws InstrumentationException {
        instrumentationManager.reset();
    }

    public void execute(ITest test) throws DeadServiceException {

        /* check if service is dead */
        if (isServiceDead.get())
            throw new DeadServiceException();

        long startTime = System.nanoTime();

        test.setNumericId(id++);

        test.execute(instrumentationManager.getTracer(), isServiceDead);

        test.setExecutionTime(System.nanoTime()-startTime);

    }

    public static class DeadServiceException extends Exception {
    }
}
