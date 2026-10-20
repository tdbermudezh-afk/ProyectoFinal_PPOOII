import React, { useState, useEffect } from 'react';
import {
  Car,
  FileCheck2,
  PlusCircle,
  Search,
  Trash2,
  Edit3,
  AlertTriangle,
  CheckCircle2,
  RefreshCw,
  FilePlus,
  ShieldAlert,
  Eye,
  Sparkles,
  Filter
} from 'lucide-react';
import { apiRequest, setInspectorOpen, generateSamplePdfBase64 } from '../services/api';

export default function Modulo1Entrega1({ activeSubTab, setActiveSubTab, onOpenPdfModal }) {
  const [subTab, setSubTab] = useState(activeSubTab || 'vehiculos');

  useEffect(() => {
    if (activeSubTab) setSubTab(activeSubTab);
  }, [activeSubTab]);

  const changeTab = (tab) => {
    setSubTab(tab);
    if (setActiveSubTab) setActiveSubTab(tab);
  };

  // Estados de datos
  const [documentos, setDocumentos] = useState([]);
  const [vehiculos, setVehiculos] = useState([]);
  const [loading, setLoading] = useState(false);
  const [feedback, setFeedback] = useState(null);

  // Estado formulario Documento Paramétrico
  const [docForm, setDocForm] = useState({
    id: null,
    codigo: '',
    nombre: '',
    aplicaA: 'AM',
    obligatorio: 'RR',
    descripcion: ''
  });

  // Estado formulario Vehículo + Documento Inicial Obligatorio
  const today = new Date().toISOString().slice(0, 10);
  const nextYear = new Date(Date.now() + 365 * 86400000).toISOString().slice(0, 10);

  const [vehForm, setVehForm] = useState({
    id: null,
    placa: '',
    tipoVehiculo: 'Automóvil',
    marca: 'Toyota',
    linea: 'Corolla SEG',
    modelo: 2025,
    tipoServicio: 'Pr',
    tipoCombustible: 'Gasolina',
    capacidadPasajeros: 5,
    color: '#2563EB',
    // Asociación inicial de documento (Regla Crítica E1)
    documentoIdInicial: 1,
    fechaExpedicionInicial: today,
    fechaVencimientoInicial: nextYear,
    incluirPdfInicial: true
  });

  // Estado formulario Asociar Documento a Vehículo Existente
  const [asocForm, setAsocForm] = useState({
    vehiculoId: '',
    documentoId: '',
    fechaExpedicion: today,
    fechaVencimiento: nextYear,
    estado: 'En Verificación',
    incluirPdfMuestra: true
  });

  // Estado para Centro de 4 Consultas de Entrega 1
  const [consultaTipo, setConsultaTipo] = useState('placa'); // 'placa' | 'tipo' | 'documento' | 'estado'
  const [filtroPlaca, setFiltroPlaca] = useState('ABC123');
  const [filtroTipoVehiculo, setFiltroTipoVehiculo] = useState('Automóvil');
  const [filtroDocumentoId, setFiltroDocumentoId] = useState('1');
  const [filtroEstadoDoc, setFiltroEstadoDoc] = useState('Habilitado');
  const [resultadoConsulta, setResultadoConsulta] = useState([]);

  const cargarDatosBase = async () => {
    setLoading(true);
    try {
      const [resDocs, resVehs] = await Promise.all([
        apiRequest('/api/documentos', { method: 'GET', description: 'Listar catálogo de documentos paramétricos' }),
        apiRequest('/api/vehiculos', { method: 'GET', description: 'Listar todos los vehículos registrados' })
      ]);

      if (resDocs.ok && Array.isArray(resDocs.data)) {
        setDocumentos(resDocs.data);
        if (resDocs.data.length > 0 && !asocForm.documentoId) {
          setAsocForm((prev) => ({ ...prev, documentoId: resDocs.data[0].id }));
        }
      }
      if (resVehs.ok && Array.isArray(resVehs.data)) {
        setVehiculos(resVehs.data);
        if (resVehs.data.length > 0 && !asocForm.vehiculoId) {
          setAsocForm((prev) => ({ ...prev, vehiculoId: resVehs.data[0].id }));
        }
      }
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    cargarDatosBase();
  }, []);

  const notify = (type, title, detail, status) => {
    setFeedback({ type, title, detail, status });
  };

  // =========================================================================
  // 1. ACCIONES CRUD DOCUMENTOS PARAMÉTRICOS
  // =========================================================================
  const handleSaveDocumento = async (e) => {
    e.preventDefault();
    const isEdit = !!docForm.id;
    const endpoint = isEdit ? `/api/documentos/${docForm.id}` : '/api/documentos';
    const method = isEdit ? 'PUT' : 'POST';

    const payload = {
      codigo: docForm.codigo,
      nombre: docForm.nombre,
      aplicaA: docForm.aplicaA,
      obligatorio: docForm.obligatorio,
      descripcion: docForm.descripcion
    };

    const res = await apiRequest(endpoint, {
      method,
      body: payload,
      description: `${isEdit ? 'Actualizar' : 'Crear'} Documento Paramétrico (${docForm.codigo})`
    });

    if (res.ok) {
      notify('success', `Documento ${isEdit ? 'actualizado' : 'creado'} correctamente`, `HTTP ${res.status} — ${res.data?.nombre}`, res.status);
      setDocForm({ id: null, codigo: '', nombre: '', aplicaA: 'AM', obligatorio: 'RR', descripcion: '' });
      cargarDatosBase();
    } else {
      notify('error', 'Rechazo del Backend / Restricción CHECK en Documento', res.data?.error || JSON.stringify(res.data), res.status);
      setInspectorOpen(true);
    }
  };

  const handleTestInvalidCheckDocumento = async () => {
    const res = await apiRequest('/api/documentos', {
      method: 'POST',
      body: {
        codigo: 'DOC-ERR-99',
        nombre: 'Documento Violación CHECK',
        aplicaA: 'CAMION_INVALIDO',
        obligatorio: 'INVALIDO_XX',
        descripcion: 'Prueba de violación de restricción CHECK en Documento'
      },
      description: 'Prueba de Fallo: Violación CHECK en Documento (aplicaA="CAMION_INVALIDO")'
    });
    notify(
      res.ok ? 'warning' : 'error',
      'Prueba de Violación CHECK ejecutada',
      res.data?.error || JSON.stringify(res.data),
      res.status
    );
    setInspectorOpen(true);
  };

  const handleDeleteDocumento = async (id, nombre) => {
    const res = await apiRequest(`/api/documentos/${id}`, {
      method: 'DELETE',
      description: `Eliminar documento paramétrico ID ${id} (${nombre})`
    });
    if (res.ok) {
      notify('success', `Documento ID ${id} eliminado`, `HTTP ${res.status}`, res.status);
      cargarDatosBase();
    } else {
      notify('error', `No se pudo eliminar el documento ID ${id}`, res.data?.error || JSON.stringify(res.data), res.status);
      setInspectorOpen(true);
    }
  };

  // =========================================================================
  // 2. ACCIONES CRUD VEHÍCULOS + PRUEBAS DE FALLO REGLA CRÍTICA
  // =========================================================================
  const generarPlacaSugerida = (tipo) => {
    const letras = 'ABCDEFGHJKLMNPQRSTUVWXYZ';
    const rL = () => letras[Math.floor(Math.random() * letras.length)];
    const rN = () => Math.floor(Math.random() * 10);
    if (tipo === 'Motocicleta') {
      return `${rL()}${rL()}${rL()}${rN()}${rN()}${rL()}`;
    }
    return `${rL()}${rL()}${rL()}${rN()}${rN()}${rN()}`;
  };

  const handleSaveVehiculo = async (e) => {
    e.preventDefault();
    const isEdit = !!vehForm.id;

    if (isEdit) {
      const res = await apiRequest(`/api/vehiculos/${vehForm.id}`, {
        method: 'PUT',
        body: {
          placa: vehForm.placa,
          tipoVehiculo: vehForm.tipoVehiculo,
          marca: vehForm.marca,
          linea: vehForm.linea,
          modelo: Number(vehForm.modelo),
          tipoServicio: vehForm.tipoServicio,
          tipoCombustible: vehForm.tipoCombustible,
          capacidadPasajeros: Number(vehForm.capacidadPasajeros),
          color: vehForm.color
        },
        description: `Actualizar vehículo ID ${vehForm.id} (${vehForm.placa})`
      });

      if (res.ok) {
        notify('success', `Vehículo ${vehForm.placa} actualizado`, `HTTP ${res.status}`, res.status);
        setVehForm((prev) => ({ ...prev, id: null, placa: '' }));
        cargarDatosBase();
      } else {
        notify('error', 'Error al actualizar vehículo', res.data?.error || JSON.stringify(res.data), res.status);
        setInspectorOpen(true);
      }
      return;
    }

    // Creación POST con al menos 1 documento asociado (el backend forzará estado = 'En Verificación')
    const pdfBase64 = vehForm.incluirPdfInicial
      ? generateSamplePdfBase64(`DOCUMENTO INICIAL VEHICULO ${vehForm.placa}`, `Estado inicial automatico: En Verificacion`)
      : null;

    const payload = {
      placa: vehForm.placa.trim().toUpperCase(),
      tipoVehiculo: vehForm.tipoVehiculo,
      marca: vehForm.marca,
      linea: vehForm.linea,
      modelo: Number(vehForm.modelo),
      tipoServicio: vehForm.tipoServicio,
      tipoCombustible: vehForm.tipoCombustible,
      capacidadPasajeros: Number(vehForm.capacidadPasajeros),
      color: vehForm.color,
      documentos: [
        {
          documento: { id: Number(vehForm.documentoIdInicial) },
          fechaExpedicion: vehForm.fechaExpedicionInicial,
          fechaVencimiento: vehForm.fechaVencimientoInicial,
          // Enviamos intencionalmente "Habilitado" para demostrar que el Backend lo sobreescribe a "En Verificación"
          estado: 'Habilitado',
          documentoPdfBase64: pdfBase64
        }
      ]
    };

    const res = await apiRequest('/api/vehiculos', {
      method: 'POST',
      body: payload,
      description: `Crear Vehículo (${payload.placa}) con documento inicial -> Estado forzado 'En Verificación'`
    });

    if (res.ok) {
      const estadoAsignado = res.data?.documentos?.[0]?.estado || 'En Verificación';
      notify(
        'success',
        `Vehículo ${res.data?.placa} creado exitosamente`,
        `Regla crítica verificada: el documento asociado quedó automáticamente con estado "${estadoAsignado}".`,
        res.status
      );
      setVehForm((prev) => ({ ...prev, id: null, placa: generarPlacaSugerida(prev.tipoVehiculo) }));
      cargarDatosBase();
    } else {
      notify('error', 'Rechazo de Validación al Crear Vehículo', res.data?.error || JSON.stringify(res.data), res.status);
      setInspectorOpen(true);
    }
  };

  // Botón de Prueba de Fallo 1: Intentar crear vehículo SIN documento asociado
  const handleTestCrearSinDocumento = async () => {
    const placaTest = generarPlacaSugerida('Automóvil');
    const res = await apiRequest('/api/vehiculos', {
      method: 'POST',
      body: {
        placa: placaTest,
        tipoVehiculo: 'Automóvil',
        marca: 'Mazda',
        linea: 'CX-30',
        modelo: 2024,
        tipoServicio: 'Pr',
        tipoCombustible: 'Gasolina',
        capacidadPasajeros: 5,
        color: '#E11D48',
        documentos: [] // Sin documentos asociados -> Debe fallar con HTTP 400
      },
      description: 'PRUEBA DE FALLO (Entrega 1): Intentar crear vehículo SIN documento asociado'
    });

    notify(
      'error',
      'Bloqueo Exitoso: Intento de crear vehículo sin documento',
      `El Backend rechazó la petición con HTTP ${res.status}: "${res.data?.error || JSON.stringify(res.data)}"`,
      res.status
    );
    setInspectorOpen(true);
  };

  // Botón de Prueba de Fallo 2: Intentar crear Automóvil con placa de Motocicleta o combustible inválido
  const handleTestPlacaInvalida = async () => {
    const res = await apiRequest('/api/vehiculos', {
      method: 'POST',
      body: {
        placa: 'MOT99A', // Placa de moto en un Automóvil
        tipoVehiculo: 'Automóvil',
        marca: 'Renault',
        linea: 'Duster',
        modelo: 2024,
        tipoServicio: 'Pr',
        tipoCombustible: 'Gasolina',
        capacidadPasajeros: 5,
        color: '#10B981',
        documentos: [
          {
            documento: { id: 1 },
            fechaExpedicion: today,
            fechaVencimiento: nextYear
          }
        ]
      },
      description: 'PRUEBA DE FALLO (Entrega 1): Placa de Motocicleta (MOT99A) en un Automóvil'
    });

    notify(
      'error',
      'Bloqueo Exitoso: Validación de Formato de Placa según Tipo de Vehículo',
      `El Backend rechazó la petición con HTTP ${res.status}: "${res.data?.error || JSON.stringify(res.data)}"`,
      res.status
    );
    setInspectorOpen(true);
  };

  const handleDeleteVehiculo = async (id, placa) => {
    const res = await apiRequest(`/api/vehiculos/${id}`, {
      method: 'DELETE',
      description: `Eliminar vehículo ID ${id} (${placa})`
    });
    if (res.ok) {
      notify('success', `Vehículo ${placa} eliminado`, `HTTP ${res.status}`, res.status);
      cargarDatosBase();
    } else {
      notify('error', `No se pudo eliminar el vehículo ${placa}`, res.data?.error || JSON.stringify(res.data), res.status);
      setInspectorOpen(true);
    }
  };

  // =========================================================================
  // 3. ASOCIAR DOCUMENTOS A VEHÍCULO EXISTENTE + CAMBIAR ESTADO DE DOCUMENTO
  // =========================================================================
  const handleAsociarDocumentoExistente = async (e) => {
    e.preventDefault();
    if (!asocForm.vehiculoId || !asocForm.documentoId) return;

    const pdfBase64 = asocForm.incluirPdfMuestra
      ? generateSamplePdfBase64(
          `SOPORTE DOCUMENTAL VEHICULO ID ${asocForm.vehiculoId}`,
          `Documento ID ${asocForm.documentoId} - Estado: ${asocForm.estado}`
        )
      : null;

    const res = await apiRequest(`/api/vehiculos/${asocForm.vehiculoId}/documentos`, {
      method: 'POST',
      body: {
        documento: { id: Number(asocForm.documentoId) },
        fechaExpedicion: asocForm.fechaExpedicion,
        fechaVencimiento: asocForm.fechaVencimiento,
        estado: asocForm.estado,
        documentoPdfBase64: pdfBase64
      },
      description: `Agregar documento ID ${asocForm.documentoId} al vehículo ID ${asocForm.vehiculoId} con estado '${asocForm.estado}'`
    });

    if (res.ok) {
      notify(
        'success',
        `Documento agregado al vehículo ${res.data?.placa}`,
        `Estado registrado: ${asocForm.estado}`,
        res.status
      );
      cargarDatosBase();
    } else {
      notify('error', 'Error al asociar documento al vehículo', res.data?.error || JSON.stringify(res.data), res.status);
      setInspectorOpen(true);
    }
  };

  const handleCambiarEstadoDocVehiculo = async (docRelId, nuevoEstado) => {
    const res = await apiRequest(`/api/operaciones/documentos/${docRelId}/estado`, {
      method: 'PUT',
      body: { estado: nuevoEstado },
      description: `Cambiar estado del documento vehicular #${docRelId} a '${nuevoEstado}'`
    });
    if (res.ok) {
      notify('success', `Estado del documento #${docRelId} actualizado a "${nuevoEstado}"`, `HTTP ${res.status}`, res.status);
      cargarDatosBase();
    } else {
      notify('error', 'No se pudo cambiar el estado del documento', res.data?.error || JSON.stringify(res.data), res.status);
    }
  };

  // =========================================================================
  // 4. CENTRO DE LAS 4 CONSULTAS DE LA ENTREGA 1
  // =========================================================================
  const ejecutarConsultaE1 = async (e) => {
    if (e) e.preventDefault();
    let endpoint = '';
    let desc = '';

    if (consultaTipo === 'placa') {
      endpoint = `/api/vehiculos/placa/${encodeURIComponent(filtroPlaca.trim().toUpperCase())}`;
      desc = `Consulta E1 #1: Buscar vehículo por placa (${filtroPlaca})`;
    } else if (consultaTipo === 'tipo') {
      endpoint = `/api/vehiculos/tipo/${encodeURIComponent(filtroTipoVehiculo)}`;
      desc = `Consulta E1 #2: Buscar vehículos por tipo (${filtroTipoVehiculo})`;
    } else if (consultaTipo === 'documento') {
      endpoint = `/api/vehiculos/documento/${encodeURIComponent(filtroDocumentoId)}`;
      desc = `Consulta E1 #3: Buscar vehículos con documento ID ${filtroDocumentoId}`;
    } else if (consultaTipo === 'estado') {
      endpoint = `/api/vehiculos/estado-documento/${encodeURIComponent(filtroEstadoDoc)}`;
      desc = `Consulta E1 #4: Buscar vehículos con documento en estado '${filtroEstadoDoc}'`;
    }

    const res = await apiRequest(endpoint, { method: 'GET', description: desc });
    if (res.ok) {
      const lista = Array.isArray(res.data) ? res.data : res.data ? [res.data] : [];
      setResultadoConsulta(lista);
      notify('success', `Consulta ejecutada (${lista.length} vehículos encontrados)`, `${desc} -> HTTP ${res.status}`, res.status);
    } else {
      setResultadoConsulta([]);
      notify('error', 'Sin resultados o error en consulta', res.data?.error || JSON.stringify(res.data), res.status);
    }
  };

  const getDocEstadoBadge = (estado) => {
    if (estado === 'Habilitado') {
      return 'bg-emerald-500/20 text-emerald-300 border-emerald-500/40';
    }
    if (estado === 'Vencido') {
      return 'bg-rose-500/20 text-rose-300 border-rose-500/40';
    }
    return 'bg-amber-500/20 text-amber-300 border-amber-500/40';
  };

  return (
    <div className="space-y-5">
      {/* Encabezado y Sub-navegación del Módulo 1 */}
      <div className="bg-slate-900/90 border border-slate-800 rounded-2xl p-4 flex flex-wrap items-center justify-between gap-4">
        <div>
          <div className="flex items-center gap-2">
            <span className="px-2.5 py-0.5 rounded-full text-xs font-bold bg-indigo-500/20 text-indigo-300 border border-indigo-500/30">
              MÓDULO 1 • ENTREGA 1
            </span>
            <h2 className="text-lg font-bold text-white">
              Vehículos, Documentos Paramétricos y Centro de 4 Consultas
            </h2>
          </div>
          <p className="text-xs text-slate-400 mt-1">
            Endpoints protegidos con <code className="text-emerald-300">Authorization: Bearer JWT</code> y <code className="text-amber-300">x-api-key</code> • Incluye validación de restricciones <code className="text-indigo-300">CHECK</code> y estado inicial <code className="text-amber-300">En Verificación</code>
          </p>
        </div>

        <div className="flex flex-wrap items-center gap-2">
          {[
            { id: 'vehiculos', label: '1. CRUD Vehículos + Regla Crítica', icon: Car },
            { id: 'documentos', label: '2. CRUD Documentos Paramétricos', icon: FileCheck2 },
            { id: 'asociar', label: '3. Asociar Docs a Vehículo Existente', icon: FilePlus },
            { id: 'consultas', label: '4. Centro de 4 Consultas (E1)', icon: Search }
          ].map((t) => {
            const Icon = t.icon;
            const active = subTab === t.id;
            return (
              <button
                key={t.id}
                onClick={() => changeTab(t.id)}
                className={`flex items-center gap-1.5 px-3.5 py-2 rounded-xl text-xs font-semibold transition border ${
                  active
                    ? 'bg-indigo-600 text-white border-indigo-500 shadow-md shadow-indigo-600/20'
                    : 'bg-slate-800/80 hover:bg-slate-800 text-slate-300 border-slate-700'
                }`}
              >
                <Icon className="w-3.5 h-3.5" />
                <span>{t.label}</span>
              </button>
            );
          })}
          <button
            onClick={cargarDatosBase}
            className="p-2 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-300 border border-slate-700"
            title="Refrescar datos desde MySQL"
          >
            <RefreshCw className={`w-4 h-4 ${loading ? 'animate-spin' : ''}`} />
          </button>
        </div>
      </div>

      {/* Banner de Feedback en Vivo */}
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
              <div className="font-bold flex items-center gap-2">
                <span>{feedback.title}</span>
                {feedback.status && (
                  <span className="px-2 py-0.5 rounded bg-slate-950/80 font-mono text-[11px]">
                    HTTP {feedback.status}
                  </span>
                )}
              </div>
              <p className="mt-0.5 text-slate-300">{feedback.detail}</p>
            </div>
          </div>
          <button
            onClick={() => setFeedback(null)}
            className="text-xs text-slate-400 hover:text-white px-2"
          >
            ✕
          </button>
        </div>
      )}

      {/* =====================================================================
          SUB-TAB 1: CRUD DE VEHÍCULOS + REGLA CRÍTICA DE DOCUMENTO INICIAL
         ===================================================================== */}
      {subTab === 'vehiculos' && (
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-5">
          {/* Formulario de Creación / Edición de Vehículo */}
          <div className="lg:col-span-5 bg-slate-900 border border-slate-800 rounded-2xl p-5 space-y-4">
            <div className="flex items-center justify-between border-b border-slate-800 pb-3">
              <div>
                <h3 className="text-sm font-bold text-white flex items-center gap-2">
                  <Car className="w-4 h-4 text-indigo-400" />
                  <span>{vehForm.id ? `Editar Vehículo ID #${vehForm.id}` : 'Registrar Nuevo Vehículo (POST /api/vehiculos)'}</span>
                </h3>
                <p className="text-[11px] text-slate-400">
                  Valida restricciones <code className="text-indigo-300">CHECK</code> de tipo, placa, servicio, combustible y color Hex
                </p>
              </div>
              {vehForm.id && (
                <button
                  onClick={() => setVehForm((prev) => ({ ...prev, id: null, placa: '' }))}
                  className="text-xs text-amber-400 hover:underline"
                >
                  Cancelar edición
                </button>
              )}
            </div>

            <form onSubmit={handleSaveVehiculo} className="space-y-3">
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-semibold text-slate-300 mb-1">
                    Tipo de Vehículo (<code className="text-indigo-400">CHECK</code>)
                  </label>
                  <select
                    value={vehForm.tipoVehiculo}
                    onChange={(e) => {
                      const nuevoTipo = e.target.value;
                      setVehForm({
                        ...vehForm,
                        tipoVehiculo: nuevoTipo,
                        placa: generarPlacaSugerida(nuevoTipo),
                        capacidadPasajeros: nuevoTipo === 'Motocicleta' ? 2 : 5
                      });
                    }}
                    className="w-full px-3 py-2 rounded-lg bg-slate-950 border border-slate-700 text-xs text-white"
                  >
                    <option value="Automóvil">Automóvil (Placa: 3 Letras + 3 Números)</option>
                    <option value="Motocicleta">Motocicleta (Placa: 3 Letras + 2 Números + 1 Letra)</option>
                  </select>
                </div>

                <div>
                  <div className="flex items-center justify-between mb-1">
                    <label className="text-xs font-semibold text-slate-300">
                      Placa (<code className="text-indigo-400">UNIQUE 6 chars</code>)
                    </label>
                    <button
                      type="button"
                      onClick={() => setVehForm({ ...vehForm, placa: generarPlacaSugerida(vehForm.tipoVehiculo) })}
                      className="text-[11px] text-indigo-400 hover:text-indigo-300 flex items-center gap-1"
                    >
                      <Sparkles className="w-3 h-3" /> Sugerir
                    </button>
                  </div>
                  <input
                    type="text"
                    maxLength={6}
                    required
                    value={vehForm.placa}
                    onChange={(e) => setVehForm({ ...vehForm, placa: e.target.value.toUpperCase() })}
                    placeholder={vehForm.tipoVehiculo === 'Automóvil' ? 'Ej: ABC123' : 'Ej: XYZ12D'}
                    className="w-full px-3 py-2 rounded-lg bg-slate-950 border border-slate-700 text-xs font-mono uppercase text-amber-300"
                  />
                </div>
              </div>

              <div className="grid grid-cols-3 gap-3">
                <div>
                  <label className="block text-xs font-semibold text-slate-300 mb-1">Marca</label>
                  <input
                    type="text"
                    required
                    value={vehForm.marca}
                    onChange={(e) => setVehForm({ ...vehForm, marca: e.target.value })}
                    className="w-full px-3 py-2 rounded-lg bg-slate-950 border border-slate-700 text-xs text-white"
                  />
                </div>
                <div>
                  <label className="block text-xs font-semibold text-slate-300 mb-1">Línea</label>
                  <input
                    type="text"
                    required
                    value={vehForm.linea}
                    onChange={(e) => setVehForm({ ...vehForm, linea: e.target.value })}
                    className="w-full px-3 py-2 rounded-lg bg-slate-950 border border-slate-700 text-xs text-white"
                  />
                </div>
                <div>
                  <label className="block text-xs font-semibold text-slate-300 mb-1">Modelo (Año)</label>
                  <input
                    type="number"
                    required
                    value={vehForm.modelo}
                    onChange={(e) => setVehForm({ ...vehForm, modelo: e.target.value })}
                    className="w-full px-3 py-2 rounded-lg bg-slate-950 border border-slate-700 text-xs text-white"
                  />
                </div>
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-semibold text-slate-300 mb-1">
                    Tipo Servicio (<code className="text-indigo-400">CHECK</code>)
                  </label>
                  <select
                    value={vehForm.tipoServicio}
                    onChange={(e) => setVehForm({ ...vehForm, tipoServicio: e.target.value })}
                    className="w-full px-3 py-2 rounded-lg bg-slate-950 border border-slate-700 text-xs text-white"
                  >
                    <option value="Pr">Pr (Privado)</option>
                    <option value="Pu">Pu (Público)</option>
                    <option value="Privado">Privado (Texto completo)</option>
                    <option value="Público">Público (Texto completo)</option>
                  </select>
                </div>

                <div>
                  <label className="block text-xs font-semibold text-slate-300 mb-1">
                    Combustible (<code className="text-indigo-400">CHECK</code>)
                  </label>
                  <select
                    value={vehForm.tipoCombustible}
                    onChange={(e) => setVehForm({ ...vehForm, tipoCombustible: e.target.value })}
                    className="w-full px-3 py-2 rounded-lg bg-slate-950 border border-slate-700 text-xs text-white"
                  >
                    <option value="Gasolina">Gasolina</option>
                    <option value="Gas">Gas</option>
                    <option value="Diesel">Diesel</option>
                    <option value="Disel">Disel</option>
                  </select>
                </div>
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-semibold text-slate-300 mb-1">
                    Capacidad Pasajeros
                  </label>
                  <input
                    type="number"
                    min={1}
                    required
                    value={vehForm.capacidadPasajeros}
                    onChange={(e) => setVehForm({ ...vehForm, capacidadPasajeros: e.target.value })}
                    className="w-full px-3 py-2 rounded-lg bg-slate-950 border border-slate-700 text-xs text-white"
                  />
                </div>

                <div>
                  <label className="block text-xs font-semibold text-slate-300 mb-1">
                    Color Hexadecimal (<code className="text-indigo-400">#RRGGBB</code>)
                  </label>
                  <div className="flex items-center gap-2">
                    <input
                      type="color"
                      value={vehForm.color.startsWith('#') && vehForm.color.length === 7 ? vehForm.color : '#2563EB'}
                      onChange={(e) => setVehForm({ ...vehForm, color: e.target.value.toUpperCase() })}
                      className="w-9 h-9 rounded cursor-pointer bg-slate-950 border border-slate-700"
                    />
                    <input
                      type="text"
                      value={vehForm.color}
                      onChange={(e) => setVehForm({ ...vehForm, color: e.target.value.toUpperCase() })}
                      className="flex-1 px-3 py-2 rounded-lg bg-slate-950 border border-slate-700 text-xs font-mono text-white"
                      placeholder="#FF5733"
                    />
                  </div>
                </div>
              </div>

              {/* Sección Crítica: Asociación Obligatoria de Documento Inicial al Crear */}
              {!vehForm.id && (
                <div className="p-3.5 rounded-xl bg-indigo-950/30 border border-indigo-500/30 space-y-2.5">
                  <div className="flex items-center justify-between">
                    <span className="text-xs font-bold text-indigo-300">
                      Regla Crítica E1: Documento Inicial Asociado
                    </span>
                    <span className="px-2 py-0.5 rounded text-[10px] font-bold bg-amber-500/20 text-amber-300 border border-amber-500/40">
                      Estado Inicial: En Verificación
                    </span>
                  </div>

                  <div>
                    <label className="block text-[11px] font-semibold text-slate-300 mb-1">
                      Documento Paramétrico a Asociar
                    </label>
                    <select
                      value={vehForm.documentoIdInicial}
                      onChange={(e) => setVehForm({ ...vehForm, documentoIdInicial: e.target.value })}
                      className="w-full px-2.5 py-1.5 rounded-lg bg-slate-950 border border-slate-700 text-xs text-white"
                    >
                      {documentos.map((d) => (
                        <option key={d.id} value={d.id}>
                          #{d.id} - {d.nombre} (Aplica a: {d.aplicaA} | Oblig: {d.obligatorio})
                        </option>
                      ))}
                    </select>
                  </div>

                  <div className="grid grid-cols-2 gap-2">
                    <div>
                      <label className="block text-[11px] text-slate-400 mb-0.5">Fecha Expedición</label>
                      <input
                        type="date"
                        value={vehForm.fechaExpedicionInicial}
                        onChange={(e) => setVehForm({ ...vehForm, fechaExpedicionInicial: e.target.value })}
                        className="w-full px-2.5 py-1.5 rounded bg-slate-950 border border-slate-700 text-xs text-white"
                      />
                    </div>
                    <div>
                      <label className="block text-[11px] text-slate-400 mb-0.5">Fecha Vencimiento</label>
                      <input
                        type="date"
                        value={vehForm.fechaVencimientoInicial}
                        onChange={(e) => setVehForm({ ...vehForm, fechaVencimientoInicial: e.target.value })}
                        className="w-full px-2.5 py-1.5 rounded bg-slate-950 border border-slate-700 text-xs text-white"
                      />
                    </div>
                  </div>
                </div>
              )}

              <button
                type="submit"
                className="w-full py-2.5 rounded-xl bg-indigo-600 hover:bg-indigo-500 text-white text-xs font-bold transition shadow-lg shadow-indigo-600/25"
              >
                {vehForm.id ? 'Guardar Cambios en Vehículo (PUT)' : 'Crear Vehículo con Documento en "En Verificación" (POST)'}
              </button>
            </form>

            {/* Botones de Prueba de Fallo Exigidos para la Sustentación */}
            <div className="pt-3 border-t border-slate-800 space-y-2">
              <div className="text-[11px] font-bold uppercase tracking-wider text-rose-400 flex items-center gap-1.5">
                <ShieldAlert className="w-3.5 h-3.5" />
                <span>Pruebas de Fallo en Vivo para el Evaluador (HTTP 400)</span>
              </div>
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-2">
                <button
                  type="button"
                  onClick={handleTestCrearSinDocumento}
                  className="px-3 py-2 rounded-lg bg-rose-950/50 hover:bg-rose-900/60 text-rose-300 border border-rose-500/40 text-xs font-semibold transition text-left"
                >
                  ⚡ Intentar crear SIN documento
                </button>
                <button
                  type="button"
                  onClick={handleTestPlacaInvalida}
                  className="px-3 py-2 rounded-lg bg-amber-950/50 hover:bg-amber-900/60 text-amber-300 border border-amber-500/40 text-xs font-semibold transition text-left"
                >
                  ⚡ Probar placa Moto en Automóvil
                </button>
              </div>
            </div>
          </div>

          {/* Tabla de Vehículos Registrados y sus Documentos */}
          <div className="lg:col-span-7 bg-slate-900 border border-slate-800 rounded-2xl p-5 flex flex-col">
            <div className="flex items-center justify-between mb-3">
              <div>
                <h3 className="text-sm font-bold text-white">
                  Flota de Vehículos Registrados ({vehiculos.length})
                </h3>
                <p className="text-xs text-slate-400">
                  Haz clic en cualquier badge de documento para cambiar su estado o ver el PDF en Base64
                </p>
              </div>
            </div>

            <div className="overflow-x-auto flex-1">
              <table className="w-full text-left border-collapse text-xs">
                <thead>
                  <tr className="border-b border-slate-800 text-slate-400 uppercase text-[11px]">
                    <th className="py-2.5 px-2">Placa / Tipo</th>
                    <th className="py-2.5 px-2">Marca / Línea / Modelo</th>
                    <th className="py-2.5 px-2">Serv. / Comb. / Color</th>
                    <th className="py-2.5 px-2">Documentos Asociados (Estado)</th>
                    <th className="py-2.5 px-2 text-right">Acciones</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-800/70">
                  {vehiculos.map((v) => (
                    <tr key={v.id} className="hover:bg-slate-800/40">
                      <td className="py-2.5 px-2">
                        <div className="font-mono font-bold text-amber-300 text-sm">{v.placa}</div>
                        <div className="text-[11px] text-slate-400">
                          ID #{v.id} • {v.tipoVehiculo}
                        </div>
                      </td>
                      <td className="py-2.5 px-2">
                        <div className="font-semibold text-white">
                          {v.marca} {v.linea}
                        </div>
                        <div className="text-[11px] text-slate-400">
                          Modelo {v.modelo} • {v.capacidadPasajeros} pasaj.
                        </div>
                      </td>
                      <td className="py-2.5 px-2">
                        <div className="text-slate-200">
                          Serv: <strong>{v.tipoServicio}</strong> • {v.tipoCombustible}
                        </div>
                        <div className="flex items-center gap-1.5 mt-1">
                          <span
                            className="w-3.5 h-3.5 rounded-full border border-white/30 inline-block"
                            style={{ backgroundColor: v.color }}
                          />
                          <code className="text-[11px] font-mono text-slate-400">{v.color}</code>
                        </div>
                      </td>
                      <td className="py-2.5 px-2">
                        <div className="flex flex-col gap-1.5">
                          {(v.documentos || []).map((docRel) => (
                            <div
                              key={docRel.id}
                              className="flex flex-wrap items-center justify-between gap-1.5 bg-slate-950/80 px-2 py-1 rounded-lg border border-slate-800"
                            >
                              <div>
                                <span className="font-semibold text-slate-200">
                                  {docRel.documento?.nombre || `Doc`}
                                </span>
                                <span className="text-[10px] text-slate-400 block">
                                  Vence: {docRel.fechaVencimiento}
                                </span>
                              </div>
                              <div className="flex items-center gap-1">
                                <span
                                  className={`px-2 py-0.5 rounded text-[10px] font-bold border ${getDocEstadoBadge(
                                    docRel.estado
                                  )}`}
                                >
                                  {docRel.estado}
                                </span>
                                {docRel.documentoPdfBase64 && (
                                  <button
                                    onClick={() =>
                                      onOpenPdfModal({
                                        isOpen: true,
                                        title: `${docRel.documento?.nombre || 'Documento'} - Vehículo ${v.placa}`,
                                        subtitle: `Vigencia: ${docRel.fechaExpedicion} a ${docRel.fechaVencimiento} (${docRel.estado})`,
                                        base64Data: docRel.documentoPdfBase64
                                      })
                                    }
                                    className="p-1 rounded bg-indigo-500/20 hover:bg-indigo-500/40 text-indigo-300"
                                    title="Ver PDF Base64 almacenado en LONGBLOB"
                                  >
                                    <Eye className="w-3.5 h-3.5" />
                                  </button>
                                )}
                              </div>
                            </div>
                          ))}
                        </div>
                      </td>
                      <td className="py-2.5 px-2 text-right">
                        <div className="flex items-center justify-end gap-1">
                          <button
                            onClick={() =>
                              setVehForm({
                                ...vehForm,
                                id: v.id,
                                placa: v.placa,
                                tipoVehiculo: v.tipoVehiculo,
                                marca: v.marca,
                                linea: v.linea,
                                modelo: v.modelo,
                                tipoServicio: v.tipoServicio,
                                tipoCombustible: v.tipoCombustible,
                                capacidadPasajeros: v.capacidadPasajeros,
                                color: v.color
                              })
                            }
                            className="p-1.5 rounded-lg bg-slate-800 hover:bg-indigo-600 text-slate-300 hover:text-white transition"
                            title="Editar vehículo"
                          >
                            <Edit3 className="w-3.5 h-3.5" />
                          </button>
                          <button
                            onClick={() => handleDeleteVehiculo(v.id, v.placa)}
                            className="p-1.5 rounded-lg bg-slate-800 hover:bg-rose-600 text-slate-300 hover:text-white transition"
                            title="Eliminar vehículo"
                          >
                            <Trash2 className="w-3.5 h-3.5" />
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      )}

      {/* =====================================================================
          SUB-TAB 2: CRUD DE ENTIDAD PARAMÉTRICA DOCUMENTOS
         ===================================================================== */}
      {subTab === 'documentos' && (
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-5">
          <div className="lg:col-span-5 bg-slate-900 border border-slate-800 rounded-2xl p-5 space-y-4">
            <h3 className="text-sm font-bold text-white flex items-center gap-2 border-b border-slate-800 pb-3">
              <FileCheck2 className="w-4 h-4 text-indigo-400" />
              <span>{docForm.id ? `Editar Documento #${docForm.id}` : 'Nuevo Documento Paramétrico (POST /api/documentos)'}</span>
            </h3>

            <form onSubmit={handleSaveDocumento} className="space-y-3">
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-semibold text-slate-300 mb-1">Código Único</label>
                  <input
                    type="text"
                    required
                    value={docForm.codigo}
                    onChange={(e) => setDocForm({ ...docForm, codigo: e.target.value.toUpperCase() })}
                    placeholder="DOC-05"
                    className="w-full px-3 py-2 rounded-lg bg-slate-950 border border-slate-700 text-xs font-mono text-amber-300"
                  />
                </div>
                <div>
                  <label className="block text-xs font-semibold text-slate-300 mb-1">Nombre del Documento</label>
                  <input
                    type="text"
                    required
                    value={docForm.nombre}
                    onChange={(e) => setDocForm({ ...docForm, nombre: e.target.value })}
                    placeholder="Ej: Póliza Contractual"
                    className="w-full px-3 py-2 rounded-lg bg-slate-950 border border-slate-700 text-xs text-white"
                  />
                </div>
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-300 mb-1">
                  Tipos de Vehículos a los que Aplica (<code className="text-indigo-400">CHECK: A, M, AM</code>)
                </label>
                <select
                  value={docForm.aplicaA}
                  onChange={(e) => setDocForm({ ...docForm, aplicaA: e.target.value })}
                  className="w-full px-3 py-2 rounded-lg bg-slate-950 border border-slate-700 text-xs text-white"
                >
                  <option value="AM">AM — Aplica para Ambos (Automóvil y Motocicleta)</option>
                  <option value="A">A — Exclusivo para Automóvil</option>
                  <option value="M">M — Exclusivo para Motocicleta</option>
                </select>
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-300 mb-1">
                  Carácter Obligatorio (<code className="text-indigo-400">CHECK: RA, RM, RR, NO</code>)
                </label>
                <select
                  value={docForm.obligatorio}
                  onChange={(e) => setDocForm({ ...docForm, obligatorio: e.target.value })}
                  className="w-full px-3 py-2 rounded-lg bg-slate-950 border border-slate-700 text-xs text-white"
                >
                  <option value="RR">RR — Requerido Obligatorio para Ambos</option>
                  <option value="RA">RA — Obligatorio solo para Automóvil</option>
                  <option value="RM">RM — Obligatorio solo para Motocicleta</option>
                  <option value="NO">NO — Opcional / No Obligatorio</option>
                </select>
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-300 mb-1">Descripción</label>
                <textarea
                  rows={2}
                  required
                  value={docForm.descripcion}
                  onChange={(e) => setDocForm({ ...docForm, descripcion: e.target.value })}
                  placeholder="Descripción normativa del documento..."
                  className="w-full px-3 py-2 rounded-lg bg-slate-950 border border-slate-700 text-xs text-white"
                />
              </div>

              <button
                type="submit"
                className="w-full py-2.5 rounded-xl bg-indigo-600 hover:bg-indigo-500 text-white text-xs font-bold transition"
              >
                {docForm.id ? 'Actualizar Documento Paramétrico (PUT)' : 'Crear Documento Paramétrico (POST)'}
              </button>
            </form>

            <div className="pt-3 border-t border-slate-800">
              <button
                type="button"
                onClick={handleTestInvalidCheckDocumento}
                className="w-full px-3 py-2 rounded-lg bg-rose-950/50 hover:bg-rose-900/60 text-rose-300 border border-rose-500/40 text-xs font-semibold transition"
              >
                ⚡ Probar Violación CHECK en BD (Enviar aplicaA = "CAMION_INVALIDO")
              </button>
            </div>
          </div>

          <div className="lg:col-span-7 bg-slate-900 border border-slate-800 rounded-2xl p-5">
            <h3 className="text-sm font-bold text-white mb-3">
              Catálogo Paramétrico de Documentos ({documentos.length})
            </h3>
            <div className="overflow-x-auto">
              <table className="w-full text-left border-collapse text-xs">
                <thead>
                  <tr className="border-b border-slate-800 text-slate-400 uppercase text-[11px]">
                    <th className="py-2.5 px-2">ID / Código</th>
                    <th className="py-2.5 px-2">Nombre y Descripción</th>
                    <th className="py-2.5 px-2">Aplica A (CHECK)</th>
                    <th className="py-2.5 px-2">Obligatorio (CHECK)</th>
                    <th className="py-2.5 px-2 text-right">Acciones</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-800/70">
                  {documentos.map((d) => (
                    <tr key={d.id} className="hover:bg-slate-800/40">
                      <td className="py-2.5 px-2 font-mono">
                        <div className="font-bold text-indigo-300">{d.codigo}</div>
                        <div className="text-[11px] text-slate-500">ID #{d.id}</div>
                      </td>
                      <td className="py-2.5 px-2">
                        <div className="font-semibold text-white">{d.nombre}</div>
                        <div className="text-[11px] text-slate-400">{d.descripcion}</div>
                      </td>
                      <td className="py-2.5 px-2">
                        <span className="px-2 py-0.5 rounded bg-sky-500/20 text-sky-300 border border-sky-500/30 font-mono font-bold">
                          {d.aplicaA}
                        </span>
                      </td>
                      <td className="py-2.5 px-2">
                        <span className="px-2 py-0.5 rounded bg-amber-500/20 text-amber-300 border border-amber-500/30 font-mono font-bold">
                          {d.obligatorio}
                        </span>
                      </td>
                      <td className="py-2.5 px-2 text-right">
                        <div className="flex items-center justify-end gap-1">
                          <button
                            onClick={() => setDocForm(d)}
                            className="p-1.5 rounded bg-slate-800 hover:bg-indigo-600 text-slate-300 hover:text-white"
                          >
                            <Edit3 className="w-3.5 h-3.5" />
                          </button>
                          <button
                            onClick={() => handleDeleteDocumento(d.id, d.nombre)}
                            className="p-1.5 rounded bg-slate-800 hover:bg-rose-600 text-slate-300 hover:text-white"
                          >
                            <Trash2 className="w-3.5 h-3.5" />
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      )}

      {/* =====================================================================
          SUB-TAB 3: ASOCIAR NUEVOS DOCUMENTOS A UN VEHÍCULO EXISTENTE
         ===================================================================== */}
      {subTab === 'asociar' && (
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-5">
          <div className="lg:col-span-5 bg-slate-900 border border-slate-800 rounded-2xl p-5 space-y-4">
            <h3 className="text-sm font-bold text-white flex items-center gap-2 border-b border-slate-800 pb-3">
              <FilePlus className="w-4 h-4 text-indigo-400" />
              <span>Asociar Documento a Vehículo Existente (POST /api/vehiculos/&#123;id&#125;/documentos)</span>
            </h3>

            <form onSubmit={handleAsociarDocumentoExistente} className="space-y-3">
              <div>
                <label className="block text-xs font-semibold text-slate-300 mb-1">Seleccionar Vehículo</label>
                <select
                  value={asocForm.vehiculoId}
                  onChange={(e) => setAsocForm({ ...asocForm, vehiculoId: e.target.value })}
                  className="w-full px-3 py-2 rounded-lg bg-slate-950 border border-slate-700 text-xs text-white"
                >
                  {vehiculos.map((v) => (
                    <option key={v.id} value={v.id}>
                      {v.placa} — {v.tipoVehiculo} ({v.marca} {v.linea})
                    </option>
                  ))}
                </select>
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-300 mb-1">
                  Seleccionar Documento Paramétrico (Valida compatibilidad A / M / AM)
                </label>
                <select
                  value={asocForm.documentoId}
                  onChange={(e) => setAsocForm({ ...asocForm, documentoId: e.target.value })}
                  className="w-full px-3 py-2 rounded-lg bg-slate-950 border border-slate-700 text-xs text-white"
                >
                  {documentos.map((d) => (
                    <option key={d.id} value={d.id}>
                      {d.codigo} - {d.nombre} (Aplica a: {d.aplicaA})
                    </option>
                  ))}
                </select>
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-semibold text-slate-300 mb-1">Fecha Expedición</label>
                  <input
                    type="date"
                    value={asocForm.fechaExpedicion}
                    onChange={(e) => setAsocForm({ ...asocForm, fechaExpedicion: e.target.value })}
                    className="w-full px-3 py-2 rounded-lg bg-slate-950 border border-slate-700 text-xs text-white"
                  />
                </div>
                <div>
                  <label className="block text-xs font-semibold text-slate-300 mb-1">Fecha Vencimiento</label>
                  <input
                    type="date"
                    value={asocForm.fechaVencimiento}
                    onChange={(e) => setAsocForm({ ...asocForm, fechaVencimiento: e.target.value })}
                    className="w-full px-3 py-2 rounded-lg bg-slate-950 border border-slate-700 text-xs text-white"
                  />
                </div>
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-300 mb-1">
                  Estado del Documento (<code className="text-indigo-400">CHECK: Habilitado, Vencido, En Verificación</code>)
                </label>
                <select
                  value={asocForm.estado}
                  onChange={(e) => setAsocForm({ ...asocForm, estado: e.target.value })}
                  className="w-full px-3 py-2 rounded-lg bg-slate-950 border border-slate-700 text-xs text-white"
                >
                  <option value="En Verificación">En Verificación</option>
                  <option value="Habilitado">Habilitado</option>
                  <option value="Vencido">Vencido</option>
                </select>
              </div>

              <button
                type="submit"
                className="w-full py-2.5 rounded-xl bg-indigo-600 hover:bg-indigo-500 text-white text-xs font-bold transition"
              >
                Asociar Documento al Vehículo
              </button>
            </form>
          </div>

          {/* Gestión Rápida de Estados de Documentos Asociados */}
          <div className="lg:col-span-7 bg-slate-900 border border-slate-800 rounded-2xl p-5">
            <h3 className="text-sm font-bold text-white mb-2">
              Control de Estados de Documentos por Vehículo (<code className="text-indigo-300">Habilitado</code> / <code className="text-amber-300">En Verificación</code> / <code className="text-rose-300">Vencido</code>)
            </h3>
            <p className="text-xs text-slate-400 mb-3">
              Cambia el estado de cualquier documento con un clic para probar las consultas de Entrega 1, 2 y 3
            </p>

            <div className="space-y-3 max-h-[55vh] overflow-y-auto pr-1">
              {vehiculos.map((v) => (
                <div key={v.id} className="p-3 rounded-xl bg-slate-950 border border-slate-800 space-y-2">
                  <div className="flex items-center justify-between">
                    <span className="font-mono font-bold text-amber-300">
                      {v.placa} <span className="text-xs font-sans text-slate-300">— {v.tipoVehiculo} ({v.marca} {v.linea})</span>
                    </span>
                    <span className="text-[11px] text-slate-500">ID Vehículo #{v.id}</span>
                  </div>
                  <div className="divide-y divide-slate-800/60">
                    {(v.documentos || []).map((docRel) => (
                      <div key={docRel.id} className="py-2 flex flex-wrap items-center justify-between gap-2 text-xs">
                        <div>
                          <span className="font-semibold text-white">{docRel.documento?.nombre}</span>
                          <span className="text-[11px] text-slate-400 ml-2">
                            ({docRel.fechaExpedicion} → {docRel.fechaVencimiento})
                          </span>
                        </div>
                        <div className="flex items-center gap-1">
                          {['Habilitado', 'En Verificación', 'Vencido'].map((est) => (
                            <button
                              key={est}
                              onClick={() => handleCambiarEstadoDocVehiculo(docRel.id, est)}
                              className={`px-2 py-0.5 rounded text-[10px] font-bold border transition ${
                                docRel.estado === est
                                  ? getDocEstadoBadge(est)
                                  : 'bg-slate-900 text-slate-400 border-slate-800 hover:bg-slate-800'
                              }`}
                            >
                              {est}
                            </button>
                          ))}
                        </div>
                      </div>
                    ))}
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      )}

      {/* =====================================================================
          SUB-TAB 4: CENTRO DE 4 CONSULTAS DE LA ENTREGA 1
         ===================================================================== */}
      {subTab === 'consultas' && (
        <div className="bg-slate-900 border border-slate-800 rounded-2xl p-5 space-y-4">
          <div className="flex flex-wrap items-center justify-between gap-3 border-b border-slate-800 pb-3">
            <div>
              <h3 className="text-sm font-bold text-white flex items-center gap-2">
                <Filter className="w-4 h-4 text-indigo-400" />
                <span>Centro de las 4 Consultas Específicas de la Entrega 1</span>
              </h3>
              <p className="text-xs text-slate-400">
                Selecciona cualquiera de los 4 criterios exigidos para invocar su endpoint dedicado en el Backend
              </p>
            </div>

            <div className="flex flex-wrap gap-2">
              {[
                { id: 'placa', label: '3.1. Por Número de Placa' },
                { id: 'tipo', label: '3.2. Por Tipo de Vehículo' },
                { id: 'documento', label: '3.3. Por Tipo de Documento en Común' },
                { id: 'estado', label: '3.4. Por Estado del Documento' }
              ].map((c) => (
                <button
                  key={c.id}
                  onClick={() => setConsultaTipo(c.id)}
                  className={`px-3 py-1.5 rounded-lg text-xs font-semibold border transition ${
                    consultaTipo === c.id
                      ? 'bg-indigo-600 text-white border-indigo-500'
                      : 'bg-slate-950 text-slate-300 border-slate-800 hover:bg-slate-800'
                  }`}
                >
                  {c.label}
                </button>
              ))}
            </div>
          </div>

          <form onSubmit={ejecutarConsultaE1} className="flex flex-wrap items-end gap-3 bg-slate-950 p-4 rounded-xl border border-slate-800">
            {consultaTipo === 'placa' && (
              <div className="flex-1 min-w-[220px]">
                <label className="block text-xs font-semibold text-slate-300 mb-1">
                  Número de Placa (<code className="text-sky-300">GET /api/vehiculos/placa/&#123;placa&#125;</code>)
                </label>
                <div className="flex gap-2">
                  <input
                    type="text"
                    value={filtroPlaca}
                    onChange={(e) => setFiltroPlaca(e.target.value.toUpperCase())}
                    placeholder="Ej: ABC123"
                    className="flex-1 px-3 py-2 rounded-lg bg-slate-900 border border-slate-700 text-xs font-mono text-amber-300"
                  />
                  <select
                    onChange={(e) => e.target.value && setFiltroPlaca(e.target.value)}
                    value=""
                    className="px-2.5 py-2 rounded-lg bg-slate-900 border border-slate-700 text-xs text-slate-300"
                  >
                    <option value="">Elegir placa registrada...</option>
                    {vehiculos.map((v) => (
                      <option key={v.id} value={v.placa}>
                        {v.placa} ({v.marca})
                      </option>
                    ))}
                  </select>
                </div>
              </div>
            )}

            {consultaTipo === 'tipo' && (
              <div className="flex-1 min-w-[220px]">
                <label className="block text-xs font-semibold text-slate-300 mb-1">
                  Tipo de Vehículo (<code className="text-sky-300">GET /api/vehiculos/tipo/&#123;tipoVehiculo&#125;</code>)
                </label>
                <select
                  value={filtroTipoVehiculo}
                  onChange={(e) => setFiltroTipoVehiculo(e.target.value)}
                  className="w-full px-3 py-2 rounded-lg bg-slate-900 border border-slate-700 text-xs text-white"
                >
                  <option value="Automóvil">Automóvil</option>
                  <option value="Motocicleta">Motocicleta</option>
                </select>
              </div>
            )}

            {consultaTipo === 'documento' && (
              <div className="flex-1 min-w-[220px]">
                <label className="block text-xs font-semibold text-slate-300 mb-1">
                  Tipo de Documento en Común (<code className="text-sky-300">GET /api/vehiculos/documento/&#123;documentoId&#125;</code>)
                </label>
                <select
                  value={filtroDocumentoId}
                  onChange={(e) => setFiltroDocumentoId(e.target.value)}
                  className="w-full px-3 py-2 rounded-lg bg-slate-900 border border-slate-700 text-xs text-white"
                >
                  {documentos.map((d) => (
                    <option key={d.id} value={d.id}>
                      ID #{d.id} — {d.nombre} ({d.codigo})
                    </option>
                  ))}
                </select>
              </div>
            )}

            {consultaTipo === 'estado' && (
              <div className="flex-1 min-w-[220px]">
                <label className="block text-xs font-semibold text-slate-300 mb-1">
                  Estado del Documento Asociado (<code className="text-sky-300">GET /api/vehiculos/estado-documento/&#123;estado&#125;</code>)
                </label>
                <select
                  value={filtroEstadoDoc}
                  onChange={(e) => setFiltroEstadoDoc(e.target.value)}
                  className="w-full px-3 py-2 rounded-lg bg-slate-900 border border-slate-700 text-xs text-white"
                >
                  <option value="Habilitado">Habilitado</option>
                  <option value="En Verificación">En Verificación</option>
                  <option value="Vencido">Vencido</option>
                </select>
              </div>
            )}

            <button
              type="submit"
              className="px-5 py-2 rounded-xl bg-indigo-600 hover:bg-indigo-500 text-white text-xs font-bold flex items-center gap-1.5 transition"
            >
              <Search className="w-4 h-4" />
              <span>Ejecutar Consulta en Backend</span>
            </button>
          </form>

          {/* Resultados de la Consulta */}
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-3">
            {resultadoConsulta.length === 0 ? (
              <div className="col-span-full py-8 text-center text-xs text-slate-500">
                Haz clic en "Ejecutar Consulta en Backend" para visualizar los vehículos retornados por el servicio.
              </div>
            ) : (
              resultadoConsulta.map((v) => (
                <div key={v.id} className="p-4 rounded-xl bg-slate-950 border border-slate-800 space-y-2">
                  <div className="flex items-center justify-between">
                    <span className="font-mono font-bold text-base text-amber-300">{v.placa}</span>
                    <span className="px-2 py-0.5 rounded text-[11px] bg-indigo-500/20 text-indigo-300 border border-indigo-500/30">
                      {v.tipoVehiculo}
                    </span>
                  </div>
                  <div className="text-xs text-white font-semibold">
                    {v.marca} {v.linea} • Modelo {v.modelo}
                  </div>
                  <div className="text-[11px] text-slate-400">
                    Servicio: {v.tipoServicio} • Combustible: {v.tipoCombustible} • Pasajeros: {v.capacidadPasajeros}
                  </div>
                  <div className="pt-2 border-t border-slate-800 space-y-1">
                    <div className="text-[10px] font-bold uppercase text-slate-400">Documentos asociados:</div>
                    {(v.documentos || []).map((d) => (
                      <div key={d.id} className="flex items-center justify-between text-[11px]">
                        <span className="text-slate-300">{d.documento?.nombre}</span>
                        <span className={`px-1.5 py-0.5 rounded text-[10px] font-bold border ${getDocEstadoBadge(d.estado)}`}>
                          {d.estado}
                        </span>
                      </div>
                    ))}
                  </div>
                </div>
              ))
            )}
          </div>
        </div>
      )}
    </div>
  );
}

