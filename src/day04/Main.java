package day04;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.IntStream;

public class Main {
    public static void main() throws Exception {
        System.out.println("Part 1: " + part1());
        System.out.println("Part 2: " + part2());
    }

    static int part1() throws Exception {
        final var input = parseInput(Path.of("./src/day04/input.txt"));

        return IntStream.range(0, input.length)
            .flatMap(row ->
                IntStream.range(0, input[row].length)
                    .map(col ->
                        countXmas(input, row, col)
                    )
            )
            .sum();
    }

    private static int part2() throws Exception {
        final var input = parseInput(Path.of("./src/day04/input.txt"));

        return IntStream.range(0, input.length)
            .flatMap(row ->
                IntStream.range(0, input[row].length)
                    .map(col ->
                        countXDashMas(input, row, col)
                    )
            )
            .sum();
    }

    static int countXmas(final char[][] board, final int row, final int col) {
        return IntStream.rangeClosed(-1, 1)
            .flatMap(rowInc ->
                IntStream.rangeClosed(-1, 1)
                    .map(colInc ->
                        isMatch(board, row, rowInc, col, colInc, "XMAS") ? 1 : 0
                    )
            )
            .sum();
    }

    private static int countXDashMas(final char[][] board, final int row, final int col) {
        final var downRight = isMatch(board, row, 1, col, 1, "MAS") ||
            isMatch(board, row, 1, col, 1, "SAM");
        final var downLeft = isMatch(board, row, 1, col + 2, -1, "MAS") ||
            isMatch(board, row, 1, col + 2, -1, "SAM");
        return downRight && downLeft ? 1 : 0;
    }

    static boolean isMatch(
        final char[][] board,
        final int row,
        final int rowIncrement,
        final int col,
        final int colIncrement,
        final String word
    ) {
        if ("".equals(word)) {
            return true;
        }

        if (row < 0 || row >= board.length || col < 0 || col >= board[row].length) {
            return false;
        }

        if (board[row][col] != word.charAt(0)) {
            return false;
        }

        return isMatch(
            board,
            row + rowIncrement,
            rowIncrement,
            col + colIncrement,
            colIncrement,
            word.substring(1)
        );
    }

    static char[][] parseInput(final Path input) throws Exception {
        return Files.readAllLines(input)
            .stream()
            .map(String::toCharArray)
            .toArray(char[][]::new);
    }
}
