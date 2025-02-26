package com.danielremsburg.archinex.retention;

import com.danielremsburg.archinex.metadata.FileMetadata;
import com.danielremsburg.archinex.metadata.MetadataStoreException;
import java.io.IOException;

public interface RetentionPolicy {

    String getName();

    String getDescription();

    void apply(FileMetadata metadata) throws MetadataStoreException, IOException;

    boolean shouldDelete(FileMetadata fileMetadata);
}
