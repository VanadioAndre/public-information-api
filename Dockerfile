# Stage 1: Build stage
FROM eclipse-temurin:21-jdk-jammy AS builder
WORKDIR /build
# Copy only the build files first to cache dependencies
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
# Cache dependencies
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline

# Copy source code
COPY src src
# Build the application
RUN ./mvnw package -DskipTests

# Stage 2: Runtime stage
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Create a non-root user
RUN addgroup --system javauser && adduser --system --group javauser
USER javauser

# Copy the jar from builder stage
COPY --from=builder /build/target/*.jar app.jar

# Configure health check
HEALTHCHECK --interval=30s --timeout=3s \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Use environment variables for configuration (maxHeapSize, minHeapSize)
ENV JAVA_OPTS="-Xmx512m -Xms256m"
    #SPRING_PROFILES_ACTIVE="prod"

# Expose the application port
EXPOSE 8080

# Run with proper Java options
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]