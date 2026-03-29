package com.example.pma_calculator;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class StartActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
    }

    // Кнопка "Калькулятор"
    public void onOpenCalculator(View v) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    // Кнопка "Выход"
    public void onExitApp(View v) {
        finishAffinity();
    }
}