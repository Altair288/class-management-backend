# ===========================
# Backend Multi-stage Build
# Java 23 + Spring Boot 3.4.x
# ===========================

# ---- Build Stage ----
FROM docker.altair288.eu.org/maven:3.9-eclipse-temurin-23 AS build
WORKDIR /app

# 使用阿里云或官方中央仓库可选: 在 settings.xml 中配置镜像以加速
# 仅复制必要的 pom 进行依赖分层缓存
COPY pom.xml .
# 预下载依赖 (跳过测试)
RUN --mount=type=cache,target=/root/.m2 mvn -B -q dependency:go-offline

# 复制源码
COPY src ./src

# 编译 & 打包（不含测试，可根据需要开启）
RUN --mount=type=cache,target=/root/.m2 mvn -B -q package -DskipTests

# ---- Runtime Stage ----
FROM docker.altair288.eu.org/eclipse-temurin:23-jre AS runtime
WORKDIR /app

# 
# 可以通过构建参数传递版本 (可选)
ARG JAR_NAME=class-management-0.0.1-SNAPSHOT.jar

## 安装 curl 供 healthcheck 使用（基础镜像若是 debian/ubuntu 系）
USER root
RUN apt-get update 2>/dev/null || true && \
	(apt-get install -y --no-install-recommends curl || true) && \
	rm -rf /var/lib/apt/lists/* || true && \
	(command -v curl || echo 'curl not installed')

# 复制 jar
COPY --from=build /app/target/${JAR_NAME} app.jar

# 暴露端口
EXPOSE 8080

# 健康检查 (可在 compose 中定义)
# HEALTHCHECK --interval=30s --timeout=3s CMD wget -qO- http://localhost:8080/actuator/health || exit 1

# 默认 JVM 参数可通过 JAVA_OPTS 环境变量覆盖
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
