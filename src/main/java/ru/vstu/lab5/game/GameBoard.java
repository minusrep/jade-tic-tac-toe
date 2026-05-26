package ru.vstu.lab5.game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Модель игрового поля 3x3 для игры "Крестики-нолики".
 */
public final class GameBoard {
    public static final char EMPTY = '.';
    public static final char X = 'X';
    public static final char O = 'O';

    private static final int[][] WIN_LINES = {
            {0, 1, 2}, {3, 4, 5}, {6, 7, 8},
            {0, 3, 6}, {1, 4, 7}, {2, 5, 8},
            {0, 4, 8}, {2, 4, 6}
    };

    private final char[] cells;

    public GameBoard() {
        this.cells = new char[9];
        Arrays.fill(this.cells, EMPTY);
    }

    private GameBoard(char[] cells) {
        if (cells.length != 9) {
            throw new IllegalArgumentException("Board must contain exactly 9 cells.");
        }
        this.cells = Arrays.copyOf(cells, cells.length);
    }

    public static GameBoard fromString(String boardText) {
        if (boardText == null || boardText.length() != 9) {
            throw new IllegalArgumentException("Board string must contain exactly 9 characters.");
        }

        char[] parsed = boardText.toCharArray();
        for (char cell : parsed) {
            if (cell != X && cell != O && cell != EMPTY) {
                throw new IllegalArgumentException("Unsupported board cell: " + cell);
            }
        }
        return new GameBoard(parsed);
    }

    public GameBoard copy() {
        return new GameBoard(cells);
    }

    public String serialize() {
        return new String(cells);
    }

    public boolean makeMove(int index, char mark) {
        validateIndex(index);
        validateMark(mark);
        if (cells[index] != EMPTY) {
            return false;
        }
        cells[index] = mark;
        return true;
    }

    public void forceSet(int index, char mark) {
        validateIndex(index);
        if (mark != X && mark != O && mark != EMPTY) {
            throw new IllegalArgumentException("Unsupported mark: " + mark);
        }
        cells[index] = mark;
    }

    public char getCell(int index) {
        validateIndex(index);
        return cells[index];
    }

    public boolean isEmpty(int index) {
        validateIndex(index);
        return cells[index] == EMPTY;
    }

    public boolean isFull() {
        for (char cell : cells) {
            if (cell == EMPTY) {
                return false;
            }
        }
        return true;
    }

    public char getWinner() {
        for (int[] line : WIN_LINES) {
            char a = cells[line[0]];
            char b = cells[line[1]];
            char c = cells[line[2]];
            if (a != EMPTY && a == b && b == c) {
                return a;
            }
        }
        return EMPTY;
    }

    public boolean isTerminal() {
        return getWinner() != EMPTY || isFull();
    }

    public List<Integer> getAvailableMoves() {
        List<Integer> moves = new ArrayList<Integer>();
        for (int i = 0; i < cells.length; i++) {
            if (cells[i] == EMPTY) {
                moves.add(i);
            }
        }
        return Collections.unmodifiableList(moves);
    }

    public String pretty() {
        StringBuilder builder = new StringBuilder();
        builder.append("\n");
        for (int row = 0; row < 3; row++) {
            if (row > 0) {
                builder.append("\n-----\n");
            }
            for (int col = 0; col < 3; col++) {
                if (col > 0) {
                    builder.append('|');
                }
                char cell = cells[row * 3 + col];
                builder.append(cell == EMPTY ? ' ' : cell);
            }
        }
        return builder.toString();
    }

    private static void validateIndex(int index) {
        if (index < 0 || index >= 9) {
            throw new IllegalArgumentException("Cell index must be in range 0..8.");
        }
    }

    private static void validateMark(char mark) {
        if (mark != X && mark != O) {
            throw new IllegalArgumentException("Move mark must be X or O.");
        }
    }
}
