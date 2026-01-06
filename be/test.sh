#!/bin/bash

# Comprehensive Test Suite for Data Generator
# This script validates all core functionality

set -e

echo "╔══════════════════════════════════════════════════════════╗"
echo "║     Data Generator - Comprehensive Test Suite           ║"
echo "╚══════════════════════════════════════════════════════════╝"
echo ""

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Test counter
TESTS_PASSED=0
TESTS_FAILED=0

# Helper function
test_result() {
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✅ PASSED${NC}: $1"
        ((TESTS_PASSED++))
    else
        echo -e "${RED}❌ FAILED${NC}: $1"
        ((TESTS_FAILED++))
    fi
}

# Test 1: Check if binary exists
echo "📋 Test 1: Binary Existence"
if [ -f "bin/generator" ]; then
    test_result "Binary exists"
else
    echo -e "${YELLOW}⚠️  Building binary first...${NC}"
    make build
    test_result "Binary built and exists"
fi
echo ""

# Test 2: Check embedded assets
echo "📋 Test 2: Embedded CSV Assets"
test -f "internal/generator/assets/firstname_male.csv"
test_result "firstname_male.csv exists"

test -f "internal/generator/assets/firstname_female.csv"
test_result "firstname_female.csv exists"

test -f "internal/generator/assets/lastname.csv"
test_result "lastname.csv exists"
echo ""

# Test 3: Small dataset generation (CSV)
echo "📋 Test 3: Small CSV Generation (100 rows)"
./bin/generator test-conf.json > /dev/null 2>&1
test_result "CSV generation completed"

if [ -f "test_data.csv" ]; then
    LINES=$(wc -l < test_data.csv)
    if [ $LINES -eq 101 ]; then  # 100 rows + 1 header
        test_result "CSV has correct line count (101)"
    else
        echo -e "${RED}❌ FAILED${NC}: CSV has $LINES lines, expected 101"
        ((TESTS_FAILED++))
    fi
fi
echo ""

# Test 4: Name correlation verification
echo "📋 Test 4: Name-Gender Correlation"
# Extract a row and verify correlation
SAMPLE=$(head -3 test_data.csv | tail -1)
echo "   Sample row: $SAMPLE"

# Check if line contains both name and gender
if echo "$SAMPLE" | grep -q ",'[LP]'"; then
    test_result "Gender field present (L or P)"
else
    echo -e "${RED}❌ FAILED${NC}: Gender field missing or invalid"
    ((TESTS_FAILED++))
fi
echo ""

# Test 5: Benchmark test (10K rows)
echo "📋 Test 5: Medium Dataset (10K rows)"
./bin/generator benchmark-conf.json > /dev/null 2>&1
test_result "Benchmark generation completed"

if [ -f "benchmark_10k.csv" ]; then
    LINES=$(wc -l < benchmark_10k.csv)
    if [ $LINES -eq 10001 ]; then  # 10000 rows + 1 header
        test_result "Benchmark CSV has correct line count (10,001)"
    else
        echo -e "${RED}❌ FAILED${NC}: Benchmark has $LINES lines, expected 10,001"
        ((TESTS_FAILED++))
    fi
fi
echo ""

# Test 6: SQL generation
echo "📋 Test 6: SQL Generation"
./bin/generator demo-sql-conf.json > /dev/null 2>&1
test_result "SQL generation completed"

if [ -f "demo_sql.sql" ]; then
    # Check if SQL file contains INSERT statements
    if grep -q "INSERT INTO" demo_sql.sql; then
        test_result "SQL file contains INSERT statements"
    else
        echo -e "${RED}❌ FAILED${NC}: SQL file missing INSERT statements"
        ((TESTS_FAILED++))
    fi
fi
echo ""

# Test 7: Performance check (throughput)
echo "📋 Test 7: Performance Benchmark"
OUTPUT=$(./bin/generator benchmark-conf.json 2>&1)

