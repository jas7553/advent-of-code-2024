package day05;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Main {
    public static void main() throws Exception {
        System.out.println("Part 1: " + part1());
        System.out.println("Part 2: " + part2());
    }

    static int part1() throws Exception {
        final var input = parseInput(Path.of("./src/day05/input.txt"));

        return input.updates
            .stream()
            .filter(update -> isValid(input.pageOrderingRules, update))
            .mapToInt(Update::middlePage)
            .sum();
    }

    static int part2() throws Exception {
        final var input = parseInput(Path.of("./src/day05/input.txt"));

        return input.updates
            .stream()
            .filter(update -> !isValid(input.pageOrderingRules, update))
            .map(update -> makeValid(input.pageOrderingRules, update))
            .mapToInt(Update::middlePage)
            .sum();
    }

    private static boolean isValid(
        final List<PageOrderingRule> pageOrderingRules,
        final Update update
    ) {
        return IntStream.range(0, update.pageNumbers.size())
            .allMatch(position -> {
                final var left = update.pageNumbers.subList(0, position);
                final var right = update.pageNumbers.subList(
                    Math.min(position + 1, update.pageNumbers.size()),
                    update.pageNumbers.size()
                );

                return pageOrderingRules
                    .stream()
                    .noneMatch(pageOrderingRule -> {
                        if (update.pageNumbers.get(position) == pageOrderingRule.pageNumber) {
                            return left.contains(pageOrderingRule.mustComeBefore);
                        } else if (update.pageNumbers.get(position) == pageOrderingRule.mustComeBefore) {
                            return right.contains(pageOrderingRule.pageNumber);
                        } else {
                            return false;
                        }
                    });
            });
    }

    private static Update makeValid(
        final List<PageOrderingRule> pageOrderingRules,
        final Update update
    ) {
        var result = new Update(List.of());
        for (Integer page : update.pageNumbers) {
            result = allPossibleUpdatesWithNewPage(result, page)
                .filter(newUpdate -> isValid(pageOrderingRules, newUpdate))
                .findFirst()
                .get();
        }

        return result;
    }

    private static Stream<Update> allPossibleUpdatesWithNewPage(
        final Update update,
        final int page
    ) {
        return IntStream.rangeClosed(0, update.pageNumbers.size())
            .mapToObj(i -> {
                final var newPages = new ArrayList<>(update.pageNumbers);
                newPages.add(i, page);
                return new Update(newPages);
            });
    }

    record Input(
        List<PageOrderingRule> pageOrderingRules,
        List<Update> updates
    ) {
    }

    record PageOrderingRule(
        int pageNumber,
        int mustComeBefore
    ) {
    }

    record Update(List<Integer> pageNumbers) {
        public int middlePage() {
            return pageNumbers.get(pageNumbers.size() / 2);
        }
    }

    static Input parseInput(final Path input) throws Exception {
        final var lines = Files.readAllLines(input);
        final var sectionDivider = lines.indexOf("");

        final var pageOrderingRules = lines.subList(0, sectionDivider)
            .stream()
            .map(line -> {
                final var pair = Arrays.stream(line.split("\\|"))
                    .map(Integer::parseInt)
                    .toList();
                return new PageOrderingRule(pair.get(0), pair.get(1));
            })
            .toList();

        final var updates = lines.subList(sectionDivider + 1, lines.size())
            .stream()
            .map(line ->
                new Update(
                    Arrays.stream(line.split(","))
                        .map(Integer::parseInt)
                        .toList()
                )
            )
            .toList();

        return new Input(pageOrderingRules, updates);
    }
}
