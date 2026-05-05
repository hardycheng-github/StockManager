package com.msi.stockmanager.ui.main.setting;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;

import com.msi.stockmanager.BuildConfig;
import com.msi.stockmanager.R;
import com.msi.stockmanager.data.ExternalApiPrefs;
import com.msi.stockmanager.data.notify.MaAlertLevel;
import com.msi.stockmanager.data.profile.Profile;

public class SettingsActivity extends AppCompatActivity {
    private static final String TAG = SettingsActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        ((TextView)findViewById(R.id.version))
                .setText(getString(R.string.version) + " " + BuildConfig.VERSION_NAME);
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
        
        /** 更新平均線關注等級 summary；listener 內應傳 newValue，因 getValue() 此時仍為舊值。 */
        private void updateMaAlertLevelSummary(ListPreference preference, String valueOverride) {
            if (preference == null) return;

            String currentValue = valueOverride != null ? valueOverride : preference.getValue();
            if (currentValue == null) {
                currentValue = MaAlertLevel.DEFAULT.toString();
            }
            
            MaAlertLevel level = MaAlertLevel.fromString(currentValue);
            String summaryText;
            switch (level) {
                case LOW:
                    summaryText = getString(R.string.ma_alert_level_low);
                    break;
                case DEFAULT:
                    summaryText = getString(R.string.ma_alert_level_default);
                    break;
                case HIGH:
                    summaryText = getString(R.string.ma_alert_level_high);
                    break;
                default:
                    summaryText = getString(R.string.ma_alert_level_default);
                    break;
            }
            preference.setSummary(summaryText);
        }

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
            SwitchPreference enable_finmind_api = findPreference(ExternalApiPrefs.KEY_ENABLE_FINMIND_API);
            if (enable_finmind_api != null) {
                enable_finmind_api.setOnPreferenceChangeListener((preference, newValue) -> {
                    boolean enabled = Boolean.parseBoolean(newValue.toString());
                    Log.i(TAG, "enable_finmind_api: " + enabled);
                    return true;
                });
            }
            SwitchPreference enable_marketaux_api = findPreference(ExternalApiPrefs.KEY_ENABLE_MARKETAUX_API);
            if (enable_marketaux_api != null) {
                enable_marketaux_api.setOnPreferenceChangeListener((preference, newValue) -> {
                    boolean enabled = Boolean.parseBoolean(newValue.toString());
                    Log.i(TAG, "enable_marketaux_api: " + enabled);
                    return true;
                });
            }
            
            ListPreference ma_alert_level = findPreference("setting_ma_alert_level");
            
            // 設置初始 summary 顯示當前設定值
            updateMaAlertLevelSummary(ma_alert_level, null);
            
            ma_alert_level.setOnPreferenceChangeListener((preference, newValue) -> {
                try {
                    String levelStr = newValue.toString();
                    Profile.maAlertLevel = MaAlertLevel.fromString(levelStr);
                    Log.d(TAG, "setting_ma_alert_level: " + Profile.maAlertLevel);
                    
                    // 更新 summary：listener 回傳前 getValue() 仍為舊值，必須依 newValue 更新
                    updateMaAlertLevelSummary(ma_alert_level, levelStr);
                    
                    return true;
                } catch (Exception e){
                    Log.e(TAG, "Error setting ma_alert_level", e);
                }
                return false;
            });
        }
    }
}