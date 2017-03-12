package jp.hazuki.yuzubrowser.action.item;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.IOException;

import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.action.SingleAction;
import jp.hazuki.yuzubrowser.action.view.ActionActivity;
import jp.hazuki.yuzubrowser.browser.BrowserManager;
import jp.hazuki.yuzubrowser.utils.ArrayUtils;
import jp.hazuki.yuzubrowser.utils.app.StartActivityInfo;

public class OpenUrlSingleAction extends SingleAction implements Parcelable {
    private static final String FIELD_NAME_URL = "0";
    private static final String FIELD_NAME_TARGET_TAB = "1";
    private String mUrl = "";
    private int mTargetTab = BrowserManager.LOAD_URL_TAB_CURRENT;

    public OpenUrlSingleAction(int id, JsonParser parser) throws IOException {
        super(id);

        if (parser != null) {
            if (parser.nextToken() != JsonToken.START_OBJECT) return;
            while (parser.nextToken() != JsonToken.END_OBJECT) {
                if (parser.getCurrentToken() != JsonToken.FIELD_NAME) return;
                if (FIELD_NAME_URL.equals(parser.getCurrentName())) {
                    if (parser.nextToken() != JsonToken.VALUE_STRING) return;
                    mUrl = parser.getText();
                    continue;
                }
                if (FIELD_NAME_TARGET_TAB.equals(parser.getCurrentName())) {
                    if (parser.nextToken() != JsonToken.VALUE_NUMBER_INT) return;
                    mTargetTab = parser.getIntValue();
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
        generator.writeStringField(FIELD_NAME_URL, mUrl);
        generator.writeNumberField(FIELD_NAME_TARGET_TAB, mTargetTab);
        generator.writeEndObject();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(mUrl);
        dest.writeInt(mTargetTab);
    }

    protected OpenUrlSingleAction(Parcel source) {
        super(source.readInt());
        mUrl = source.readString();
        mTargetTab = source.readInt();
    }

    public static final Creator<OpenUrlSingleAction> CREATOR = new Creator<OpenUrlSingleAction>() {
        @Override
        public OpenUrlSingleAction createFromParcel(Parcel source) {
            return new OpenUrlSingleAction(source);
        }

        @Override
        public OpenUrlSingleAction[] newArray(int size) {
            return new OpenUrlSingleAction[size];
        }
    };

    @Override
    public StartActivityInfo showMainPreference(ActionActivity context) {
        return showSubPreference(context);
    }

    @Override
    public StartActivityInfo showSubPreference(ActionActivity context) {
        View view = LayoutInflater.from(context).inflate(R.layout.action_open_url_setting, null);
        final EditText urlEditText = (EditText) view.findViewById(R.id.urlEditText);
        final Spinner tabSpinner = (Spinner) view.findViewById(R.id.tabSpinner);

        urlEditText.setText(mUrl);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, context.getResources().getStringArray(R.array.pref_newtab_list));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        tabSpinner.setAdapter(adapter);
        final int targetValues[] = context.getResources().getIntArray(R.array.pref_newtab_values);
        tabSpinner.setSelection(ArrayUtils.findIndexOfValue(mTargetTab, targetValues));

        new AlertDialog.Builder(context)
                .setTitle(R.string.action_settings)
                .setView(view)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mUrl = urlEditText.getText().toString();
                        mTargetTab = targetValues[tabSpinner.getSelectedItemPosition()];
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();

        return null;
    }

    public String getUrl() {
        return mUrl;
    }

    public int getTargetTab() {
        return mTargetTab;
    }
}
