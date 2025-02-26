package com.danielremsburg.archinex.metadata;

import com.danielremsburg.archinex.config.ArchinexConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PostgresMetadataStore implements MetadataStore {

    private static final Logger logger = LoggerFactory.getLogger(PostgresMetadataStore.class);

    private final String jdbcUrl;
    private final String username;
    private final String password;

    public PostgresMetadataStore(ArchinexConfig config) {
        this.jdbcUrl = config.getString("postgres.jdbcUrl");
        this.username = config.getString("postgres.username");
        this.password = config.getString("postgres.password");

        if (jdbcUrl == null || jdbcUrl.isEmpty() || username == null || username.isEmpty() || password == null || password.isEmpty()) {
            throw new IllegalArgumentException("PostgreSQL configuration (JDBC URL, username, password) is missing.");
        }

        try {
            createTableIfNotExists();
        } catch (MetadataStoreException e) {
            throw new RuntimeException("Failed to initialize metadata store", e);
        }
    }

    private void createTableIfNotExists() throws MetadataStoreException {
        try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password);
             Statement statement = connection.createStatement()) {

            String createTableSQL = "CREATE TABLE IF NOT EXISTS files (" +
                    "uuid UUID PRIMARY KEY," +
                    "path TEXT NOT NULL," +
                    "size BIGINT NOT NULL," +
                    "creation_date TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP)";
            statement.execute(createTableSQL);
            logger.info("Files table created or already exists.");

        } catch (SQLException e) {
            logger.error("Error creating table: {}", e.getMessage(), e);
            throw new MetadataStoreException("Error creating table: " + e.getMessage(), e);
        }
    }

    @Override
    public void store(FileMetadata metadata) throws MetadataStoreException {
        String insertSQL = "INSERT INTO files (uuid, path, size, creation_date) VALUES (?, ?, ?, ?)";
        try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password);
             PreparedStatement statement = connection.prepareStatement(insertSQL)) {

            statement.setObject(1, metadata.getUuid());
            statement.setString(2, metadata.getPath());
            statement.setLong(3, metadata.getSize());
            statement.setTimestamp(4, Timestamp.from(metadata.getCreationDate()));
            statement.executeUpdate();
            logger.info("Stored metadata for UUID: {}", metadata.getUuid());

        } catch (SQLException e) {
            logger.error("Error storing metadata: {}", e.getMessage(), e);
            throw new MetadataStoreException("Error storing metadata: " + e.getMessage(), e);
        }
    }

    @Override
    public FileMetadata get(UUID uuid) throws MetadataStoreException {
        String selectSQL = "SELECT path, size, creation_date FROM files WHERE uuid = ?";
        try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password);
             PreparedStatement statement = connection.prepareStatement(selectSQL)) {

            statement.setObject(1, uuid);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    String path = resultSet.getString("path");
                    long size = resultSet.getLong("size");
                    Timestamp creationDate = resultSet.getTimestamp("creation_date");
                    return new FileMetadata(uuid, path, size, creationDate.toInstant());
                } else {
                    return null;
                }
            }
        } catch (SQLException e) {
            logger.error("Error getting metadata: {}", e.getMessage(), e);
            throw new MetadataStoreException("Error getting metadata: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(UUID uuid) throws MetadataStoreException {
        String deleteSQL = "DELETE FROM files WHERE uuid = ?";
        try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password);
             PreparedStatement statement = connection.prepareStatement(deleteSQL)) {

            statement.setObject(1, uuid);
            statement.executeUpdate();
            logger.info("Deleted metadata for UUID: {}", uuid);

        } catch (SQLException e) {
            logger.error("Error deleting metadata: {}", e.getMessage(), e);
            throw new MetadataStoreException("Error deleting metadata: " + e.getMessage(), e);
        }
    }

    @Override
    public List<FileMetadata> getAllFiles() throws MetadataStoreException {
        List<FileMetadata> files = new ArrayList<>();
        String selectSQL = "SELECT uuid, path, size, creation_date FROM files";
        try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(selectSQL)) {

            while (resultSet.next()) {
                UUID uuid = (UUID) resultSet.getObject("uuid");
                String path = resultSet.getString("path");
                long size = resultSet.getLong("size");
                Timestamp creationDate = resultSet.getTimestamp("creation_date");
                files.add(new FileMetadata(uuid, path, size, creationDate.toInstant()));
            }
            return files;

        } catch (SQLException e) {
            logger.error("Error getting all files: {}", e.getMessage(), e);
            throw new MetadataStoreException("Error getting all files: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteAll() throws MetadataStoreException {
        String deleteSQL = "DELETE FROM files";
        try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password);
             Statement statement = connection.createStatement()) {

            statement.executeUpdate(deleteSQL);
            logger.info("Deleted all metadata.");

        } catch (SQLException e) {
            logger.error("Error deleting all metadata: {}", e.getMessage(), e);
            throw new MetadataStoreException("Error deleting all metadata: " + e.getMessage(), e);
        }
    }

    @Override
    public void update(FileMetadata metadata) throws MetadataStoreException {
        String updateSQL = "UPDATE files SET path = ?, size = ? WHERE uuid = ?";
        try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password);
             PreparedStatement statement = connection.prepareStatement(updateSQL)) {

            statement.setString(1, metadata.getPath());
            statement.setLong(2, metadata.getSize());
            statement.setObject(3, metadata.getUuid());
            statement.executeUpdate();
            logger.info("Updated metadata for UUID: {}", metadata.getUuid());

        } catch (SQLException e) {
            logger.error("Error updating metadata: {}", e.getMessage(), e);
            throw new MetadataStoreException("Error updating metadata: " + e.getMessage(), e);
        }
    }
}
