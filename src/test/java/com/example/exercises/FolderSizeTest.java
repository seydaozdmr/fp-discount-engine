package com.example.exercises;

import com.example.fpcore.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.example.exercises.FolderSize.readTree;
import static com.example.exercises.FolderSize.totalSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FolderSizeTest {

    @TempDir
    Path directory;

    @Test
    void sums_nested_nodes() {
        var root = new FolderSize.Dir("root", List.list(
                new FolderSize.File("a.txt", 10),
                new FolderSize.Dir("nested", List.list(
                        new FolderSize.File("b.txt", 30),
                        new FolderSize.File("c.txt", 5)))));

        assertEquals(45L, totalSize(root));
    }

    @Test
    void reads_nested_files_from_disk() throws IOException {
        Files.write(directory.resolve("a.txt"), new byte[10]);
        Path nested = Files.createDirectory(directory.resolve("alt klasör"));
        Files.write(nested.resolve("b.txt"), new byte[30]);
        Files.write(nested.resolve("c.txt"), new byte[5]);
        Files.createDirectory(nested.resolve("empty"));

        assertEquals(45L, totalSize(readTree(directory).getOrThrow()));
    }

    @Test
    void empty_directory_has_zero_size() {
        assertEquals(0L, totalSize(readTree(directory).getOrThrow()));
    }

    @Test
    void accepts_a_single_file_path() throws IOException {
        Path file = Files.write(directory.resolve("single.txt"), new byte[7]);

        assertEquals(7L, totalSize(readTree(file).getOrThrow()));
    }

    @Test
    void missing_path_is_a_failure() {
        assertTrue(readTree(directory.resolve("missing")).isFailure());
    }

    @Test
    void symbolic_link_cycle_is_a_failure_instead_of_partial_total() throws IOException {
        Files.write(directory.resolve("a.txt"), new byte[10]);
        Files.createSymbolicLink(directory.resolve("cycle"), directory);

        assertTrue(readTree(directory).isFailure());
    }
}
