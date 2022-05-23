package com.msi.stockmanager;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;

import androidx.navigation.ui.AppBarConfiguration;

import com.msi.stockmanager.databinding.ActivityOverviewBinding;


public class OverviewActivity extends AppCompatActivity {

    private ActivityOverviewBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityOverviewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);

        binding.btnHost.setOnClickListener(v->{
            Intent intent = new Intent(OverviewActivity.this, HostActivity.class);
            startActivity(intent);
        });
        binding.fabOverviewAddCash.setOnClickListener(v -> {
            //TODO add cash transaction
        });
        binding.fabOverviewAddStock.setOnClickListener(v -> {
            //TODO add stock transaction
        });
        binding.fabOverviewAddOther.setOnClickListener(v -> {
            //TODO add dividend or reduction transaction
        });
    }
}