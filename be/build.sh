#!/bin/bash

# High-Performance Data Generator - Build Script
# This script compiles the generator for multiple platforms

set -e

echo "╔════════════════════════════════════════════════════════╗"
echo "║       Building Data Generator for All Platforms      ║"
echo "╚════════════════════════════════════════════════════════╝"
echo ""

# Create bin directory if it doesn't exist
mkdir -p bin

# Install dependencies
echo "📦 Installing dependencies..."
go get github.com/google/uuid
go mod tidy
echo "✅ Dependencies ready"
echo ""

# Build for Linux
echo "🐧 Building for Linux (amd64)..."
GOOS=linux GOARCH=amd64 go build -o bin/generator-linux cmd/app/main.go
chmod +x bin/generator-linux
echo "✅ Build complete: bin/generator-linux"
echo ""

# Build for Windows
echo "🪟 Building for Windows (amd64)..."
GOOS=windows GOARCH=amd64 go build -o bin/generator-win.exe cmd/app/main.go
echo "✅ Build complete: bin/generator-win.exe"
echo ""

# Build for current platform
echo "🔨 Building for current platform..."
go build -o bin/generator cmd/app/main.go
echo "✅ Build complete: bin/generator"
echo ""

# Show results
echo "🎉 All builds complete!"
ls -lh bin/
echo ""
echo "📋 Usage:"
echo "   Linux:   ./bin/generator-linux request-conf.json"
echo "   Windows: generator-win.exe request-conf.json"
echo "   Current: ./bin/generator request-conf.json"
