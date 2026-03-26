package com.example.pma_calculator;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    // Требования к экрану
    private static final int REQ_MIN_WIDTH_DP = 360;
    private static final int REQ_MIN_HEIGHT_DP = 640;

    private static final String KEY_PANEL_SCI_VISIBLE = "panel_scientific_visible";

    private TextView tvExpr;
    private TextView tvValue;
    private View panelScientific;

    private final CalculatorTwoOperands calc = new CalculatorTwoOperands();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!checkScreenRequirements()) {
            showErrorAndClose("Разрешение/размер экрана не соответствует требованиям приложения.");
            return;
        }

        setContentView(R.layout.activity_main);

        tvExpr = findViewById(R.id.tvExpr);
        tvValue = findViewById(R.id.tvValue);
        panelScientific = findViewById(R.id.panelScientific);

        if (savedInstanceState != null) {
            calc.restoreFrom(savedInstanceState);
            boolean visible = savedInstanceState.getBoolean(KEY_PANEL_SCI_VISIBLE, false);
            panelScientific.setVisibility(visible ? View.VISIBLE : View.GONE);
        }

        refreshUi();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        calc.saveTo(outState);
        outState.putBoolean(KEY_PANEL_SCI_VISIBLE, panelScientific.getVisibility() == View.VISIBLE);
    }

    // --- Цифры ---
    public void onDigit0(View v) { pressDigit(0); }
    public void onDigit1(View v) { pressDigit(1); }
    public void onDigit2(View v) { pressDigit(2); }
    public void onDigit3(View v) { pressDigit(3); }
    public void onDigit4(View v) { pressDigit(4); }
    public void onDigit5(View v) { pressDigit(5); }
    public void onDigit6(View v) { pressDigit(6); }
    public void onDigit7(View v) { pressDigit(7); }
    public void onDigit8(View v) { pressDigit(8); }
    public void onDigit9(View v) { pressDigit(9); }

    // --- Вещественные ---
    public void onComma(View v) {
        if (!calc.appendComma()) showError(calc.getLastError());
        refreshUi();
    }

    // --- Операции ---
    public void onAdd(View v) { pressOp(CalculatorTwoOperands.Op.ADD); }
    public void onSub(View v) { pressOp(CalculatorTwoOperands.Op.SUB); }
    public void onMul(View v) { pressOp(CalculatorTwoOperands.Op.MUL); }
    public void onDiv(View v) { pressOp(CalculatorTwoOperands.Op.DIV); }
    public void onPow(View v) { pressOp(CalculatorTwoOperands.Op.POW); }

    // --- Результат ---
    public void onEq(View v) {
        if (!calc.equals()) showError(calc.getLastError());
        refreshUi();
    }

    // --- Служебные ---
    public void onClear(View v) {
        calc.clear();
        refreshUi();
    }

    public void onBack(View v) {
        calc.backspace();
        refreshUi();
    }

    public void onExit(View v) {
        finish();
    }

    // --- Инженерные функции ---
    public void onSin(View v) {
        if (!calc.applySin()) showError(calc.getLastError());
        refreshUi();
    }

    public void onCos(View v) {
        if (!calc.applyCos()) showError(calc.getLastError());
        refreshUi();
    }

    // --- Панель функций ---
    public void onFunc(View v) {
        toggleScientificPanel();
    }

    private void pressDigit(int digit) {
        calc.appendDigit(digit);
        refreshUi();
    }

    private void pressOp(CalculatorTwoOperands.Op op) {
        calc.setOp(op);
        refreshUi();
    }

    private void toggleScientificPanel() {
        if (panelScientific == null) return;
        panelScientific.setVisibility(panelScientific.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
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