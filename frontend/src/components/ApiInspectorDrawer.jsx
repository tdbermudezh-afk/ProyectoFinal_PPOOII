import React, { useState } from 'react';
import {
  Terminal,
  ChevronUp,
  ChevronDown,
  Trash2,
  X,
  CheckCircle2,
  AlertOctagon,
  Clock,
  Send,
  Copy,
  Check
} from 'lucide-react';
import { setInspectorOpen, clearApiLogs } from '../services/api';

function getStatusBadgeClass(status) {
  if (status >= 200 && status < 300) {
    return 'bg-emerald-500/20 text-emerald-300 border-emerald-500/40';
  }
  if (status === 400) {
    return 'bg-amber-500/20 text-amber-300 border-amber-500/40';
  }
  if (status === 401 || status === 403) {
    return 'bg-rose-500/25 text-rose-300 border-rose-500/50';
  }
  if (status === 404) {
    return 'bg-orange-500/20 text-orange-300 border-orange-500/40';
  }
  return 'bg-red-500/25 text-red-300 border-red-500/50';
}

function getMethodBadgeClass(method) {
  switch (method) {
    case 'GET':
      return 'bg-sky-500/20 text-sky-300 border-sky-500/30';
    case 'POST':
      return 'bg-emerald-500/20 text-emerald-300 border-emerald-500/30';
    case 'PUT':
      return 'bg-amber-500/20 text-amber-300 border-amber-500/30';
    case 'DELETE':
      return 'bg-rose-500/20 text-rose-300 border-rose-500/30';
    default:
      return 'bg-slate-700 text-slate-300 border-slate-600';
  }
}

