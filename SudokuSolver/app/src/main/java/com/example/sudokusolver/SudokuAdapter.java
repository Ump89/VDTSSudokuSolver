package com.example.sudokusolver;

import android.app.AlertDialog;
import android.content.Context;
import android.text.InputType;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class SudokuAdapter extends RecyclerView.Adapter<SudokuCellViewHolder> {
    private final Context context;
    private boolean gameOver;
    private SudokuCell[][] sudokuData;

    public SudokuAdapter(Context context, SudokuCell[][] sudokuData, boolean gameOver) {
        this.context = context;
        this.sudokuData = sudokuData;
        this.gameOver = gameOver;
    }

    @NonNull
    @Override
    public SudokuCellViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.sudoku_cell, parent, false);
        return new SudokuCellViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull SudokuCellViewHolder holder, int position) {
        int row = position / 9;
        int col = position % 9;
        int value = sudokuData[row][col].getValue();

        if (value != 0) {
            holder.tvSudokuNumber.setText(String.valueOf(value));
        }
        else {
            holder.tvSudokuNumber.setText("x");
        }
        if (!gameOver) {
            holder.tvSudokuNumber.setOnClickListener(view -> {
                if (value == 0) {
                    List<Integer> validNumbers = getValidNumbers(row, col);
                    if (!validNumbers.isEmpty()) {
                        showNumberInputDialog(row, col);
                    } else {
                        // No valid numbers for this cell, show an error or toast message
                        Toast.makeText(view.getContext(), "No valid numbers for this cell", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return sudokuData.length * sudokuData[0].length;
    }

    public void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
    }
    public void updateSudokuData(SudokuCell[][] newData) {
        this.sudokuData = newData;
        notifyDataSetChanged();
    }

    public SudokuCell[][] getSudokuData() {
        return sudokuData;
    }
    private List<Integer> getValidNumbers(int row, int col) {
        List<Integer> validNumbers = new ArrayList<>();
        for (int num = 1; num <= 9; num++){
            if (isValidPlacement(row, col, num)) {
                validNumbers.add(num);
            }
        }
        return validNumbers;
    }
    private boolean isValidPlacement(int row, int col, int num) {
        for (int i = 0; i < 9; i++) {
            if (sudokuData[row][i].getValue() == num || sudokuData[i][col].getValue() == num) {
                return false;
            }
        }

        int subGridRow = (row / 3) * 3;
        int subGridCol = (col / 3) * 3;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if(sudokuData[subGridRow + i][subGridCol + j].getValue() == num) {
                    return false;
                }
            }
        }

        return true;
    }
    /**
     * Example Call:
     * showNumberInputDialog(row, col);
     *
     * Parameters:
     * @param row (int): The row index of the cell for which the number input dialog is displayed.
     * @param col (int): The column index of the cell for which the number input dialog is displayed.
     *
     * Returns:
     * @return void
     *
     * Description:
     * Displays an alert dialog with an input field for the user.
     *
     */
    private void showNumberInputDialog(int row, int col) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Enter a number");

        final EditText input = new EditText(context);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setGravity(Gravity.CENTER);
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String userInput = input.getText().toString().trim();
            if(!userInput.isEmpty())
            {
                int value = Integer.parseInt(userInput);
                if (value >= 1 && value <= 9) {
                    sudokuData[row][col].setValue(value);
                    notifyDataSetChanged(); // Reloads the RecycleView
                }
                else {
                    Toast.makeText(context, "Invalid number! Please enter a number from 1-9.", Toast.LENGTH_SHORT).show();
                }
            }
            else {
                Toast.makeText(context, "Please enter a number.", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }
}
