package com.danielremsburg.archinex.storage;

import java.io.IOException;

public class StorageException extends IOException {

    public StorageException(String message) {
        super(message);
    }

    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
