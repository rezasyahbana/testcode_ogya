package entity

// Config represents the entire configuration structure
type Config struct {
	GlobalSettings GlobalSettings `json:"global_settings"`
	SQLSettings    SQLSettings    `json:"sql_settings,omitempty"`
	Columns        []Column       `json:"columns"`
}

// GlobalSettings contains global generation parameters
type GlobalSettings struct {
	RowCount       int    `json:"row_count"`
	TargetSizeMB   int    `json:"target_size_mb,omitempty"`
	FileName       string `json:"file_name"`
	OutputFormat   string `json:"output_format"`   // csv, json, sql
	Platform       string `json:"platform"`        // windows, linux
	GenerationMode string `json:"generation_mode"` // rows, size
}

// SQLSettings contains SQL-specific configurations
type SQLSettings struct {
	Dialect   string `json:"dialect"`    // postgresql, mysql, mssql
	TableName string `json:"table_name"` // e.g., public.transactions
}

// Column defines a single column in the output
type Column struct {
	ColumnName    string                 `json:"column_name"`
	GeneratorType string                 `json:"generator_type"` // uuid, full_name, gender, date, embedded_csv, etc.
	SQLType       string                 `json:"sql_type,omitempty"`
	DateFormat    string                 `json:"date_format,omitempty"` // Deprecated: use Options["format"]
	SourceFile    string                 `json:"source_file,omitempty"` // Deprecated: use Options["source_file"]
	Options       map[string]interface{} `json:"options,omitempty"`     // Flexible options for generators
}
