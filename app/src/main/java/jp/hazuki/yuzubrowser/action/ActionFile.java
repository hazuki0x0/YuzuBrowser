package jp.hazuki.yuzubrowser.action;

import android.content.Context;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;

import jp.hazuki.yuzubrowser.utils.ErrorReport;
import jp.hazuki.yuzubrowser.utils.Logger;

public abstract class ActionFile implements Serializable {
    private static final long serialVersionUID = 9159377694255234638L;

    private static final String TAG = "ActionFile";

    public abstract File getFile(Context context);

    public abstract void reset();

    public boolean load(Context context) {
        reset();

        File file = getFile(context);
        if (!file.exists() || file.isDirectory()) return true;

        try (InputStream is = new BufferedInputStream(new FileInputStream(file))) {

            JsonFactory factory = new JsonFactory();
            JsonParser parser = factory.createParser(is);

            if (!load(parser)) {
                Logger.e(TAG, "loadMain error (return false)");
                parser.close();
                return false;
            }

            parser.close();
            return true;
        } catch (IOException e) {
            ErrorReport.printAndWriteLog(e);
        }
        return false;
    }

    public abstract boolean load(JsonParser parser) throws IOException;

    public boolean write(Context context) {
        File file = getFile(context);

        try (OutputStream os = new BufferedOutputStream(new FileOutputStream(file))) {

            JsonFactory factory = new JsonFactory();
            JsonGenerator generator = factory.createGenerator(os);

            if (!write(generator)) {
                Logger.e(TAG, "writeMain error (return false)");
                generator.close();
                return false;
            }

            generator.close();
            return true;
        } catch (IOException e) {
            ErrorReport.printAndWriteLog(e);
        }
        return false;
    }

    public abstract boolean write(JsonGenerator generator) throws IOException;
}
