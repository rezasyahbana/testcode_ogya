#!/bin/bash

# 🔧 Auto-Rebuild and Restart Script
# This script kills the running server, rebuilds it, and starts it again

set -e  # Exit on error

echo "🛑 Stopping running server..."
# Find and kill the server process
pkill -f "./bin/server" || echo "  (No running server found)"

echo ""
echo "🔨 Rebuilding server..."
cd /home/fitkhi/KERJA/DEVELOPMENT/CODEBASE/data-generator-fe-be-v1/be
go build -o bin/server cmd/server/main.go

echo ""
echo "✅ Build complete!"
echo ""
echo "🚀 Starting server..."
./bin/server
