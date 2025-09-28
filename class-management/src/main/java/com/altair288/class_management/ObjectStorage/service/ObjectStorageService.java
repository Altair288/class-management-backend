package com.altair288.class_management.ObjectStorage.service;

import com.altair288.class_management.ObjectStorage.dto.*;
import com.altair288.class_management.ObjectStorage.model.*;
import com.altair288.class_management.ObjectStorage.repository.*;
import com.altair288.class_management.model.LeaveAttachment;
import com.altair288.class_management.model.LeaveRequest;
import com.altair288.class_management.model.User;
import com.altair288.class_management.repository.LeaveAttachmentRepository;
import com.altair288.class_management.repository.LeaveRequestRepository;
import com.altair288.class_management.repository.UserRepository;
import io.minio.*;
import io.minio.http.Method;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.Base64;
import java.time.*;
import java.time.format.DateTimeFormatter;

@Service
public class ObjectStorageService {

    @Autowired private ObjectStorageConnectionRepository connectionRepo;
    @Autowired private FileStorageConfigRepository configRepo;
    @Autowired private FileObjectRepository fileRepo;
    @Autowired private LeaveAttachmentRepository leaveAttachmentRepository;
    @Autowired private LeaveRequestRepository leaveRequestRepository;
    @Autowired private UserRepository userRepository;

    // ============= Connection =============
    public List<ConnectionDTO> listConnections(){
        List<ObjectStorageConnection> list = connectionRepo.findAll();
        List<ConnectionDTO> out = new ArrayList<>();
        for(ObjectStorageConnection c: list){
            ConnectionDTO dto = new ConnectionDTO();
            dto.setId(c.getId());
            dto.setName(c.getName());
            dto.setProvider(c.getProvider());
            dto.setEndpointUrl(c.getEndpointUrl());
            dto.setSecureFlag(c.getSecureFlag());
            dto.setPathStyleAccess(c.getPathStyleAccess());
            dto.setDefaultPresignExpireSeconds(c.getDefaultPresignExpireSeconds());
            dto.setActive(c.getActive());
            dto.setLastTestStatus(c.getLastTestStatus());
            dto.setLastTestError(c.getLastTestError());
            out.add(dto);
        }
        return out;
    }

    @Transactional
    public ConnectionDTO saveOrUpdateConnection(ObjectStorageConnection conn){
        if(!StringUtils.hasText(conn.getProvider())) conn.setProvider("MINIO");
        // 加密凭证（幂等：已加密则跳过）
        if(StringUtils.hasText(conn.getAccessKeyEncrypted()))
            conn.setAccessKeyEncrypted(encryptIfNeeded(conn.getAccessKeyEncrypted()));
        if(StringUtils.hasText(conn.getSecretKeyEncrypted()))
            conn.setSecretKeyEncrypted(encryptIfNeeded(conn.getSecretKeyEncrypted()));
        ObjectStorageConnection saved = connectionRepo.save(conn);
        ConnectionDTO dto = new ConnectionDTO();
        dto.setId(saved.getId());
        dto.setName(saved.getName());
        dto.setProvider(saved.getProvider());
        dto.setEndpointUrl(saved.getEndpointUrl());
        dto.setSecureFlag(saved.getSecureFlag());
        dto.setPathStyleAccess(saved.getPathStyleAccess());
        dto.setDefaultPresignExpireSeconds(saved.getDefaultPresignExpireSeconds());
        dto.setActive(saved.getActive());
        dto.setLastTestStatus(saved.getLastTestStatus());
        dto.setLastTestError(saved.getLastTestError());
        return dto;
    }

    public ConnectionDTO testConnection(Long id) {
        ObjectStorageConnection conn = connectionRepo.findById(id).orElseThrow(() -> new RuntimeException("连接不存在"));
        try {
            MinioClient client = buildClient(conn);
            client.listBuckets(); // 简单探活
            conn.setLastTestStatus("SUCCESS");
            conn.setLastTestError(null);
        } catch (Exception ex){
            conn.setLastTestStatus("FAIL");
            conn.setLastTestError(truncate(ex.getMessage()));
        }
        conn.setLastTestTime(new Date());
        connectionRepo.save(conn);
        return listConnections().stream().filter(c-> Objects.equals(c.getId(), id)).findFirst().orElseThrow();
    }

