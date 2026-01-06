package generator

import (
	"fmt"
	"math/rand"
	"strconv"
	"strings"
	"time"

	"github.com/google/uuid"
)

// ValueGenerator generates values for different data types
type ValueGenerator struct {
	dataSource *DataSource
	rng        *rand.Rand
}

// NewValueGenerator creates a new value generator
func NewValueGenerator(dataSource *DataSource) *ValueGenerator {
	source := rand.NewSource(time.Now().UnixNano())
	return &ValueGenerator{
		dataSource: dataSource,
		rng:        rand.New(source),
	}
}

// --- IDENTITY ---

// GenerateUUID generates a new UUID v4
func (vg *ValueGenerator) GenerateUUID() string {
	return uuid.New().String()
}

func (vg *ValueGenerator) GenerateNIK() string {
	// Simple mock NIK: 16 digits
	// Province (2) + City (2) + District (2) + Date (6) + Seq (4)
	return fmt.Sprintf("%02d%02d%02d%06d%04d",
		vg.GenerateInteger(11, 99),
		vg.GenerateInteger(1, 99),
		vg.GenerateInteger(1, 99),
		vg.GenerateInteger(10100, 711299), // Rough date part
		vg.GenerateInteger(1, 9999))
}

func (vg *ValueGenerator) GenerateMotherName() string {
	// Just use a female name
	return vg.dataSource.GeneratePerson().FullName // Simplified, ideally strictly female
}

func (vg *ValueGenerator) GenerateBirthPlace() string {
	cities := []string{"Jakarta", "Bandung", "Surabaya", "Medan", "Yogyakarta", "Semarang", "Makassar", "Denpasar"}
	return cities[vg.rng.Intn(len(cities))]
}

func (vg *ValueGenerator) GenerateReligion() string {
	religions := []string{"Islam", "Kristen", "Katolik", "Hindu", "Buddha", "Konghucu"}
	return religions[vg.rng.Intn(len(religions))]
}

func (vg *ValueGenerator) GenerateMaritalStatus() string {
	statuses := []string{"Single", "Married", "Divorced", "Widowed"}
	return statuses[vg.rng.Intn(len(statuses))]
}

// --- CONTACT & LOCATION ---

func (vg *ValueGenerator) GenerateLandline() string {
	prefixes := []string{"021", "022", "024", "031", "061"}
	return fmt.Sprintf("%s-%d", prefixes[vg.rng.Intn(len(prefixes))], vg.GenerateInteger(1000000, 9999999))
}

func (vg *ValueGenerator) GenerateFullAddress() string {
	streets := []string{"Jl. Sudirman", "Jl. Thamrin", "Jl. Gatot Subroto", "Jl. Ahmad Yani", "Jl. Diponegoro"}
	return fmt.Sprintf("%s No. %d, %s", streets[vg.rng.Intn(len(streets))], vg.GenerateInteger(1, 200), vg.GenerateBirthPlace())
}

func (vg *ValueGenerator) GenerateProvince() string {
	provinces := []string{"DKI Jakarta", "Jawa Barat", "Jawa Tengah", "Jawa Timur", "Bali", "Sumatera Utara"}
	return provinces[vg.rng.Intn(len(provinces))]
}

func (vg *ValueGenerator) GenerateCity() string {
	return vg.GenerateBirthPlace()
}

func (vg *ValueGenerator) GenerateDistrict() string {
	return "Kecamatan " + vg.GenerateBirthPlace() // Simplified
}

func (vg *ValueGenerator) GenerateSubDistrict() string {
	return "Kelurahan " + vg.GenerateBirthPlace() // Simplified
}

func (vg *ValueGenerator) GenerateZipCode() string {
	return fmt.Sprintf("%05d", vg.GenerateInteger(10000, 99999))
}

// GenerateEmail generates a random email address
func (vg *ValueGenerator) GenerateEmail(person string) string {
	domains := []string{"gmail.com", "yahoo.com", "outlook.com", "email.com", "mail.com"}
	domain := domains[vg.rng.Intn(len(domains))]
	namePart := strings.ReplaceAll(strings.ToLower(person), " ", ".")
	if namePart == "" {
		namePart = fmt.Sprintf("user%d", vg.rng.Intn(100000))
	}
	return fmt.Sprintf("%s@%s", namePart, domain)
}

// GeneratePhone generates a random Indonesian phone number
func (vg *ValueGenerator) GeneratePhone(prefix string) string {
	if prefix == "" {
		prefixes := []string{"0812", "0813", "0821", "0822", "0852", "0853", "0878"}
		prefix = prefixes[vg.rng.Intn(len(prefixes))]
	}
	suffix := vg.rng.Intn(100000000)
	return fmt.Sprintf("%s%08d", prefix, suffix)
}

