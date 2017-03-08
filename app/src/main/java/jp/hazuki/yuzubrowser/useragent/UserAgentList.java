package jp.hazuki.yuzubrowser.useragent;

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
 * Created by hazuki on 17/01/19.
 */

public class UserAgentList extends ArrayList<UserAgent> {
    private static final String FILENAME = "ualist_1.dat";

    public boolean read(Context context) {
        clear();

        File file = context.getFileStreamPath(FILENAME);

        if (file == null || !file.exists() || file.isDirectory()) return true;
        try (InputStream is = new BufferedInputStream(new FileInputStream(file))) {

            List<UserAgent> userAgents = new ObjectMapper().readValue(is, new TypeReference<List<UserAgent>>() {
            });
            addAll(userAgents);

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
