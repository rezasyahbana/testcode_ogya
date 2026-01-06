import React, { useState, useEffect, useMemo, useRef } from 'react';
import {
  Trash2,
  Plus,
  GripVertical,
  Download,
  Database,
  Settings,
  CheckCircle,
  ArrowUp,
  ArrowDown,
  X,
  Clipboard,
  AlertCircle,
  Edit3,
  Monitor,
  Terminal,
  Calculator,
  AlertTriangle,
  LayoutTemplate,
  User,
  CreditCard,
  Plane,
  Users,
  Fingerprint,
  Hash,
  Mail,
  Phone,
  Calendar,
  ToggleLeft,
  Globe,
  FileSpreadsheet,
  BookOpen,
  ChevronDown,
  Eye,
  Table as TableIcon,
  LogOut,
  IdCard, UserCheck, MapPin, Book, Heart, PhoneCall, Map, Building, Shield, Banknote, DollarSign, Coins, Percent, Activity, ArrowLeftRight, ShoppingBag, Tag, Cpu, Smartphone, Key, Palette, Lock
} from 'lucide-react';

// --- Types & Constants ---

type DataMethod = 'synthetic' | 'embedded_csv' | 'none';
type OutputFormat = 'csv' | 'json' | 'sql';
type SqlDialect = 'postgresql' | 'mysql' | 'sqlserver';
type Platform = 'windows' | 'linux';
type GenerationMode = 'rows' | 'size';

interface SchemaField {
  id: string;
  columnName: string;
  method: DataMethod;
  type: string;
  sqlType?: string;
  sourceFile?: string;
  dateFormat?: string;  // Deprecated: use options.format
  options?: Record<string, any>;  // New: flexible options for generators
}

interface GlobalSettings {
  rowCount: number;
  targetSizeMB: number;
  fileName: string;
  format: OutputFormat;
  sqlDialect: SqlDialect;
  tableName: string;
  platform: Platform;
}

// --- CONFIG ---
// Detect if running on localhost or a real IP to support VM/LAN access
const getApiBase = () => {
  const hostname = window.location.hostname;
  return `http://${hostname}:8080`;
};
const API_BASE = getApiBase();

interface TemplateField {
  columnName: string;
  key: string;
  sqlType: string;
  method?: string;
  sourceFile?: string;
  dateFormat?: string;
  options?: Record<string, any>;
}

interface Template {
  id: string;
  name: string;
  desc: string;
  icon: string;
  fields: TemplateField[];
}

// --- HELPER: UUID Generator Polyfill for insecure contexts (HTTP) ---
const generateUUID = () => {
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function (c) {
    var r = Math.random() * 16 | 0, v = c == 'x' ? r : (r & 0x3 | 0x8);
    return v.toString(16);
  });
};

// --- HELPER: MOCK DATA GENERATOR ---
const MOCK_DATA = {
  names: ["Budi Santoso", "Siti Aminah", "Agus Pratama", "Dewi Lestari", "Rina Wati", "Eko Saputra"],
  emails: ["budi@example.com", "siti.aminah@mail.com", "agus.p@work.id", "dewi@site.net", "rina.w@domain.com"],
  cities: ["Jakarta Pusat", "Surabaya", "Bandung", "Medan", "Semarang", "Makassar"],
  products: ["Laptop Gaming", "Wireless Mouse", "Mechanical Keyboard", "Monitor 24inch", "USB Hub"],
  dates: ["2023-01-15", "2023-05-20", "2023-08-10", "2023-11-05", "2023-12-25"]
};

const generateMockValue = (field: SchemaField, rowIndex: number): string | number | boolean => {
  if (field.method === 'embedded_csv') {
    if (field.sourceFile?.includes('cities')) return MOCK_DATA.cities[rowIndex % MOCK_DATA.cities.length];
    if (field.sourceFile?.includes('sku')) return `SKU-${1000 + rowIndex}`;
    return MOCK_DATA.products[rowIndex % MOCK_DATA.products.length];
  }

  switch (field.type) {
    case 'uuid':
      // Generate deterministic fake UUID for visual consistency
      return `550e8400-e29b-41d4-a716-${(446655440000 + rowIndex).toString(16)}`;
    case 'increment_id': return rowIndex + 1;
    case 'full_name': return MOCK_DATA.names[rowIndex % MOCK_DATA.names.length];
    case 'email': return MOCK_DATA.emails[rowIndex % MOCK_DATA.emails.length];
    case 'phone': return `0812${10000000 + rowIndex * 123}`;
    case 'date': return MOCK_DATA.dates[rowIndex % MOCK_DATA.dates.length];
    case 'boolean': return rowIndex % 2 === 0 ? 'true' : 'false';
    case 'ip_address': return `192.168.1.${10 + rowIndex}`;
    default: return `sample_${field.columnName}_${rowIndex}`;
  }
};


const transformSqlType = (baseSql: string, dialect: SqlDialect): string => {
  const type = baseSql.toUpperCase();

  switch (dialect) {
    case 'postgresql':
      if (type === 'UUID') return 'UUID';
      if (type === 'BOOLEAN') return 'BOOLEAN';
      return type;

    case 'mysql':
      if (type === 'UUID') return 'CHAR(36)';
      if (type === 'BOOLEAN') return 'TINYINT(1)';
      return type;

    case 'sqlserver':
      if (type === 'UUID') return 'UNIQUEIDENTIFIER';
      if (type.includes('VARCHAR')) return type.replace('VARCHAR', 'NVARCHAR');
      if (type === 'TEXT') return 'NVARCHAR(MAX)';
      if (type === 'BOOLEAN') return 'BIT';
      return type;

    default:
      return type;
  }
};

const SQL_TYPE_OPTIONS: Record<SqlDialect, string[]> = {
  postgresql: ['UUID', 'VARCHAR(255)', 'TEXT', 'INTEGER', 'BIGINT', 'BOOLEAN', 'DATE', 'TIMESTAMP', 'JSONB', 'NUMERIC'],
  mysql: ['CHAR(36)', 'VARCHAR(255)', 'TEXT', 'INT', 'BIGINT', 'TINYINT(1)', 'DATE', 'DATETIME', 'JSON', 'DECIMAL'],
  sqlserver: ['UNIQUEIDENTIFIER', 'NVARCHAR(255)', 'NVARCHAR(MAX)', 'INT', 'BIGINT', 'BIT', 'DATE', 'DATETIME2', 'DECIMAL']
};

const REFERENCE_FILES = [
  { label: 'Indonesian Cities', value: 'ref_indonesia_cities.csv' },
  { label: 'Branch Offices', value: 'ref_branches.csv' },
  { label: 'Product SKUs', value: 'ref_product_sku.csv' },
  { label: 'Department Codes', value: 'ref_dept_codes.csv' },
];

const ICON_MAP: Record<string, React.ElementType> = {
  Fingerprint, IdCard, User, UserCheck, Calendar, MapPin, Book, Heart, PhoneCall, Map, Building, Shield, Banknote, DollarSign, Coins, Percent, Activity, ArrowLeftRight, ShoppingBag, Tag, Cpu, Smartphone, Key, Palette, Lock,
  Hash, Mail, Phone, ToggleLeft, Globe, FileSpreadsheet, CreditCard, Plane, Users, Monitor
};

interface GeneratorMeta {
  key: string;
  label: string;
  category: string;
  description: string;
  icon: string;
  sql_type: string;
  options?: Record<string, any>;
}


