#!/bin/bash

# ==============================================================================
# Frontend Build Script (Standalone)
# Builds the React application and places artifacts in 'resource-build/fe'
# ==============================================================================

set -e # Exit immediately if a command exits with a non-zero status

# 1. Configuration & Paths
# ------------------------------------------------------------------------------
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
FE_DIR="$PROJECT_ROOT/fe"

# MODIFIED: Output directory is now 'fe' instead of 'dist'
TARGET_BASE="$SCRIPT_DIR/resource-build"
TARGET_FE="$TARGET_BASE/fe"

echo "========================================================"
echo "🎨 Starting Frontend Build"
echo "========================================================"
echo "📍 Project Root: $PROJECT_ROOT"
echo "📍 Frontend Dir: $FE_DIR"
echo "📍 Output Target: $TARGET_FE"
echo "--------------------------------------------------------"

# 2. Environment Check
# ------------------------------------------------------------------------------
echo "🔍 Checking Environment..."
if ! command -v npm &> /dev/null; then
    echo "❌ Error: 'npm' is not installed or not in PATH."
    exit 1
fi

NODE_VER=$(node -v)
NPM_VER=$(npm -v)
echo "✅ Node: $NODE_VER | NPM: $NPM_VER"

# 3. Directory Setup
# ------------------------------------------------------------------------------
echo "🧹 Preparing Target Directory..."
mkdir -p "$TARGET_BASE"

# Clean previous 'fe' folder if it exists
if [ -d "$TARGET_FE" ]; then
    echo "   Removing stale artifacts in 'fe'..."
    rm -rf "$TARGET_FE"
fi

# 4. Build Process
# ------------------------------------------------------------------------------
echo "🚀 Building Frontend..."
cd "$FE_DIR"

# Install Dependencies
if [ -f "package-lock.json" ]; then
    echo "📦 Installing dependencies (Clean Install)..."
    npm ci
else
    echo "📦 Installing dependencies..."
    npm install
fi

# Run Build
echo "🔨 Running 'npm run build'..."
npm run build

# 5. Artifact Transfer
# ------------------------------------------------------------------------------
echo "🚚 Transferring Artifacts..."

# Verify build output exists (Vite creates 'dist' by default)
if [ ! -d "dist" ]; then
    echo "❌ Error: 'dist' folder not found in fe/. Build may have failed silently."
    exit 1
fi

# MODIFIED: Rename 'dist' to 'fe' and move to target
mv dist "$TARGET_FE"

# 6. Validation
# ------------------------------------------------------------------------------
echo "--------------------------------------------------------"
if [ -d "$TARGET_FE" ] && [ "$(ls -A "$TARGET_FE")" ]; then
    SIZE=$(du -sh "$TARGET_FE" | awk '{print $1}')
    echo "✅ Success! Frontend Build Complete."
    echo "📂 Artifacts Location: $TARGET_FE"
    echo "📊 Total Size: $SIZE"
else
    echo "❌ Error: Target directory is empty or missing after move."
    exit 1
fi
echo "========================================================"