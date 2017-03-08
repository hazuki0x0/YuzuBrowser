package jp.hazuki.yuzubrowser.utils;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.widget.Toast;

import jp.hazuki.yuzubrowser.R;

public class ClipboardUtils {
    private ClipboardUtils() {
        throw new UnsupportedOperationException();
    }

    public static void setClipboardText(Context context, String txt) {
        if (txt != null) {
            ClipData.Item clipItem = new ClipData.Item(txt);
            String[] mineType = {ClipDescription.MIMETYPE_TEXT_PLAIN};
            ClipData clipData = new ClipData("text_data", mineType, clipItem);
            ClipboardManager manager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            manager.setPrimaryClip(clipData);
            Toast.makeText(context, context.getResources().getString(R.string.copy_clipboard_mes_before) + txt, Toast.LENGTH_SHORT).show();
        }
    }

    public static String getClipboardText(Context context) {
        ClipboardManager manager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = manager.getPrimaryClip();

        if (clipData != null) {
            ClipData.Item clipItem = clipData.getItemAt(0);
            CharSequence txt = clipItem.getText();

            return (txt == null) ? "" : txt.toString();
        }

        return "";
    }
}
