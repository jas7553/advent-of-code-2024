package day02;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Main {
    public static void main() throws Exception {
        System.out.println("Part 1: " + part1());
        System.out.println("Part 2: " + part2());
    }

    static long part1() throws Exception {
        final var lines = Files.readAllLines(Path.of("./src/day02/input.txt"));

        return parseInput(lines)
            .stream()
            .filter(Main::isSafe)
            .count();
    }

    static long part2() throws Exception {
        final var lines = Files.readAllLines(Path.of("./src/day02/input.txt"));

        return parseInput(lines)
            .stream()
            .filter(line -> isSafe(line) || isSafeWithProblemDampener(line))
            .count();
    }

    static List<List<Integer>> parseInput(final List<String> lines) {
        return lines.stream()
            .map(line ->
                Arrays.stream(line.split("\\s+"))
                    .map(Integer::parseInt)
                    .toList()
            )
            .toList();
    }

    static boolean isSafe(final List<Integer> line) {
        final var differences = IntStream.range(0, line.size() - 1)
            .map(i -> line.get(i) - line.get(i + 1))
            .boxed()
            .toList();
        final var allPositive = differences.stream().allMatch(x -> x > 0);
        final var allNegative = differences.stream().allMatch(x -> x < 0);
        final var notTooSteep = differences.stream().allMatch(x -> Math.abs(x) <= 3);
        return (allPositive || allNegative) && notTooSteep;
    }

    static boolean isSafeWithProblemDampener(final List<Integer> line) {
        return IntStream.range(0, line.size())
            .mapToObj(i ->
                IntStream.range(0, line.size())
                    .filter(j -> i != j)
                    .mapToObj(line::get)
            )
            .map(Stream::toList)
            .anyMatch(Main::isSafe);
    }
}
