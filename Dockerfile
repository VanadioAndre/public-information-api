ARG REGISTRY_PREFIX=""

FROM ${REGISTRY_PREFIX}eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /build

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
RUN chmod +x ./mvnw

RUN ./mvnw dependency:go-offline

COPY src src
RUN ./mvnw package -DskipTests

RUN mv /build/target/*.jar /build/target/app.jar

FROM ${REGISTRY_PREFIX}eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S javauser && \
    adduser -S -G javauser javauser && \
    apk add --no-cache curl

COPY --from=builder /build/target/app.jar .

RUN chown -R javauser:javauser /app

USER javauser

HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8088/actuator/health || exit 1

EXPOSE 8088

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
