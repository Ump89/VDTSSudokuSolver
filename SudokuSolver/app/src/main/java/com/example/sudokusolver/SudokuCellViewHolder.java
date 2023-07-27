package com.example.sudokusolver;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class SudokuCellViewHolder extends RecyclerView.ViewHolder {
    TextView tvSudokuNumber;

    public SudokuCellViewHolder(View itemView) {
        super(itemView);
        tvSudokuNumber = itemView.findViewById(R.id.tvSudukoCell);
    }
}
