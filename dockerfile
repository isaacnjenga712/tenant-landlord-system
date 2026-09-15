# Stage 1: Build the JAR inside the container
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /build

# Copy only necessary files
COPY pom.xml .
COPY common common/
COPY notification-engine notification-engine/

# Remove missing modules from parent pom.xml to avoid build errors
RUN sed -i '/<module>lease-service<\/module>/d' pom.xml && \
    sed -i '/<module>property-management-service<\/module>/d' pom.xml && \
    sed -i '/<module>maintainance-ticket-service<\/module>/d' pom.xml && \
    sed -i '/<module>payment-service<\/module>/d' pom.xml

# Build only the notification-engine module (plus common)
RUN mvn clean package -pl notification-engine -am -DskipTests

# Stage 2: Runtime image (lightweight Alpine)
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy JAR from builder
COPY --from=builder /build/notification-engine/target/notification-engine-*.jar app.jar

# Install wget for healthcheck (Alpine uses apk)
RUN apk add --no-cache wget

# Expose port
EXPOSE 8080

# Health check (requires Spring Boot Actuator)
HEALTHCHECK --interval=30s --timeout=3s --start-period=10s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Run the application
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]_OPTS -jar app.jar"]