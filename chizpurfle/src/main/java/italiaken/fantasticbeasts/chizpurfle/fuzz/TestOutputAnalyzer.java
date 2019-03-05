package italiaken.fantasticbeasts.chizpurfle.fuzz;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.DateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Locale;

import italiaken.fantasticbeasts.chizpurfle.L;
import italiaken.fantasticbeasts.chizpurfle.Utils;
import italiaken.fantasticbeasts.chizpurfle.configuration.ConfigurationManager;

/**
 * Created by ken on 28/11/17 for fantastic_beasts
 */

abstract public class TestOutputAnalyzer<T extends ITest> {

    private final long creationTime;
    protected final File campaignFolder;

    public TestOutputAnalyzer() {
        creationTime = System.nanoTime();

        String campaignTimeString = DateFormat.getDateTimeInstance()
                .format(new Date())
                .replaceAll("\\s", "_")
                .replace(":", "")
                .replace(",", "");

        /* create the folder for the output */
        File chizpurfleFolder = new File(ConfigurationManager.getOutputRootDir());
        if (!chizpurfleFolder.exists() || !chizpurfleFolder.isDirectory()){
            L.d("output root folder creation: "+(chizpurfleFolder.mkdir()?"success":"fail"));
        }

        campaignFolder = new File(chizpurfleFolder.getPath() + File.separator +
                campaignTimeString);
        if (!campaignFolder.exists() || !campaignFolder.isDirectory()){
            L.d("campaign folder creation: "+(campaignFolder.mkdir()?"success":"fail"));
        }


    }

    public void startAnalysis(T test){
        long startTime = System.nanoTime();
        analyze(test);
        test.setAnalysisTime(System.nanoTime() - startTime);
    }

    public void startSave(T test){
        File testFolder = new File(campaignFolder.getPath() + File.separator +
                "test_" + String.format((Locale) null,"%010d", test.getNumericId()));
        if (testFolder.exists() && testFolder.isDirectory()){
            throw new RuntimeException("stavo per sovrascrivere un cartella!");
        }
        if (!testFolder.exists() || !testFolder.isDirectory()){
            L.d("experiment folder creation: "+(testFolder.mkdir()?"success":"fail"));
        }

        saveTestInfo(test, testFolder);

        save(test, testFolder);
    }

    public void analyzeAndSave(T test){
        startAnalysis(test);
        startSave(test);
    }

    private void saveTestInfo(T test, File testFolder) {
        try {
            JSONObject testInfo = new JSONObject();
            testInfo.put("id", test.getNumericId())
                    .put("isKiller", test.isKiller())
                    .put("execution_time", test.getExecutionTime())
                    .put("analysis_time", test.getAnalysisTime())
                    .put("elapsed_time", System.nanoTime()-creationTime);
            Utils.saveJsonToFile(testInfo, testFolder, "test_info");
        } catch (JSONException e) {
            L.e("can't save test info", e);
        }

    }

    abstract public void analyze(T test);

    abstract public void save(T test, File testFolder);

}
