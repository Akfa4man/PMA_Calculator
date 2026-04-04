package com.example.pma_calculator;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewConfiguration;

import java.util.Locale;

public class GraphCanvasView extends View {

    private static final int GRID_STEP = 30;

    // Базовый диапазон x задаётся в коде приложения
    private static final double BASE_X_MIN = -2.0 * Math.PI;
    private static final double BASE_X_MAX =  2.0 * Math.PI;

    // Базовый диапазон y немного шире [-1; 1], чтобы график не прилипал к краям
    private static final double BASE_Y_MIN = -1.2;
    private static final double BASE_Y_MAX =  1.2;

    private static final float MIN_ZOOM = 0.5f;
    private static final float MAX_ZOOM = 8.0f;

    // Числа на осях показываем только при достаточном приближении
    private static final float AXIS_LABEL_MIN_ZOOM = 1.8f;

    private final Paint gridPaint = new Paint();
    private final Paint axisPaint = new Paint();
    private final Paint graphPaint = new Paint();
    private final Paint pointPaint = new Paint();
    private final Paint pointLinePaint = new Paint();
    private final Paint textPaint = new Paint();

    private final ScaleGestureDetector scaleDetector;
    private final GestureDetector gestureDetector;
    private final int touchSlop;

    private float zoom = 1.0f;
    private double centerX = 0.0;
    private double centerY = 0.0;

    private boolean pointSelected = false;
    private double selectedX = 0.0;
    private double selectedY = 0.0;

    private float lastTouchX = 0f;
    private float lastTouchY = 0f;
    private boolean isDragging = false;

    private OnPointSelectedListener onPointSelectedListener;

    public interface OnPointSelectedListener {
        void onPointSelected(double x, double y);
    }

    public GraphCanvasView(Context context) {
        super(context);
        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        scaleDetector = new ScaleGestureDetector(context, new ScaleListener());
        gestureDetector = new GestureDetector(context, new TapListener());
        init();
    }

    public GraphCanvasView(Context context, AttributeSet attrs) {
        super(context, attrs);
        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        scaleDetector = new ScaleGestureDetector(context, new ScaleListener());
        gestureDetector = new GestureDetector(context, new TapListener());
        init();
    }

