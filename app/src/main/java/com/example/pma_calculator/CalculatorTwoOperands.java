package com.example.pma_calculator;

public class CalculatorTwoOperands {

    public enum Op { NONE, ADD, SUB, MUL, DIV, POW }

    private StringBuilder input = new StringBuilder();
    private String operand1 = null;
    private Op op = Op.NONE;

    private boolean enteringSecond = false;
    private boolean resultShown = false;

    private String lastError = null;
    public String getLastError() { return lastError; }

    public void clear() {
        input.setLength(0);
        operand1 = null;
        op = Op.NONE;
        enteringSecond = false;
        resultShown = false;
        lastError = null;
    }

    public void backspace() {
        lastError = null;
        if (resultShown) {
            input.setLength(0);
            resultShown = false;
            return;
        }
        if (input.length() > 0) input.deleteCharAt(input.length() - 1);
    }

    public void appendDigit(int d) {
        lastError = null;
        if (resultShown) clear();
        input.append(d);
    }

    public boolean appendComma() {
        lastError = null;
        if (resultShown) clear();

        if (input.indexOf(",") >= 0) {
            lastError = "В числе уже есть запятая.";
            return false;
        }
        if (input.length() == 0) input.append("0");
        input.append(",");
        return true;
    }

    public void setOp(Op newOp) {
        lastError = null;

        if (!enteringSecond) {
            operand1 = normalizeNumber(input.length() == 0 ? "0" : input.toString());
            input.setLength(0);
            enteringSecond = true;
        }
        op = newOp;
        resultShown = false;
    }

    public boolean equals() {
        lastError = null;

        if (op == Op.NONE) {
            lastError = "Оператор не выбран.";
            return false;
        }
        if (operand1 == null) {
            lastError = "Первый операнд не задан.";
            return false;
        }

        String operand2Text = input.length() == 0 ? "0" : input.toString();

        Double a = parseOrError(operand1);
        Double b = parseOrError(normalizeNumber(operand2Text));
        if (a == null || b == null) return false;

        double res;
        switch (op) {
            case ADD: res = a + b; break;
            case SUB: res = a - b; break;
            case MUL: res = a * b; break;
            case DIV:
                if (b == 0.0) {
                    lastError = "Деление на 0 запрещено.";
                    return false;
                }
                res = a / b;
                break;
            case POW:
                res = Math.pow(a, b);
                break;
            default:
                lastError = "Неизвестная операция.";
                return false;
        }

        input.setLength(0);
        input.append(formatNumber(res));

        operand1 = null;
        op = Op.NONE;
        enteringSecond = false;
        resultShown = true;
        return true;
    }

    public boolean applySin() { return applyUnaryTrig(true); }
    public boolean applyCos() { return applyUnaryTrig(false); }

    private boolean applyUnaryTrig(boolean sin) {
        lastError = null;

        String xText = input.length() == 0 ? "0" : input.toString();
        Double x = parseOrError(normalizeNumber(xText));
        if (x == null) return false;

        // sin/cos работают в радианах
        double res = sin ? Math.sin(x) : Math.cos(x);

        input.setLength(0);
        input.append(formatNumber(res));
        resultShown = true;
        return true;
    }

    public String getExpressionText() {
        String a = operand1 == null ? "" : denormalizeNumber(operand1);
        String opText = opToText(op);
        if (!enteringSecond) return a.isEmpty() ? "" : a;
        return (a.isEmpty() ? "0" : a) + " " + opText;
    }

    public String getCurrentText() {
        return input.length() == 0 ? "0" : input.toString();
    }

    private String opToText(Op op) {
        switch (op) {
            case ADD: return "+";
            case SUB: return "−";
            case MUL: return "×";
            case DIV: return "÷";
            case POW: return "^";
            default: return "";
        }
    }

    private String normalizeNumber(String s) { return s.replace(',', '.'); }
    private String denormalizeNumber(String s) { return s.replace('.', ','); }

    private Double parseOrError(String s) {
        try { return Double.parseDouble(s); }
        catch (Exception e) { lastError = "Некорректное число."; return null; }
    }

    private String formatNumber(double v) {
        String s = Double.toString(v);
        if (s.contains("E") || s.contains("e")) return denormalizeNumber(s);

        if (s.contains(".")) {
            while (s.endsWith("0")) s = s.substring(0, s.length() - 1);
            if (s.endsWith(".")) s = s.substring(0, s.length() - 1);
        }
        return denormalizeNumber(s);
    }
}