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
import androidx.preference.MultiSelectListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;

import com.msi.stockmanager.BuildConfig;
import com.msi.stockmanager.R;
import com.msi.stockmanager.data.demo.SimulatedDataImporter;
import com.msi.stockmanager.data.notify.EventSubscriptionConfig;
import com.msi.stockmanager.data.notify.MaAlertLevel;
import com.msi.stockmanager.data.notify.MacdSignalConfig;
import com.msi.stockmanager.data.profile.ChartIndicatorType;
import com.msi.stockmanager.data.profile.Profile;
import com.msi.stockmanager.util.AppExitUtil;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SettingsActivity extends AppCompatActivity {
    private static final String TAG = SettingsActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        setSupportActionBar(findViewById(R.id.toolbar));
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
        
        /**
         * 更新 MACD 關注等級的 summary 顯示
         * @param preference ListPreference
         * @param newValue 新選定的值；OnPreferenceChangeListener 觸發時 preference 尚未更新，需傳入此參數
         */
        private void updateMaAlertLevelSummary(ListPreference preference, String newValue) {
            if (preference == null) return;

            String currentValue = newValue != null ? newValue : preference.getValue();
            if (currentValue == null) {
                currentValue = MaAlertLevel.DEFAULT.toString();
            }

            MaAlertLevel level = MaAlertLevel.fromString(currentValue);
            String levelName;
            switch (level) {
                case LOW:
                    levelName = getString(R.string.ma_alert_level_low);
                    break;
                case HIGH:
                    levelName = getString(R.string.ma_alert_level_high);
                    break;
                case DEFAULT:
                default:
                    levelName = getString(R.string.ma_alert_level_default);
                    break;
            }
            String macdLabel = MacdSignalConfig.getMacdLabel(MacdSignalConfig.getMacdParams(level));
            preference.setSummary(getString(R.string.ma_alert_level_summary, levelName, macdLabel));
        }

        private void updateChartIndicatorSummary(ListPreference preference, String newValue) {
            if (preference == null) return;

            String currentValue = newValue != null ? newValue : preference.getValue();
            if (currentValue == null) {
                currentValue = ChartIndicatorType.EMA.toString();
            }

            ChartIndicatorType type = ChartIndicatorType.fromString(currentValue);
            String label;
            switch (type) {
                case EMA:
                    label = getString(R.string.chart_indicator_ema);
                    break;
                case BBAND:
                    label = getString(R.string.chart_indicator_bband);
                    break;
                case MA:
                default:
                    label = getString(R.string.chart_indicator_ma);
                    break;
            }
            preference.setSummary(label);
        }

        private void updateMacdEventSubscriptionSummary(MultiSelectListPreference preference, Set<String> values) {
            if (preference == null) return;

            Set<String> selected = values != null ? values : preference.getValues();
            if (selected == null) {
                selected = EventSubscriptionConfig.defaultSubscribedEvents();
            }

            int count = 0;
            for (String key : EventSubscriptionConfig.orderedEventKeys()) {
                if (selected.contains(key)) {
                    count++;
                }
            }

            if (count == 0) {
                preference.setSummary(getString(R.string.macd_event_subscription_none));
            } else {
                preference.setSummary(getString(R.string.macd_event_subscription_summary, count));
            }
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
            
            ListPreference chartIndicator = findPreference("setting_chart_indicator");
            updateChartIndicatorSummary(chartIndicator, null);
            chartIndicator.setOnPreferenceChangeListener((preference, newValue) -> {
                try {
                    String typeStr = newValue.toString();
                    Profile.chartIndicatorType = ChartIndicatorType.fromString(typeStr);
                    Log.d(TAG, "setting_chart_indicator: " + Profile.chartIndicatorType);
                    updateChartIndicatorSummary(chartIndicator, typeStr);
                    return true;
                } catch (Exception e) {
                    Log.e(TAG, "Error setting chart_indicator", e);
                }
                return false;
            });

            ListPreference ma_alert_level = findPreference("setting_ma_alert_level");
            
            // 設置初始 summary 顯示當前設定值
            updateMaAlertLevelSummary(ma_alert_level, null);
            
            ma_alert_level.setOnPreferenceChangeListener((preference, newValue) -> {
                try {
                    String levelStr = newValue.toString();
                    Profile.maAlertLevel = MaAlertLevel.fromString(levelStr);
                    Log.d(TAG, "setting_ma_alert_level: " + Profile.maAlertLevel);
                    
                    // OnPreferenceChangeListener 在 preference 寫入前觸發，需傳入 newValue
                    updateMaAlertLevelSummary(ma_alert_level, levelStr);
                    
                    return true;
                } catch (Exception e){
                    Log.e(TAG, "Error setting ma_alert_level", e);
                }
                return false;
            });

            MultiSelectListPreference macdEventSubscription = findPreference("setting_macd_event_subscription");
            Set<String> mergedEvents = EventSubscriptionConfig.mergeWithDefaults(macdEventSubscription.getValues());
            macdEventSubscription.setValues(mergedEvents);
            Profile.subscribedEvents = new HashSet<>(mergedEvents);
            updateMacdEventSubscriptionSummary(macdEventSubscription, mergedEvents);
            macdEventSubscription.setOnPreferenceChangeListener((preference, newValue) -> {
                @SuppressWarnings("unchecked")
                Set<String> values = new HashSet<>((Set<String>) newValue);
                Profile.subscribedEvents = values;
                Log.d(TAG, "setting_macd_event_subscription: " + Profile.subscribedEvents);
                updateMacdEventSubscriptionSummary(macdEventSubscription, values);
                return true;
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