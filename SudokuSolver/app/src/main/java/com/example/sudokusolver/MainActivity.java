package com.example.sudokusolver;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.os.Handler;
import android.provider.BaseColumns;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    private SudokuAdapter sudokuAdapter;
    private Handler handler;
    private Long startTime;
    private boolean gameOver;
    private boolean solvingInProgress;

    // View Objects
    private TextView tvTimer;
    private Button btnStart;
    private Button btnSolve;
    private Button btnSave;

    private RecyclerView rvSudokuBoard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        initDB(convertIntToSudokuCells(callFromDatabase()));
        initDB();
        gameOver = true;
        initBoard(gameOver);
        handler = new Handler();
        tvTimer = findViewById(R.id.tvTimer);
        btnStart = findViewById(R.id.btnStart);
        btnSolve = findViewById(R.id.btnSolve);
        btnSave = findViewById(R.id.btnSave);
        btnStart.setOnClickListener(view -> {
            if(gameOver) {
                startTimer();
                rvSudokuBoard.setVisibility(view.VISIBLE);
            }
            else { stopTimer(); }
        });
        btnSolve.setOnClickListener(view -> {
            if (!gameOver) {
                solvingInProgress = true;
                solveNextStep();
            }
            else if (!solvingInProgress){
                gameOver = true;
                stopTimer();
                rvSudokuBoard.setVisibility(view.INVISIBLE);
            }
        });
        btnSave.setOnClickListener(view -> {
            if(sudokuAdapter.getSudokuData() != null)
                showDatabaseAlert(sudokuAdapter.getSudokuData());
            else {
                Toast.makeText(this, "No Data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initDB() {
        // Initialize Database Connections
        SudokuDatabaseMngr dbManager = new SudokuDatabaseMngr(this.getApplicationContext());
        SQLiteDatabase db = dbManager.getWritableDatabase();

        // Data to insert into tables
        int[][] sudokuBoard = {
                {0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0}
        };

        // Send data to database
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                int value = sudokuBoard[row][col];
                if(value > -1) {
                    ContentValues cv = new ContentValues();
                    cv.put(SudokuContract.SudokuEntry.COLUMN_ROW, row);
                    cv.put(SudokuContract.SudokuEntry.COLUMN_COL, col);
                    cv.put(SudokuContract.SudokuEntry.COLUMN_VALUE, value);
                    db.insert(SudokuContract.SudokuEntry.TABLE_NAME, null, cv);
                }
            }
        }
        db.close();
        dbManager.close();
    }

    private void initDB(SudokuCell[][] sudokuBoard) {
        // Initialize Database Connections
        SudokuDatabaseMngr dbManager = new SudokuDatabaseMngr(this.getApplicationContext());
        SQLiteDatabase db = dbManager.getWritableDatabase();

        // Send data to database
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                int value = sudokuBoard[row][col].getValue();
                if(value > -1) {
                    ContentValues cv = new ContentValues();
                    cv.put(SudokuContract.SudokuEntry.COLUMN_ROW, row);
                    cv.put(SudokuContract.SudokuEntry.COLUMN_COL, col);
                    cv.put(SudokuContract.SudokuEntry.COLUMN_VALUE, value);
                    db.insert(SudokuContract.SudokuEntry.TABLE_NAME, null, cv);
                }
            }
        }
        db.close();
        dbManager.close();
    }

    private void initBoard(boolean gameOver) {
        // Initialize RecyclerView and adapter
        SudokuCell[][] sudokuBoard = convertIntToSudokuCells(callFromDatabase());
        rvSudokuBoard = findViewById(R.id.rvSudokuBoard);
        sudokuAdapter = new SudokuAdapter(this, sudokuBoard, gameOver);
        rvSudokuBoard.setLayoutManager(new GridLayoutManager(this, 9));
        rvSudokuBoard.setAdapter(sudokuAdapter);
    }

    private void startTimer() {
        startTime = System.currentTimeMillis();
        gameOver = false;
        btnStart.setText("STOP");
        updateTimer();
        tvTimer.setText(calculateTime(startTime, startTime));
        initBoard(gameOver);
    }

    private void updateTimer() {
        handler.post(() -> {
            if(!gameOver) {
                long currentTime = System.currentTimeMillis();
                tvTimer.setText(calculateTime(currentTime, startTime));
                updateTimer();
            }
        });
    }

    private void stopTimer() {
        if (!gameOver) {
            long endTime = System.currentTimeMillis();
            tvTimer.setText(calculateTime(endTime, startTime));
            gameOver = true;
            btnStart.setText("Restart");
            sudokuAdapter.setGameOver(gameOver);
        }
    }

    // https://www.youtube.com/watch?v=MlyTq-xVkQE
    private boolean solveNextStep() {
        SudokuCell cellToFill = chooseSpot();
        if (cellToFill == null) {
            // Sudoku is already solved or no solution exists
            solvingInProgress = false; // Reset the flag
            return true;
        }

        int row = cellToFill.getRow();
        int col = cellToFill.getCol();

        // Solve the cell
        for (int num = 1; num <=9; num++) {
            if(isValidPlacement(row, col, num)) {
                cellToFill.setValue(num);
                sudokuAdapter.notifyItemChanged(row * 9 + col);

                if(solveNextStep()) { return true; }

                cellToFill.setValue(0);
                sudokuAdapter.notifyItemChanged(row * 9 + col);
            }
        }

        // No valid number found for the cell, backtrack to previous cells
        return false;

    }
    private SudokuCell chooseSpot() {
        Random random = new Random();
        List<SudokuCell> emptyCells = new ArrayList<>();

        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                if (sudokuAdapter.getSudokuData()[row][col].getValue() == 0) {
                    emptyCells.add(sudokuAdapter.getSudokuData()[row][col]);
                }
            }
        }

        if(!emptyCells.isEmpty()) {
            int randomIndex = random.nextInt(emptyCells.size());

            return emptyCells.get(randomIndex);
        }

        return null;
    }

    private boolean isValidPlacement(int row, int col, int num) {
        for (int i = 0; i < 9; i++) {
            if (sudokuAdapter.getSudokuData()[row][i].getValue() == num || sudokuAdapter.getSudokuData()[i][col].getValue() == num) {
                return false;
            }
        }

        int subGridRow = (row / 3) * 3;
        int subGridCol = (col / 3) * 3;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if(sudokuAdapter.getSudokuData()[subGridRow + i][subGridCol + j].getValue() == num) {
                    return false;
                }
            }
        }

        return true;
    }

    private String calculateTime(long currentTime, long pastTime) {
        long timeTaken = currentTime - pastTime;
        long seconds = (timeTaken / 1000) % 60;
        long minutes = (timeTaken / (1000 * 60) % 60);
        long hours = (timeTaken / (1000 * 60 * 60) % 24);
        return(String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds));
    }

    private int[][] callFromDatabase() {
        SudokuDatabaseMngr dbManager = new SudokuDatabaseMngr(this);
        SQLiteDatabase db = dbManager.getReadableDatabase();
        Cursor cursor = db.query(SudokuContract.SudokuEntry.TABLE_NAME, null, null, null, null, null, null);
        int[][] sudokuData = new int[9][9];

        int rowColIndex = cursor.getColumnIndex(SudokuContract.SudokuEntry.COLUMN_ROW);
        int colColIndex = cursor.getColumnIndex(SudokuContract.SudokuEntry.COLUMN_COL);
        int valueColIndex = cursor.getColumnIndex(SudokuContract.SudokuEntry.COLUMN_VALUE);

        if (rowColIndex >= 0 && colColIndex >= 0 && valueColIndex >=0) {
            while (cursor.moveToNext()) {
                int row = cursor.getInt(rowColIndex);
                int col = cursor.getInt(colColIndex);
                int value = cursor.getInt(valueColIndex);
                if (row >= 0 && row < 9 && col >= 0 && col < 9) {
                    sudokuData[row][col] = value;
                } else {
                    Log.i("Database data retrieval error", "Invalid row or column index: " + row + ", " + col);
                }
            }
        }

        cursor.close();
        db.close();
        dbManager.close();

        return(sudokuData);
    }

    private void showDatabaseAlert(SudokuCell[][] sudokuBoard) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select an Option")
                .setItems(R.array.alert_dialog_options, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            initDB(sudokuBoard);
                            break;
                        case 1:
                            initBoard(gameOver);
                            break;
                        case 2:
                            // do nothing
                            break;
                    }
                })
                .show();
    }

    private SudokuCell[][] convertIntToSudokuCells(int[][] intArray)
    {
        SudokuCell[][] sudokuCells = new SudokuCell[9][9];
        for (int row = 0; row < 9; row ++) {
            for (int col = 0; col < 9; col++) {
                int value = intArray[row][col];
                sudokuCells[row][col] = new SudokuCell(row, col, value);
            }
        }
        return sudokuCells;
    }
}

