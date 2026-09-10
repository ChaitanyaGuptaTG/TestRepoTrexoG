package pages.IDSPage.helpers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Comparator;
import java.util.stream.Stream;

public final class DownloadFileHelper {

    private DownloadFileHelper() {
        // static utility - no instances
    }

    public static Path waitForNewFile(Path downloadDir, String filenameContains,
                                       int timeoutSeconds, long pollMillis, Instant after)
            throws InterruptedException, IOException {

        Instant deadline = Instant.now().plusSeconds(timeoutSeconds);

        while (Instant.now().isBefore(deadline)) {
            Path candidate;
            // Files.list holds an OPEN OS directory handle - it must be closed,
            // or a long-running suite exhausts its file descriptors.
            try (Stream<Path> files = Files.list(downloadDir)) {
                candidate = files
                        .filter(Files::isRegularFile)
                        .filter(DownloadFileHelper::isNotBrowserTempFile)
                        .filter(path -> matchesName(path, filenameContains))
                        .filter(path -> isModifiedAfter(path, after))
                        .max(Comparator.comparingLong(path -> path.toFile().lastModified()))
                        .orElse(null);
            }

            if (candidate != null) {
                return candidate;
            }
            Thread.sleep(pollMillis);
        }
        return null;
    }

    public static boolean isNotBrowserTempFile(Path path) {
        String name = path.getFileName().toString().toLowerCase();
        return !name.endsWith(".crdownload")   // Chrome partial
                && !name.endsWith(".part")      // Firefox partial
                && !name.endsWith(".tmp")
                && !name.startsWith(".")        // .DS_Store etc.
                && !name.startsWith("~$")       // Office lock files
                && !name.contains("chrome")
                && !name.contains("google")
                && !name.contains("edge")
                && !name.contains("chromium");
    }

    public static boolean isModifiedAfter(Path path, Instant after) {
        return Instant.ofEpochMilli(path.toFile().lastModified()).isAfter(after);
    }

    public static long sizeInKb(Path file) {
        try {
            return Files.size(file) / 1024;
        } catch (IOException e) {
            return -1;
        }
    }

    private static boolean matchesName(Path path, String filenameContains) {
        return filenameContains == null
                || filenameContains.isBlank()
                || path.getFileName().toString().contains(filenameContains);
    }
}
