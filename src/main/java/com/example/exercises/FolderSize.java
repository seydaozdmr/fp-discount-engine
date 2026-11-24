package com.example.exercises;

import com.example.fpcore.List;
import com.example.fpcore.Result;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

public class FolderSize {

    sealed interface Node permits File, Dir {}

    record File(String name, long size) implements Node {}

    record Dir(String name, List<Node> children) implements Node {}

    static long totalSize(Node node) {
        if (node instanceof File file) {
            return file.size();
        }

        Dir dir = (Dir) node;
        return dir.children().foldLeft(0L, acc -> child -> acc + totalSize(child));
    }

    static Result<Node> readTree(Path path) {
        try {
            BasicFileAttributes attributes = Files.readAttributes(
                    path, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
            if (attributes.isRegularFile()) {
                return Result.success(new File(path.toString(), attributes.size()));
            }
            if (attributes.isDirectory()) {
                Path[] children;
                try (var paths = Files.list(path)) {
                    children = paths.sorted().toArray(Path[]::new);
                }
                return List.traverse(List.list(children), FolderSize::readTree)
                        .map(nodes -> new Dir(path.toString(), nodes));
            }
            return Result.failure("Sembolik bağlantı veya desteklenmeyen dosya türü: " + path);
        } catch (IOException | UncheckedIOException | SecurityException exception) {
            return Result.failure(exception);
        }
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Kullanım: FolderSize <dosya veya klasör yolu>");
            return;
        }

        Result.of(args[0])
                .map(Path::of)
                .flatMap(FolderSize::readTree)
                .map(FolderSize::totalSize)
                .forEachOrFail(size -> System.out.println("Toplam: " + size + " bayt"))
                .forEach(error -> System.err.println("Okunamadı: " + error));
    }
}
