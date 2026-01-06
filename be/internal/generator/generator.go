package generator

import (
	"encoding/json"
	"fmt"
	"os"
	"runtime"
	"sync"

	"strings"

	"github.com/fitkhi/data-generator/internal/entity"
)

// Generator is the main data generation engine
type Generator struct {
	config      *entity.Config
	dataSource  *DataSource
	valueGen    *ValueGenerator
	workerCount int
}

// NewGenerator creates a new generator instance
func NewGenerator(config *entity.Config) *Generator {
	dataSource := NewDataSource()
	valueGen := NewValueGenerator(dataSource)

	// Use all available CPU cores for workers
	workerCount := runtime.NumCPU()

	return &Generator{
		config:      config,
		dataSource:  dataSource,
		valueGen:    valueGen,
		workerCount: workerCount,
	}
}

// Generate starts the data generation process
func (g *Generator) Generate() error {
	fmt.Printf("🚀 Starting data generation...\n")
	fmt.Printf("📊 Configuration:\n")
	fmt.Printf("   - Rows: %d\n", g.config.GlobalSettings.RowCount)
	fmt.Printf("   - Format: %s\n", g.config.GlobalSettings.OutputFormat)
	fmt.Printf("   - Workers: %d (CPU cores)\n", g.workerCount)
	fmt.Printf("   - Output: %s\n", g.config.GlobalSettings.FileName)

	switch g.config.GlobalSettings.OutputFormat {
	case "csv":
		return g.generateCSV()
	case "json":
		return g.generateJSON()
	case "sql":
		return g.generateSQL()
	default:
		return fmt.Errorf("unsupported output format: %s", g.config.GlobalSettings.OutputFormat)
	}
}

// generateCSV generates CSV output using worker pool
func (g *Generator) generateCSV() error {
	fileName := g.config.GlobalSettings.FileName + ".csv"
	file, err := os.Create(fileName)
	if err != nil {
		return fmt.Errorf("failed to create file: %w", err)
	}
	defer file.Close()

	// Write header
	header := ""
	for i, col := range g.config.Columns {
		if i > 0 {
			header += ","
		}
		header += col.ColumnName
	}
	header += "\n"
	file.WriteString(header)

	// Use worker pool to generate rows
	rows := g.generateRowsParallel()

	// Write rows sequentially to maintain order
	for row := range rows {
		file.WriteString(row)
	}

	fmt.Printf("✅ CSV file generated: %s\n", fileName)
	return nil
}

// generateJSON generates JSON output
func (g *Generator) generateJSON() error {
	fileName := g.config.GlobalSettings.FileName + ".json"
	file, err := os.Create(fileName)
	if err != nil {
		return fmt.Errorf("failed to create file: %w", err)
	}
	defer file.Close()

	file.WriteString("[\n")

	// Use worker pool to generate rows
	rows := g.generateRowsParallel()

	first := true
	for rowData := range rows {
		if !first {
			file.WriteString(",\n")
		}
		first = false

		// For now, write raw data (simplified JSON generation)
		file.WriteString("  " + rowData)
	}

	file.WriteString("\n]\n")

	fmt.Printf("✅ JSON file generated: %s\n", fileName)
	return nil
}

// generateSQL generates SQL INSERT statements with dialect-specific formatting
func (g *Generator) generateSQL() error {
	fileName := g.config.GlobalSettings.FileName + ".sql"
	file, err := os.Create(fileName)
	if err != nil {
		return fmt.Errorf("failed to create file: %w", err)
	}
	defer file.Close()

	// Create SQL formatter for the specified dialect
	formatter := NewSQLDialectFormatter(g.config.SQLSettings.Dialect)

	// Write SQL header
	file.WriteString(fmt.Sprintf("-- Generated SQL for %s\n", g.config.SQLSettings.TableName))
	file.WriteString(fmt.Sprintf("-- Dialect: %s\n", g.config.SQLSettings.Dialect))
	file.WriteString(fmt.Sprintf("-- Total rows: %d\n\n", g.config.GlobalSettings.RowCount))

	// Generate CREATE TABLE statement
	createStmt := formatter.GenerateCREATEStatement(g.config.SQLSettings.TableName, g.config.Columns)
	file.WriteString(createStmt)

	// Use worker pool to generate rows
	rows := g.generateRowsParallel()

	for rowData := range rows {
		// Generate INSERT statement with quoted identifiers
		insertStmt := fmt.Sprintf("INSERT INTO %s (", formatter.QuoteIdentifier(g.config.SQLSettings.TableName))

		// Add column names with proper quoting
		for i, col := range g.config.Columns {
			if i > 0 {
				insertStmt += ", "
			}
			insertStmt += formatter.QuoteIdentifier(col.ColumnName)
		}

		insertStmt += ") VALUES (" + rowData + ");\n"
		file.WriteString(insertStmt)
	}

	fmt.Printf("✅ SQL file generated: %s\n", fileName)
	return nil
}

