package com.blautic.pikkuacademysnake;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.blautic.pikkuAcademyLib.PikkuAcademy;
import com.blautic.pikkuAcademyLib.ScanInfo;
import com.blautic.pikkuAcademyLib.callback.ScanCallback;
import com.blautic.pikkuacademysnake.databinding.FragmentConnectBinding;

public class ConnectFragment extends Fragment {

    private FragmentConnectBinding binding;
    private PikkuAcademy pikkuAcademy;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentConnectBinding.inflate(inflater, container, false);
        pikkuAcademy = PikkuAcademy.getInstance(getContext());
        pikkuAcademy.scan(true, new ScanCallback() {
            @Override
            public void onScan(ScanInfo scanInfo) {
                binding.connectProgress.setVisibility(View.GONE);
                binding.buttonSave.setEnabled(true);
                binding.imagePikku.setAlpha((float) 1.0);
                binding.buttonSave.setOnClickListener(v ->{
                    pikkuAcademy.saveDevice(scanInfo);
                    getActivity().onBackPressed();
                } );
            }

        });


        binding.toolbar.setNavigationOnClickListener(view -> {
            getActivity().onBackPressed();
        });
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

}