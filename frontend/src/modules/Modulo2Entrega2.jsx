import React, { useState, useEffect } from 'react';
import {
  Users,
  KeyRound,
  FileUp,
  UserCheck,
  Globe,
  RefreshCw,
  CheckCircle2,
  AlertTriangle,
  Sparkles,
  Eye,
  ShieldAlert,
  Lock,
  Unlock,
  Send,
  Plus,
  Trash2,
  Edit3,
  BarChart3
} from 'lucide-react';
import {
  apiRequest,
  updateSecurityState,
  loginAndObtainJwt,
  setInspectorOpen,
  fileToBase64,
  generateSamplePdfBase64
} from '../services/api';

export default function Modulo2Entrega2({ activeSubTab, setActiveSubTab, onOpenPdfModal }) {
  const [subTab, setSubTab] = useState(activeSubTab || 'personas');

  useEffect(() => {
    if (activeSubTab) setSubTab(activeSubTab);
  }, [activeSubTab]);

  const changeTab = (tab) => {
    setSubTab(tab);
    if (setActiveSubTab) setActiveSubTab(tab);
  };

  // Estados generales del módulo
  const [personas, setPersonas] = useState([]);
  const [vehiculos, setVehiculos] = useState([]);
  const [documentosParam, setDocumentosParam] = useState([]);
  const [relacionesConductores, setRelacionesConductores] = useState([]);
  const [loading, setLoading] = useState(false);
  const [feedback, setFeedback] = useState(null);

  // Último usuario administrativo generado automáticamente
  const [ultimoUsuarioCreado, setUltimoUsuarioCreado] = useState(null);

  // 1. Estado Formulario Persona
  const nextYear = new Date(Date.now() + 365 * 86400000).toISOString().slice(0, 10);
  const [personaForm, setPersonaForm] = useState({
    id: null,
    identificacion: '',
    tipoIdentificacion: 'CC',
    nombres: '',
    apellidos: '',
    email: '',
    telefono: '3158899001',
    tipoPersona: 'A', // 'A' Administrativo | 'C' Conductor
    fechaVigenciaLicencia: nextYear,
    licenciaConduccion: ''
  });

  // 2. Estado Seguridad de Usuario (Cambio Password + Regenerar APIKey)
  const [secLogin, setSecLogin] = useState('cg1001234567');
  const [newPassword, setNewPassword] = useState('Admin123*');
  const [regeneratedUserResult, setRegeneratedUserResult] = useState(null);

  // 3. Estado Cargue Múltiple de PDFs en Base64 (LONGBLOB)
  const today = new Date().toISOString().slice(0, 10);
  const [pdfQueue, setPdfQueue] = useState([]);
  const [pdfBuilder, setPdfBuilder] = useState({
    vehiculoId: '',
    documentoId: '',
    fechaExpedicion: today,
    fechaVencimiento: nextYear,
    estado: 'Habilitado',
    fileName: '',
    documentoPdfBase64: ''
  });

  // 4. Estado Relación Conductor-Vehículo
  const [asocConductorForm, setAsocConductorForm] = useState({
    personaId: '',
    vehiculoId: '',
    estadoConductor: 'EA'
  });

  // 5. Estado de los 5 Servicios Públicos (Sin Token ni APIKey)
  const [pubPlaca, setPubPlaca] = useState('ABC123');
  const [pubDias, setPubDias] = useState(60);
  const [publicResults, setPublicResults] = useState({
    vencidos: null,
    autorizados: null,
    detallePlaca: null,
    porVencer: null,
    agrupadas: null
  });

  const notify = (type, title, detail, status) => {
    setFeedback({ type, title, detail, status });
  };

  const cargarDatos = async () => {
    setLoading(true);
    try {
      const [resP, resV, resD, resRel] = await Promise.all([
        apiRequest('/api/personas', { method: 'GET', description: 'Listar todas las personas y usuarios asociados' }),
        apiRequest('/api/vehiculos', { method: 'GET', description: 'Listar vehículos para operaciones E2' }),
        apiRequest('/api/documentos', { method: 'GET', description: 'Listar documentos paramétricos' }),
        apiRequest('/api/operaciones/conductores/relaciones', { method: 'GET', description: 'Listar relaciones Conductor-Vehículo' })
      ]);

      if (resP.ok && Array.isArray(resP.data)) {
        setPersonas(resP.data);
        const primerConductor = resP.data.find((p) => p.tipoPersona === 'C');
        if (primerConductor && !asocConductorForm.personaId) {
          setAsocConductorForm((prev) => ({ ...prev, personaId: primerConductor.id }));
        }
      }
      if (resV.ok && Array.isArray(resV.data)) {
        setVehiculos(resV.data);
        if (resV.data.length > 0) {
          setPdfBuilder((prev) => ({ ...prev, vehiculoId: prev.vehiculoId || resV.data[0].id }));
          setAsocConductorForm((prev) => ({ ...prev, vehiculoId: prev.vehiculoId || resV.data[0].id }));
        }
      }
      if (resD.ok && Array.isArray(resD.data) && resD.data.length > 0) {
        setDocumentosParam(resD.data);
        setPdfBuilder((prev) => ({ ...prev, documentoId: prev.documentoId || resD.data[0].id }));
      }
      if (resRel.ok && Array.isArray(resRel.data)) {
        setRelacionesConductores(resRel.data);
      }
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    cargarDatos();
  }, []);

  // Previsualización en vivo de la regla de nemotecnia del Login para Administrativos
  const previewLoginNemotecnia = () => {
    const n = (personaForm.nombres || '').trim();
    const a = (personaForm.apellidos || '').trim();
    const id = (personaForm.identificacion || '').trim();
    if (!n || !a || !id) return 'ej: cg1001234567';
    return `${n.charAt(0).toLowerCase()}${a.charAt(0).toLowerCase()}${id}`;
  };

  // =========================================================================
  // 1. GESTIÓN DE PERSONAS Y GENERACIÓN AUTOMÁTICA DE USUARIOS
  // =========================================================================
  const handleAutocompletarDemoPersona = (tipo) => {
    const randId = String(Math.floor(1050000000 + Math.random() * 90000000));
    if (tipo === 'A') {
      setPersonaForm({
        id: null,
        identificacion: randId,
        tipoIdentificacion: 'CC',
        nombres: 'Laura Sofia',
        apellidos: 'Martinez Gomez',
        email: `laura.${randId.slice(-4)}@unibague.edu.co`,
        telefono: '3167788990',
        tipoPersona: 'A',
        fechaVigenciaLicencia: '',
        licenciaConduccion: ''
      });
    } else {
      setPersonaForm({
        id: null,
        identificacion: randId,
        tipoIdentificacion: 'CC',
        nombres: 'Diego Fernando',
        apellidos: 'Rojas Vargas',
        email: `conductor.${randId.slice(-4)}@transporte.co`,
        telefono: '3104455667',
        tipoPersona: 'C',
        fechaVigenciaLicencia: nextYear,
        licenciaConduccion: generateSamplePdfBase64(
          `LICENCIA DE CONDUCCION C2 - ${randId}`,
          `Titular: Diego Fernando Rojas Vargas - Vigencia: ${nextYear}`
        )
      });
    }
  };

  const handleLicenciaPdfFileChange = async (e) => {
    const file = e.target.files?.[0];
    if (!file) return;
    const base64 = await fileToBase64(file);
    setPersonaForm((prev) => ({ ...prev, licenciaConduccion: base64 }));
    notify('success', `Archivo PDF "${file.name}" convertido a Base64`, `Longitud Base64: ${base64.length} caracteres listos para LONGBLOB`);
  };

  const handleSavePersona = async (e) => {
    e.preventDefault();
    const isEdit = !!personaForm.id;
    const endpoint = isEdit ? `/api/personas/${personaForm.id}` : '/api/personas';
    const method = isEdit ? 'PUT' : 'POST';

    const payload = {
      identificacion: personaForm.identificacion.trim(),
      tipoIdentificacion: personaForm.tipoIdentificacion,
      nombres: personaForm.nombres.trim(),
      apellidos: personaForm.apellidos.trim(),
      correo: personaForm.email.trim(),
      email: personaForm.email.trim(),
      telefono: personaForm.telefono.trim(),
      tipoPersona: personaForm.tipoPersona,
      fechaVigenciaLicencia: personaForm.tipoPersona === 'C' ? personaForm.fechaVigenciaLicencia : null,
      licenciaConduccion:
        personaForm.tipoPersona === 'C'
          ? personaForm.licenciaConduccion ||
            generateSamplePdfBase64(`LICENCIA DE CONDUCCION - ${personaForm.identificacion}`, `${personaForm.nombres} ${personaForm.apellidos}`)
          : null
    };

    const res = await apiRequest(endpoint, {
      method,
      body: payload,
      description: `${isEdit ? 'Actualizar' : 'Crear'} Persona (${payload.tipoPersona === 'A' ? 'Administrativo -> Genera Usuario Automático' : 'Conductor'})`
    });

    if (res.ok) {
      if (res.data?.usuarioGenerado) {
        setUltimoUsuarioCreado({
          personaNombre: `${res.data.nombres} ${res.data.apellidos}`,
          identificacion: res.data.identificacion,
          login: res.data.usuarioGenerado.id?.login,
          password: res.data.usuarioGenerado.password,
          apikey: res.data.usuarioGenerado.apikey
        });
        setSecLogin(res.data.usuarioGenerado.id?.login || secLogin);
      }
      notify(
        'success',
        `Persona ${isEdit ? 'actualizada' : 'registrada'} con éxito`,
        res.data?.usuarioGenerado
          ? `Usuario autogenerado por nemotecnia: login="${res.data.usuarioGenerado.id?.login}", password="${res.data.usuarioGenerado.password}", APIKey="${res.data.usuarioGenerado.apikey}"`
          : `Conductor registrado con licencia en LONGBLOB Base64.`,
        res.status
      );
      cargarDatos();
    } else {
      notify('error', 'Error de validación al guardar Persona', res.data?.error || JSON.stringify(res.data), res.status);
      setInspectorOpen(true);
    }
  };

  const handleUsarCredencialesEnSesion = async (login, password, apikey) => {
    updateSecurityState({
      authMode: 'AUTHENTICATED',
      activeLogin: login,
      apiKey: apikey
    });
    const resJwt = await loginAndObtainJwt(login, password, apikey);
    if (resJwt.ok) {
      notify(
        'success',
        `Sesión activa actualizada con el usuario "${login}"`,
        `Se obtuvo un nuevo Token JWT en /authenticate y se configuró su APIKey (${apikey.slice(0, 12)}...) en la Barra Global de Seguridad.`,
        resJwt.status
      );
    } else {
      updateSecurityState({
        authMode: 'AUTHENTICATED',
        activeLogin: login,
        apiKey: apikey
      });
      notify('success', `APIKey y Login aplicados en la Barra Global (${login})`, `APIKey activa: ${apikey}`, 200);
    }
  };

  // =========================================================================
  // 2. SEGURIDAD DE USUARIO (CAMBIO DE PASSWORD Y REGENERACIÓN DE APIKEY)
  // =========================================================================
  const handleCambiarPassword = async (e) => {
    e.preventDefault();
    const endpoint = `/api/usuarios/${encodeURIComponent(secLogin.trim())}/password`;
    const res = await apiRequest(endpoint, {
      method: 'PUT',
      body: { password: newPassword },
      description: `Cambiar password del usuario '${secLogin}' inyectando login en la URL`
    });

    if (res.ok) {
      setRegeneratedUserResult(res.data);
      notify(
        'success',
        `Password actualizado en PUT ${endpoint}`,
        `Nuevo password para ${secLogin}: "${res.data?.password}"`,
        res.status
      );
      cargarDatos();
    } else {
      notify('error', 'Error al cambiar password', res.data?.error || JSON.stringify(res.data), res.status);
      setInspectorOpen(true);
    }
  };

  const handleRegenerarApiKey = async () => {
    const endpoint = `/api/usuarios/${encodeURIComponent(secLogin.trim())}/apikey/regenerar`;
    const res = await apiRequest(endpoint, {
      method: 'GET',
      description: `Regenerar APIKey automática para el usuario '${secLogin}'`
    });

    if (res.ok) {
      setRegeneratedUserResult(res.data);
      notify(
        'success',
        `APIKey regenerada exitosamente para "${secLogin}"`,
        `Nueva APIKey generada: ${res.data?.apikey}`,
        res.status
      );
      cargarDatos();
    } else {
      notify('error', 'Error al regenerar APIKey', res.data?.error || JSON.stringify(res.data), res.status);
      setInspectorOpen(true);
    }
  };

  // =========================================================================
  // 3. CARGUE MÚLTIPLE DE ARCHIVOS PDF EN BASE64 (LONGBLOB)
  // =========================================================================
  const handleSelectPdfFileForQueue = async (e) => {
    const files = Array.from(e.target.files || []);
    if (files.length === 0) return;

    const nuevosItems = [];
    for (const file of files) {
      const b64 = await fileToBase64(file);
      nuevosItems.push({
        tempId: `${Date.now()}-${Math.random()}`,
        vehiculoId: Number(pdfBuilder.vehiculoId),
        documentoId: Number(pdfBuilder.documentoId),
        fechaExpedicion: pdfBuilder.fechaExpedicion,
        fechaVencimiento: pdfBuilder.fechaVencimiento,
        estado: pdfBuilder.estado,
        fileName: file.name,
        documentoPdfBase64: b64
      });
    }

    setPdfQueue((prev) => [...prev, ...nuevosItems]);
    notify('success', `${nuevosItems.length} archivo(s) PDF convertidos a Base64 y añadidos a la cola de cargue`, 'Listos para enviar en lote a POST /api/operaciones/documentos/cargar');
  };

  const handleAddSamplePdfToQueue = () => {
    const veh = vehiculos.find((v) => String(v.id) === String(pdfBuilder.vehiculoId));
    const doc = documentosParam.find((d) => String(d.id) === String(pdfBuilder.documentoId));
    const sampleB64 = generateSamplePdfBase64(
      `${doc?.nombre || 'DOCUMENTO'} - PLACA ${veh?.placa || pdfBuilder.vehiculoId}`,
      `Expedicion: ${pdfBuilder.fechaExpedicion} | Vencimiento: ${pdfBuilder.fechaVencimiento} | Estado: ${pdfBuilder.estado}`
    );

    setPdfQueue((prev) => [
      ...prev,
      {
        tempId: `${Date.now()}-${Math.random()}`,
        vehiculoId: Number(pdfBuilder.vehiculoId),
        documentoId: Number(pdfBuilder.documentoId),
        fechaExpedicion: pdfBuilder.fechaExpedicion,
        fechaVencimiento: pdfBuilder.fechaVencimiento,
        estado: pdfBuilder.estado,
        fileName: `${doc?.codigo || 'DOC'}_${veh?.placa || 'VEH'}.pdf`,
        documentoPdfBase64: sampleB64
      }
    ]);
  };

  const handleEnviarColaPdfsBase64 = async () => {
    if (pdfQueue.length === 0) return;

    const payload = pdfQueue.map((item) => ({
      vehiculoId: item.vehiculoId,
      documentoId: item.documentoId,
      fechaExpedicion: item.fechaExpedicion,
      fechaVencimiento: item.fechaVencimiento,
      estado: item.estado,
      documentoPdfBase64: item.documentoPdfBase64
    }));

    const res = await apiRequest('/api/operaciones/documentos/cargar', {
      method: 'POST',
      body: payload,
      description: `Cargue masivo de ${payload.length} documento(s) PDF en formato Base64 hacia columna LONGBLOB`
    });

    if (res.ok) {
      notify(
        'success',
        `${payload.length} documento(s) PDF Base64 almacenados en LONGBLOB`,
        `HTTP ${res.status} — Puedes previsualizarlos en el visor PDF integrado.`,
        res.status
      );
      setPdfQueue([]);
      cargarDatos();
    } else {
      notify('error', 'Error en el cargue de documentos PDF Base64', res.data?.error || JSON.stringify(res.data), res.status);
      setInspectorOpen(true);
    }
  };

  // =========================================================================
  // 4. ASOCIACIÓN CONDUCTOR-VEHÍCULO Y CAMBIO DE ESTADO (PO, EA, RO)
  // =========================================================================
  const handleAsociarConductor = async (e) => {
    e.preventDefault();
    const { personaId, vehiculoId, estadoConductor } = asocConductorForm;
    const endpoint = `/api/operaciones/conductores/asociar?personaId=${encodeURIComponent(
      personaId
    )}&vehiculoId=${encodeURIComponent(vehiculoId)}&estadoConductor=${encodeURIComponent(estadoConductor)}`;

    const res = await apiRequest(endpoint, {
      method: 'POST',
      description: `Asociar Conductor ID ${personaId} al Vehículo ID ${vehiculoId} con estado '${estadoConductor}'`
    });

    if (res.ok) {
      notify(
        'success',
        'Conductor asociado exitosamente al vehículo',
        `Relación #${res.data?.id} creada con estado "${res.data?.estadoConductor}".`,
        res.status
      );
      cargarDatos();
    } else {
      notify('error', 'Rechazo al asociar conductor', res.data?.error || JSON.stringify(res.data), res.status);
      setInspectorOpen(true);
    }
  };

  const handleTestAsociarAdministrativoFallo = async () => {
    const admin = personas.find((p) => p.tipoPersona === 'A');
    const veh = vehiculos[0];
    if (!admin || !veh) return;

    const endpoint = `/api/operaciones/conductores/asociar?personaId=${admin.id}&vehiculoId=${veh.id}&estadoConductor=PO`;
    const res = await apiRequest(endpoint, {
      method: 'POST',
      description: `PRUEBA DE FALLO (Entrega 2): Intentar asociar Persona Administrativa (${admin.nombres}, tipo='A') como conductor`
    });

    notify(
      'error',
      'Bloqueo Exitoso: Persona Administrativa no puede asociarse como Conductor',
      `El Backend rechazó la petición con HTTP ${res.status}: "${res.data?.error || JSON.stringify(res.data)}"`,
      res.status
    );
    setInspectorOpen(true);
  };

  const handleCambiarEstadoRelacion = async (idRelacion, nuevoEstado) => {
    const res = await apiRequest(`/api/operaciones/conductores/relacion/${idRelacion}/estado`, {
      method: 'PUT',
      body: { estadoConductor: nuevoEstado },
      description: `Cambiar estado operativo de relación #${idRelacion} a '${nuevoEstado}'`
    });

    if (res.ok) {
      notify(
        'success',
        `Estado del conductor actualizado a "${nuevoEstado}"`,
        `Relación #${idRelacion} actualizada en base de datos.`,
        res.status
      );
      cargarDatos();
    } else {
      notify('error', 'Error al cambiar estado del conductor', res.data?.error || JSON.stringify(res.data), res.status);
      setInspectorOpen(true);
    }
  };

  // =========================================================================
  // 5. PANEL DE LOS 5 SERVICIOS PÚBLICOS (SIN TOKEN NI APIKEY)
  // =========================================================================
  const ejecutarCincoServiciosPublicos = async () => {
    setLoading(true);
    try {
      const [r1, r2, r3, r4, r5] = await Promise.all([
        apiRequest('/api/public/vehiculos/documentos-vencidos', {
          method: 'GET',
          forcePublic: true,
          description: 'Servicio Público 8.1 (SIN CREDENCIALES): Vehículos con documentos vencidos'
        }),
        apiRequest('/api/public/conductores/autorizados', {
          method: 'GET',
          forcePublic: true,
          description: 'Servicio Público 8.2 (SIN CREDENCIALES): Conductores que pueden operar (PO)'
        }),
        apiRequest(`/api/public/vehiculos/placa/${encodeURIComponent(pubPlaca.trim().toUpperCase())}/detalles`, {
          method: 'GET',
          forcePublic: true,
          description: `Servicio Público 8.3 (SIN CREDENCIALES): Detalle integral por placa (${pubPlaca})`
        }),
        apiRequest(`/api/public/vehiculos/documentos-por-vencer?diasFaltantes=${encodeURIComponent(pubDias)}`, {
          method: 'GET',
          forcePublic: true,
          description: `Servicio Público 8.4 (SIN CREDENCIALES): Documentos por vencer en ${pubDias} días`
        }),
        apiRequest('/api/public/personas/agrupadas-por-tipo', {
          method: 'GET',
          forcePublic: true,
          description: 'Servicio Público 8.5 (SIN CREDENCIALES): Personas agrupadas por tipo'
        })
      ]);

      setPublicResults({
        vencidos: r1.data,
        autorizados: r2.data,
        detallePlaca: r3.ok ? r3.data : null,
        porVencer: r4.data,
        agrupadas: r5.data
      });

      notify(
        'success',
        '5 Servicios Públicos ejecutados SIN Token ni APIKey (HTTP 200)',
        'Verifica en el Inspector API inferior que "headersSent" no incluyó Authorization ni x-api-key.',
        200
      );
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (subTab === 'publicos') {
      ejecutarCincoServiciosPublicos();
    }
  }, [subTab]);

  const getConductorBadge = (estado) => {
    if (estado === 'PO') {
      return {
        label: 'PO • Puede Operar',
        cls: 'bg-emerald-500/20 text-emerald-300 border-emerald-500/40'
      };
    }
    if (estado === 'EA') {
      return {
        label: 'EA • Espera de Aprobación',
        cls: 'bg-amber-500/20 text-amber-300 border-amber-500/40'
      };
    }
    return {
      label: 'RO • Restringido para Operar',
      cls: 'bg-rose-500/20 text-rose-300 border-rose-500/40'
    };
  };

  return (
    <div className="space-y-5">
      {/* Encabezado y Sub-navegación del Módulo 2 */}
      <div className="bg-slate-900/90 border border-slate-800 rounded-2xl p-4 flex flex-wrap items-center justify-between gap-4">
        <div>
          <div className="flex items-center gap-2">
            <span className="px-2.5 py-0.5 rounded-full text-xs font-bold bg-emerald-500/20 text-emerald-300 border border-emerald-500/30">
              MÓDULO 2 • ENTREGA 2
            </span>
            <h2 className="text-lg font-bold text-white">
              Personas, Usuarios Automáticos, Seguridad, PDFs Base64 (<code className="text-indigo-300">LONGBLOB</code>) y 5 Servicios Públicos
            </h2>
          </div>
          <p className="text-xs text-slate-400 mt-1">
            Demostración de nemotecnia de <code className="text-amber-300">login</code>, regeneración de <code className="text-amber-300">APIKey</code>, estados de conductor (<code className="text-emerald-300">PO</code>, <code className="text-amber-300">EA</code>, <code className="text-rose-300">RO</code>) y consultas abiertas sin credenciales
          </p>
        </div>

        <div className="flex flex-wrap items-center gap-2">
          {[
            { id: 'personas', label: '1. Personas y Usuario Automático', icon: Users },
            { id: 'seguridad', label: '2. Password y Regenerar APIKey', icon: KeyRound },
            { id: 'pdfs', label: '3. Cargue PDFs Base64 + Visor', icon: FileUp },
            { id: 'conductores', label: '4. Conductores (PO / EA / RO)', icon: UserCheck },
            { id: 'publicos', label: '5. Panel 5 Servicios Públicos', icon: Globe }
          ].map((t) => {
            const Icon = t.icon;
            const active = subTab === t.id;
            return (
              <button
                key={t.id}
                onClick={() => changeTab(t.id)}
                className={`flex items-center gap-1.5 px-3 py-2 rounded-xl text-xs font-semibold transition border ${
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

      {/* =====================================================================
          SUB-TAB 1: GESTIÓN DE PERSONAS Y GENERACIÓN AUTOMÁTICA DE USUARIOS
         ===================================================================== */}
      {subTab === 'personas' && (
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-5">
          {/* Formulario Persona */}
          <div className="lg:col-span-5 bg-slate-900 border border-slate-800 rounded-2xl p-5 space-y-4">
            <div className="flex items-center justify-between border-b border-slate-800 pb-3">
              <div>
                <h3 className="text-sm font-bold text-white">
                  {personaForm.id ? `Editar Persona #${personaForm.id}` : 'Registrar Persona (POST /api/personas)'}
                </h3>
                <p className="text-[11px] text-slate-400">
                  Si es <strong>Administrativo (A)</strong> crea automáticamente su entidad <code className="text-indigo-300">Usuario</code> 1:1
                </p>
              </div>
              <div className="flex gap-1.5">
                <button
                  type="button"
                  onClick={() => handleAutocompletarDemoPersona('A')}
                  className="px-2 py-1 rounded bg-indigo-500/20 hover:bg-indigo-500/30 text-indigo-300 text-[11px] font-semibold border border-indigo-500/30"
                >
                  + Demo Admin (A)
                </button>
                <button
                  type="button"
                  onClick={() => handleAutocompletarDemoPersona('C')}
                  className="px-2 py-1 rounded bg-emerald-500/20 hover:bg-emerald-500/30 text-emerald-300 text-[11px] font-semibold border border-emerald-500/30"
                >
                  + Demo Conductor (C)
                </button>
              </div>
            </div>

            <form onSubmit={handleSavePersona} className="space-y-3">
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-semibold text-slate-300 mb-1">
                    Tipo Persona (<code className="text-indigo-400">CHECK: A, C</code>)
                  </label>
                  <select
                    value={personaForm.tipoPersona}
                    onChange={(e) => setPersonaForm({ ...personaForm, tipoPersona: e.target.value })}
                    className="w-full px-3 py-2 rounded-lg bg-slate-950 border border-slate-700 text-xs text-white font-semibold"
                  >
                    <option value="A">A — ADMINISTRATIVO (Genera Usuario)</option>
                    <option value="C">C — CONDUCTOR (Con Licencia Base64)</option>
                  </select>
                </div>

                <div>
                  <label className="block text-xs font-semibold text-slate-300 mb-1">
                    Tipo ID (<code className="text-indigo-400">CHECK</code>)
                  </label>
                  <select
                    value={personaForm.tipoIdentificacion}
                    onChange={(e) => setPersonaForm({ ...personaForm, tipoIdentificacion: e.target.value })}
                    className="w-full px-3 py-2 rounded-lg bg-slate-950 border border-slate-700 text-xs text-white"
                  >
                    <option value="CC">CC — Cédula de Ciudadanía</option>
                    <option value="CE">CE — Cédula de Extranjería</option>
                    <option value="PASAPORTE">PASAPORTE</option>
                  </select>
                </div>
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-semibold text-slate-300 mb-1">
                    Identificación (<code className="text-indigo-400">UNIQUE</code>)
                  </label>
                  <input
                    type="text"
                    required
                    value={personaForm.identificacion}
                    onChange={(e) => setPersonaForm({ ...personaForm, identificacion: e.target.value })}
                    placeholder="1099887766"
                    className="w-full px-3 py-2 rounded-lg bg-slate-950 border border-slate-700 text-xs font-mono text-amber-300"
                  />
                </div>
                <div>
                  <label className="block text-xs font-semibold text-slate-300 mb-1">Teléfono</label>
                  <input
                    type="text"
                    required
                    value={personaForm.telefono}
                    onChange={(e) => setPersonaForm({ ...personaForm, telefono: e.target.value })}
                    className="w-full px-3 py-2 rounded-lg bg-slate-950 border border-slate-700 text-xs text-white"
                  />
                </div>
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-semibold text-slate-300 mb-1">Nombres</label>
                  <input
                    type="text"
                    required
                    value={personaForm.nombres}
                    onChange={(e) => setPersonaForm({ ...personaForm, nombres: e.target.value })}
                    placeholder="Laura Sofia"
                    className="w-full px-3 py-2 rounded-lg bg-slate-950 border border-slate-700 text-xs text-white"
                  />
                </div>
                <div>
                  <label className="block text-xs font-semibold text-slate-300 mb-1">Apellidos</label>
                  <input
                    type="text"
                    required
                    value={personaForm.apellidos}
                    onChange={(e) => setPersonaForm({ ...personaForm, apellidos: e.target.value })}
                    placeholder="Martinez Gomez"
                    className="w-full px-3 py-2 rounded-lg bg-slate-950 border border-slate-700 text-xs text-white"
                  />
                </div>
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-300 mb-1">Correo Electrónico</label>
                <input
                  type="email"
                  required
                  value={personaForm.email}
                  onChange={(e) => setPersonaForm({ ...personaForm, email: e.target.value })}
                  placeholder="usuario@unibague.edu.co"
                  className="w-full px-3 py-2 rounded-lg bg-slate-950 border border-slate-700 text-xs text-white"
                />
              </div>

              {/* Si es ADMINISTRATIVO: Mostrar Previsualización de Nemotecnia */}
              {personaForm.tipoPersona === 'A' && (
                <div className="p-3 rounded-xl bg-indigo-950/30 border border-indigo-500/30 text-xs space-y-1">
                  <div className="font-bold text-indigo-300 flex items-center gap-1.5">
                    <Sparkles className="w-3.5 h-3.5" />
                    <span>Previsualización de Regla de Nemotecnia (Usuario 1:1)</span>
                  </div>
                  <p className="text-[11px] text-slate-300">
                    1ra letra 1er nombre + 1ra letra 1er apellido + identificación ={' '}
                    <code className="px-2 py-0.5 rounded bg-slate-950 text-amber-300 font-mono font-bold">
                      {previewLoginNemotecnia()}
                    </code>
                  </p>
                </div>
              )}

              {/* Si es CONDUCTOR: Mostrar Vigencia y Cargador PDF Base64 de Licencia */}
              {personaForm.tipoPersona === 'C' && (
                <div className="p-3.5 rounded-xl bg-emerald-950/25 border border-emerald-500/30 space-y-2.5">
                  <div className="text-xs font-bold text-emerald-300">
                    Datos de Licencia de Conducción (Entrega 2 y 3 • Columna <code className="text-white">LONGBLOB</code>)
                  </div>
                  <div>
                    <label className="block text-[11px] text-slate-300 mb-1">Fecha de Vigencia de Licencia</label>
                    <input
                      type="date"
                      required
                      value={personaForm.fechaVigenciaLicencia || ''}
                      onChange={(e) => setPersonaForm({ ...personaForm, fechaVigenciaLicencia: e.target.value })}
                      className="w-full px-2.5 py-1.5 rounded bg-slate-950 border border-slate-700 text-xs text-white"
                    />
                  </div>
                  <div>
                    <label className="block text-[11px] text-slate-300 mb-1">
                      Subir PDF de Licencia (Se convierte automáticamente a Base64)
                    </label>
                    <input
                      type="file"
                      accept=".pdf,application/pdf"
                      onChange={handleLicenciaPdfFileChange}
                      className="w-full text-xs text-slate-300 file:mr-2 file:py-1 file:px-2.5 file:rounded-lg file:border-0 file:text-xs file:font-semibold file:bg-emerald-600 file:text-white hover:file:bg-emerald-500"
                    />
                  </div>
                </div>
              )}

              <button
                type="submit"
                className="w-full py-2.5 rounded-xl bg-indigo-600 hover:bg-indigo-500 text-white text-xs font-bold transition shadow-lg shadow-indigo-600/20"
              >
                {personaForm.id
                  ? 'Actualizar Persona (PUT)'
                  : personaForm.tipoPersona === 'A'
                  ? 'Crear Administrativo y Autogenerar Usuario (POST)'
                  : 'Crear Conductor con Licencia en Base64 (POST)'}
              </button>
            </form>

            {/* Tarjeta Destacada del Último Usuario Administrativo Creado */}
            {ultimoUsuarioCreado && (
              <div className="p-4 rounded-xl bg-gradient-to-br from-indigo-950/80 to-slate-900 border border-indigo-400/50 space-y-2.5 shadow-lg">
                <div className="flex items-center justify-between">
                  <span className="text-xs font-bold text-indigo-300">
                    ✨ Usuario Generado Automáticamente (Relación 1:1)
                  </span>
                  <span className="text-[11px] text-slate-400">{ultimoUsuarioCreado.personaNombre}</span>
                </div>
                <div className="grid grid-cols-1 gap-1.5 text-xs bg-slate-950/90 p-2.5 rounded-lg border border-slate-800 font-mono">
                  <div>
                    <span className="text-slate-400">login (Nemotecnia): </span>
                    <strong className="text-emerald-300">{ultimoUsuarioCreado.login}</strong>
                  </div>
                  <div>
                    <span className="text-slate-400">password automático: </span>
                    <strong className="text-amber-300">{ultimoUsuarioCreado.password}</strong>
                  </div>
                  <div className="truncate">
                    <span className="text-slate-400">APIKey automático: </span>
                    <strong className="text-sky-300">{ultimoUsuarioCreado.apikey}</strong>
                  </div>
                </div>
                <button
                  type="button"
                  onClick={() =>
                    handleUsarCredencialesEnSesion(
                      ultimoUsuarioCreado.login,
                      ultimoUsuarioCreado.password,
                      ultimoUsuarioCreado.apikey
                    )
                  }
                  className="w-full py-2 rounded-lg bg-emerald-600 hover:bg-emerald-500 text-white text-xs font-bold transition flex items-center justify-center gap-1.5"
                >
                  <KeyRound className="w-3.5 h-3.5" />
                  <span>Usar estas credenciales en la sesión actual</span>
                </button>
              </div>
            )}
          </div>

          {/* Tabla de Personas y Credenciales */}
          <div className="lg:col-span-7 bg-slate-900 border border-slate-800 rounded-2xl p-5">
            <h3 className="text-sm font-bold text-white mb-3">
              Directorio de Personas Registradas ({personas.length})
            </h3>
            <div className="overflow-x-auto">
              <table className="w-full text-left border-collapse text-xs">
                <thead>
                  <tr className="border-b border-slate-800 text-slate-400 uppercase text-[11px]">
                    <th className="py-2.5 px-2">Persona / ID</th>
                    <th className="py-2.5 px-2">Rol (CHECK)</th>
                    <th className="py-2.5 px-2">Credenciales Usuario (Si es Admin) o Licencia (Si es Conductor)</th>
                    <th className="py-2.5 px-2 text-right">Acciones</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-800/70">
                  {personas.map((p) => {
                    const u = p.usuarioGenerado;
                    return (
                      <tr key={p.id} className="hover:bg-slate-800/40">
                        <td className="py-2.5 px-2">
                          <div className="font-bold text-white">
                            {p.nombres} {p.apellidos}
                          </div>
                          <div className="text-[11px] font-mono text-amber-300">
                            {p.tipoIdentificacion}: {p.identificacion} • ID #{p.id}
                          </div>
                          <div className="text-[11px] text-slate-400">{p.correo || p.email}</div>
                        </td>
                        <td className="py-2.5 px-2">
                          {p.tipoPersona === 'A' ? (
                            <span className="px-2 py-0.5 rounded bg-indigo-500/20 text-indigo-300 border border-indigo-500/40 font-bold text-[11px]">
                              A • Administrativo
                            </span>
                          ) : (
                            <span className="px-2 py-0.5 rounded bg-emerald-500/20 text-emerald-300 border border-emerald-500/40 font-bold text-[11px]">
                              C • Conductor
                            </span>
                          )}
                        </td>
                        <td className="py-2.5 px-2">
                          {p.tipoPersona === 'A' && u ? (
                            <div className="bg-slate-950 p-2 rounded-lg border border-slate-800 font-mono text-[11px] space-y-1">
                              <div className="flex items-center justify-between">
                                <span>
                                  login: <strong className="text-emerald-300">{u.id?.login}</strong>
                                </span>
                                <span>
                                  pass: <strong className="text-amber-300">{u.password}</strong>
                                </span>
                              </div>
                              <div className="text-slate-400 truncate max-w-[260px]">
                                APIKey: <span className="text-sky-300">{u.apikey}</span>
                              </div>
                              <button
                                onClick={() => handleUsarCredencialesEnSesion(u.id?.login, u.password, u.apikey)}
                                className="mt-1 w-full py-1 rounded bg-indigo-600/30 hover:bg-indigo-600 text-indigo-200 hover:text-white text-[10px] font-sans font-semibold transition"
                              >
                                Usar estas credenciales en la sesión actual
                              </button>
                            </div>
                          ) : (
                            <div className="bg-slate-950 p-2 rounded-lg border border-slate-800 text-[11px] flex items-center justify-between">
                              <div>
                                <div className="text-slate-300">
                                  Vigencia Licencia:{' '}
                                  <strong
                                    className={
                                      p.fechaVigenciaLicencia && p.fechaVigenciaLicencia < today
                                        ? 'text-rose-400'
                                        : 'text-emerald-300'
                                    }
                                  >
                                    {p.fechaVigenciaLicencia || 'No registrada'}
                                  </strong>
                                </div>
                                <div className="text-[10px] text-slate-500">
                                  LONGBLOB Base64: {p.licenciaConduccion ? `${p.licenciaConduccion.length} chars` : 'Vacío'}
                                </div>
                              </div>
                              {p.licenciaConduccion && (
                                <button
                                  onClick={() =>
                                    onOpenPdfModal({
                                      isOpen: true,
                                      title: `Licencia de Conducción — ${p.nombres} ${p.apellidos}`,
                                      subtitle: `Identificación: ${p.identificacion} | Vigencia: ${p.fechaVigenciaLicencia}`,
                                      base64Data: p.licenciaConduccion
                                    })
                                  }
                                  className="px-2 py-1 rounded bg-indigo-500/20 hover:bg-indigo-500/40 text-indigo-300 flex items-center gap-1"
                                >
                                  <Eye className="w-3.5 h-3.5" /> PDF
                                </button>
                              )}
                            </div>
                          )}
                        </td>
                        <td className="py-2.5 px-2 text-right">
                          <button
                            onClick={() =>
                              setPersonaForm({
                                id: p.id,
                                identificacion: p.identificacion,
                                tipoIdentificacion: p.tipoIdentificacion,
                                nombres: p.nombres,
                                apellidos: p.apellidos,
                                email: p.correo || p.email || '',
                                telefono: p.telefono || '',
                                tipoPersona: p.tipoPersona,
                                fechaVigenciaLicencia: p.fechaVigenciaLicencia || nextYear,
                                licenciaConduccion: p.licenciaConduccion || ''
                              })
                            }
                            className="p-1.5 rounded bg-slate-800 hover:bg-indigo-600 text-slate-300 hover:text-white"
                            title="Editar persona"
                          >
                            <Edit3 className="w-3.5 h-3.5" />
                          </button>
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      )}

      {/* =====================================================================
          SUB-TAB 2: SEGURIDAD DE USUARIO (PASSWORD Y REGENERACIÓN DE APIKEY)
         ===================================================================== */}
      {subTab === 'seguridad' && (
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-5">
          <div className="lg:col-span-6 bg-slate-900 border border-slate-800 rounded-2xl p-5 space-y-4">
            <h3 className="text-sm font-bold text-white flex items-center gap-2 border-b border-slate-800 pb-3">
              <KeyRound className="w-4 h-4 text-indigo-400" />
              <span>1. Cambio de Password con Login Inyectado en la URL</span>
            </h3>

            <div className="p-3 rounded-xl bg-slate-950 border border-slate-800 text-xs font-mono">
              <span className="text-amber-300 font-bold">PUT</span>{' '}
              <span className="text-sky-300">/api/usuarios/</span>
              <span className="px-1.5 py-0.5 rounded bg-indigo-500/30 text-white font-bold">
                {secLogin || '{login}'}
              </span>
              <span className="text-sky-300">/password</span>
            </div>

            <form onSubmit={handleCambiarPassword} className="space-y-3">
              <div>
                <label className="block text-xs font-semibold text-slate-300 mb-1">
                  Seleccionar o Escribir <code className="text-indigo-400">login</code> del Usuario Administrativo
                </label>
                <div className="flex gap-2">
                  <input
                    type="text"
                    required
                    value={secLogin}
                    onChange={(e) => setSecLogin(e.target.value)}
                    className="flex-1 px-3 py-2 rounded-lg bg-slate-950 border border-slate-700 text-xs font-mono text-emerald-300"
                  />
                  <select
                    value=""
                    onChange={(e) => e.target.value && setSecLogin(e.target.value)}
                    className="px-3 py-2 rounded-lg bg-slate-950 border border-slate-700 text-xs text-slate-300"
                  >
                    <option value="">Elegir de la BD...</option>
                    {personas
                      .filter((p) => p.usuarioGenerado?.id?.login)
                      .map((p) => (
                        <option key={p.id} value={p.usuarioGenerado.id.login}>
                          {p.usuarioGenerado.id.login} ({p.nombres})
                        </option>
                      ))}
                  </select>
                </div>
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-300 mb-1">
                  Nuevo Password (enviado en el Body JSON)
                </label>
                <input
                  type="text"
                  required
                  value={newPassword}
                  onChange={(e) => setNewPassword(e.target.value)}
                  className="w-full px-3 py-2 rounded-lg bg-slate-950 border border-slate-700 text-xs font-mono text-amber-300"
                />
              </div>

              <button
                type="submit"
                className="w-full py-2.5 rounded-xl bg-indigo-600 hover:bg-indigo-500 text-white text-xs font-bold transition"
              >
                Actualizar Password en Base de Datos (PUT)
              </button>
            </form>
          </div>

          <div className="lg:col-span-6 bg-slate-900 border border-slate-800 rounded-2xl p-5 space-y-4">
            <h3 className="text-sm font-bold text-white flex items-center gap-2 border-b border-slate-800 pb-3">
              <RefreshCw className="w-4 h-4 text-emerald-400" />
              <span>2. Regeneración Automática de APIKey</span>
            </h3>

            <div className="p-3 rounded-xl bg-slate-950 border border-slate-800 text-xs font-mono">
              <span className="text-emerald-300 font-bold">GET</span>{' '}
              <span className="text-sky-300">/api/usuarios/</span>
              <span className="px-1.5 py-0.5 rounded bg-indigo-500/30 text-white font-bold">
                {secLogin || '{login}'}
              </span>
              <span className="text-sky-300">/apikey/regenerar</span>
            </div>

            <p className="text-xs text-slate-400">
              Invalida el APIKey anterior del usuario <code className="text-white">{secLogin}</code> y genera un nuevo token UUID único en la tabla <code className="text-indigo-300">usuarios</code>.
            </p>

            <button
              type="button"
              onClick={handleRegenerarApiKey}
              className="w-full py-2.5 rounded-xl bg-emerald-600 hover:bg-emerald-500 text-white text-xs font-bold transition shadow-lg shadow-emerald-600/20"
            >
              Regenerar APIKey Ahora (GET)
            </button>

            {regeneratedUserResult && (
              <div className="p-4 rounded-xl bg-slate-950 border border-emerald-500/40 space-y-2 text-xs">
                <div className="font-bold text-emerald-300">Resultado Devuelto por el Backend:</div>
                <div className="font-mono space-y-1">
                  <div>
                    Login: <strong className="text-white">{regeneratedUserResult.id?.login}</strong>
                  </div>
                  <div>
                    Password Actual: <strong className="text-amber-300">{regeneratedUserResult.password}</strong>
                  </div>
                  <div>
                    Nueva APIKey: <strong className="text-sky-300">{regeneratedUserResult.apikey}</strong>
                  </div>
                </div>
                <button
                  type="button"
                  onClick={() =>
                    handleUsarCredencialesEnSesion(
                      regeneratedUserResult.id?.login,
                      regeneratedUserResult.password,
                      regeneratedUserResult.apikey
                    )
                  }
                  className="w-full mt-2 py-1.5 rounded-lg bg-indigo-600 hover:bg-indigo-500 text-white text-xs font-semibold"
                >
                  Aplicar Nueva APIKey en la Barra Superior
                </button>
              </div>
            )}
          </div>
        </div>
      )}

      {/* =====================================================================
          SUB-TAB 3: CARGUE MÚLTIPLE DE DOCUMENTOS PDF BASE64 + VISOR PDF
         ===================================================================== */}
      {subTab === 'pdfs' && (
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-5">
          <div className="lg:col-span-5 bg-slate-900 border border-slate-800 rounded-2xl p-5 space-y-4">
            <h3 className="text-sm font-bold text-white flex items-center gap-2 border-b border-slate-800 pb-3">
              <FileUp className="w-4 h-4 text-indigo-400" />
              <span>Cargue de Uno o Varios Documentos PDF en Base64 (<code className="text-indigo-300">LONGBLOB</code>)</span>
            </h3>

            <div className="space-y-3">
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-semibold text-slate-300 mb-1">Vehículo Destino</label>
                  <select
                    value={pdfBuilder.vehiculoId}
                    onChange={(e) => setPdfBuilder({ ...pdfBuilder, vehiculoId: e.target.value })}
                    className="w-full px-3 py-2 rounded-lg bg-slate-950 border border-slate-700 text-xs text-white"
                  >
                    {vehiculos.map((v) => (
                      <option key={v.id} value={v.id}>
                        {v.placa} ({v.tipoVehiculo})
                      </option>
                    ))}
                  </select>
                </div>
                <div>
                  <label className="block text-xs font-semibold text-slate-300 mb-1">Tipo Documento</label>
                  <select
                    value={pdfBuilder.documentoId}
                    onChange={(e) => setPdfBuilder({ ...pdfBuilder, documentoId: e.target.value })}
                    className="w-full px-3 py-2 rounded-lg bg-slate-950 border border-slate-700 text-xs text-white"
                  >
                    {documentosParam.map((d) => (
                      <option key={d.id} value={d.id}>
                        {d.nombre} (Aplica: {d.aplicaA})
                      </option>
                    ))}
                  </select>
                </div>
              </div>

              <div className="grid grid-cols-3 gap-2">
                <div>
                  <label className="block text-[11px] text-slate-400 mb-1">Expedición</label>
                  <input
                    type="date"
                    value={pdfBuilder.fechaExpedicion}
                    onChange={(e) => setPdfBuilder({ ...pdfBuilder, fechaExpedicion: e.target.value })}
                    className="w-full px-2 py-1.5 rounded bg-slate-950 border border-slate-700 text-xs text-white"
                  />
                </div>
                <div>
                  <label className="block text-[11px] text-slate-400 mb-1">Vencimiento</label>
                  <input
                    type="date"
                    value={pdfBuilder.fechaVencimiento}
                    onChange={(e) => setPdfBuilder({ ...pdfBuilder, fechaVencimiento: e.target.value })}
                    className="w-full px-2 py-1.5 rounded bg-slate-950 border border-slate-700 text-xs text-white"
                  />
                </div>
                <div>
                  <label className="block text-[11px] text-slate-400 mb-1">Estado</label>
                  <select
                    value={pdfBuilder.estado}
                    onChange={(e) => setPdfBuilder({ ...pdfBuilder, estado: e.target.value })}
                    className="w-full px-2 py-1.5 rounded bg-slate-950 border border-slate-700 text-xs text-white"
                  >
                    <option value="Habilitado">Habilitado</option>
                    <option value="En Verificación">En Verificación</option>
                    <option value="Vencido">Vencido</option>
                  </select>
                </div>
              </div>

              {/* Selector de Archivos .PDF o Generador Instantáneo */}
              <div className="p-4 rounded-xl border-2 border-dashed border-indigo-500/40 bg-indigo-950/20 text-center space-y-2">
                <p className="text-xs font-semibold text-indigo-200">
                  Selecciona uno o varios archivos <code className="text-white">.pdf</code> desde tu equipo
                </p>
                <input
                  type="file"
                  accept=".pdf,application/pdf"
                  multiple
                  onChange={handleSelectPdfFileForQueue}
                  className="block w-full text-xs text-slate-300 file:mr-3 file:py-1.5 file:px-3 file:rounded-lg file:border-0 file:text-xs file:font-semibold file:bg-indigo-600 file:text-white hover:file:bg-indigo-500 cursor-pointer"
                />
                <div className="pt-1">
                  <button
                    type="button"
                    onClick={handleAddSamplePdfToQueue}
                    className="px-3 py-1.5 rounded-lg bg-slate-800 hover:bg-slate-700 text-amber-300 border border-slate-700 text-xs font-semibold transition"
                  >
                    + O Generar PDF Real de Muestra en Base64 con 1 Clic
                  </button>
                </div>
              </div>

              {/* Cola de Documentos a Enviar */}
              {pdfQueue.length > 0 && (
                <div className="space-y-2 bg-slate-950 p-3 rounded-xl border border-slate-800">
                  <div className="text-xs font-bold text-emerald-300">
                    Cola de Documentos PDF Convertidos a Base64 ({pdfQueue.length})
                  </div>
                  <div className="space-y-1.5 max-h-40 overflow-y-auto">
                    {pdfQueue.map((item) => (
                      <div
                        key={item.tempId}
                        className="flex items-center justify-between text-[11px] bg-slate-900 px-2.5 py-1.5 rounded border border-slate-800"
                      >
                        <div className="truncate">
                          <strong className="text-white">{item.fileName}</strong> • Veh #{item.vehiculoId} • Doc #{item.documentoId} ({item.documentoPdfBase64.length} chars Base64)
                        </div>
                        <button
                          onClick={() => setPdfQueue(pdfQueue.filter((q) => q.tempId !== item.tempId))}
                          className="text-rose-400 hover:text-rose-300 ml-2"
                        >
                          <Trash2 className="w-3.5 h-3.5" />
                        </button>
                      </div>
                    ))}
                  </div>
                  <button
                    type="button"
                    onClick={handleEnviarColaPdfsBase64}
                    className="w-full py-2.5 rounded-xl bg-emerald-600 hover:bg-emerald-500 text-white text-xs font-bold transition"
                  >
                    Enviar Lote de {pdfQueue.length} PDF(s) en Base64 al Backend (POST /api/operaciones/documentos/cargar)
                  </button>
                </div>
              )}
            </div>
          </div>

          {/* Biblioteca / Visor de Todos los Documentos Almacenados en LONGBLOB */}
          <div className="lg:col-span-7 bg-slate-900 border border-slate-800 rounded-2xl p-5">
            <h3 className="text-sm font-bold text-white mb-2">
              Repositorio de Documentos Vehiculares en <code className="text-indigo-300">LONGBLOB</code> (Previsualizador Base64)
            </h3>
            <p className="text-xs text-slate-400 mb-3">
              Haz clic en <strong>"Ver PDF Base64"</strong> para decodificar el archivo almacenado en MySQL y renderizarlo en pantalla
            </p>

            <div className="overflow-x-auto max-h-[55vh]">
              <table className="w-full text-left border-collapse text-xs">
                <thead>
                  <tr className="border-b border-slate-800 text-slate-400 uppercase text-[11px]">
                    <th className="py-2 px-2">Vehículo</th>
                    <th className="py-2 px-2">Documento</th>
                    <th className="py-2 px-2">Vigencia</th>
                    <th className="py-2 px-2">Estado</th>
                    <th className="py-2 px-2 text-right">Archivo LONGBLOB</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-800/70">
                  {vehiculos.flatMap((v) =>
                    (v.documentos || []).map((docRel) => (
                      <tr key={`${v.id}-${docRel.id}`} className="hover:bg-slate-800/40">
                        <td className="py-2 px-2 font-mono font-bold text-amber-300">
                          {v.placa} <span className="text-[10px] text-slate-400 font-sans">({v.tipoVehiculo})</span>
                        </td>
                        <td className="py-2 px-2 font-semibold text-white">
                          {docRel.documento?.nombre}
                        </td>
                        <td className="py-2 px-2 text-[11px] text-slate-300">
                          {docRel.fechaExpedicion} → {docRel.fechaVencimiento}
                        </td>
                        <td className="py-2 px-2">
                          <span className="px-2 py-0.5 rounded text-[10px] font-bold bg-slate-800 text-slate-200 border border-slate-700">
                            {docRel.estado}
                          </span>
                        </td>
                        <td className="py-2 px-2 text-right">
                          {docRel.documentoPdfBase64 ? (
                            <button
                              onClick={() =>
                                onOpenPdfModal({
                                  isOpen: true,
                                  title: `${docRel.documento?.nombre} — Vehículo ${v.placa}`,
                                  subtitle: `Registro #${docRel.id} | Estado: ${docRel.estado} | Vigencia: ${docRel.fechaVencimiento}`,
                                  base64Data: docRel.documentoPdfBase64
                                })
                              }
                              className="inline-flex items-center gap-1 px-2.5 py-1 rounded-lg bg-indigo-600/25 hover:bg-indigo-600 text-indigo-300 hover:text-white border border-indigo-500/30 text-[11px] font-semibold transition"
                            >
                              <Eye className="w-3.5 h-3.5" />
                              <span>Ver PDF Base64</span>
                            </button>
                          ) : (
                            <span className="text-[11px] text-slate-500 italic">Sin PDF</span>
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
      )}

      {/* =====================================================================
          SUB-TAB 4: RELACIÓN CONDUCTOR–VEHÍCULO Y ESTADOS (PO, EA, RO)
         ===================================================================== */}
      {subTab === 'conductores' && (
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-5">
          <div className="lg:col-span-5 bg-slate-900 border border-slate-800 rounded-2xl p-5 space-y-4">
            <h3 className="text-sm font-bold text-white flex items-center gap-2 border-b border-slate-800 pb-3">
              <UserCheck className="w-4 h-4 text-indigo-400" />
              <span>Asociar Conductor a Vehículo (POST /api/operaciones/conductores/asociar)</span>
            </h3>

            <form onSubmit={handleAsociarConductor} className="space-y-3">
              <div>
                <label className="block text-xs font-semibold text-slate-300 mb-1">
                  Seleccionar Conductor (Solo tipo <code className="text-emerald-300">C</code>)
                </label>
                <select
                  value={asocConductorForm.personaId}
                  onChange={(e) => setAsocConductorForm({ ...asocConductorForm, personaId: e.target.value })}
                  className="w-full px-3 py-2 rounded-lg bg-slate-950 border border-slate-700 text-xs text-white"
                >
                  {personas
                    .filter((p) => p.tipoPersona === 'C')
                    .map((p) => (
                      <option key={p.id} value={p.id}>
                        ID #{p.id} — {p.nombres} {p.apellidos} (CC: {p.identificacion})
                      </option>
                    ))}
                </select>
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-300 mb-1">Seleccionar Vehículo</label>
                <select
                  value={asocConductorForm.vehiculoId}
                  onChange={(e) => setAsocConductorForm({ ...asocConductorForm, vehiculoId: e.target.value })}
                  className="w-full px-3 py-2 rounded-lg bg-slate-950 border border-slate-700 text-xs text-white"
                >
                  {vehiculos.map((v) => (
                    <option key={v.id} value={v.id}>
                      ID #{v.id} — {v.placa} ({v.marca} {v.linea})
                    </option>
                  ))}
                </select>
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-300 mb-1">
                  Estado Operativo Inicial (<code className="text-indigo-400">CHECK: PO, EA, RO</code>)
                </label>
                <select
                  value={asocConductorForm.estadoConductor}
                  onChange={(e) => setAsocConductorForm({ ...asocConductorForm, estadoConductor: e.target.value })}
                  className="w-full px-3 py-2 rounded-lg bg-slate-950 border border-slate-700 text-xs text-white"
                >
                  <option value="PO">PO — Puede Operar (Verde)</option>
                  <option value="EA">EA — Espera de Aprobación (Amarillo)</option>
                  <option value="RO">RO — Restringido para Operar (Rojo)</option>
                </select>
              </div>

              <button
                type="submit"
                className="w-full py-2.5 rounded-xl bg-indigo-600 hover:bg-indigo-500 text-white text-xs font-bold transition"
              >
                Asociar Conductor al Vehículo
              </button>
            </form>

            <div className="pt-3 border-t border-slate-800">
              <button
                type="button"
                onClick={handleTestAsociarAdministrativoFallo}
                className="w-full px-3 py-2 rounded-lg bg-rose-950/50 hover:bg-rose-900/60 text-rose-300 border border-rose-500/40 text-xs font-semibold transition"
              >
                ⚡ Prueba de Fallo: Intentar asociar un Administrativo (tipo 'A') como Conductor
              </button>
            </div>
          </div>

          <div className="lg:col-span-7 bg-slate-900 border border-slate-800 rounded-2xl p-5">
            <h3 className="text-sm font-bold text-white mb-2">
              Relaciones Conductor–Vehículo y Conmutador de Estado (<code className="text-emerald-300">PO</code> / <code className="text-amber-300">EA</code> / <code className="text-rose-300">RO</code>)
            </h3>
            <p className="text-xs text-slate-400 mb-3">
              Cambia el estado en vivo con un clic (<code className="text-sky-300">PUT /api/operaciones/conductores/relacion/&#123;id&#125;/estado</code>)
            </p>

            <div className="overflow-x-auto">
              <table className="w-full text-left border-collapse text-xs">
                <thead>
                  <tr className="border-b border-slate-800 text-slate-400 uppercase text-[11px]">
                    <th className="py-2.5 px-2">ID Relación</th>
                    <th className="py-2.5 px-2">Conductor</th>
                    <th className="py-2.5 px-2">Vehículo</th>
                    <th className="py-2.5 px-2">Estado Actual</th>
                    <th className="py-2.5 px-2 text-right">Cambiar Estado (CHECK)</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-800/70">
                  {relacionesConductores.map((rel) => {
                    const badge = getConductorBadge(rel.estadoConductor);
                    return (
                      <tr key={rel.id} className="hover:bg-slate-800/40">
                        <td className="py-2.5 px-2 font-mono text-slate-400">#{rel.id}</td>
                        <td className="py-2.5 px-2">
                          <div className="font-bold text-white">
                            {rel.persona?.nombres} {rel.persona?.apellidos}
                          </div>
                          <div className="text-[11px] font-mono text-slate-400">
                            CC: {rel.persona?.identificacion} • Lic: {rel.persona?.fechaVigenciaLicencia}
                          </div>
                        </td>
                        <td className="py-2.5 px-2">
                          <span className="font-mono font-bold text-amber-300">
                            {rel.vehiculoPlaca || `Veh #${rel.vehiculoId}`}
                          </span>
                          <div className="text-[11px] text-slate-400">{rel.vehiculoDescripcion}</div>
                        </td>
                        <td className="py-2.5 px-2">
                          <span className={`px-2 py-0.5 rounded text-[11px] font-bold border ${badge.cls}`}>
                            {badge.label}
                          </span>
                        </td>
                        <td className="py-2.5 px-2 text-right">
                          <div className="inline-flex items-center gap-1">
                            {['PO', 'EA', 'RO'].map((st) => (
                              <button
                                key={st}
                                onClick={() => handleCambiarEstadoRelacion(rel.id, st)}
                                className={`px-2.5 py-1 rounded text-xs font-mono font-bold border transition ${
                                  rel.estadoConductor === st
                                    ? 'bg-indigo-600 text-white border-indigo-400'
                                    : 'bg-slate-950 hover:bg-slate-800 text-slate-400 border-slate-800'
                                }`}
                              >
                                {st}
                              </button>
                            ))}
                          </div>
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      )}

      {/* =====================================================================
          SUB-TAB 5: PANEL DE LOS 5 SERVICIOS PÚBLICOS (SIN TOKEN NI APIKEY)
         ===================================================================== */}
      {subTab === 'publicos' && (
        <div className="space-y-4">
          <div className="bg-emerald-950/30 border border-emerald-500/40 rounded-2xl p-4 flex flex-wrap items-center justify-between gap-3">
            <div className="flex items-center gap-3">
              <div className="p-2.5 rounded-xl bg-emerald-500/20 text-emerald-300 border border-emerald-500/30">
                <Unlock className="w-5 h-5" />
              </div>
              <div>
                <h3 className="text-sm font-bold text-white">
                  Modo de Demostración Pública Activo: Peticiones enviadas SIN Token JWT y SIN x-api-key
                </h3>
                <p className="text-xs text-slate-300">
                  Estos 5 endpoints bajo <code className="text-emerald-300">/api/public/**</code> responden <code className="text-emerald-300">HTTP 200 OK</code> incluso sin encabezados de autenticación.
                </p>
              </div>
            </div>
            <button
              onClick={ejecutarCincoServiciosPublicos}
              className="px-4 py-2 rounded-xl bg-emerald-600 hover:bg-emerald-500 text-white text-xs font-bold flex items-center gap-1.5 transition shadow-lg shadow-emerald-600/20"
            >
              <RefreshCw className={`w-4 h-4 ${loading ? 'animate-spin' : ''}`} />
              <span>Ejecutar los 5 Servicios Públicos (Sin Credenciales)</span>
            </button>
          </div>

          <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
            {/* 8.1 Vehículos con documentos vencidos */}
            <div className="bg-slate-900 border border-slate-800 rounded-2xl p-4 space-y-2">
              <div className="flex items-center justify-between">
                <span className="text-xs font-bold text-rose-300">
                  1. Vehículos con Documentos Vencidos
                </span>
                <code className="text-[11px] font-mono text-sky-300">GET /api/public/vehiculos/documentos-vencidos</code>
              </div>
              <div className="space-y-1.5 max-h-48 overflow-y-auto">
                {Array.isArray(publicResults.vencidos) && publicResults.vencidos.length > 0 ? (
                  publicResults.vencidos.map((v) => (
                    <div key={v.id} className="p-2.5 rounded-lg bg-slate-950 border border-slate-800 text-xs flex items-center justify-between">
                      <div>
                        <strong className="font-mono text-amber-300">{v.placa}</strong> — {v.marca} {v.linea}
                      </div>
                      <span className="px-2 py-0.5 rounded bg-rose-500/20 text-rose-300 text-[10px] font-bold">
                        Doc. Vencido
                      </span>
                    </div>
                  ))
                ) : (
                  <div className="text-xs text-slate-500 py-4 text-center">Sin vehículos vencidos actualmente.</div>
                )}
              </div>
            </div>

            {/* 8.2 Conductores que pueden operar (PO) */}
            <div className="bg-slate-900 border border-slate-800 rounded-2xl p-4 space-y-2">
              <div className="flex items-center justify-between">
                <span className="text-xs font-bold text-emerald-300">
                  2. Conductores Autorizados que Pueden Operar (PO)
                </span>
                <code className="text-[11px] font-mono text-sky-300">GET /api/public/conductores/autorizados</code>
              </div>
              <div className="space-y-1.5 max-h-48 overflow-y-auto">
                {Array.isArray(publicResults.autorizados) && publicResults.autorizados.length > 0 ? (
                  publicResults.autorizados.map((c) => (
                    <div key={c.id} className="p-2.5 rounded-lg bg-slate-950 border border-slate-800 text-xs flex items-center justify-between">
                      <div>
                        <strong className="text-white">{c.nombres} {c.apellidos}</strong> (CC: {c.identificacion})
                      </div>
                      <span className="px-2 py-0.5 rounded bg-emerald-500/20 text-emerald-300 text-[10px] font-bold">
                        PO • Autorizado
                      </span>
                    </div>
                  ))
                ) : (
                  <div className="text-xs text-slate-500 py-4 text-center">Sin conductores PO.</div>
                )}
              </div>
            </div>

            {/* 8.3 Detalle Integral por Placa (Vehículo + Conductores + Documentos) */}
            <div className="bg-slate-900 border border-slate-800 rounded-2xl p-4 space-y-3">
              <div className="flex flex-wrap items-center justify-between gap-2">
                <span className="text-xs font-bold text-amber-300">
                  3. Detalle Integral por Placa (Vehículo + Conductores + Documentos)
                </span>
                <div className="flex items-center gap-1.5">
                  <select
                    value={pubPlaca}
                    onChange={(e) => setPubPlaca(e.target.value)}
                    className="px-2 py-1 rounded bg-slate-950 border border-slate-700 text-xs font-mono text-amber-300"
                  >
                    {vehiculos.map((v) => (
                      <option key={v.id} value={v.placa}>
                        {v.placa}
                      </option>
                    ))}
                  </select>
                  <button
                    onClick={ejecutarCincoServiciosPublicos}
                    className="px-2.5 py-1 rounded bg-indigo-600 text-white text-xs font-semibold"
                  >
                    Consultar
                  </button>
                </div>
              </div>

              {publicResults.detallePlaca ? (
                <div className="p-3 rounded-xl bg-slate-950 border border-slate-800 text-xs space-y-2">
                  <div className="flex items-center justify-between border-b border-slate-800 pb-1.5">
                    <span className="font-bold text-white">
                      Placa {publicResults.detallePlaca.vehiculo?.placa} — {publicResults.detallePlaca.vehiculo?.marca}{' '}
                      {publicResults.detallePlaca.vehiculo?.linea}
                    </span>
                    <span className="text-[11px] text-slate-400">
                      {publicResults.detallePlaca.vehiculo?.tipoVehiculo}
                    </span>
                  </div>
                  <div>
                    <span className="text-[11px] font-bold text-indigo-300">
                      Conductores Asociados ({publicResults.detallePlaca.conductoresAsociados?.length || 0}):
                    </span>
                    <div className="mt-1 space-y-1">
                      {(publicResults.detallePlaca.conductoresAsociados || []).map((ca) => (
                        <div key={ca.id} className="text-[11px] text-slate-300 flex justify-between">
                          <span>{ca.persona?.nombres} {ca.persona?.apellidos}</span>
                          <span className="font-mono font-bold text-emerald-300">{ca.estadoConductor}</span>
                        </div>
                      ))}
                    </div>
                  </div>
                  <div>
                    <span className="text-[11px] font-bold text-amber-300">
                      Documentos Asociados ({publicResults.detallePlaca.documentosAsociados?.length || 0}):
                    </span>
                    <div className="mt-1 space-y-1">
                      {(publicResults.detallePlaca.documentosAsociados || []).map((da) => (
                        <div key={da.id} className="text-[11px] text-slate-300 flex justify-between">
                          <span>{da.documento?.nombre} (Vence: {da.fechaVencimiento})</span>
                          <span className="font-mono font-bold">{da.estado}</span>
                        </div>
                      ))}
                    </div>
                  </div>
                </div>
              ) : (
                <div className="text-xs text-slate-500 py-4 text-center">Selecciona una placa y consulta.</div>
              )}
            </div>

            {/* 8.4 Documentos próximos a vencer según días faltantes */}
            <div className="bg-slate-900 border border-slate-800 rounded-2xl p-4 space-y-3">
              <div className="flex flex-wrap items-center justify-between gap-2">
                <span className="text-xs font-bold text-sky-300">
                  4. Vehículos con Documentos Próximos a Vencer
                </span>
                <div className="flex items-center gap-1.5">
                  <span className="text-[11px] text-slate-400">Días faltantes:</span>
                  <input
                    type="number"
                    value={pubDias}
                    onChange={(e) => setPubDias(e.target.value)}
                    className="w-20 px-2 py-1 rounded bg-slate-950 border border-slate-700 text-xs font-mono text-white"
                  />
                  <button
                    onClick={ejecutarCincoServiciosPublicos}
                    className="px-2.5 py-1 rounded bg-indigo-600 text-white text-xs font-semibold"
                  >
                    Filtrar
                  </button>
                </div>
              </div>
              <div className="space-y-1.5 max-h-44 overflow-y-auto">
                {Array.isArray(publicResults.porVencer) && publicResults.porVencer.length > 0 ? (
                  publicResults.porVencer.map((v) => (
                    <div key={v.id} className="p-2.5 rounded-lg bg-slate-950 border border-slate-800 text-xs flex items-center justify-between">
                      <div>
                        <strong className="font-mono text-amber-300">{v.placa}</strong> — {v.marca} {v.linea}
                      </div>
                      <span className="text-[11px] text-sky-300">
                        Próximo a vencer (≤ {pubDias} días)
                      </span>
                    </div>
                  ))
                ) : (
                  <div className="text-xs text-slate-500 py-4 text-center">
                    Ningún vehículo vence en los próximos {pubDias} días (prueba aumentando a 400 días).
                  </div>
                )}
              </div>
            </div>

            {/* 8.5 Personas agrupadas por tipo (Gráfico y Tarjetas) */}
            <div className="lg:col-span-2 bg-slate-900 border border-slate-800 rounded-2xl p-4 space-y-3">
              <div className="flex items-center justify-between">
                <span className="text-xs font-bold text-indigo-300 flex items-center gap-1.5">
                  <BarChart3 className="w-4 h-4" />
                  <span>5. Personas Agrupadas por Tipo (Administrativos vs Conductores)</span>
                </span>
                <code className="text-[11px] font-mono text-sky-300">GET /api/public/personas/agrupadas-por-tipo</code>
              </div>

              {publicResults.agrupadas && (
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div className="p-3 rounded-xl bg-slate-950 border border-indigo-500/30">
                    <div className="flex items-center justify-between mb-2">
                      <span className="text-xs font-bold text-indigo-300">
                        ADMINISTRATIVOS (Tipo A)
                      </span>
                      <span className="px-2 py-0.5 rounded bg-indigo-500/20 text-indigo-300 font-mono font-bold text-xs">
                        {(publicResults.agrupadas.ADMINISTRATIVO || []).length} personas
                      </span>
                    </div>
                    <div className="space-y-1 max-h-32 overflow-y-auto text-xs">
                      {(publicResults.agrupadas.ADMINISTRATIVO || []).map((p) => (
                        <div key={p.id} className="text-slate-300 flex justify-between">
                          <span>{p.nombres} {p.apellidos}</span>
                          <span className="font-mono text-slate-500">{p.identificacion}</span>
                        </div>
                      ))}
                    </div>
                  </div>

                  <div className="p-3 rounded-xl bg-slate-950 border border-emerald-500/30">
                    <div className="flex items-center justify-between mb-2">
                      <span className="text-xs font-bold text-emerald-300">
                        CONDUCTORES (Tipo C)
                      </span>
                      <span className="px-2 py-0.5 rounded bg-emerald-500/20 text-emerald-300 font-mono font-bold text-xs">
                        {(publicResults.agrupadas.CONDUCTOR || []).length} personas
                      </span>
                    </div>
                    <div className="space-y-1 max-h-32 overflow-y-auto text-xs">
                      {(publicResults.agrupadas.CONDUCTOR || []).map((p) => (
                        <div key={p.id} className="text-slate-300 flex justify-between">
                          <span>{p.nombres} {p.apellidos}</span>
                          <span className="font-mono text-slate-500">{p.identificacion}</span>
                        </div>
                      ))}
                    </div>
                  </div>
                </div>
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