// generateRowsParallel uses a worker pool to generate rows in parallel
func (g *Generator) generateRowsParallel() <-chan string {
	rowChannel := make(chan string, g.workerCount*10)

	var wg sync.WaitGroup
	totalRows := g.config.GlobalSettings.RowCount
	rowsPerWorker := totalRows / g.workerCount

	// Start workers
	for i := 0; i < g.workerCount; i++ {
		wg.Add(1)
		startRow := i * rowsPerWorker
		endRow := startRow + rowsPerWorker

		// Last worker handles remaining rows
		if i == g.workerCount-1 {
			endRow = totalRows
		}

		go g.worker(startRow, endRow, rowChannel, &wg)
	}

	// Close channel when all workers are done
	go func() {
		wg.Wait()
		close(rowChannel)
	}()

	return rowChannel
}

// worker generates rows for a specific range
func (g *Generator) worker(startRow, endRow int, rowChannel chan<- string, wg *sync.WaitGroup) {
	defer wg.Done()

	for rowNum := startRow; rowNum < endRow; rowNum++ {
		row := g.generateSingleRow()
		rowChannel <- row
	}
}

// generateSingleRow generates a single data row
func (g *Generator) generateSingleRow() string {
	// Generate a person profile once per row for correlation
	person := g.dataSource.GeneratePerson()

	row := ""
	for i, col := range g.config.Columns {
		if i > 0 {
			row += ","
		}

		value := g.generateValue(col, person)

		// Quote string values for CSV/SQL
		if needsQuoting(col.GeneratorType) {
			row += fmt.Sprintf("'%s'", value)
		} else {
			row += value
		}
	}

	row += "\n"
	return row
}

// generateValue generates a value for a specific column
func (g *Generator) generateValue(col entity.Column, person *entity.Person) string {
	genFunc, err := GetGenerator(col.GeneratorType)
	if err != nil {
		// Fallback for unknown types or handle silently
		return "NULL"
	}

	return genFunc(g.valueGen, person, col.Options)
}

// needsQuoting determines if a value needs to be quoted
func needsQuoting(generatorType string) bool {
	meta := GetMeta(generatorType)
	if meta == nil {
		return true // Default to quoting for safety
	}

	sqlType := strings.ToUpper(meta.SqlType)
	if strings.Contains(sqlType, "INT") ||
		strings.Contains(sqlType, "DECIMAL") ||
		strings.Contains(sqlType, "NUMERIC") ||
		strings.Contains(sqlType, "FLOAT") ||
		strings.Contains(sqlType, "DOUBLE") ||
		strings.Contains(sqlType, "BOOLEAN") {
		return false
	}

	return true
}

// LoadConfig loads configuration from a JSON file
func LoadConfig(filename string) (*entity.Config, error) {
	data, err := os.ReadFile(filename)
	if err != nil {
		return nil, fmt.Errorf("failed to read config file: %w", err)
	}

	var config entity.Config
	if err := json.Unmarshal(data, &config); err != nil {
		return nil, fmt.Errorf("failed to parse config JSON: %w", err)
	}

	return &config, nil
}

// LoadConfigFromString loads configuration from a JSON string (for embedded configs)
func LoadConfigFromString(jsonStr string) (*entity.Config, error) {
	var config entity.Config
	if err := json.Unmarshal([]byte(jsonStr), &config); err != nil {
		return nil, fmt.Errorf("failed to parse config JSON: %w", err)
	}

	return &config, nil
}
