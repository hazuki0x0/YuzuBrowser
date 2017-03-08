package jp.hazuki.yuzubrowser.action.item;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Environment;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.File;
import java.io.IOException;

import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.action.SingleAction;
import jp.hazuki.yuzubrowser.action.view.ActionActivity;
import jp.hazuki.yuzubrowser.utils.app.StartActivityInfo;

public class SaveScreenshotSingleAction extends SingleAction implements Parcelable {
    private static final String FIELD_NAME_SS_TYPE = "0";
    private static final String FIELD_NAME_SAVE_FOLDER = "1";
    public static final int SS_TYPE_ALL = 0;
    public static final int SS_TYPE_PART = 1;
    private int mSsType = SS_TYPE_PART;
    private File mPath = new File(Environment.getExternalStorageDirectory() + File.separator + "mikanbrowser" + File.separator + "screenshot" + File.separator);

    public SaveScreenshotSingleAction(int id, JsonParser parser) throws IOException {
        super(id);

        if (parser != null) {
            if (parser.nextToken() != JsonToken.START_OBJECT) return;
            while (parser.nextToken() != JsonToken.END_OBJECT) {
                if (parser.getCurrentToken() != JsonToken.FIELD_NAME) return;
                if (FIELD_NAME_SS_TYPE.equals(parser.getCurrentName())) {
                    if (parser.nextToken() != JsonToken.VALUE_NUMBER_INT) return;
                    mSsType = parser.getIntValue();
                    continue;
                }
                if (FIELD_NAME_SAVE_FOLDER.equals(parser.getCurrentName())) {
                    if (parser.nextToken() != JsonToken.VALUE_STRING) return;
                    mPath = new File(parser.getText());
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
        generator.writeNumberField(FIELD_NAME_SS_TYPE, mSsType);
        generator.writeStringField(FIELD_NAME_SAVE_FOLDER, mPath.getAbsolutePath());
        generator.writeEndObject();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeInt(mSsType);
        dest.writeString(mPath.getAbsolutePath());
    }

    protected SaveScreenshotSingleAction(Parcel source) {
        super(source.readInt());
        mSsType = source.readInt();
        mPath = new File(source.readString());
    }

    public static final Creator<SaveScreenshotSingleAction> CREATOR = new Creator<SaveScreenshotSingleAction>() {
        @Override
        public SaveScreenshotSingleAction createFromParcel(Parcel source) {
            return new SaveScreenshotSingleAction(source);
        }

        @Override
        public SaveScreenshotSingleAction[] newArray(int size) {
            return new SaveScreenshotSingleAction[size];
        }
    };

    @Override
    public StartActivityInfo showSubPreference(final ActionActivity context) {
        View view = LayoutInflater.from(context).inflate(R.layout.action_screenshot_settings, null);
        final CheckBox captureAllCheckBox = (CheckBox) view.findViewById(R.id.captureAllCheckBox);

        captureAllCheckBox.setChecked(mSsType == SS_TYPE_ALL);

        final EditText folderEditText = (EditText) view.findViewById(R.id.folderEditText);
        folderEditText.setText(mPath.getAbsolutePath());

        new AlertDialog.Builder(context)
                .setTitle(R.string.action_settings)
                .setView(view)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (captureAllCheckBox.isChecked())
                            mSsType = SS_TYPE_ALL;
                        else
                            mSsType = SS_TYPE_PART;

                        //mPath = folderButton.getCurrentFolder();

                        mPath = new File(folderEditText.getText().toString());
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();

        return null;
    }

    public int getType() {
        return mSsType;
    }

    public File getFolder() {
        return mPath;
    }
}
