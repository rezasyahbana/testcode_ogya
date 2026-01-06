package generator

import (
	_ "embed"
	"math/rand"
	"strings"
	"time"

	"github.com/fitkhi/data-generator/internal/entity"
)

//go:embed assets/firstname_male.csv
var firstnameMaleCSV string

//go:embed assets/firstname_female.csv
var firstnameFemaleCSV string

//go:embed assets/lastname.csv
var lastnameCSV string

// DataSource holds all embedded data sources
type DataSource struct {
	FirstNameMale   []string
	FirstNameFemale []string
	LastName        []string
	rng             *rand.Rand
}

// NewDataSource initializes the data source with embedded CSV files
func NewDataSource() *DataSource {
	// Create a new random source with current time seed
	source := rand.NewSource(time.Now().UnixNano())
	rng := rand.New(source)

	return &DataSource{
		FirstNameMale:   parseCSV(firstnameMaleCSV),
		FirstNameFemale: parseCSV(firstnameFemaleCSV),
		LastName:        parseCSV(lastnameCSV),
		rng:             rng,
	}
}

// parseCSV splits CSV content by newlines and filters empty lines
func parseCSV(content string) []string {
	lines := strings.Split(strings.TrimSpace(content), "\n")
	var result []string
	for _, line := range lines {
		trimmed := strings.TrimSpace(line)
		if trimmed != "" {
			result = append(result, trimmed)
		}
	}
	return result
}

// GeneratePerson creates a complete person profile with correlated attributes
func (ds *DataSource) GeneratePerson() *entity.Person {
	// Randomly select gender (50/50 chance)
	gender := "L"
	if ds.rng.Intn(2) == 0 {
		gender = "P"
	}

	var firstName string
	if gender == "L" {
		firstName = ds.randomElement(ds.FirstNameMale)
	} else {
		firstName = ds.randomElement(ds.FirstNameFemale)
	}

	// 30% chance of having a middle name
	var middleName string
	if ds.rng.Intn(100) < 30 {
		// Middle name can be from either first names or last names
		if ds.rng.Intn(2) == 0 {
			// Use a firstname as middle name
			if gender == "L" {
				middleName = ds.randomElement(ds.FirstNameMale)
			} else {
				middleName = ds.randomElement(ds.FirstNameFemale)
			}
		} else {
			// Use a lastname as middle name
			middleName = ds.randomElement(ds.LastName)
		}
	}

	lastName := ds.randomElement(ds.LastName)

	return entity.NewPerson(gender, firstName, middleName, lastName)
}

// randomElement returns a random element from a slice
func (ds *DataSource) randomElement(slice []string) string {
	if len(slice) == 0 {
		return ""
	}
	return slice[ds.rng.Intn(len(slice))]
}

// RandomGender returns a random gender (L or P)
func (ds *DataSource) RandomGender() string {
	if ds.rng.Intn(2) == 0 {
		return "L"
	}
	return "P"
}