export default function ApiInspectorDrawer({ apiLogs, latestLog, inspectorOpen }) {
  const [selectedLogId, setSelectedLogId] = useState(null);
  const [copied, setCopied] = useState(false);

  const activeLog =
    apiLogs.find((l) => l.id === selectedLogId) || latestLog || apiLogs[0] || null;

  const handleCopyJson = (obj) => {
    navigator.clipboard.writeText(JSON.stringify(obj, null, 2));
    setCopied(true);
    setTimeout(() => setCopied(false), 1500);
  };

  return (
    <>
      {/* Barra Inferior Persistente (Mini-Inspector Toast en vivo) */}
      <div className="fixed bottom-0 left-0 right-0 z-40 bg-slate-900/95 backdrop-blur-md border-t border-slate-800 shadow-2xl">
        <div className="max-w-[1600px] mx-auto px-4 py-2 flex flex-wrap items-center justify-between gap-2">
          <div className="flex items-center gap-2.5 min-w-0 flex-1">
            <button
              onClick={() => setInspectorOpen(!inspectorOpen)}
              className="flex items-center gap-1.5 px-2.5 py-1 rounded-md bg-indigo-500/15 hover:bg-indigo-500/25 text-indigo-300 border border-indigo-500/30 text-xs font-semibold transition shrink-0"
            >
              <Terminal className="w-3.5 h-3.5" />
              <span>Inspector API en Vivo</span>
              {inspectorOpen ? <ChevronDown className="w-3.5 h-3.5" /> : <ChevronUp className="w-3.5 h-3.5" />}
            </button>

            {latestLog ? (
              <div
                onClick={() => setInspectorOpen(true)}
                className="flex items-center gap-2 min-w-0 cursor-pointer hover:bg-slate-800/60 px-2 py-1 rounded-lg transition overflow-hidden"
              >
                <span className={`px-2 py-0.5 rounded text-[11px] font-mono font-bold border ${getMethodBadgeClass(latestLog.method)}`}>
                  {latestLog.method}
                </span>
                <span className="text-xs font-mono text-slate-200 truncate max-w-[260px] sm:max-w-[420px]">
                  {latestLog.endpoint}
                </span>
                <span className={`px-2 py-0.5 rounded text-[11px] font-mono font-bold border ${getStatusBadgeClass(latestLog.status)}`}>
                  HTTP {latestLog.status || 'ERR'}
                </span>
                <span className="hidden md:inline-flex items-center gap-1 text-[11px] text-slate-400 font-mono">
                  <Clock className="w-3 h-3" /> {latestLog.durationMs}ms
                </span>
                <span className="hidden lg:inline-block text-xs text-slate-400 truncate max-w-[400px]">
                  • {latestLog.responseBody?.error || latestLog.responseBody?.message || latestLog.responseBody?.mensaje || latestLog.description}
                </span>
              </div>
            ) : (
              <span className="text-xs text-slate-500 italic">
                Ejecuta cualquier acción en los módulos para inspeccionar el método HTTP, URL, código de estado y JSON del Backend...
              </span>
            )}
          </div>

          <div className="flex items-center gap-2 shrink-0">
            {latestLog && (
              <span className="hidden sm:inline-block text-[11px] font-mono px-2 py-0.5 rounded bg-slate-800 text-slate-400 border border-slate-700">
                {latestLog.authModeUsed}
              </span>
            )}
            <button
              onClick={() => setInspectorOpen(!inspectorOpen)}
              className="text-xs text-indigo-400 hover:text-indigo-300 font-semibold px-2 py-1"
            >
              {inspectorOpen ? 'Ocultar Detalle' : 'Ver JSON Completo'}
            </button>
          </div>
        </div>

        {/* Drawer Colapsable con el Historial y el Inspector JSON Completo */}
        {inspectorOpen && (
          <div className="border-t border-slate-800 bg-slate-950/98 max-h-[62vh] flex flex-col">
            <div className="px-4 py-2 bg-slate-900/80 border-b border-slate-800 flex items-center justify-between">
              <div className="flex items-center gap-2">
                <span className="text-xs font-bold uppercase tracking-wider text-indigo-300">
                  Auditoría de Tráfico HTTP y Restricciones CHECK / Negocio ({apiLogs.length} peticiones registradas)
                </span>
              </div>
              <div className="flex items-center gap-2">
                <button
                  onClick={clearApiLogs}
                  className="flex items-center gap-1 px-2.5 py-1 rounded text-xs bg-slate-800 hover:bg-rose-950/60 text-slate-300 hover:text-rose-300 border border-slate-700 transition"
                >
                  <Trash2 className="w-3.5 h-3.5" /> Limpiar Historial
                </button>
                <button
                  onClick={() => setInspectorOpen(false)}
                  className="p-1 rounded hover:bg-slate-800 text-slate-400 hover:text-white"
                >
                  <X className="w-4 h-4" />
                </button>
              </div>
            </div>

            <div className="grid grid-cols-1 lg:grid-cols-12 flex-1 overflow-hidden">
              {/* Lista de Peticiones Ejecutadas */}
              <div className="lg:col-span-4 border-r border-slate-800 overflow-y-auto max-h-[52vh] divide-y divide-slate-800/60">
                {apiLogs.length === 0 ? (
                  <div className="p-6 text-center text-xs text-slate-500">
                    No hay peticiones registradas todavía.
                  </div>
                ) : (
                  apiLogs.map((log) => {
                    const isSelected = activeLog && activeLog.id === log.id;
                    return (
                      <button
                        key={log.id}
                        onClick={() => setSelectedLogId(log.id)}
                        className={`w-full text-left p-3 transition flex flex-col gap-1 ${
                          isSelected ? 'bg-indigo-950/40 border-l-2 border-l-indigo-400' : 'hover:bg-slate-900/60'
                        }`}
                      >
                        <div className="flex items-center justify-between gap-2">
                          <div className="flex items-center gap-1.5 min-w-0">
                            <span className={`px-1.5 py-0.5 rounded text-[10px] font-mono font-bold border ${getMethodBadgeClass(log.method)}`}>
                              {log.method}
                            </span>
                            <span className="text-xs font-mono text-slate-200 truncate">
                              {log.endpoint}
                            </span>
                          </div>
                          <span className={`px-1.5 py-0.5 rounded text-[10px] font-mono font-bold border shrink-0 ${getStatusBadgeClass(log.status)}`}>
                            {log.status || 'ERR'}
                          </span>
                        </div>
                        <div className="flex items-center justify-between text-[11px] text-slate-400">
                          <span className="truncate">{log.description}</span>
                          <span className="font-mono shrink-0">{log.timestamp} ({log.durationMs}ms)</span>
                        </div>
                      </button>
                    );
                  })
                )}
              </div>

              {/* Detalle de la Petición Seleccionada */}
              <div className="lg:col-span-8 p-4 overflow-y-auto max-h-[52vh] space-y-3">
                {activeLog ? (
                  <>
                    <div className="flex flex-wrap items-center justify-between gap-2 bg-slate-900 p-3 rounded-xl border border-slate-800">
                      <div className="flex flex-wrap items-center gap-2">
                        <span className={`px-2.5 py-1 rounded text-xs font-mono font-bold border ${getMethodBadgeClass(activeLog.method)}`}>
                          {activeLog.method}
                        </span>
                        <code className="text-xs sm:text-sm font-mono text-white break-all">
                          {activeLog.url}
                        </code>
                      </div>
                      <div className="flex items-center gap-2">
                        <span className={`px-2.5 py-1 rounded text-xs font-mono font-bold border ${getStatusBadgeClass(activeLog.status)}`}>
                          HTTP {activeLog.status} {activeLog.statusText}
                        </span>
                        <span className="text-xs font-mono text-slate-400 bg-slate-950 px-2 py-1 rounded border border-slate-800">
                          {activeLog.durationMs} ms
                        </span>
                      </div>
                    </div>

                    <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
                      {/* Encabezados y Payload Enviado */}
                      <div className="bg-slate-900/70 rounded-xl border border-slate-800 p-3">
                        <div className="flex items-center justify-between mb-2">
                          <span className="text-[11px] font-bold uppercase tracking-wider text-slate-400">
                            Headers & Request Payload ({activeLog.authModeUsed})
                          </span>
                        </div>
                        <pre className="text-[11px] font-mono text-sky-300 bg-slate-950 p-2.5 rounded-lg overflow-x-auto border border-slate-800/80 max-h-48">
                          {JSON.stringify(
                            {
                              headers: activeLog.headersSent,
                              body: activeLog.requestBody
                            },
                            null,
                            2
                          )}
                        </pre>
                      </div>

                      {/* Respuesta JSON Devuelta por el Backend Spring Boot */}
                      <div className="bg-slate-900/70 rounded-xl border border-slate-800 p-3">
                        <div className="flex items-center justify-between mb-2">
                          <span className="text-[11px] font-bold uppercase tracking-wider text-emerald-400">
                            Respuesta JSON del Backend Spring Boot / MySQL
                          </span>
                          <button
                            onClick={() => handleCopyJson(activeLog.responseBody)}
                            className="flex items-center gap-1 text-[11px] text-slate-400 hover:text-white px-2 py-0.5 rounded bg-slate-800"
                          >
                            {copied ? <Check className="w-3 h-3 text-emerald-400" /> : <Copy className="w-3 h-3" />}
                            <span>{copied ? 'Copiado' : 'Copiar JSON'}</span>
                          </button>
                        </div>
                        <pre
                          className={`text-[11px] font-mono p-2.5 rounded-lg overflow-x-auto border max-h-48 ${
                            activeLog.ok
                              ? 'text-emerald-300 bg-slate-950 border-emerald-500/20'
                              : 'text-rose-300 bg-rose-950/20 border-rose-500/30'
                          }`}
                        >
                          {JSON.stringify(activeLog.responseBody, null, 2)}
                        </pre>
                      </div>
                    </div>
                  </>
                ) : (
                  <div className="text-center text-xs text-slate-500 py-8">
                    Selecciona una petición de la izquierda para inspeccionar su JSON.
                  </div>
                )}
              </div>
            </div>
          </div>
        )}
      </div>
    </>
  );
}

