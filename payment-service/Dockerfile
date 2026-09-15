# ---- Build stage ----
	# ---- Build stage ----
	FROM maven:3.9.6-eclipse-temurin-17 AS build

	WORKDIR /app

	# Copy only the pom.xml first to cache dependencies
	COPY pom.xml .

	# Add Alibaba mirror for faster dependency downloads (optional but recommended)
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

	# Download dependencies with retry (3 attempts)
	RUN for i in 1 2 3; do \
	        echo "Attempt $i to download dependencies..." && \
	        mvn dependency:go-offline -B && \
	        echo "Download successful!" && \
	        break || \
	        echo "Download failed, retrying... (attempt $i)" && \
	        sleep 5; \
	    done

	# Copy the rest of the source code
	COPY src ./src

	# Build the application (skip tests for speed)
	RUN mvn clean package -DskipTests

	# ---- Runtime stage ----
	FROM eclipse-temurin:17-jre-alpine

	WORKDIR /app

	# Copy the built JAR from the build stage
	COPY --from=build /app/target/*.jar app.jar

	# Expose the port the service runs on (configured via environment variable)
	EXPOSE 8080

	# Run the JAR
	ENTRYPOINT ["java", "-jar", "app.jar"]