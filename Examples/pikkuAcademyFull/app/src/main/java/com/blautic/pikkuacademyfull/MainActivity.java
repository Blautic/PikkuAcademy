package com.blautic.pikkuacademyfull;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.blautic.pikkuacademyfull.databinding.ActivityMainBinding;
import com.blautic.pikkuAcademyLib.PikkuAcademy;

public class MainActivity extends AppCompatActivity {

    public static final int REQUEST_CODE_OPEN_GPS = 1002;
    public static final int REQUEST_CODE_ENABLE_BLUETOOTH = 1002;
    public static final int REQUEST_CODE_PERMISSION_LOCATION = 1003;


    private ActivityMainBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        PikkuAcademy.getInstance(this).destroy();
    }
}