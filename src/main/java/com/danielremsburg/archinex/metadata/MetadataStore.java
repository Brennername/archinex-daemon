package com.danielremsburg.archinex.metadata;

import java.util.List;
import java.util.UUID;

public interface MetadataStore {

    void store(FileMetadata metadata) throws MetadataStoreException;

    FileMetadata get(UUID uuid) throws MetadataStoreException;

    void delete(UUID uuid) throws MetadataStoreException;

    List<FileMetadata> getAllFiles() throws MetadataStoreException;

    void deleteAll() throws MetadataStoreException;

    void update(FileMetadata metadata) throws MetadataStoreException;

}