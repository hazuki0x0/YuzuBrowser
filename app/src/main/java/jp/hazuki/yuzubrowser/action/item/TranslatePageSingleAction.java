package jp.hazuki.yuzubrowser.action.item;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.IOException;

import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.action.SingleAction;
import jp.hazuki.yuzubrowser.action.view.ActionActivity;
import jp.hazuki.yuzubrowser.utils.ArrayUtils;
import jp.hazuki.yuzubrowser.utils.app.StartActivityInfo;

public class TranslatePageSingleAction extends SingleAction implements Parcelable {
    private static final String FIELD_NAME_FROM = "0";
    private static final String FIELD_NAME_TO = "1";
    private String mFrom;
    private String mTo;

    public TranslatePageSingleAction(int id, JsonParser parser) throws IOException {
        super(id);

        if (parser != null) {
            if (parser.nextToken() != JsonToken.START_OBJECT) return;
            while (parser.nextToken() != JsonToken.END_OBJECT) {
                if (parser.getCurrentToken() != JsonToken.FIELD_NAME) return;
                if (FIELD_NAME_FROM.equals(parser.getCurrentName())) {
                    switch (parser.nextToken()) {
                        case VALUE_STRING:
                            mFrom = parser.getText();
                            break;
                        case VALUE_NULL:
                            break;
                    }
                    continue;
                }
                if (FIELD_NAME_TO.equals(parser.getCurrentName())) {
                    switch (parser.nextToken()) {
                        case VALUE_STRING:
                            mTo = parser.getText();
                            break;
                        case VALUE_NULL:
                            break;
                    }
                    continue;
                }
                parser.skipChildren();
            }
        }
    }

    @Override
    public void writeIdAndData(JsonGenerator generator) throws IOException {
        generator.writeNumber(id);
        generator.writeStartObject();
        generator.writeStringField(FIELD_NAME_FROM, mFrom);
        generator.writeStringField(FIELD_NAME_TO, mTo);
        generator.writeEndObject();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(mFrom);
        dest.writeString(mTo);
    }

    protected TranslatePageSingleAction(Parcel source) {
        super(source.readInt());
        mFrom = source.readString();
        mTo = source.readString();
    }

    public static final Creator<TranslatePageSingleAction> CREATOR = new Creator<TranslatePageSingleAction>() {
        @Override
        public TranslatePageSingleAction createFromParcel(Parcel source) {
            return new TranslatePageSingleAction(source);
        }

        @Override
        public TranslatePageSingleAction[] newArray(int size) {
            return new TranslatePageSingleAction[size];
        }
    };

    @Override
    public StartActivityInfo showMainPreference(ActionActivity context) {
        return showSubPreference(context);
    }

    @Override
    public StartActivityInfo showSubPreference(ActionActivity context) {
        View view = LayoutInflater.from(context).inflate(R.layout.action_translate_page_setting, null);
        final Spinner fromSpinner = (Spinner) view.findViewById(R.id.fromSpinner);
        final Spinner toSpinner = (Spinner) view.findViewById(R.id.toSpinner);

        String list[] = context.getResources().getStringArray(R.array.translate_language_list);
        final String values[] = context.getResources().getStringArray(R.array.translate_language_values);
        int length = list.length;

        String fromList[] = new String[length + 1];
        fromList[0] = context.getString(R.string.action_translate_auto_select);
        System.arraycopy(list, 0, fromList, 1, length);
        ArrayAdapter<String> from_adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, fromList);
        from_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fromSpinner.setAdapter(from_adapter);

        String toList[] = new String[length + 1];
        toList[0] = context.getString(R.string.action_translate_select_each);
        System.arraycopy(list, 0, toList, 1, length);
        ArrayAdapter<String> to_adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, toList);
        to_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        toSpinner.setAdapter(to_adapter);

        int index;
        index = ArrayUtils.findIndexOfValue(mFrom, values) + 1;
        fromSpinner.setSelection(index);
        index = ArrayUtils.findIndexOfValue(mTo, values) + 1;
        toSpinner.setSelection(index);

        new AlertDialog.Builder(context)
                .setTitle(R.string.action_settings)
                .setView(view)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int position;
                        position = fromSpinner.getSelectedItemPosition();
                        mFrom = (position == 0) ? "auto" : values[position - 1];
                        position = toSpinner.getSelectedItemPosition();
                        mTo = (position == 0) ? null : values[position - 1];
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();

        return null;
    }

    public String getTranslateFrom() {
        return mFrom;
    }

    public String getTranslateTo() {
        return mTo;
    }
}
