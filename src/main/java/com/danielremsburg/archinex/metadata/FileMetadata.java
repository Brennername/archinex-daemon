package com.danielremsburg.archinex.metadata;

import java.time.Instant;
import java.util.UUID;

public class FileMetadata {

    private final UUID uuid;
    private final String path;
    private final long size; // Size in bytes
    private final Instant creationDate;
    private Instant lastModifiedDate;
    private String contentType;


    public FileMetadata(UUID uuid, String path, long size) {
        this.uuid = uuid;
        this.path = path;
        this.size = size;
        this.creationDate = Instant.now(); // Set creation date on object creation
        this.lastModifiedDate = creationDate; // Initialize last modified to creation date
    }

    public FileMetadata(UUID uuid, String path, long size, Instant creationDate) {
        this.uuid = uuid;
        this.path = path;
        this.size = size;
        this.creationDate = creationDate;
        this.lastModifiedDate = creationDate; // Important: Initialize in the other constructor as well.
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getPath() {
        return path;
    }

    public long getSize() {
        return size;
    }

    public Instant getCreationDate() {
        return creationDate;
    }

    public Instant getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(Instant lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }


    @Override
    public String toString() {
        return "FileMetadata{" +
                "uuid=" + uuid +
                ", path='" + path + '\'' +
                ", size=" + size +
                ", creationDate=" + creationDate +
                ", lastModifiedDate=" + lastModifiedDate +
                ", contentType='" + contentType + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        FileMetadata that = (FileMetadata) obj;

        if (size != that.size) return false;
        if (!uuid.equals(that.uuid)) return false;
        if (!path.equals(that.path)) return false;
        if (!creationDate.equals(that.creationDate)) return false;
        if (lastModifiedDate != null ? !lastModifiedDate.equals(that.lastModifiedDate) : that.lastModifiedDate != null)
            return false;
        return contentType != null ? contentType.equals(that.contentType) : that.contentType == null;
    }

    @Override
    public int hashCode() {
        int result = uuid.hashCode();
        result = 31 * result + path.hashCode();
        result = 31 * result + (int) (size ^ (size >>> 32));
        result = 31 * result + creationDate.hashCode();
        result = 31 * result + (lastModifiedDate != null ? lastModifiedDate.hashCode() : 0);
        result = 31 * result + (contentType != null ? contentType.hashCode() : 0);
        return result;
    }
}