package com.altair288.class_management.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;

/**
 * 生产环境可选的初始化：默认什么都不做。
 * 如果日后需要一次性写入某些基础非结构性数据（且不适合放入 Flyway），
 * 可以设置环境变量 PROD_SEED_ENABLED=true 开启。务必保持幂等。
 */
@Component
@Profile("prod")
public class TestDataInitializerProd {
    private static final Logger log = LoggerFactory.getLogger(TestDataInitializerProd.class);

    @Value("${PROD_SEED_ENABLED:false}")
    private boolean prodSeedEnabled;

    @PostConstruct
    public void maybeInit() {
        if (!prodSeedEnabled) {
            log.info("[prod] 未开启 PROD_SEED_ENABLED，跳过生产数据初始化");
            return;
        }
        // 幂等逻辑占位：
        log.info("[prod] 生产环境数据初始化开始 (当前为空实现，可按需扩展)");
    }
}
