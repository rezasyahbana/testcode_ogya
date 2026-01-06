.PHONY: build build-linux build-windows build-all clean run test

# Default build for current platform
build:
	@echo "🔨 Building for current platform..."
	go build -o bin/generator cmd/app/main.go
	@echo "✅ Build complete: bin/generator"

# Build for Linux
build-linux:
	@echo "🐧 Building for Linux (amd64)..."
	GOOS=linux GOARCH=amd64 go build -o bin/generator-linux cmd/app/main.go
	@chmod +x bin/generator-linux
	@echo "✅ Build complete: bin/generator-linux"

# Build for Windows
build-windows:
	@echo "🪟 Building for Windows (amd64)..."
	GOOS=windows GOARCH=amd64 go build -o bin/generator-win.exe cmd/app/main.go
	@echo "✅ Build complete: bin/generator-win.exe"

# Build for all platforms
build-all: build-linux build-windows
	@echo "🎉 All builds complete!"
	@ls -lh bin/

# Clean build artifacts
clean:
	@echo "🧹 Cleaning build artifacts..."
	rm -rf bin/
	rm -f *.csv *.json *.sql
	@echo "✅ Clean complete"

# Run the application with default config
run: build
	@echo "🚀 Running generator..."
	./bin/generator request-conf.json

# Run tests
test:
	@echo "🧪 Running tests..."
	go test -v ./...

# Download dependencies
deps:
	@echo "📦 Downloading dependencies..."
	go mod download
	go mod tidy
	@echo "✅ Dependencies ready"

# Install required packages
install-deps:
	@echo "📦 Installing required packages..."
	go get github.com/google/uuid
	go mod tidy
	@echo "✅ Dependencies installed"

# Build with optimizations (smaller binary)
build-optimized:
	@echo "⚡ Building optimized binary..."
	go build -ldflags="-s -w" -o bin/generator-optimized cmd/app/main.go
	@echo "✅ Optimized build complete"

# Show help
help:
	@echo "Available commands:"
	@echo "  make build          - Build for current platform"
	@echo "  make build-linux    - Build for Linux"
	@echo "  make build-windows  - Build for Windows"
	@echo "  make build-all      - Build for all platforms"
	@echo "  make clean          - Remove build artifacts"
	@echo "  make run            - Build and run with default config"
	@echo "  make test           - Run tests"
	@echo "  make deps           - Download dependencies"
	@echo "  make install-deps   - Install required packages"
	@echo "  make help           - Show this help message"
