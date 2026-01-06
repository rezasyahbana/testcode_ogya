# 🚀 High-Performance Data Generator Engine

A blazing-fast, concurrent data generator written in Go that produces massive datasets (CSV/JSON/SQL) with **correlated Indonesian name logic**.

## ✨ Key Features

### 🎯 Core Capabilities
- **Embedded Data Sources** - CSV files baked into binary using `//go:embed` (no external dependencies)
- **Indonesian Name Correlation** - First name, last name, gender, and full name are always consistent within a row
- **High-Performance Parallel Computing** - Uses goroutines and worker pools to leverage all CPU cores
- **Cross-Platform** - Compiles to standalone binaries for Windows (.exe) and Linux
- **Multiple Output Formats** - CSV, JSON, SQL (PostgreSQL, MySQL, MSSQL)
- **Zero External Dependencies** - Self-contained executable

### 🧠 Smart Name Generation Logic

When you request `full_name`, `first_name`, and `gender` in the same row:
```
✅ CORRECT (Correlated):
full_name: "Siti Ratna Wijaya"
first_name: "Siti"
gender: "P"

❌ WRONG (Random - Not this engine):
full_name: "Budi Santoso"
first_name: "Siti"
gender: "L"
```

The engine generates a complete `Person` profile **once per row**, ensuring all name-related fields are derived from the same person.

## 📂 Project Structure

```
be/
├── cmd/
│   └── app/
│       └── main.go              # Entry point
├── internal/
│   ├── entity/
│   │   ├── config.go            # Configuration structs
│   │   └── person.go            # Person entity
│   └── generator/
│       ├── datasource.go        # Embedded CSV manager
│       ├── valuegen.go          # Value generators
│       └── generator.go         # Core engine with worker pool
├── assets/
│   ├── firstname_male.csv       # Male first names (embedded)
│   ├── firstname_female.csv     # Female first names (embedded)
│   └── lastname.csv             # Last names (embedded)
├── request-conf.json            # Sample configuration
├── Makefile                     # Build automation
├── go.mod
└── README.md
```

## 🛠️ Installation & Build

### Prerequisites
- Go 1.18+ installed
- Make (optional, for using Makefile)

### Install Dependencies
```bash
cd be
make install-deps
```

### Build

**For your current platform:**
```bash
make build
# Output: bin/generator
```

**For Linux:**
```bash
make build-linux
# Output: bin/generator-linux
```

**For Windows:**
```bash
make build-windows
# Output: bin/generator-win.exe
```

**For all platforms:**
```bash
make build-all
# Output: bin/generator-linux, bin/generator-win.exe
```

## 🚀 Usage

### 1. Configure Your Data Schema

Edit `request-conf.json`:

```json
{
  "global_settings": {
    "row_count": 1000000,
    "file_name": "data_transaksi_2024",
    "output_format": "sql",
    "platform": "linux",
    "generation_mode": "rows"
  },
  "sql_settings": {
    "dialect": "postgresql",
    "table_name": "public.transactions"
  },
  "columns": [
    { "column_name": "id", "generator_type": "uuid", "sql_type": "UUID" },
    { "column_name": "full_name", "generator_type": "full_name", "sql_type": "VARCHAR(100)" },
    { "column_name": "gender", "generator_type": "gender", "sql_type": "CHAR(1)" }
  ]
}
```

### 2. Run the Generator

```bash
./bin/generator request-conf.json
```

Or use make:
```bash
make run
```

### 3. Output

The generator will create your file (e.g., `data_transaksi_2024.sql`) with performance metrics:

```
🚀 Starting data generation...
📊 Configuration:
   - Rows: 1000000
   - Format: sql
   - Workers: 8 (CPU cores)
   - Output: data_transaksi_2024

✅ SQL file generated: data_transaksi_2024.sql

⏱️  Generation completed in: 12.5s
⚡ Throughput: 80000 rows/second

✨ Done! Your data is ready.
```

## 📋 Supported Generator Types

