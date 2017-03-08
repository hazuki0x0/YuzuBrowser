package jp.hazuki.yuzubrowser.webencode;

import android.content.Context;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import jp.hazuki.yuzubrowser.utils.ErrorReport;

/**
 * Created by hazuki on 17/01/20.
 */

public class WebTextEncodeList extends ArrayList<WebTextEncode> {
    private static final long serialVersionUID = -5725369528478732443L;
    private static final String FILENAME = "webencodelist_1.dat";
    private static final String FIELD_ENCODING = "0";

    public boolean read(Context context) {
        clear();

        File file = context.getFileStreamPath(FILENAME);

        if (file == null || !file.exists() || file.isDirectory()) return true;

        try (InputStream is = new BufferedInputStream(new FileInputStream(file))) {
            List<WebTextEncode> encodes = new ObjectMapper().readValue(is, new TypeReference<List<WebTextEncode>>() {
            });
            addAll(encodes);
            return true;
        } catch (IOException e) {
            ErrorReport.printAndWriteLog(e);
        }
        return false;
    }

    public boolean write(Context context) {
        File file = context.getFileStreamPath(FILENAME);

        try (OutputStream os = new BufferedOutputStream(new FileOutputStream(file))) {

            new ObjectMapper().writeValue(os, this);
            return true;
        } catch (IOException e) {
            ErrorReport.printAndWriteLog(e);
        }
        return false;
    }
}