    // ============= Upload Flow =============
    @Transactional
    public CreateUploadResponse createUpload(CreateUploadRequest req){
        Integer currentUserId = resolveCurrentUserId();
        FileStorageConfig cfg = configRepo.findByBucketPurpose(req.getBucketPurpose())
                .orElseThrow(() -> new RuntimeException("未配置对应用途的存储桶:"+req.getBucketPurpose()));
        ObjectStorageConnection conn = connectionRepo.findById(cfg.getConnectionId().longValue())
                .orElseThrow(() -> new RuntimeException("关联连接不存在"));
        if(Boolean.FALSE.equals(cfg.getEnabled())) throw new RuntimeException("存储配置已禁用");

        String ext = extractExt(req.getOriginalFilename());
        // 校验扩展名
        validateExtension(ext, cfg);
        String objectKey = buildObjectKey(cfg.getBasePath(), ext);

        FileObject fo = new FileObject();
        fo.setStorageConfigId(cfg.getId());
        fo.setBucketName(cfg.getBucketName());
        fo.setObjectKey(objectKey);
        fo.setOriginalFilename(req.getOriginalFilename());
        fo.setExt(ext);
        fo.setUploaderUserId(currentUserId);
        fo.setBusinessRefType(req.getBusinessRefType());
        fo.setBusinessRefId(req.getBusinessRefId());
        fo.setStatus("UPLOADING");
        fo = fileRepo.save(fo);

        int expire = conn.getDefaultPresignExpireSeconds()==null?600:conn.getDefaultPresignExpireSeconds();
        String presignUrl = presignPut(conn, cfg.getBucketName(), objectKey, expire);

        CreateUploadResponse resp = new CreateUploadResponse();
        resp.setFileObjectId(fo.getId());
        resp.setBucketName(cfg.getBucketName());
        resp.setObjectKey(objectKey);
        resp.setPresignUrl(presignUrl);
        resp.setExpireSeconds(expire);
        return resp;
    }

    @Transactional
    public FileObjectDTO confirmUpload(ConfirmUploadRequest req){
        FileObject fo = fileRepo.findById(req.getFileObjectId())
                .orElseThrow(() -> new RuntimeException("文件记录不存在"));
        if(!"UPLOADING".equals(fo.getStatus())){
            throw new RuntimeException("当前状态不允许确认:"+fo.getStatus());
        }
        if(req.getSizeBytes()!=null) fo.setSizeBytes(req.getSizeBytes());
        if(req.getMimeType()!=null) fo.setMimeType(req.getMimeType());

        // 校验 mime / size
        FileStorageConfig cfg = configRepo.findById(fo.getStorageConfigId())
                .orElseThrow(() -> new RuntimeException("存储配置不存在"));
        if(req.getMimeType()!=null) validateMimeType(req.getMimeType(), cfg);
        if(req.getSizeBytes()!=null && cfg.getMaxFileSize()!=null && req.getSizeBytes() > cfg.getMaxFileSize()){
            throw new RuntimeException("文件大小超过允许限制:"+cfg.getMaxFileSize());
        }
        fo.setStatus("COMPLETED");
        fo.setCompletedAt(new Date());
        fileRepo.save(fo);

        // 业务挂接（仅针对请假 LEAVE_REQUEST）
        if("LEAVE_REQUEST".equalsIgnoreCase(fo.getBusinessRefType()) && fo.getBusinessRefId()!=null){
            Integer leaveId = fo.getBusinessRefId().intValue();
            LeaveRequest lr = leaveRequestRepository.findById(leaveId)
                    .orElse(null); // 不存在则忽略关联
            if(lr != null){
                LeaveAttachment att = new LeaveAttachment();
                att.setLeaveRequestId(leaveId);
                att.setFileObjectId(fo.getId());
                att.setCreatedAt(new Date());
                att.setCreatedBy(fo.getUploaderUserId());
                leaveAttachmentRepository.save(att);
                Integer cnt = lr.getAttachmentCount()==null?0:lr.getAttachmentCount();
                lr.setAttachmentCount(cnt + 1);
                leaveRequestRepository.save(lr);
            }
        }
        return toDTO(fo, null);
    }

    public List<FileObjectDTO> listBusinessFiles(String refType, Long refId){
        List<FileObject> list = fileRepo.findByBusinessRefTypeAndBusinessRefIdAndStatus(refType, refId, "COMPLETED");
        List<FileObjectDTO> out = new ArrayList<>();
        for(FileObject fo : list){
            out.add(toDTO(fo, null));
        }
        return out;
    }

    public FileObjectDTO getDownloadInfo(Long id){
        FileObject fo = fileRepo.findById(id).orElseThrow(() -> new RuntimeException("文件不存在"));
        FileStorageConfig cfg = configRepo.findById(fo.getStorageConfigId())
                .orElseThrow(() -> new RuntimeException("存储配置不存在"));
        ObjectStorageConnection conn = connectionRepo.findById(cfg.getConnectionId().longValue())
                .orElseThrow(() -> new RuntimeException("连接不存在"));
        int expire = conn.getDefaultPresignExpireSeconds()==null?600:conn.getDefaultPresignExpireSeconds();
        String url = presignGet(conn, fo.getBucketName(), fo.getObjectKey(), expire);
        return toDTO(fo, url);
    }

