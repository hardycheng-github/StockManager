package com.msi.stockmanager.ui.main.setting;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;

import com.msi.stockmanager.R;
import com.msi.stockmanager.data.profile.Profile;

public class SettingsActivity extends AppCompatActivity {
    private static final String TAG = SettingsActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    public static class SettingsFragment extends PreferenceFragmentCompat
    {

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.setting_preferences, rootKey);
            EditTextPreference setting_fee_discount = findPreference("setting_fee_discount");
            setting_fee_discount.setOnBindEditTextListener(editText -> editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL));
            setting_fee_discount.setOnPreferenceChangeListener((preference, newValue) -> {
                if(newValue.toString().isEmpty()){
                    Profile.fee_discount = 1.0;
                    return true;
                }
                try {
                    Profile.fee_discount = Double.parseDouble(newValue.toString());
                    Log.d(TAG, "setting_fee_discount: " + Profile.fee_discount);
                    return true;
                } catch (Exception e){}
                return false;
            });
            EditTextPreference setting_fee_minimum = findPreference("setting_fee_minimum");
            setting_fee_minimum.setOnBindEditTextListener(editText -> editText.setInputType(InputType.TYPE_CLASS_NUMBER));
            setting_fee_minimum.setOnPreferenceChangeListener((preference, newValue) -> {
                try {
                    Profile.fee_minimum = Integer.parseInt(newValue.toString());
                    Log.d(TAG, "setting_fee_minimum: " + Profile.fee_minimum);
                    return true;
                } catch (Exception e){}
                return false;
            });
            SwitchPreference profit_color_reverse = findPreference("profit_color_reverse");
            profit_color_reverse.setOnPreferenceChangeListener((preference, newValue) -> {
                try {
                    Profile.profit_color_reverse = Boolean.parseBoolean(newValue.toString());
                    Log.d(TAG, "profit_color_reverse: " + Profile.profit_color_reverse);
                    return true;
                } catch (Exception e){}
                return false;
            });
        }
    }
}