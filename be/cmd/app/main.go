package main

import (
	_ "embed"
	"fmt"
	"os"
	"time"

	"github.com/fitkhi/data-generator/internal/entity"
	"github.com/fitkhi/data-generator/internal/generator"
)

//go:embed default_config.json
var embeddedConfigJSON []byte

func main() {
	fmt.Println("╔════════════════════════════════════════════════════════╗")
	fmt.Println("║      🚀 High-Performance Data Generator Engine       ║")
	fmt.Println("║          Indonesian Name Correlation Support          ║")
	fmt.Println("╚════════════════════════════════════════════════════════╝")
	fmt.Println()

	var config *entity.Config
	var err error

	// Try to load embedded config first (for built binaries)
	embeddedConfig := getEmbeddedConfig()
	if len(embeddedConfig) > 0 && string(embeddedConfig) != "{}" {
		fmt.Println("📦 Using embedded configuration")
		config, err = generator.LoadConfigFromString(string(embeddedConfig))
		if err != nil {
			fmt.Printf("❌ Error loading embedded config: %v\n", err)
			os.Exit(1)
		}
	} else {
		// Fall back to CLI argument
		configFile := "request-conf.json"
		if len(os.Args) > 1 {
			configFile = os.Args[1]
		}

		fmt.Printf("📂 Loading configuration from: %s\n", configFile)
		config, err = generator.LoadConfig(configFile)
		if err != nil {
			fmt.Printf("❌ Error loading config: %v\n", err)
			fmt.Println("\nUsage: ./generator [config-file.json]")
			fmt.Println("Default: ./generator request-conf.json")
			os.Exit(1)
		}
	}

	// Create generator
	gen := generator.NewGenerator(config)

	// Start generation with timing
	startTime := time.Now()

	if err := gen.Generate(); err != nil {
		fmt.Printf("❌ Error during generation: %v\n", err)
		os.Exit(1)
	}

	elapsed := time.Since(startTime)
	fmt.Printf("\n⏱️  Generation completed in: %s\n", elapsed)
	fmt.Printf("⚡ Throughput: %.0f rows/second\n", float64(config.GlobalSettings.RowCount)/elapsed.Seconds())
	fmt.Println("\n✨ Done! Your data is ready.")
}

// getEmbeddedConfig returns the embedded configuration
// This will be replaced by the builder with actual user config
func getEmbeddedConfig() []byte {
	return embeddedConfigJSON
}
