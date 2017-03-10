package jp.hazuki.yuzubrowser.utils.view.applist;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import jp.hazuki.yuzubrowser.R;

public class ApplicationListAdapter extends ArrayAdapter<ResolveInfo> {
    private final PackageManager mPackageManager;

    public ApplicationListAdapter(Context context, List<ResolveInfo> objects) {
        super(context, 0, objects);
        mPackageManager = context.getPackageManager();
    }

    public ApplicationListAdapter(Context context, ResolveInfo[] objects) {
        super(context, 0, objects);
        mPackageManager = context.getPackageManager();
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.image_text_list_item, null);

            ImageView imageView = (ImageView) convertView.findViewById(R.id.imageView);
            ViewGroup.LayoutParams params = imageView.getLayoutParams();
            int app_icon_size = (int) getContext().getResources().getDimension(android.R.dimen.app_icon_size);
            params.height = app_icon_size;
            params.width = app_icon_size;
            imageView.setLayoutParams(params);
        }

        ImageView imageView = (ImageView) convertView.findViewById(R.id.imageView);
        TextView textView = (TextView) convertView.findViewById(R.id.textView);

        ResolveInfo info = getItem(position);
        imageView.setImageDrawable(info.loadIcon(mPackageManager));
        textView.setText(info.loadLabel(mPackageManager));

        return convertView;
    }
}
