// Servicio centralizado de comunicación HTTP con el Backend Spring Boot (Puerto 9001)
// Incluye gestor reactivo de credenciales (JWT + x-api-key) e Inspector de Peticiones API en tiempo real

export const API_BASE_URL = 'http://localhost:9001';

// Credenciales semilla del administrador configurado en PPOOII_Proyecto.sql
export const DEFAULT_CREDENTIALS = {
  login: 'cg1001234567',
  password: 'Admin123*',
  apiKey: 'api-key-admin-token-secret-12345',
  fallbackToken: 'Bearer token-admin-2026'
};

// Estado global reactivo de seguridad e historial de peticiones
let securityState = {
  authMode: 'AUTHENTICATED', // 'AUTHENTICATED' | 'PUBLIC'
  token: DEFAULT_CREDENTIALS.fallbackToken,
  apiKey: DEFAULT_CREDENTIALS.apiKey,
  activeLogin: DEFAULT_CREDENTIALS.login,
  backendOnline: null
};

let apiLogs = [];
let latestLog = null;
let inspectorOpen = false;
const listeners = new Set();

function notifyListeners() {
  const snapshot = {
    securityState: { ...securityState },
    apiLogs: [...apiLogs],
    latestLog,
    inspectorOpen
  };
  listeners.forEach((cb) => cb(snapshot));
}

export function subscribeApiState(callback) {
  listeners.add(callback);
  callback({
    securityState: { ...securityState },
    apiLogs: [...apiLogs],
    latestLog,
    inspectorOpen
  });
  return () => listeners.delete(callback);
}

export function getSecurityState() {
  return { ...securityState };
}

export function updateSecurityState(partial) {
  securityState = { ...securityState, ...partial };
  notifyListeners();
}

export function setInspectorOpen(isOpen) {
  inspectorOpen = isOpen;
  notifyListeners();
}

export function clearApiLogs() {
  apiLogs = [];
  latestLog = null;
  notifyListeners();
}

/**
 * Ejecuta una petición HTTP contra el Backend y registra la telemetría completa en el Inspector API.
 * @param {string} endpoint Ruta relativa (ej. '/api/vehiculos')
 * @param {object} options { method, body, headers, forcePublic, isMultipart, responseType, description }
 */
export async function apiRequest(endpoint, options = {}) {
  const method = (options.method || 'GET').toUpperCase();
  const url = endpoint.startsWith('http') ? endpoint : `${API_BASE_URL}${endpoint}`;
  const startTime = performance.now();

  const headers = { ...(options.headers || {}) };

  // Si no es multipart, por defecto enviamos Content-Type application/json cuando hay body
  if (!options.isMultipart && options.body !== undefined && !headers['Content-Type']) {
    headers['Content-Type'] = 'application/json';
  }

  // Inyección condicional de credenciales según el interruptor global o si el servicio es público
  const useCredentials = securityState.authMode === 'AUTHENTICATED' && !options.forcePublic;
  if (useCredentials) {
    if (securityState.token && securityState.token.trim() !== '') {
      const cleanToken = securityState.token.trim().startsWith('Bearer ')
        ? securityState.token.trim()
        : `Bearer ${securityState.token.trim()}`;
      headers['Authorization'] = cleanToken;
    }
    if (securityState.apiKey && securityState.apiKey.trim() !== '') {
      headers['x-api-key'] = securityState.apiKey.trim();
    }
  }

  let fetchBody = undefined;
  let loggedRequestBody = null;

  if (options.body !== undefined && options.body !== null) {
    if (options.isMultipart && options.body instanceof FormData) {
      fetchBody = options.body;
      const formSummary = {};
      for (const [key, val] of options.body.entries()) {
        if (val instanceof File) {
          formSummary[key] = `[Archivo: ${val.name} (${val.size} bytes, ${val.type || 'application/octet-stream'})]`;
        } else {
          formSummary[key] = val;
        }
      }
      loggedRequestBody = formSummary;
    } else if (typeof options.body === 'string') {
      fetchBody = options.body;
      try {
        loggedRequestBody = JSON.parse(options.body);
      } catch {
        loggedRequestBody = options.body;
      }
    } else {
      fetchBody = JSON.stringify(options.body);
      loggedRequestBody = options.body;
    }
  }

  let responseStatus = 0;
  let responseStatusText = '';
  let responseData = null;
  let isOk = false;

  try {
    const response = await fetch(url, {
      method,
      headers,
      body: fetchBody
    });

    responseStatus = response.status;
    responseStatusText = response.statusText;
    isOk = response.ok;
    securityState.backendOnline = true;

    if (options.responseType === 'blob') {
      const blob = await response.blob();
      responseData = {
        _tipo: 'Archivo Binario Descargado',
        sizeBytes: blob.size,
        mimeType: blob.type
      };
      const durationMs = Math.round(performance.now() - startTime);
      recordLog({
        method,
        endpoint,
        url,
        status: responseStatus,
        statusText: responseStatusText,
        ok: isOk,
        durationMs,
        authModeUsed: useCredentials ? 'AUTENTICADO (Token + APIKey)' : 'PÚBLICO / SIN CREDENCIALES',
        headersSent: { ...headers },
        requestBody: loggedRequestBody,
        responseBody: responseData,
        description: options.description || `${method} ${endpoint}`
      });
      return { ok: isOk, status: responseStatus, data: blob, headers: response.headers };
    }

    const rawText = await response.text();
    if (rawText) {
      try {
        responseData = JSON.parse(rawText);
      } catch {
        responseData = { rawResponse: rawText };
      }
    } else {
      responseData = responseStatus === 204 ? { mensaje: '204 No Content — Recurso eliminado o procesado sin cuerpo de respuesta' } : null;
    }
  } catch (networkError) {
    securityState.backendOnline = false;
    responseStatus = 0;
    responseStatusText = 'Network / CORS Error';
    isOk = false;
    responseData = {
      error: 'No se pudo conectar con el servidor Backend en ' + API_BASE_URL,
      detalle: networkError.message
    };
  }

  const durationMs = Math.round(performance.now() - startTime);

  const logEntry = recordLog({
    method,
    endpoint,
    url,
    status: responseStatus,
    statusText: responseStatusText,
    ok: isOk,
    durationMs,
    authModeUsed: useCredentials ? 'AUTENTICADO (Token + APIKey)' : 'PÚBLICO / SIN CREDENCIALES',
    headersSent: { ...headers },
    requestBody: loggedRequestBody,
    responseBody: responseData,
    description: options.description || `${method} ${endpoint}`
  });

  return {
    ok: isOk,
    status: responseStatus,
    data: responseData,
    logEntry
  };
}

