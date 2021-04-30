

package com.blautic.pikkuacademyfull;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.blautic.pikkuacademyfull.databinding.FragmentHomeBinding;
import com.blautic.pikkuAcademyLib.PikkuAcademy;
import com.blautic.pikkuAcademyLib.callback.AccelerometerCallback;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import timber.log.Timber;

import static com.blautic.pikkuacademyfull.MainActivity.REQUEST_CODE_ENABLE_BLUETOOTH;
import static com.blautic.pikkuacademyfull.MainActivity.REQUEST_CODE_OPEN_GPS;
import static com.blautic.pikkuacademyfull.MainActivity.REQUEST_CODE_PERMISSION_LOCATION;
import static com.blautic.pikkuAcademyLib.utils.Utils.calculateDistance;
import static com.blautic.pikkuacademyfull.utils.Utils.getVersion;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private PikkuAcademy pikkuAcademy;
    private final int RSSI_ONE_METER = 83;
    private boolean firstState = true;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        pikkuAcademy = PikkuAcademy.getInstance(getContext());
        pikkuAcademy.enableLog();
        checkBlePermissions();
        binding.connect.setOnClickListener(v -> {
            connect();
        });
        binding.settings.setOnClickListener(v -> {
            NavHostFragment.findNavController(HomeFragment.this)
                    .navigate(R.id.action_HomeFragment_to_settingsFragment);
        });
        initSensorsLinealChart();
        checkIsConnected();

        binding.connect.setOnLongClickListener(v -> {
            if (pikkuAcademy.isConnected()) {
                new MaterialAlertDialogBuilder(getContext())
                        .setTitle(R.string.turn_off_pikku)
                        .setNegativeButton(android.R.string.cancel, null)
                        .setPositiveButton(android.R.string.ok,
                                (dialog, which) -> {
                                    pikkuAcademy.turnOffDevice();
                                })
                        .setCancelable(false)
                        .show();
            }
            return false;
        });

        binding.version.setText(getVersion(getContext()));
    }

    private void checkIsConnected() {
        if (pikkuAcademy.isConnected()) {
            updateUI(true);
            readValues();
        }
    }

    private void initSensorsLinealChart() {
        binding.accelerometerChart.setLabelSensorName(R.string.accelerometer);
        binding.gyroscopeChart.setLabelSensorName(R.string.gyroscope);
    }


    private void connect() {
        if (pikkuAcademy.isConnected()) {
            pikkuAcademy.disconnect();
            return;
        }
        checkBlePermissions();
        binding.connectProgress.setVisibility(View.VISIBLE);
        if (pikkuAcademy.getAddressDevice().isEmpty()) {
            NavHostFragment.findNavController(HomeFragment.this)
                    .navigate(R.id.action_HomeFragment_to_ConnectFragment);

        } else {
            pikkuAcademy.connect(state -> {
                switch (state) {
                    case CONNECTED: {
                        updateUI(true);
                        readValues();
                        break;
                    }
                    case DISCONNECTED:
                    case FAILED: {
                        updateUI(false);
                        break;
                    }
                }
            });

        }
    }

    private void updateUI(boolean connected) {
        binding.connect.setImageResource(connected ? R.drawable.ic_ble_connected : R.drawable.ic_ble_disconnected);
        binding.connectProgress.setVisibility(View.INVISIBLE);
        binding.switchVibration.setEnabled(connected);
        binding.ledOn.setEnabled(connected);
        binding.ledOff.setEnabled(connected);
        binding.ledFlashing.setEnabled(connected);

    }


    private void readValues() {
        binding.name.setText(pikkuAcademy.getNameDevice());
       // pikkuAcademy.turnOnLed();
        //  binding.switchVibration.setChecked(false);
        //   pikkuAcademy.stopEngine();
        binding.imgPikkuNormal.setImageResource(R.drawable.ic_pikku_on);
        pikkuAcademy.readRssiConnectedDevice(rssi -> {
            if (!isVisible()) return;
            int accuracy = calculateDistance(rssi, RSSI_ONE_METER);
            binding.proximityBar.setProgress((100 - 10 * accuracy));
            binding.proximityText.setText(String.format(getString(R.string.proximity_value), accuracy));
        });

        pikkuAcademy.readStatusDevice(statusDevice -> {
            Timber.d(statusDevice.toString());
            updateBatteryIU(statusDevice.battery);
            if (firstState) {
                binding.switchVibration.setChecked(statusDevice.engineOn);
                binding.radioGroupLed.check(statusDevice.ledStatus == 0 ? R.id.led_off :
                        statusDevice.ledStatus == 1 ? R.id.led_on : R.id.led_flashing);
                firstState = false;
            }

        });


        pikkuAcademy.readButtons((nButton, pressedButton, durationMilliseconds) -> {
            String durationSeconds = String.format("%.1f''", durationMilliseconds / 1000.0);
            //Button 1 or 2
            if (nButton == 1) {
                binding.button1Time.setText(durationSeconds);
            } else {
                binding.button2Time.setText(durationSeconds);
            }

        });

        binding.accelerometerChart.setScale(pikkuAcademy.getDefaultAccelerometerScale().mask);
        pikkuAcademy.readAccelerometer(new AccelerometerCallback() {
            @Override
            public void onReadSuccess(float x, float y, float z) {
                binding.accelerometerChart.addEntryLineChart(x, y, z);
                drawAngles(x, y, z);
            }

            @Override
            public void onReadAngles(float xy, float zy, float xz) {
            }

        });

        binding.gyroscopeChart.setScale(pikkuAcademy.getDefaultGyroscopeScale().mask);

        pikkuAcademy.readGyroscope((x, y, z) -> {
            binding.gyroscopeChart.addEntryLineChart(x, y, z);
        });


        binding.switchVibration.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                pikkuAcademy.startEngine();
            } else {
                pikkuAcademy.stopEngine();
            }
            binding.imgVibrationLeft.setVisibility(isChecked ? View.VISIBLE : View.INVISIBLE);
            binding.imgVibrationRight.setVisibility(isChecked ? View.VISIBLE : View.INVISIBLE);

        });

        binding.radioGroupLed.setOnCheckedChangeListener((group, checkedId) -> {
            switch (checkedId) {
                case R.id.led_off:
                    pikkuAcademy.turnOffLed();
                    binding.imgPikkuNormal.setImageResource(R.drawable.ic_pikku_off);
                    break;
                case R.id.led_on:
                    pikkuAcademy.turnOnLed();
                    binding.imgPikkuNormal.setImageResource(R.drawable.ic_pikku_on);
                    break;
                case R.id.led_flashing:
                    pikkuAcademy.flashingLed();
                    binding.imgPikkuNormal.setImageResource(R.drawable.ic_pikku_on);
                    break;

            }
        });

    }

    private void drawAngles(float x, float y, float z) {
        if (x != 0 && y != 0) {
            binding.incXY.drawArc(pikkuAcademy.getAngles(x, y));
        }
        if (z != 0 && y != 0) {
            binding.incZY.drawArc(pikkuAcademy.getAngles(z, y));
        }
        if (x != 0 && z != 0) {
            binding.incXZ.drawArc(pikkuAcademy.getAngles(x, z));
        }
    }

    private void updateBatteryIU(int value) {
        binding.battery.setImageResource(
                value > 75 ? R.drawable.ic_battery_3 :
                        value > 50 ? R.drawable.ic_battery_2 :
                                value > 25 ? R.drawable.ic_battery_1 :
                                        R.drawable.ic_battery_0);
    }


    private void checkBlePermissions() {
        if (!pikkuAcademy.isBluetoothOn()) {
            new MaterialAlertDialogBuilder(getContext())
                    .setTitle(R.string.enable_bluetooth)
                    .setNegativeButton(android.R.string.cancel,
                            (dialog, which) -> getActivity().finish())
                    .setPositiveButton(android.R.string.ok,
                            (dialog, which) -> {
                                pikkuAcademy.enableBluetooth(getActivity(), REQUEST_CODE_ENABLE_BLUETOOTH);
                            })

                    .setCancelable(false)
                    .show();

            return;
        }
        String permission = Manifest.permission.ACCESS_FINE_LOCATION;
        int permissionCheck = ContextCompat.checkSelfPermission(getContext(), permission);
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            onPermissionGranted(permission);
        } else {
            ActivityCompat.requestPermissions(getActivity(), new String[]{permission}, REQUEST_CODE_PERMISSION_LOCATION);
        }

    }


    private void onPermissionGranted(String permission) {
        if (Manifest.permission.ACCESS_FINE_LOCATION.equals(permission)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !pikkuAcademy.checkGPSIsOpen(getContext())) {
                new AlertDialog.Builder(getContext())
                        .setMessage(R.string.enable_gps)
                        .setNegativeButton(android.R.string.cancel,
                                (dialog, which) -> getActivity().finish())
                        .setPositiveButton(R.string.enable_gps,
                                (dialog, which) -> {
                                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                    startActivityForResult(intent, REQUEST_CODE_OPEN_GPS);
                                })

                        .setCancelable(false)
                        .show();
            }
        }
    }

}