const DOCS_HTML = `
<!DOCTYPE html>
<html lang="id">
<head>
  <meta charset="UTF-8">
  <title>Panduan Pengguna Data Generator</title>
  <style>
    body { font-family: sans-serif; background: #f8fafc; color: #1e293b; padding: 2rem; margin: 0; }
    .max-w-4xl { max-width: 56rem; margin: 0 auto; background: white; padding: 2rem; border-radius: 0.75rem; box-shadow: 0 1px 3px rgba(0,0,0,0.1); border: 1px solid #e2e8f0; }
    h1 { font-size: 1.875rem; font-weight: bold; color: #0f172a; margin-bottom: 0.5rem; }
    h3 { font-weight: bold; font-size: 1.125rem; margin-bottom: 0.75rem; display: flex; align-items: center; gap: 0.5rem; color: #1d4ed8; }
    h4 { font-weight: bold; color: #1e3a8a; margin-bottom: 0.5rem; }
    p { font-size: 0.875rem; color: #64748b; line-height: 1.6; }
    ul { list-style: disc; list-style-position: inside; margin-top: 0.25rem; margin-left: 0.5rem; color: #64748b; font-size: 0.875rem; }
    strong { font-weight: 600; }
    .text-blue-700 { color: #1d4ed8; }
    .text-slate-500 { color: #64748b; }
    .text-slate-400 { color: #94a3b8; }
    .text-blue-800 { color: #1e40af; }
    .text-blue-700-text { color: #1d4ed8; }
    .bg-blue-100 { background: #dbeafe; color: #1e40af; width: 1.5rem; height: 1.5rem; border-radius: 9999px; display: inline-flex; align-items: center; justify-content: center; font-size: 0.75rem; }
    .grid { display: grid; grid-template-columns: 1fr; gap: 2rem; margin-bottom: 2.5rem; }
    @media(min-width:768px) { .grid { grid-template-columns: repeat(2,1fr); } }
    .bg-blue-50 { background: #eff6ff; border: 1px solid #bfdbfe; border-radius: 0.5rem; padding: 1.25rem; margin-bottom: 2rem; }
    .border-b { border-bottom: 1px solid #f1f5f9; padding-bottom: 1.5rem; margin-bottom: 1.5rem; }
    .border-t { border-top: 1px solid #e2e8f0; padding-top: 1.5rem; text-center; }
    .text-center { text-align: center; }
    .text-xs { font-size: 0.75rem; }
    .text-sm { font-size: 0.875rem; }
    .text-lg { font-size: 1.125rem; }
    .text-3xl { font-size: 1.875rem; }
    .mb-2 { margin-bottom: 0.5rem; }
    .mb-3 { margin-bottom: 0.75rem; }
    .mb-6 { margin-bottom: 1.5rem; }
    .mb-8 { margin-bottom: 2rem; }
    .mb-10 { margin-bottom: 2.5rem; }
    .gap-2 { gap: 0.5rem; }
    .gap-8 { gap: 2rem; }
    .flex { display: flex; }
    .items-center { align-items: center; }
    .leading-relaxed { line-height: 1.625; }
    .rounded-lg { border-radius: 0.5rem; }
    .rounded-xl { border-radius: 0.75rem; }
    .rounded-full { border-radius: 9999px; }
    .p-5 { padding: 1.25rem; }
    .p-8 { padding: 2rem; }
    .pb-6 { padding-bottom: 1.5rem; }
    .pt-6 { padding-top: 1.5rem; }
    .border-blue-200 { border-color: #bfdbfe; }
    .border-slate-100 { border-color: #f1f5f9; }
    .border-slate-200 { border-color: #e2e8f0; }
    .shadow-lg { box-shadow: 0 10px 15px -3px rgba(0,0,0,0.1); }
  </style>
</head>
<body class="bg-slate-50 text-slate-800 font-sans p-8">
  <div class="max-w-4xl mx-auto bg-white p-8 rounded-xl shadow-lg border border-slate-200">
    
    <!-- Header -->
    <div class="border-b border-slate-100 pb-6 mb-6">
      <h1 class="text-3xl font-bold text-slate-900 mb-2">Panduan Penggunaan Data Generator</h1>
      <p class="text-slate-500">Aplikasi generator data dummy berskala besar yang cepat, fleksibel, dan 100% offline.</p>
    </div>

    <!-- Steps -->
    <div class="grid grid-cols-1 md:grid-cols-2 gap-8 mb-10">
      
      <!-- Step 1 -->
      <div>
        <h3 class="font-bold text-lg mb-3 flex items-center gap-2 text-blue-700">
          <span class="bg-blue-100 text-blue-700 w-6 h-6 rounded-full flex items-center justify-center text-xs">1</span>
          Definisikan Schema
        </h3>
        <p class="text-sm text-slate-600 leading-relaxed">
          Tambahkan kolom sesuai kebutuhan Anda. Anda dapat memberi nama kolom dan memilih tipe data (Generator).
          Tersedia template (Bank, PII, dll) untuk mempercepat proses.
        </p>
      </div>

      <!-- Step 2 -->
      <div>
        <h3 class="font-bold text-lg mb-3 flex items-center gap-2 text-blue-700">
          <span class="bg-blue-100 text-blue-700 w-6 h-6 rounded-full flex items-center justify-center text-xs">2</span>
          Konfigurasi Generator
        </h3>
        <p class="text-sm text-slate-600 leading-relaxed">
          Pilih dari berbagai jenis generator:
          <ul class="list-disc list-inside mt-1 ml-2 text-slate-500">
            <li><strong>Synthetic:</strong> Data acak logis (Nama, Email, Tanggal).</li>
            <li><strong>Embedded CSV:</strong> Data nyata dari file referensi internal (Kota, Produk).</li>
          </ul>
        </p>
      </div>

      <!-- Step 3 -->
      <div>
        <h3 class="font-bold text-lg mb-3 flex items-center gap-2 text-blue-700">
          <span class="bg-blue-100 text-blue-700 w-6 h-6 rounded-full flex items-center justify-center text-xs">3</span>
          Atur Output & Target
        </h3>
        <p class="text-sm text-slate-600 leading-relaxed">
          Tentukan jumlah baris data (rows) atau target ukuran file (MB).
          Pilih format output: <strong>CSV</strong>, <strong>JSON</strong>, atau <strong>SQL Insert</strong> (mendukung PostgreSQL, MySQL, SQLServer).
        </p>
      </div>

      <!-- Step 4 -->
      <div>
        <h3 class="font-bold text-lg mb-3 flex items-center gap-2 text-blue-700">
          <span class="bg-blue-100 text-blue-700 w-6 h-6 rounded-full flex items-center justify-center text-xs">4</span>
          Build & Jalankan
        </h3>
        <p class="text-sm text-slate-600 leading-relaxed">
          Klik tombol "Build Generator" untuk mendapatkan file eksekusi (<strong>.exe</strong> untuk Windows atau <strong>.sh</strong> untuk Linux).
          File ini dapat dijalankan di mana saja <strong>tanpa internet</strong>.
        </p>
      </div>
    </div>

    <!-- Info Box -->
    <div class="bg-blue-50 border border-blue-200 rounded-lg p-5 mb-8">
      <h4 class="font-bold text-blue-800 mb-2">Mengapa Aplikasi Ini?</h4>
      <p class="text-sm text-blue-700">
        Aplikasi ini didesain untuk menghasilkan jutaan baris data dalam hitungan detik langsung di komputer Anda. 
        Sangat cocok untuk testing database, load testing API, atau keperluan demo aplikasi tanpa mengambil resiko privasi data asli.
      </p>
    </div>

    <!-- Footer -->
    <div class="text-center border-t border-slate-100 pt-6">
      <p class="text-xs text-slate-400">Data Forge Configurator v1.3 &copy; 2024</p>
    </div>

  </div>
</body>
</html>
`;

// --- Main Application Component ---

interface DataForgeAppProps {
  onLogout?: () => void;
}

