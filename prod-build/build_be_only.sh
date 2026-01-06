#!/bin/bash

# ==============================================================================
# Backend Build Script (Standalone)
# Compiles the Go server and places artifacts in 'resource-build/be'
# ==============================================================================

set -e # Exit immediately on error

# 1. Configuration & Paths
# ------------------------------------------------------------------------------
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
BE_DIR="$PROJECT_ROOT/be"
TARGET_BASE="$SCRIPT_DIR/resource-build"
TARGET_BE="$TARGET_BASE/be"
TARGET_BINARY="$TARGET_BE/server"

echo "========================================================"
echo "🔨 Starting Backend Build"
echo "========================================================"
echo "📍 Project Root: $PROJECT_ROOT"
echo "📍 Backend Dir:  $BE_DIR"
echo "📍 Output Target: $TARGET_BINARY"
echo "--------------------------------------------------------"

# 2. Environment Check
# ------------------------------------------------------------------------------
echo "🔍 Checking Environment..."
if ! command -v go &> /dev/null; then
    echo "❌ Error: 'go' is not installed or not in PATH."
    exit 1
fi

GO_VER=$(go version)
echo "✅ Go Installed: $GO_VER"

# 3. Directory Setup
# ------------------------------------------------------------------------------
echo "🧹 Preparing Target Directory..."
mkdir -p "$TARGET_BASE"

# Clean previous 'be' folder if it exists
if [ -d "$TARGET_BE" ]; then
    echo "   Removing stale artifacts in 'be'..."
    rm -rf "$TARGET_BE"
fi

# Create target structure
mkdir -p "$TARGET_BE"

# 4. Dependency Check
# ------------------------------------------------------------------------------
echo "📦 Checking Dependencies..."
cd "$BE_DIR"

if [ ! -f "go.mod" ]; then
    echo "❌ Error: 'go.mod' not found in $BE_DIR"
    exit 1
fi

echo "   Running 'go mod tidy'..."
go mod tidy

# 5. Build Process
# ------------------------------------------------------------------------------
echo "🚀 Compiling Binary..."

# Build options:
# GOOS=linux   : Target Linux OS
# GOARCH=amd64 : Target AMD64 CPU
# -ldflags="-s -w" : Strip debug info (Symbol table, DWARF) for smaller size

env GOOS=linux GOARCH=amd64 go build \
    -ldflags="-s -w" \
    -o "$TARGET_BINARY" \
    ./cmd/server/main.go

# 6. Asset Handling / Final Setup
# ------------------------------------------------------------------------------
echo "📂 Setting up Backend Assets..."

# Create storage directory for the app to use
mkdir -p "$TARGET_BE/storage"
echo "   Created 'storage' directory."

# Copy .env.example if it exists (Optional)
if [ -f ".env.example" ]; then
    cp ".env.example" "$TARGET_BE/.env.example"
    echo "   Copied .env.example"
fi

# 7. Validation
# ------------------------------------------------------------------------------
echo "--------------------------------------------------------"
if [ -f "$TARGET_BINARY" ]; then
    SIZE=$(du -h "$TARGET_BINARY" | awk '{print $1}')
    echo "✅ Success! Backend Build Complete."
    echo "📂 Binary Location: $TARGET_BINARY"
    echo "📊 Binary Size: $SIZE"
else
    echo "❌ Error: Binary not found after build."
    exit 1
fi
echo "========================================================"