// --- BANKING & FINANCIAL ---

func (vg *ValueGenerator) GenerateAccountNumber(digits int) string {
	if digits <= 0 {
		digits = 10
	}
	return vg.GenerateRandomNumber(digits)
}

func (vg *ValueGenerator) GeneratePAN() string {
	// Luhn algorithm not implemented for simplicity, just random 16 digits
	return fmt.Sprintf("4%015d", vg.rng.Int63n(1e15))
}

func (vg *ValueGenerator) GenerateCVV() string {
	return fmt.Sprintf("%03d", vg.GenerateInteger(1, 999))
}

func (vg *ValueGenerator) GenerateCardExpiry() string {
	month := vg.GenerateInteger(1, 12)
	year := vg.GenerateInteger(24, 30) // Future years
	return fmt.Sprintf("%02d/%d", month, year)
}

func (vg *ValueGenerator) GenerateIBAN() string {
	return fmt.Sprintf("ID%02d%s%010d", vg.GenerateInteger(10, 99), "BANK", vg.GenerateInteger(1000000000, 9999999999))
}

func (vg *ValueGenerator) GenerateSWIFT() string {
	banks := []string{"BCA", "BRI", "BNI", "MANDIRI"}
	bank := banks[vg.rng.Intn(len(banks))]
	return fmt.Sprintf("%sIDJA", bank)
}

func (vg *ValueGenerator) GenerateNPWP() string {
	// Format: 99.999.999.9-999.000
	return fmt.Sprintf("%02d.%03d.%03d.%d-%03d.000",
		vg.GenerateInteger(1, 99),
		vg.GenerateInteger(1, 999),
		vg.GenerateInteger(1, 999),
		vg.GenerateInteger(1, 9),
		vg.GenerateInteger(1, 999))
}

func (vg *ValueGenerator) GenerateBalance() string {
	return fmt.Sprintf("%d", vg.GenerateInteger(0, 1000000000))
}

func (vg *ValueGenerator) GenerateCurrencyCode() string {
	currencies := []string{"IDR", "USD", "EUR", "SGD", "AUD"}
	return currencies[vg.rng.Intn(len(currencies))]
}

func (vg *ValueGenerator) GenerateInterestRate() string {
	return fmt.Sprintf("%.2f", vg.GenerateDecimal(0.5, 15.0, 2))
}

func (vg *ValueGenerator) GenerateCreditScore() string {
	return fmt.Sprintf("%d", vg.GenerateInteger(300, 850))
}

// --- TRANSACTIONS ---

func (vg *ValueGenerator) GenerateTrxID() string {
	return uuid.New().String()
}

func (vg *ValueGenerator) GenerateMerchant() string {
	merchants := []string{"Tokopedia", "Shopee", "Gojek", "Grab", "Indomaret", "Alfamart", "Starbucks", "McDonalds"}
	return merchants[vg.rng.Intn(len(merchants))]
}

func (vg *ValueGenerator) GenerateMCC() string {
	return fmt.Sprintf("%04d", vg.GenerateInteger(1000, 9999))
}

func (vg *ValueGenerator) GenerateTrxType() string {
	types := []string{"DB", "CR"}
	return types[vg.rng.Intn(len(types))]
}

func (vg *ValueGenerator) GenerateRefNum() string {
	return vg.GenerateRandomNumber(12)
}

// --- TECHNICAL ---

func (vg *ValueGenerator) GenerateMACAddress() string {
	return fmt.Sprintf("%02x:%02x:%02x:%02x:%02x:%02x",
		vg.rng.Intn(256), vg.rng.Intn(256), vg.rng.Intn(256),
		vg.rng.Intn(256), vg.rng.Intn(256), vg.rng.Intn(256))
}

func (vg *ValueGenerator) GenerateUserAgent() string {
	uas := []string{
		"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36",
		"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/14.1.1 Safari/605.1.15",
		"Mozilla/5.0 (Linux; Android 10; SM-G981B) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.162 Mobile Safari/537.36",
	}
	return uas[vg.rng.Intn(len(uas))]
}

func (vg *ValueGenerator) GenerateHexColor() string {
	return fmt.Sprintf("#%06x", vg.rng.Intn(0xFFFFFF))
}

func (vg *ValueGenerator) GenerateOTP() string {
	return vg.GenerateRandomNumber(6)
}

func (vg *ValueGenerator) GeneratePassword() string {
	return "Pass" + vg.GenerateRandomNumber(4) + "!" // Simplified
}

func (vg *ValueGenerator) GenerateStatus() string {
	statuses := []string{"ACTIVE", "INACTIVE", "PENDING", "SUSPENDED", "CLOSED"}
	return statuses[vg.rng.Intn(len(statuses))]
}

