package com.altair288.class_management.ObjectStorage.repository;

import com.altair288.class_management.ObjectStorage.model.FileObject;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FileObjectRepository extends JpaRepository<FileObject, Long> {
    List<FileObject> findByBusinessRefTypeAndBusinessRefIdAndStatus(String businessRefType, Long businessRefId, String status);
}
