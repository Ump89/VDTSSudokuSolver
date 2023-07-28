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
import java.util.Stack;

public class MainActivity extends AppCompatActivity {
    private SudokuAdapter sudokuAdapter;
    private Handler handler;
    private Long startTime;
    private boolean gameOver;
    private boolean solvingInProgress = true;

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
        // Initialize game settings
        initDB();
        gameOver = true;
        initBoard(gameOver);

        handler = new Handler();

        // connect view objects
        tvTimer = findViewById(R.id.tvTimer);
        btnStart = findViewById(R.id.btnStart);
        btnSolve = findViewById(R.id.btnSolve);
        btnSave = findViewById(R.id.btnSave);
        // listen for click event on btn
        btnStart.setOnClickListener(view -> {
            if(gameOver) {
                startTimer();
                rvSudokuBoard.setVisibility(view.VISIBLE);
            }
            else {
                stopTimer();
                initBoard(gameOver);
            }
        });
        // listen for click event on btn
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
        // listen for click event on btn
        btnSave.setOnClickListener(view -> {
            if(sudokuAdapter.getSudokuData() != null)
                showDatabaseAlert(sudokuAdapter.getSudokuData());
            else {
                Toast.makeText(this, "No Data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Example Call:
     * initDB();
     *
     * Parameters:
     * None
     *
     * Returns:
     * @return void
     *
     * Dependencies:
     * @see SudokuDatabaseMngr: A custom class to manage SQLite database connections for the Sudoku app.
     * @see SudokuContract: A class containing constants for the database schema, including table name and column names.
     *
     * Description:
     * This function initializes the database connections and inserts initial data (a 9x9 Sudoku board) into the database.
     */
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
    /**
     * Example Call:
     * initDB(sudokuBoard);
     *
     * Parameters:
     * @param sudokuBoard (SudokuCell[][]): A 9x9 two-dimensional array representing the Sudoku board. Each element of the array is a SudokuCell object containing the value of the cell.
     *
     * Returns:
     * @return void
     *
     * Dependencies:
     * @see SudokuCell: The class representing a cell in the Sudoku board, containing cell value and related information.
     * @see SudokuDatabaseMngr: A custom class to manage SQLite database connections for the Sudoku app.
     * @see SudokuContract: A class containing constants for the database schema, including table name and column names.
     *
     * Description:
     * Same as above, except calls the database to get initial board data rather than an array
     */
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
    /**
     * Example Call:
     * --------------
     * initBoard(true);
     *
     * Parameters:
     * --------------
     * @param gameOver (boolean): A boolean flag indicating whether the game is over or not. If true, the Sudoku board will be read-only, and users cannot modify the cells. If false, users can interact with the Sudoku board.
     *
     * Returns:
     * --------------
     * @return void
     *
     * Dependencies:
     * --------------
     * @see SudokuCell: The class representing a cell in the Sudoku board, containing cell value and related information.
     * @see SudokuAdapter: The custom RecyclerView adapter responsible for rendering SudokuCell objects within the RecyclerView.
     *
     */
    private void initBoard(boolean gameOver) {
        // Initialize RecyclerView and adapter
        SudokuCell[][] sudokuBoard = convertIntToSudokuCells(callFromDatabase());
        rvSudokuBoard = findViewById(R.id.rvSudokuBoard);
        sudokuAdapter = new SudokuAdapter(this, sudokuBoard, gameOver);
        rvSudokuBoard.setLayoutManager(new GridLayoutManager(this, 9));
        rvSudokuBoard.setAdapter(sudokuAdapter);
    }
    /**
     * Example Call:
     * startTimer();
     *
     * Parameters:
     * None
     *
     * Returns:
     * @return void
     *
     * Description:
     * Starts the timer for the Sudoku game, initializes the game state, and sets up the game board.
     */
    private void startTimer() {
        startTime = System.currentTimeMillis();
        gameOver = false;
        btnStart.setText("STOP");
        updateTimer();
        tvTimer.setText(calculateTime(startTime, startTime));
        initBoard(gameOver);
    }
    /**
     * Example Call:
     * updateTimer();
     *
     * Parameters:
     * None
     *
     * Returns:
     * @return void
     *
     * Description:
     * Updates the timer displayed on the UI for the Sudoku game. The function uses a handler to schedule periodic updates of the timer while the game is active (not over).
     */
    private void updateTimer() {
        handler.post(() -> {
            if(!gameOver) {
                long currentTime = System.currentTimeMillis();
                tvTimer.setText(calculateTime(currentTime, startTime));
                updateTimer();
            }
        });
    }
    /**
     * Example Call:
     * stopTimer();
     *
     * Parameters:
     * None
     *
     * Returns:
     * @return void
     *
     * Description:
     * Stops the timer for the Sudoku game and updates the game state to indicate that the game is over. The function also updates the UI to display the final time and changes the text of the start button to "Restart".
     */
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
    /**
     * Example Call:
     * boolean result = solveNextStep();
     *
     * Parameters:
     * None
     *
     * Returns:
     * @return boolean - True if Sudoku is successfully solved, false if no solution exists.
     *
     * Description:
     * Attempts to solve the next step in the Sudoku puzzle using a backtracking algorithm.
     * It chooses a spot, tries placing a number, and then backtracks if the number doesn't lead to a solution.
     * The Sudoku puzzle is updated in the process using the `sudokuAdapter`.
     */
    private boolean solveNextStep() {
        Stack<SudokuCell> stack = new Stack<>();
        stack.push(chooseSpot());

        while (!stack.isEmpty()) {
            SudokuCell cellToFill = stack.pop();

            if (cellToFill == null) {
                solvingInProgress = false;
                return true;
            }

            int row = cellToFill.getRow();
            int col = cellToFill.getCol();

            int num = cellToFill.getValue();
            boolean backtrack = true;

            while (num <= 9) {
                if (isValidPlacement(row, col, num)) {
                    cellToFill.setValue(num);
                    sudokuAdapter.notifyItemChanged(row * 9 + col);

                    if (!solveNextStep()) {
                        solvingInProgress = false;
                        return true;
                    }

                    stack.push(chooseSpot());
                    backtrack = false;
                    break;
                }
                num++;
            }
        }
        solvingInProgress = true;
        return false;
    }

//            // Solve the cell
//            for (int num = 1; num <=9; num++) {
//                if(isValidPlacement(row, col, num)) {
//                    cellToFill.setValue(num);
//                    sudokuAdapter.notifyItemChanged(row * 9 + col);
//
//                    if(solveNextStep()) { return true; }
//
//                    cellToFill.setValue(0);
//                    sudokuAdapter.notifyItemChanged(row * 9 + col);
//                }
//            }
//        }
//        // No valid number found for the cell, backtrack to previous cells
//        solvingInProgress = true;
//        return false;
//    }
    /**
     * Example Call:
     * SudokuCell cell = chooseSpot();
     *
     * Parameters:
     * None
     *
     * Returns:
     * @return SudokuCell - A randomly chosen empty cell (cell with a value of 0) from the Sudoku board. If there are no empty cells, the function returns null.
     *
     * Description:
     * Randomly selects an empty cell (cell with a value of 0) from the Sudoku board to be filled with a number during the Sudoku solving process.
     * The function iterates through the entire Sudoku board and compiles a list of empty cells.
     * It then selects a random cell from the list and returns it.
     * If there are no empty cells on the board, the function returns null, indicating that the Sudoku puzzle is already solved or there's no solution for the current configuration.
     */
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
    /**
     * Example Call:
     * boolean isValid = isValidPlacement(row, col, num);
     *
     * Parameters:
     * @param row (int): The row index of the cell to be checked.
     * @param col (int): The column index of the cell to be checked.
     * @param num (int): The number to be placed in the cell and checked for validity.
     *
     * Returns:
     * @return boolean - True if placing 'num' in the cell at position (row, col) is valid, false otherwise.
     *
     * Description:
     * Checks if placing a given 'num' in the cell at position (row, col) of the Sudoku board is a valid move according to Sudoku rules.
     * The function checks the row, column, and the corresponding 3x3 sub-grid for any conflicts with 'num'.
     * If there are no conflicts, the placement is considered valid, and the function returns true. Otherwise, it returns false, indicating an invalid placement.
     */
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
    /**
     * Example Call:
     * String formattedTime = calculateTime(currentTime, pastTime);
     *
     * Parameters:
     * @param currentTime (long): The current time in milliseconds (usually obtained using System.currentTimeMillis()).
     * @param pastTime (long): The past time in milliseconds from which the time difference is calculated.
     *
     * Returns:
     * @return String - A formatted string representing the time difference between currentTime and pastTime in the format "HH:mm:ss".
     *
     * Description:
     * Calculates the time difference between two given timestamps (currentTime and pastTime)
     */
    private String calculateTime(long currentTime, long pastTime) {
        long timeTaken = currentTime - pastTime;
        long seconds = (timeTaken / 1000) % 60;
        long minutes = (timeTaken / (1000 * 60) % 60);
        long hours = (timeTaken / (1000 * 60 * 60) % 24);
        return(String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds));
    }
    /**
     * Example Call:
     * int[][] sudokuData = callFromDatabase();
     *
     * Parameters:
     * None
     *
     * Returns:
     * @return int[][] - A 9x9 two-dimensional array representing the Sudoku board data retrieved from the database.
     *
     * Dependencies:
     * @see SudokuDatabaseMngr: A custom class to manage SQLite database connections for the Sudoku app.
     * @see SudokuContract: A class containing constants for the database schema, including table name and column names.
     *
     * Description:
     * Retrieves the Sudoku board data from the database and converts it into a 9x9 two-dimensional array.
     */
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
    /**
     * Example Call:
     * showDatabaseAlert(sudokuBoard);
     *
     * Parameters:
     * @param sudokuBoard (SudokuCell[][]): A 9x9 two-dimensional array representing the Sudoku board.
     *
     * Returns:
     * @return void
     *
     * Description:
     * Displays an alert dialog with options for interacting with the Sudoku database.
     *   1. Save Database: Initializes the database and inserts the data from `sudokuBoard`.
     *   2. Load Board: Initializes the RecyclerView and adapter for the Sudoku board using `initBoard` function.
     *   3. Cancel: Closes the dialog without performing any action.
     *
     * Dependencies:
     * @see AlertDialog: A class provided by Android SDK to create alert dialogs.
     * @see SudokuCell: The class representing a cell in the Sudoku board, containing cell value and related information.
     *
     */
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
    /**
     * Example Call:
     * SudokuCell[][] sudokuCells = convertIntToSudokuCells(sudokuBoard);
     *
     * Parameters:
     * @param sudokuBoard (int[][]): A 9x9 two-dimensional array representing the Sudoku board with integer values.
     *
     * Returns:
     * @return SudokuCell[][] - A 9x9 two-dimensional array of SudokuCell objects.
     *
     * Description:
     * Converts a 9x9 two-dimensional array of integers (sudokuBoard) representing the Sudoku board into a 9x9 two-dimensional array of SudokuCell objects (sudokuCells).
     * The function iterates through each cell of intArray, creates a new SudokuCell object with the row, column, and value information, and assigns it to the corresponding cell in sudokuCells.
     * The resulting array contains SudokuCell objects representing each cell in the Sudoku board with their associated data.
     *
     * Dependencies:
     * @see SudokuCell: The class representing a cell in the Sudoku board, containing cell value and related information.
     *
     */
    private SudokuCell[][] convertIntToSudokuCells(int[][] sudokuBoard)
    {
        SudokuCell[][] sudokuCells = new SudokuCell[9][9];
        for (int row = 0; row < 9; row ++) {
            for (int col = 0; col < 9; col++) {
                int value = sudokuBoard[row][col];
                sudokuCells[row][col] = new SudokuCell(row, col, value);
            }
        }
        return sudokuCells;
    }
}

/**
 * Class: SudokuContract
 *
 * Description:
 * A contract class that defines the database schema for the Sudoku app.
 */
class SudokuContract {
    public static class SudokuEntry implements BaseColumns {
        public static final String TABLE_NAME = "sudokuboard";
        public static final String COLUMN_ROW = "rows";
        public static final String COLUMN_COL = "columns";
        public static final String COLUMN_VALUE = "value";
    }
}

/**
 * Class: SudokuDatabaseMngr
 * Extends: SQLiteOpenHelper
 *
 * Description:
 * A custom class that manages the SQLite database for the Sudoku app.
 * It extends the SQLiteOpenHelper class and handles database creation and version management.
 */
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
