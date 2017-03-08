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

public class Action extends ArrayList<SingleAction> implements Parcelable, JsonConvertable {
    private static final long serialVersionUID = 1712925333386047748L;

    public Action() {
        super(1);
    }

    public Action(Action action) {
        super(action);
    }

    public Action(SingleAction action) {
        super(1);
        add(action);
    }

    public Action(String jsonstr) {
        super(1);
        fromJsonString(jsonstr);
    }

    public Action(Parcel source) {
        super();
        source.readList(this, SingleAction.class.getClassLoader());
    }

    public static final Creator<Action> CREATOR = new Creator<Action>() {
        @Override
        public Action createFromParcel(Parcel source) {
            return new Action(source);
        }

        @Override
        public Action[] newArray(int size) {
            return new Action[size];
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

    public void writeAction(String field_name, JsonGenerator generator) throws IOException {
        generator.writeFieldName(field_name);
        writeAction(generator);
    }

    public void writeAction(JsonGenerator generator) throws IOException {
        generator.writeStartArray();
        for (SingleAction action : this) {
            generator.writeStartArray();
            action.writeIdAndData(generator);
            generator.writeEndArray();
        }
        generator.writeEndArray();
    }

    public boolean loadAction(JsonParser parser) throws IOException {
        if (parser.nextToken() != JsonToken.START_ARRAY) return false;
        while (parser.nextToken() != JsonToken.END_ARRAY) {
            if (parser.getCurrentToken() != JsonToken.START_ARRAY) return false;

            parser.nextToken();
            if (parser.getCurrentToken() == JsonToken.VALUE_NUMBER_INT) {
                int id = parser.getIntValue();

                //in makeInstance, should use getCurrentToken
                SingleAction action;
                action = SingleAction.makeInstance(id, parser);
                //parser.skipChildren();
                if (parser.nextToken() != JsonToken.END_ARRAY) return false;
                if (action != null) add(action);
            } else if (parser.getCurrentToken() != JsonToken.END_ARRAY) {
                return false;
            }
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

    public static Action makeInstance(int id) {
        return new Action(SingleAction.makeInstance(id));
    }

    public String toString(ActionNameArray nameArray) {
        if (isEmpty())
            return null;
        return get(0).toString(nameArray);
    }
}
