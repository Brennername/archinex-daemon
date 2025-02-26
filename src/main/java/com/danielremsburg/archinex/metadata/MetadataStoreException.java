package com.danielremsburg.archinex.metadata;

public class MetadataStoreException extends Exception {

    public MetadataStoreException(String message) {
        super(message);
    }

    public MetadataStoreException(String message, Throwable cause) {
        super(message, cause);
    }
}