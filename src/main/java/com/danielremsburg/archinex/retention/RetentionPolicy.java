package com.danielremsburg.archinex.retention;

import com.danielremsburg.archinex.cache.Cache;
import com.danielremsburg.archinex.metadata.FileMetadata;
import com.danielremsburg.archinex.metadata.MetadataStore;
import com.danielremsburg.archinex.metadata.MetadataStoreException;
import com.danielremsburg.archinex.storage.StorageSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

public interface RetentionPolicy {

    String getName();

    String getDescription();

    void apply(FileMetadata metadata) throws MetadataStoreException, IOException;

    boolean shouldDelete(FileMetadata fileMetadata);
}
