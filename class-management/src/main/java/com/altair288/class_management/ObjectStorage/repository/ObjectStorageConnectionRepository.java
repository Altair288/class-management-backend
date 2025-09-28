package com.altair288.class_management.ObjectStorage.repository;

import com.altair288.class_management.ObjectStorage.model.ObjectStorageConnection;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ObjectStorageConnectionRepository extends JpaRepository<ObjectStorageConnection, Long> {
    Optional<ObjectStorageConnection> findByName(String name);
}
