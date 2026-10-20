import React, { useState, useEffect } from 'react';
import {
  FileSpreadsheet,
  UploadCloud,
  Download,
  Database,
  CheckCircle2,
  AlertTriangle,
  Search,
  RefreshCw,
  ArrowRight,
  Play,
  Filter,
  Layers
} from 'lucide-react';
import { apiRequest, setInspectorOpen, API_BASE_URL, getSecurityState } from '../services/api';

export default function Modulo4Entrega4() {
  const [selectedFile, setSelectedFile] = useState(null);
  const [loginCargue, setLoginCargue] = useState(getSecurityState().activeLogin || 'cg1001234567');
  const [uploading, setUploading] = useState(false);
  const [feedback, setFeedback] = useState(null);

  // Lotes históricos y auditoría por idCargue
  const [lotesHistoricos, setLotesHistoricos] = useState([]);
  const [idCargueBusqueda, setIdCargueBusqueda] = useState('');
  const [resumenCargue, setResumenCargue] = useState(null);
  const [filtroEstadoTabla, setFiltroEstadoTabla] = useState('TODOS'); // 'TODOS' | 'PROCESADO' | 'ERROR'

  const notify = (type, title, detail, status) => {
    setFeedback({ type, title, detail, status });
  };

  const cargarLotesHistoricos = async () => {
    const res = await apiRequest('/api/cargue-masivo/trayectos/lotes', {
      method: 'GET',
      description: 'Listar identificadores de cargue (idCargue) históricos en trayecto_tmp'
    });
    if (res.ok && Array.isArray(res.data)) {
      setLotesHistoricos(res.data);
      if (res.data.length > 0 && !idCargueBusqueda) {
        const masReciente = String(res.data[0]);
        setIdCargueBusqueda(masReciente);
        await consultarLotePorId(masReciente);
      }
    }
  };

  const consultarLotePorId = async (idLote) => {
    if (!idLote) return;
    const res = await apiRequest(`/api/cargue-masivo/trayectos/${encodeURIComponent(String(idLote).trim())}`, {
      method: 'GET',
      description: `Auditar resultados en trayecto_tmp para el lote idCargue=${idLote}`
    });

    if (res.ok && res.data) {
      setResumenCargue(res.data);
    } else {
      notify('error', 'No se pudo consultar el lote de cargue', res.data?.error || JSON.stringify(res.data), res.status);
    }
  };

  useEffect(() => {
    cargarLotesHistoricos();
  }, []);

  // Subir archivo Excel seleccionado desde el computador
  const handleSubirExcel = async (e) => {
    e.preventDefault();
    if (!selectedFile) {
      notify('error', 'Selecciona primero un archivo Excel (.xlsx)', 'Puedes usar el botón de cargue instantáneo con plantilla de demostración.');
      return;
    }

    setUploading(true);
    try {
      const formData = new FormData();
      formData.append('file', selectedFile);
      formData.append('login', loginCargue.trim() || 'cg1001234567');

      const res = await apiRequest('/api/cargue-masivo/trayectos', {
        method: 'POST',
        isMultipart: true,
        body: formData,
        description: `Procesar archivo Excel '${selectedFile.name}' e invocar los 3 Procedimientos Almacenados`
      });

      if (res.ok && res.data) {
        setResumenCargue(res.data);
        setIdCargueBusqueda(String(res.data.idCargue));
        await cargarLotesHistoricos();
        notify(
          'success',
          `Cargue Masivo Finalizado — Lote idCargue: ${res.data.idCargue}`,
          `Total leídos: ${res.data.totalRegistros} | Procesados a tabla 'trayecto': ${res.data.exitososProcesados} | Rechazados con observación en 'trayecto_tmp': ${res.data.rechazadosConError}`,
          res.status
        );
      } else {
        notify('error', 'Error al procesar el archivo Excel', res.data?.error || JSON.stringify(res.data), res.status);
        setInspectorOpen(true);
      }
    } finally {
      setUploading(false);
    }
  };

  // Descargar plantilla .xlsx generada por Apache POI
  const handleDescargarPlantilla = async () => {
    const res = await apiRequest('/api/cargue-masivo/trayectos/plantilla', {
      method: 'GET',
      responseType: 'blob',
      description: 'Descargar plantilla Excel (.xlsx) generada con Apache POI'
    });

    if (res.ok && res.data instanceof Blob) {
      const url = window.URL.createObjectURL(res.data);
      const a = document.createElement('a');
      a.href = url;
      a.download = 'Plantilla_Cargue_Trayectos_Ejemplo.xlsx';
      document.body.appendChild(a);
      a.click();
      a.remove();
      window.URL.revokeObjectURL(url);
      notify('success', 'Plantilla Excel descargada', 'Incluye casos válidos y casos con errores de negocio para probar los 3 procedimientos.', 200);
    }
  };

  // Ejecutar demostración en 1 clic: obtiene la plantilla Apache POI y la envía al procesador de cargue masivo
  const handleEjecutarDemoUnClic = async () => {
    setUploading(true);
    try {
      const resBlob = await apiRequest('/api/cargue-masivo/trayectos/plantilla', {
        method: 'GET',
        responseType: 'blob',
        description: 'Obtener plantilla Excel Apache POI para prueba automática'
      });

      if (!resBlob.ok || !(resBlob.data instanceof Blob)) {
        notify('error', 'No se pudo obtener la plantilla de demostración', 'Verifica que estés en Modo Autenticado.');
        setInspectorOpen(true);
        return;
      }

      const demoFile = new File([resBlob.data], 'Plantilla_Demostracion_PPOOII.xlsx', {
        type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
      });

      const formData = new FormData();
      formData.append('file', demoFile);
      formData.append('login', loginCargue.trim() || 'cg1001234567');

      const resCargue = await apiRequest('/api/cargue-masivo/trayectos', {
        method: 'POST',
        isMultipart: true,
        body: formData,
        description: 'Ejecutar los 3 Procedimientos Almacenados con Plantilla Excel de Demostración'
      });

      if (resCargue.ok && resCargue.data) {
        setResumenCargue(resCargue.data);
        setIdCargueBusqueda(String(resCargue.data.idCargue));
        await cargarLotesHistoricos();
        notify(
          'success',
          `¡3 Procedimientos Almacenados Ejecutados! Lote idCargue: ${resCargue.data.idCargue}`,
          `Total registros: ${resCargue.data.totalRegistros} → ${resCargue.data.exitososProcesados} PROCESADOS a tabla 'trayecto' y ${resCargue.data.rechazadosConError} en ERROR con observaciones acumulativas.`,
          resCargue.status
        );
      } else {
        notify('error', 'Rechazo en Cargue Masivo', resCargue.data?.error || JSON.stringify(resCargue.data), resCargue.status);
        setInspectorOpen(true);
      }
    } finally {
      setUploading(false);
    }
  };

  const registrosDetalle = resumenCargue?.detalleRegistros || [];
  const registrosFiltrados = registrosDetalle.filter((r) => {
    if (filtroEstadoTabla === 'TODOS') return true;
    return r.estado === filtroEstadoTabla;
  });

  const getEstadoTmpBadge = (estado) => {
    switch (estado) {
      case 'PROCESADO':
        return 'bg-emerald-500/20 text-emerald-300 border-emerald-500/40';
      case 'ERROR':
        return 'bg-rose-500/20 text-rose-300 border-rose-500/40';
      case 'VALIDADO':
        return 'bg-sky-500/20 text-sky-300 border-sky-500/40';
      default:
        return 'bg-amber-500/20 text-amber-300 border-amber-500/40';
    }
  };

  return (
    <div className="space-y-5">
      {/* Encabezado Módulo 4 */}
      <div className="bg-slate-900/90 border border-slate-800 rounded-2xl p-4 flex flex-wrap items-center justify-between gap-4">
        <div>
          <div className="flex items-center gap-2">
            <span className="px-2.5 py-0.5 rounded-full text-xs font-bold bg-amber-500/20 text-amber-300 border border-amber-500/30">
              MÓDULO 4 • ENTREGA 4
            </span>
            <h2 className="text-lg font-bold text-white">
              Cargue Masivo de Trayectos en Excel (Apache POI) y 3 Procedimientos Almacenados
            </h2>
          </div>
          <p className="text-xs text-slate-400 mt-1">
            Orquestación transaccional en MySQL: <code className="text-amber-300">CARGAR_TRAYECTOS_EXCEL</code> → <code className="text-sky-300">VALIDAR_TRAYECTOS_CARGUE</code> (acumulativo) → <code className="text-emerald-300">PROCESAR_TRAYECTOS_EXCEL</code>
          </p>
        </div>

        <div className="flex flex-wrap items-center gap-2">
          <button
            onClick={handleDescargarPlantilla}
            className="flex items-center gap-1.5 px-3.5 py-2 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-200 border border-slate-700 text-xs font-semibold transition"
          >
            <Download className="w-3.5 h-3.5 text-sky-400" />
            <span>Descargar Plantilla .xlsx</span>
          </button>

          <button
            onClick={handleEjecutarDemoUnClic}
            disabled={uploading}
            className="flex items-center gap-1.5 px-4 py-2 rounded-xl bg-emerald-600 hover:bg-emerald-500 disabled:opacity-50 text-white text-xs font-bold transition shadow-lg shadow-emerald-600/20"
          >
            <Play className="w-3.5 h-3.5" />
            <span>{uploading ? 'Procesando...' : 'Demostración en 1 Clic (Cargar + Validar + Procesar)'}</span>
          </button>
        </div>
      </div>

      {/* Feedback Banner */}
      {feedback && (
        <div
          className={`p-3.5 rounded-xl border flex items-start justify-between gap-3 ${
            feedback.type === 'success'
              ? 'bg-emerald-950/40 border-emerald-500/40 text-emerald-200'
              : 'bg-rose-950/40 border-rose-500/40 text-rose-200'
          }`}
        >
          <div className="flex items-start gap-2.5">
            {feedback.type === 'success' ? (
              <CheckCircle2 className="w-5 h-5 text-emerald-400 shrink-0 mt-0.5" />
            ) : (
              <AlertTriangle className="w-5 h-5 text-rose-400 shrink-0 mt-0.5" />
            )}
            <div className="text-xs">
              <div className="font-bold">{feedback.title}</div>
              <p className="mt-0.5 text-slate-300">{feedback.detail}</p>
            </div>
          </div>
          <button onClick={() => setFeedback(null)} className="text-xs text-slate-400 hover:text-white px-2">
            ✕
          </button>
        </div>
      )}

      {/* Visualizador de Arquitectura de los 3 Procedimientos Almacenados */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <div className="p-4 rounded-2xl bg-slate-900 border border-amber-500/30 space-y-1.5">
          <div className="flex items-center justify-between">
            <span className="px-2 py-0.5 rounded text-[10px] font-bold bg-amber-500/20 text-amber-300">
              FASE 1 • PROCEDIMIENTO 1
            </span>
            <Database className="w-4 h-4 text-amber-400" />
          </div>
          <div className="font-mono font-bold text-xs text-white">
            CALL CARGAR_TRAYECTOS_EXCEL(...)
          </div>
          <p className="text-[11px] text-slate-400">
            Lee cada fila con <strong>Apache POI</strong> e inserta en la tabla temporal <code className="text-indigo-300">trayecto_tmp</code> con estado inicial <strong className="text-amber-300">CARGADO</strong> y el <code className="text-white">id_cargue</code> del lote.
          </p>
        </div>

        <div className="p-4 rounded-2xl bg-slate-900 border border-sky-500/30 space-y-1.5">
          <div className="flex items-center justify-between">
            <span className="px-2 py-0.5 rounded text-[10px] font-bold bg-sky-500/20 text-sky-300">
              FASE 2 • PROCEDIMIENTO 2
            </span>
            <Database className="w-4 h-4 text-sky-400" />
          </div>
          <div className="font-mono font-bold text-xs text-white">
            CALL VALIDAR_TRAYECTOS_CARGUE(id_cargue)
          </div>
          <p className="text-[11px] text-slate-400">
            Evalúa reglas de negocio de forma <strong>acumulativa</strong>: concatenando todos los hallazgos en <code className="text-rose-300">observacion</code> (<strong className="text-rose-300">ERROR</strong>) o aprobando como <strong className="text-sky-300">VALIDADO</strong>.
          </p>
        </div>

        <div className="p-4 rounded-2xl bg-slate-900 border border-emerald-500/30 space-y-1.5">
          <div className="flex items-center justify-between">
            <span className="px-2 py-0.5 rounded text-[10px] font-bold bg-emerald-500/20 text-emerald-300">
              FASE 3 • PROCEDIMIENTO 3
            </span>
            <Database className="w-4 h-4 text-emerald-400" />
          </div>
          <div className="font-mono font-bold text-xs text-white">
            CALL PROCESAR_TRAYECTOS_EXCEL(id_cargue)
          </div>
          <p className="text-[11px] text-slate-400">
            Traslada los registros en estado <strong className="text-sky-300">VALIDADO</strong> hacia la tabla definitiva <code className="text-indigo-300">trayecto</code> y actualiza su estado en <code className="text-indigo-300">trayecto_tmp</code> a <strong className="text-emerald-300">PROCESADO</strong>.
          </p>
        </div>
      </div>

      {/* Cargador de Archivo Excel y Buscador por idCargue */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-5">
        {/* Formulario Subida de Archivo .xlsx */}
        <div className="lg:col-span-5 bg-slate-900 border border-slate-800 rounded-2xl p-5 space-y-4">
          <h3 className="text-sm font-bold text-white flex items-center gap-2 border-b border-slate-800 pb-3">
            <UploadCloud className="w-4 h-4 text-indigo-400" />
            <span>Subir Archivo Excel (.xlsx) — POST /api/cargue-masivo/trayectos</span>
          </h3>

          <form onSubmit={handleSubirExcel} className="space-y-3">
            <div>
              <label className="block text-xs font-semibold text-slate-300 mb-1">
                Archivo Excel (<code className="text-indigo-400">.xlsx</code> / <code className="text-indigo-400">Plantilla_Cargue_Trayectos_Ibague.xlsx</code>)
              </label>
              <input
                type="file"
                accept=".xlsx,.xls"
                onChange={(e) => setSelectedFile(e.target.files?.[0] || null)}
                className="block w-full text-xs text-slate-300 file:mr-3 file:py-2 file:px-3 file:rounded-lg file:border-0 file:text-xs file:font-semibold file:bg-indigo-600 file:text-white hover:file:bg-indigo-500 cursor-pointer bg-slate-950 p-2 rounded-xl border border-slate-800"
              />
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-300 mb-1">
                Login del Usuario que Ejecuta el Cargue
              </label>
              <input
                type="text"
                value={loginCargue}
                onChange={(e) => setLoginCargue(e.target.value)}
                className="w-full px-3 py-2 rounded-lg bg-slate-950 border border-slate-700 text-xs font-mono text-emerald-300"
              />
            </div>

            <button
              type="submit"
              disabled={uploading}
              className="w-full py-2.5 rounded-xl bg-indigo-600 hover:bg-indigo-500 disabled:opacity-50 text-white text-xs font-bold transition shadow-lg shadow-indigo-600/20"
            >
              {uploading ? 'Ejecutando Procedimientos Almacenados...' : 'Subir Archivo Excel y Procesar Lote (POST)'}
            </button>
          </form>
        </div>

        {/* Buscador y Selector de Lotes por idCargue */}
        <div className="lg:col-span-7 bg-slate-900 border border-slate-800 rounded-2xl p-5 space-y-4">
          <div className="flex flex-wrap items-center justify-between gap-2 border-b border-slate-800 pb-3">
            <h3 className="text-sm font-bold text-white flex items-center gap-2">
              <Search className="w-4 h-4 text-indigo-400" />
              <span>Auditoría por Identificador de Cargue (<code className="text-sky-300">GET /api/cargue-masivo/trayectos/&#123;idCargue&#125;</code>)</span>
            </h3>
          </div>

          <div className="flex flex-wrap items-center gap-2">
            <input
              type="text"
              value={idCargueBusqueda}
              onChange={(e) => setIdCargueBusqueda(e.target.value)}
              placeholder="Ej: 202610200105"
              className="flex-1 min-w-[180px] px-3 py-2 rounded-lg bg-slate-950 border border-slate-700 text-xs font-mono text-amber-300"
            />
            <select
              value={idCargueBusqueda}
              onChange={(e) => {
                setIdCargueBusqueda(e.target.value);
                consultarLotePorId(e.target.value);
              }}
              className="px-3 py-2 rounded-lg bg-slate-950 border border-slate-700 text-xs font-mono text-slate-200"
            >
              <option value="">Seleccionar lote histórico...</option>
              {lotesHistoricos.map((lote) => (
                <option key={lote} value={lote}>
                  Lote #{lote}
                </option>
              ))}
            </select>
            <button
              onClick={() => consultarLotePorId(idCargueBusqueda)}
              className="px-4 py-2 rounded-lg bg-indigo-600 hover:bg-indigo-500 text-white text-xs font-bold"
            >
              Consultar Lote
            </button>
          </div>

          {/* Tarjetas KPI del Lote Consultado */}
          {resumenCargue && (
            <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 pt-1">
              <div className="p-3 rounded-xl bg-slate-950 border border-slate-800">
                <div className="text-[11px] text-slate-400">ID de Cargue (BIGINT)</div>
                <div className="text-sm font-mono font-bold text-amber-300 mt-0.5">
                  {resumenCargue.idCargue}
                </div>
              </div>
              <div className="p-3 rounded-xl bg-slate-950 border border-slate-800">
                <div className="text-[11px] text-slate-400">Total Filas Leídas</div>
                <div className="text-lg font-mono font-bold text-white mt-0.5">
                  {resumenCargue.totalRegistros}
                </div>
              </div>
              <div className="p-3 rounded-xl bg-emerald-950/25 border border-emerald-500/30">
                <div className="text-[11px] text-emerald-300">Exitosos (PROCESADO)</div>
                <div className="text-lg font-mono font-bold text-emerald-300 mt-0.5">
                  {resumenCargue.exitososProcesados}
                </div>
              </div>
              <div className="p-3 rounded-xl bg-rose-950/25 border border-rose-500/30">
                <div className="text-[11px] text-rose-300">Con Errores (ERROR)</div>
                <div className="text-lg font-mono font-bold text-rose-300 mt-0.5">
                  {resumenCargue.rechazadosConError}
                </div>
              </div>
            </div>
          )}
        </div>
      </div>

      {/* Tabla de Auditoría de trayecto_tmp con Observaciones Acumulativas */}
      <div className="bg-slate-900 border border-slate-800 rounded-2xl p-5 space-y-3">
        <div className="flex flex-wrap items-center justify-between gap-3">
          <div>
            <h3 className="text-sm font-bold text-white">
              Detalle de Registros en Tabla Temporal <code className="text-indigo-300">trayecto_tmp</code> ({registrosFiltrados.length} filas)
            </h3>
            <p className="text-xs text-slate-400">
              Evidencia la validación acumulativa del Procedimiento Almacenado #2 en el campo <code className="text-rose-300">observacion</code>
            </p>
          </div>

          <div className="flex items-center gap-1.5">
            {['TODOS', 'PROCESADO', 'ERROR'].map((f) => (
              <button
                key={f}
                onClick={() => setFiltroEstadoTabla(f)}
                className={`px-3 py-1 rounded-lg text-xs font-bold border transition ${
                  filtroEstadoTabla === f
                    ? 'bg-indigo-600 text-white border-indigo-400'
                    : 'bg-slate-950 text-slate-400 border-slate-800 hover:bg-slate-800'
                }`}
              >
                {f}
              </button>
            ))}
          </div>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse text-xs">
            <thead>
              <tr className="border-b border-slate-800 text-slate-400 uppercase text-[11px]">
                <th className="py-2.5 px-2">ID Temp</th>
                <th className="py-2.5 px-2">Conductor / Vehículo</th>
                <th className="py-2.5 px-2">Ruta / Orden / Ubicación</th>
                <th className="py-2.5 px-2">Coordenadas</th>
                <th className="py-2.5 px-2">Estado (CHECK)</th>
                <th className="py-2.5 px-2">Observación de Validación Acumulativa</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-800/70">
              {registrosFiltrados.length === 0 ? (
                <tr>
                  <td colSpan={6} className="py-8 text-center text-slate-500">
                    Ejecuta un cargue o selecciona un lote para inspeccionar los registros de trayecto_tmp.
                  </td>
                </tr>
              ) : (
                registrosFiltrados.map((row) => (
                  <tr key={row.id} className="hover:bg-slate-800/40">
                    <td className="py-2.5 px-2 font-mono text-slate-400">#{row.id}</td>
                    <td className="py-2.5 px-2">
                      <div className="font-mono text-white">Persona: {row.idPersonaTxt}</div>
                      <div className="font-mono text-amber-300">Vehículo: {row.idVehiculoTxt}</div>
                    </td>
                    <td className="py-2.5 px-2">
                      <div className="font-mono font-bold text-indigo-300">
                        {row.codigoRuta} <span className="text-slate-400">(Parada #{row.ordenParadaTxt})</span>
                      </div>
                      <div className="text-[11px] text-slate-300">{row.ubicacion}</div>
                    </td>
                    <td className="py-2.5 px-2 font-mono text-[11px] text-slate-400">
                      {row.latitudTxt || 'NULL'}, {row.longitudTxt || 'NULL'}
                    </td>
                    <td className="py-2.5 px-2">
                      <span className={`px-2 py-0.5 rounded text-[10px] font-mono font-bold border ${getEstadoTmpBadge(row.estado)}`}>
                        {row.estado}
                      </span>
                    </td>
                    <td className="py-2.5 px-2 max-w-md">
                      {row.estado === 'ERROR' ? (
                        <div className="p-2 rounded-lg bg-rose-950/30 border border-rose-500/30 text-rose-200 text-[11px]">
                          {row.observacion}
                        </div>
                      ) : (
                        <span className="text-emerald-300 text-[11px]">{row.observacion}</span>
                      )}
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}

