# ============================================================
# Stage 1: Build the JAR using Maven
# ============================================================
FROM maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /app

# Copy the entire project (parent POM + modules)
COPY pom.xml .
COPY common common/
COPY lease-service lease-service/

# ============================================================
# FIX: Remove (comment out) other modules from the POM
# ============================================================
RUN sed -i 's/<module>property-management-service<\/module>/<!-- <module>property-management-service<\/module> -->/g' pom.xml
RUN sed -i 's/<module>billing-service<\/module>/<!-- <module>billing-service<\/module> -->/g' pom.xml
RUN sed -i 's/<module>maintenance-service<\/module>/<!-- <module>maintenance-service<\/module> -->/g' pom.xml

# Build only the lease-service module (skip tests)
RUN mvn clean package -pl lease-service -am -DskipTests

# ============================================================
# Stage 2: Create the final runtime image
# ============================================================
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

COPY --from=build /app/lease-service/target/*.jar app.jar

EXPOSE 8081

HEALTHCHECK --interval=30s --timeout=3s --start-period=10s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost:8081/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]