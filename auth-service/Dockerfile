# syntax=docker/dockerfile:1.4

FROM maven:3.9-eclipse-temurin-17-alpine AS build



WORKDIR /build



COPY settings.xml /root/.m2/settings.xml



COPY pom.xml .

RUN --mount=type=cache,target=/root/.m2 mvn -B -ntp dependency:go-offline -Dmaven.test.skip=true || true



COPY src ./src

RUN --mount=type=cache,target=/root/.m2 mvn -B -ntp clean package -Dmaven.test.skip=true



FROM eclipse-temurin:17-jre



RUN apt-get update && apt-get install -y --no-install-recommends wget && rm -rf /var/lib/apt/lists/*



WORKDIR /app

COPY --from=build /build/target/*.jar app.jar

EXPOSE 8080

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError"

HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]

