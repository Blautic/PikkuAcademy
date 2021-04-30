package com.blautic.pikkuacademybase;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;
import com.blautic.pikkuAcademyLib.PikkuAcademy;
import com.blautic.pikkuacademybase.databinding.FragmentSettingsBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.jetbrains.annotations.NotNull;

public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;
    private PikkuAcademy pikkuAcademy;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        pikkuAcademy = PikkuAcademy.getInstance(getContext());
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
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
                    binding.name.setText("00:00:00:00:00:00");
                    binding.name.setText("");
                }).setNegativeButton(android.R.string.cancel, null)
                .show());

        binding.toolbar.setNavigationOnClickListener(v -> {
            getActivity().onBackPressed();
        });
        binding.name.setText(String.valueOf(pikkuAcademy.getNameDevice()));
        binding.mac.setText(String.valueOf(pikkuAcademy.getAddressDevice()));
        binding.version.setText(String.valueOf(pikkuAcademy.getFirmwareDevice()));
    }

    private void changeNameDevice(String name) {
        try {
            if (pikkuAcademy.changeNameDevice(name)) {
                showMessage(getString(R.string.rename_ok));
                binding.name.setText(name);
            }
        } catch (Exception e) {
            showMessage(getString(R.string.error_chamge_name));
        }
    }

    public void showMessage(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }
}