package com.blautic.pikkuacademyfull;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.preference.PreferenceManager;

import com.blautic.pikkuAcademyLib.PikkuAcademy;
import com.blautic.pikkuAcademyLib.StatusDevice;
import com.blautic.pikkuAcademyLib.callback.AccelerometerCallback;
import com.blautic.pikkuAcademyLib.callback.StatusDeviceCallback;
import com.blautic.pikkuacademyfull.databinding.FragmentHomeBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import timber.log.Timber;

import static com.blautic.pikkuacademyfull.MainActivity.REQUEST_CODE_CALL;
import static com.blautic.pikkuacademyfull.MainActivity.REQUEST_CODE_ENABLE_BLUETOOTH;
import static com.blautic.pikkuacademyfull.MainActivity.REQUEST_CODE_OPEN_GPS;
import static com.blautic.pikkuacademyfull.MainActivity.REQUEST_CODE_PERMISSION_LOCATION;
import static com.blautic.pikkuacademyfull.SettingsNumberActivity.NUMBER_KEY;
import static com.blautic.pikkuacademyfull.utils.Utils.getVersion;

public class HomeFragment extends Fragment implements MovementDetector.MovementListener {

    private FragmentHomeBinding binding;
    private final int TIME_PRESS_BUTTON = 3000;
    private PikkuAcademy pikkuAcademy;
    private long timeWhenStoppedRest;
    private long timeWhenStoppedStandUp;
    private boolean call;
    private boolean startRest;
    private boolean startStandUp;
    private MovementDetector movementDetector;

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
        checkIsConnected();
        movementDetector = new MovementDetector(this);
        binding.connect.setOnClickListener(v -> {
            connect();
        });
        binding.settings.setOnClickListener(v -> {
            NavHostFragment.findNavController(HomeFragment.this)
                    .navigate(R.id.action_HomeFragment_to_settingsFragment);
        });
        binding.connect.setOnLongClickListener(v -> {
            TurnOffPikku();
            return false;
        });

        binding.switchActivity.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                pikkuAcademy.readAccelerometer(new AccelerometerCallback() {
                    @Override
                    public void onReadSuccess(float x, float y, float z) {
                        movementDetector.setDataAccelerometer(x, y, z);
                    }

                    @Override
                    public void onReadAngles(float xy, float zy, float xz) {
                        //  Timber.d("xy: " + xy +"zy: " + zy +"xz: " + xz);
                        movementDetector.setDataAngles(xy, zy, xz);
                    }

                });
            } else {
                pikkuAcademy.enableReportSensors(false);
                stopRestChronometer();
                stopStandChronometer();
            }
        });

        binding.switchSos.setOnCheckedChangeListener((buttonView, isChecked) -> {
            binding.switchEnableCall.setEnabled(isChecked);
            binding.switchEnableVibrate.setEnabled(isChecked);

            pikkuAcademy.readButtons((nButton, pressed, duration) -> {
                if (isChecked && binding.switchEnableCall.isChecked() && pressed && duration > TIME_PRESS_BUTTON && !call) {
                    call = true;
                    callPhone();
                }
                if (isChecked && binding.switchEnableVibrate.isChecked() && pressed && duration > TIME_PRESS_BUTTON) {
                    vibrate();
                }
            });

        });
        binding.switchEnableCall.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                buttonView.setChecked(checkCallPermissions() && checkExistNumberConfig() != null);
            }
        });

        binding.version.setText(getVersion(getContext()));
    }

    private String checkExistNumberConfig() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        String number = preferences.getString(NUMBER_KEY, "");
        if (!number.isEmpty()) {
            return number;
        }
        EditText edit = new EditText(getContext());
        edit.setInputType(InputType.TYPE_CLASS_PHONE);
        new MaterialAlertDialogBuilder(getContext())
                .setView(edit)
                .setTitle(R.string.emergency_number)
                .setNegativeButton(android.R.string.cancel,
                        (dialog, which) -> getActivity().finish())
                .setPositiveButton(android.R.string.ok,
                        (dialog, which) -> {
                            if (!edit.getText().toString().isEmpty()) {
                                preferences.edit().putString(NUMBER_KEY, edit.getText().toString()).commit();
                            }
                        })
                .setCancelable(false)
                .show();
        return null;
    }

    private void vibrate() {
        Vibrator v = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(5000, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            v.vibrate(5000);
        }
    }

    private void callPhone() {
        String number = checkExistNumberConfig();
        if (number != null) {
            call = true;
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse("tel:" + number));
            startActivityForResult(callIntent, REQUEST_CODE_CALL);
        }

    }

    private void readValues() {
        pikkuAcademy.turnOnLed();
        pikkuAcademy.readStatusDevice(statusDevice -> updateBatteryIU(statusDevice.battery));

    }


    private void startRestChronometer() {
        if (!startRest && binding.switchActivity.isChecked()) {
            binding.rest.setBase(SystemClock.elapsedRealtime() + timeWhenStoppedRest);
            binding.rest.start();
            startRest = true;
        }
    }

    private void stopRestChronometer() {
        if (startRest ) {
            startRest = false;
            binding.rest.stop();
            timeWhenStoppedRest = binding.rest.getBase() - SystemClock.elapsedRealtime();
        }
    }

    private void startStandChronometer() {
        if (!startStandUp && binding.switchActivity.isChecked()) {
            binding.stand.setBase(SystemClock.elapsedRealtime() + timeWhenStoppedStandUp);
            binding.stand.start();
            startStandUp = true;
        }
    }

    private void stopStandChronometer() {
        if (startStandUp) {
            startStandUp = false;
            binding.stand.stop();
            timeWhenStoppedStandUp = binding.stand.getBase() - SystemClock.elapsedRealtime();
        }
    }

    private void TurnOffPikku() {
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
    }

    private void checkIsConnected() {
        if (pikkuAcademy.isConnected()) {
            updateUI(true);
            readValues();
        }
    }


    private void connect() {
        if (pikkuAcademy.isConnected()) {
            movementDetector.writeDataToFile(getContext());
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
        binding.connectProgress.setVisibility(View.GONE);
        binding.switchActivity.setEnabled(connected);
        binding.switchSos.setEnabled(connected);
        stopStandChronometer();
        stopRestChronometer();
        binding.switchActivity.setChecked(false);


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

    private boolean checkCallPermissions() {
        String permission = Manifest.permission.CALL_PHONE;
        int permissionCheck = ContextCompat.checkSelfPermission(getContext(), permission);
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        ActivityCompat.requestPermissions(getActivity(), new String[]{permission}, REQUEST_CODE_PERMISSION_LOCATION);
        return false;
    }

    private void onPermissionGranted(String permission) {
        if (Manifest.permission.ACCESS_FINE_LOCATION.equals(permission)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !pikkuAcademy.checkGPSIsOpen(getActivity())) {
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CALL) {
            call = false;
        }
    }

    @Override
    public void onStep(int steps, float distance) {
        binding.steps.setText(String.valueOf(steps));
        binding.distance.setText(String.format("%.1f", distance));
        stopRestChronometer();
        stopStandChronometer();
    }

    @Override
    public void onJump(int jumps) {
        stopRestChronometer();
        stopStandChronometer();
        binding.jumps.setText(String.valueOf(jumps));
    }

    @Override
    public void onStand() {
        stopRestChronometer();
        startStandChronometer();
    }

    @Override
    public void onRest() {
        startRestChronometer();
        stopStandChronometer();
    }
}