package com.example.sudokusolver;

public class SudokuCell {
    private int row;
    private int col;
    private int value;
    private boolean editable;

    public SudokuCell (int row, int col, int value) {
        setRow(row);
        setCol(col);
        setValue(value);
        if (value != 0) { setEditable(false); }
        else { setEditable(true); }
    }


    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getCol() {
        return col;
    }

    public void setCol(int col) {
        this.col = col;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }
}
