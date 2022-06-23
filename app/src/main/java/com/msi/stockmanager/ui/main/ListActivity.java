package com.msi.stockmanager.ui.main;

import androidx.appcompat.app.AppCompatActivity;

import com.msi.stockmanager.databinding.ActivityListBinding;

import android.os.Bundle;

public class ListActivity extends AppCompatActivity {

    ActivityListBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

    }
}