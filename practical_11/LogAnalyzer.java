import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.Locale;

public class LogAnalyzer {
    private static final String KEYWORD = "ERROR";

    public static void main(String[] args) {
        Path logDirectory = Path.of("sample-logs");

        List<String> fileNames = List.of(
                "application.log",
                "database.log",
                "server.log"
        );

        try {
            Files.createDirectories(logDirectory);

            // Create sample files so the program can run immediately.
            Files.write(logDirectory.resolve("application.log"), List.of(
                    "INFO Application started",
                    "ERROR Could not load configuration",
                    "INFO Application stopped"
            ));

            Files.write(logDirectory.resolve("database.log"), List.of(
                    "INFO Database connected",
                    "WARNING Slow query",
                    "ERROR Connection lost"
            ));

            Files.write(logDirectory.resolve("server.log"), List.of(
                    "INFO Server listening",
                    "INFO Request received"
            ));

            long totalLines = 0;
            long matchingLines = 0;

            for (String fileName : fileNames) {
                Path file = logDirectory.resolve(fileName);
                long fileLines = 0;
                long fileMatches = 0;

                try (BufferedReader reader = Files.newBufferedReader(file)) {
                    String line;

                    while ((line = reader.readLine()) != null) {
                        fileLines++;

                        if (line.toLowerCase(Locale.ROOT)
                                .contains(KEYWORD.toLowerCase(Locale.ROOT))) {
                            fileMatches++;
                        }
                    }
                }

                BasicFileAttributes attributes =
                        Files.readAttributes(file, BasicFileAttributes.class);

                totalLines += fileLines;
                matchingLines += fileMatches;

                System.out.println(fileName + ":");
                System.out.println("  Size: " + attributes.size() + " bytes");
                System.out.println("  Lines: " + fileLines);
                System.out.println("  Lines containing \"" + KEYWORD + "\": "
                        + fileMatches);
            }

            System.out.println("\nTotals across all files:");
            System.out.println("  Total lines: " + totalLines);
            System.out.println("  Lines containing \"" + KEYWORD + "\": "
                    + matchingLines);
        } catch (IOException e) {
            System.err.println("File operation failed: " + e.getMessage());
        }
    }
}