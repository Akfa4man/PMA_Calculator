package com.example.pma_calculator;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.TextView;
import android.util.DisplayMetrics;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static final int REQ_MIN_WIDTH_DP = 360;
    private static final int REQ_MIN_HEIGHT_DP = 640;

    private android.view.View panelScientific;

    private TextView tvExpr;
    private TextView tvValue;

    private final CalculatorTwoOperands calc = new CalculatorTwoOperands();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!checkScreenRequirements()) {
            showErrorAndClose("Разрешение/размер экрана не соответствует требованиям приложения.");
            return;
        }

        setContentView(R.layout.activity_main);

        panelScientific = findViewById(R.id.panelScientific);

        tvExpr = findViewById(R.id.tvExpr);
        tvValue = findViewById(R.id.tvValue);

        wireButtons();
        refreshUi();
    }

    private void wireButtons() {
        int[] digitIds = { R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4,
                R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9 };

        for (int i = 0; i < digitIds.length; i++) {
            final int digit = i;
            findViewById(digitIds[i]).setOnClickListener(v -> {
                calc.appendDigit(digit);
                refreshUi();
            });
        }

        findViewById(R.id.btnComma).setOnClickListener(v -> {
            if (!calc.appendComma()) showError(calc.getLastError());
            refreshUi();
        });

        findViewById(R.id.btnAdd).setOnClickListener(v -> { calc.setOp(CalculatorTwoOperands.Op.ADD); refreshUi(); });
        findViewById(R.id.btnSub).setOnClickListener(v -> { calc.setOp(CalculatorTwoOperands.Op.SUB); refreshUi(); });
        findViewById(R.id.btnMul).setOnClickListener(v -> { calc.setOp(CalculatorTwoOperands.Op.MUL); refreshUi(); });
        findViewById(R.id.btnDiv).setOnClickListener(v -> { calc.setOp(CalculatorTwoOperands.Op.DIV); refreshUi(); });

        findViewById(R.id.btnPow).setOnClickListener(v -> { calc.setOp(CalculatorTwoOperands.Op.POW); refreshUi(); });

        findViewById(R.id.btnSin).setOnClickListener(v -> {
            if (!calc.applySin()) showError(calc.getLastError());
            refreshUi();
        });

        findViewById(R.id.btnCos).setOnClickListener(v -> {
            if (!calc.applyCos()) showError(calc.getLastError());
            refreshUi();
        });

        findViewById(R.id.btnClear).setOnClickListener(v -> { calc.clear(); refreshUi(); });

        findViewById(R.id.btnBack).setOnClickListener(v -> { calc.backspace(); refreshUi(); });

        findViewById(R.id.btnEq).setOnClickListener(v -> {
            if (!calc.equals()) showError(calc.getLastError());
            refreshUi();
        });

        findViewById(R.id.btnFunc).setOnClickListener(v -> {
            if (panelScientific.getVisibility() == android.view.View.VISIBLE) {
                panelScientific.setVisibility(android.view.View.GONE);
            } else {
                panelScientific.setVisibility(android.view.View.VISIBLE);
            }
        });
    }

    private void refreshUi() {
        tvExpr.setText(calc.getExpressionText());
        tvValue.setText(calc.getCurrentText());
    }

    private boolean checkScreenRequirements() {
        DisplayMetrics dm = getResources().getDisplayMetrics();

        float widthDp = dm.widthPixels / dm.density;
        float heightDp = dm.heightPixels / dm.density;

        float minSide = Math.min(widthDp, heightDp);
        float maxSide = Math.max(widthDp, heightDp);

        return minSide >= REQ_MIN_WIDTH_DP && maxSide >= REQ_MIN_HEIGHT_DP;
    }

    private void showError(String message) {
        new AlertDialog.Builder(this)
                .setTitle("Ошибка")
                .setMessage(message)
                .setPositiveButton("ОК", null)
                .show();
    }

    private void showErrorAndClose(String message) {
        new AlertDialog.Builder(this)
                .setTitle("Ошибка")
                .setMessage(message)
                .setPositiveButton("ОК", (d, w) -> finish())
                .setCancelable(false)
                .show();
    }
}