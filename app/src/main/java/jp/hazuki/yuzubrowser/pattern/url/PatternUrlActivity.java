package jp.hazuki.yuzubrowser.pattern.url;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Collections;
import java.util.List;
import java.util.regex.PatternSyntaxException;

import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.pattern.PatternAction;
import jp.hazuki.yuzubrowser.pattern.PatternActivity;
import jp.hazuki.yuzubrowser.pattern.action.OpenOthersPatternAction;
import jp.hazuki.yuzubrowser.pattern.action.WebSettingPatternAction;
import jp.hazuki.yuzubrowser.useragent.UserAgentListActivity;
import jp.hazuki.yuzubrowser.utils.ErrorReport;
import jp.hazuki.yuzubrowser.utils.ImeUtils;
import jp.hazuki.yuzubrowser.utils.WebUtils;
import jp.hazuki.yuzubrowser.utils.fastmatch.FastMatcherFactory;

public class PatternUrlActivity extends PatternActivity<PatternUrlChecker> {
    private EditText urlEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View header_view = getLayoutInflater().inflate(R.layout.pattern_list_url, null);
        urlEditText = header_view.findViewById(R.id.urlEditText);
        addHeaderView(header_view);

        if (getIntent() != null) {
            urlEditText.setText(getIntent().getStringExtra(Intent.EXTRA_TEXT));
        }

