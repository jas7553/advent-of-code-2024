package day06;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static day06.Main.Direction.DOWN;
import static day06.Main.Direction.LEFT;
import static day06.Main.Direction.RIGHT;
import static day06.Main.Direction.UP;

public class Main {
    public static void main() throws Exception {
        System.out.println("Part 1: " + part1());
        System.out.println("Part 2: " + part2());
    }

    static long part1() throws Exception {
        final var initialState = parseInput(Path.of("./src/day06/input.txt"));
        return getAllVisited(initialState)
            .count();
    }

    static long part2() throws Exception {
        final var initialState = parseInput(Path.of("./src/day06/input.txt"));

        final var invalidChars = Set.of('#', '^', '>', 'v', '<');

        return getAllVisited(initialState)
            .filter(position -> !invalidChars.contains(initialState.map.at(position)))
            .parallel()
            .map(position -> doesObstaclePositionLoop(initialState, position))
            .filter(Boolean::booleanValue)
            .count();
    }

    static Stream<Position> getAllVisited(final State state) {
        var currentState = state;
        while (currentState.map.isOnBoard(currentState.guard)) {
            currentState = nextState(currentState);
        }

        return currentState.visited
            .stream()
            .map(position -> new Position(position.row, position.col))
            .distinct();
    }

    static boolean doesObstaclePositionLoop(
        final State state,
        final Position obstaclePosition
    ) {
        var stateWithObstacle = new State(
            state.map,
            state.guard,
            Set.of(),
            Optional.of(obstaclePosition)
        );

        while (true) {
            if (!stateWithObstacle.map.isOnBoard(stateWithObstacle.guard)) {
                // not loop
                return false;
            } else if (stateWithObstacle.visited.contains(stateWithObstacle.guard)) {
                // loop
                return true;
            } else {
                // continue
                stateWithObstacle = nextState(stateWithObstacle);
            }
        }
    }

    static State nextState(final State currentState) {
        final var map = currentState.map;
        final var guard = currentState.guard;
        final var visited = currentState.visited;

        final var newPosition = switch (guard.direction) {
            case UP -> new PositionWithDirection(guard.row - 1, guard.col, UP);
            case RIGHT -> new PositionWithDirection(guard.row, guard.col + 1, RIGHT);
            case DOWN -> new PositionWithDirection(guard.row + 1, guard.col, DOWN);
            case LEFT -> new PositionWithDirection(guard.row, guard.col - 1, LEFT);
        };

        if (map.isOnBoard(newPosition)) {
            final var isObstacle = currentState.obstacle
                .map(position -> position.row == newPosition.row && position.col == newPosition.col)
                .orElse(false);
            if (map.at(newPosition) == '#' || isObstacle) {
                return new State(map, guard.turn(), visited, currentState.obstacle);
            }
        }

        return new State(
            map,
            newPosition,
            Stream.concat(visited.stream(), Stream.of(guard))
                .collect(Collectors.toSet()),
            currentState.obstacle
        );
    }

    record State(
        Map map,
        PositionWithDirection guard,
        Set<PositionWithDirection> visited,
        Optional<Position> obstacle
    ) {
    }

    record Position(
        int row,
        int col
    ) {
    }

    record PositionWithDirection(
        int row,
        int col,
        Direction direction
    ) {
        PositionWithDirection turn() {
            return new PositionWithDirection(
                row,
                col,
                switch (direction) {
                    case UP -> RIGHT;
                    case RIGHT -> DOWN;
                    case DOWN -> LEFT;
                    case LEFT -> UP;
                }
            );
        }
    }

    enum Direction {
        UP, RIGHT, DOWN, LEFT
    }

    record Map(List<String> lines) {
        boolean isOnBoard(final PositionWithDirection position) {
            return position.row >= 0 && position.row < lines.size() &&
                position.col >= 0 && position.col < lines.getFirst().length();
        }

        char at(final Position position) {
            return lines.get(position.row).charAt(position.col);
        }

        char at(final PositionWithDirection position) {
            return lines.get(position.row).charAt(position.col);
        }
    }

    static PositionWithDirection findGuard(final Map map) {
        for (int row = 0; row < map.lines.size(); row++) {
            for (int col = 0; col < map.lines.get(row).length(); col++) {
                switch (map.lines.get(row).charAt(col)) {
                    case '^' -> {
                        return new PositionWithDirection(row, col, UP);
                    }
                    case '>' -> {
                        return new PositionWithDirection(row, col, RIGHT);
                    }
                    case 'v' -> {
                        return new PositionWithDirection(row, col, DOWN);
                    }
                    case '<' -> {
                        return new PositionWithDirection(row, col, LEFT);
                    }
                }
            }
        }

        throw new RuntimeException("No guard found?");
    }

    static State parseInput(final Path input) throws Exception {
        final var lines = Files.readAllLines(input);
        final var map = new Map(lines);
        return new State(
            map,
            findGuard(map),
            Set.of(),
            Optional.empty()
        );
    }
}
