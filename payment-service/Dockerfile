
# syntax=docker/dockerfile:1.4

FROM maven:3.9-eclipse-temurin-17-alpine AS build



WORKDIR /build



COPY settings.xml /root/.m2/settings.xml



COPY libs/ /tmp/libs/



COPY pom.xml .



COPY src ./src



RUN --mount=type=cache,target=/root/.m2 mvn -B -ntp install:install-file -Dfile=/tmp/libs/common-1.0.0-SNAPSHOT.jar -DgroupId=com.platform -DartifactId=common -Dversion=1.0.0-SNAPSHOT -Dpackaging=jar -DgeneratePom=true && mvn -B -ntp clean package -Dmaven.test.skip=true



FROM eclipse-temurin:17-jre



RUN apt-get update && apt-get install -y --no-install-recommends wget && rm -rf /var/lib/apt/lists/*



WORKDIR /app

COPY --from=build /build/target/*.jar app.jar

EXPOSE 8086

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError"

HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 CMD wget --no-verbose --tries=1 --spider http://localhost:8086/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "exec java -jar app.jar"]

