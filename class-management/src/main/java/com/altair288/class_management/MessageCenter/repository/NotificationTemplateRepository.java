package com.altair288.class_management.MessageCenter.repository;

import com.altair288.class_management.MessageCenter.model.NotificationTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate, Long> {

    @Query("select t from NotificationTemplate t where t.code = :code and (t.channel is null or t.channel = :channel) and t.status = 'ACTIVE' order by t.channel desc, t.version desc")
    java.util.List<NotificationTemplate> findActiveByCodeAndChannel(String code, String channel);

    default Optional<NotificationTemplate> pickActive(String code, String channel) {
        var list = findActiveByCodeAndChannel(code, channel);
        return list == null || list.isEmpty()? Optional.empty() : Optional.of(list.get(0));
    }

    @Query("select t from NotificationTemplate t where t.code = :code and t.status='ACTIVE' order by t.version desc")
    java.util.List<NotificationTemplate> findAllActiveByCode(String code);

    default Optional<NotificationTemplate> findLatestActiveByCode(String code) {
        var list = findAllActiveByCode(code);
        return list == null || list.isEmpty()? Optional.empty(): Optional.of(list.get(0));
    }
}
