package jp.hazuki.yuzubrowser.legacy.utils.view;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AlertDialog;
import android.view.View;

import jp.hazuki.yuzubrowser.legacy.settings.preference.common.SeekBarPreferenceController;

public class SeekBarDialog {
    private final SeekBarPreferenceController mController;
    private final AlertDialog.Builder mBuilder;

    public SeekBarDialog(Context context) {
        mBuilder = new AlertDialog.Builder(context);
        mController = new SeekBarPreferenceController(context);
    }

    public SeekBarDialog setSeekMin(int i) {
        mController.setSeekMin(i);
        return this;
    }

    public SeekBarDialog setSeekMax(int i) {
        mController.setSeekMax(i);
        return this;
    }

    public SeekBarDialog setValue(int value) {
        mController.setValue(value);
        return this;
    }

    public interface OnClickListener {
        void onClick(DialogInterface dialog, int which, int value);
    }

    public SeekBarDialog setPositiveButton(int textId, final OnClickListener listener) {
        mBuilder.setPositiveButton(textId, (listener == null) ? null :
                (DialogInterface.OnClickListener) (dialog, which) -> listener.onClick(dialog, which, mController.getCurrentValue()));
        return this;
    }

    public SeekBarDialog setPositiveButton(CharSequence text, final OnClickListener listener) {
        mBuilder.setPositiveButton(text, (listener == null) ? null : new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                listener.onClick(dialog, which, mController.getCurrentValue());
            }
        });
        return this;
    }

    public SeekBarDialog setNegativeButton(int textId, final OnClickListener listener) {
        mBuilder.setNegativeButton(textId, (listener == null) ? null : new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                listener.onClick(dialog, which, mController.getCurrentValue());
            }
        });
        return this;
    }

    public SeekBarDialog setNegativeButton(CharSequence text, final OnClickListener listener) {
        mBuilder.setNegativeButton(text, (listener == null) ? null : new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                listener.onClick(dialog, which, mController.getCurrentValue());
            }
        });
        return this;
    }

    public SeekBarDialog setNeutralButton(int textId, final OnClickListener listener) {
        mBuilder.setNeutralButton(textId, (listener == null) ? null : new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                listener.onClick(dialog, which, mController.getCurrentValue());
            }
        });
        return this;
    }

    public SeekBarDialog setNeutralButton(CharSequence text, final OnClickListener listener) {
        mBuilder.setNeutralButton(text, (listener == null) ? null : new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                listener.onClick(dialog, which, mController.getCurrentValue());
            }
        });
        return this;
    }

    public SeekBarDialog setTitle(int titleId) {
        mBuilder.setTitle(titleId);
        return this;
    }

    public SeekBarDialog setTitle(CharSequence title) {
        mBuilder.setTitle(title);
        return this;
    }

    public SeekBarDialog setCustomTitle(View customTitleView) {
        mBuilder.setCustomTitle(customTitleView);
        return this;
    }

    public SeekBarDialog setIcon(int iconId) {
        mBuilder.setIcon(iconId);
        return this;
    }

    public SeekBarDialog setIcon(Drawable icon) {
        mBuilder.setIcon(icon);
        return this;
    }

    public SeekBarDialog setCancelable(boolean cancelable) {
        mBuilder.setCancelable(cancelable);
        return this;
    }

    public SeekBarDialog setOnCancelListener(OnCancelListener onCancelListener) {
        mBuilder.setOnCancelListener(onCancelListener);
        return this;
    }

    public AlertDialog create() {
        mController.onPrepareDialogBuilder(mBuilder);
        return mBuilder.create();
    }

    public AlertDialog show() {
        AlertDialog dialog = create();
        dialog.show();
        return dialog;
    }
}
