package jp.hazuki.yuzubrowser.legacy.gesture;

import android.content.Context;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.Prediction;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import jp.hazuki.yuzubrowser.legacy.action.Action;
import jp.hazuki.yuzubrowser.legacy.action.ActionDatabase;
import jp.hazuki.yuzubrowser.legacy.utils.Logger;

public abstract class GestureManager {
    public static final String INTENT_EXTRA_GESTURE_ID = "GestureManager.extra.GESTURE_ID";
    private static final String TAG = "GestureManager";
    public static final int GESTURE_TYPE_WEB = 0;
    public static final int GESTURE_TYPE_SUB = 1;

    private final GestureLibrary mLibrary;
    private final ActionDatabase mDatabase;

    protected GestureManager(Context context, int id) {
        File dir = context.getDir("gestures1", Context.MODE_PRIVATE);
        mLibrary = GestureLibraries.fromFile(new File(dir, id + ".lib.dat"));
        mDatabase = new ActionDatabase(context, new File(dir, id + ".list.dat"));

        if (!load()) {
            Logger.e(TAG, "load error at constructor");
        }
    }

    public boolean load() {
        return mLibrary.load();
    }

    protected boolean save() {
        return mLibrary.save();
    }

    public boolean add(Gesture gesture, Action action) {
        long id = mDatabase.add(action);
        if (id < 0) {
            Logger.e(TAG, "Database add error");
            return false;
        }
        mLibrary.addGesture(String.valueOf(id), gesture);
        if (!save()) {
            Logger.e(TAG, "save error at add");
            return false;
        }
        return true;
    }

    public boolean remove(long id, Gesture gesture) {
        mDatabase.remove(id);
        mLibrary.removeGesture(String.valueOf(id), gesture);
        if (!save()) {
            Logger.e(TAG, "save error at remove");
            return false;
        }
        return true;
    }

    public boolean remove(GestureItem gesture) {
        return remove(gesture.getId(), gesture.getGesture());
    }

    public boolean updateAction(GestureItem gesture, Action action) {
        return mDatabase.update(gesture.getId(), action);
    }

    public boolean updateAction(long id, Action action) {
        return mDatabase.update(id, action);
    }

    public Action recognize(Gesture gesture) {
        ArrayList<Prediction> list = mLibrary.recognize(gesture);
        if (!list.isEmpty()) {
            Prediction prediction = list.get(0);
            if (prediction.score > getGestureScore()) {
                return mDatabase.get(Long.parseLong(prediction.name, 10));
            }
        }
        return null;
    }

    public boolean exists(Gesture gesture) {
        ArrayList<Prediction> list = mLibrary.recognize(gesture);
        if (!list.isEmpty()) {
            Prediction prediction = list.get(0);
            if (prediction.score > getGestureScore()) {
                return true;
            }
        }
        return false;
    }

    public GestureScore getScore(Gesture gesture) {
        ArrayList<Prediction> list = mLibrary.recognize(gesture);
        if (!list.isEmpty()) {
            Prediction prediction = list.get(0);
            return new GestureScore(prediction.score, mDatabase.get(Long.parseLong(prediction.name, 10)));
        }
        return null;
    }

    public List<GestureItem> getList() {
        ArrayList<GestureItem> list = new ArrayList<>();
        for (String entryName : mLibrary.getGestureEntries()) {
            long id = Long.parseLong(entryName, 10);
            list.add(new GestureItem(Long.parseLong(entryName, 10), mLibrary.getGestures(entryName).get(0), mDatabase.get(id)));
        }
        return list;
    }

    public abstract int getGestureStrokeType();

    public abstract double getGestureScore();

    public static GestureManager getInstance(Context context, int id) {
        switch (id) {
            case GESTURE_TYPE_WEB:
                return new WebGestureManager(context);
            case GESTURE_TYPE_SUB:
                return new SubGestureManager(context);
        }

        throw new IllegalArgumentException("Unknown id:" + id);
    }
}
