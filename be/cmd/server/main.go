package main

import (
	"encoding/json"
	"fmt"
	"io"
	"log"
	"net/http"
	"os"
	"path/filepath"
	"runtime/debug"
	"sync"
	"time"

	"github.com/fitkhi/data-generator/internal/builder"
	"github.com/fitkhi/data-generator/internal/generator"
)

// BuildRequest represents the incoming build request
type BuildRequest struct {
	Config     json.RawMessage `json:"config"`
	Platform   string          `json:"platform"`    // "windows" or "linux"
	BinaryName string          `json:"binary_name"` // Optional custom name
}

// BuildResponse represents the build job response
type BuildResponse struct {
	JobID  string `json:"job_id"`
	Status string `json:"status"`
}

// StatusResponse represents the polling status response
type StatusResponse struct {
	Status      string `json:"status"` // "processing", "completed", "failed"
	DownloadURL string `json:"download_url,omitempty"`
	Error       string `json:"error,omitempty"`
}

// ErrorResponse represents a JSON error response
type ErrorResponse struct {
	Error string `json:"error"`
}

// Job tracking
var (
	jobs      = make(map[string]*builder.BuildJob)
	jobsMutex sync.RWMutex
)

func main() {
	// Ensure storage directory exists
	if err := os.MkdirAll("./storage", 0755); err != nil {
		log.Fatalf("Failed to create storage directory: %v", err)
	}

	// Setup routes with panic recovery
	http.HandleFunc("/api/build", panicRecovery(corsMiddleware(handleBuild)))
	http.HandleFunc("/api/poll/", panicRecovery(corsMiddleware(handlePoll)))
	http.HandleFunc("/api/download/", panicRecovery(corsMiddleware(handleDownload)))
	http.HandleFunc("/api/capabilities", panicRecovery(corsMiddleware(handleCapabilities)))
	http.HandleFunc("/api/templates", panicRecovery(corsMiddleware(handleTemplates)))
	http.HandleFunc("/health", panicRecovery(handleHealth))

	// Start server
	port := ":8080"
	fmt.Printf("🚀 Data Generator Build Server\n")
	fmt.Printf("📡 Listening on http://localhost%s\n", port)
	fmt.Printf("🌐 CORS enabled for http://localhost:5173\n")
	fmt.Printf("💾 Storage: ./storage\n\n")

	log.Fatal(http.ListenAndServe(port, nil))
}

// panicRecovery middleware catches panics and returns proper error response
func panicRecovery(next http.HandlerFunc) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		defer func() {
			if err := recover(); err != nil {
				// Log the panic with stack trace
				log.Printf("❌ PANIC RECOVERED: %v\n%s", err, debug.Stack())

				// Return JSON error response
				w.Header().Set("Content-Type", "application/json")
				w.WriteHeader(http.StatusInternalServerError)
				json.NewEncoder(w).Encode(ErrorResponse{
					Error: fmt.Sprintf("Internal server error: %v", err),
				})
			}
		}()
		next(w, r)
	}
}

// CORS middleware
func corsMiddleware(next http.HandlerFunc) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		// Allow any origin for local VM access
		w.Header().Set("Access-Control-Allow-Origin", "*")
		w.Header().Set("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
		w.Header().Set("Access-Control-Allow-Headers", "Content-Type")

		if r.Method == "OPTIONS" {
			w.WriteHeader(http.StatusOK)
			return
		}

		next(w, r)
	}
}

// handleBuild processes build requests
func handleBuild(w http.ResponseWriter, r *http.Request) {
	log.Printf("📥 Build request received from %s", r.RemoteAddr)

	if r.Method != http.MethodPost {
		w.Header().Set("Content-Type", "application/json")
		w.WriteHeader(http.StatusMethodNotAllowed)
		json.NewEncoder(w).Encode(ErrorResponse{Error: "Method not allowed"})
		return
	}

	// Parse request
	body, err := io.ReadAll(r.Body)
	if err != nil {
		log.Printf("❌ Failed to read request body: %v", err)
		w.Header().Set("Content-Type", "application/json")
		w.WriteHeader(http.StatusBadRequest)
		json.NewEncoder(w).Encode(ErrorResponse{Error: "Failed to read request"})
		return
	}

	var req BuildRequest
	if err := json.Unmarshal(body, &req); err != nil {
		log.Printf("❌ Invalid JSON: %v", err)
		w.Header().Set("Content-Type", "application/json")
		w.WriteHeader(http.StatusBadRequest)
		json.NewEncoder(w).Encode(ErrorResponse{Error: "Invalid JSON"})
		return
	}

	// Validate platform
	if req.Platform != "windows" && req.Platform != "linux" {
		req.Platform = "linux" // Default
	}

	// Generate job ID
	jobID := fmt.Sprintf("build_%d", time.Now().UnixNano())

	// Create job
	job := &builder.BuildJob{
		ID:         jobID,
		Config:     req.Config,
		Platform:   req.Platform,
		Status:     "processing",
		Created:    time.Now(),
		BinaryName: req.BinaryName,
	}

	// Store job
	jobsMutex.Lock()
	jobs[jobID] = job
	jobsMutex.Unlock()

	log.Printf("✅ Build job created: %s (platform: %s)", jobID, req.Platform)

	// Start build in background
	go buildWorker(job)

	// Return response
	resp := BuildResponse{
		JobID:  jobID,
		Status: "processing",
	}

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(resp)
}

