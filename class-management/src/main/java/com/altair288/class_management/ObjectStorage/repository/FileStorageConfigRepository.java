package com.altair288.class_management.ObjectStorage.repository;

import com.altair288.class_management.ObjectStorage.model.FileStorageConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface FileStorageConfigRepository extends JpaRepository<FileStorageConfig, Integer> {
    Optional<FileStorageConfig> findByBucketPurpose(String bucketPurpose);
    Optional<FileStorageConfig> findByBucketName(String bucketName);
}