        setPatternManager(new PatternUrlManager(getApplicationContext()));
    }

    protected View makeHeaderView(PatternUrlChecker checker) {
        View header_view = getLayoutInflater().inflate(R.layout.pattern_list_url, null);
        EditText view_urlEditText = header_view.findViewById(R.id.urlEditText);
        CharSequence url;
        if (checker == null) {
            url = urlEditText.getText();
            urlEditText.setText(null);
        } else {
            url = checker.getPatternUrl();
        }
        view_urlEditText.setText(url);
        return header_view;
    }

    @Override
    protected DialogFragment getWebSettingDialog(PatternUrlChecker checker) {
        return SettingWebDialog.getInstance(getPosition(checker), checker);
    }

    @Override
    protected DialogFragment getOpenOtherDialog(PatternUrlChecker checker) {
        return OpenOtherDialog.newInstance(getPosition(checker), checker, urlEditText.getText().toString());
    }

    @Override
    protected void settingBlockAction(PatternUrlChecker checker, View header_view) {
        super.settingBlockAction(checker, makeHeaderView(checker));
    }

    @Override
    public PatternUrlChecker makeActionChecker(PatternAction pattern_action, View header_view) {
        String pattern_url = ((EditText) header_view.findViewById(R.id.urlEditText)).getText().toString();
        try {
            PatternUrlChecker checker = new PatternUrlChecker(pattern_action, new FastMatcherFactory(), pattern_url);
            urlEditText.setText("");
            return checker;
        } catch (PatternSyntaxException e) {
            ErrorReport.printAndWriteLog(e);
            Toast.makeText(getApplicationContext(), R.string.pattern_syntax_error, Toast.LENGTH_SHORT).show();
        }
        return null;
    }

    public static class SettingWebDialog extends DialogFragment {
        private static final String ID = "id";
        private static final String CHECKER = "checker";

        private static final int REQUEST_USER_AGENT = 1;

        private View header = null;
        private EditText view_uaEditText;

        public static DialogFragment getInstance(int id, PatternUrlChecker checker) {
            DialogFragment fragment = new SettingWebDialog();
            Bundle bundle = new Bundle();
            bundle.putInt(ID, id);
            bundle.putSerializable(CHECKER, checker);
            fragment.setArguments(bundle);
            return fragment;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            PatternUrlChecker checker = (PatternUrlChecker) getArguments().getSerializable(CHECKER);
            ViewGroup view = (ViewGroup) LayoutInflater.from(getActivity()).inflate(R.layout.pattern_add_websetting, null);
            if (getActivity() instanceof PatternUrlActivity) {
                header = ((PatternUrlActivity) getActivity()).makeHeaderView(checker);
                if (header != null)
                    ((LinearLayout) view.findViewById(R.id.inner)).addView(header, 0);
            }

            final CheckBox view_uaCheckBox = view.findViewById(R.id.uaCheckBox);
            view_uaEditText = view.findViewById(R.id.uaEditText);
            final ImageButton view_uaButton = view.findViewById(R.id.uaButton);
            final CheckBox view_jsCheckBox = view.findViewById(R.id.jsCheckBox);
            final Spinner view_jsSpinner = view.findViewById(R.id.jsSpinner);

            if (checker != null) {
                WebSettingPatternAction action = (WebSettingPatternAction) checker.getAction();
                String ua = action.getUserAgentString();
                view_uaCheckBox.setChecked(ua != null);
                if (ua != null)
                    view_uaEditText.setText(ua);

                int js = action.getJavaScriptSetting();
                view_jsCheckBox.setChecked(js != WebSettingPatternAction.UNDEFINED);
                switch (js) {
                    case WebSettingPatternAction.ENABLE:
                        view_jsSpinner.setSelection(0);
                        break;
                    case WebSettingPatternAction.DISABLE:
                        view_jsSpinner.setSelection(1);
                        break;
                }
            }

            view_uaButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), UserAgentListActivity.class);
                    intent.putExtra(Intent.EXTRA_TEXT, view_uaEditText.getText().toString());
                    startActivityForResult(intent, REQUEST_USER_AGENT);
                }
            });

            final AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.pattern_change_websettings)
                    .setView(view)
                    .setPositiveButton(android.R.string.ok, null)
                    .setNegativeButton(android.R.string.cancel, null)
                    .create();

            alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialog) {
                    alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String ua = null;
                            if (view_uaCheckBox.isChecked()) {
                                ua = view_uaEditText.getText().toString();
                            }

                            int js = WebSettingPatternAction.UNDEFINED;
                            if (view_jsCheckBox.isChecked()) {
                                switch (view_jsSpinner.getSelectedItemPosition()) {
                                    case 0:
                                        js = WebSettingPatternAction.ENABLE;
                                        break;
                                    case 1:
                                        js = WebSettingPatternAction.DISABLE;
                                        break;
                                }
                            }

                            WebSettingPatternAction action = new WebSettingPatternAction(ua, js);
                            if (getActivity() instanceof PatternUrlActivity) {
                                PatternUrlChecker newChecker = ((PatternUrlActivity) getActivity()).makeActionChecker(action, header);
                                if (newChecker != null) {
                                    int id = getArguments().getInt(ID);
                                    ((PatternUrlActivity) getActivity()).add(id, newChecker);
                                    dismiss();
                                }
                            }
                        }
                    });
                }
            });


            return alertDialog;
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            if (requestCode == REQUEST_USER_AGENT && resultCode == RESULT_OK) {
                view_uaEditText.setText(data.getStringExtra(Intent.EXTRA_TEXT));
            }
        }
    }

    public static class OpenOtherDialog extends DialogFragment {
        private static final String ID = "id";
        private static final String CHECKER = "checker";
        private static final String URL = "url";

        private PatternUrlActivity patternActivity;
        private Intent intent;

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            String url = getArguments().getString(URL);
            final PatternUrlChecker checker = (PatternUrlChecker) getArguments().getSerializable(CHECKER);

            final LayoutInflater inflater = LayoutInflater.from(getActivity());
            final View header_view = inflater.inflate(R.layout.pattern_list_url, null);
            final EditText view_urlEditText = header_view.findViewById(R.id.urlEditText);
            if (checker != null) {
                url = checker.getPatternUrl();
            }
            if (url == null) {
                url = "";
            }
            view_urlEditText.setText(url);

            ViewGroup view = (ViewGroup) inflater.inflate(R.layout.pattern_add_open, null);
            view.addView(header_view, 0);
            ListView view_listView = view.findViewById(R.id.listView);

            final PackageManager pm = getActivity().getPackageManager();
            String intentUrl = url.replace("*", "");
            intent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse((WebUtils.maybeContainsUrlScheme(intentUrl)) ? intentUrl : "http://" + intentUrl));
            final int flag;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                flag = PackageManager.MATCH_ALL;
            } else {
                flag = PackageManager.MATCH_DEFAULT_ONLY;
            }
            final List<ResolveInfo> open_app_list = pm.queryIntentActivities(intent, flag);
            Collections.sort(open_app_list, new ResolveInfo.DisplayNameComparator(pm));

            final ArrayAdapter<ResolveInfo> arrayAdapter = new ArrayAdapter<ResolveInfo>(getActivity(), 0, open_app_list) {
                private final int app_icon_size = (int) getResources().getDimension(android.R.dimen.app_icon_size);

                @NonNull
                @Override
                public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                    if (convertView == null) {
                        convertView = inflater.inflate(R.layout.image_text_list_item, null);
                        ImageView imageView = convertView.findViewById(R.id.imageView);

                        ViewGroup.LayoutParams params = imageView.getLayoutParams();
                        params.height = app_icon_size;
                        params.width = app_icon_size;
                        imageView.setLayoutParams(params);
                    }

                    ImageView imageView = convertView.findViewById(R.id.imageView);
                    TextView textView = convertView.findViewById(R.id.textView);

                    if (position == 0) {
                        imageView.setImageDrawable(null);
                        textView.setText(getString(R.string.pattern_open_app_list));
                    } else {
                        ResolveInfo item = getItem(position);
                        imageView.setImageDrawable(item.loadIcon(pm));
                        textView.setText(item.loadLabel(pm));
                    }

                    return convertView;
                }

                @Override
                public ResolveInfo getItem(int position) {
                    return super.getItem(position - 1);
                }

                @Override
                public int getCount() {
                    return super.getCount() + 1;
                }
            };

            view_listView.setAdapter(arrayAdapter);

            final AlertDialog.Builder dialog_builder = new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.pattern_open_others)
                    .setView(view)
                    .setNegativeButton(android.R.string.cancel, null);

            if (checker != null)
                dialog_builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        PatternUrlChecker new_checker = patternActivity.makeActionChecker(checker.getAction(), header_view);
                        if (new_checker != null) {
                            patternActivity.add(getArguments().getInt(ID), new_checker);
                        }
                    }
                });

            view_listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    OpenOthersPatternAction pattern;
                    if (position == 0) {
                        pattern = new OpenOthersPatternAction(OpenOthersPatternAction.TYPE_APP_LIST);
                    } else {
                        ResolveInfo item = open_app_list.get(position - 1);
                        intent.setClassName(item.activityInfo.packageName, item.activityInfo.name);
                        pattern = new OpenOthersPatternAction(intent);
                    }
                    PatternUrlChecker new_checker = patternActivity.makeActionChecker(pattern, header_view);
                    if (new_checker != null) {
                        patternActivity.add(getArguments().getInt(ID), new_checker);
                        dismiss();
                    }
                }
            });

            view_listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    if (position == 0) {
                        OpenOthersPatternAction new_pattern = new OpenOthersPatternAction(OpenOthersPatternAction.TYPE_APP_CHOOSER);
                        PatternUrlChecker new_checker = patternActivity.makeActionChecker(new_pattern, header_view);
                        if (new_checker != null) {
                            patternActivity.add(getArguments().getInt(ID), new_checker);
                            dismiss();
                        }
                    }
                    return true;
                }
            });

            view_urlEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        ImeUtils.hideIme(getActivity(), view_urlEditText);
                        String url = view_urlEditText.getText().toString();
                        intent = new Intent(Intent.ACTION_VIEW, Uri.parse((WebUtils.maybeContainsUrlScheme(url)) ? url : "http://" + url));
                        List<ResolveInfo> open_app_list = pm.queryIntentActivities(intent, flag);
                        Collections.sort(open_app_list, new ResolveInfo.DisplayNameComparator(pm));
                        arrayAdapter.clear();
                        arrayAdapter.addAll(open_app_list);
                        arrayAdapter.notifyDataSetChanged();
                        return true;
                    }
                    return false;
                }
            });

            return dialog_builder.create();
        }

        public static OpenOtherDialog newInstance(int id, PatternUrlChecker checker, String url) {
            OpenOtherDialog dialog = new OpenOtherDialog();
            Bundle bundle = new Bundle();
            bundle.putInt(ID, id);
            bundle.putSerializable(CHECKER, checker);
            bundle.putString(URL, url);
            dialog.setArguments(bundle);
            return dialog;
        }

        @Override
        public void onAttach(Context context) {
            super.onAttach(context);
            patternActivity = (PatternUrlActivity) getActivity();
        }

        @Override
        public void onDetach() {
            super.onDetach();
            patternActivity = null;
        }
    }
}

