import React, { useState } from 'react';
import {
  ShieldCheck,
  ShieldOff,
  KeyRound,
  Terminal,
  RefreshCw,
  Lock,
  Unlock,
  CheckCircle2,
  AlertTriangle,
  Sparkles,
  ClipboardCheck
} from 'lucide-react';
import {
  updateSecurityState,
  loginAndObtainJwt,
  setInspectorOpen,
  DEFAULT_CREDENTIALS,
  apiRequest
} from '../services/api';

export default function HeaderSecurityBar({
  securityState,
  latestLog,
  inspectorOpen,
  logsCount,
  onOpenChecklist,
  completedChecksCount,
  totalChecksCount
}) {
  const [authenticating, setAuthenticating] = useState(false);
  const [showCredentialsEditor, setShowCredentialsEditor] = useState(false);

  const isAuthMode = securityState.authMode === 'AUTHENTICATED';

  const handleToggleAuthMode = () => {
    updateSecurityState({
      authMode: isAuthMode ? 'PUBLIC' : 'AUTHENTICATED'
    });
  };

  const handleQuickJwtLogin = async () => {
    setAuthenticating(true);
    try {
      await loginAndObtainJwt(
        securityState.activeLogin || DEFAULT_CREDENTIALS.login,
        DEFAULT_CREDENTIALS.password,
        securityState.apiKey || DEFAULT_CREDENTIALS.apiKey
      );
    } finally {
      setAuthenticating(false);
    }
  };

  const handleRestoreAdmin = async () => {
    setAuthenticating(true);
    try {
      updateSecurityState({
        authMode: 'AUTHENTICATED',
        apiKey: DEFAULT_CREDENTIALS.apiKey,
        activeLogin: DEFAULT_CREDENTIALS.login,
        token: DEFAULT_CREDENTIALS.fallbackToken
      });
      await loginAndObtainJwt(
        DEFAULT_CREDENTIALS.login,
        DEFAULT_CREDENTIALS.password,
        DEFAULT_CREDENTIALS.apiKey
      );
    } finally {
      setAuthenticating(false);
    }
  };

  const handleTestUnauthorized401 = async () => {
    // Dispara una petición intencionalmente sin encabezados a un endpoint protegido para demostrar el rechazo 401
    await apiRequest('/api/vehiculos', {
      method: 'GET',
      forcePublic: true,
      description: 'Prueba en vivo: Petición sin credenciales a endpoint protegido (/api/vehiculos)'
    });
    setInspectorOpen(true);
  };

  return (
    <header className="sticky top-0 z-40 bg-slate-900/95 backdrop-blur-md border-b border-slate-800 shadow-lg">
      <div className="max-w-[1600px] mx-auto px-4 py-2.5">
        <div className="flex flex-wrap items-center justify-between gap-3">
          {/* Identidad del Sistema */}
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-xl bg-gradient-to-br from-indigo-500 to-violet-600 flex items-center justify-center shadow-md shadow-indigo-500/20">
              <ShieldCheck className="w-6 h-6 text-white" />
            </div>
            <div>
              <div className="flex items-center gap-2">
                <h1 className="text-sm md:text-base font-bold tracking-tight text-white">
                  Plataforma de Operación y Sustentación PPOOII
                </h1>
                <span className="hidden sm:inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-[11px] font-semibold bg-indigo-500/15 text-indigo-300 border border-indigo-500/30">
                  Spring Boot + MySQL :9001
                </span>
              </div>
              <p className="text-xs text-slate-400">
                Auditoría interactiva de Entregas 1, 2, 3 y 4 • Validación en vivo de restricciones <code className="text-indigo-300">CHECK</code>, <code className="text-indigo-300">LONGBLOB</code>, <code className="text-indigo-300">@Scheduled</code> y Procedimientos Almacenados
              </p>
            </div>
          </div>

          {/* Controles Globales de Seguridad e Inspector */}
          <div className="flex flex-wrap items-center gap-2">
            {/* Botón Guía de Sustentación */}
            <button
              onClick={onOpenChecklist}
              className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-semibold bg-amber-500/15 hover:bg-amber-500/25 text-amber-300 border border-amber-500/30 transition"
              title="Abrir checklist interactivo de sustentación académica"
            >
              <ClipboardCheck className="w-4 h-4" />
              <span>Guía Sustentación ({completedChecksCount}/{totalChecksCount})</span>
            </button>

            {/* Interruptor Rápido: Modo Autenticado vs Modo Público / Sin Credenciales */}
            <button
              onClick={handleToggleAuthMode}
              className={`flex items-center gap-2 px-3.5 py-1.5 rounded-lg text-xs font-bold transition shadow-sm border ${
                isAuthMode
                  ? 'bg-emerald-500/15 hover:bg-emerald-500/25 text-emerald-300 border-emerald-500/40'
                  : 'bg-rose-500/20 hover:bg-rose-500/30 text-rose-300 border-rose-500/50 animate-pulse'
              }`}
              title="Alternar entre envío automático de Token + APIKey o peticiones sin credenciales"
            >
              {isAuthMode ? (
                <>
                  <Lock className="w-3.5 h-3.5 text-emerald-400" />
                  <span>Modo Autenticado (Token + APIKey)</span>
                </>
              ) : (
                <>
                  <Unlock className="w-3.5 h-3.5 text-rose-400" />
                  <span>Modo Público / Sin Credenciales</span>
                </>
              )}
            </button>

            {/* Botón para probar rechazo 401 inmediato */}
            <button
              onClick={handleTestUnauthorized401}
              className="hidden xl:flex items-center gap-1.5 px-2.5 py-1.5 rounded-lg text-xs font-medium bg-slate-800 hover:bg-rose-950/60 text-slate-300 hover:text-rose-300 border border-slate-700 hover:border-rose-500/40 transition"
              title="Disparar GET /api/vehiculos sin Token ni APIKey para demostrar bloqueo HTTP 401"
            >
              <ShieldOff className="w-3.5 h-3.5 text-rose-400" />
              <span>Demostrar 401</span>
            </button>

            {/* Botón para desplegar/ocultar editor de Token y APIKey */}
            <button
              onClick={() => setShowCredentialsEditor(!showCredentialsEditor)}
              className={`flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-medium border transition ${
                showCredentialsEditor
                  ? 'bg-indigo-600 text-white border-indigo-500'
                  : 'bg-slate-800 hover:bg-slate-700 text-slate-200 border-slate-700'
              }`}
            >
              <KeyRound className="w-3.5 h-3.5" />
              <span>Credenciales ({securityState.activeLogin})</span>
            </button>

            {/* Botón Inspector de Respuestas API */}
            <button
              onClick={() => setInspectorOpen(!inspectorOpen)}
              className={`flex items-center gap-2 px-3 py-1.5 rounded-lg text-xs font-semibold border transition ${
                inspectorOpen
                  ? 'bg-indigo-600 text-white border-indigo-400'
                  : 'bg-slate-800 hover:bg-slate-700 text-slate-200 border-slate-700'
              }`}
            >
              <Terminal className="w-3.5 h-3.5 text-indigo-400" />
              <span>Inspector API</span>
              <span className="px-1.5 py-0.2 rounded bg-slate-950 text-[11px] font-mono text-indigo-300">
                {logsCount}
              </span>
              {latestLog && (
                <span
                  className={`px-1.5 py-0.2 rounded text-[11px] font-mono font-bold ${
                    latestLog.status >= 200 && latestLog.status < 300
                      ? 'bg-emerald-500/20 text-emerald-300'
                      : 'bg-rose-500/20 text-rose-300'
                  }`}
                >
                  {latestLog.status || 'ERR'}
                </span>
              )}
            </button>
          </div>
        </div>

        {/* Barra Editable de Token JWT y x-api-key (Visible al activar o siempre accesible) */}
        {showCredentialsEditor && (
          <div className="mt-3 pt-3 border-t border-slate-800/90 grid grid-cols-1 lg:grid-cols-12 gap-3 items-end bg-slate-950/60 p-3 rounded-xl">
            <div className="lg:col-span-2">
              <label className="block text-[11px] font-semibold uppercase tracking-wider text-slate-400 mb-1">
                Login Usuario Activo
              </label>
              <input
                type="text"
                value={securityState.activeLogin}
                onChange={(e) => updateSecurityState({ activeLogin: e.target.value })}
                className="w-full px-2.5 py-1.5 rounded-lg bg-slate-900 border border-slate-700 text-xs font-mono text-white focus:outline-none focus:border-indigo-500"
                placeholder="cg1001234567"
              />
            </div>

            <div className="lg:col-span-3">
              <label className="block text-[11px] font-semibold uppercase tracking-wider text-slate-400 mb-1">
                Header <code className="text-indigo-400">x-api-key</code> Actual
              </label>
              <input
                type="text"
                value={securityState.apiKey}
                onChange={(e) => updateSecurityState({ apiKey: e.target.value })}
                className="w-full px-2.5 py-1.5 rounded-lg bg-slate-900 border border-slate-700 text-xs font-mono text-amber-300 focus:outline-none focus:border-indigo-500"
                placeholder="api-key-admin-token-secret-12345"
              />
            </div>

            <div className="lg:col-span-4">
              <label className="block text-[11px] font-semibold uppercase tracking-wider text-slate-400 mb-1">
                Header <code className="text-indigo-400">Authorization (JWT Bearer)</code> Actual
              </label>
              <input
                type="text"
                value={securityState.token}
                onChange={(e) => updateSecurityState({ token: e.target.value })}
                className="w-full px-2.5 py-1.5 rounded-lg bg-slate-900 border border-slate-700 text-xs font-mono text-emerald-300 focus:outline-none focus:border-indigo-500"
                placeholder="Bearer eyJhbGciOiJIUzUxMiJ9..."
              />
            </div>

            <div className="lg:col-span-3 flex items-center gap-2">
              <button
                onClick={handleQuickJwtLogin}
                disabled={authenticating}
                className="flex-1 flex items-center justify-center gap-1.5 px-3 py-1.5 rounded-lg bg-indigo-600 hover:bg-indigo-500 disabled:opacity-50 text-white text-xs font-semibold transition"
                title="Invoca POST /authenticate para generar un JWT firmado HS512"
              >
                <RefreshCw className={`w-3.5 h-3.5 ${authenticating ? 'animate-spin' : ''}`} />
                <span>Generar JWT (/authenticate)</span>
              </button>
              <button
                onClick={handleRestoreAdmin}
                disabled={authenticating}
                className="px-2.5 py-1.5 rounded-lg bg-slate-800 hover:bg-slate-700 text-slate-300 text-xs font-medium border border-slate-700 transition"
                title="Restaurar credenciales del Administrador por defecto (cg1001234567)"
              >
                <Sparkles className="w-3.5 h-3.5 text-amber-400" />
              </button>
            </div>
          </div>
        )}
      </div>
    </header>
  );
}

