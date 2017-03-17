package jp.hazuki.yuzubrowser.action.item;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.hardware.SensorManager;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.IOException;

import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.action.SingleAction;
import jp.hazuki.yuzubrowser.action.view.ActionActivity;
import jp.hazuki.yuzubrowser.utils.ArrayUtils;
import jp.hazuki.yuzubrowser.utils.DisplayUtils;
import jp.hazuki.yuzubrowser.utils.app.StartActivityInfo;
import jp.hazuki.yuzubrowser.webkit.CustomWebView;

public class WebScrollSingleAction extends SingleAction implements Parcelable {
    private static final String FIELD_NAME_TYPE = "0";
    private static final String FIELD_NAME_X = "1";
    private static final String FIELD_NAME_Y = "2";
    public static final int TYPE_FAST = 0;
    public static final int TYPE_FLING = 1;
    private int mType = TYPE_FLING;
    private int mX, mY;

    public WebScrollSingleAction(int id, JsonParser parser) throws IOException {
        super(id);

        if (parser != null) {
            if (parser.nextToken() != JsonToken.START_OBJECT) return;
            while (parser.nextToken() != JsonToken.END_OBJECT) {
                if (parser.getCurrentToken() != JsonToken.FIELD_NAME) return;
                if (FIELD_NAME_TYPE.equals(parser.getCurrentName())) {
                    if (parser.nextToken() != JsonToken.VALUE_NUMBER_INT) return;
                    mType = parser.getIntValue();
                    continue;
                }
                if (FIELD_NAME_X.equals(parser.getCurrentName())) {
                    if (parser.nextToken() != JsonToken.VALUE_NUMBER_INT) return;
                    mX = parser.getIntValue();
                    continue;
                }
                if (FIELD_NAME_Y.equals(parser.getCurrentName())) {
                    if (parser.nextToken() != JsonToken.VALUE_NUMBER_INT) return;
                    mY = parser.getIntValue();
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
        generator.writeNumberField(FIELD_NAME_TYPE, mType);
        generator.writeNumberField(FIELD_NAME_X, mX);
        generator.writeNumberField(FIELD_NAME_Y, mY);
        generator.writeEndObject();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeInt(mType);
        dest.writeInt(mX);
        dest.writeInt(mY);
    }

    protected WebScrollSingleAction(Parcel source) {
        super(source.readInt());
        mType = source.readInt();
        mX = source.readInt();
        mY = source.readInt();
    }

    public static final Creator<WebScrollSingleAction> CREATOR = new Creator<WebScrollSingleAction>() {
        @Override
        public WebScrollSingleAction createFromParcel(Parcel source) {
            return new WebScrollSingleAction(source);
        }

        @Override
        public WebScrollSingleAction[] newArray(int size) {
            return new WebScrollSingleAction[size];
        }
    };

    @Override
    public StartActivityInfo showMainPreference(ActionActivity context) {
        return showSubPreference(context);
    }

    @Override
    public StartActivityInfo showSubPreference(final ActionActivity context) {
        View view = LayoutInflater.from(context).inflate(R.layout.action_web_scroll_setting, null);
        final Spinner typeSpinner = (Spinner) view.findViewById(R.id.typeSpinner);
        final EditText editTextX = (EditText) view.findViewById(R.id.editTextX);
        final EditText editTextY = (EditText) view.findViewById(R.id.editTextY);

        Resources res = context.getResources();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, res.getStringArray(R.array.action_web_scroll_type_list));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeSpinner.setAdapter(adapter);
        final int values[] = res.getIntArray(R.array.action_web_scroll_type_values);
        int current = ArrayUtils.findIndexOfValue(mType, values);
        if (current < 0)
            current = TYPE_FLING;
        typeSpinner.setSelection(current);

        editTextX.setText(String.valueOf(mX));
        editTextY.setText(String.valueOf(mY));

        new AlertDialog.Builder(context)
                .setTitle(R.string.action_settings)
                .setView(view)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mType = values[typeSpinner.getSelectedItemPosition()];

                        int x = 0;
                        int y = 0;

                        try {
                            x = Integer.parseInt(editTextX.getText().toString());
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                        }

                        try {
                            y = Integer.parseInt(editTextY.getText().toString());
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                        }

                        if (x == 0 && y == 0) {
                            Toast.makeText(context.getApplicationContext(), R.string.action_web_scroll_x_y_zero, Toast.LENGTH_SHORT).show();
                            showSubPreference(context);
                            return;
                        }

                        mX = x;
                        mY = y;
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();

        return null;
    }

    public int getIconResourceId() {
        if (mX > 0) {
            if (mY > 0)
                return R.drawable.ic_arrow_down_right_white_24dp;
            else if (mY < 0)
                return R.drawable.ic_arrow_up_right_white_24px;
            else
                return R.drawable.ic_arrow_forward_white_24dp;
        } else if (mX < 0) {
            if (mY > 0)
                return R.drawable.ic_arrow_down_left_24dp;
            else if (mY < 0)
                return R.drawable.ic_arrow_up_left_white_24px;
            else
                return R.drawable.ic_arrow_back_white_24dp;
        } else {
            if (mY > 0)
                return R.drawable.ic_arrow_downward_white_24dp;
            else if (mY < 0)
                return R.drawable.ic_arrow_upward_white_24dp;
            else
                return -1;
        }
    }

    public void scrollWebView(Context context, CustomWebView web) {
        switch (mType) {
            case TYPE_FAST:
                web.scrollBy(mX, mY);
                break;
            case TYPE_FLING:
                web.flingScroll(makeFlingValue(context, mX), makeFlingValue(context, mY));
                break;
            default:
                throw new RuntimeException("Unknown type : " + mType);
        }
    }

    private int makeFlingValue(Context context, int d) {
        final float DECELERATION_RATE = (float) (Math.log(0.78) / Math.log(0.9));
        final float PHYSICAL_COEF = SensorManager.GRAVITY_EARTH * 39.37f * DisplayUtils.getDensity(context) * 160.0f * 0.84f;
        final float FLING_FLICTION = ViewConfiguration.getScrollFriction();
        return (int) (Integer.signum(d) * Math.round(Math.exp(Math.log(Math.abs(d) / (FLING_FLICTION * PHYSICAL_COEF)) / DECELERATION_RATE * (DECELERATION_RATE - 1.0)) / 0.35f * (FLING_FLICTION * PHYSICAL_COEF)));
    }
}
