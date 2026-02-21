package com.example.pma_calculator;

import android.os.Bundle;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleUnaryOperator;

public class CalculatorTwoOperands {

    public enum Op { NONE, ADD, SUB, MUL, DIV, POW }

    private final StringBuilder input = new StringBuilder();
    private String operand1 = null;
    private Op op = Op.NONE;
    private boolean resultShown = false;

    private String lastError = "";

    public void appendDigit(int digit) {
        if (resultShown) clear();

        if (input.length() == 0) {
            if (digit == 0) {
                input.append('0');
            } else {
                input.append((char) ('0' + digit));
            }
            return;
        }

        if (input.length() == 1 && input.charAt(0) == '0' && input.indexOf(".") < 0) {
            if (digit == 0) return;
            input.setLength(0);
            input.append((char) ('0' + digit));
            return;
        }

        input.append((char) ('0' + digit));
    }

    public boolean appendComma() {
        if (resultShown) clear();
        if (input.indexOf(".") >= 0) {
            lastError = "Число уже содержит запятую.";
            return false;
        }
        if (input.length() == 0) input.append("0");
        input.append(".");
        return true;
    }

    public void backspace() {
        if (resultShown) { clear(); return; }
        if (input.length() > 0)
            input.deleteCharAt(input.length() - 1);
    }

    public void clear() {
        input.setLength(0);
        operand1 = null;
        op = Op.NONE;
        resultShown = false;
    }

    public void setOp(Op newOp) {
        if (!hasInput()) {
            lastError = "Введите число.";
            return;
        }
        operand1 = input.toString();
        input.setLength(0);
        op = newOp;
        resultShown = false;
    }

    public boolean equals() {
        return applyBinary((a, b) -> {
            switch (op) {
                case ADD: return a + b;
                case SUB: return a - b;
                case MUL: return a * b;
                case DIV:
                    if (b == 0) throw new ArithmeticException("Деление на ноль!");
                    return a / b;
                case POW: return Math.pow(a, b);
                default: throw new IllegalStateException();
            }
        });
    }

    public boolean applySin() { return applyUnary(Math::sin); }
    public boolean applyCos() { return applyUnary(Math::cos); }

    private boolean applyUnary(DoubleUnaryOperator operator) {
        if (!hasInput()) {
            lastError = "Введите число.";
            return false;
        }
        try {
            double value = parse(input.toString());
            double result = operator.applyAsDouble(value);
            showResult(result);
            return true;
        } catch (Exception e) {
            lastError = "Ошибка вычисления.";
            return false;
        }
    }

    private boolean applyBinary(DoubleBinaryOperator operator) {
        if (operand1 == null) {
            lastError = "Недостаточно данных.";
            return false;
        }

        String bText = (input.length() == 0) ? "0" : input.toString();

        try {
            double a = parse(operand1);
            double b = parse(bText);
            double result = operator.applyAsDouble(a, b);
            showResult(result);
            return true;
        } catch (Exception e) {
            lastError = e.getMessage() != null ? e.getMessage() : "Ошибка вычисления.";
            return false;
        }
    }

    private void showResult(double value) {
        input.setLength(0);
        input.append(format(value));
        operand1 = null;
        op = Op.NONE;
        resultShown = true;
    }

    public String getCurrentText() {
        return input.length() == 0 ? "0" : input.toString().replace('.', ',');
    }

    public String getExpressionText() {
        if (operand1 == null || op == Op.NONE) return "";

        String symbol;

        switch (op) {
            case ADD:
                symbol = "+";
                break;
            case SUB:
                symbol = "−";
                break;
            case MUL:
                symbol = "×";
                break;
            case DIV:
                symbol = "÷";
                break;
            case POW:
                symbol = "^";
                break;
            default:
                symbol = "";
        }

        return operand1.replace('.', ',') + " " + symbol;
    }

    public String getLastError() {
        return lastError;
    }

    private boolean hasInput() {
        return input.length() > 0;
    }

    private double parse(String s) {
        return Double.parseDouble(s);
    }

    private String format(double value) {
        return (value == (long) value)
                ? String.valueOf((long) value)
                : String.valueOf(value);
    }

    public void saveTo(Bundle out) {
        out.putString("calc_input", input.toString());
        out.putString("calc_operand1", operand1);
        out.putString("calc_op", op.name());
        out.putBoolean("calc_resultShown", resultShown);
    }

    public void restoreFrom(Bundle in) {
        input.setLength(0);
        input.append(in.getString("calc_input", ""));
        operand1 = in.getString("calc_operand1", null);

        try { op = Op.valueOf(in.getString("calc_op", Op.NONE.name())); }
        catch (Exception e) { op = Op.NONE; }

        resultShown = in.getBoolean("calc_resultShown", false);
    }
}