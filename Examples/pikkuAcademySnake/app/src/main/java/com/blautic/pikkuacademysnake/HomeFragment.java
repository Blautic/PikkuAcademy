package com.blautic.pikkuacademysnake;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.RotateAnimation;

import androidx.annotation.NonNull;
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
import com.blautic.pikkuacademysnake.databinding.FragmentHomeBinding;
import com.blautic.pikkuacademysnake.data.GameType;
import com.blautic.pikkuacademysnake.view.SnakeCallback;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import timber.log.Timber;

import static com.blautic.pikkuacademysnake.utils.Utils.getVersion;

public class HomeFragment extends Fragment implements SnakeCallback {

    private final String SCORE_KEY_SETTINGS = "max_score";
    private FragmentHomeBinding binding;
    private PikkuAcademy pikkuAcademy;
    private long stepPrevTime;
    private SharedPreferences preferences;


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
        preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        binding.maxScore.setText(String.valueOf(preferences.getInt(SCORE_KEY_SETTINGS, 0)));
        binding.startButton.setOnClickListener(v -> {
            binding.gameOver.setVisibility(View.INVISIBLE);
            binding.snakeView.reStartGame();
        });

        binding.version.setText(getVersion(getContext()));
        binding.snakeView.setCallBack(this);

    }

    private void vibrate() {
        Vibrator v = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            v.vibrate(500);
        }
    }

    private void animImg(View view, int grades) {
        view.clearAnimation();
        RotateAnimation animation = new RotateAnimation(0, grades,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f);

        animation.setDuration(500);
        view.startAnimation(animation);
    }


    @Override
    public void onGameOver(int count) {
        vibrate();
        binding.gameOver.setVisibility(View.VISIBLE);
        if (count > preferences.getInt(SCORE_KEY_SETTINGS, 0)) {
            preferences.edit().putInt(SCORE_KEY_SETTINGS, count).apply();
            binding.maxScore.setText(String.valueOf(count));
        }
    }

    @Override
    public void onCatchFood(int count) {
        binding.score.setText(String.valueOf(count));
    }

    private void readValues() {
        pikkuAcademy.turnOnLed();
        binding.gameOver.setVisibility(View.INVISIBLE);
        binding.snakeView.reStartGame();
        stepPrevTime = 0;
        pikkuAcademy.readStatusDevice(new StatusDeviceCallback() {
            @Override
            public void onReadSuccess(StatusDevice statusDevice) {
                updateBatteryIU(statusDevice.battery);
            }
        });
        pikkuAcademy.readAccelerometer(new AccelerometerCallback() {
            @Override
            public void onReadSuccess(float x, float y, float z) {
            }

            @Override
            public void onReadAngles(float xy, float zy, float xz) {

                long stepStartTime = System.currentTimeMillis();
                //Timber.d("xy: " + xy + "  zy: " + zy + "  xz: " + xz);

                if ((stepStartTime - stepPrevTime) > 300f) {
                    if (Math.abs(xy) > 107) {
                        animImg(binding.imgPikku, -20);
                        stepPrevTime = stepStartTime;
                        binding.snakeView.setSnakeDirection(GameType.TOP);
                    } else if (Math.abs(xy) < 50) {
                        animImg(binding.imgPikku, 20);
                        stepPrevTime = stepStartTime;
                        binding.snakeView.setSnakeDirection(GameType.BOTTOM);
                    } else if (Math.abs(xz) > 107) {
                        stepPrevTime = stepStartTime;
                        binding.snakeView.setSnakeDirection(GameType.LEFT);
                    } else if (Math.abs(xz) < 50) {
                        stepPrevTime = stepStartTime;
                        binding.snakeView.setSnakeDirection(GameType.RIGHT);
                    }
                }
            }
        });
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
                    case DISCONNECTED:{
                        binding.snakeView.stopGame();
                    }
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
        binding.startButton.setEnabled(connected);

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
                                pikkuAcademy.enableBluetooth(getActivity(), MainActivity.REQUEST_CODE_ENABLE_BLUETOOTH);
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
            ActivityCompat.requestPermissions(getActivity(), new String[]{permission}, MainActivity.REQUEST_CODE_PERMISSION_LOCATION);
        }

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
                                    startActivityForResult(intent, MainActivity.REQUEST_CODE_OPEN_GPS);
                                })

                        .setCancelable(false)
                        .show();
            }
        }
    }

}