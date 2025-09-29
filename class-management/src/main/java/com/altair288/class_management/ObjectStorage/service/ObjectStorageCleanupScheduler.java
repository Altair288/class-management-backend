package com.altair288.class_management.ObjectStorage.service;

import com.altair288.class_management.ObjectStorage.model.FileObject;
import com.altair288.class_management.ObjectStorage.model.FileStorageConfig;
import com.altair288.class_management.ObjectStorage.repository.FileObjectRepository;
import com.altair288.class_management.ObjectStorage.repository.FileStorageConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 简单的基于 retentionDays 清理任务：
 * 逻辑：对 autoCleanup=true 的配置，删除 createdAt 超过 retentionDays 的且状态=COMPLETED 的文件记录。
 * 当前仅删除数据库记录（与 leave_attachment 解关联会因 FK CASCADE? -> file_object 被引用不应直接删，未来可改 DELETED 标记）。
 * 为安全起见：此处只打标（status=DELETED, deletedAt=now），不物理删除；后续可添加真正的对象存储删除逻辑。
 */
@Component
public class ObjectStorageCleanupScheduler {

    @Autowired private FileStorageConfigRepository configRepo;
    @Autowired private FileObjectRepository fileRepo;

    // 每天 02:30 执行一次
    @Scheduled(cron = "0 30 2 * * *")
    @Transactional
    public void dailyCleanup(){
        List<FileStorageConfig> configs = configRepo.findAll();
        Instant now = Instant.now();
        for(FileStorageConfig cfg : configs){
            if(Boolean.TRUE.equals(cfg.getAutoCleanup()) && cfg.getRetentionDays()!=null && cfg.getRetentionDays() > 0){
                Date threshold = daysAgo(cfg.getRetentionDays());
                // 简单处理：遍历（可在未来添加 Repository 按条件查询）
                List<FileObject> all = fileRepo.findAll();
                for(FileObject fo : all){
                    if(!"COMPLETED".equals(fo.getStatus())) continue;
                    if(!cfg.getId().equals(fo.getStorageConfigId())) continue;
                    if(fo.getCreatedAt()!=null && fo.getCreatedAt().before(threshold)){
                        fo.setStatus("DELETED");
                        fo.setDeletedAt(Date.from(now));
                        fileRepo.save(fo);
                    }
                }
            }
        }
    }

    private Date daysAgo(int days){
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -days);
        return cal.getTime();
    }
}
