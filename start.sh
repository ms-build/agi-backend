#!/bin/bash

# AGI Backend Start Script
echo "🚀 Starting AGI Backend System..."

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "❌ Java is not installed. Please install Java 17 or higher."
    exit 1
fi

# Check Java version
JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 17 ]; then
    echo "❌ Java 17 or higher is required. Current version: $JAVA_VERSION"
    exit 1
fi

echo "✅ Java version check passed"

# Create necessary directories
echo "📁 Creating necessary directories..."
mkdir -p logs models nlp-models embeddings

# Check if Docker is available for infrastructure
if command -v docker &> /dev/null && command -v docker-compose &> /dev/null; then
    echo "🐳 Docker detected. Starting infrastructure services..."
    
    # Start infrastructure services in background
    docker-compose up -d redis elasticsearch kafka zookeeper minio
    
    echo "⏳ Waiting for services to start..."
    sleep 10
    
    echo "✅ Infrastructure services started"
else
    echo "⚠️  Docker not available. Running in standalone mode (H2 database only)"
fi

# Build the application if jar doesn't exist
if [ ! -f "build/libs/agi-backend-1.0.0.jar" ]; then
    echo "🔨 Building application..."
    ./gradlew clean build -x test
    
    if [ $? -ne 0 ]; then
        echo "❌ Build failed"
        exit 1
    fi
    
    echo "✅ Build completed"
fi

# Set JVM options
export JAVA_OPTS="-Xmx2g -Xms512m -XX:+UseG1GC"

# Start the application
echo "🎯 Starting AGI Backend application..."
echo "📊 Access H2 Console: http://localhost:8080/h2-console"
echo "📈 Access Actuator: http://localhost:8080/actuator/health"
echo "🔍 View logs: tail -f logs/agi-backend.log"
echo ""

java $JAVA_OPTS -jar build/libs/agi-backend-1.0.0.jar

echo "👋 AGI Backend stopped"

