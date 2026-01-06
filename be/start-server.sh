#!/bin/bash

# Data Generator Build Server - Startup Script

echo "🚀 Starting Data Generator Build Server..."
echo ""

# Check if server binary exists
if [ ! -f "bin/server" ]; then
    echo "📦 Building server..."
    go build -o bin/server cmd/server/main.go
    if [ $? -ne 0 ]; then
        echo "❌ Build failed"
        exit 1
    fi
    echo "✅ Server built"
fi

# Create storage directory
mkdir -p storage

# Start server
echo ""
echo "Starting server on http://localhost:8080"
echo "Frontend should connect from http://localhost:5173"
echo ""
echo "Press Ctrl+C to stop"
echo ""

./bin/server
