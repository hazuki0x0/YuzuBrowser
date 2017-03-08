package jp.hazuki.yuzubrowser.useragent;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * Created by hazuki on 17/01/19.
 */

public class UserAgent implements Serializable {
    @JsonProperty("0")
    public String useragent;
    @JsonProperty("1")
    public String name;

    public UserAgent() {
    }

    public UserAgent(String name, String useragent) {
        this.name = name;
        this.useragent = useragent;
    }
}
