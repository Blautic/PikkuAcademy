package com.blautic.pikkuacademyfull;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.blautic.pikkuacademyfull.databinding.FragmentSettingsBinding;
import com.blautic.pikkuAcademyLib.PikkuAcademy;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.jetbrains.annotations.NotNull;

public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;
    private PikkuAcademy pikkuAcademy;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        pikkuAcademy = PikkuAcademy.getInstance(getContext());

        binding.connectButton.setOnClickListener(v -> {
            pikkuAcademy.disconnect();
            NavHostFragment.findNavController(SettingsFragment.this)
                    .navigate(R.id.action_settingsFragment_to_ConnectFragment);
        });
        binding.deletButton.setOnClickListener(v -> new MaterialAlertDialogBuilder(getActivity())
                .setTitle(R.string.delete_device)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    pikkuAcademy.deleteDevice();
                    pikkuAcademy.disconnect();
                    binding.mac.setText("");
                    binding.name.setText("");
                    binding.version.setText("");

                }).setNegativeButton(android.R.string.cancel, null)
                .show());

        binding.toolbar.setNavigationOnClickListener(v -> {
            getActivity().onBackPressed();
        });
        binding.name.setText(String.valueOf(pikkuAcademy.getNameDevice()));
        binding.mac.setText(String.valueOf(pikkuAcademy.getAddressDevice()));
        binding.version.setText(String.valueOf(pikkuAcademy.getFirmwareDevice()));
        binding.cardDevice.setOnClickListener(v -> {
            Intent settings = new Intent(getContext(), SensorSettingsActivity.class);
            startActivity(settings);
        });

    }


}