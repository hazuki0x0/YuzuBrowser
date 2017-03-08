package jp.hazuki.yuzubrowser.speeddial.view.appdata;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.BaseAdapter;

import java.util.ArrayList;

/**
 * Created by hazuki on 16/12/03.
 */

public abstract class ArrayBaseAdapter<T> extends BaseAdapter {

    private LayoutInflater inflater;
    private ArrayList<T> arrayList;
    private Context context;

    public ArrayBaseAdapter(Context context, ArrayList<T> items) {
        arrayList = items;
        inflater = LayoutInflater.from(context);
        this.context = context;
    }

    @Override
    public int getCount() {
        return arrayList.size();
    }

    @Override
    public T getItem(int i) {
        return arrayList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    public int indexOf(T item) {
        return arrayList.indexOf(item);
    }

    public ArrayList<T> getItems() {
        return arrayList;
    }

    public void add(T item) {
        arrayList.add(item);
        notifyDataSetChanged();
    }

    public void remove(T item) {
        arrayList.remove(item);
        notifyDataSetChanged();
    }

    public void remove(int pos) {
        arrayList.remove(pos);
        notifyDataSetChanged();
    }

    public void set(int id, T item) {
        arrayList.set(id, item);
        notifyDataSetChanged();
    }

    public void replace(ArrayList<T> items) {
        if (items != null) {
            arrayList = items;
            notifyDataSetChanged();
        }
    }

    public int size() {
        return arrayList.size();
    }

    public Context getContext() {
        return context;
    }

    public LayoutInflater getInflater() {
        return inflater;
    }
}
