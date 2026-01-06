package generator

import (
	"fmt"
	"sort"

	"github.com/fitkhi/data-generator/internal/entity"
)

// GeneratorFunc defines the signature for all generator functions
type GeneratorFunc func(g *ValueGenerator, person *entity.Person, options map[string]interface{}) string

// Registry holds the mapping of generator keys to their functions and metadata
var (
	registry = make(map[string]GeneratorFunc)
	metaData = []entity.GeneratorMeta{}
)

// InitRegistry initializes the generator registry
// This is called automatically by init()
func init() {
	// --- IDENTITY ---
	register("uuid", "UUID / GUID", "Identity", "Unique Identifier (32 chars)", "Fingerprint", "UUID", nil,
		func(g *ValueGenerator, p *entity.Person, opts map[string]interface{}) string {
			return g.GenerateUUID()
		})
	register("nik", "NIK KTP Indonesia", "Identity", "16-digit Resident ID", "IdCard", "CHAR(16)", nil,
		func(g *ValueGenerator, p *entity.Person, opts map[string]interface{}) string {
			return g.GenerateNIK()
		})
	register("full_name", "Full Name", "Identity", "First and Last Name", "User", "VARCHAR(100)", nil,
		func(g *ValueGenerator, p *entity.Person, opts map[string]interface{}) string {
			return p.FullName
		})
	register("first_name", "First Name", "Identity", "First Name Only", "User", "VARCHAR(50)", nil,
		func(g *ValueGenerator, p *entity.Person, opts map[string]interface{}) string {
			return p.FirstName
		})
	register("last_name", "Last Name", "Identity", "Last Name Only", "User", "VARCHAR(50)", nil,
		func(g *ValueGenerator, p *entity.Person, opts map[string]interface{}) string {
			return p.LastName
		})
	register("mother_name", "Mother's Maiden Name", "Identity", "Mother's Name", "UserCheck", "VARCHAR(100)", nil,
		func(g *ValueGenerator, p *entity.Person, opts map[string]interface{}) string {
			return g.GenerateMotherName()
		})
	register("birth_date", "Birth Date", "Identity", "Date of Birth", "Calendar", "DATE", map[string]interface{}{"format": "yyyy-mm-dd"},
		func(g *ValueGenerator, p *entity.Person, opts map[string]interface{}) string {
			return g.GenerateDate(getString(opts, "format", "yyyy-mm-dd"))
		})
	// --- EXPANDED DATE & TIME ---
	register("date_iso", "Date (YYYY-MM-DD)", "Date & Time", "ISO Standard", "Calendar", "DATE", map[string]interface{}{"format": "2006-01-02"},
		func(g *ValueGenerator, p *entity.Person, opts map[string]interface{}) string {
			return g.GenerateDate(getString(opts, "format", "2006-01-02"))
		})
	register("date_us", "Date (MM/DD/YYYY)", "Date & Time", "US Format", "Calendar", "VARCHAR(10)", map[string]interface{}{"format": "01/02/2006"},
		func(g *ValueGenerator, p *entity.Person, opts map[string]interface{}) string {
			return g.GenerateDate(getString(opts, "format", "01/02/2006"))
		})
	register("date_eu", "Date (DD/MM/YYYY)", "Date & Time", "EU/ID Format", "Calendar", "VARCHAR(10)", map[string]interface{}{"format": "02/01/2006"},
		func(g *ValueGenerator, p *entity.Person, opts map[string]interface{}) string {
			return g.GenerateDate(getString(opts, "format", "02/01/2006"))
		})
	register("date_text", "Date (DD Mon YYYY)", "Date & Time", "Readable Text", "Calendar", "VARCHAR(20)", map[string]interface{}{"format": "02 Jan 2006"},
		func(g *ValueGenerator, p *entity.Person, opts map[string]interface{}) string {
			return g.GenerateDate(getString(opts, "format", "02 Jan 2006"))
		})
	register("timestamp_iso", "Timestamp (ISO8601)", "Date & Time", "ISO8601 with Offset", "Clock", "TIMESTAMP", map[string]interface{}{"format": "2006-01-02T15:04:05Z07:00"},
		func(g *ValueGenerator, p *entity.Person, opts map[string]interface{}) string {
			return g.GenerateDate(getString(opts, "format", "2006-01-02T15:04:05Z07:00"))
		})
	register("timestamp_simple", "Timestamp (Simple)", "Date & Time", "YYYY-MM-DD HH:mm:ss", "Clock", "DATETIME", map[string]interface{}{"format": "2006-01-02 15:04:05"},
		func(g *ValueGenerator, p *entity.Person, opts map[string]interface{}) string {
			return g.GenerateDate(getString(opts, "format", "2006-01-02 15:04:05"))
		})
	register("timestamp_sql", "Timestamp (SQL)", "Date & Time", "SQL Format (ms)", "Database", "TIMESTAMP", map[string]interface{}{"format": "2006-01-02 15:04:05.000"},
		func(g *ValueGenerator, p *entity.Person, opts map[string]interface{}) string {
			return g.GenerateDate(getString(opts, "format", "2006-01-02 15:04:05.000"))
		})
	register("birth_place", "Birth Place", "Identity", "City of Birth", "MapPin", "VARCHAR(50)", nil,
		func(g *ValueGenerator, p *entity.Person, opts map[string]interface{}) string {
			return g.GenerateBirthPlace()
		})
	register("gender", "Gender", "Identity", "Male/Female", "Users", "CHAR(1)", nil,
		func(g *ValueGenerator, p *entity.Person, opts map[string]interface{}) string {
			return p.Gender
		})
	register("religion", "Religion", "Identity", "Religion", "Book", "VARCHAR(20)", nil,
		func(g *ValueGenerator, p *entity.Person, opts map[string]interface{}) string {
			return g.GenerateReligion()
		})
	register("marital_status", "Marital Status", "Identity", "Marital Status", "Heart", "VARCHAR(20)", nil,
		func(g *ValueGenerator, p *entity.Person, opts map[string]interface{}) string {
			return g.GenerateMaritalStatus()
		})

	// --- CONTACT & LOCATION ---
	register("email", "Email Address", "Contact & Location", "Email Address", "Mail", "VARCHAR(100)", nil,
		func(g *ValueGenerator, p *entity.Person, opts map[string]interface{}) string {
			return g.GenerateEmail(p.FirstName)
		})
	register("phone", "Phone +62", "Contact & Location", "Phone Number", "Phone", "VARCHAR(20)", map[string]interface{}{"prefix": "08"},
		func(g *ValueGenerator, p *entity.Person, opts map[string]interface{}) string {
			return g.GeneratePhone(getString(opts, "prefix", ""))
		})
	register("landline", "Home Phone", "Contact & Location", "Landline Number", "PhoneCall", "VARCHAR(20)", nil,
		func(g *ValueGenerator, p *entity.Person, opts map[string]interface{}) string {
			return g.GenerateLandline()
		})
	register("full_address", "Full Address", "Contact & Location", "Complete Address", "Map", "TEXT", nil,
		func(g *ValueGenerator, p *entity.Person, opts map[string]interface{}) string {
			return g.GenerateFullAddress()
		})
	register("province", "Province", "Contact & Location", "Province Name", "MapPin", "VARCHAR(50)", nil,
		func(g *ValueGenerator, p *entity.Person, opts map[string]interface{}) string {
			return g.GenerateProvince()
		})
	register("city", "City", "Contact & Location", "City Name", "Building", "VARCHAR(50)", nil,
		func(g *ValueGenerator, p *entity.Person, opts map[string]interface{}) string {
			return g.GenerateCity()
		})
	register("district", "Kecamatan", "Contact & Location", "District", "Map", "VARCHAR(50)", nil,
		func(g *ValueGenerator, p *entity.Person, opts map[string]interface{}) string {
			return g.GenerateDistrict()
		})
	register("sub_district", "Kelurahan", "Contact & Location", "Sub District", "Map", "VARCHAR(50)", nil,
		func(g *ValueGenerator, p *entity.Person, opts map[string]interface{}) string {
			return g.GenerateSubDistrict()
		})
	register("zip_code", "Postal Code", "Contact & Location", "Zip Code", "Hash", "CHAR(5)", nil,
		func(g *ValueGenerator, p *entity.Person, opts map[string]interface{}) string {
			return g.GenerateZipCode()
		})

	// --- BANKING & FINANCIAL ---
	register("account_number", "Bank Account Number", "Banking & QA Focus", "Bank Account No", "CreditCard", "VARCHAR(20)", map[string]interface{}{"digits": 10},
		func(g *ValueGenerator, p *entity.Person, opts map[string]interface{}) string {
			return g.GenerateAccountNumber(getInt(opts, "digits", 10))
		})
	register("pan", "Credit Card PAN", "Banking & QA Focus", "16-digit Card Number", "CreditCard", "VARCHAR(16)", nil,
		func(g *ValueGenerator, p *entity.Person, opts map[string]interface{}) string {
			return g.GeneratePAN()
		})
	register("cvv", "CVV/CVC", "Banking & QA Focus", "Security Code", "Shield", "CHAR(3)", nil,
		func(g *ValueGenerator, p *entity.Person, opts map[string]interface{}) string {
			return g.GenerateCVV()
		})
	register("card_expiry", "Card Expiry MM/YY", "Banking & QA Focus", "Expiration Date", "Calendar", "CHAR(5)", nil,
		func(g *ValueGenerator, p *entity.Person, opts map[string]interface{}) string {
			return g.GenerateCardExpiry()
		})
	register("iban", "IBAN", "Banking & QA Focus", "Intl Bank Account No", "Globe", "VARCHAR(34)", nil,
		func(g *ValueGenerator, p *entity.Person, opts map[string]interface{}) string {
			return g.GenerateIBAN()
		})
	register("swift", "SWIFT Code", "Banking & QA Focus", "Bank SWIFT/BIC", "Globe", "VARCHAR(11)", nil,
		func(g *ValueGenerator, p *entity.Person, opts map[string]interface{}) string {
			return g.GenerateSWIFT()
		})
	register("npwp", "NPWP Tax ID", "Banking & QA Focus", "Indonesian Tax ID", "FileText", "VARCHAR(20)", nil,
		func(g *ValueGenerator, p *entity.Person, opts map[string]interface{}) string {
			return g.GenerateNPWP()
		})
	register("salary", "Monthly Salary", "Banking & QA Focus", "Salary Range", "Banknote", "VARCHAR(20)", map[string]interface{}{"min": 3000000, "max": 20000000, "currency": "IDR"},
		func(g *ValueGenerator, p *entity.Person, opts map[string]interface{}) string {
			return g.GenerateSalary(getInt(opts, "min", 3000000), getInt(opts, "max", 20000000), getString(opts, "currency", "IDR"))
		})
	register("balance", "Account Balance", "Banking & QA Focus", "Account Balance", "DollarSign", "BIGINT", nil,
		func(g *ValueGenerator, p *entity.Person, opts map[string]interface{}) string {
			return g.GenerateBalance()
		})
	register("currency_code", "Currency Code", "Banking & QA Focus", "ISO Currency Code", "Coins", "CHAR(3)", nil,
		func(g *ValueGenerator, p *entity.Person, opts map[string]interface{}) string {
			return g.GenerateCurrencyCode()
		})
	register("interest_rate", "Interest Rate %", "Banking & QA Focus", "Interest Rate", "Percent", "DECIMAL(5,2)", nil,
		func(g *ValueGenerator, p *entity.Person, opts map[string]interface{}) string {
			return g.GenerateInterestRate()
		})
	register("credit_score", "Credit Score", "Banking & QA Focus", "Credit Score (300-850)", "Activity", "INT", nil,
		func(g *ValueGenerator, p *entity.Person, opts map[string]interface{}) string {
			return g.GenerateCreditScore()
		})

	// --- TRANSACTIONS ---
	register("trx_id", "Transaction ID", "Transactions", "Unique Trx ID", "Hash", "UUID", nil,
		func(g *ValueGenerator, p *entity.Person, opts map[string]interface{}) string {
			return g.GenerateTrxID()
		})
	register("trx_date", "Transaction Date", "Transactions", "Date of Transaction", "Calendar", "TIMESTAMP", nil,
		func(g *ValueGenerator, p *entity.Person, opts map[string]interface{}) string {
			return g.GenerateTimestamp(getString(opts, "format", "yyyy-mm-dd HH:MM:ss"))
		})
	register("trx_amount", "Trx Amount", "Transactions", "Amount", "DollarSign", "DECIMAL(15,2)", nil,
		func(g *ValueGenerator, p *entity.Person, opts map[string]interface{}) string {
			return g.GenerateBalance() // Similar range
		})
	register("trx_type", "Trx Type Dr/Cr", "Transactions", "Debit / Credit", "ArrowLeftRight", "CHAR(2)", nil,
		func(g *ValueGenerator, p *entity.Person, opts map[string]interface{}) string {
			return g.GenerateTrxType()
		})
	register("merchant", "Merchant Name", "Transactions", "Merchant/Shop Name", "ShoppingBag", "VARCHAR(100)", nil,
		func(g *ValueGenerator, p *entity.Person, opts map[string]interface{}) string {
			return g.GenerateMerchant()
		})
	register("mcc", "Merchant Category Code", "Transactions", "MCC Code", "Tag", "CHAR(4)", nil,
		func(g *ValueGenerator, p *entity.Person, opts map[string]interface{}) string {
			return g.GenerateMCC()
		})
	register("ref_num", "RRN", "Transactions", "Reference Number", "Hash", "VARCHAR(20)", nil,
		func(g *ValueGenerator, p *entity.Person, opts map[string]interface{}) string {
			return g.GenerateRefNum()
		})
	register("trx_desc", "Description", "Transactions", "Transaction Description", "FileText", "VARCHAR(255)", nil,
		func(g *ValueGenerator, p *entity.Person, opts map[string]interface{}) string {
			return "Payment to " + g.GenerateMerchant()
		})

	// --- TECHNICAL ---
	register("ipv4", "IP Address v4", "Technical", "IPv4 Address", "Globe", "VARCHAR(15)", nil,
		func(g *ValueGenerator, p *entity.Person, opts map[string]interface{}) string {
			return g.GenerateIPv4()
		})
	register("mac_addr", "MAC Address", "Technical", "MAC Address", "Cpu", "CHAR(17)", nil,
		func(g *ValueGenerator, p *entity.Person, opts map[string]interface{}) string {
			return g.GenerateMACAddress()
		})
	register("user_agent", "User Agent", "Technical", "Browser User Agent", "Monitor", "TEXT", nil,
		func(g *ValueGenerator, p *entity.Person, opts map[string]interface{}) string {
			return g.GenerateUserAgent()
		})
	register("device_id", "Device ID", "Technical", "Device Identifier", "Smartphone", "UUID", nil,
		func(g *ValueGenerator, p *entity.Person, opts map[string]interface{}) string {
			return g.GenerateUUID()
		})
	register("session_id", "Session Token", "Technical", "Session ID", "Key", "VARCHAR(64)", nil,
		func(g *ValueGenerator, p *entity.Person, opts map[string]interface{}) string {
			return g.GenerateUUID()
		})
	register("boolean", "Boolean", "Technical", "True/False", "ToggleLeft", "BOOLEAN", nil,
		func(g *ValueGenerator, p *entity.Person, opts map[string]interface{}) string {
			if g.GenerateBoolean() {
				return "true"
			}
			return "false"
		})
	register("otp", "OTP Code", "Technical", "One Time Password", "Lock", "CHAR(6)", nil,
		func(g *ValueGenerator, p *entity.Person, opts map[string]interface{}) string {
			return g.GenerateOTP()
		})
	register("password", "Password", "Technical", "Random Password", "Lock", "VARCHAR(20)", nil,
		func(g *ValueGenerator, p *entity.Person, opts map[string]interface{}) string {
			return g.GeneratePassword()
		})
	register("status", "Status", "Technical", "Status Code", "Activity", "VARCHAR(20)", nil,
		func(g *ValueGenerator, p *entity.Person, opts map[string]interface{}) string {
			return g.GenerateStatus()
		})
	register("color", "Hex Color", "Technical", "Hex Color Code", "Palette", "CHAR(7)", nil,
		func(g *ValueGenerator, p *entity.Person, opts map[string]interface{}) string {
			return g.GenerateHexColor()
		})

	// Fallback/Legacy
	register("increment_id", "Incremental ID", "Technical", "Counter", "Plus", "INT", nil,
		func(g *ValueGenerator, p *entity.Person, opts map[string]interface{}) string {
			return fmt.Sprintf("%d", g.GenerateInteger(1, 999999))
		})
	register("random_number", "Random Number", "Technical", "Fixed Length Number", "Hash", "VARCHAR", map[string]interface{}{"digits": 10},
		func(g *ValueGenerator, p *entity.Person, opts map[string]interface{}) string {
			return g.GenerateRandomNumber(getInt(opts, "digits", 10))
		})
}

