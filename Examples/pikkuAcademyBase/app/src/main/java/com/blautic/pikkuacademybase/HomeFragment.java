package com.blautic.pikkuacademybase;

import android.Manifest;
import android.content.Intent;
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
import com.blautic.pikkuAcademyLib.PikkuAcademy;
import com.blautic.pikkuacademybase.databinding.FragmentHomeBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;
import timber.log.Timber;

import static com.blautic.pikkuacademybase.MainActivity.REQUEST_CODE_ENABLE_BLUETOOTH;
import static com.blautic.pikkuacademybase.MainActivity.REQUEST_CODE_OPEN_GPS;
import static com.blautic.pikkuacademybase.MainActivity.REQUEST_CODE_PERMISSION_LOCATION;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private PikkuAcademy pikkuAcademy;

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

    }

    public void connect() {
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

    public void updateUI(boolean connected) {
        binding.connect.setImageResource(connected ? R.drawable.ic_ble_connected : R.drawable.ic_ble_disconnected);
        binding.connectProgress.setVisibility(View.GONE);
    }

    public void readValues() {

        pikkuAcademy.readRssiConnectedDevice(rssi -> {
            Timber.d("rssi: %s", rssi);
        });

        pikkuAcademy.readStatusDevice(statusDevice -> {
            Timber.d("readBatteryLevel: %s", statusDevice.battery);
            updateBatteryLevel(statusDevice.battery);
        });

    }

    public void updateBatteryLevel(int value) {
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !pikkuAcademy.checkGPSIsOpen(getActivity())) {
                new AlertDialog.Builder(getContext())
                        .setMessage(R.string.enable_gps)
                        .setNegativeButton(android.R.string.cancel,
                                (dialog, which) -> getActivity().finish())
                        .setPositiveButton(R.string.action_settings,
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
    public void onDestroyView() {
        super.onDestroyView();
    }

}