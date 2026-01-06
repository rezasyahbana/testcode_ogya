package entity

// Person represents a complete person profile with correlated attributes
// This ensures that fullname, firstname, gender, etc. are all consistent
type Person struct {
	Gender     string // L (Laki-laki/Male) or P (Perempuan/Female)
	FirstName  string
	MiddleName string
	LastName   string
	FullName   string
}

// NewPerson creates a new Person with correlated attributes
func NewPerson(gender, firstName, middleName, lastName string) *Person {
	fullName := firstName
	if middleName != "" {
		fullName += " " + middleName
	}
	if lastName != "" {
		fullName += " " + lastName
	}

	return &Person{
		Gender:     gender,
		FirstName:  firstName,
		MiddleName: middleName,
		LastName:   lastName,
		FullName:   fullName,
	}
}
