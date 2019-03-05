package italiaken.fantasticbeasts.chizpurfle.infiltration.logcat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import italiaken.fantasticbeasts.chizpurfle.L;

/**
 * Created by ken on 10/07/15.
 */
public class LongLogcatReader extends Reader {

    private BufferedReader in = null;


    LongLogcatReader(BufferedReader bufferedReader) {
        super(bufferedReader);
        this.in = bufferedReader;
    }

    @Override
    public void close() throws IOException {
        in.close();
        in = null;
    }

    @Override
    public int read(char[] buffer, int offset, int count) throws IOException {
        return 0;
    }

    public List<String> readAll() throws IOException {
        List<String> result = new ArrayList<>();

        String line = readLine();
        while (line != null){
            result.add(line);
            line = readLine();
        }

        return result;
    }

    List<LogcatLine> readAllLogcat() throws IOException {
        List<LogcatLine> result = new ArrayList<>();

        LogcatLine line = readLogcatLine();
        while (line != null){
            result.add(line);
            line = readLogcatLine();
        }

        return result;
    }

    private String readLine() throws IOException {
        String line;
        StringBuilder builder = new StringBuilder();

        while((line = in.readLine()) != null){
            if(line.equals("") || line.startsWith("---")){
                if (builder.length() == 0)
                    continue;
                else
                    return builder.toString();
            }
            builder.append(line);
        }

        return null;

    }

    private LogcatLine readLogcatLine() throws IOException {

        String line = readLine();
        if (line == null)
            return null;

        Pattern pattern = Pattern.compile(
                "\\[\\s*(\\S*\\s*\\S*)\\s*(\\d*):\\s*(\\d*)\\s*([VAFEWID])\\/(\\S*)\\s*\\](.*)");
        Matcher matcher = pattern.matcher(line);
        if (matcher.matches()) {
            LogcatLine logcatLine = new LogcatLine(line);
            logcatLine.setTime(matcher.group(1));
            logcatLine.setPid(matcher.group(2));
            logcatLine.setUid(matcher.group(3));
            logcatLine.setLevel(matcher.group(4));
            logcatLine.setProcess(matcher.group(5));
            logcatLine.setMsg(matcher.group(6));
            return logcatLine;
        } else {
            L.w("*UNMATCHED LOGCAT LINE*"+line);
            return new LogcatLine(line);
        }

    }


}
