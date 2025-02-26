package com.danielremsburg.archinex.journal;

import com.danielremsburg.archinex.config.ArchinexConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FileJournal implements Journal {

    private static final Logger logger = LoggerFactory.getLogger(FileJournal.class);

    private final Path journalFile;

    public FileJournal(ArchinexConfig config) {
        String filePath = config.getJournalFilePath();
        if (filePath == null || filePath.isEmpty()) {
            throw new IllegalArgumentException("Journal file path is not configured.");
        }
        journalFile = Paths.get(filePath);
        try {
            Files.createDirectories(journalFile.getParent()); // Create parent directories
            if (!Files.exists(journalFile)) {
                Files.createFile(journalFile); // Create the file if it doesn't exist
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to create journal file: " + e.getMessage(), e);
        }
    }

    @Override
    public void log(String message) {
        log(message, Instant.now());
    }

    @Override
    public void log(String message, Instant timestamp) {
        String logEntry = timestamp + ": " + message;
        try {
            Files.write(journalFile, (logEntry + System.lineSeparator()).getBytes(), java.nio.file.StandardOpenOption.APPEND);
            logger.info("Logged to file: {}", logEntry);
        } catch (IOException e) {
            logger.error("Error writing to journal file: {}", e.getMessage(), e);
            throw new RuntimeException("Error writing to journal file: " + e.getMessage(), e);
        }
    }


    public List<String> getLogs() {
        try {
            if (!Files.exists(journalFile)) {
                return new ArrayList<>(); // Return empty list if file doesn't exist
            }
            return Files.lines(journalFile).collect(Collectors.toList());
        } catch (IOException e) {
            logger.error("Error reading from journal file: {}", e.getMessage(), e);
            throw new RuntimeException("Error reading from journal file: " + e.getMessage(), e);
        }
    }

    public void clear() {
        try {
            Files.write(journalFile, new byte[0]); // Overwrite with empty content
            logger.info("Journal file cleared.");
        } catch (IOException e) {
            logger.error("Error clearing journal file: {}", e.getMessage(), e);
            throw new RuntimeException("Error clearing journal file: " + e.getMessage(), e);
        }
    }

    public List<String> search(String searchTerm) {
        try {
            return Files.lines(journalFile)
                    .filter(log -> log.contains(searchTerm))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            logger.error("Error searching journal file: {}", e.getMessage(), e);
            throw new RuntimeException("Error searching journal file: " + e.getMessage(), e);
        }
    }

    public List<String> getLogsSince(Instant timestamp) {
        try {
            return Files.lines(journalFile)
                    .filter(log -> Instant.parse(log.substring(0, log.indexOf(':'))).isAfter(timestamp))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            logger.error("Error getting logs since timestamp: {}", e.getMessage(), e);
            throw new RuntimeException("Error getting logs since timestamp: " + e.getMessage(), e);
        }
    }

    public int count() {
        try {
            return (int) Files.lines(journalFile).count();
        } catch (IOException e) {
            logger.error("Error getting log count: {}", e.getMessage(), e);
            throw new RuntimeException("Error getting log count: " + e.getMessage(), e);
        }
    }
}