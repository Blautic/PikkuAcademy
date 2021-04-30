package com.blautic.pikkuacademyfull;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceFragmentCompat;

import com.blautic.pikkuAcademyLib.PikkuAcademy;
import com.blautic.pikkuAcademyLib.sensors.AccScale;
import com.blautic.pikkuAcademyLib.sensors.GyrScale;

import timber.log.Timber;

import static com.blautic.pikkuacademyfull.utils.Utils.showMessage;

public class SensorSettingsActivity extends AppCompatActivity {

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
        if (item.getItemId() == android.R.id.home) // Press Back Icon
        {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {

        private PikkuAcademy pikkuAcademy;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
            pikkuAcademy = PikkuAcademy.getInstance(getContext());

            EditTextPreference listPrefPeriod = findPreference("transmitting_period");
            listPrefPeriod.setSummary(String.valueOf(pikkuAcademy.getTransmittingPeriod()));
            listPrefPeriod.setOnPreferenceChangeListener((preference, newValue) -> {
                try {
                    int period = Integer.parseInt(newValue.toString());
                    if (period > 0) {
                        if (pikkuAcademy.isConnected()) {
                            pikkuAcademy.changeTransmittingPeriod(period);
                            listPrefPeriod.setSummary(String.valueOf(period));
                            return true;
                        } else {
                            showMessage(getContext(), R.string.error_change_settings);
                        }

                    }
                } catch (Exception e) {
                    Timber.d(e);
                    Toast.makeText(getContext(), "invalid number", Toast.LENGTH_SHORT).show();
                }

                return false;
            });
            EditTextPreference group = findPreference("group");
            group.setSummary(String.valueOf(pikkuAcademy.getGroup()));
            group.setOnPreferenceChangeListener((preference, newValue) -> {
                try {
                    int value = Integer.parseInt(newValue.toString());
                    if (value >= 0) {
                        if (pikkuAcademy.changeGroup(value)) {
                            group.setSummary(String.valueOf(value));
                            return true;
                        } else {
                            showMessage(getContext(), R.string.error_change_settings);
                        }
                    }
                } catch (Exception e) {
                    Timber.d(e);
                    Toast.makeText(getContext(), "invalid number", Toast.LENGTH_SHORT).show();
                }

                return false;
            });
            EditTextPreference code = findPreference("code");
            code.setSummary(String.valueOf(pikkuAcademy.getCode()));

            code.setOnPreferenceChangeListener((preference, newValue) -> {
                try {
                    int value = Integer.parseInt(newValue.toString());
                    if (value >= 0) {
                        if (pikkuAcademy.changeCode(value)) {
                            code.setSummary(String.valueOf(value));
                            return true;
                        } else {
                            showMessage(getContext(), R.string.error_change_settings);
                        }
                    }
                } catch (Exception e) {
                    Timber.d(e);
                    Toast.makeText(getContext(), "invalid number", Toast.LENGTH_SHORT).show();
                }

                return false;
            });
            EditTextPreference number = findPreference("number");
            number.setSummary(String.valueOf(pikkuAcademy.getNumber()));
            number.setOnPreferenceChangeListener((preference, newValue) -> {
                try {
                    int value = Integer.parseInt(newValue.toString());
                    if (value >= 0) {
                        if (pikkuAcademy.changeNumber(value)) {
                            number.setSummary(String.valueOf(value));
                            return true;
                        } else {
                            showMessage(getContext(), R.string.error_change_settings);
                        }

                    }
                } catch (Exception e) {
                    Timber.d(e);
                    Toast.makeText(getContext(), "invalid number", Toast.LENGTH_SHORT).show();
                }
                return false;
            });

            EditTextPreference name = findPreference("name_pikku");
            name.setSummary(pikkuAcademy.getNameDevice());
            name.setOnPreferenceChangeListener((preference, newValue) -> {
                try {
                    if (!pikkuAcademy.isConnected()) {
                        showMessage(getContext(), R.string.error_change_settings);
                        return false;
                    }

                    if (pikkuAcademy.changeNameDevice(String.valueOf(newValue))) {
                        showMessage(getContext(), getString(R.string.rename_ok));
                        name.setSummary(String.valueOf(newValue));
                        return true;
                    } else {
                        showMessage(getContext(), R.string.error_change_settings);
                    }
                } catch (Exception e) {
                    Timber.d(e);
                    Toast.makeText(getContext(), "invalid name", Toast.LENGTH_SHORT).show();
                }

                return false;
            });
            ListPreference listPrefAcce = findPreference("accelerometer_scale");
            listPrefAcce.setOnPreferenceChangeListener((preference, newValue) -> {
                if (pikkuAcademy.isConnected()) {
                    String value = (String) newValue;
                    int scale = Integer.parseInt(value, 16);
                    AccScale scaleValue = AccScale.fromValue(scale);
                    pikkuAcademy.changeDefaultAccelerometerScale(scaleValue);
                    return true;
                }else {
                    showMessage(getContext(), R.string.error_change_settings);
                }

                return false;
            });

            ListPreference listPrefGyr = findPreference("gyroscope_scale");
            listPrefGyr.setOnPreferenceChangeListener((preference, newValue) -> {
                if (pikkuAcademy.isConnected()) {
                    String value = (String) newValue;
                    int scale = Integer.parseInt(value, 16);
                    GyrScale gyrScale = GyrScale.fromValue(scale);
                    pikkuAcademy.changeDefaultGyroscopeScale(gyrScale);
                    return true;
                }else {
                    showMessage(getContext(), R.string.error_change_settings);
                }
                return false;
            });


        }

    }
}