| Type | Description | Example Output |
|------|-------------|----------------|
| `uuid` | UUID v4 | `a1b2c3d4-e5f6-...` |
| `full_name` | Full Indonesian name | `Siti Ratna Wijaya` |
| `first_name` | First name (correlated) | `Siti` |
| `middle_name` | Middle name (optional) | `Ratna` |
| `last_name` | Last name | `Wijaya` |
| `gender` | Gender (L/P, correlated) | `P` |
| `email` | Email address | `siti@gmail.com` |
| `phone` | Indonesian phone | `081234567890` |
| `date` | Random date/timestamp | `2024-05-15 14:30:45` |
| `integer` | Random integer | `42` |
| `decimal` | Random decimal | `1234.56` |

## ⚡ Performance Features

### Worker Pool Architecture
- Automatically detects CPU cores (`runtime.NumCPU()`)
- Distributes workload evenly across workers
- Uses buffered channels for efficient communication
- Thread-safe concurrent writing

### Benchmarks (Example)
- **1M rows**: ~12 seconds (80K rows/sec)
- **10M rows**: ~2 minutes (83K rows/sec)
- *Actual performance depends on your CPU*

## 🔧 Configuration Options

### Global Settings
- `row_count`: Number of rows to generate
- `target_size_mb`: Target file size (future feature)
- `file_name`: Output file name (without extension)
- `output_format`: `csv`, `json`, or `sql`
- `platform`: `linux` or `windows`
- `generation_mode`: `rows` or `size`

### SQL Settings
- `dialect`: `postgresql`, `mysql`, or `mssql`
- `table_name`: Target table (e.g., `public.transactions`)

### Column Configuration
Each column requires:
- `column_name`: Name in output
- `generator_type`: Type of data (see table above)
- `sql_type`: SQL data type (for SQL output)
- `date_format`: Custom format (for date types)

## 🧪 Testing

```bash
make test
```

## 🧹 Cleanup

```bash
make clean
```

Removes:
- `bin/` directory
- Generated CSV/JSON/SQL files

## 📝 Example Workflows

### Generate 1M SQL rows
```bash
./bin/generator request-conf.json
```

### Generate CSV with different config
```bash
./bin/generator my-custom-config.json
```

## 🎓 How It Works

### Indonesian Name Correlation Flow

1. **Person Generation** (once per row):
   ```go
   person := GeneratePerson()
   // Randomly selects gender (L/P)
   // Picks first name based on gender
   // Picks last name
   // Optionally adds middle name
   // Constructs full name
   ```

2. **Column Mapping**:
   - When columns request `full_name`, `first_name`, or `gender`
   - All values come from the **same** `Person` instance
   - Guarantees correlation within that row

### Worker Pool Implementation

```
Main Thread
    │
    ├─► Worker 1 (rows 0-124,999)
    ├─► Worker 2 (rows 125,000-249,999)
    ├─► Worker 3 (rows 250,000-374,999)
    ├─► Worker 4 (rows 375,000-499,999)
    ├─► Worker 5 (rows 500,000-624,999)
    ├─► Worker 6 (rows 625,000-749,999)
    ├─► Worker 7 (rows 750,000-874,999)
    └─► Worker 8 (rows 875,000-999,999)
         │
         └─► Results → Buffered Channel → File Writer
```

## 🤝 Contributing

Feel free to submit issues or pull requests!

## 📄 License

MIT License - Free to use for any purpose.

## 🙋 FAQ

**Q: Can I add my own name lists?**  
A: Yes! Edit the CSV files in `assets/` and rebuild.

**Q: Does it work on macOS?**  
A: Yes, use `make build` on macOS to build for macOS.

**Q: Can I generate 100M rows?**  
A: Absolutely! Just set `row_count` to 100000000.

**Q: How do I add new generator types?**  
A: Edit `internal/generator/valuegen.go` and add your generator function.

---

**Built with ❤️ using Go's concurrency primitives**
