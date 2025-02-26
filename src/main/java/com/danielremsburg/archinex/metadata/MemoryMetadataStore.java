package com.danielremsburg.archinex.metadata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class MemoryMetadataStore implements MetadataStore {

    private final Map<UUID, FileMetadata> metadataMap = new HashMap<>();

    @Override
    public void store(FileMetadata metadata) throws MetadataStoreException {
        metadataMap.put(metadata.getUuid(), metadata);
    }

    @Override
    public FileMetadata get(UUID uuid) throws MetadataStoreException {
        return metadataMap.get(uuid);
    }

    @Override
    public void delete(UUID uuid) throws MetadataStoreException {
        metadataMap.remove(uuid);
    }

    @Override
    public List<FileMetadata> getAllFiles() throws MetadataStoreException {
        return new ArrayList<>(metadataMap.values());
    }

    @Override
    public void deleteAll() throws MetadataStoreException {
        metadataMap.clear(); // Clear the map to delete all entries
    }

    public FileMetadata getByPath(String path) throws MetadataStoreException {
        return metadataMap.values().stream()
                .filter(metadata -> metadata.getPath().equals(path))
                .findFirst()
                .orElse(null);
    }

    public List<FileMetadata> searchByPath(String path) throws MetadataStoreException {
         return metadataMap.values().stream()
                .filter(metadata -> metadata.getPath().contains(path))
                .collect(Collectors.toList());
    }

    public void update(FileMetadata metadata) throws MetadataStoreException {
        metadataMap.put(metadata.getUuid(), metadata); // Update by replacing
    }

    public int count() throws MetadataStoreException {
        return metadataMap.size();
    }
}