// handlePoll checks build status
func handlePoll(w http.ResponseWriter, r *http.Request) {
	// Extract job ID from path
	jobID := filepath.Base(r.URL.Path)
	log.Printf("📊 Poll request for job: %s", jobID)

	jobsMutex.RLock()
	job, exists := jobs[jobID]
	jobsMutex.RUnlock()

	if !exists {
		log.Printf("❌ Job not found: %s", jobID)
		w.Header().Set("Content-Type", "application/json")
		w.WriteHeader(http.StatusNotFound)
		json.NewEncoder(w).Encode(ErrorResponse{Error: "Job not found"})
		return
	}

	resp := StatusResponse{
		Status: job.Status,
	}

	if job.Status == "completed" {
		resp.DownloadURL = fmt.Sprintf("/api/download/%s", jobID)
	} else if job.Status == "failed" {
		resp.Error = job.Error
	}

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(resp)
}

// handleDownload serves the compiled binary with robust file discovery
func handleDownload(w http.ResponseWriter, r *http.Request) {
	jobID := filepath.Base(r.URL.Path)
	log.Printf("📥 Download request for job: %s", jobID)

	jobsMutex.RLock()
	job, exists := jobs[jobID]
	jobsMutex.RUnlock()

	if !exists {
		log.Printf("❌ Job not found: %s", jobID)
		w.Header().Set("Content-Type", "application/json")
		w.WriteHeader(http.StatusNotFound)
		json.NewEncoder(w).Encode(ErrorResponse{Error: "Job not found"})
		return
	}

	if job.Status != "completed" {
		log.Printf("❌ Job not completed yet: %s (status: %s)", jobID, job.Status)
		w.Header().Set("Content-Type", "application/json")
		w.WriteHeader(http.StatusNotFound)
		json.NewEncoder(w).Encode(ErrorResponse{Error: fmt.Sprintf("Binary not ready (status: %s)", job.Status)})
		return
	}

	// Robust file discovery: scan the storage directory for this job
	storageDir := filepath.Join("./storage", jobID)
	log.Printf("🔍 Scanning directory: %s", storageDir)

	// Check if directory exists
	if _, err := os.Stat(storageDir); os.IsNotExist(err) {
		log.Printf("❌ Storage directory not found: %s", storageDir)
		w.Header().Set("Content-Type", "application/json")
		w.WriteHeader(http.StatusNotFound)
		json.NewEncoder(w).Encode(ErrorResponse{Error: "Build output not found"})
		return
	}

	// Read directory contents
	entries, err := os.ReadDir(storageDir)
	if err != nil {
		log.Printf("❌ Failed to read directory %s: %v", storageDir, err)
		w.Header().Set("Content-Type", "application/json")
		w.WriteHeader(http.StatusInternalServerError)
		json.NewEncoder(w).Encode(ErrorResponse{Error: "Failed to read build output"})
		return
	}

	// Find the first file in the directory
	var targetFile string
	var targetFilename string
	for _, entry := range entries {
		if !entry.IsDir() {
			targetFile = filepath.Join(storageDir, entry.Name())
			targetFilename = entry.Name()
			log.Printf("✅ Found binary: %s", targetFilename)
			break
		}
	}

	// If no file found
	if targetFile == "" {
		log.Printf("❌ No files found in directory: %s", storageDir)
		w.Header().Set("Content-Type", "application/json")
		w.WriteHeader(http.StatusNotFound)
		json.NewEncoder(w).Encode(ErrorResponse{Error: "Build output is empty"})
		return
	}

	// Verify file exists and is readable
	fileInfo, err := os.Stat(targetFile)
	if err != nil {
		log.Printf("❌ File stat error for %s: %v", targetFile, err)
		w.Header().Set("Content-Type", "application/json")
		w.WriteHeader(http.StatusInternalServerError)
		json.NewEncoder(w).Encode(ErrorResponse{Error: "Build output is not accessible"})
		return
	}

	log.Printf("📦 Serving file: %s (size: %d bytes)", targetFilename, fileInfo.Size())

	// Set proper headers for download
	w.Header().Set("Content-Type", "application/octet-stream")
	w.Header().Set("Content-Disposition", fmt.Sprintf("attachment; filename=\"%s\"", targetFilename))
	w.Header().Set("Content-Length", fmt.Sprintf("%d", fileInfo.Size()))

	// Serve the file
	http.ServeFile(w, r, targetFile)
	log.Printf("✅ Download completed for job: %s", jobID)
}

// handleHealth simple health check
func handleHealth(w http.ResponseWriter, r *http.Request) {
	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(map[string]string{"status": "ok"})
}

// buildWorker performs the actual build
func buildWorker(job *builder.BuildJob) {
	log.Printf("🔨 Starting build for job: %s", job.ID)

	// Run builder
	outputPath, err := builder.CompileBinary(job.ID, job.Config, job.Platform, job.BinaryName)

	jobsMutex.Lock()
	defer jobsMutex.Unlock()

	if err != nil {
		job.Status = "failed"
		job.Error = err.Error()
		log.Printf("❌ Build failed for %s: %v", job.ID, err)
		return
	}

	job.Status = "completed"
	job.OutputPath = outputPath
	job.Completed = time.Now()
	log.Printf("✅ Build completed for %s: %s", job.ID, outputPath)
}

// handleCapabilities returns the list of available generators
func handleCapabilities(w http.ResponseWriter, r *http.Request) {
	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(generator.GetCapabilities())
}

// handleTemplates returns the list of schema templates
func handleTemplates(w http.ResponseWriter, r *http.Request) {
	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(generator.GetTemplates())
}
