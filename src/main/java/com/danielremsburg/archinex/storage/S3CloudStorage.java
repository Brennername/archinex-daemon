package com.danielremsburg.archinex.storage;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.UUID;
import com.danielremsburg.archinex.config.ArchinexConfig;

public class S3CloudStorage implements CloudStorage {

    private static final Logger logger = LoggerFactory.getLogger(S3CloudStorage.class);

    private final AmazonS3 s3Client;
    private final String bucketName;

    public S3CloudStorage(ArchinexConfig config) {
        String region = config.getString("storage.cloud.region");
        this.bucketName = config.getString("storage.cloud.bucket");

        this.s3Client = AmazonS3ClientBuilder.standard()
                .withRegion(region)
                .build();
    }

    @Override
    public void store(UUID uuid, byte[] data, Map<String, String> metadata) throws IOException {
        try (InputStream inputStream = new ByteArrayInputStream(data)) {
            ObjectMetadata s3Metadata = new ObjectMetadata();
            if (metadata != null) {
                s3Metadata.setUserMetadata(metadata);
            }
            s3Client.putObject(bucketName, uuid.toString(), inputStream, s3Metadata);
            logger.info("File stored to S3: {}/{}", bucketName, uuid);
        }
    }

    @Override
    public byte[] retrieve(UUID uuid) throws IOException {
        S3Object s3Object = s3Client.getObject(bucketName, uuid.toString());
        try (InputStream inputStream = s3Object.getObjectContent()) {
            return inputStream.readAllBytes();
        }
    }

    @Override
    public void delete(UUID uuid) throws IOException {
        s3Client.deleteObject(bucketName, uuid.toString());
        logger.info("File deleted from S3: {}/{}", bucketName, uuid);
    }

    @Override
    public void archive(UUID uuid) throws IOException {
        String key = uuid.toString();
        s3Client.copyObject(bucketName, key, bucketName, "archive/" + key); // Archiving logic (moving to an "archive" folder)
        logger.info("File archived in S3: {}/{}", bucketName, key);
    }
}
