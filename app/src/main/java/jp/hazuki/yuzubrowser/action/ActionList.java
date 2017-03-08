package jp.hazuki.yuzubrowser.action;

import android.os.Parcel;
import android.os.Parcelable;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;

import jp.hazuki.yuzubrowser.utils.ErrorReport;
import jp.hazuki.yuzubrowser.utils.util.JsonConvertable;

public class ActionList extends ArrayList<Action> implements Parcelable, JsonConvertable {
    private static final long serialVersionUID = 4454998466204378989L;

    public ActionList() {
        super();
    }

    public ActionList(String jsonStr) {
        super();
        fromJsonString(jsonStr);
    }

    public ActionList(Parcel source) {
        super();
        source.readList(this, Action.class.getClassLoader());
    }

    public static final Creator<ActionList> CREATOR = new Creator<ActionList>() {
        @Override
        public ActionList createFromParcel(Parcel source) {
            return new ActionList(source);
        }

        @Override
        public ActionList[] newArray(int size) {
            return new ActionList[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeList(this);
    }

    public boolean add(SingleAction object) {
        return add(new Action(object));
    }

    public void writeAction(String field_name, JsonGenerator generator) throws IOException {
        generator.writeFieldName(field_name);
        writeAction(generator);
    }

    public void writeAction(JsonGenerator generator) throws IOException {
        generator.writeStartArray();
        for (Action action : this) {
            action.writeAction(generator);
        }
        generator.writeEndArray();
    }

    public boolean loadAction(JsonParser parser) throws IOException {
        if (parser.nextToken() != JsonToken.START_ARRAY) return false;
        for (; ; ) {
            Action action = new Action();
            if (!action.loadAction(parser)) {
                if (parser.getCurrentToken() == JsonToken.END_ARRAY)
                    break;
                else
                    return false;
            }
            add(action);
        }
        return true;
    }

    @Override
    public String toJsonString() {
        StringWriter writer = null;
        try {
            writer = new StringWriter();
            JsonFactory factory = new JsonFactory();
            JsonGenerator generator = factory.createGenerator(writer);

            writeAction(generator);

            generator.close();
            return writer.toString();
        } catch (IOException e) {
            ErrorReport.printAndWriteLog(e);
        } finally {
            if (writer != null)
                try {
                    writer.close();
                } catch (IOException e) {
                    ErrorReport.printAndWriteLog(e);
                }
        }
        return null;
    }

    @Override
    public boolean fromJsonString(String str) {
        clear();

        StringReader reader = null;
        try {
            reader = new StringReader(str);
            JsonFactory factory = new JsonFactory();
            JsonParser parser = factory.createParser(reader);

            boolean ret = loadAction(parser);

            parser.close();
            return ret;
        } catch (IOException e) {
            ErrorReport.printAndWriteLog(e);
        } finally {
            if (reader != null)
                reader.close();
        }
        return false;
    }
}
