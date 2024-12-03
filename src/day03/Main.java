package day03;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

public class Main {
    public static void main() throws Exception {
        System.out.println("Part 1: " + part1());
        System.out.println("Part 2: " + part2());
    }

    static int part1() throws Exception {
        final var input = parseInput(Path.of("./src/day03/input.txt"));

        return Pattern.compile("mul\\((\\d{1,3}),(\\d{1,3})\\)")
            .matcher(input)
            .results()
            .map(match -> Integer.parseInt(match.group(1)) * Integer.parseInt(match.group(2)))
            .mapToInt(Integer::intValue)
            .sum();
    }

    record State(boolean enabled, int total) { }

    static int part2() throws Exception {
        final var input = parseInput(Path.of("./src/day03/input.txt"));

        return Pattern
            .compile("(mul\\((\\d{1,3}),(\\d{1,3})\\))|(do\\(\\))|(don't\\(\\))")
            .matcher(input)
            .results()
            .reduce(
                new State(true, 0),
                (state, match) ->
                    switch (match.group()) {
                        case "don't()" -> new State(false, state.total);
                        case "do()" -> new State(true, state.total);
                        default -> state.enabled
                            ? new State(true, state.total + Integer.parseInt(match.group(2)) * Integer.parseInt(match.group(3)))
                            : state;
                    },
                (state1, _) -> state1 // ignore
            )
            .total;
    }

    static String parseInput(final Path input) throws Exception {
        return String.join(
            "",
            Files.readAllLines(input)
        );
    }
}
