package com.altair288.class_management.ObjectStorage.controller;

import com.altair288.class_management.ObjectStorage.dto.*;
import com.altair288.class_management.ObjectStorage.model.ObjectStorageConnection;
import com.altair288.class_management.ObjectStorage.dto.BusinessPurposeInfo;
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

    @DeleteMapping("/connections/{id}")
    public void deleteConnection(@PathVariable Long id){
        service.deleteConnection(id);
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

    // ========== Storage Config Management ==========
    @GetMapping("/storage-configs")
    public List<FileStorageConfigDTO> listStorageConfigs(){
        return service.listStorageConfigs();
    }

    @GetMapping("/storage-configs/{id}")
    public FileStorageConfigDTO getStorageConfig(@PathVariable Integer id){
        return service.getStorageConfig(id);
    }

    @PostMapping("/storage-configs")
    public FileStorageConfigDTO saveStorageConfig(@Valid @RequestBody SaveFileStorageConfigRequest req){
        return service.saveStorageConfig(req);
    }

    @PostMapping("/storage-configs/{id}/enable")
    public FileStorageConfigDTO enableStorageConfig(@PathVariable Integer id, @RequestParam("enabled") boolean enabled){
        return service.enableStorageConfig(id, enabled);
    }

    @DeleteMapping("/storage-configs/{id}")
    public void deleteStorageConfig(@PathVariable Integer id){
        service.deleteStorageConfig(id);
    }

    // ========== Business Purposes (static suggestions) ==========
    @GetMapping("/purposes")
    public List<BusinessPurposeInfo> listBusinessPurposes(){
        // 目前静态返回，可后续改为数据库 / 配置化
        List<BusinessPurposeInfo> list = new ArrayList<>();
        list.add(new BusinessPurposeInfo("LEAVE_ATTACHMENT", "请假附件", "学生请假申请上传的证明或补充材料", "LEAVE", true));
        list.add(new BusinessPurposeInfo("USER_AVATAR", "用户头像", "平台用户个人头像文件", "USER", true));
        list.add(new BusinessPurposeInfo("NOTICE_ATTACHMENT", "通知附件", "公告/通知的附件文件", "NOTICE", false));
        list.add(new BusinessPurposeInfo("COMMON_TEMP", "临时文件", "通用临时占位文件（可定期清理）", "COMMON", false));
        return list;
    }
}
