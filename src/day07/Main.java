package day07;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class Main {
    public static void main() throws Exception {
        System.out.println("Part 1: " + part1());
    }

    static long part1() throws Exception {
        return parseInput(Path.of("./src/day07/input.txt"))
            .stream()
            .filter(Main::canBeMadeTrue)
            .mapToLong(Equation::testValue)
            .sum();
    }

    static boolean canBeMadeTrue(final Equation equation) {
        if (equation.numbers.isEmpty()) {
            return false;
        }

        if (equation.numbers.size() == 1) {
            return equation.testValue == equation.numbers.getFirst();
        }

        final var remainingNumbers = equation.numbers.subList(0, equation.numbers.size() - 1);

        final var add = new Equation(
            equation.testValue - equation.numbers.getLast(),
            remainingNumbers
        );

        final var mul = equation.testValue % equation.numbers.getLast() == 0
            ? Optional.of(
                new Equation(
                    equation.testValue / equation.numbers.getLast(),
                    remainingNumbers
                )
            )
            : Optional.<Equation>empty();

        return canBeMadeTrue(add) || mul.map(Main::canBeMadeTrue).orElse(false);
    }

    record Equation(
        long testValue,
        List<Integer> numbers
    ) {
    }

    static List<Equation> parseInput(final Path input) throws Exception {
        return Files.readAllLines(input)
            .stream()
            .map(line -> {
                final var parts = line.split(": ");
                return new Equation(
                    Long.parseLong(parts[0]),
                    Stream.of(parts[1].split("\\s")).map(Integer::parseInt).toList()
                );
            })
            .toList();
    }
}
