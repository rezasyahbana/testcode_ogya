package builder

import (
	"archive/zip"
	"encoding/json"
	"fmt"
	"io"
	"log"
	"os"
	"os/exec"
	"path/filepath"
	"time"
)

// BuildJob represents a build job
type BuildJob struct {
	ID         string          `json:"id"`
	Config     json.RawMessage `json:"config"`
	Platform   string          `json:"platform"`
	Status     string          `json:"status"`
	Error      string          `json:"error,omitempty"`
	OutputPath string          `json:"output_path,omitempty"`
	Created    time.Time       `json:"created"`
	Completed  time.Time       `json:"completed,omitempty"`
	BinaryName string          `json:"binary_name,omitempty"`
}

// CompileBinary builds a custom binary with embedded configuration
func CompileBinary(jobID string, userConfig []byte, platform string, binaryName string) (string, error) {
	log.Printf("🔨 [%s] Starting build process (platform: %s)", jobID, platform)

	// Create temp build directory
	tempDir := filepath.Join("/tmp", "builds", jobID)
	if err := os.MkdirAll(tempDir, 0755); err != nil {
		return "", fmt.Errorf("failed to create temp dir: %w", err)
	}
	defer func() {
		if err := os.RemoveAll(tempDir); err != nil {
			log.Printf("⚠️  [%s] Failed to cleanup temp dir: %v", jobID, err)
		}
	}()

	// Get source root
	sourceRoot, err := filepath.Abs(".")
	if err != nil {
		return "", fmt.Errorf("failed to get source root: %w", err)
	}
	log.Printf("📂 [%s] Source root: %s", jobID, sourceRoot)

	// Copy necessary source files to temp directory
	log.Printf("📋 [%s] Copying source files...", jobID)
	if err := copySourceFiles(sourceRoot, tempDir); err != nil {
		return "", fmt.Errorf("failed to copy source files: %w", err)
	}

	// Inject user configuration into main.go
	log.Printf("💉 [%s] Injecting configuration...", jobID)
	if err := injectConfig(tempDir, userConfig, binaryName); err != nil {
		return "", fmt.Errorf("failed to inject config: %w", err)
	}

	// Run go mod tidy to ensure dependencies are resolved
	log.Printf("📦 [%s] Running go mod tidy...", jobID)
	if err := runGoModTidy(tempDir, jobID); err != nil {
		return "", fmt.Errorf("go mod tidy failed: %w", err)
	}

	// Compile binary
	log.Printf("⚙️  [%s] Compiling binary...", jobID)
	outputPath, err := compileBinary(tempDir, jobID, platform, binaryName)
	if err != nil {
		return "", err // Already formatted with details
	}

	log.Printf("✅ [%s] Build completed successfully: %s", jobID, outputPath)
	return outputPath, nil
}

// copySourceFiles copies the generator source code to temp directory
func copySourceFiles(sourceRoot, destDir string) error {
	// Directories to copy (including all subdirectories)
	dirs := []string{
		"cmd/app",
		"internal/generator",
		"internal/entity",
	}

	for _, dir := range dirs {
		srcPath := filepath.Join(sourceRoot, dir)
		destPath := filepath.Join(destDir, dir)

		// Verify source exists
		if _, err := os.Stat(srcPath); os.IsNotExist(err) {
			return fmt.Errorf("source directory not found: %s", srcPath)
		}

		if err := os.MkdirAll(filepath.Dir(destPath), 0755); err != nil {
			return fmt.Errorf("failed to create dest dir for %s: %w", dir, err)
		}

		if err := copyDir(srcPath, destPath); err != nil {
			return fmt.Errorf("failed to copy %s: %w", dir, err)
		}
	}

	// Copy go.mod and go.sum (CRITICAL for dependency resolution)
	for _, file := range []string{"go.mod", "go.sum"} {
		src := filepath.Join(sourceRoot, file)
		dest := filepath.Join(destDir, file)

		// Verify file exists
		if _, err := os.Stat(src); err != nil {
			return fmt.Errorf("required file not found: %s (%w)", file, err)
		}

		if err := copyFile(src, dest); err != nil {
			return fmt.Errorf("failed to copy %s: %w", file, err)
		}
		log.Printf("  ✓ Copied %s", file)
	}

	return nil
}

