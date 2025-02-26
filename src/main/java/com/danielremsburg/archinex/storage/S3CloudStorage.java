package com.danielremsburg.archinex.storage;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CopyObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.StorageClass;
import com.danielremsburg.archinex.config.ArchinexConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.UUID;

public class S3CloudStorage implements CloudStorage {

    private static final Logger logger = LoggerFactory.getLogger(S3CloudStorage.class);

    private final AmazonS3 s3Client;
    private final String bucketName;
    private final String archiveBucketName;

    public S3CloudStorage(ArchinexConfig config) {
        // Initialize AmazonS3 client
        this.s3Client = AmazonS3ClientBuilder.standard()
                .withRegion(config.getString("storage.cloud.region")) // Specify region from config
                .build();

        // Get bucket names and other configurations from ArchinexConfig
        this.bucketName = config.getCloudStorageBucket();
        this.archiveBucketName = config.getString("storage.cloud.archiveBucket"); // Optional archive bucket
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
        } catch (AmazonServiceException e) {
            logger.error("Error storing file to S3: {}", e.getMessage(), e);
            throw e;
        } catch (SdkClientException e) {
            logger.error("Error storing file to S3 (client error): {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public byte[] retrieve(UUID uuid) throws IOException {
        try {
            com.amazonaws.services.s3.model.S3Object s3Object = s3Client.getObject(bucketName, uuid.toString());
            try (InputStream inputStream = s3Object.getObjectContent()) {
                return inputStream.readAllBytes();
            }
        } catch (AmazonServiceException e) {
            logger.error("Error retrieving file from S3: {}", e.getMessage(), e);
            throw e;
        } catch (SdkClientException e) {
            logger.error("Error retrieving file from S3 (client error): {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public void delete(UUID uuid) throws IOException {
        try {
            s3Client.deleteObject(bucketName, uuid.toString());
            logger.info("File deleted from S3: {}/{}", bucketName, uuid);
        } catch (AmazonServiceException e) {
            logger.error("Error deleting file from S3: {}", e.getMessage(), e);
            throw e;
        } catch (SdkClientException e) {
            logger.error("Error deleting file from S3 (client error): {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public void archive(UUID uuid) throws IOException {
        try {
            String key = uuid.toString();

            if (archiveBucketName != null && !archiveBucketName.isEmpty()) {
                // Copy to archive bucket and delete from original bucket
                s3Client.copyObject(bucketName, key, archiveBucketName, key);
                s3Client.deleteObject(bucketName, key);
                logger.info("File archived to S3: {}/{}", archiveBucketName, key);
            } else {
                // Change storage class to Glacier in the same bucket (using CopyObjectRequest)
                CopyObjectRequest copyObjectRequest = new CopyObjectRequest(
                        bucketName,
                        key,
                        bucketName,
                        key
                ).withStorageClass(StorageClass.Glacier); // Set storage class

                s3Client.copyObject(copyObjectRequest); // Use CopyObjectRequest
                logger.info("File archived to S3 (same bucket): {}/{} - Storage class changed to Glacier", bucketName, key);
            }
        } catch (AmazonServiceException e) {
            logger.error("Error archiving file to S3: {}", e.getMessage(), e);
            throw e;
        } catch (SdkClientException e) {
            logger.error("Error archiving file to S3 (client error): {}", e.getMessage(), e);
            throw e;
        }
    }
}