    public GraphCanvasView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        scaleDetector = new ScaleGestureDetector(context, new ScaleListener());
        gestureDetector = new GestureDetector(context, new TapListener());
        init();
    }

    private void init() {
        gridPaint.setColor(Color.LTGRAY);
        gridPaint.setStrokeWidth(1f);
        gridPaint.setStyle(Paint.Style.STROKE);

        axisPaint.setColor(Color.DKGRAY);
        axisPaint.setStrokeWidth(3f);
        axisPaint.setStyle(Paint.Style.STROKE);
        axisPaint.setAntiAlias(true);

        graphPaint.setColor(Color.RED);
        graphPaint.setStrokeWidth(4f);
        graphPaint.setStyle(Paint.Style.STROKE);
        graphPaint.setAntiAlias(true);

        pointPaint.setColor(Color.BLUE);
        pointPaint.setStyle(Paint.Style.FILL);
        pointPaint.setAntiAlias(true);

        pointLinePaint.setColor(Color.BLUE);
        pointLinePaint.setStrokeWidth(2f);
        pointLinePaint.setStyle(Paint.Style.STROKE);
        pointLinePaint.setAntiAlias(true);

        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(24f);
        textPaint.setAntiAlias(true);

        setBackgroundColor(Color.WHITE);
    }

    public void setOnPointSelectedListener(OnPointSelectedListener listener) {
        this.onPointSelectedListener = listener;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();

        drawGrid(canvas, width, height);
        drawAxes(canvas, width, height);
        drawAxisLabels(canvas, width, height);
        drawGraph(canvas, width, height);

        if (pointSelected) {
            drawSelectedPoint(canvas, width, height);
        }
    }

    private void drawGrid(Canvas canvas, int width, int height) {
        // Шаг сетки всегда 30x30 пикселей — основное ТЗ сохраняется
        for (int x = 0; x <= width; x += GRID_STEP) {
            canvas.drawLine(x, 0, x, height, gridPaint);
        }

        for (int y = 0; y <= height; y += GRID_STEP) {
            canvas.drawLine(0, y, width, y, gridPaint);
        }
    }

    private void drawAxes(Canvas canvas, int width, int height) {
        float xAxisY = mapY(0.0, height);
        float yAxisX = mapX(0.0, width);

        if (xAxisY >= 0 && xAxisY <= height) {
            canvas.drawLine(0, xAxisY, width, xAxisY, axisPaint);
        }

        if (yAxisX >= 0 && yAxisX <= width) {
            canvas.drawLine(yAxisX, 0, yAxisX, height, axisPaint);
        }
    }

    private void drawAxisLabels(Canvas canvas, int width, int height) {
        if (zoom < AXIS_LABEL_MIN_ZOOM) {
            return;
        }

        float xAxisY = mapY(0.0, height);
        float yAxisX = mapX(0.0, width);

        boolean xAxisVisible = xAxisY >= 0 && xAxisY <= height;
        boolean yAxisVisible = yAxisX >= 0 && yAxisX <= width;

        if (!xAxisVisible && !yAxisVisible) {
            return;
        }

        double xStep = calculateNiceStep(getVisibleXSpan(), width);
        double yStep = calculateNiceStep(getVisibleYSpan(), height);

        if (xAxisVisible) {
            drawXLabels(canvas, width, xAxisY, xStep);
        }

        if (yAxisVisible) {
            drawYLabels(canvas, height, yAxisX, yStep);
        }
    }

    private void drawXLabels(Canvas canvas, int width, float xAxisY, double step) {
        double left = getVisibleLeft();
        double right = left + getVisibleXSpan();

        double start = Math.ceil(left / step) * step;

        for (double value = start; value <= right; value += step) {
            float sx = mapX(value, width);

            if (sx < 0 || sx > width) {
                continue;
            }

            // Небольшая риска на оси
            canvas.drawLine(sx, xAxisY - 8f, sx, xAxisY + 8f, axisPaint);

            String text = formatAxisValue(value);
            float textWidth = textPaint.measureText(text);

            float textX = sx - textWidth / 2f;
            float textY = xAxisY + 28f;

            if (textX < 2f) textX = 2f;
            if (textX + textWidth > width - 2f) textX = width - textWidth - 2f;

            if (textY > getHeight() - 4f) {
                textY = xAxisY - 12f;
            }

            canvas.drawText(text, textX, textY, textPaint);
        }
    }

    private void drawYLabels(Canvas canvas, int height, float yAxisX, double step) {
        double top = getVisibleTop();
        double bottom = top - getVisibleYSpan();

        double start = Math.ceil(bottom / step) * step;

        for (double value = start; value <= top; value += step) {
            float sy = mapY(value, height);

            if (sy < 0 || sy > height) {
                continue;
            }

            // Небольшая риска на оси
            canvas.drawLine(yAxisX - 8f, sy, yAxisX + 8f, sy, axisPaint);

            String text = formatAxisValue(value);
            float textWidth = textPaint.measureText(text);

            float textX = yAxisX + 10f;
            float textY = sy - 6f;

            if (textX + textWidth > getWidth() - 2f) {
                textX = yAxisX - textWidth - 10f;
            }

            if (textY < 20f) {
                textY = sy + 20f;
            }

            canvas.drawText(text, textX, textY, textPaint);
        }
    }

    private double calculateNiceStep(double visibleSpan, int pixelSize) {
        double minPixelSpacing = 90.0;
        double rawStep = visibleSpan / Math.max(1.0, pixelSize / minPixelSpacing);
        return niceNumber(rawStep);
    }

    private double niceNumber(double value) {
        double exponent = Math.floor(Math.log10(value));
        double fraction = value / Math.pow(10.0, exponent);

        double niceFraction;
        if (fraction <= 1.0) {
            niceFraction = 1.0;
        } else if (fraction <= 2.0) {
            niceFraction = 2.0;
        } else if (fraction <= 5.0) {
            niceFraction = 5.0;
        } else {
            niceFraction = 10.0;
        }

        return niceFraction * Math.pow(10.0, exponent);
    }

    private String formatAxisValue(double value) {
        if (Math.abs(value) < 1e-9) {
            value = 0.0;
        }

        if (Math.abs(value - Math.rint(value)) < 1e-9) {
            return String.format(Locale.US, "%.0f", value).replace('.', ',');
        }

        if (Math.abs(value) >= 10.0) {
            return String.format(Locale.US, "%.1f", value).replace('.', ',');
        }

        return String.format(Locale.US, "%.2f", value).replace('.', ',');
    }

    private void drawGraph(Canvas canvas, int width, int height) {
        Path path = new Path();
        boolean firstPoint = true;

        for (int px = 0; px <= width; px++) {
            double x = screenToMathX(px, width);
            double y = calculateY(x);

            float screenX = mapX(x, width);
            float screenY = mapY(y, height);

            if (firstPoint) {
                path.moveTo(screenX, screenY);
                firstPoint = false;
            } else {
                path.lineTo(screenX, screenY);
            }
        }

        canvas.drawPath(path, graphPaint);
    }

    private void drawSelectedPoint(Canvas canvas, int width, int height) {
        float sx = mapX(selectedX, width);
        float sy = mapY(selectedY, height);

        canvas.drawLine(sx, 0, sx, height, pointLinePaint);
        canvas.drawLine(0, sy, width, sy, pointLinePaint);
        canvas.drawCircle(sx, sy, 10f, pointPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        scaleDetector.onTouchEvent(event);
        gestureDetector.onTouchEvent(event);

        if (event.getPointerCount() > 1) {
            isDragging = false;
            return true;
        }

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                lastTouchX = event.getX();
                lastTouchY = event.getY();
                isDragging = false;
                return true;

            case MotionEvent.ACTION_MOVE:
                if (scaleDetector.isInProgress()) {
                    return true;
                }

                float dx = event.getX() - lastTouchX;
                float dy = event.getY() - lastTouchY;

                if (!isDragging) {
                    if (Math.hypot(dx, dy) >= touchSlop) {
                        isDragging = true;
                    }
                }

                if (isDragging) {
                    panByPixels(dx, dy, getWidth(), getHeight());
                    lastTouchX = event.getX();
                    lastTouchY = event.getY();
                    invalidate();
                }

                return true;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                isDragging = false;
                return true;

            default:
                return super.onTouchEvent(event);
        }
    }

    private void panByPixels(float dx, float dy, int width, int height) {
        double visibleXSpan = getVisibleXSpan();
        double visibleYSpan = getVisibleYSpan();

        centerX -= dx / width * visibleXSpan;
        centerY += dy / height * visibleYSpan;
    }

    private double calculateY(double x) {
        return Math.sin(3.0 * x) * Math.cos(2.0 * x);
    }

    private double getVisibleXSpan() {
        return (BASE_X_MAX - BASE_X_MIN) / zoom;
    }

    private double getVisibleYSpan() {
        return (BASE_Y_MAX - BASE_Y_MIN) / zoom;
    }

    private double getVisibleLeft() {
        return centerX - getVisibleXSpan() / 2.0;
    }

    private double getVisibleTop() {
        return centerY + getVisibleYSpan() / 2.0;
    }

    private float mapX(double x, int width) {
        double left = getVisibleLeft();
        double span = getVisibleXSpan();
        return (float) ((x - left) / span * width);
    }

    private float mapY(double y, int height) {
        double top = getVisibleTop();
        double span = getVisibleYSpan();
        return (float) ((top - y) / span * height);
    }

    private double screenToMathX(float screenX, int width) {
        return getVisibleLeft() + (screenX / width) * getVisibleXSpan();
    }

    private double screenToMathY(float screenY, int height) {
        return getVisibleTop() - (screenY / height) * getVisibleYSpan();
    }

    private void selectPointAt(float screenX, int width) {
        selectedX = screenToMathX(screenX, width);
        selectedY = calculateY(selectedX);
        pointSelected = true;

        if (onPointSelectedListener != null) {
            onPointSelectedListener.onPointSelected(selectedX, selectedY);
        }

        invalidate();
    }

    private class TapListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            if (!scaleDetector.isInProgress() && !isDragging) {
                selectPointAt(e.getX(), getWidth());
                return true;
            }
            return false;
        }
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            int width = getWidth();
            int height = getHeight();

            if (width <= 0 || height <= 0) {
                return false;
            }

            double focusMathXBefore = screenToMathX(detector.getFocusX(), width);
            double focusMathYBefore = screenToMathY(detector.getFocusY(), height);

            zoom *= detector.getScaleFactor();
            zoom = Math.max(MIN_ZOOM, Math.min(zoom, MAX_ZOOM));

            double newXSpan = getVisibleXSpan();
            double newYSpan = getVisibleYSpan();

            centerX = focusMathXBefore - (detector.getFocusX() / width - 0.5) * newXSpan;
            centerY = focusMathYBefore - (0.5 - detector.getFocusY() / height) * newYSpan;

            invalidate();
            return true;
        }
    }
}