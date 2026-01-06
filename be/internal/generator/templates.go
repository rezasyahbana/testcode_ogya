package generator

// TemplateField represents a single field within a template
type TemplateField struct {
	ColumnName string                 `json:"columnName"`
	Key        string                 `json:"key"` // Maps to GeneratorMeta.Key
	SqlType    string                 `json:"sqlType"`
	Method     string                 `json:"method,omitempty"` // Default "synthetic"
	SourceFile string                 `json:"sourceFile,omitempty"`
	DateFormat string                 `json:"dateFormat,omitempty"`
	Options    map[string]interface{} `json:"options,omitempty"`
}

// Template represents a predefined schema template
type Template struct {
	ID     string          `json:"id"`
	Name   string          `json:"name"`
	Desc   string          `json:"desc"`
	Icon   string          `json:"icon"` // Lucide icon name string
	Fields []TemplateField `json:"fields"`
}

// GetTemplates returns the list of available schema templates
func GetTemplates() []Template {
	return []Template{
		{
			ID:   "pii",
			Name: "Data Pribadi (PII)",
			Desc: "Nama, Email, Telepon, Tanggal Lahir",
			Icon: "User",
			Fields: []TemplateField{
				{ColumnName: "user_id", Key: "uuid", SqlType: "UUID"},
				{ColumnName: "full_name", Key: "full_name", SqlType: "VARCHAR(100)"},
				{ColumnName: "email", Key: "email", SqlType: "VARCHAR(150)"},
				{ColumnName: "phone", Key: "phone", SqlType: "VARCHAR(20)"},
				{ColumnName: "birth_date", Key: "birth_date", SqlType: "DATE", Options: map[string]interface{}{"format": "yyyy-mm-dd"}},
			},
		},
		{
			ID:   "bank",
			Name: "Data Perbankan",
			Desc: "ID Transaksi, Akun, Saldo, Status",
			Icon: "CreditCard",
			Fields: []TemplateField{
				{ColumnName: "tx_id", Key: "uuid", SqlType: "UUID"},
				{ColumnName: "account_number", Key: "account_number", SqlType: "VARCHAR(20)"},
				{ColumnName: "customer_name", Key: "full_name", SqlType: "VARCHAR(100)"},
				{ColumnName: "tx_date", Key: "timestamp_simple", SqlType: "DATETIME"},
				{ColumnName: "is_fraud", Key: "boolean", SqlType: "BOOLEAN"},
			},
		},
		{
			ID:   "flight",
			Name: "Data Penerbangan",
			Desc: "Kode Booking, Penumpang, Tanggal",
			Icon: "Plane",
			Fields: []TemplateField{
				{ColumnName: "booking_code", Key: "uuid", SqlType: "VARCHAR(36)"},
				{ColumnName: "passenger_name", Key: "full_name", SqlType: "VARCHAR(100)"},
				{ColumnName: "flight_date", Key: "date_iso", SqlType: "DATE"},
				{ColumnName: "seat_number", Key: "increment_id", SqlType: "VARCHAR(5)"},
			},
		},
		{
			ID:   "population",
			Name: "Data Kependudukan",
			Desc: "NIK, Nama, Alamat, Status",
			Icon: "Users",
			Fields: []TemplateField{
				{ColumnName: "nik", Key: "nik", SqlType: "CHAR(16)"},
				{ColumnName: "nama_lengkap", Key: "full_name", SqlType: "VARCHAR(100)"},
				{ColumnName: "tgl_lahir", Key: "birth_date", SqlType: "DATE", Options: map[string]interface{}{"format": "yyyy-mm-dd"}},
				{ColumnName: "kota_kelahiran", Key: "city", SqlType: "VARCHAR(50)"},
				{ColumnName: "status_aktif", Key: "boolean", SqlType: "BOOLEAN"},
			},
		},
	}
}
