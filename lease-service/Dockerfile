# ---- Build stage ----
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copy only this service's pom.xml
COPY pom.xml .

# Create a Maven settings.xml with Alibaba mirror (faster and more reliable)
RUN mkdir -p /root/.m2 && \
    echo '<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0" \
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" \
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 \
                              https://maven.apache.org/xsd/settings-1.0.0.xsd"> \
        <mirrors> \
            <mirror> \
                <id>aliyun</id> \
                <name>Aliyun Maven</name> \
                <url>https://maven.aliyun.com/repository/public</url> \
                <mirrorOf>central</mirrorOf> \
            </mirror> \
        </mirrors> \
    </settings>' > /root/.m2/settings.xml

# Download dependencies with retry on failure
RUN for i in 1 2 3; do \
        echo "Attempt $i to download dependencies..." && \
        mvn dependency:go-offline -B && \
        echo "Download successful!" && \
        break || \
        echo "Download failed, retrying... (attempt $i)" && \
        sleep 5; \
    done

# Copy source and build
COPY src ./src
RUN mvn clean package -DskipTests

# ---- Runtime stage ----
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]