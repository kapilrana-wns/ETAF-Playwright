package wns.automation.utilities;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipFile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TestUtilityTest {

    @Test
    void reportBundleContainsOnlyTheCurrentRun() throws Exception {
        Path runDirectory = Files.createTempDirectory("etaf-report-");
        Path historicalFile = runDirectory.resolveSibling(
                runDirectory.getFileName() + "-previous.html");
        Files.writeString(runDirectory.resolve("Result.html"), "<html>current</html>");
        Files.createDirectories(runDirectory.resolve("screenshots"));
        Files.writeString(runDirectory.resolve("screenshots").resolve("failed.png"), "image");
        Files.writeString(historicalFile, "historical");

        Method createBundle = TestUtility.class.getDeclaredMethod("createReportBundle", Path.class);
        createBundle.setAccessible(true);
        Path bundle = (Path) createBundle.invoke(null, runDirectory);

        try (ZipFile zip = new ZipFile(bundle.toFile())) {
            Set<String> entries = zip.stream()
                    .map(entry -> entry.getName())
                    .collect(Collectors.toSet());

            assertEquals(Set.of("Result.html", "screenshots/failed.png"), entries);
            assertTrue(entries.contains("Result.html"));
            assertTrue(entries.contains("screenshots/failed.png"));
            assertFalse(entries.contains(historicalFile.getFileName().toString()));
            assertFalse(entries.stream().anyMatch(name -> name.endsWith(".zip")));
        } finally {
            Files.deleteIfExists(bundle);
            Files.deleteIfExists(runDirectory.resolve("screenshots").resolve("failed.png"));
            Files.deleteIfExists(runDirectory.resolve("screenshots"));
            Files.deleteIfExists(runDirectory.resolve("Result.html"));
            Files.deleteIfExists(runDirectory);
            Files.deleteIfExists(historicalFile);
        }
    }
}
