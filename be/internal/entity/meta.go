package entity

type GeneratorMeta struct {
	Key         string                 `json:"key"`         // e.g., "birth_date"
	Label       string                 `json:"label"`       // e.g., "Birth Date" (Display Name)
	Category    string                 `json:"category"`    // e.g., "Personal Identity"
	Description string                 `json:"description"` // Tooltip text
	Icon        string                 `json:"icon"`        // String name of Lucide icon
	SqlType     string                 `json:"sql_type"`    // Default SQL definition
	Options     map[string]interface{} `json:"options"`     // UI hints (min, max, prefix, digits)
}

type CapabilityResponse struct {
	Generators []GeneratorMeta `json:"generators"`
	Platforms  []string        `json:"platforms"` // ["windows", "linux"]
	Formats    []string        `json:"formats"`   // ["csv", "json", "sql"]
}
