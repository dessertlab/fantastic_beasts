package italiaken.fantasticbeasts.chizpurfle.infiltration.logcat;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by ken on 16/03/17.
 */

public class LogcatLine {

    private final String originalLine;

    private String time = "";
    private String pid = "";
    private String uid = "";
    private String level = "";
    private String process = "";
    private String msg = "";

    public LogcatLine(String line) {
        this.originalLine = line;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getProcess() {
        return process;
    }

    public void setProcess(String process) {
        this.process = process;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    @Override
    public String toString() {
        return originalLine;
    }

    public JSONObject toJson() throws JSONException {
        JSONObject jsonObject = new JSONObject();

        jsonObject.put("time", time);
        jsonObject.put("pid", pid);
        jsonObject.put("uid", uid);
        jsonObject.put("level", level);
        jsonObject.put("process", process);
        jsonObject.put("msg", msg);
        jsonObject.put("originalLine", originalLine);

        return jsonObject;
    }
}
