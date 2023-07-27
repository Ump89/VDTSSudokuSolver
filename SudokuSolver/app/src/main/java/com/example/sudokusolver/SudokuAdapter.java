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
            holder.tvSudokuNumber.setText("X");
        }
        if(!gameOver){
            holder.tvSudokuNumber.setOnClickListener(view -> {
                if(value == 0) {
                    showNumberInputDialog(row, col);
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
