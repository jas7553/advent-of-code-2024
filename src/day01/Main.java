import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

private record Input(List<Integer> leftList, List<Integer> rightList) { }

public static void main() throws Exception {
    System.out.println("Part 1: " + part1());
    System.out.println("Part 2: " + part2());
}

public static int part1() throws Exception {
    final var input = parseInput(Files.readAllLines(Path.of("./src/day01/input.txt")));

    final var leftList = input.leftList.stream().sorted().toList();
    final var rightList = input.rightList.stream().sorted().toList();

    return IntStream.range(0, leftList.size())
        .map(i -> Math.abs(leftList.get(i) - rightList.get(i)))
        .sum();
}

public static int part2() throws Exception {
    final var input = parseInput(Files.readAllLines(Path.of("./src/day01/input.txt")));

    final var rightListFrequency = input.rightList.stream()
        .collect(
            Collectors.groupingBy(
                Function.identity(),
                Collectors.summingInt(x -> x)
            )
        );

    return input.leftList.stream()
        .reduce(
            0,
            (x, y) -> x + rightListFrequency.getOrDefault(y, 0)
        );
}

private static Input parseInput(final List<String> lines) {
    return lines.stream().map(line -> {
            final var parts = line.split("\\s+");
            return new int[]{
                Integer.parseInt(parts[0]),
                Integer.parseInt(parts[1])
            };
        })
        .collect(
            () -> new Input(new ArrayList<>(), new ArrayList<>()),
            (acc, pair) -> {
                acc.leftList.add(pair[0]);
                acc.rightList.add(pair[1]);
            },
            (_, _) -> { } // no-op combiner
        );
}
