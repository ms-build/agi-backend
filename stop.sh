#!/bin/bash

# AGI Backend Stop Script
echo "🛑 Stopping AGI Backend System..."

# Find and kill the Java process
JAVA_PID=$(ps aux | grep 'agi-backend' | grep -v grep | awk '{print $2}')

if [ -n "$JAVA_PID" ]; then
    echo "🔄 Stopping AGI Backend application (PID: $JAVA_PID)..."
    kill -TERM $JAVA_PID
    
    # Wait for graceful shutdown
    sleep 5
    
    # Force kill if still running
    if ps -p $JAVA_PID > /dev/null; then
        echo "⚡ Force stopping application..."
        kill -KILL $JAVA_PID
    fi
    
    echo "✅ AGI Backend application stopped"
else
    echo "ℹ️  AGI Backend application is not running"
fi

# Stop Docker services if available
if command -v docker-compose &> /dev/null; then
    echo "🐳 Stopping infrastructure services..."
    docker-compose down
    echo "✅ Infrastructure services stopped"
fi

echo "👋 AGI Backend system completely stopped"

