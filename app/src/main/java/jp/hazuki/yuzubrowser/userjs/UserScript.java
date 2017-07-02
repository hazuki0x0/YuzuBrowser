package jp.hazuki.yuzubrowser.userjs;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jp.hazuki.yuzubrowser.utils.ErrorReport;
import jp.hazuki.yuzubrowser.utils.Logger;
import jp.hazuki.yuzubrowser.utils.WebUtils;

public class UserScript implements Parcelable {
    private static final String TAG = "UserScript";

    private final UserScriptInfo info;

    private String name;
    private String version;
    private String author;
    private String description;
    private List<Pattern> include;
    private List<Pattern> exclude;
    private boolean unwrap;
    private boolean runStart;
    public UserScript() {
        info = new UserScriptInfo();
    }

    public UserScript(long id, String data, boolean enabled) {
        info = new UserScriptInfo(id, data, enabled);
        loadData();
    }

    public UserScript(String data) {
        info = new UserScriptInfo(data);
        loadData();
    }

    public UserScript(UserScriptInfo info) {
        this.info = info;
        loadData();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(info.getId());
        dest.writeString(info.getData());
        dest.writeInt(info.isEnabled() ? 1 : 0);
    }

    public UserScript(Parcel source) {
        long id = source.readLong();
        String data = source.readString();
        boolean enabled = source.readInt() == 1;
        info = new UserScriptInfo(id, data, enabled);
        loadData();
    }

    public static final Creator<UserScript> CREATOR = new Creator<UserScript>() {
        @Override
        public UserScript createFromParcel(Parcel source) {
            return new UserScript(source);
        }

        @Override
        public UserScript[] newArray(int size) {
            return new UserScript[size];
        }
    };

    public long getId() {
        return info.getId();
    }

    public void setId(long id) {
        info.setId(id);
    }

    public String getData() {
        return info.getData();
    }

    public String getRunnable() {
        if (unwrap) {
            return info.getData();
        } else {
            return "(function() {\n" + info.getData() + "\n})()";
        }
    }

    public void setData(String data) {
        info.setData(data);
        loadData();
    }

    public boolean isEnabled() {
        return info.isEnabled();
    }

    public void setEnabled(boolean enabled) {
        info.setEnabled(enabled);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Pattern> getInclude() {
        return include;
    }

    public void setInclude(List<Pattern> include) {
        this.include = include;
    }

    public List<Pattern> getExclude() {
        return exclude;
    }

    public void setExclude(List<Pattern> exclude) {
        this.exclude = exclude;
    }

    public boolean isUnwrap() {
        return unwrap;
    }

    public boolean isRunStart() {
        return runStart;
    }

    public void setRunStart(boolean runStart) {
        this.runStart = runStart;
    }

    private void loadData() {
        name = null;
        version = null;
        description = null;
        include = null;
        exclude = null;

        try {
            BufferedReader reader = new BufferedReader(new StringReader(info.getData()));
            String line;

            if ((line = reader.readLine()) == null || !sHeaderStartPattern.matcher(line).matches()) {
                Logger.w(TAG, "Header (start) parser error");
                return;
            }

            while ((line = reader.readLine()) != null) {
                Matcher matcher = sHeaderMainPattern.matcher(line);
                if (!matcher.matches()) {
                    if (sHeaderEndPattern.matcher(line).matches()) {
                        return;
                    }
                    Logger.w(TAG, "Unknown header : " + line);
                } else {
                    String field = matcher.group(1);
                    String value = matcher.group(2);
                    readData(field, value, line);
                }
            }

            Logger.w(TAG, "Header (end) parser error");
        } catch (IOException e) {
            ErrorReport.printAndWriteLog(e);
        }
    }

    protected void readData(String field, String value, String line) {
        if ("name".equalsIgnoreCase(field)) {
            name = value;
        } else if ("version".equalsIgnoreCase(field)) {
            version = value;
        } else if ("author".equalsIgnoreCase(field)) {
            author = value;
        } else if ("description".equalsIgnoreCase(field)) {
            description = value;
        } else if ("include".equalsIgnoreCase(field)) {
            if (include == null)
                include = new ArrayList<>();
            Pattern pattern = WebUtils.makeUrlPattern(value);
            if (pattern != null)
                include.add(pattern);
        } else if ("exclude".equalsIgnoreCase(field)) {
            if (exclude == null)
                exclude = new ArrayList<>();
            Pattern pattern = WebUtils.makeUrlPattern(value);
            if (pattern != null)
                exclude.add(pattern);
        } else if ("unwrap".equalsIgnoreCase(field)) {
            unwrap = true;
        } else if ("run-at".equalsIgnoreCase(field)) {
            runStart = "document-start".equalsIgnoreCase(value);
        } else if ("match".equalsIgnoreCase(field)) {
            if (include == null)
                include = new ArrayList<>();
            String pattern_url = "?^" + value.replace("?", "\\?").replace(".", "\\.")
                    .replace("*", ".*").replace("+", ".+")
                    .replace("://.*\\.", "://((?![\\./]).)*\\.").replaceAll("^\\.\\*://", "https?://");
            Pattern pattern = WebUtils.makeUrlPattern(pattern_url);
            if (pattern != null)
                include.add(pattern);
        } else {
            Logger.w(TAG, "Unknown header : " + line);
        }
    }

    public UserScriptInfo getInfo() {
        return info;
    }

    private static final Pattern sHeaderStartPattern = Pattern.compile("\\s*//\\s*==UserScript==\\s*", Pattern.CASE_INSENSITIVE);
    private static final Pattern sHeaderEndPattern = Pattern.compile("\\s*//\\s*==/UserScript==\\s*", Pattern.CASE_INSENSITIVE);
    private static final Pattern sHeaderMainPattern = Pattern.compile("\\s*//\\s*@(\\S+)(?:\\s+(.*))?", Pattern.CASE_INSENSITIVE);
}