function recordLog(entry) {
  const fullEntry = {
    id: `${Date.now()}-${Math.random().toString(36).substring(2, 7)}`,
    timestamp: new Date().toLocaleTimeString('es-CO', { hour12: false }),
    ...entry
  };
  apiLogs = [fullEntry, ...apiLogs.slice(0, 49)];
  latestLog = fullEntry;
  notifyListeners();
  return fullEntry;
}

/**
 * Autentica contra POST /authenticate para obtener un JWT real HS512 firmado por el Backend.
 */
export async function loginAndObtainJwt(username = DEFAULT_CREDENTIALS.login, password = DEFAULT_CREDENTIALS.password, customApiKey = null) {
  if (customApiKey) {
    updateSecurityState({ apiKey: customApiKey });
  }
  const res = await apiRequest('/authenticate', {
    method: 'POST',
    body: { username, password },
    description: `Autenticación JWT (${username})`
  });

  if (res.ok && res.data && res.data.token) {
    updateSecurityState({
      authMode: 'AUTHENTICATED',
      token: res.data.token,
      activeLogin: username
    });
  }
  return res;
}

/**
 * Convierte un objeto File (.pdf) del navegador a una cadena Base64 pura (sin prefijo data:...).
 */
export function fileToBase64(file) {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.onload = () => {
      const result = String(reader.result || '');
      const base64Clean = result.includes(',') ? result.split(',')[1] : result;
      resolve(base64Clean);
    };
    reader.onerror = (err) => reject(err);
    reader.readAsDataURL(file);
  });
}

/**
 * Genera en el navegador un documento PDF real (%PDF-1.4) válido codificado en Base64
 * para pruebas inmediatas de almacenamiento LONGBLOB y previsualización en iframe.
 */
export function generateSamplePdfBase64(titulo = 'CERTIFICADO OFICIAL SOAT / LICENCIA', subtitulo = 'Proyecto Final PPOOII - Universidad de Ibague') {
  const cleanTitle = titulo.replace(/[()\\]/g, '');
  const cleanSub = subtitulo.replace(/[()\\]/g, '');
  const fecha = new Date().toISOString().slice(0, 10);

  const pdfContent = `%PDF-1.4
1 0 obj
<< /Type /Catalog /Pages 2 0 R >>
endobj
2 0 obj
<< /Type /Pages /Kids [3 0 R] /Count 1 >>
endobj
3 0 obj
<< /Type /Page /Parent 2 0 R /Resources << /Font << /F1 4 0 R >> >> /MediaBox [0 0 612 792] /Contents 5 0 R >>
endobj
4 0 obj
<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold >>
endobj
5 0 obj
<< /Length 265 >>
stream
BT
/F1 18 Tf
50 720 Td
(${cleanTitle}) Tj
/F1 12 Tf
0 -35 Td
(${cleanSub}) Tj
0 -25 Td
(Fecha de generacion: ${fecha}) Tj
0 -25 Td
(Almacenado en MySQL como LONGBLOB y codificado en Base64) Tj
0 -25 Td
(Estado de verificacion: Documento Digital Valido) Tj
ET
endstream
endobj
xref
0 6
0000000000 65535 f 
0000000009 00000 n 
0000000058 00000 n 
0000000115 00000 n 
0000000229 00000 n 
0000000297 00000 n 
trailer
<< /Size 6 /Root 1 0 R >>
startxref
614
%%EOF`;

  return btoa(pdfContent);
}

