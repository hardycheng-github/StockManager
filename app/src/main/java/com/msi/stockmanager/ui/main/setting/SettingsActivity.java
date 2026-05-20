package com.msi.stockmanager.ui.main.setting;

import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;

import com.msi.stockmanager.BuildConfig;
import com.msi.stockmanager.R;
import com.msi.stockmanager.data.ExternalApiPrefs;
import com.msi.stockmanager.data.demo.SimulatedDataImporter;
import com.msi.stockmanager.data.notify.MaAlertLevel;
import com.msi.stockmanager.data.profile.Profile;
import com.msi.stockmanager.util.AppExitUtil;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

            Preference importSimulated = findPreference("import_simulated_data");
            if (importSimulated != null) {
                importSimulated.setOnPreferenceClickListener(preference -> {
                    new AlertDialog.Builder(requireContext())
                            .setTitle(R.string.import_simulated_data_confirm_title)
                            .setMessage(R.string.import_simulated_data_confirm_message)
                            .setPositiveButton(android.R.string.ok, (dialog, which) -> runImportSimulatedData())
                            .setNegativeButton(android.R.string.cancel, null)
                            .show();
                    return true;
                });
            }
        }

        private final ExecutorService importExecutor = Executors.newSingleThreadExecutor();

        private void runImportSimulatedData() {
            importExecutor.execute(() -> {
                SimulatedDataImporter.ImportResult result = new SimulatedDataImporter().importAll();
                if (!isAdded()) {
                    return;
                }
                requireActivity().runOnUiThread(() -> {
                    if (result.successCount > 0) {
                        showImportSuccessExitDialog(result);
                    } else {
                        Toast.makeText(
                                requireContext(),
                                R.string.import_simulated_data_fail,
                                Toast.LENGTH_LONG).show();
                    }
                });
            });
        }

        private void showImportSuccessExitDialog(SimulatedDataImporter.ImportResult result) {
            String message;
            if (result.failCount == 0) {
                message = getString(
                        R.string.import_simulated_data_success_dialog_message,
                        result.successCount);
            } else {
                message = getString(
                        R.string.import_simulated_data_partial_dialog_message,
                        result.successCount,
                        result.failCount);
            }
            new AlertDialog.Builder(requireContext())
                    .setTitle(R.string.import_simulated_data_success_dialog_title)
                    .setMessage(message)
                    .setCancelable(false)
                    .setPositiveButton(android.R.string.ok, (dialog, which) ->
                            AppExitUtil.exitApp(requireActivity()))
                    .show();
        }
    }
}