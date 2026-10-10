
# syntax=docker/dockerfile:1.4

# ---------- Build stage ----------
FROM maven:3.9-eclipse-temurin-17-alpine AS build

WORKDIR /build

# Root aggregator pom — installed first (non-recursive) so common + service
# can resolve their parent at build time.
COPY pom.xml /build/pom.xml
RUN --mount=type=cache,target=/root/.m2 \
    cd /build && mvn -B -ntp install -N -Dmaven.test.skip=true

# Common module — built from source inside the container
COPY common /build/common
RUN --mount=type=cache,target=/root/.m2 \
    cd /build/common && mvn -B -ntp clean install -Dmaven.test.skip=true

# Service
COPY maintainance-ticket-service /build/maintainance-ticket-service
RUN --mount=type=cache,target=/root/.m2 \
    cd /build/maintainance-ticket-service && mvn -B -ntp clean package -Dmaven.test.skip=true

# ---------- Runtime stage ----------
FROM eclipse-temurin:17-jre

RUN apt-get update && apt-get install -y --no-install-recommends wget \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY --from=build /build/maintainance-ticket-service/target/*.jar app.jar

EXPOSE 8084

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError"

HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8084/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]