    // ============= Helper =============
    private FileObjectDTO toDTO(FileObject fo, String downloadUrl){
        FileObjectDTO dto = new FileObjectDTO();
        dto.setId(fo.getId());
        dto.setBucketName(fo.getBucketName());
        dto.setObjectKey(fo.getObjectKey());
        dto.setOriginalFilename(fo.getOriginalFilename());
        dto.setExt(fo.getExt());
        dto.setMimeType(fo.getMimeType());
        dto.setSizeBytes(fo.getSizeBytes());
        dto.setStatus(fo.getStatus());
        dto.setUploaderUserId(fo.getUploaderUserId());
        dto.setBusinessRefType(fo.getBusinessRefType());
        dto.setBusinessRefId(fo.getBusinessRefId());
        dto.setCreatedAt(fo.getCreatedAt());
        dto.setCompletedAt(fo.getCompletedAt());
        dto.setDownloadUrl(downloadUrl);
        return dto;
    }

    private String buildObjectKey(String basePath, String ext){
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String random = UUID.randomUUID().toString().replaceAll("-", "");
        StringBuilder sb = new StringBuilder();
        if(StringUtils.hasText(basePath)) sb.append(basePath).append('/');
        sb.append(datePath).append('/').append(random);
        if(StringUtils.hasText(ext)) sb.append('.').append(ext.toLowerCase());
        return sb.toString();
    }

    private String extractExt(String filename){
        if(!StringUtils.hasText(filename) || !filename.contains(".")) return "";
        String ext = filename.substring(filename.lastIndexOf('.')+1);
        return ext.toLowerCase();
    }

    private MinioClient buildClient(ObjectStorageConnection conn){
        return MinioClient.builder()
                .endpoint(conn.getEndpointUrl())
                .credentials(decrypt(conn.getAccessKeyEncrypted()), decrypt(conn.getSecretKeyEncrypted()))
                .build();
    }

    private String presignPut(ObjectStorageConnection conn, String bucket, String objectKey, int expire){
        try {
            MinioClient client = buildClient(conn);
            return client.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .method(Method.PUT)
                    .bucket(bucket)
                    .object(objectKey)
                    .expiry(expire)
                    .build());
        } catch (Exception e){
            throw new RuntimeException("生成上传预签名失败:"+e.getMessage(), e);
        }
    }

    private String presignGet(ObjectStorageConnection conn, String bucket, String objectKey, int expire){
        try {
            MinioClient client = buildClient(conn);
            return client.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(bucket)
                    .object(objectKey)
                    .expiry(expire)
                    .build());
        } catch (Exception e){
            throw new RuntimeException("生成下载预签名失败:"+e.getMessage(), e);
        }
    }

    // ============= Credential Encryption Helpers =============
    private String encryptIfNeeded(String plain){
        if(plain == null) return null;
        if(plain.startsWith("ENC:")) return plain; // already encrypted
        try {
            String b64 = Base64.getEncoder().encodeToString(plain.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return "ENC:"+b64;
        } catch (Exception e){
            throw new RuntimeException("加密失败", e);
        }
    }
    private String decrypt(String cipher){
        if(cipher == null) return null;
        if(!cipher.startsWith("ENC:")) return cipher; // plain fallback
        String b64 = cipher.substring(4);
        try {
            byte[] data = Base64.getDecoder().decode(b64);
            return new String(data, java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e){
            throw new RuntimeException("解密失败", e);
        }
    }

    // ============= Validation Helpers =============
    private void validateExtension(String ext, FileStorageConfig cfg){
        if(!StringUtils.hasText(cfg.getAllowedExtensions())) return; // 未配置不校验
        Set<String> allowed = parseJsonArray(cfg.getAllowedExtensions());
        if(!allowed.isEmpty() && !allowed.contains(ext.toLowerCase())){
            throw new RuntimeException("文件扩展不允许: "+ext);
        }
    }
    private void validateMimeType(String mime, FileStorageConfig cfg){
        if(!StringUtils.hasText(cfg.getAllowedMimeTypes())) return;
        Set<String> allowed = parseJsonArray(cfg.getAllowedMimeTypes());
        if(!allowed.isEmpty() && !allowed.contains(mime.toLowerCase())){
            throw new RuntimeException("MIME 类型不允许: "+mime);
        }
    }
    private Set<String> parseJsonArray(String json){
        try {
            String s = json.trim();
            if(s.startsWith("[") && s.endsWith("]")) s = s.substring(1, s.length()-1);
            if(!StringUtils.hasText(s)) return Collections.emptySet();
            String[] parts = s.split(",");
        return Arrays.stream(parts)
            .map(p -> p.replaceAll("^[\\\\\"'\\n\\r\\t ]+", ""))
            .map(p -> p.replaceAll("[\\\\\"'\\n\\r\\t ]+$", ""))
            .map(String::toLowerCase)
            .filter(StringUtils::hasText)
            .collect(Collectors.toSet());
        } catch (Exception e){
            return Collections.emptySet();
        }
    }

    // ============= Security Helper =============
    private Integer resolveCurrentUserId(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if(auth == null || auth.getName()==null) return null; // 允许匿名为 null
        var userOpt = userRepository.findByUsernameOrIdentityNo(auth.getName());
        return userOpt.map(User::getId).orElse(null);
    }
    private String truncate(String s){ if(s==null) return null; return s.length()>300? s.substring(0,300): s; }
}
