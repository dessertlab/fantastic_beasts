package italiaken.fantasticbeasts.chizpurfle;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ken on 28/11/17 for fantastic_beasts
 */

public class Utils {

    public static void saveJsonToFile(JSONObject json, File folder, String fileName) {

        try {
            if (!folder.exists())
                throw new IOException("folder "+folder.getName()+" does not exist");

            File file = new File(folder.getPath() + File.separator + fileName + ".json");
            if (!file.exists()){
                L.d("create new file "+fileName+": "+(file.createNewFile()?"success":"fail"));
            }

            try (FileWriter fileWriter = new FileWriter(file)) {
                if (json == null)
                    fileWriter.write("check chizpurfle logs... " +
                            "something went wrong... " +
                            "json is null!\n");
                else
                    fileWriter.write(json.toString(4)+"\n");
            } catch (IOException e) {
                L.e("some problem writing info on file", e);
            } catch (JSONException e) {
                L.e("some problem creating info json", e);
            }

        } catch (IOException e) {
            L.e(e.getMessage(), e);
        }

    }


    public static Map<String, String> readCSVMapFromFile(String s) {

        Map<String, String> result = new HashMap<>();

        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(s))){

            String sCurrentLine;

            while ((sCurrentLine = bufferedReader.readLine()) != null) {
                String[] splits = sCurrentLine.split(",");
                result.put(splits[0], splits[1]);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;

    }
}
