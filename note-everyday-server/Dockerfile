FROM paas-fe30816e.ecis.huadong-7.cmecloud.cn/mbo/base/maven:3.6.3-openjdk-17-reach-nexus AS builder
WORKDIR /app
COPY . .
RUN mvn clean package


FROM paas-fe30816e.ecis.huadong-7.cmecloud.cn/mbo/base/openjdk:17-jdk-slim
ENV SPRING_PROFILES_ACTIVE=test
ENV ASYNC_LOG_OUTPUT=console
ENV SPRING_CLOUD_NACOS_DISCOVERY_SERVER_ADDR=10.170.136.88:8848
ENV SPRING_CLOUD_NACOS_CONFIG_SERVER_ADDR=10.170.136.88:8848
ENV JAVA_OPTS="-Xms1g -Xmx1g"

WORKDIR /app
COPY --from=builder /app/target/mb-terminal-server.jar /app/mb-terminal-server.jar
EXPOSE 8081
ENTRYPOINT java \
            ${JAVA_OPTS} \
            -Dspring.profiles.active=${SPRING_PROFILES_ACTIVE} \
            -Dasync.log.output=${ASYNC_LOG_OUTPUT} \
            -Dspring.cloud.nacos.discovery.server-addr=${SPRING_CLOUD_NACOS_DISCOVERY_SERVER_ADDR} \
            -Dspring.cloud.nacos.config.server-addr=${SPRING_CLOUD_NACOS_CONFIG_SERVER_ADDR} \
            -Dspring.cloud.nacos.discovery.namespace=${SPRING_CLOUD_NACOS_DISCOVERY_NAMESPACE} \
            -Dspring.cloud.nacos.config.namespace=${SPRING_CLOUD_NACOS_CONFIG_NAMESPACE} \
            -Dspring.cloud.nacos.config.group=${SPRING_CLOUD_NACOS_CONFIG_GROUP} \
            -Dspring.cloud.nacos.discovery.group=${SPRING_CLOUD_NACOS_DISCOVERY_GROUP} \
            -Dspring.application.name=${SPRING_APPLICATION_NAME} \
            -jar mb-terminal-server.jar