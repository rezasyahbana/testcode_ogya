# Quick Start Guide

## 🚀 Get Started in 3 Minutes

### Step 1: Build the Binary (30 seconds)

```bash
cd be

# Option A: Using Make (Recommended)
make build-all

# Option B: Using build script
./build.sh

# Option C: Manual build
go build -o bin/generator cmd/app/main.go
```

**Output:**
```
✅ bin/generator-linux    (Linux executable)
✅ bin/generator-win.exe  (Windows executable)
```

### Step 2: Create Your Configuration (1 minute)

Create a file called `my-config.json`:

```json
{
  "global_settings": {
    "row_count": 1000,
    "file_name": "my_data",
    "output_format": "csv",
    "platform": "linux",
    "generation_mode": "rows"
  },
  "columns": [
    {
      "column_name": "id",
      "generator_type": "uuid"
    },
    {
      "column_name": "full_name",
      "generator_type": "full_name"
    },
    {
      "column_name": "gender",
      "generator_type": "gender"
    },
    {
      "column_name": "email",
      "generator_type": "email"
    }
  ]
}
```

### Step 3: Generate Data! (1 second)

```bash
# Linux / macOS
./bin/generator my-config.json

# Windows
generator-win.exe my-config.json
```

**Output:**
```
🚀 Starting data generation...
📊 Configuration:
   - Rows: 1000
   - Format: csv
   - Workers: 12 (CPU cores)
   - Output: my_data

✅ CSV file generated: my_data.csv

⏱️  Generation completed in: 5ms
⚡ Throughput: 200,000 rows/second

✨ Done! Your data is ready.
```

---

## 📝 Common Use Cases

### Use Case 1: Generate SQL INSERT Statements

**Config: `sql-config.json`**
```json
{
  "global_settings": {
    "row_count": 10000,
    "file_name": "insert_users",
    "output_format": "sql"
  },
  "sql_settings": {
    "dialect": "postgresql",
    "table_name": "public.users"
  },
  "columns": [
    {"column_name": "id", "generator_type": "uuid", "sql_type": "UUID"},
    {"column_name": "name", "generator_type": "full_name", "sql_type": "VARCHAR(100)"},
    {"column_name": "email", "generator_type": "email", "sql_type": "VARCHAR(100)"}
  ]
}
```

**Run:**
```bash
./bin/generator sql-config.json
```

**Result:** `insert_users.sql` with 10,000 INSERT statements ready to import!

---

### Use Case 2: Generate CSV for Data Analysis

**Config: `analytics-config.json`**
```json
{
  "global_settings": {
    "row_count": 100000,
    "file_name": "customer_data",
    "output_format": "csv"
  },
  "columns": [
    {"column_name": "customer_id", "generator_type": "uuid"},
    {"column_name": "name", "generator_type": "full_name"},
    {"column_name": "gender", "generator_type": "gender"},
    {"column_name": "phone", "generator_type": "phone"},
    {"column_name": "registration_date", "generator_type": "date", "date_format": "yyyy-mm-dd"}
  ]
}
```

**Run:**
```bash
./bin/generator analytics-config.json
```

**Result:** `customer_data.csv` ready for Excel, Pandas, or any analysis tool!

---

### Use Case 3: Correlated Indonesian Names

**Important:** When you need `full_name`, `first_name`, and `gender` to match:

**Config: `correlated-names.json`**
```json
{
  "global_settings": {
    "row_count": 1000,
    "file_name": "employees",
    "output_format": "csv"
  },
  "columns": [
    {"column_name": "employee_id", "generator_type": "uuid"},
    {"column_name": "full_name", "generator_type": "full_name"},
    {"column_name": "first_name", "generator_type": "first_name"},
    {"column_name": "last_name", "generator_type": "last_name"},
    {"column_name": "gender", "generator_type": "gender"}
  ]
}
```

**Result:**
```csv
employee_id,full_name,first_name,last_name,gender
uuid-123,"Siti Ratna Wijaya","Siti","Wijaya","P"
uuid-456,"Budi Santoso","Budi","Santoso","L"
```

✅ **Guaranteed:** First name matches full name and gender!

---

## 🎯 Generator Types Reference

| Type | Description | Example |
|------|-------------|---------|
| `uuid` | Unique identifier | `a1b2c3d4-e5f6-...` |
| `full_name` | Complete Indonesian name | `Siti Ratna Wijaya` |
| `first_name` | First name (correlated) | `Siti` |
| `middle_name` | Middle name (optional) | `Ratna` |
| `last_name` | Last name | `Wijaya` |
| `gender` | L (Male) or P (Female) | `P` |
| `email` | Email address | `siti@gmail.com` |
| `phone` | Indonesian phone number | `081234567890` |
| `date` | Random date | `2024-05-15` |
| `timestamp` | Date with time | `2024-05-15 14:30:45` |
| `integer` | Random integer | `42` |
| `decimal` | Random decimal | `1234.56` |

---

## ⚡ Performance Tips

### 1. Generate Large Datasets Fast

```bash
# 1 Million rows
./bin/generator million-rows-config.json

# Expected: ~5 seconds on modern CPU
```

### 2. Optimize for Your CPU

The generator automatically uses all CPU cores. If you want to limit:

```bash
# Use only 4 cores
GOMAXPROCS=4 ./bin/generator config.json
```

### 3. Multiple Output Formats

Generate the same schema in multiple formats:

```bash
# CSV
./bin/generator config-csv.json

# SQL
./bin/generator config-sql.json

# JSON  
./bin/generator config-json.json
```

---

## 🐛 Troubleshooting

### Problem: "command not found"

**Solution:** Make sure the binary is executable:
```bash
chmod +x bin/generator
./bin/generator config.json
```

### Problem: "failed to read config file"

**Solution:** Check that your JSON is valid:
```bash
# Validate JSON
cat config.json | python3 -m json.tool
```

### Problem: Low performance

**Solution:** Check CPU usage:
```bash
# Run with verbose output (future feature)
./bin/generator config.json --verbose

# Monitor CPU
htop  # or top
```

---

## 📚 Next Steps

1. **Read the full documentation:** `README.md`
2. **Understand the architecture:** `ARCHITECTURE.md`
3. **Customize data sources:** Edit CSV files in `internal/generator/assets/`
4. **Add new generators:** Extend `internal/generator/valuegen.go`

---

## 💡 Pro Tips

### Tip 1: Copy Binaries to Production

```bash
# Copy to a server
scp bin/generator-linux user@server:/usr/local/bin/datagenerator

# Run remotely
ssh user@server 'datagenerator config.json'
```

### Tip 2: Automate with Scripts

```bash
#!/bin/bash
# generate-all.sh

./generator users-config.json
./generator orders-config.json
./generator products-config.json

echo "All datasets generated!"
```

### Tip 3: Version Control Configs

```bash
# Keep configs in Git
git add *-config.json
git commit -m "Add data generation configs"
```

---

## 🎉 You're Ready!

You now have a powerful data generator that can create millions of rows with:

✅ **Correlated Indonesian names**
✅ **Multiple output formats** (CSV, JSON, SQL)
✅ **Blazing-fast performance** (200K+ rows/sec)
✅ **Zero dependencies** (standalone binary)

**Happy Generating! 🚀**

---

**Need Help?** Check the full documentation in `README.md` and `ARCHITECTURE.md`
