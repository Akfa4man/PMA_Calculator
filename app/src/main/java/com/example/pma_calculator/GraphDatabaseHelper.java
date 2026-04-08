package com.example.pma_calculator;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class GraphDatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "graph_points.db";
    private static final int DB_VERSION = 1;

    public static final String TABLE_POINTS = "graph_points";

    public static final String COL_ID = "_id";
    public static final String COL_POINT_ORDER = "point_order";
    public static final String COL_X = "x_value";
    public static final String COL_Y = "y_value";

    public GraphDatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE " + TABLE_POINTS + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_POINT_ORDER + " INTEGER NOT NULL, "
                + COL_X + " REAL NOT NULL, "
                + COL_Y + " REAL NOT NULL"
                + ");";

        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_POINTS);
        onCreate(db);
    }

    public void replacePoints(List<GraphPoint> points) {
        SQLiteDatabase db = getWritableDatabase();

        db.beginTransaction();
        try {
            db.delete(TABLE_POINTS, null, null);

            ContentValues values = new ContentValues();

            for (GraphPoint point : points) {
                values.clear();
                values.put(COL_POINT_ORDER, point.pointOrder);
                values.put(COL_X, point.x);
                values.put(COL_Y, point.y);
                db.insert(TABLE_POINTS, null, values);
            }

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public List<GraphPoint> readAllPointsOrdered() {
        List<GraphPoint> result = new ArrayList<>();

        SQLiteDatabase db = getReadableDatabase();

        try (Cursor cursor = db.query(
                TABLE_POINTS,
                new String[]{COL_POINT_ORDER, COL_X, COL_Y},
                null,
                null,
                null,
                null,
                COL_POINT_ORDER + " ASC"
        )) {
            int orderIndex = cursor.getColumnIndexOrThrow(COL_POINT_ORDER);
            int xIndex = cursor.getColumnIndexOrThrow(COL_X);
            int yIndex = cursor.getColumnIndexOrThrow(COL_Y);

            while (cursor.moveToNext()) {
                int pointOrder = cursor.getInt(orderIndex);
                double x = cursor.getDouble(xIndex);
                double y = cursor.getDouble(yIndex);

                result.add(new GraphPoint(pointOrder, x, y));
            }
        }

        return result;
    }

    public List<GraphPoint> findExtremaPoints() {
        List<GraphPoint> orderedPoints = readAllPointsOrdered();
        List<GraphPoint> extrema = new ArrayList<>();

        for (int i = 1; i < orderedPoints.size() - 1; i++) {
            GraphPoint prev = orderedPoints.get(i - 1);
            GraphPoint curr = orderedPoints.get(i);
            GraphPoint next = orderedPoints.get(i + 1);

            boolean isMaximum = curr.y > prev.y && curr.y > next.y;
            boolean isMinimum = curr.y < prev.y && curr.y < next.y;

            if (isMaximum || isMinimum) {
                extrema.add(curr);
            }
        }

        return extrema;
    }
}