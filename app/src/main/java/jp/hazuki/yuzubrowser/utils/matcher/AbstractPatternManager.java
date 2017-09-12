package jp.hazuki.yuzubrowser.utils.matcher;

import android.content.Context;
import android.widget.Toast;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.regex.PatternSyntaxException;

import jp.hazuki.yuzubrowser.utils.ErrorReport;
import jp.hazuki.yuzubrowser.utils.JsonUtils;

public abstract class AbstractPatternManager<T extends AbstractPatternChecker<?>> {
    private final File mFile;
    private final ArrayList<T> mList = new ArrayList<>();

    public AbstractPatternManager(Context context, File file) {
        mFile = file;
        load(context);
    }

    public ArrayList<T> getList() {
        return mList;
    }

    public T remove(int index) {
        return mList.remove(index);
    }

    public T get(int index) {
        return mList.get(index);
    }

    public int getIndex(T object) {
        return mList.indexOf(object);
    }

    public void add(T object) {
        mList.add(object);
    }

    public void add(int index, T object) {
        mList.add(index, object);
    }

    public void set(T from, T to) {
        mList.set(mList.indexOf(from), to);
    }

    public void set(int id, T to) {
        mList.set(id, to);
    }

    protected abstract T newInstance(JsonParser parser) throws IOException;

    public boolean load(Context context) {
        mList.clear();

        if (!mFile.exists() || !mFile.isFile())
            return true;

        try (InputStream is = new BufferedInputStream(new FileInputStream(mFile));
             JsonParser parser = JsonUtils.getFactory().createParser(is)) {

            if (parser.nextToken() != JsonToken.START_ARRAY) return false;
            while (parser.nextToken() != JsonToken.END_ARRAY) {
                if (parser.getCurrentToken() != JsonToken.START_ARRAY) return false;
                mList.add(newInstance(parser));
                if (parser.nextToken() != JsonToken.END_ARRAY) return false;
            }
            return true;
        } catch (PatternSyntaxException | IOException e) {
            ErrorReport.printAndWriteLog(e);
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
        }
        return false;
    }

    public boolean save(Context context) {
        try (OutputStream os = new BufferedOutputStream(new FileOutputStream(mFile));
             JsonGenerator generator = JsonUtils.getFactory().createGenerator(os)) {

            generator.writeStartArray();
            for (T item : mList) {
                if (item != null) {
                    generator.writeStartArray();
                    item.write(generator);
                    generator.writeEndArray();
                }
            }
            generator.writeEndArray();

            return true;
        } catch (IOException e) {
            ErrorReport.printAndWriteLog(e);
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
        }
        return false;
    }
}
