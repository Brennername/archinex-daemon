package com.danielremsburg.archinex.journal;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MemoryJournal implements Journal {

    private final List<String> logs = new ArrayList<>();

    @Override
    public void log(String message) {
        log(message, Instant.now());
    }

    @Override
    public void log(String message, Instant timestamp) {
        String logEntry = timestamp + ": " + message;
        logs.add(logEntry);
    }

    public List<String> getLogs() {
        return new ArrayList<>(logs); // Return a copy
    }

    public void clear() {
        logs.clear();
    }

    public List<String> search(String searchTerm) {
        return logs.stream()
                .filter(log -> log.contains(searchTerm))
                .collect(Collectors.toList());
    }

    public List<String> getLogsSince(Instant timestamp) {
        return logs.stream()
                .filter(log -> Instant.parse(log.substring(0, log.indexOf(':'))).isAfter(timestamp))
                .collect(Collectors.toList());
    }

    public int count() {
        return logs.size();
    }
}