// register adds a generator to the registry
func register(key, label, category, desc, icon, sqlType string, options map[string]interface{}, fn GeneratorFunc) {
	registry[key] = fn
	metaData = append(metaData, entity.GeneratorMeta{
		Key:         key,
		Label:       label,
		Category:    category,
		Description: desc,
		Icon:        icon,
		SqlType:     sqlType,
		Options:     options,
	})
}

// GetCapabilities returns the list of available generators
func GetCapabilities() entity.CapabilityResponse {
	// Sort metaData by category then label
	sort.Slice(metaData, func(i, j int) bool {
		if metaData[i].Category != metaData[j].Category {
			return metaData[i].Category < metaData[j].Category
		}
		return metaData[i].Label < metaData[j].Label
	})

	return entity.CapabilityResponse{
		Generators: metaData,
		Platforms:  []string{"windows", "linux"},
		Formats:    []string{"csv", "json", "sql"},
	}
}

// GetGenerator returns the generator function for a given key
func GetGenerator(key string) (GeneratorFunc, error) {
	if fn, ok := registry[key]; ok {
		return fn, nil
	}
	return nil, fmt.Errorf("generator not found: %s", key)
}

// GetMeta returns the metadata for a given key, useful for determining SQL types or quoting
func GetMeta(key string) *entity.GeneratorMeta {
	for _, m := range metaData {
		if m.Key == key {
			return &m
		}
	}
	return nil
}

// Helper functions for safely getting options
func getString(opts map[string]interface{}, key, def string) string {
	if opts == nil {
		return def
	}
	if v, ok := opts[key]; ok {
		if s, ok := v.(string); ok {
			return s
		}
	}
	return def
}

func getInt(opts map[string]interface{}, key string, def int) int {
	if opts == nil {
		return def
	}
	if v, ok := opts[key]; ok {
		switch i := v.(type) {
		case int:
			return i
		case float64:
			return int(i)
		}
	}
	return def
}
