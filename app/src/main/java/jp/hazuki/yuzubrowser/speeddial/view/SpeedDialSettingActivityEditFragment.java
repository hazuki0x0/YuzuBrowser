package jp.hazuki.yuzubrowser.speeddial.view;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.Toast;

import java.io.File;

import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.RootLayout;
import jp.hazuki.yuzubrowser.download.DownloadFileProvider;
import jp.hazuki.yuzubrowser.speeddial.SpeedDial;
import jp.hazuki.yuzubrowser.speeddial.WebIcon;
import jp.hazuki.yuzubrowser.utils.ImageUtils;

public class SpeedDialSettingActivityEditFragment extends Fragment {

    private static final String DATA = "dat";
    private static final int REQUEST_PICK_IMAGE = 100;
    private static final int REQUEST_CROP_IMAGE = 101;

    private SpeedDial speedDial;
    private EditText name;
    private EditText url;
    private ImageButton icon;
    private View bottomBar;
    private SpeedDialEditCallBack mCallBack;
    private GoBackController goBack;

    public static Fragment newInstance(SpeedDial speedDial) {
        Fragment fragment = new SpeedDialSettingActivityEditFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(DATA, speedDial);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_edit_speeddial, container, false);

        speedDial = (SpeedDial) getArguments().getSerializable(DATA);

        if (speedDial == null) {
            speedDial = new SpeedDial();
        }

        name = (EditText) v.findViewById(R.id.name);
        url = (EditText) v.findViewById(R.id.url);
        icon = (ImageButton) v.findViewById(R.id.imageButton2);
        bottomBar = v.findViewById(R.id.bottomBar);

        Switch sw = (Switch) v.findViewById(R.id.use_favicon);

        ((RootLayout) v.findViewById(R.id.superFrameLayout)).setOnImeShownListener(new RootLayout.OnImeShownListener() {
            @Override
            public void onImeVisibilityChanged(boolean visible) {
                bottomBar.setVisibility(visible ? View.GONE : View.VISIBLE);
            }
        });

        name.setText(speedDial.getTitle());
        url.setText(speedDial.getUrl());

        if (speedDial.getIcon() == null) {
            speedDial.setIcon(WebIcon.createIcon(ImageUtils.getBitmapFromVectorDrawable(getActivity(), R.drawable.ic_public_white_24dp)));
        }

        icon.setImageBitmap(speedDial.getIcon().getBitmap());

        sw.setChecked(speedDial.isFavicon());
        setIconEnable(!speedDial.isFavicon());

        sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                speedDial.setFaviconMode(isChecked);
                setIconEnable(!isChecked);
            }
        });

        icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, REQUEST_PICK_IMAGE);
            }
        });

        v.findViewById(R.id.okButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCallBack != null) {
                    speedDial.setTitle(name.getText().toString());
                    speedDial.setUrl(url.getText().toString());
                    mCallBack.onEdited(speedDial);
                }
            }
        });

        v.findViewById(R.id.cancelButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (goBack != null)
                    goBack.goBack();
            }
        });

        return v;
    }

    private void setIconEnable(boolean enable) {
        icon.setEnabled(enable);
        icon.setAlpha(enable ? 1.0f : 0.6f);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_PICK_IMAGE:
                if (resultCode == Activity.RESULT_OK) {
                    try {
                        Uri uri = data.getData();
                        if ("file".equals(uri.getScheme())) {
                            uri = DownloadFileProvider.getUriForFIle(new File(uri.getPath()));
                        }
                        Intent intent = new Intent("com.android.camera.action.CROP");
                        intent.setData(uri);
                        intent.putExtra("outputX", 200);
                        intent.putExtra("outputY", 200);
                        intent.putExtra("aspectX", 1);
                        intent.putExtra("aspectY", 1);
                        intent.putExtra("scale", true);
                        intent.putExtra("return-data", true);
                        startActivityForResult(intent, REQUEST_CROP_IMAGE);
                    } catch (ActivityNotFoundException e) {
                        Toast.makeText(getActivity(), "Activity not found", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case REQUEST_CROP_IMAGE:
                if (resultCode == Activity.RESULT_OK && data != null && data.getExtras() != null) {
                    Bitmap bitmap = data.getExtras().getParcelable("data");
                    speedDial.setIcon(WebIcon.createIcon(bitmap));
                    icon.setImageBitmap(bitmap);
                }
                break;
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mCallBack = (SpeedDialEditCallBack) getActivity();
            goBack = (GoBackController) getActivity();
        } catch (ClassCastException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallBack = null;
        goBack = null;
    }

    interface GoBackController {
        boolean goBack();
    }
}