// --- BASE & UTILS ---

// GenerateDate generates a random date/timestamp with support for multiple formats
func (vg *ValueGenerator) GenerateDate(format string) string {
	// Generate a random time within the last 2 years
	now := time.Now()
	randomDays := vg.rng.Intn(730) // 2 years
	randomHours := vg.rng.Intn(24)
	randomMinutes := vg.rng.Intn(60)
	randomSeconds := vg.rng.Intn(60)

	randomTime := now.AddDate(0, 0, -randomDays).
		Add(time.Duration(randomHours) * time.Hour).
		Add(time.Duration(randomMinutes) * time.Minute).
		Add(time.Duration(randomSeconds) * time.Second)

	// Parse the format and convert to Go format
	goFormat := convertDateFormat(format)
	return randomTime.Format(goFormat)
}

// GenerateTimestamp generates a timestamp with time component
func (vg *ValueGenerator) GenerateTimestamp(format string) string {
	return vg.GenerateDate(format)
}

// convertDateFormat converts custom date format to Go's time format
func convertDateFormat(format string) string {
	if format == "" {
		return "2006-01-02 15:04:05"
	}

	// Map common formats to Go's reference time
	switch format {
	case "YYYY-MM-DD", "yyyy-mm-dd":
		return "2006-01-02"
	case "YYYY/MM/DD", "yyyy/mm/dd":
		return "2006/01/02"
	case "DD-MM-YYYY", "dd-mm-yyyy":
		return "02-01-2006"
	case "DD/MM/YYYY", "dd/mm/yyyy":
		return "02/01/2006"
	case "MM/DD/YYYY", "mm/dd/yyyy":
		return "01/02/2006"
	case "YYYY-MM-DD HH:MM:ss", "yyyy-mm-dd HH:MM:ss":
		return "2006-01-02 15:04:05"
	case "YYYY/MM/DD HH:MM:ss", "yyyy/mm/dd HH:MM:ss":
		return "2006/01/02 15:04:05"
	case "ISO8601", "iso8601":
		return time.RFC3339
	default:
		// Try custom format conversion
		return convertCustomDateFormat(format)
	}
}

// convertCustomDateFormat converts custom date format to Go's time format
func convertCustomDateFormat(format string) string {
	// Custom format mapping
	// yyyy-mm-dd HH:MM:ss -> 2006-01-02 15:04:05
	goFormat := format
	goFormat = replaceAll(goFormat, "yyyy", "2006")
	goFormat = replaceAll(goFormat, "mm", "01")
	goFormat = replaceAll(goFormat, "dd", "02")
	goFormat = replaceAll(goFormat, "HH", "15")
	goFormat = replaceAll(goFormat, "MM", "04")
	goFormat = replaceAll(goFormat, "ss", "05")

	return goFormat
}

func replaceAll(s, old, new string) string {
	return strings.ReplaceAll(s, old, new)
}

// GenerateInteger generates a random integer
func (vg *ValueGenerator) GenerateInteger(min, max int) int {
	if max <= min {
		return min
	}
	return min + vg.rng.Intn(max-min+1)
}

// GenerateDecimal generates a random decimal number
func (vg *ValueGenerator) GenerateDecimal(min, max float64, decimals int) float64 {
	value := min + vg.rng.Float64()*(max-min)
	// Round to specified decimal places
	multiplier := float64(1)
	for i := 0; i < decimals; i++ {
		multiplier *= 10
	}
	return float64(int(value*multiplier)) / multiplier
}

// GenerateBoolean generates a random boolean value
func (vg *ValueGenerator) GenerateBoolean() bool {
	return vg.rng.Intn(2) == 1
}

// GenerateIPv4 generates a random IPv4 address
func (vg *ValueGenerator) GenerateIPv4() string {
	return fmt.Sprintf("%d.%d.%d.%d",
		vg.rng.Intn(256),
		vg.rng.Intn(256),
		vg.rng.Intn(256),
		vg.rng.Intn(256))
}

// GenerateSalary generates a random salary within a range
func (vg *ValueGenerator) GenerateSalary(min, max int, currency string) string {
	salary := vg.GenerateInteger(min, max)
	if currency != "" {
		return fmt.Sprintf("%s %d", currency, salary)
	}
	return fmt.Sprintf("%d", salary)
}

// GenerateRandomNumber generates a fixed-length numeric string
func (vg *ValueGenerator) GenerateRandomNumber(digits int) string {
	if digits <= 0 {
		digits = 10
	}

	// Generate random number with exact digit count
	// Use string building for large digits to avoid int64 overflow
	var sb strings.Builder
	for i := 0; i < digits; i++ {
		sb.WriteString(strconv.Itoa(vg.rng.Intn(10)))
	}
	return sb.String()
}
