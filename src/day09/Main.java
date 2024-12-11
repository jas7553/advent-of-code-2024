package day09;

import day09.Main.Block.FileBlock;
import day09.Main.Block.FreeBlock;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Main {
    public static void main() throws Exception {
        System.out.println("Part 1: " + part1());
    }

    static long part1() throws Exception {
        final var diskMap = parseInput(Path.of("./src/day09/input.txt"));
        var disk = Disk.from(diskMap);

        while (!isCompacted(disk)) {
            disk = next(disk);
        }

        return checksum(disk);
    }

    static boolean isCompacted(final Disk disk) {
        boolean gap = false;
        for (int i = 0; i < disk.blocks.size(); i++) {
            switch (disk.blocks.get(i)) {
                case FileBlock _ -> {
                    if (gap) {
                        return false;
                    }
                }
                case FreeBlock _ ->
                    gap = true;
            }
        }

        return true;
    }

    private static Disk next(final Disk disk) {
        final var firstFreeBlock = getFirstFreeBlock(disk)
            .orElseThrow(() -> new RuntimeException("could not find a free block"));

        final var lastFileBlock = getLastFileBlock(disk)
            .orElseThrow(() -> new RuntimeException("could not find a file block"));

        final var lastBlock = disk.blocks.getLast();

        return new Disk(
            Stream.of(
                disk.blocks.subList(0, firstFreeBlock.index)
                    .stream(),
                firstFreeBlock.block.repeat == 1
                    ? Stream.<Block>of(
                        new FileBlock(lastFileBlock.block.id, 1)
                    )
                    : Stream.<Block>of(
                        new FileBlock(lastFileBlock.block.id, 1),
                        new FreeBlock(firstFreeBlock.block.repeat - 1)
                    ),
                disk.blocks.subList(firstFreeBlock.index + 1, lastFileBlock.index)
                    .stream(),
                (switch (lastBlock) {
                    case FileBlock fileBlock -> Stream.<Block>of(
                        new FileBlock(fileBlock.id, fileBlock.repeat - 1),
                        new FreeBlock(1)
                    );
                    case FreeBlock freeBlock -> lastFileBlock.block.repeat == 1
                        ? Stream.<Block>of(
                            new FreeBlock(freeBlock.repeat + 1)
                        )
                        : Stream.<Block>of(
                            new FileBlock(lastFileBlock.block.id, lastFileBlock.block.repeat - 1),
                            new FreeBlock(freeBlock.repeat + 1)
                        );
                })
            )
            .flatMap(Function.identity())
            .toList()
        );
    }

    record IndexAndFreeBlock(int index, FreeBlock block) { }
    private static Optional<IndexAndFreeBlock> getFirstFreeBlock(final Disk disk) {
        for (int i = 0; i < disk.blocks.size(); i++) {
            if (disk.blocks.get(i) instanceof FreeBlock freeBlock) {
                return Optional.of(new IndexAndFreeBlock(i, freeBlock));
            }
        }

        return Optional.empty();
    }

    record IndexAndFileBlock(int index, FileBlock block) { }
    private static Optional<IndexAndFileBlock> getLastFileBlock(final Disk disk) {
        for (int i = disk.blocks.size() - 1; i >= 0; i--) {
            if (disk.blocks.get(i) instanceof FileBlock fileBlock) {
                return Optional.of(new IndexAndFileBlock(i, fileBlock));
            }
        }

        return Optional.empty();
    }

    private static long checksum(final Disk disk) {
        final var expandedBlocks = IntStream.range(0, disk.blocks.size())
            .boxed()
            .flatMap(i ->
                switch (disk.blocks.get(i)) {
                    case FreeBlock freeBlock -> Stream.generate(() -> 0)
                        .limit(freeBlock.repeat);
                    case FileBlock fileBlock -> Stream.generate(() -> fileBlock.id)
                        .limit(fileBlock.repeat);
                }
            )
            .toList();

        return IntStream.range(0, expandedBlocks.size())
            .mapToLong(i -> i * ((long) expandedBlocks.get(i)))
            .sum();
    }

    record DiskMap(String value) {
        @Override
        public String toString() {
            final var out = new StringBuilder();
            for (int i = 0; i < value.length(); i++) {
                final var repeat = Integer.parseInt(String.valueOf(value.charAt(i)));
                if (i % 2 == 1) {
                    out.append(".".repeat(repeat));
                } else {
                    out.append(String.valueOf(i / 2).repeat(repeat));
                }
            }

            return out.toString();
        }
    }

    record Disk(List<Block> blocks) {
        static Disk from(final DiskMap diskMap) {
            return new Disk(
                IntStream.range(0, diskMap.value.length())
                    .<Optional<Block>>mapToObj(i -> {
                        final var repeat = Integer.parseInt(String.valueOf(diskMap.value.charAt(i)));
                        return repeat == 0
                            ? Optional.empty()
                            : i % 2 == 1
                                ? Optional.of(new FreeBlock(repeat))
                                : Optional.of(new FileBlock(i / 2, repeat));
                    })
                    .flatMap(Optional::stream)
                    .toList()
            );
        }

        @Override
        public String toString() {
            return blocks.stream().map(Block::toString).collect(Collectors.joining(""));
        }
    }

    sealed interface Block {
        record FileBlock(int id, int repeat) implements Block {
            @Override
            public String toString() {
                return String.valueOf(id).repeat(repeat);
            }
        }

        record FreeBlock(int repeat) implements Block {
            @Override
            public String toString() {
                return ".".repeat(repeat);
            }
        }
    }

    static DiskMap parseInput(final Path input) throws Exception {
        final var lines = Files.readAllLines(input);

        return new DiskMap(lines.getFirst());
    }
}