export default function DataForgeApp({ onLogout }: DataForgeAppProps = {}) {
  // --- State ---
  const [schema, setSchema] = useState<SchemaField[]>([
    { id: '1', columnName: 'transaction_id', method: 'synthetic', type: 'uuid', sqlType: 'UUID' },
    { id: '2', columnName: 'customer_name', method: 'synthetic', type: 'full_name', sqlType: 'VARCHAR(100)' },
  ]);

  const [globalSettings, setGlobalSettings] = useState<GlobalSettings>({
    rowCount: 1000000,
    targetSizeMB: 100,
    fileName: 'output',
    format: 'csv',
    sqlDialect: 'postgresql',
    tableName: 'my_table',
    platform: 'windows'
  });

  const [generationMode, setGenerationMode] = useState<GenerationMode>('rows');
  const [binaryName, setBinaryName] = useState(''); // Custom binary name
  const [isBuilding, setIsBuilding] = useState(false);
  const [buildComplete, setBuildComplete] = useState(false);

  // Drag and Drop Refs
  const dragItem = useRef<number | null>(null);
  const dragOverItem = useRef<number | null>(null);

  // Modal States
  const [activeFieldId, setActiveFieldId] = useState<string | null>(null);
  const [showTypeModal, setShowTypeModal] = useState(false);
  const [showBulkModal, setShowBulkModal] = useState(false);
  const [showTemplateModal, setShowTemplateModal] = useState(false);
  const [showPreviewModal, setShowPreviewModal] = useState(false);

  // Custom Confirm Modals
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);
  const [pendingTemplate, setPendingTemplate] = useState<Template | null>(null);

  const [bulkText, setBulkText] = useState('');
  const [errorMsg, setErrorMsg] = useState<string | null>(null);

  // --- Logic & Helpers ---

  // --- Capabilities Fetching ---
  // --- Capabilities Fetching ---
  const [capabilities, setCapabilities] = useState<GeneratorMeta[]>([]);
  const [templates, setTemplates] = useState<Template[]>([]);
  const [loadingCaps, setLoadingCaps] = useState(true);

  useEffect(() => {
    // Fetch capabilities
    fetch(`${API_BASE}/api/capabilities`)
      .then(res => res.json())
      .then(data => {
        if (data.generators) {
          setCapabilities(data.generators);
        }
      })
      .catch(err => {
        console.error("Failed to fetch capabilities:", err);
      });

    // Fetch templates
    fetch(`${API_BASE}/api/templates`)
      .then(res => res.json())
      .then(data => {
        if (Array.isArray(data)) {
          setTemplates(data);
        }
        setLoadingCaps(false);
      })
      .catch(err => {
        console.error("Failed to fetch templates:", err);
        setLoadingCaps(false);
      });
  }, []);

  const groupedCapabilities = useMemo(() => {
    const groups: Record<string, any[]> = {};
    capabilities.forEach(cap => {
      if (!groups[cap.category]) groups[cap.category] = [];
      const IconComponent = ICON_MAP[cap.icon] || Hash;
      groups[cap.category].push({
        label: cap.label,
        value: cap.key,
        method: 'synthetic',
        icon: IconComponent,
        desc: cap.description,
        size: 20,
        genericSql: cap.sql_type,
        options: cap.options
      });
    });
    return Object.keys(groups).map(g => ({ group: g, options: groups[g] }));
  }, [capabilities]);

  // Helper moved inside to access capabilities
  const getGenericSqlType = (generatorType: string): string => {
    const cap = capabilities.find(c => c.key === generatorType);
    return cap ? cap.sql_type : 'VARCHAR(255)';
  };

  const estimatedRowSizeBytes = useMemo(() => {
    let size = 0;
    schema.forEach(_ => {
      let fieldSize = 15;
      // Default estimation
      size += fieldSize;
    });
    size += Math.max(0, schema.length - 1);
    if (globalSettings.format === 'sql') size += 50;
    return size || 1;
  }, [schema, globalSettings.format]);

  // Auto-Sync SQL Types
  useEffect(() => {
    if (globalSettings.format === 'sql') {
      setSchema(prevSchema => {
        return prevSchema.map(field => {
          const genericSql = getGenericSqlType(field.type);
          const newSqlType = transformSqlType(genericSql, globalSettings.sqlDialect);
          return { ...field, sqlType: newSqlType };
        });
      });
    }
  }, [globalSettings.sqlDialect, globalSettings.format]);


  // --- Actions ---

  const addColumn = () => {
    const generic = getGenericSqlType('none');
    const defaultSql = transformSqlType(generic, globalSettings.sqlDialect);

    const newField: SchemaField = {
      id: generateUUID(),
      columnName: `col_${schema.length + 1}`,
      method: 'synthetic',
      type: 'none',
      sqlType: defaultSql
    };
    setSchema([...schema, newField]);
    setBuildComplete(false);
  };

  const handleOpenDocs = () => {
    const blob = new Blob([DOCS_HTML], { type: 'text/html' });
    const url = URL.createObjectURL(blob);
    window.open(url, '_blank');
  };

  const handleTemplateClick = (template: Template) => {
    setPendingTemplate(template);
  };

  const confirmApplyTemplate = () => {
    if (!pendingTemplate) return;
    const newFields: SchemaField[] = pendingTemplate.fields.map(tf => {
      let adjustedSqlType = tf.sqlType || 'VARCHAR(255)';
      adjustedSqlType = transformSqlType(adjustedSqlType, globalSettings.sqlDialect);

      return {
        id: generateUUID(),
        columnName: tf.columnName || 'col',
        method: (tf.method as DataMethod) || 'synthetic',
        type: tf.key || 'none', // Map 'key' to 'type'
        sqlType: adjustedSqlType,
        dateFormat: tf.dateFormat,
        sourceFile: tf.sourceFile,
        options: tf.options
      };
    });
    setSchema(newFields);
    setPendingTemplate(null);
    setShowTemplateModal(false);
    setBuildComplete(false);
  };

  const handleRemoveAll = () => {
    setSchema([]);
    setBuildComplete(false);
    setShowDeleteConfirm(false);
  };

  const handleBulkImport = () => {
    if (!bulkText.trim()) return;
    const generic = getGenericSqlType('none');
    const defaultSql = transformSqlType(generic, globalSettings.sqlDialect);

    const columns = bulkText.split(/[\n,]+/).map(s => s.trim()).filter(s => s.length > 0);
    const newFields = columns.map(colName => ({
      id: generateUUID(),
      columnName: colName,
      method: 'none' as DataMethod,
      type: 'none',
      sqlType: defaultSql
    }));
    setSchema([...schema, ...newFields]);
    setBulkText('');
    setShowBulkModal(false);
    setBuildComplete(false);
  };

  const removeColumn = (id: string) => {
    setSchema(schema.filter(field => field.id !== id));
    setBuildComplete(false);
  };

  const updateColumn = (id: string, updates: Partial<SchemaField>) => {
    setSchema(schema.map(field => {
      if (field.id !== id) return field;
      return { ...field, ...updates };
    }));
    setBuildComplete(false);
  };

  const selectTypeForColumn = (typeValue: string) => {
    if (!activeFieldId) return;
    const foundMethod: DataMethod = 'synthetic';
    const genericSql = getGenericSqlType(typeValue);

    // Find options
    const cap = capabilities.find(c => c.key === typeValue);
    const finalSqlType = transformSqlType(genericSql, globalSettings.sqlDialect);

    updateColumn(activeFieldId, {
      type: typeValue,
      method: foundMethod,
      sqlType: finalSqlType,
      dateFormat: typeValue.includes('date') ? 'yyyy-mm-dd' : undefined,
      options: cap?.options
    });
    setShowTypeModal(false);
    setActiveFieldId(null);
  };

  const openTypeModal = (id: string) => {
    setActiveFieldId(id);
    setShowTypeModal(true);
  };

  const handleSort = () => {
    let _schema = [...schema];
    const draggedItemContent = _schema.splice(dragItem.current!, 1)[0];
    _schema.splice(dragOverItem.current!, 0, draggedItemContent);
    dragItem.current = null;
    dragOverItem.current = null;
    setSchema(_schema);
  };

  const moveColumn = (index: number, direction: 'up' | 'down') => {
    if (direction === 'up' && index === 0) return;
    if (direction === 'down' && index === schema.length - 1) return;
    const newSchema = [...schema];
    const temp = newSchema[index];
    const targetIndex = direction === 'up' ? index - 1 : index + 1;
    newSchema[index] = newSchema[targetIndex];
    newSchema[targetIndex] = temp;
    setSchema(newSchema);
  };

  const handleRowCountChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const val = e.target.value;
    const newRows = val === '' ? 0 : parseInt(val);
    setGlobalSettings(prev => ({
      ...prev,
      rowCount: newRows,
      targetSizeMB: parseFloat(((newRows * estimatedRowSizeBytes) / (1024 * 1024)).toFixed(2))
    }));
  };

  const handleSizeChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const val = e.target.value;
    const newSize = val === '' ? 0 : parseFloat(val);
    setGlobalSettings(prev => ({
      ...prev,
      targetSizeMB: newSize,
      rowCount: Math.floor((newSize * 1024 * 1024) / estimatedRowSizeBytes)
    }));
  };

  const handleDownload = async () => {
    setErrorMsg(null);
    if (globalSettings.rowCount < 1) {
      setErrorMsg("Jumlah baris harus lebih dari 0.");
      return;
    }
    if (schema.length === 0) {
      setErrorMsg("Schema tidak boleh kosong.");
      return;
    }
    if (schema.some(f => f.type === 'none')) {
      setErrorMsg("Ada kolom yang belum memiliki Generator Data (Sumber Data).");
      return;
    }

    setIsBuilding(true);
    setBuildComplete(false);

    try {
      // Build the config payload matching entity.Config structure
      const configPayload = {
        global_settings: {
          row_count: globalSettings.rowCount,
          target_size_mb: Math.round(globalSettings.targetSizeMB),
          file_name: globalSettings.fileName,
          output_format: globalSettings.format,
          platform: globalSettings.platform,
          generation_mode: generationMode
        },
        sql_settings: globalSettings.format === 'sql' ? {
          dialect: globalSettings.sqlDialect,
          table_name: globalSettings.tableName
        } : undefined,
        columns: schema.map(field => ({
          column_name: field.columnName,
          generator_type: field.type,
          sql_type: field.sqlType || undefined,
          date_format: field.dateFormat || undefined,
          source_file: field.sourceFile || undefined,
          options: field.options || undefined
        }))
      };

      // Step 1: POST to /api/build
      const buildResponse = await fetch(`${API_BASE}/api/build`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          config: configPayload,
          platform: globalSettings.platform,
          binary_name: binaryName
        })
      });

      if (!buildResponse.ok) {
        throw new Error(`Build request failed: ${buildResponse.statusText}`);
      }

      const buildData = await buildResponse.json();
      const jobId = buildData.job_id;

      // Step 2: Poll /api/poll/:job_id until completed
      const pollInterval = setInterval(async () => {
        try {
          const pollResponse = await fetch(`${API_BASE}/api/poll/${jobId}`);

          if (!pollResponse.ok) {
            clearInterval(pollInterval);
            throw new Error(`Polling failed: ${pollResponse.statusText}`);
          }

          const pollData = await pollResponse.json();

          if (pollData.status === 'completed') {
            clearInterval(pollInterval);
            setIsBuilding(false);
            setBuildComplete(true);

            // Step 3: Trigger download
            window.location.href = `${API_BASE}${pollData.download_url}`;
          } else if (pollData.status === 'failed') {
            clearInterval(pollInterval);
            throw new Error(pollData.error || 'Build failed');
          }
          // else status is still 'processing', continue polling
        } catch (error) {
          clearInterval(pollInterval);
          setIsBuilding(false);
          setErrorMsg(error instanceof Error ? error.message : 'Unknown error during polling');
        }
      }, 1000); // Poll every 1 second

    } catch (error) {
      setIsBuilding(false);
      setErrorMsg(error instanceof Error ? error.message : 'Failed to build generator');
      console.error('Build error:', error);
    }
  };

  const getCurrentTypeLabel = (typeValue: string) => {
    const cap = capabilities.find(c => c.key === typeValue);
    return cap ? cap.label : typeValue;
  };

  const isSqlMode = globalSettings.format === 'sql';

  return (
    <div className="min-h-screen bg-slate-50 text-slate-800 font-sans selection:bg-blue-100">

      {/* 1. Navbar */}
      <header className="bg-slate-900 text-white shadow-md sticky top-0 z-40">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 h-16 flex items-center justify-between">
          <div className="flex items-center gap-2">
            <Database className="text-blue-400 w-6 h-6" />
            <div>
              <h1 className="text-xl font-bold tracking-tight">Data Generator</h1>
              <span className="text-xs text-slate-400 block -mt-1">Generate data anywhere, securely.</span>
            </div>
          </div>
          <div className="flex items-center gap-3">
            <button
              onClick={handleOpenDocs}
              className="flex items-center gap-1.5 text-sm font-medium text-slate-300 hover:text-white px-3 py-1.5 rounded-lg hover:bg-slate-800 transition-colors"
            >
              <BookOpen className="w-4 h-4" />
              Panduan
            </button>
            {onLogout && (
              <button
                onClick={onLogout}
                className="flex items-center gap-1.5 text-sm font-medium text-red-300 hover:text-red-200 px-3 py-1.5 rounded-lg hover:bg-red-900/30 border border-red-800/30 hover:border-red-700/50 transition-all"
                title="Sign Out"
              >
                <LogOut className="w-4 h-4" />
                Sign Out
              </button>
            )}
            <span className="text-xs bg-slate-800 px-2 py-1 rounded text-slate-300 border border-slate-700">v1.3 Offline</span>
          </div>
        </div>
      </header>

      <main className="max-w-7xl mx-auto p-4 md:p-6 lg:p-8">
        <div className="flex flex-col lg:flex-row gap-4 md:gap-6 lg:gap-8">

          {/* 2. Left Panel: Schema Builder */}
          <div className="flex-1 space-y-4">

            {/* Header Actions */}
            <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 bg-white p-4 rounded-xl border border-slate-200 shadow-sm">
              <div>
                <h3 className="text-lg font-bold text-slate-800 flex items-center gap-2">
                  <Settings className="w-5 h-5 text-slate-500" />
                  Struktur Schema
                </h3>
                <p className="text-xs text-slate-500">Definisikan kolom dan tipe data Anda</p>
              </div>

              <div className="flex flex-wrap gap-2 w-full sm:w-auto">
                <button
                  onClick={() => setShowDeleteConfirm(true)}
                  disabled={schema.length === 0}
                  className="flex-1 sm:flex-none flex items-center justify-center px-3 py-2 text-sm font-medium text-red-600 bg-red-50 border border-red-100 rounded-xl hover:bg-red-100 transition-colors disabled:opacity-50 disabled:cursor-not-allowed shadow-sm"
                >
                  <Trash2 className="w-4 h-4 mr-2" />
                  Hapus
                </button>
                <button
                  onClick={() => setShowTemplateModal(true)}
                  className="flex-1 sm:flex-none flex items-center justify-center px-3 py-2 text-sm font-medium text-purple-700 bg-purple-50 border border-purple-200 rounded-xl hover:bg-purple-100 transition-colors shadow-sm"
                >
                  <LayoutTemplate className="w-4 h-4 mr-2" />
                  Template
                </button>
                <button
                  onClick={() => setShowPreviewModal(true)}
                  disabled={schema.length === 0}
                  className="flex-1 sm:flex-none flex items-center justify-center px-3 py-2 text-sm font-medium text-teal-700 bg-teal-50 border border-teal-200 rounded-xl hover:bg-teal-100 transition-colors shadow-sm disabled:opacity-50"
                >
                  <Eye className="w-4 h-4 mr-2" />
                  Preview
                </button>
                <button
                  onClick={() => setShowBulkModal(true)}
                  className="flex-1 sm:flex-none flex items-center justify-center px-3 py-2 text-sm font-medium text-slate-700 bg-white border border-slate-200 rounded-xl hover:bg-slate-50 transition-colors shadow-sm"
                >
                  <Clipboard className="w-4 h-4 mr-2" />
                  Paste
                </button>
                <button
                  onClick={addColumn}
                  className="flex-1 sm:flex-none flex items-center justify-center px-3 py-2 text-sm font-medium text-white bg-blue-600 rounded-xl hover:bg-blue-700 transition-colors shadow-md"
                >
                  <Plus className="w-4 h-4 mr-2" />
                  Tambah
                </button>
              </div>
            </div>

            {/* List Container */}
            <div className="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden min-h-[400px]">
              {/* Wrap table content in overflow-x-auto container */}
              <div className="overflow-x-auto">

                {/* Table Header (Responsive) */}
                {schema.length > 0 && (
                  <div className={`hidden md:grid gap-4 px-4 py-3 bg-slate-50 border-b border-slate-100 text-[10px] font-bold text-slate-500 uppercase tracking-wider items-center ${isSqlMode ? 'grid-cols-12' : 'grid-cols-12'
                    }`}>
                    <div className="col-span-1">No</div>
                    <div className={`${isSqlMode ? 'col-span-3' : 'col-span-4'}`}>Nama Kolom</div>
                    <div className={`${isSqlMode ? 'col-span-3' : 'col-span-4'}`}>Sumber Data (Generator)</div>
                    {isSqlMode && <div className="col-span-2 text-blue-700">Tipe Kolom SQL</div>}
                    <div className={`${isSqlMode ? 'col-span-2' : 'col-span-2'}`}>Konfigurasi</div>
                    <div className="col-span-1 text-right">Aksi</div>
                  </div>
                )}

                {schema.length === 0 ? (
                  <div className="h-full flex flex-col items-center justify-center p-12 text-center">
                    <div className="bg-slate-50 p-4 rounded-full mb-4">
                      <Database className="w-8 h-8 text-slate-300" />
                    </div>
                    <h4 className="text-lg font-medium text-slate-600">Schema Kosong</h4>
                    <p className="text-slate-500 mb-6 max-w-xs">Gunakan template untuk memulai dengan cepat atau tambah kolom manual.</p>
                    <div className="flex gap-2">
                      <button onClick={() => setShowTemplateModal(true)} className="text-purple-600 font-medium hover:underline text-sm hover:scale-105 transition-transform">
                        Pilih Template
                      </button>
                      <span className="text-slate-300">|</span>
                      <button onClick={addColumn} className="text-blue-600 font-medium hover:underline text-sm hover:scale-105 transition-transform">
                        Tambah Manual
                      </button>
                    </div>
                  </div>
                ) : (
                  <div className="divide-y divide-slate-100">
                    {schema.map((field, index) => (
                      <div
                        key={field.id}
                        draggable
                        onDragStart={(e) => {
                          dragItem.current = index;
                          e.dataTransfer.effectAllowed = "move";
                        }}
                        onDragEnter={() => {
                          dragOverItem.current = index;
                        }}
                        onDragEnd={handleSort}
                        onDragOver={(e) => e.preventDefault()}
                        className={`group hover:bg-slate-50 transition-colors p-3 cursor-move ${field.type === 'none' ? 'bg-orange-50/40' : ''}`}
                      >
                        <div className={`flex flex-col md:grid gap-3 items-center ${isSqlMode ? 'md:grid-cols-12' : 'md:grid-cols-12'}`}>

                          {/* 1. Drag & Index */}
                          <div className="flex items-center gap-2 col-span-1 w-full md:w-auto text-slate-400">
                            <GripVertical className="w-4 h-4 text-slate-300 group-hover:text-slate-500 transition-colors" />
                            <span className="text-xs font-mono text-slate-400 w-5">{index + 1}</span>
                            <div className="flex md:hidden ml-auto gap-1">
                              <button onClick={(e) => { e.stopPropagation(); moveColumn(index, 'up'); }} disabled={index === 0}><ArrowUp className="w-4 h-4" /></button>
                              <button onClick={(e) => { e.stopPropagation(); moveColumn(index, 'down'); }} disabled={index === schema.length - 1}><ArrowDown className="w-4 h-4" /></button>
                            </div>
                          </div>

                          {/* 2. Column Name */}
                          <div className={`${isSqlMode ? 'md:col-span-3' : 'md:col-span-4'} w-full`}>
                            <label className="md:hidden text-[10px] uppercase font-bold text-slate-400 mb-1 block">Nama Kolom</label>
                            <input
                              type="text"
                              value={field.columnName}
                              onChange={(e) => updateColumn(field.id, { columnName: e.target.value })}
                              onMouseDown={(e) => e.stopPropagation()}
                              placeholder="nama_kolom"
                              className="w-full px-3 py-2 border border-slate-200 bg-white shadow-sm rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 font-mono font-medium transition-shadow hover:shadow"
                            />
                          </div>

                          {/* 3. Generator Selection */}
                          <div className={`${isSqlMode ? 'md:col-span-3' : 'md:col-span-4'} w-full`}>
                            <label className="md:hidden text-[10px] uppercase font-bold text-slate-400 mb-1 block">Generator</label>
                            <button
                              onClick={() => openTypeModal(field.id)}
                              onMouseDown={(e) => e.stopPropagation()}
                              className={`w-full text-left px-3 py-2 border rounded-lg shadow-sm text-sm flex justify-between items-center hover:shadow transition-colors ${field.type === 'none'
                                ? 'border-orange-300 bg-orange-50 text-orange-700'
                                : 'border-slate-200 bg-white text-slate-700'
                                }`}
                            >
                              <span className="truncate flex items-center gap-2">
                                {field.type === 'none' && <AlertCircle className="w-3 h-3" />}
                                {getCurrentTypeLabel(field.type)}
                              </span>
                              <Edit3 className="w-3 h-3 opacity-40 group-hover:opacity-100 transition-opacity" />
                            </button>
                          </div>

                          {/* 4. SQL Type Selection (Conditional) */}
                          {isSqlMode && (
                            <div className="md:col-span-2 w-full">
                              <label className="md:hidden text-[10px] uppercase font-bold text-blue-500 mb-1 block">Tipe SQL</label>
                              {/* Stylized Select Wrapper */}
                              <div className="relative">
                                <select
                                  value={field.sqlType}
                                  onChange={(e) => updateColumn(field.id, { sqlType: e.target.value })}
                                  onMouseDown={(e) => e.stopPropagation()}
                                  className="w-full px-3 py-2 border border-blue-200 bg-blue-50/30 rounded-lg shadow-sm text-xs text-blue-900 font-mono focus:outline-none focus:ring-2 focus:ring-blue-500 appearance-none pr-10 transition-shadow"
                                >
                                  {SQL_TYPE_OPTIONS[globalSettings.sqlDialect].map(type => (
                                    <option key={type} value={type}>{type}</option>
                                  ))}
                                </select>
                                <ChevronDown className="w-4 h-4 text-slate-400 absolute right-3 top-1/2 -translate-y-1/2 pointer-events-none" />
                              </div>
                            </div>
                          )}

                          {/* 5. Extra Config */}
                          <div className="md:col-span-2 w-full">
                            {field.method === 'embedded_csv' && (
                              <div className="relative">
                                <select
                                  value={field.sourceFile}
                                  onChange={(e) => updateColumn(field.id, { sourceFile: e.target.value })}
                                  onMouseDown={(e) => e.stopPropagation()}
                                  className="w-full px-3 py-2 border border-slate-200 bg-white rounded-lg shadow-sm text-xs text-slate-700 focus:outline-none focus:ring-2 focus:ring-blue-500 appearance-none pr-10 transition-shadow"
                                >
                                  {REFERENCE_FILES.map(file => (
                                    <option key={file.value} value={file.value}>{file.label.split('(')[0]}</option>
                                  ))}
                                </select>
                                <ChevronDown className="w-4 h-4 text-slate-400 absolute right-3 top-1/2 -translate-y-1/2 pointer-events-none" />
                              </div>
                            )}
                            {field.type === 'date' && (
                              <div>
                                <input
                                  type="text"
                                  value={field.dateFormat}
                                  onChange={(e) => updateColumn(field.id, { dateFormat: e.target.value })}
                                  onMouseDown={(e) => e.stopPropagation()}
                                  placeholder="Format: YYYY-MM-DD"
                                  className="w-full px-3 py-2 border border-slate-200 bg-white shadow-sm rounded-lg text-xs focus:outline-none focus:ring-2 focus:ring-blue-500 font-mono transition-shadow"
                                />
                              </div>
                            )}
                          </div>

                          {/* 6. Delete Action */}
                          <div className="md:col-span-1 w-full flex justify-end">
                            <button
                              onClick={() => removeColumn(field.id)}
                              onMouseDown={(e) => e.stopPropagation()}
                              className="text-slate-400 hover:text-red-500 p-2 hover:bg-red-50 rounded-lg transition-colors hover:scale-110"
                            >
                              <Trash2 className="w-4 h-4" />
                            </button>
                          </div>

                        </div>
                      </div>
                    ))}

                    {/* Bottom Add Button */}
                    <div className="p-3 bg-slate-50 border-t border-slate-100">
                      <button
                        onClick={addColumn}
                        className="w-full py-3 border border-dashed border-slate-300 rounded-lg text-slate-500 hover:border-blue-500 hover:text-blue-600 hover:bg-white transition-all flex items-center justify-center font-medium text-sm hover:shadow-sm"
                      >
                        <Plus className="w-4 h-4 mr-2" />
                        Tambah Kolom Baru
                      </button>
                    </div>
                  </div>
                )}
              </div>
            </div>

            <div className="text-xs text-slate-400 px-2 text-right">
              Total {schema.length} Kolom &bull; Est. {estimatedRowSizeBytes} bytes/baris
            </div>

          </div>

          {/* 3. Right Panel: Control Tower */}
          <div className="w-full lg:w-80 shrink-0">
            {/* ... Right Panel Code ... */}
            <div className="space-y-4 md:space-y-6 lg:sticky lg:top-24">

              <div className="bg-white rounded-xl shadow-lg border border-slate-200 overflow-hidden hover:shadow-xl transition-shadow duration-300">
                <div className="bg-slate-50 px-5 py-3 border-b border-slate-200">
                  <h3 className="font-semibold text-slate-800 text-sm">Target Generasi</h3>
                </div>

                <div className="p-5 space-y-5">

                  {/* Generation Mode Toggle */}
                  <div className="flex bg-slate-100 p-1 rounded-lg">
                    <button
                      onClick={() => setGenerationMode('rows')}
                      className={`flex-1 py-1.5 text-xs font-bold flex items-center justify-center gap-1 rounded-lg transition-all duration-300 ${generationMode === 'rows'
                        ? 'bg-white text-blue-600 shadow-sm'
                        : 'text-slate-500 hover:text-slate-700 hover:bg-slate-200/50'
                        }`}
                    >
                      <Database className="w-3 h-3" /> By Rows
                    </button>
                    <button
                      onClick={() => setGenerationMode('size')}
                      className={`flex-1 py-1.5 text-xs font-bold flex items-center justify-center gap-1 rounded-lg transition-all duration-300 ${generationMode === 'size'
                        ? 'bg-white text-blue-600 shadow-sm'
                        : 'text-slate-500 hover:text-slate-700 hover:bg-slate-200/50'
                        }`}
                    >
                      <Calculator className="w-3 h-3" /> By Size (MB)
                    </button>
                  </div>

                  {/* Input based on Mode */}
                  {generationMode === 'rows' ? (
                    <div>
                      <label className="block text-xs font-bold text-slate-500 uppercase mb-1">
                        Jumlah Baris
                      </label>
                      <input
                        type="number"
                        min="1"
                        value={globalSettings.rowCount || ''}
                        onChange={handleRowCountChange}
                        className="w-full px-3 py-2 border border-slate-200 shadow-sm rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 transition-shadow"
                        placeholder="0"
                      />
                      <p className="text-xs text-slate-400 mt-1 text-right">
                        Est. Output: <span className="font-medium text-slate-600">{globalSettings.targetSizeMB} MB</span>
                      </p>
                    </div>
                  ) : (
                    <div>
                      <label className="block text-xs font-bold text-slate-500 uppercase mb-1">
                        Target Ukuran (MB)
                      </label>
                      <input
                        type="number"
                        min="1"
                        value={globalSettings.targetSizeMB || ''}
                        onChange={handleSizeChange}
                        className="w-full px-3 py-2 border border-slate-200 shadow-sm rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 transition-shadow"
                        placeholder="0"
                      />
                      <p className="text-xs text-slate-400 mt-1 text-right">
                        Est. Rows: <span className="font-medium text-slate-600">{globalSettings.rowCount.toLocaleString()}</span>
                      </p>
                    </div>
                  )}

                  <hr className="border-slate-100" />

                  {/* Output Configuration */}
                  <div>
                    <label className="block text-xs font-bold text-slate-500 uppercase mb-2">
                      Format Output
                    </label>
                    <div className="flex bg-slate-100 p-1 rounded-lg mb-3">
                      {(['csv', 'json', 'sql'] as OutputFormat[]).map(fmt => (
                        <button
                          key={fmt}
                          onClick={() => setGlobalSettings({ ...globalSettings, format: fmt })}
                          className={`flex-1 py-1 text-xs font-bold uppercase rounded-lg transition-all duration-200 ${globalSettings.format === fmt
                            ? 'bg-white text-blue-600 shadow-sm'
                            : 'text-slate-500 hover:text-slate-700 hover:bg-slate-200/50'
                            }`}
                        >
                          {fmt}
                        </button>
                      ))}
                    </div>

                    {/* SQL Specific Settings */}
                    {globalSettings.format === 'sql' && (
                      <div className="space-y-3 bg-slate-50 p-3 rounded-lg border border-slate-200 origin-top">
                        <div className="flex items-start gap-2 mb-2">
                          <AlertCircle className="w-4 h-4 text-blue-500 mt-0.5" />
                          <p className="text-[10px] text-blue-600 leading-tight">
                            Pilih Dialect untuk menetapkan default tipe data SQL.
                          </p>
                        </div>
                        <div>
                          <label className="block text-xs font-medium text-slate-600 mb-1">SQL Dialect</label>
                          <div className="relative">
                            <select
                              value={globalSettings.sqlDialect}
                              onChange={(e) => setGlobalSettings({ ...globalSettings, sqlDialect: e.target.value as SqlDialect })}
                              className="w-full px-3 py-2 text-xs border border-slate-300 bg-white rounded-lg shadow-sm focus:ring-2 focus:ring-blue-500 font-medium appearance-none pr-10 transition-shadow"
                            >
                              <option value="postgresql">PostgreSQL</option>
                              <option value="mysql">MySQL</option>
                              <option value="sqlserver">SQL Server</option>
                            </select>
                            <ChevronDown className="w-4 h-4 text-slate-400 absolute right-3 top-1/2 -translate-y-1/2 pointer-events-none" />
                          </div>
                        </div>
                        <div>
                          <label className="block text-xs font-medium text-slate-600 mb-1">Table Name</label>
                          <input
                            type="text"
                            value={globalSettings.tableName}
                            onChange={(e) => setGlobalSettings({ ...globalSettings, tableName: e.target.value })}
                            className="w-full px-3 py-2 text-xs border border-slate-300 bg-white rounded-lg shadow-sm focus:ring-2 focus:ring-blue-500 font-mono transition-shadow"
                          />
                        </div>
                      </div>
                    )}

                    {globalSettings.format !== 'sql' && (
                      <div className="mt-2">
                        <label className="block text-xs font-medium text-slate-500 mb-1">Base Filename</label>
                        <div className="relative">
                          <input
                            type="text"
                            value={globalSettings.fileName}
                            onChange={(e) => setGlobalSettings({ ...globalSettings, fileName: e.target.value })}
                            className="w-full pl-3 pr-10 py-2 border border-slate-200 bg-white shadow-sm rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 transition-shadow"
                          />
                          <div className="absolute inset-y-0 right-0 pr-3 flex items-center pointer-events-none text-slate-400 text-xs">
                            .{globalSettings.format}
                          </div>
                        </div>
                      </div>
                    )}
                  </div>

                  <hr className="border-slate-100" />

                  {/* Platform Selection */}
                  <div>
                    <label className="block text-xs font-bold text-slate-500 uppercase mb-2">
                      Target Platform
                    </label>
                    <div className="grid grid-cols-2 gap-2">
                      <button
                        onClick={() => setGlobalSettings({ ...globalSettings, platform: 'windows' })}
                        className={`flex items-center justify-center gap-2 py-2 px-3 border rounded-lg text-xs font-medium transition-all duration-200 active:scale-95 ${globalSettings.platform === 'windows'
                          ? 'border-blue-500 bg-blue-50 text-blue-700 ring-1 ring-blue-500 shadow-sm'
                          : 'border-slate-200 bg-white text-slate-600 hover:border-slate-300 hover:shadow-sm'
                          }`}
                      >
                        <Monitor className="w-4 h-4" /> Windows (.exe)
                      </button>
                      <button
                        onClick={() => setGlobalSettings({ ...globalSettings, platform: 'linux' })}
                        className={`flex items-center justify-center gap-2 py-2 px-3 border rounded-lg text-xs font-medium transition-all duration-200 active:scale-95 ${globalSettings.platform === 'linux'
                          ? 'border-slate-700 bg-slate-100 text-slate-900 ring-1 ring-slate-700 shadow-sm'
                          : 'border-slate-200 bg-white text-slate-600 hover:border-slate-300 hover:shadow-sm'
                          }`}
                      >
                        <Terminal className="w-4 h-4" /> Linux (.sh)
                      </button>
                    </div>

                    {/* Binary Name */}
                    <div className="mt-4">
                      <label className="block text-xs font-bold text-slate-500 uppercase tracking-wide mb-1.5 flex items-center gap-1.5">
                        <Terminal className="w-3.5 h-3.5" />
                        Binary Filename (Optional)
                      </label>
                      <input
                        type="text"
                        value={binaryName}
                        onChange={(e) => setBinaryName(e.target.value)}
                        placeholder={globalSettings.platform === 'windows' ? "e.g., my_generator (auto .exe)" : "e.g., my_generator"}
                        className="w-full pl-3 pr-4 py-2 bg-slate-50 border border-slate-200 text-slate-700 rounded-lg focus:ring-1 focus:ring-blue-500 focus:border-blue-500 transition-shadow text-sm placeholder:text-slate-400"
                      />
                    </div>
                  </div>

                  {/* Error Message */}
                  {errorMsg && (
                    <div className="p-3 bg-red-50 text-red-700 text-xs rounded-lg border border-red-200 flex items-start gap-2">
                      <AlertTriangle className="w-4 h-4 shrink-0 mt-0.5" />
                      {errorMsg}
                    </div>
                  )}

                  {/* CTA Button */}
                  <button
                    onClick={handleDownload}
                    disabled={isBuilding}
                    className={`w-full py-3 px-4 rounded-lg font-bold text-white shadow-lg transition-all transform active:scale-95 flex items-center justify-center gap-2 text-sm hover:shadow-xl ${isBuilding
                      ? 'bg-slate-400 cursor-wait'
                      : buildComplete
                        ? 'bg-green-600 hover:bg-green-700'
                        : 'bg-blue-600 hover:bg-blue-700'
                      }`}
                  >
                    {isBuilding ? (
                      <>
                        <svg className="animate-spin -ml-1 mr-2 h-4 w-4 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                          <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                          <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                        </svg>
                        Building Binary...
                      </>
                    ) : buildComplete ? (
                      <>
                        <CheckCircle className="w-4 h-4" />
                        Download {globalSettings.platform === 'windows' ? '.exe' : '.sh'}
                      </>
                    ) : (
                      <>
                        <Download className="w-4 h-4" />
                        Build Generator
                      </>
                    )}
                  </button>
                </div>
              </div>

              {/* Success Feedback */}
              {buildComplete && (
                <div className="bg-green-50 border border-green-200 rounded-lg p-4 shadow-sm">
                  <h4 className="font-bold text-green-800 flex items-center gap-2 text-sm mb-2">
                    <CheckCircle className="w-4 h-4 text-green-600" /> Build Success!
                  </h4>
                  <p className="text-xs text-green-700 mb-3">
                    File <strong>DataGenerator_Client.{globalSettings.platform === 'windows' ? 'exe' : 'sh'}</strong> siap diunduh.
                  </p>
                  <div className="bg-slate-900 rounded-lg p-3 overflow-x-auto">
                    <pre className="text-[10px] text-green-400 font-mono">
                      {JSON.stringify({
                        target: globalSettings.platform,
                        sql_dialect: globalSettings.format === 'sql' ? globalSettings.sqlDialect : 'N/A',
                        schema_preview: schema.map(s => ({
                          name: s.columnName,
                          gen: s.method,
                          sql: globalSettings.format === 'sql' ? s.sqlType : 'N/A'
                        }))
                      }, null, 2)}
                    </pre>
                  </div>
                </div>
              )}

            </div>
          </div>

        </div>
      </main>

      {/* --- MODAL: MODERN TYPE SELECTION --- */}
      {showTypeModal && (
        <div className="fixed inset-0 bg-slate-900/40 backdrop-blur-sm z-50 flex items-center justify-center p-4">
          <div className="bg-white rounded-xl shadow-2xl w-[95%] max-w-4xl max-h-[90vh] overflow-hidden flex flex-col">
            <div className="p-5 border-b border-slate-100 flex justify-between items-center bg-white z-10">
              <div>
                <h3 className="font-bold text-xl text-slate-800">Pilih Generator Data</h3>
                <p className="text-sm text-slate-500">Pilih jenis data yang ingin Anda hasilkan untuk kolom ini.</p>
              </div>
              <button onClick={() => setShowTypeModal(false)} className="p-2 hover:bg-slate-100 rounded-full text-slate-500 transition-colors"><X className="w-6 h-6" /></button>
            </div>

            <div className="overflow-y-auto p-4 md:p-6 bg-slate-50/50">
              {loadingCaps ? (
                <div className="p-10 text-center text-slate-400">Loading Capabilities...</div>
              ) : groupedCapabilities.map((group) => (
                <div key={group.group} className="mb-8 last:mb-0">
                  <h4 className="text-sm font-bold text-slate-500 uppercase tracking-wider mb-4 border-b border-slate-200 pb-2">{group.group}</h4>
                  <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
                    {group.options.map((opt) => (
                      <button
                        key={opt.value}
                        onClick={() => selectTypeForColumn(opt.value)}
                        className="flex flex-col text-left p-4 rounded-xl border border-slate-200 bg-white hover:border-blue-500 hover:shadow-md transition-all group/card relative overflow-hidden"
                      >
                        <div className="absolute top-0 right-0 p-2 opacity-0 group-hover/card:opacity-100 transition-opacity duration-300">
                          <CheckCircle className="w-5 h-5 text-blue-500" />
                        </div>
                        <div className="w-10 h-10 rounded-lg bg-blue-50 text-blue-600 flex items-center justify-center mb-3 group-hover/card:bg-blue-600 group-hover/card:text-white transition-colors duration-300">
                          {opt.icon && <opt.icon className="w-6 h-6" />}
                        </div>
                        <div className="font-bold text-slate-800 mb-1">{opt.label}</div>
                        <div className="text-xs text-slate-500 leading-relaxed mb-2">{opt.desc}</div>
                        <div className="mt-auto pt-2 border-t border-slate-50 w-full flex justify-between items-center">
                          <span className="text-[10px] font-mono text-slate-400">Size: ~{opt.size}B</span>
                          {isSqlMode && <span className="text-[10px] font-mono text-blue-600 bg-blue-50 px-1.5 py-0.5 rounded">{transformSqlType(opt.genericSql, globalSettings.sqlDialect)}</span>}
                        </div>
                        {opt.options && (
                          <div className="text-[10px] bg-slate-100 rounded px-1.5 py-0.5 mt-1 text-slate-500 line-clamp-1">
                            Opts: {Object.keys(opt.options).join(', ')}
                          </div>
                        )}
                      </button>
                    ))}
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      )}

      {/* --- MODAL: TEMPLATE SELECTION --- */}
      {showTemplateModal && (
        <div className="fixed inset-0 bg-slate-900/40 backdrop-blur-sm z-50 flex items-center justify-center p-4">
          <div className="bg-white rounded-xl shadow-2xl w-[95%] max-w-3xl flex flex-col max-h-[90vh]">
            <div className="p-5 border-b border-slate-100 flex justify-between items-center">
              <div>
                <h3 className="font-bold text-xl text-slate-800 flex items-center gap-2">
                  <LayoutTemplate className="w-6 h-6 text-purple-600" />
                  Pilih Template Schema
                </h3>
                <p className="text-sm text-slate-500">Mulai cepat dengan pola data yang umum digunakan.</p>
              </div>
              <button onClick={() => setShowTemplateModal(false)} className="p-2 hover:bg-slate-100 rounded-full text-slate-500 transition-transform"><X className="w-6 h-6" /></button>
            </div>

            <div className="p-4 md:p-6 grid grid-cols-1 md:grid-cols-2 gap-4 overflow-y-auto bg-slate-50/50">
              {templates.map((tpl) => {
                const IconComp = ICON_MAP[tpl.icon] || LayoutTemplate;
                return (
                  <button
                    key={tpl.id}
                    onClick={() => handleTemplateClick(tpl)}
                    className="flex items-start gap-4 p-5 rounded-xl border border-slate-200 bg-white hover:border-purple-500 hover:ring-1 hover:ring-purple-500 transition-all text-left group"
                  >
                    <div className="w-12 h-12 rounded-lg bg-purple-50 text-purple-600 flex items-center justify-center shrink-0 group-hover:bg-purple-600 group-hover:text-white transition-colors duration-300">
                      <IconComp className="w-6 h-6" />
                    </div>
                    <div>
                      <h4 className="font-bold text-slate-800 group-hover:text-purple-700 transition-colors">{tpl.name}</h4>
                      <p className="text-xs text-slate-500 mt-1 mb-2">{tpl.desc}</p>
                      <div className="flex flex-wrap gap-1">
                        {tpl.fields.slice(0, 3).map((f, i) => (
                          <span key={i} className="text-[10px] bg-slate-100 px-1.5 py-0.5 rounded text-slate-500 border border-slate-200">
                            {f.columnName}
                          </span>
                        ))}
                        {tpl.fields.length > 3 && <span className="text-[10px] text-slate-400">+{tpl.fields.length - 3} more</span>}
                      </div>
                    </div>
                  </button>
                )
              })}
            </div>
          </div>
        </div>
      )}

      {/* --- MODAL: PREVIEW DATA --- */}
      {showPreviewModal && (
        <div className="fixed inset-0 bg-slate-900/60 backdrop-blur-sm z-50 flex items-center justify-center p-4">
          <div className="bg-white rounded-xl shadow-2xl w-[95%] max-w-5xl flex flex-col max-h-[90vh]">
            <div className="p-5 border-b border-slate-100 flex justify-between items-center bg-white rounded-t-xl">
              <div>
                <h3 className="font-bold text-xl text-slate-800 flex items-center gap-2">
                  <TableIcon className="w-5 h-5 text-teal-600" />
                  Data Preview
                </h3>
                <p className="text-sm text-slate-500">Simulasi 5 baris data berdasarkan konfigurasi Anda.</p>
              </div>
              <button onClick={() => setShowPreviewModal(false)} className="p-2 hover:bg-slate-100 rounded-full text-slate-500 transition-transform duration-300"><X className="w-6 h-6" /></button>
            </div>

            <div className="p-4 md:p-6 overflow-auto bg-slate-50/50">
              <div className="border border-slate-200 rounded-lg overflow-x-auto bg-white shadow-sm">
                <table className="min-w-full divide-y divide-slate-200">
                  <thead className="bg-slate-50">
                    <tr>
                      <th scope="col" className="px-6 py-3 text-left text-xs font-bold text-slate-500 uppercase tracking-wider">
                        #
                      </th>
                      {schema.map(col => (
                        <th key={col.id} scope="col" className="px-6 py-3 text-left text-xs font-bold text-slate-500 uppercase tracking-wider whitespace-nowrap">
                          {col.columnName || 'Untitled'}
                          <span className="block text-[9px] font-normal text-slate-400 mt-0.5 lowercase">{col.type}</span>
                        </th>
                      ))}
                    </tr>
                  </thead>
                  <tbody className="bg-white divide-y divide-slate-200">
                    {[...Array(5)].map((_, rowIndex) => (
                      <tr key={rowIndex} className="hover:bg-slate-50 transition-colors">
                        <td className="px-6 py-3 whitespace-nowrap text-xs font-medium text-slate-400">
                          {rowIndex + 1}
                        </td>
                        {schema.map(col => (
                          <td key={col.id} className="px-6 py-3 whitespace-nowrap text-sm text-slate-700 font-mono">
                            {String(generateMockValue(col, rowIndex))}
                          </td>
                        ))}
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
              <p className="mt-4 text-xs text-center text-slate-400">
                * Data di atas hanya simulasi dummy dan bukan representasi akurat dari generator binary asli.
              </p>
            </div>

            <div className="p-4 border-t border-slate-100 bg-white rounded-b-xl flex justify-end">
              <button
                onClick={() => setShowPreviewModal(false)}
                className="px-6 py-2 bg-slate-800 text-white rounded-lg font-medium hover:bg-slate-900 transition-colors"
              >
                Tutup Preview
              </button>
            </div>
          </div>
        </div>
      )}

      {/* --- MODAL: BULK IMPORT --- */}
      {showBulkModal && (
        <div className="fixed inset-0 bg-slate-900/40 backdrop-blur-sm z-50 flex items-center justify-center p-4">
          <div className="bg-white rounded-xl shadow-2xl w-[95%] max-w-lg flex flex-col max-h-[90vh] overflow-hidden">
            <div className="p-4 border-b border-slate-100 flex justify-between items-center">
              <h3 className="font-bold text-lg text-slate-800">Bulk Add Columns</h3>
              <button onClick={() => setShowBulkModal(false)} className="p-1 hover:bg-slate-100 rounded-full transition-transform duration-300"><X className="w-5 h-5 text-slate-500" /></button>
            </div>
            <div className="p-4 md:p-6 overflow-y-auto">
              <label className="block text-sm font-medium text-slate-700 mb-2">
                Paste daftar kolom dibawah ini:
              </label>
              <div className="bg-blue-50 p-3 rounded-lg mb-3 text-xs text-blue-800 border border-blue-100">
                <strong>Tips:</strong> Pisahkan nama kolom dengan tanda koma (,) atau baris baru (Enter).
                <br />Contoh: <code>id, nama, alamat, no_telp</code>
              </div>
              <textarea
                value={bulkText}
                onChange={(e) => setBulkText(e.target.value)}
                placeholder="id, first_name, last_name..."
                className="w-full h-40 p-3 border border-slate-200 shadow-sm rounded-xl focus:ring-2 focus:ring-blue-500 focus:outline-none font-mono text-sm transition-shadow"
              />
            </div>
            <div className="p-4 border-t border-slate-100 bg-slate-50 rounded-b-xl flex justify-end gap-2">
              <button
                onClick={() => setShowBulkModal(false)}
                className="px-4 py-2 text-sm font-medium text-slate-600 hover:text-slate-800 hover:bg-slate-200 rounded-lg transition-colors"
              >
                Batal
              </button>
              <button
                onClick={handleBulkImport}
                className="px-4 py-2 text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 rounded-lg shadow-sm transition-colors hover:shadow-md"
              >
                Proses Import
              </button>
            </div>
          </div>
        </div>
      )}

      {/* --- MODAL: CONFIRM TEMPLATE --- */}
      {pendingTemplate && (
        <div className="fixed inset-0 bg-slate-900/60 backdrop-blur-sm z-[60] flex items-center justify-center p-4">
          <div className="bg-white rounded-xl shadow-2xl w-[95%] max-w-sm flex flex-col overflow-hidden">
            <div className="p-6 text-center">
              <div className="w-12 h-12 bg-purple-100 rounded-full flex items-center justify-center mx-auto mb-4">
                <LayoutTemplate className="w-6 h-6 text-purple-600" />
              </div>
              <h3 className="text-lg font-bold text-slate-800 mb-2">Terapkan Template?</h3>
              <p className="text-sm text-slate-500 mb-4">
                Schema Anda saat ini akan <strong>ditimpa</strong> dengan template <strong>"{pendingTemplate.name}"</strong>.
              </p>
            </div>
            <div className="flex border-t border-slate-100">
              <button
                onClick={() => setPendingTemplate(null)}
                className="flex-1 py-3 text-sm font-medium text-slate-600 hover:bg-slate-50 transition-colors"
              >
                Batal
              </button>
              <button
                onClick={confirmApplyTemplate}
                className="flex-1 py-3 text-sm font-medium text-purple-600 bg-purple-50 hover:bg-purple-100 transition-colors border-l border-slate-100"
              >
                Ya, Terapkan
              </button>
            </div>
          </div>
        </div>
      )}

      {/* --- MODAL: CONFIRM DELETE ALL --- */}
      {showDeleteConfirm && (
        <div className="fixed inset-0 bg-slate-900/40 backdrop-blur-sm z-50 flex items-center justify-center p-4">
          <div className="bg-white rounded-xl shadow-2xl w-[95%] max-w-sm flex flex-col overflow-hidden">
            <div className="p-6 text-center">
              <div className="w-12 h-12 bg-red-100 rounded-full flex items-center justify-center mx-auto mb-4">
                <Trash2 className="w-6 h-6 text-red-600" />
              </div>
              <h3 className="text-lg font-bold text-slate-800 mb-2">Hapus Semua Kolom?</h3>
              <p className="text-sm text-slate-500">
                Tindakan ini akan menghapus {schema.length} kolom yang sudah Anda buat. Anda tidak bisa mengembalikannya.
              </p>
            </div>
            <div className="flex border-t border-slate-100">
              <button
                onClick={() => setShowDeleteConfirm(false)}
                className="flex-1 py-3 text-sm font-medium text-slate-600 hover:bg-slate-50 transition-colors"
              >
                Batal
              </button>
              <button
                onClick={handleRemoveAll}
                className="flex-1 py-3 text-sm font-medium text-red-600 bg-red-50 hover:bg-red-100 transition-colors border-l border-slate-100"
              >
                Ya, Hapus Semua
              </button>
            </div>
          </div>
        </div>
      )}

    </div>
  );
}