package ru.vstu.lab5.game;

import java.util.List;

/**
 * Интеллектуальный модуль агента.
 * Выбирает ход с помощью алгоритма minimax, то есть просматривает дерево
 * возможных состояний игры и выбирает оптимальный ход.
 */
public final class MoveSelector {
    private MoveSelector() {
    }

    public static int chooseBestMove(GameBoard board, char myMark, char opponentMark) {
        if (board.isTerminal()) {
            return -1;
        }

        int bestMove = -1;
        int bestScore = Integer.MIN_VALUE;
        List<Integer> availableMoves = board.getAvailableMoves();

        for (Integer move : availableMoves) {
            GameBoard candidate = board.copy();
            candidate.makeMove(move, myMark);

            int score = minimax(candidate, myMark, opponentMark, false, 0);
            if (score > bestScore) {
                bestScore = score;
                bestMove = move;
            }
        }

        return bestMove;
    }

    private static int minimax(GameBoard board,
                               char myMark,
                               char opponentMark,
                               boolean maximizing,
                               int depth) {
        char winner = board.getWinner();
        if (winner == myMark) {
            return 10 - depth;
        }
        if (winner == opponentMark) {
            return depth - 10;
        }
        if (board.isFull()) {
            return 0;
        }

        if (maximizing) {
            int bestScore = Integer.MIN_VALUE;
            for (Integer move : board.getAvailableMoves()) {
                GameBoard candidate = board.copy();
                candidate.makeMove(move, myMark);
                bestScore = Math.max(bestScore, minimax(candidate, myMark, opponentMark, false, depth + 1));
            }
            return bestScore;
        }

        int bestScore = Integer.MAX_VALUE;
        for (Integer move : board.getAvailableMoves()) {
            GameBoard candidate = board.copy();
            candidate.makeMove(move, opponentMark);
            bestScore = Math.min(bestScore, minimax(candidate, myMark, opponentMark, true, depth + 1));
        }
        return bestScore;
    }
}
