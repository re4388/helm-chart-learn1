FROM openjdk:22-jdk-slim

WORKDIR /app

# Install curl for health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Create OpenTelemetry directory
RUN mkdir -p /opt/opentelemetry

# Download OpenTelemetry Java Agent
ADD https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/latest/download/opentelemetry-javaagent.jar /opt/opentelemetry/opentelemetry-javaagent.jar
RUN chmod 644 /opt/opentelemetry/opentelemetry-javaagent.jar

# Copy the jar file
COPY target/demo-app-*.jar app.jar

# Create log directory
RUN mkdir -p /var/log && chmod 755 /var/log

# 創建日誌目錄
RUN mkdir -p /app/logs && chmod 755 /app/logs

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run the application with OpenTelemetry Java Agent
ENTRYPOINT ["java", "-javaagent:/opt/opentelemetry/opentelemetry-javaagent.jar", "-jar", "app.jar"]