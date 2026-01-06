package generator

import (
	"fmt"
	"strings"

	"github.com/fitkhi/data-generator/internal/entity"
)

// SQLDialectFormatter handles dialect-specific SQL formatting
type SQLDialectFormatter struct {
	dialect string
}

// NewSQLDialectFormatter creates a new formatter for a specific SQL dialect
func NewSQLDialectFormatter(dialect string) *SQLDialectFormatter {
	return &SQLDialectFormatter{dialect: dialect}
}

// QuoteIdentifier quotes a column or table name according to dialect
func (f *SQLDialectFormatter) QuoteIdentifier(name string) string {
	switch f.dialect {
	case "postgresql":
		return fmt.Sprintf("\"%s\"", name)
	case "mysql":
		return fmt.Sprintf("`%s`", name)
	case "sqlserver", "mssql":
		return fmt.Sprintf("[%s]", name)
	default:
		return name
	}
}

// GenerateCREATEStatement generates a CREATE TABLE statement
func (f *SQLDialectFormatter) GenerateCREATEStatement(tableName string, columns []entity.Column) string {
	var builder strings.Builder

	builder.WriteString(fmt.Sprintf("CREATE TABLE %s (\n", f.QuoteIdentifier(tableName)))

	for i, col := range columns {
		if i > 0 {
			builder.WriteString(",\n")
		}

		// Column definition
		builder.WriteString(fmt.Sprintf("  %s %s",
			f.QuoteIdentifier(col.ColumnName),
			f.ConvertSQLType(col.SQLType, col.GeneratorType)))
	}

	builder.WriteString("\n);\n\n")
	return builder.String()
}

// ConvertSQLType converts a generic SQL type to dialect-specific type
func (f *SQLDialectFormatter) ConvertSQLType(sqlType, generatorType string) string {
	if sqlType == "" {
		sqlType = f.getDefaultSQLType(generatorType)
	}

	// Handle auto-increment specifically based on generator type
	if generatorType == "increment_id" {
		switch f.dialect {
		case "postgresql":
			return "SERIAL"
		case "mysql":
			return "INT AUTO_INCREMENT"
		case "sqlserver", "mssql":
			return "INT IDENTITY(1,1)"
		}
	}

	upperType := strings.ToUpper(sqlType)
	// Fallback to default conversions
	switch f.dialect {
	case "postgresql":
		switch upperType {
		case "UUID":
			return "UUID"
		case "BOOLEAN", "BOOL":
			return "BOOLEAN"
		case "TIMESTAMP":
			return "TIMESTAMP"
		case "TEXT":
			return "TEXT"
		case "INTEGER", "INT":
			return "INTEGER"
		case "BIGINT":
			return "BIGINT"
		default:
			return sqlType
		}

	case "mysql":
		switch upperType {
		case "UUID":
			return "CHAR(36)"
		case "BOOLEAN", "BOOL":
			return "TINYINT(1)"
		case "TIMESTAMP":
			return "DATETIME"
		case "TEXT":
			return "TEXT"
		case "INTEGER", "INT":
			return "INT"
		case "BIGINT":
			return "BIGINT"
		default:
			return sqlType
		}

	case "sqlserver", "mssql":
		switch upperType {
		case "UUID":
			return "UNIQUEIDENTIFIER"
		case "BOOLEAN", "BOOL":
			return "BIT"
		case "TIMESTAMP":
			return "DATETIME2"
		case "TEXT":
			return "NVARCHAR(MAX)"
		case "INTEGER", "INT":
			return "INT"
		case "BIGINT":
			return "BIGINT"
		default:
			// Convert VARCHAR to NVARCHAR for SQL Server
			if strings.HasPrefix(upperType, "VARCHAR") {
				return strings.Replace(upperType, "VARCHAR", "NVARCHAR", 1)
			}
			return sqlType
		}

	default:
		return sqlType
	}
}

// getDefaultSQLType returns a default SQL type based on generator type
func (f *SQLDialectFormatter) getDefaultSQLType(generatorType string) string {
	switch generatorType {
	case "uuid":
		return "UUID"
	case "boolean", "bool":
		return "BOOLEAN"
	case "integer", "increment_id":
		return "INTEGER"
	case "decimal":
		return "DECIMAL(10,2)"
	case "date":
		return "DATE"
	case "timestamp":
		return "TIMESTAMP"
	case "email":
		return "VARCHAR(150)"
	case "phone":
		return "VARCHAR(20)"
	case "ip_address", "ipv4":
		return "VARCHAR(45)"
	case "salary":
		return "VARCHAR(50)"
	case "random_number":
		return "VARCHAR(50)"
	default:
		return "VARCHAR(255)"
	}
}

// FormatBooleanValue formats boolean values according to dialect
func (f *SQLDialectFormatter) FormatBooleanValue(value bool) string {
	switch f.dialect {
	case "postgresql":
		if value {
			return "TRUE"
		}
		return "FALSE"
	case "mysql":
		if value {
			return "1"
		}
		return "0"
	case "sqlserver", "mssql":
		if value {
			return "1"
		}
		return "0"
	default:
		if value {
			return "true"
		}
		return "false"
	}
}
