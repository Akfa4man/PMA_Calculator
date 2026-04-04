package com.example.pma_calculator;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class GraphActivity extends AppCompatActivity {

    private TextView tvGraphPointInfo;
    private GraphCanvasView graphCanvas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

        tvGraphPointInfo = findViewById(R.id.tvGraphPointInfo);
        graphCanvas = findViewById(R.id.graphCanvas);

        graphCanvas.setOnPointSelectedListener((x, y) -> {
            String text = String.format(
                    Locale.US,
                    "x = %.4f    y = %.4f",
                    x,
                    y
            ).replace('.', ',');

            tvGraphPointInfo.setText(text);
        });
    }

    public void onExitGraph(View v) {
        finish();
    }
}