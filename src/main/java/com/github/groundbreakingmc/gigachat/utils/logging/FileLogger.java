package com.github.groundbreakingmc.gigachat.utils.logging;

import com.github.groundbreakingmc.gigachat.GigaChat;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;
import java.util.zip.GZIPOutputStream;

public final class FileLogger {

    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static final DateTimeFormatter TIME_PATTERN = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final Path filePath;

    public FileLogger(final GigaChat plugin, final String folderName) {
        final String logFolder = plugin.getDataFolder()
                + File.separator + "logs"
                + File.separator + folderName;
        final File logFile = new File(logFolder, "latest.log");
        this.filePath = logFile.toPath();

        if (logFile.exists()) {
            archiveLogFile(logFile, logFolder);
        } else {
            try {
                Files.createDirectories(Paths.get(logFolder));
                logFile.createNewFile();
            } catch (final Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public void log(final Supplier<String> logEntry) {
        executor.submit(() -> {
            final String time = ZonedDateTime.now(ZoneId.systemDefault()).format(TIME_PATTERN);

            try (final BufferedWriter writer = Files.newBufferedWriter(this.filePath, StandardOpenOption.APPEND)) {
                writer.write("[" + time + "] " + logEntry.get());
                writer.newLine();
            } catch (final IOException ex) {
                ex.printStackTrace();
            }
        });
    }

    private static void archiveLogFile(final File logFile, final String logFolder) {
        executor.submit(() -> {
            try {
                final File archive = getArchiveFile(logFolder);

                try (final FileInputStream inputStream = new FileInputStream(logFile);
                     final FileOutputStream outputStream = new FileOutputStream(archive);
                     final GZIPOutputStream gzip = new GZIPOutputStream(outputStream)) {
                    final byte[] buffer = new byte[1024];
                    int length;
                    while ((length = inputStream.read(buffer)) > 0) {
                        gzip.write(buffer, 0, length);
                    }
                }

                logFile.delete();
                logFile.createNewFile();
            } catch (final Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    private static File getArchiveFile(final String logFolder) {
        final String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        int index = 1;
        File archive;

        do {
            archive = new File(logFolder, date + "-" + index + ".log.gz");
            index++;
        } while (archive.exists());

        return archive;
    }
}
