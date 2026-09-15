FROM maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /app

# Install root parent pom
COPY pom.xml /tmp/root-pom.xml
RUN cd /tmp && \
    mvn install:install-file -Dfile=root-pom.xml \
        -DgroupId=com.platform \
        -DartifactId=tenant-landlord-system \
        -Dversion=1.0.0-SNAPSHOT \
        -Dpackaging=pom

# Install common module
COPY common /tmp/common
RUN cd /tmp/common && mvn clean install -DskipTests

# Copy service source
COPY property-management-service/pom.xml .
COPY property-management-service/src ./src

# Build with retry
RUN for i in 1 2 3; do \
        echo "Attempt $i to build property-management-service..." && \
        mvn clean package -DskipTests && \
        echo "Build successful!" && \
        break || \
        echo "Build failed, retrying... (attempt $i)" && \
        sleep 5; \
    done

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]