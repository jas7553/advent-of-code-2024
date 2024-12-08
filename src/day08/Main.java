package day08;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Main {
    public static void main() throws Exception {
        System.out.println("Part 1: " + part1());
        System.out.println("Part 2: " + part2());
    }

    record Input(
        Map<Character, List<Point>> antennas,
        int mapWidth,
        int mapHeight
    ) {
    }

    record Point(int row, int col) {
    }

    static long part1() throws Exception {
        final var input = parseInput(Path.of("./src/day08/input.txt"));

        return input.antennas
            .values()
            .stream()
            .flatMap(Main::getAntinodesForAntenna)
            .filter(point -> isOnMap(input, point))
            .collect(Collectors.toSet())
            .size();
    }

    static long part2() throws Exception {
        final var input = parseInput(Path.of("./src/day08/input.txt"));

        return input.antennas
            .values()
            .stream()
            .flatMap(points ->
                getAntinodesForAntennaWithResonantHarmonics(input, points)
            )
            .collect(Collectors.toSet())
            .size();
    }

    static Stream<Point> getAntinodesForAntenna(final List<Point> points) {
        return points
            .stream()
            .flatMap(point ->
                points.stream()
                    .filter(otherPoint -> point != otherPoint)
                    .map(otherPoint -> getAntinode(point, otherPoint))
            )
            .distinct();
    }

    static Stream<Point> getAntinodesForAntennaWithResonantHarmonics(
        final Input input,
        final List<Point> points
    ) {
        return points
            .stream()
            .flatMap(point ->
                points.stream()
                    .filter(otherPoint -> point != otherPoint)
                    .flatMap(otherPoint -> {
                        final var antinodes = new ArrayList<>(List.of(point, otherPoint));
                        var point1 = point;
                        var point2 = otherPoint;
                        while (true) {
                            final var antinode = getAntinode(point1, point2);
                            if (!isOnMap(input, antinode)) {
                                break;
                            }
                            antinodes.add(antinode);
                            point1 = point2;
                            point2 = antinode;
                        }

                        return antinodes.stream();
                    })
            )
            .distinct();
    }

    static Point getAntinode(final Point point, final Point otherPoint) {
        final int row;
        if (otherPoint.row > point.row) {
            row = otherPoint.row + (otherPoint.row - point.row);
        } else if (point.row == otherPoint.row) {
            row = point.row;
        } else {
            row = otherPoint.row - (point.row - otherPoint.row);
        }

        final int col;
        if (otherPoint.col > point.col) {
            col = otherPoint.col + (otherPoint.col - point.col);
        } else if (point.col == otherPoint.col) {
            col = point.col;
        } else {
            col = otherPoint.col - (point.col - otherPoint.col);
        }

        return new Point(row, col);
    }

    static boolean isOnMap(final Input input, final Point point) {
        return point.row >= 0 &&
            point.row < input.mapHeight &&
            point.col >= 0 &&
            point.col < input.mapWidth;
    }

    static Input parseInput(final Path input) throws Exception {
        final var lines = Files.readAllLines(input);

        final var antennas = new HashMap<Character, List<Point>>();
        IntStream.range(0, lines.size())
            .forEach(row ->
                IntStream.range(0, lines.getFirst().length())
                    .filter(col -> lines.get(row).charAt(col) != '.')
                    .forEach(col -> {
                        final var antenna = lines.get(row).charAt(col);
                        antennas.putIfAbsent(antenna, new ArrayList<>());
                        antennas.get(antenna).add(new Point(row, col));
                    })
            );

        return new Input(
            antennas,
            lines.size(),
            lines.getFirst().length()
        );
    }
}