/**
 * Outline Tables and Data Rows
 */
class SudokuContract {
    public static class SudokuEntry implements BaseColumns {
        public static final String TABLE_NAME = "sudokuboard";
        public static final String COLUMN_ROW = "rows";
        public static final String COLUMN_COL = "columns";
        public static final String COLUMN_VALUE = "value";
    }
}

// Visual Aid
//int[][] sudokuBoard = {
//        {0, 0, 0, 0, 0, 0, 0, 0, 0},
//        {0, 0, 0, 0, 0, 0, 0, 0, 0},
//        {0, 0, 0, 0, 0, 0, 0, 0, 0},
//        {0, 0, 0, 0, 0, 0, 0, 0, 0},
//        {0, 0, 0, 0, 0, 0, 0, 0, 0},
//        {0, 0, 0, 0, 0, 0, 0, 0, 0},
//        {0, 0, 0, 0, 0, 0, 0, 0, 0},
//        {0, 0, 0, 0, 0, 0, 0, 0, 0},
//        {0, 0, 0, 0, 0, 0, 0, 0, 0}
//};
//int[][] sudokuBoard = {
//        {5, 3, 0, 0, 7, 0, 0, 0, 0},
//        {6, 0, 0, 1, 9, 5, 0, 0, 0},
//        {0, 9, 8, 0, 0, 0, 0, 6, 0},
//        {8, 0, 0, 0, 6, 0, 0, 0, 3},
//        {4, 0, 0, 8, 0, 3, 0, 0, 1},
//        {7, 0, 0, 0, 2, 0, 0, 0, 6},
//        {0, 6, 0, 0, 0, 0, 2, 8, 0},
//        {0, 0, 0, 4, 1, 9, 0, 0, 5},
//        {0, 0, 0, 0, 8, 0, 0, 7, 9}
//};

// Create and manage the database using a Database Manager class
class SudokuDatabaseMngr extends SQLiteOpenHelper {
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

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//        db.execSQL("DROP TABLE IF EXISTS " + SudokuContract.SudokuEntry.TABLE_NAME);
//        onCreate(db);
    }
}
