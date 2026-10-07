# syntax=docker/dockerfile:1.4

# ---------- Build stage ----------
FROM maven:3.9-eclipse-temurin-17-alpine AS build

WORKDIR /build

# 1. Maven settings (mirrors, credentials)
COPY auth-service/settings.xml /root/.m2/settings.xml

# 2. Root aggregator pom — install FIRST (non-recursive) so common + auth-service
#    can resolve their parent at build time. Without this, Maven fails with
#    "Non-resolvable parent POM: com.platform:tenant-landlord-system".
COPY pom.xml /build/pom.xml
RUN --mount=type=cache,target=/root/.m2 \
    cd /build && mvn -B -ntp install -N -Dmaven.test.skip=true

# 3. Install common module (jar + pom into local .m2)
COPY common /build/common
RUN --mount=type=cache,target=/root/.m2 \
    cd /build/common && mvn -B -ntp clean install -Dmaven.test.skip=true

# 4. Build auth-service
COPY auth-service /build/auth-service
RUN --mount=type=cache,target=/root/.m2 \
    cd /build/auth-service && mvn -B -ntp clean package -Dmaven.test.skip=true

# ---------- Runtime stage ----------
FROM eclipse-temurin:17-jre

RUN apt-get update && apt-get install -y --no-install-recommends wget \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY --from=build /build/auth-service/target/*.jar app.jar

EXPOSE 8080

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError"

HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]