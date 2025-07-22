# Multi-stage build for AGI Backend
FROM openjdk:17-jdk-slim as builder

# Install build dependencies
RUN apt-get update && apt-get install -y \
    curl \
    wget \
    unzip \
    && rm -rf /var/lib/apt/lists/*

# Set working directory
WORKDIR /app

# Copy gradle wrapper and build files
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# Make gradlew executable
RUN chmod +x ./gradlew

# Copy source code
COPY src src

# Build the application
RUN ./gradlew clean build -x test

# Runtime stage
FROM openjdk:17-jdk-slim

# Install runtime dependencies
RUN apt-get update && apt-get install -y \
    curl \
    wget \
    ffmpeg \
    python3 \
    python3-pip \
    && rm -rf /var/lib/apt/lists/*

# Install Python ML libraries for potential integration
RUN pip3 install --no-cache-dir \
    numpy \
    scipy \
    scikit-learn \
    tensorflow \
    torch \
    transformers

# Create app user
RUN groupadd -r agi && useradd -r -g agi agi

# Set working directory
WORKDIR /app

# Create necessary directories
RUN mkdir -p logs models nlp-models embeddings notebooks && \
    chown -R agi:agi /app

# Copy built application
COPY --from=builder /app/build/libs/*.jar app.jar

# Copy configuration files
COPY --chown=agi:agi docker-compose.yml .
COPY --chown=agi:agi monitoring monitoring

# Switch to app user
USER agi

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Set JVM options for containerized environment
ENV JAVA_OPTS="-Xmx2g -Xms1g -XX:+UseG1GC -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

