package italiaken.fantasticbeasts.chizpurfle.fuzz;

import java.util.concurrent.atomic.AtomicBoolean;

import italiaken.fantasticbeasts.chizpurfle.instrumentation.ProcessTracer;

/**
 * Created by ken on 27/11/17 for fantastic_beasts
 */

public interface ITest {

    public void execute(ProcessTracer tracer, AtomicBoolean isServiceDead) throws TestExecutor.DeadServiceException;

    public boolean executed();

    void setExecutionTime(long executionTime);

    long getExecutionTime();

    void setAnalysisTime(long analysisTime);

    long getAnalysisTime();

    void setNumericId(long id);

    long getNumericId();

    boolean isKiller();
}
