package com.altair288.class_management.ObjectStorage.controller;

import com.altair288.class_management.ObjectStorage.dto.*;
import com.altair288.class_management.ObjectStorage.model.ObjectStorageConnection;
import com.altair288.class_management.ObjectStorage.service.ObjectStorageService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/object-storage")
public class ObjectStorageController {

    @Autowired
    private ObjectStorageService service;

    // ========== Connection Management ==========
    @GetMapping("/connections")
    public List<ConnectionDTO> listConnections(){
        return service.listConnections();
    }

    @PostMapping("/connections")
    public ConnectionDTO saveConnection(@RequestBody ObjectStorageConnection conn){
        return service.saveOrUpdateConnection(conn);
    }

    @PostMapping("/connections/{id}/test")
    public ConnectionDTO testConnection(@PathVariable Long id){
        return service.testConnection(id);
    }

    // ========== Upload Flow ==========
    @PostMapping("/upload/create")
    public CreateUploadResponse createUpload(@Valid @RequestBody CreateUploadRequest req){
        return service.createUpload(req);
    }

    @PostMapping("/upload/confirm")
    public FileObjectDTO confirmUpload(@Valid @RequestBody ConfirmUploadRequest req){
        return service.confirmUpload(req);
    }

    @GetMapping("/business/{type}/{id}/files")
    public List<FileObjectDTO> listBusinessFiles(@PathVariable("type") String type,
                                                 @PathVariable("id") Long id){
        return service.listBusinessFiles(type, id);
    }

    @GetMapping("/files/{id}/download-info")
    public FileObjectDTO getDownloadInfo(@PathVariable Long id){
        return service.getDownloadInfo(id);
    }
}
