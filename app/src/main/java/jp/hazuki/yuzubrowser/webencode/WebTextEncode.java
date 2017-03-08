package jp.hazuki.yuzubrowser.webencode;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * Created by hazuki on 17/01/20.
 */

public class WebTextEncode implements Serializable {
    @JsonProperty("0")
    public String encoding;

    public WebTextEncode() {
    }

    public WebTextEncode(String encoding) {
        this.encoding = encoding;
    }
}
