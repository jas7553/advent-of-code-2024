package day10;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Main {
    public static void main() throws Exception {
        System.out.println("Part 1: " + part1());
        System.out.println("Part 2: " + part2());
    }

    static long part1() throws Exception {
        final var topographicMap = parseInput(Path.of("./src/day10/input.txt"));

        return allPoints(topographicMap)
            .filter(topographicMap::isTrailhead)
            .mapToInt(trailhead -> countSummits(topographicMap, trailhead))
            .sum();
    }

    static long part2() throws Exception {
        final var topographicMap = parseInput(Path.of("./src/day10/input.txt"));

        return allPoints(topographicMap)
            .filter(topographicMap::isTrailhead)
            .mapToInt(trailhead -> scoreTrailhead(topographicMap, trailhead))
            .sum();
    }

    static Stream<Point> allPoints(final TopographicMap map) {
        return IntStream.range(0, map.height)
            .mapToObj(row ->
                IntStream.range(0, map.width)
                    .mapToObj(col -> new Point(row, col))
            )
            .flatMap(Function.identity());
    }

    static int countSummits(final TopographicMap map, final Point trailhead) {
        return (int) followTrail(map, List.of(trailhead))
            .stream()
            .map(List::getLast)
            .distinct()
            .count();
    }

    static int scoreTrailhead(final TopographicMap map, final Point trailhead) {
        return followTrail(map, List.of(trailhead))
            .size();
    }

    record Point(int row, int col) {
        @Override
        public String toString() {
            return "(" + row + "," + col + ")";
        }
    }

    record TopographicMap(List<List<Integer>> value, int width, int height) {
        int at(final Point point) {
            return value.get(point.row).get(point.col);
        }

        boolean isTrailhead(final Point point) {
            return value.get(point.row).get(point.col) == 0;
        }

        @Override
        public String toString() {
            return value.stream()
                .map(row ->
                    row.stream()
                        .map(String::valueOf)
                        .collect(Collectors.joining())
                )
                .collect(Collectors.joining("\n"));
        }
    }

    static List<List<Point>> followTrail(final TopographicMap map, final List<Point> trail) {
        if (map.at(trail.getLast()) == 9) {
            return List.of(trail);
        }

        return surrounding(map, trail.getLast())
            .filter(neighbor -> map.at(trail.getLast()) + 1 == map.at(neighbor))
            .map(neighbor -> Stream.concat(trail.stream(), Stream.of(neighbor)).toList())
            .flatMap(newTrail -> followTrail(map, newTrail).stream())
            .toList();
    }

    static Stream<Point> surrounding(final TopographicMap map, final Point point) {
        return Stream.<Optional<Point>>of(
                point.col == map.width - 1 ? Optional.empty() : Optional.of(new Point(point.row, point.col + 1)),
                point.row == map.height - 1 ? Optional.empty() : Optional.of(new Point(point.row + 1, point.col)),
                point.col == 0 ? Optional.empty() : Optional.of(new Point(point.row, point.col - 1)),
                point.row == 0 ? Optional.empty() : Optional.of(new Point(point.row - 1, point.col))
            )
            .flatMap(Optional::stream);
    }

    static TopographicMap parseInput(final Path input) throws Exception {
        final var lines = Files.readAllLines(input);

        final var map = lines.stream()
            .map(line ->
                Arrays.stream(line.split("")).map(Integer::parseInt).toList()
            )
            .toList();
        return new TopographicMap(
            map,
            map.getFirst().size(),
            map.size()
        );
    }
}
