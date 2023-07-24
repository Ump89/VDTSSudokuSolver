package com.example.sudokusolver;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.os.Handler;
import android.provider.BaseColumns;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private Handler handler;
    private Long startTime;
    private boolean gameOver;
    private TextView tvTimer;
    private Button btnStart;
    private Button btnSolve;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvTimer = findViewById(R.id.tvTimer);
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startTimer();
            }
        });
        initDB();
    }

    private void initDB() {
        System.out.println("Initializing Database...");
    }

    private void startTimer() {
        startTime = System.currentTimeMillis();
        gameOver = false;
        updateTimer();
    }

    private void updateTimer() {

    }
}

// Outline Tables and Data Rows
class SudokuContract {
    public static class SudokuEntry implements BaseColumns {
        public static final String TABLE_NAME = "sudokuboard";
        public static final String COLUMN_ROW = "rows";
        public static final String COLUMN_COL = "columns";
        public static final String COLUMN_VALUE = "value";
    }
}

// Visual Aid
//board[][] = {
//        [0][0], [0][1], [0][2], [0][3], [0][4], [0][5], [0][6], [0][7], [0][8],

//        [1][0], [1][1], [1][2], [1][3], [1][4], [1][5], [1][6], [1][7], [1][8],

//        [2][0], [2][1], [2][2], [2][3], [2][4], [2][5], [2][6], [2][7], [2][8],

//        [3][0], [3][1], [3][2], [3][3], [3][4], [3][5], [3][6], [3][7], [3][8],

//        [4][0], [4][1], [4][2], [4][3], [4][4], [4][5], [4][6], [4][7], [4][8],

//        [5][0], [5][1], [5][2], [5][3], [5][4], [5][5], [5][6], [5][7], [5][8],

//        [6][0], [6][1], [6][2], [6][3], [6][4], [6][5], [6][6], [6][7], [6][8],

//        [7][0], [7][1], [7][2], [7][3], [7][4], [7][5], [7][6], [7][7], [7][8],

//        [8][0], [8][1], [8][2], [8][3], [8][4], [8][5], [8][6], [8][7], [8][8],
//}

// Create and manage the database using a DatabaseHelper class
abstract class SudokuDatabaseMngr extends SQLiteOpenHelper {
    public SudokuDatabaseMngr(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    private static final String DATABASE_NAME = "SudokuSolverVDTS.db";
    private static final int DATABASE_VERSION = 1;

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + SudokuContract.SudokuEntry.TABLE_NAME + " (" +
                    SudokuContract.SudokuEntry.COLUMN_ROW + " INTEGER," +
                    SudokuContract.SudokuEntry.COLUMN_COL + " INTEGER," +
                    SudokuContract.SudokuEntry.COLUMN_VALUE + " INTEGER)";
    // CREATE TABLE sudokuboard (row integer, col integer, value integer



    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + SudokuContract.SudokuEntry.TABLE_NAME);
        onCreate(db);
    }
}