// injectConfig writes the user configuration to default_config.json
// which gets embedded via //go:embed during the build
// It also overrides the output filename to match the binary name
func injectConfig(tempDir string, config []byte, binaryName string) error {
	// Parse the config to modify it
	var configMap map[string]interface{}
	if err := json.Unmarshal(config, &configMap); err != nil {
		return fmt.Errorf("failed to parse config JSON: %w", err)
	}

	// Sanitize binary name for file output (remove extension)
	binaryNameBase := filepath.Base(binaryName)
	ext := filepath.Ext(binaryNameBase)
	if ext != "" {
		binaryNameBase = binaryNameBase[:len(binaryNameBase)-len(ext)]
	}
	// Fallback if empty
	if binaryNameBase == "" || binaryNameBase == "." {
		binaryNameBase = fmt.Sprintf("generate-%d", time.Now().Unix())
	}

	// Update global_settings.file_name
	if globalSettings, ok := configMap["global_settings"].(map[string]interface{}); ok {
		globalSettings["file_name"] = fmt.Sprintf("output-%s", binaryNameBase)
	} else {
		// If structure is missing, ensure it exists
		configMap["global_settings"] = map[string]interface{}{
			"file_name": fmt.Sprintf("output-%s", binaryNameBase),
		}
	}

	// Re-serialize
	modifiedConfig, err := json.MarshalIndent(configMap, "", "  ")
	if err != nil {
		return fmt.Errorf("failed to serialize modified config: %w", err)
	}

	// Write the config JSON to default_config.json in cmd/app/
	configPath := filepath.Join(tempDir, "cmd", "app", "default_config.json")

	if err := os.WriteFile(configPath, modifiedConfig, 0644); err != nil {
		return fmt.Errorf("failed to write default_config.json: %w", err)
	}

	log.Printf("  ✓ Injected %d bytes of config to default_config.json (Filename: output-%s)", len(modifiedConfig), binaryNameBase)
	return nil
}

// runGoModTidy ensures all dependencies are resolved in the sandbox
func runGoModTidy(tempDir, jobID string) error {
	cmd := exec.Command("go", "mod", "tidy")
	cmd.Dir = tempDir
	cmd.Env = os.Environ()

	output, err := cmd.CombinedOutput()
	if err != nil {
		return fmt.Errorf("go mod tidy failed: %w\nOutput:\n%s", err, string(output))
	}

	if len(output) > 0 {
		log.Printf("  [%s] go mod tidy output: %s", jobID, string(output))
	}

	return nil
}