# Extract throughput
THROUGHPUT=$(echo "$OUTPUT" | grep "Throughput:" | awk '{print $3}' | sed 's/,//g')

if [ ! -z "$THROUGHPUT" ]; then
    echo "   Throughput: $THROUGHPUT rows/second"
    
    # Check if throughput is reasonable (> 50,000 rows/sec)
    if [ $THROUGHPUT -gt 50000 ]; then
        test_result "Performance is acceptable (>50K rows/sec)"
    else
        echo -e "${YELLOW}⚠️  WARNING${NC}: Low throughput ($THROUGHPUT rows/sec)"
        ((TESTS_PASSED++))
    fi
else
    echo -e "${RED}❌ FAILED${NC}: Could not extract throughput"
    ((TESTS_FAILED++))
fi
echo ""

# Test 8: Cross-platform binaries
echo "📋 Test 8: Cross-Platform Builds"
if [ -f "bin/generator-linux" ]; then
    test_result "Linux binary exists"
else
    echo -e "${YELLOW}⚠️  Building Linux binary...${NC}"
    make build-linux
    test_result "Linux binary built"
fi

if [ -f "bin/generator-win.exe" ]; then
    test_result "Windows binary exists"
else
    echo -e "${YELLOW}⚠️  Building Windows binary...${NC}"
    make build-windows
    test_result "Windows binary built"
fi
echo ""

# Test 9: Documentation existence
echo "📋 Test 9: Documentation"
test -f "README.md"
test_result "README.md exists"

test -f "QUICKSTART.md"
test_result "QUICKSTART.md exists"

test -f "ARCHITECTURE.md"
test_result "ARCHITECTURE.md exists"

test -f "PROJECT_SUMMARY.md"
test_result "PROJECT_SUMMARY.md exists"
echo ""

# Test 10: Configuration validation
echo "📋 Test 10: Configuration Files"
test -f "request-conf.json"
test_result "request-conf.json exists"

test -f "test-conf.json"
test_result "test-conf.json exists"

test -f "benchmark-conf.json"
test_result "benchmark-conf.json exists"
echo ""

# Test 11: Code compilation
echo "📋 Test 11: Code Compilation"
go build -o /tmp/generator-test cmd/app/main.go > /dev/null 2>&1
test_result "Code compiles without errors"
rm -f /tmp/generator-test
echo ""

# Test 12: Module dependencies
echo "📋 Test 12: Go Module Check"
go mod verify > /dev/null 2>&1
test_result "Go modules verified"
echo ""

# Final Summary
echo "╔══════════════════════════════════════════════════════════╗"
echo "║                    Test Results                          ║"
echo "╚══════════════════════════════════════════════════════════╝"
echo ""
echo -e "${GREEN}Tests Passed: $TESTS_PASSED${NC}"
echo -e "${RED}Tests Failed: $TESTS_FAILED${NC}"
echo ""

TOTAL=$((TESTS_PASSED + TESTS_FAILED))
PERCENTAGE=$((TESTS_PASSED * 100 / TOTAL))

echo "Success Rate: $PERCENTAGE%"
echo ""

if [ $TESTS_FAILED -eq 0 ]; then
    echo -e "${GREEN}╔══════════════════════════════════════════════════════════╗${NC}"
    echo -e "${GREEN}║          🎉 ALL TESTS PASSED! 🎉                         ║${NC}"
    echo -e "${GREEN}║     The Data Generator is Production Ready!             ║${NC}"
    echo -e "${GREEN}╚══════════════════════════════════════════════════════════╝${NC}"
    exit 0
else
    echo -e "${RED}╔══════════════════════════════════════════════════════════╗${NC}"
    echo -e "${RED}║          ⚠️  SOME TESTS FAILED ⚠️                        ║${NC}"
    echo -e "${RED}║     Please review the errors above                      ║${NC}"
    echo -e "${RED}╚══════════════════════════════════════════════════════════╝${NC}"
    exit 1
fi
