package jp.hazuki.yuzubrowser.pattern.url;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.regex.PatternSyntaxException;

import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.pattern.PatternAction;
import jp.hazuki.yuzubrowser.pattern.PatternActivity;
import jp.hazuki.yuzubrowser.pattern.action.WebSettingPatternAction;
import jp.hazuki.yuzubrowser.useragent.UserAgentListActivity;
import jp.hazuki.yuzubrowser.utils.ErrorReport;

public class PatternUrlActivity extends PatternActivity<PatternUrlChecker> {
    private EditText urlEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View header_view = getLayoutInflater().inflate(R.layout.pattern_list_url, null);
        urlEditText = (EditText) header_view.findViewById(R.id.urlEditText);
        addHeaderView(header_view);

        setPatternManager(new PatternUrlManager(getApplicationContext()));
    }

    protected View makeHeaderView(PatternUrlChecker checker) {
        View header_view = getLayoutInflater().inflate(R.layout.pattern_list_url, null);
        EditText view_urlEditText = (EditText) header_view.findViewById(R.id.urlEditText);
        CharSequence url;
        if (checker == null) {
            url = urlEditText.getText();
            urlEditText.setText(null);
        } else {
            url = checker.getPatternUrl();
        }
        view_urlEditText.setText(url);
        setDefaultUrl(url.toString());
        return header_view;
    }

    @Override
    protected void settingOpenOthersAction(PatternUrlChecker checker, View header_view) {
        super.settingOpenOthersAction(checker, makeHeaderView(checker));
    }

    @Override
    protected DialogFragment getWebSettingDialog(PatternUrlChecker checker) {
        return SettingWebDialog.getInstance(getPosition(checker), checker);
    }

    @Override
    protected void settingWebSettingAction(PatternUrlChecker checker, View header_view) {
        super.settingWebSettingAction(checker, makeHeaderView(checker));
    }

    @Override
    protected void settingBlockAction(PatternUrlChecker checker, View header_view) {
        super.settingBlockAction(checker, makeHeaderView(checker));
    }

    @Override
    protected PatternUrlChecker makeActionChecker(PatternAction pattern_action, View header_view) {
        String pattern_url = ((EditText) header_view.findViewById(R.id.urlEditText)).getText().toString();
        try {
            return new PatternUrlChecker(pattern_action, pattern_url);
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

            final CheckBox view_uaCheckBox = (CheckBox) view.findViewById(R.id.uaCheckBox);
            view_uaEditText = (EditText) view.findViewById(R.id.uaEditText);
            final ImageButton view_uaButton = (ImageButton) view.findViewById(R.id.uaButton);
            final CheckBox view_jsCheckBox = (CheckBox) view.findViewById(R.id.jsCheckBox);
            final Spinner view_jsSpinner = (Spinner) view.findViewById(R.id.jsSpinner);

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

            return new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.pattern_change_websettings)
                    .setView(view)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
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
                                int id = getArguments().getInt(ID);
                                ((PatternUrlActivity) getActivity()).add(id, newChecker);
                            }
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .create();
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            if (requestCode == REQUEST_USER_AGENT && resultCode == RESULT_OK) {
                view_uaEditText.setText(data.getStringExtra(Intent.EXTRA_TEXT));
            }
        }
    }
}

