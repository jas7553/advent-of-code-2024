package day07;

import day07.Main.Node.Operation;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {
    public static void main() throws Exception {
        System.out.println("Part 1: " + part1());
        System.out.println("Part 2: " + part2());
    }

    record Equation(
        long testValue,
        List<Integer> numbers
    ) {
    }

    sealed interface Node {
        long evaluate();

        record Number(long value) implements Node {
            @Override
            public long evaluate() {
                return value;
            }
        }

        enum Operator {
            ADD, MUL, CONCAT
        }

        record Operation(
            long left,
            Operator operator,
            Node right
        ) implements Node {
            @Override
            public long evaluate() {
                return switch (operator) {
                    case ADD -> switch (right) {
                        case Number number -> left + number.value;
                        case Operation operation -> new Operation(
                            left + operation.left,
                            operation.operator,
                            operation.right
                        )
                            .evaluate();
                    };
                    case MUL -> switch (right) {
                        case Number number -> left * number.value;
                        case Operation operation -> new Operation(
                            left * operation.left,
                            operation.operator,
                            operation.right
                        )
                            .evaluate();
                    };
                    case CONCAT -> switch (right) {
                        case Number number -> concatAsStrings(left, number.value);
                        case Operation operation -> new Operation(
                            concatAsStrings(left, operation.left),
                            operation.operator,
                            operation.right
                        )
                            .evaluate();
                    };
                };
            }
        }
    }

    static long part1() throws Exception {
        return parseInput(Path.of("./src/day07/input.txt"))
            .stream()
            .filter(equation ->
                get(equation.numbers, false)
                    .stream()
                    .map(Node::evaluate)
                    .anyMatch(l -> equation.testValue == l)
            )
            .mapToLong(Equation::testValue)
            .sum();
    }

    static long part2() throws Exception {
        return parseInput(Path.of("./src/day07/input.txt"))
            .stream()
            .filter(equation ->
                get(equation.numbers, true)
                    .stream()
                    .map(Node::evaluate)
                    .anyMatch(l -> equation.testValue == l)
            )
            .mapToLong(Equation::testValue)
            .sum();
    }

    static List<Node> get(final List<Integer> numbers, final boolean withConcatenation) {
        return switch (numbers.size()) {
            case 0 -> List.of(new Node.Number(0L));
            case 1 -> List.of(new Node.Number(numbers.getFirst().longValue()));
            default -> get(numbers.subList(1, numbers.size()), withConcatenation)
                .stream()
                .flatMap(node ->
                    Stream.concat(
                        Stream.<Node>of(
                            new Operation(
                                numbers.getFirst(),
                                Node.Operator.ADD,
                                node
                            ),
                            new Operation(
                                numbers.getFirst(),
                                Node.Operator.MUL,
                                node
                            )
                        ),
                        withConcatenation
                            ? Stream.of(
                                new Operation(
                                    numbers.getFirst(),
                                    Node.Operator.CONCAT,
                                    node
                                )
                            )
                            : Stream.of()
                    )
                )
                .toList();
        };
    }

    static long concatAsStrings(final long... longs) {
        return Long.parseLong(
            Arrays.stream(longs)
                .mapToObj(Long::toString)
                .collect(Collectors.joining(""))
        );
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