// compileBinary runs go build with detailed error capture
func compileBinary(tempDir, jobID, platform, binaryName string) (string, error) {
	// Determine output filename
	if binaryName == "" {
		binaryName = fmt.Sprintf("generate-%d", time.Now().Unix())
	} else {
		// Sanitize user input (remove extension if user added it, we add it back)
		// Simple approach: remove common logical extensions to prevent duplicates
		binaryName = filepath.Base(binaryName)
		ext := filepath.Ext(binaryName)
		if ext == ".exe" || ext == ".sh" {
			binaryName = binaryName[:len(binaryName)-len(ext)]
		}
	}

	var outputName string
	if platform == "windows" {
		outputName = binaryName + ".exe"
	} else {
		outputName = binaryName // Linux binary usually has no extension
	}

	// Create job-specific storage directory
	storageDir := filepath.Join("./storage", jobID)
	if err := os.MkdirAll(storageDir, 0755); err != nil {
		return "", fmt.Errorf("failed to create storage directory: %w", err)
	}

	// Absolute path for output
	outputPath, err := filepath.Abs(filepath.Join(storageDir, outputName))
	if err != nil {
		return "", fmt.Errorf("failed to resolve output path: %w", err)
	}

	// Build command targeting cmd/app/main.go explicitly
	cmd := exec.Command("go", "build", "-v", "-o", outputPath, "./cmd/app/main.go")
	cmd.Dir = tempDir

	// Set environment for cross-compilation
	goos := platformToGOOS(platform)
	cmd.Env = append(os.Environ(),
		fmt.Sprintf("GOOS=%s", goos),
		"GOARCH=amd64",
		"CGO_ENABLED=0", // Disable CGO for cleaner cross-compilation
	)

	log.Printf("  [%s] Build command: GOOS=%s GOARCH=amd64 go build -o %s ./cmd/app/main.go",
		jobID, goos, outputPath)

	// Run build and capture ALL output
	output, err := cmd.CombinedOutput()

	// Always log the output for debugging
	if len(output) > 0 {
		log.Printf("  [%s] Build output:\n%s", jobID, string(output))
	}

	if err != nil {
		// Return detailed error with compiler output
		return "", fmt.Errorf("go build failed: %w\n\n=== COMPILER OUTPUT ===\n%s\n=== END OUTPUT ===",
			err, string(output))
	}

	// Verify the output file was actually created
	if _, err := os.Stat(outputPath); os.IsNotExist(err) {
		return "", fmt.Errorf("build completed but output file not found: %s", outputPath)
	}

	// ZIP Logic for Windows
	if platform == "windows" {
		zipName := binaryName + ".zip"
		zipPath := filepath.Join(storageDir, zipName)

		log.Printf("📦 [%s] Creating ZIP archive: %s", jobID, zipName)

		// Create zip file
		archive, err := os.Create(zipPath)
		if err != nil {
			return "", fmt.Errorf("failed to create zip file: %w", err)
		}
		defer archive.Close()

		zipWriter := zip.NewWriter(archive)

		// Open executable to read
		binaryFile, err := os.Open(outputPath)
		if err != nil {
			return "", fmt.Errorf("failed to open binary for zipping: %w", err)
		}
		defer binaryFile.Close()

		// Create entry in zip
		w, err := zipWriter.Create(outputName) // outputName is filename.exe
		if err != nil {
			return "", fmt.Errorf("failed to create zip entry: %w", err)
		}

		// Copy data
		if _, err := io.Copy(w, binaryFile); err != nil {
			return "", fmt.Errorf("failed to write data to zip: %w", err)
		}

		zipWriter.Close()
		archive.Close()

		// Remove original .exe to save space and only serve zip
		os.Remove(outputPath)

		// Update output path to the zip
		outputPath = zipPath
		outputName = zipName
	}

	// Get file size for logging
	fileInfo, _ := os.Stat(outputPath)
	log.Printf("  ✓ Binary created: %s (size: %d bytes)", outputName, fileInfo.Size())

	return outputPath, nil
}

// Helper functions

func platformToGOOS(platform string) string {
	if platform == "windows" {
		return "windows"
	}
	return "linux"
}

func copyDir(src, dst string) error {
	return filepath.Walk(src, func(path string, info os.FileInfo, err error) error {
		if err != nil {
			return err
		}

		// Calculate relative path
		relPath, err := filepath.Rel(src, path)
		if err != nil {
			return err
		}

		destPath := filepath.Join(dst, relPath)

		if info.IsDir() {
			return os.MkdirAll(destPath, info.Mode())
		}

		// Copy all files including .csv assets
		return copyFile(path, destPath)
	})
}

func copyFile(src, dst string) error {
	input, err := os.ReadFile(src)
	if err != nil {
		return fmt.Errorf("failed to read %s: %w", src, err)
	}

	// Ensure destination directory exists
	if err := os.MkdirAll(filepath.Dir(dst), 0755); err != nil {
		return fmt.Errorf("failed to create dir for %s: %w", dst, err)
	}

	if err := os.WriteFile(dst, input, 0644); err != nil {
		return fmt.Errorf("failed to write %s: %w", dst, err)
	}

	return nil
}
