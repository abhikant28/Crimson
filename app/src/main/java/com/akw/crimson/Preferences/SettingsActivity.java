package com.akw.crimson.Preferences;

import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.akw.crimson.R;

public class SettingsActivity extends AppCompatActivity implements
        PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {

    private static final String TITLE_TAG = "Manage your Settings";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        setTheme(R.style.SettingsFragmentStyle);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new HeaderFragment())
                    .commit();
        } else {
            setTitle(savedInstanceState.getCharSequence(TITLE_TAG));
        }
        getSupportFragmentManager().addOnBackStackChangedListener(
                new FragmentManager.OnBackStackChangedListener() {
                    @Override
                    public void onBackStackChanged() {
                        if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
                            setTitle(R.string.title_activity_settings);
                        }
                    }
                });
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save current activity title so we can set it again after a configuration change
        outState.putCharSequence(TITLE_TAG, getTitle());
    }

    @Override
    public boolean onSupportNavigateUp() {
        if (getSupportFragmentManager().popBackStackImmediate()) {
            return true;
        }
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onPreferenceStartFragment(PreferenceFragmentCompat caller, Preference pref) {
        // Instantiate the new Fragment
        final Bundle args = pref.getExtras();
        final Fragment fragment = getSupportFragmentManager().getFragmentFactory().instantiate(
                getClassLoader(),
                pref.getFragment());
        fragment.setArguments(args);
        fragment.setTargetFragment(caller, 0);
        // Replace the existing Fragment with the new Fragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.settings, fragment)
                .addToBackStack(null)
                .commit();
        setTitle(pref.getTitle());

        return true;
    }

    public static class HeaderFragment extends PreferenceFragmentCompat {

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.settings_main_list, rootKey);

//            EditTextPreference signaturePreference = findPreference("chatTheme");
//
//            signaturePreference.getOnPreferenceClickListener();


        }
    }

    public static class AccountFragment extends PreferenceFragmentCompat {


        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.settings_account_list, rootKey);
        }
    }

    public static class PrivacyFragment extends PreferenceFragmentCompat {

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.settings_privacy_view, rootKey);
        }
    }

    public static class ChatFragment extends PreferenceFragmentCompat {

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.settings_chat_view, rootKey);
        }


    }
    public static class NotificationFragment extends PreferenceFragmentCompat {

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.settings_notifications_view, rootKey);
        }
    }
    public static class StorageFragment extends PreferenceFragmentCompat {

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.settings_storage_view, rootKey);
        }
    }
}