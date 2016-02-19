package be.groept.emedialab.preferences;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import java.util.List;

import be.groept.emedialab.R;

public class PreferenceActivity extends android.preference.PreferenceActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * Populate the activity with the top-level headers.
     */
    @Override
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.preference_headers, target);
    }

    /**
     * Fragment for preferences of pattern size
     */
    public static class PrefsFragmentPatternSize extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.preference_pattern);
        }
    }

    /**
     * This fragment shows the preferences for the second header.
     */
    public static class PrefsFragmentDebug extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.preference_debug);
        }
    }
}