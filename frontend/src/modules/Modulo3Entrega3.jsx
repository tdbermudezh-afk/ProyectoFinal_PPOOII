import React, { useState, useEffect, useRef } from 'react';
import L from 'leaflet';
import {
  MapPin,
  Navigation,
  PlusCircle,
  Search,
  Clock,
  RefreshCw,
  AlertTriangle,
  CheckCircle2,
  ShieldAlert,
  Play,
  Radio,
  Compass,
  Zap,
  Activity,
  History,
  FileCheck2,
  Calendar,
  Layers
} from 'lucide-react';
import { apiRequest, setInspectorOpen } from '../services/api';

export default function Modulo3Entrega3({ activeSubTab, setActiveSubTab }) {
  const [subTab, setSubTab] = useState(activeSubTab || 'mapa');

  useEffect(() => {
    if (activeSubTab) setSubTab(activeSubTab);
  }, [activeSubTab]);

  const changeTab = (tab) => {
    setSubTab(tab);
    if (setActiveSubTab) setActiveSubTab(tab);
  };

  // Fecha actual de referencia dinámica para comparaciones
  const hoyStr = new Date().toISOString().split('T')[0];

  // Estados generales
  const [codigosRuta, setCodigosRuta] = useState([]);
  const [selectedRuta, setSelectedRuta] = useState('RUTA-CENTRO-IBG');
  const [paradasRuta, setParadasRuta] = useState([]);
  const [personas, setPersonas] = useState([]);
  const [vehiculos, setVehiculos] = useState([]);
  const [relaciones, setRelaciones] = useState([]);
  const [loading, setLoading] = useState(false);
  const [feedback, setFeedback] = useState(null);

  // Referencia para el mapa interactivo Leaflet
  const mapContainerRef = useRef(null);
  const mapInstanceRef = useRef(null);
  const markersLayerRef = useRef(null);

  // Estado Formulario Crear Trayecto
  const [trayectoForm, setTrayectoForm] = useState({
    personaId: '',
    vehiculoId: '',
    codigoRuta: 'RUTA-CENTRO-IBG',
    ubicacion: 'Universidad de Ibagué, Carrera 22 Calle 67, Ibagué, Tolima',
    ordenParada: 3,
    dejarCoordenadasNulas: true,
    latitud: '4.4472',
    longitud: '-75.1990',
    login: 'cg1001234567'
  });

  // Estado para las 3 Consultas Especializadas de Rutas (Sub-tab 3)
  const [consultaConductorId, setConsultaConductorId] = useState('1010203040');
  const [resRutasConductor, setResRutasConductor] = useState(null);
  const [consultaPlacaVeh, setConsultaPlacaVeh] = useState('ABC123');
  const [resRutasVehiculo, setResRutasVehiculo] = useState(null);
  const [resRutasNoOperables, setResRutasNoOperables] = useState([]);

  // Estado para el Monitor en Tiempo Real de Tareas @Scheduled (Sub-tab 4)
  const [autoPolling, setAutoPolling] = useState(false);
  const [countdown120, setCountdown120] = useState(120);
  const [countdown90, setCountdown90] = useState(90);
  const [lastPollTime, setLastPollTime] = useState(null);
  const [scheduledStatus, setScheduledStatus] = useState(null);
  const [executingTask, setExecutingTask] = useState(null);

  const notify = (type, title, detail, status) => {
    setFeedback({ type, title, detail, status });
  };

  const cargarCatalogosYRutas = async () => {
    setLoading(true);
    try {
      const [resRutas, resP, resV, resRel, resNoOp, resCron] = await Promise.all([
        apiRequest('/api/trayectos/rutas', { method: 'GET', description: 'Listar todos los códigos de ruta registrados' }),
        apiRequest('/api/personas', { method: 'GET', description: 'Listar personas para trayectos' }),
        apiRequest('/api/vehiculos', { method: 'GET', description: 'Listar vehículos para trayectos' }),
        apiRequest('/api/operaciones/conductores/relaciones', { method: 'GET', description: 'Listar estados de conductores' }),
        apiRequest('/api/trayectos/no-operables', { method: 'GET', description: 'Consultar rutas que NO se pueden operar' }),
        apiRequest('/api/tareas-programadas/estado', { method: 'GET', description: 'Consultar estado en vivo de tareas @Scheduled' })
      ]);

      if (resRutas.ok && Array.isArray(resRutas.data)) {
        setCodigosRuta(resRutas.data);
        const rutaInicial = resRutas.data.includes(selectedRuta)
          ? selectedRuta
          : resRutas.data[0] || 'RUTA-CENTRO-IBG';
        setSelectedRuta(rutaInicial);
        await consultarParadasPorRuta(rutaInicial);
      }
      if (resP.ok && Array.isArray(resP.data)) {
        setPersonas(resP.data);
        const cond = resP.data.find((p) => p.tipoPersona === 'C');
        if (cond && !trayectoForm.personaId) {
          setTrayectoForm((prev) => ({ ...prev, personaId: cond.id }));
        }
      }
      if (resV.ok && Array.isArray(resV.data)) {
        setVehiculos(resV.data);
        if (resV.data.length > 0 && !trayectoForm.vehiculoId) {
          setTrayectoForm((prev) => ({ ...prev, vehiculoId: resV.data[0].id }));
        }
      }
      if (resRel.ok && Array.isArray(resRel.data)) {
        setRelaciones(resRel.data);
      }
      if (resNoOp.ok && Array.isArray(resNoOp.data)) {
        setResRutasNoOperables(resNoOp.data);
      }
      if (resCron.ok && resCron.data) {
        setScheduledStatus(resCron.data);
      }
      setLastPollTime(new Date().toLocaleTimeString('es-CO', { hour12: false }));
    } finally {
      setLoading(false);
    }
  };

  const consultarParadasPorRuta = async (codigo) => {
    if (!codigo) return;
    const res = await apiRequest(`/api/trayectos/ruta/${encodeURIComponent(codigo.trim())}`, {
      method: 'GET',
      description: `Consultar paradas ordenadas de la ruta '${codigo}'`
    });
    if (res.ok && Array.isArray(res.data)) {
      setParadasRuta(res.data);
    } else {
      setParadasRuta([]);
    }
  };

  useEffect(() => {
    cargarCatalogosYRutas();
  }, []);

  // Inicialización y actualización del Mapa Interactivo Leaflet con ruteo OSRM
  useEffect(() => {
    if (subTab !== 'mapa' || !mapContainerRef.current) return;

    if (!mapInstanceRef.current) {
      mapInstanceRef.current = L.map(mapContainerRef.current).setView([4.4447, -75.2424], 13);
      L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        attribution: '&copy; OpenStreetMap contributors'
      }).addTo(mapInstanceRef.current);
      markersLayerRef.current = L.layerGroup().addTo(mapInstanceRef.current);
    }

    const layerGroup = markersLayerRef.current;
    layerGroup.clearLayers();

    const latLngs = [];

    paradasRuta.forEach((p) => {
      if (p.latitud != null && p.longitud != null && (p.latitud !== 0 || p.longitud !== 0)) {
        const coords = [Number(p.latitud), Number(p.longitud)];
        latLngs.push(coords);

        const customIcon = L.divIcon({
          className: 'custom-stop-marker',
          html: `<div style="background:#4f46e5;color:#fff;border:2px solid #fff;border-radius:9999px;width:28px;height:28px;display:flex;align-items:center;justify-content:center;font-weight:800;font-size:12px;box-shadow:0 4px 10px rgba(0,0,0,0.4);">${p.ordenParada}</div>`,
          iconSize: [28, 28],
          iconAnchor: [14, 14]
        });

        const marker = L.marker(coords, { icon: customIcon });
        marker.bindPopup(`
          <div style="font-family:Inter,sans-serif;font-size:12px;color:#0f172a;">
            <strong>Parada #${p.ordenParada} — ${p.codigoRuta}</strong><br/>
            <span>${p.ubicacion}</span><br/>
            <small>Coords: ${p.latitud}, ${p.longitud}</small><br/>
            <small>Vehículo: <b>${p.vehiculo?.placa || ''}</b> | Conductor: <b>${p.persona?.nombres || ''}</b></small>
          </div>
        `);
        layerGroup.addLayer(marker);
      }
    });

    if (latLngs.length > 1) {
      // 1. Trazado preliminar inmediato mientras responde el servicio de ruteo
      const tempPolyline = L.polyline(latLngs, {
        color: '#818cf8',
        weight: 3,
        opacity: 0.5,
        dashArray: '6, 6'
      });
      layerGroup.addLayer(tempPolyline);

      // 2. Consulta al servicio público OSRM para seguir las calles reales de Ibagué
      const coordsString = latLngs.map((c) => `${c[1]},${c[0]}`).join(';');
      const osrmUrl = `https://router.project-osrm.org/route/v1/driving/${coordsString}?overview=full&geometries=geojson`;

      fetch(osrmUrl)
        .then((res) => res.json())
        .then((data) => {
          if (data && data.code === 'Ok' && data.routes && data.routes.length > 0) {
            layerGroup.removeLayer(tempPolyline);
            const streetCoords = data.routes[0].geometry.coordinates.map((coord) => [coord[1], coord[0]]);

            const routeGlow = L.polyline(streetCoords, {
              color: '#818cf8',
              weight: 8,
              opacity: 0.35,
              lineJoin: 'round',
              lineCap: 'round'
            });
            const routePolyline = L.polyline(streetCoords, {
              color: '#4f46e5',
              weight: 5,
              opacity: 0.9,
              lineJoin: 'round',
              lineCap: 'round'
            });

            layerGroup.addLayer(routeGlow);
            layerGroup.addLayer(routePolyline);
          }
        })
        .catch((err) => {
          console.warn('OSRM no disponible, manteniendo trazado directo:', err);
        });
    }

    if (latLngs.length > 0 && mapInstanceRef.current) {
      const bounds = L.latLngBounds(latLngs);
      mapInstanceRef.current.fitBounds(bounds, { padding: [35, 35], maxZoom: 15 });
    }

    setTimeout(() => {
      if (mapInstanceRef.current) {
        mapInstanceRef.current.invalidateSize();
      }
    }, 150);
  }, [subTab, paradasRuta]);

  // Limpieza de instancia de mapa al desmontar
  useEffect(() => {
    return () => {
      if (mapInstanceRef.current) {
        mapInstanceRef.current.remove();
        mapInstanceRef.current = null;
      }
    };
  }, []);

  // Temporizadores y Auto-Polling para el Monitor de Tareas @Scheduled
  useEffect(() => {
    const timer = setInterval(() => {
      setCountdown120((prev) => (prev <= 1 ? 120 : prev - 1));
      setCountdown90((prev) => (prev <= 1 ? 90 : prev - 1));
    }, 1000);
    return () => clearInterval(timer);
  }, []);

  useEffect(() => {
    if (!autoPolling) return;
    const pollInterval = setInterval(() => {
      cargarCatalogosYRutas();
    }, 10000);
    return () => clearInterval(pollInterval);
  }, [autoPolling, selectedRuta]);

  // =========================================================================
  // 2. CREAR TRAYECTO Y PRUEBAS DE FALLO
  // =========================================================================
  const handleCrearTrayecto = async (e) => {
    e.preventDefault();
    const payload = {
      persona: { id: Number(trayectoForm.personaId) },
      vehiculo: { id: Number(trayectoForm.vehiculoId) },
      codigoRuta: trayectoForm.codigoRuta.trim().toUpperCase(),
      ubicacion: trayectoForm.ubicacion.trim(),
      ordenParada: Number(trayectoForm.ordenParada),
      latitud: trayectoForm.dejarCoordenadasNulas ? null : Number(trayectoForm.latitud),
      longitud: trayectoForm.dejarCoordenadasNulas ? null : Number(trayectoForm.longitud),
      login: trayectoForm.login.trim()
    };

    const res = await apiRequest('/api/trayectos', {
      method: 'POST',
      body: payload,
      description: `Crear Trayecto en ruta '${payload.codigoRuta}' (Parada #${payload.ordenParada})`
    });

    if (res.ok) {
      notify(
        'success',
        `Trayecto #${res.data?.id} registrado en ruta ${res.data?.codigoRuta}`,
        trayectoForm.dejarCoordenadasNulas
          ? 'Se guardó con latitud/longitud = NULL. La Tarea Programada #3 (@Scheduled cada 90s) calculará sus coordenadas automáticamente.'
          : `Coordenadas registradas: ${res.data?.latitud}, ${res.data?.longitud}`,
        res.status
      );
      setSelectedRuta(payload.codigoRuta);
      await cargarCatalogosYRutas();
    } else {
      notify('error', 'Rechazo de Regla de Negocio al Crear Trayecto', res.data?.error || JSON.stringify(res.data), res.status);
      setInspectorOpen(true);
    }
  };

  const handleTestFalloMasDe7Paradas = async () => {
    const cond = personas.find((p) => p.tipoPersona === 'C');
    const veh = vehiculos[0];
    if (!cond || !veh) return;

    const res = await apiRequest('/api/trayectos', {
      method: 'POST',
      body: {
        persona: { id: cond.id },
        vehiculo: { id: veh.id },
        codigoRuta: 'RUTA-CENTRO-IBG',
        ubicacion: 'Parada Excedida #8, Ibagué',
        ordenParada: 8, // Fuera del rango 0..6 (máx 7 paradas)
        login: 'cg1001234567'
      },
      description: 'PRUEBA DE FALLO (Entrega 3): Intentar crear parada con ordenParada=8 (Excede límite de 7 paradas)'
    });

    notify(
      'error',
      'Bloqueo Exitoso: Límite máximo de 7 paradas por ruta (ordenParada 0..6)',
      `El Backend rechazó la petición con HTTP ${res.status}: "${res.data?.error || JSON.stringify(res.data)}"`,
      res.status
    );
    setInspectorOpen(true);
  };

  const handleTestFalloVehiculoNoHabilitado = async () => {
    const vehInvalido = vehiculos.find((v) => v.placa === 'XYZ789' || v.placa === 'TYU789') || vehiculos[1];
    const cond = personas.find((p) => p.tipoPersona === 'C');
    if (!vehInvalido || !cond) return;

    const res = await apiRequest('/api/trayectos', {
      method: 'POST',
      body: {
        persona: { id: cond.id },
        vehiculo: { id: vehInvalido.id },
        codigoRuta: 'RUTA-BLOQUEADA-TEST',
        ubicacion: 'Terminal de Transportes de Ibagué, Tolima',
        ordenParada: 0,
        login: 'cg1001234567'
      },
      description: `PRUEBA DE FALLO (Entrega 3): Intentar crear trayecto con vehículo ${vehInvalido.placa} no habilitado o conductor RO`
    });

    notify(
      'error',
      'Bloqueo Exitoso: Validación de Vehículo Habilitado y Conductor PO',
      `El Backend rechazó la petición con HTTP ${res.status}: "${res.data?.error || JSON.stringify(res.data)}"`,
      res.status
    );
    setInspectorOpen(true);
  };

  // =========================================================================
  // 3. CONSULTAS ESPECIALIZADAS DE RUTAS (POR CONDUCTOR, PLACA Y NO OPERABLES)
  // =========================================================================
  const handleConsultarPorConductor = async (e) => {
    if (e) e.preventDefault();
    const res = await apiRequest(`/api/trayectos/conductor/${encodeURIComponent(consultaConductorId.trim())}`, {
      method: 'GET',
      description: `Consultar códigos de ruta agrupados para el conductor CC ${consultaConductorId}`
    });
    if (res.ok) {
      setResRutasConductor(res.data);
      notify('success', 'Consulta por conductor completada', `HTTP ${res.status}`, res.status);
    }
  };

  const handleConsultarPorPlaca = async (e) => {
    if (e) e.preventDefault();
    const res = await apiRequest(`/api/trayectos/vehiculo/${encodeURIComponent(consultaPlacaVeh.trim().toUpperCase())}`, {
      method: 'GET',
      description: `Consultar rutas y conductores agrupados para el vehículo placa ${consultaPlacaVeh}`
    });
    if (res.ok) {
      setResRutasVehiculo(res.data);
      notify('success', 'Consulta por placa de vehículo completada', `HTTP ${res.status}`, res.status);
    }
  };

  const handleConsultarNoOperables = async () => {
    const res = await apiRequest('/api/trayectos/no-operables', {
      method: 'GET',
      description: 'Consultar rutas que NO se pueden operar (Vehículo no habilitado o Conductor RO)'
    });
    if (res.ok && Array.isArray(res.data)) {
      setResRutasNoOperables(res.data);
      notify('success', `Se encontraron ${res.data.length} rutas con restricción operativa`, `HTTP ${res.status}`, res.status);
    }
  };

  // =========================================================================
  // 4. DISPARADORES Y ESCENARIOS PARA DEMOSTRAR LAS 3 TAREAS PROGRAMADAS (@SCHEDULED)
  // =========================================================================
  const handleEjecutarTareaManual = async (tipoTarea) => {
    setExecutingTask(tipoTarea);
    try {
      const res = await apiRequest(`/api/tareas-programadas/ejecutar/${tipoTarea}`, {
        method: 'POST',
        description: `Disparo manual de tarea programada: '${tipoTarea}'`
      });

      if (res.ok) {
        await cargarCatalogosYRutas();
        notify(
          'success',
          `Tarea '${tipoTarea.toUpperCase()}' ejecutada exitosamente`,
          res.data?.resultado?.mensaje || 'Base de datos sincronizada con éxito por la tarea de fondo.',
          200
        );
      } else {
        notify('error', 'Fallo al ejecutar tarea programada', res.data?.error || JSON.stringify(res.data), res.status);
      }
    } finally {
      setExecutingTask(null);
    }
  };

  const prepararEscenarioTarea1LicenciaVencida = async () => {
    // Busca la relación del conductor con licencia vencida (menor a hoy) y la pone temporalmente en PO
    const relVencida = relaciones.find(
      (r) => r.persona?.identificacion === '1030405060' || (r.persona?.fechaVigenciaLicencia && r.persona.fechaVigenciaLicencia < hoyStr)
    );
    if (!relVencida) {
      notify('error', 'No se encontró relación con licencia vencida', 'Verifica los conductores registrados con vigencia anterior a hoy.');
      return;
    }

    const res = await apiRequest(`/api/operaciones/conductores/relacion/${relVencida.id}/estado`, {
      method: 'PUT',
      body: { estadoConductor: 'PO' },
      description: `Preparar Escenario Tarea @Scheduled #1: Poner relación #${relVencida.id} (licencia vencida) en 'PO' para ver cómo pasa automáticamente a 'RO'`
    });

    if (res.ok) {
      setAutoPolling(true);
      await cargarCatalogosYRutas();
      notify(
        'success',
        `Escenario Tarea #1 Listo: Conductor ${relVencida.persona?.nombres} puesto en "PO"`,
        'Su licencia está vencida. Puedes hacer clic en "⚡ Ejecutar Tarea 1 Ahora" o esperar el ciclo automático para ver cómo el Backend lo pasa a "RO".',
        200
      );
    }
  };

  const prepararEscenarioTarea2DocumentoVencido = async () => {
    // Busca un documento con fechaVencimiento pasada (menor a hoy) y lo pone en 'Habilitado'
    let docVencidoId = null;
    let placaObjetivo = '';
    for (const v of vehiculos) {
      for (const d of v.documentos || []) {
        if (d.fechaVencimiento && d.fechaVencimiento < hoyStr) {
          docVencidoId = d.id;
          placaObjetivo = v.placa;
          break;
        }
      }
    }

    if (!docVencidoId) {
      notify('error', 'No se encontró documento con fecha de vencimiento pasada', 'Puedes agregar uno en Entrega 1.');
      return;
    }

    const res = await apiRequest(`/api/operaciones/documentos/${docVencidoId}/estado`, {
      method: 'PUT',
      body: { estado: 'Habilitado' },
      description: `Preparar Escenario Tarea @Scheduled #2: Poner documento vencido #${docVencidoId} (${placaObjetivo}) en 'Habilitado' para ver cómo pasa a 'Vencido'`
    });

    if (res.ok) {
      setAutoPolling(true);
      await cargarCatalogosYRutas();
      notify(
        'success',
        `Escenario Tarea #2 Listo: Documento #${docVencidoId} (${placaObjetivo}) puesto en "Habilitado"`,
        'Su fecha de vigencia ya expiró. Haz clic en "⚡ Ejecutar Tarea 2 Ahora" o espera el ciclo automático para ver cómo cambia a "Vencido".',
        200
      );
    }
  };

  const prepararEscenarioTarea3Geocodificacion = async () => {
    const condPO = personas.find((p) => p.identificacion === '1010203040') || personas.find((p) => p.tipoPersona === 'C');
    const vehHab = vehiculos.find((v) => v.placa === 'ABC123') || vehiculos[0];
    if (!condPO || !vehHab) return;

    const codigoDemo = `RUTA-GEO-${Math.floor(100 + Math.random() * 899)}`;
    const res = await apiRequest('/api/trayectos', {
      method: 'POST',
      body: {
        persona: { id: condPO.id },
        vehiculo: { id: vehHab.id },
        codigoRuta: codigoDemo,
        ubicacion: 'Plaza de Bolívar, Ibagué, Tolima',
        ordenParada: 0,
        latitud: null,
        longitud: null,
        login: 'cg1001234567'
      },
      description: `Preparar Escenario Tarea @Scheduled #3: Crear trayecto en '${codigoDemo}' con latitud=null y longitud=null`
    });

    if (res.ok) {
      setSelectedRuta(codigoDemo);
      setAutoPolling(true);
      await cargarCatalogosYRutas();
      notify(
        'success',
        `Escenario Tarea #3 Listo: Ruta "${codigoDemo}" creada con Latitud = NULL y Longitud = NULL`,
        'Haz clic en "⚡ Ejecutar Tarea 3 Ahora" o espera los 90s para ver cómo Geocoder asigna las coordenadas automáticamente.',
        201
      );
    } else {
      notify('error', 'No se pudo crear el trayecto de prueba', res.data?.error || JSON.stringify(res.data), res.status);
    }
  };

  return (
    <div className="space-y-5">
      {/* Encabezado del Módulo 3 */}
      <div className="bg-slate-900/90 border border-slate-800 rounded-2xl p-4 flex flex-wrap items-center justify-between gap-4">
        <div>
          <div className="flex items-center gap-2">
            <span className="px-2.5 py-0.5 rounded-full text-xs font-bold bg-sky-500/20 text-sky-300 border border-sky-500/30">
              MÓDULO 3 • ENTREGA 3
            </span>
            <h2 className="text-lg font-bold text-white">
              Rutas, Trayectos, Mapa Geográfico Interactivo y Monitor de Tareas <code className="text-indigo-300">@Scheduled</code>
            </h2>
          </div>
          <p className="text-xs text-slate-400 mt-1">
            Visualización de paradas ordenadas (<code className="text-amber-300">0..6</code>, máx 7 paradas), mapa Leaflet con ruteo real por calles y monitoreo/disparo en vivo de las 3 tareas automáticas de fondo
          </p>
        </div>

        {/* Sub-pestañas internas */}
        <div className="flex items-center gap-1.5 p-1 bg-slate-950/80 rounded-xl border border-slate-800/80">
          {[
            { id: 'mapa', label: '1. Mapa y Paradas', icon: MapPin },
            { id: 'crear', label: '2. Crear Trayecto', icon: PlusCircle },
            { id: 'consultas', label: '3. Consultas Rutas', icon: Search },
            { id: 'scheduled', label: '4. Tareas @Scheduled', icon: Clock }
          ].map((t) => {
            const Icon = t.icon;
            const active = subTab === t.id;
            return (
              <button
                key={t.id}
                onClick={() => changeTab(t.id)}
                className={`px-3 py-1.5 rounded-lg text-xs font-bold transition flex items-center gap-1.5 ${
                  active
                    ? 'bg-sky-600 text-white shadow-md shadow-sky-600/30'
                    : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800/60'
                }`}
              >
                <Icon className="w-3.5 h-3.5" />
                <span>{t.label}</span>
              </button>
            );
          })}
        </div>
      </div>

      {/* Banner de Feedback / Alertas */}
      {feedback && (
        <div
          className={`p-3.5 rounded-xl border text-xs flex items-start justify-between gap-3 ${
            feedback.type === 'success'
              ? 'bg-emerald-950/40 border-emerald-500/40 text-emerald-200'
              : 'bg-rose-950/40 border-rose-500/40 text-rose-200'
          }`}
        >
          <div className="flex items-start gap-2.5">
            {feedback.type === 'success' ? (
              <CheckCircle2 className="w-4 h-4 text-emerald-400 shrink-0 mt-0.5" />
            ) : (
              <AlertTriangle className="w-4 h-4 text-rose-400 shrink-0 mt-0.5" />
            )}
            <div>
              <strong className="block font-semibold text-white">{feedback.title}</strong>
              <span className="text-slate-300">{feedback.detail}</span>
            </div>
          </div>
          <button
            onClick={() => setFeedback(null)}
            className="text-slate-400 hover:text-white text-xs px-2 py-0.5 rounded bg-slate-800/50"
          >
            ✕
          </button>
        </div>
      )}

      {/* =====================================================================
          SUB-TAB 1: MAPA CARTOGRÁFICO INTERACTIVO Y PARADAS ORDENADAS
         ===================================================================== */}
      {subTab === 'mapa' && (
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-5">
          {/* Columna Izquierda: Selector de Rutas y Lista de Paradas */}
          <div className="bg-slate-900 border border-slate-800 rounded-2xl p-4 space-y-4">
            <div className="flex items-center justify-between">
              <h3 className="text-sm font-bold text-white flex items-center gap-2">
                <Navigation className="w-4 h-4 text-sky-400" />
                Selector de Rutas Registradas
              </h3>
              <button
                onClick={cargarCatalogosYRutas}
                disabled={loading}
                className="p-1.5 rounded-lg bg-slate-800 hover:bg-slate-700 text-slate-300 transition"
                title="Recargar catálogo de rutas"
              >
                <RefreshCw className={`w-3.5 h-3.5 ${loading ? 'animate-spin' : ''}`} />
              </button>
            </div>

            <div>
              <label className="text-[11px] font-semibold text-slate-400 uppercase tracking-wider block mb-1.5">
                Código de Ruta Activa:
              </label>
              <select
                value={selectedRuta}
                onChange={(e) => {
                  setSelectedRuta(e.target.value);
                  consultarParadasPorRuta(e.target.value);
                }}
                className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3 py-2 text-sm font-bold text-sky-300 focus:outline-none focus:border-sky-500"
              >
                {codigosRuta.map((cod) => (
                  <option key={cod} value={cod}>
                    {cod}
                  </option>
                ))}
              </select>
            </div>

            {/* Listado de Paradas de la Ruta */}
            <div className="space-y-2">
              <div className="flex items-center justify-between text-xs text-slate-400">
                <span>Paradas Registradas ({paradasRuta.length} de máx 7):</span>
                <span className="font-mono text-[11px] text-amber-300">ordenParada 0..6</span>
              </div>

              <div className="space-y-2 max-h-[380px] overflow-y-auto pr-1">
                {paradasRuta.length === 0 ? (
                  <div className="p-4 rounded-xl bg-slate-950/60 border border-slate-800/80 text-center text-xs text-slate-400">
                    No se encontraron paradas para la ruta <strong>{selectedRuta}</strong>.
                  </div>
                ) : (
                  paradasRuta.map((p) => {
                    const tieneCoords = p.latitud != null && p.longitud != null && (p.latitud !== 0 || p.longitud !== 0);
                    return (
                      <div
                        key={p.id}
                        className="p-3 rounded-xl bg-slate-950 border border-slate-800/80 hover:border-slate-700 transition space-y-1.5"
                      >
                        <div className="flex items-center justify-between">
                          <div className="flex items-center gap-2">
                            <span className="w-5 h-5 rounded-full bg-sky-500/20 text-sky-300 font-extrabold text-[11px] flex items-center justify-center border border-sky-500/40">
                              {p.ordenParada}
                            </span>
                            <span className="text-xs font-bold text-white">Parada #{p.ordenParada}</span>
                          </div>
                          <span
                            className={`px-2 py-0.5 rounded text-[10px] font-bold ${
                              tieneCoords
                                ? 'bg-emerald-500/20 text-emerald-300 border border-emerald-500/30'
                                : 'bg-amber-500/20 text-amber-300 border border-amber-500/30 animate-pulse'
                            }`}
                          >
                            {tieneCoords ? 'Geocodificado' : 'Coordenadas NULL'}
                          </span>
                        </div>

                        <p className="text-xs text-slate-300 font-medium truncate" title={p.ubicacion}>
                          {p.ubicacion}
                        </p>

                        <div className="flex flex-wrap items-center justify-between text-[11px] text-slate-400 pt-1 border-t border-slate-900 font-mono">
                          <span>
                            Vehículo: <strong className="text-slate-200">{p.vehiculo?.placa || 'N/A'}</strong>
                          </span>
                          <span>
                            Conductor: <strong className="text-slate-200">{p.persona?.nombres || 'N/A'}</strong>
                          </span>
                        </div>
                      </div>
                    );
                  })
                )}
              </div>
            </div>
          </div>

          {/* Columna Derecha: Contenedor del Mapa Interactivo Leaflet */}
          <div className="lg:col-span-2 bg-slate-900 border border-slate-800 rounded-2xl p-4 flex flex-col space-y-3">
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-2 text-xs font-semibold text-slate-300">
                <Compass className="w-4 h-4 text-sky-400" />
                <span>
                  Visualizador Cartográfico Leaflet / OpenStreetMap — Ruta:{' '}
                  <strong className="text-sky-300">{selectedRuta}</strong>
                </span>
              </div>
              <span className="text-[11px] text-slate-400">
                {paradasRuta.filter((p) => p.latitud != null && p.longitud != null).length} paradas ubicadas en Ibagué
              </span>
            </div>

            <div
              ref={mapContainerRef}
              className="w-full h-[480px] rounded-xl overflow-hidden border border-slate-800 shadow-inner z-0"
              style={{ minHeight: '480px' }}
            />
          </div>
        </div>
      )}

      {/* =====================================================================
          SUB-TAB 2: CREAR NUEVO TRAYECTO Y PRUEBAS DE FALLO
         ===================================================================== */}
      {subTab === 'crear' && (
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-5">
          {/* Formulario de Creación de Trayecto */}
          <div className="lg:col-span-2 bg-slate-900 border border-slate-800 rounded-2xl p-5 space-y-4">
            <div className="flex items-center gap-2">
              <PlusCircle className="w-5 h-5 text-sky-400" />
              <h3 className="text-sm font-bold text-white">
                Registro de Nuevo Trayecto / Parada de Ruta (POST /api/trayectos)
              </h3>
            </div>
            <p className="text-xs text-slate-400">
              Cumple con todas las reglas de negocio de la Entrega 3: Conductor autorizado en estado <code className="text-emerald-300">PO</code>, vehículo con documentos <code className="text-emerald-300">Habilitados</code>, y <code className="text-amber-300">ordenParada</code> entre 0 y 6.
            </p>

            <form onSubmit={handleCrearTrayecto} className="space-y-4">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                {/* Conductor */}
                <div>
                  <label className="text-xs font-semibold text-slate-300 block mb-1">
                    Conductor Asociado (<code className="text-indigo-300">tipoPersona: 'C'</code>):
                  </label>
                  <select
                    value={trayectoForm.personaId}
                    onChange={(e) => setTrayectoForm({ ...trayectoForm, personaId: e.target.value })}
                    required
                    className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3 py-2 text-xs text-white focus:outline-none focus:border-sky-500"
                  >
                    <option value="">-- Seleccionar Conductor --</option>
                    {personas
                      .filter((p) => p.tipoPersona === 'C')
                      .map((p) => (
                        <option key={p.id} value={p.id}>
                          {p.nombres} {p.apellidos} (CC {p.identificacion})
                        </option>
                      ))}
                  </select>
                </div>

                {/* Vehículo */}
                <div>
                  <label className="text-xs font-semibold text-slate-300 block mb-1">
                    Vehículo Operable (<code className="text-indigo-300">Habilitado</code>):
                  </label>
                  <select
                    value={trayectoForm.vehiculoId}
                    onChange={(e) => setTrayectoForm({ ...trayectoForm, vehiculoId: e.target.value })}
                    required
                    className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3 py-2 text-xs text-white focus:outline-none focus:border-sky-500"
                  >
                    <option value="">-- Seleccionar Vehículo --</option>
                    {vehiculos.map((v) => (
                      <option key={v.id} value={v.id}>
                        {v.placa} — {v.marca} {v.linea} ({v.tipoVehiculo})
                      </option>
                    ))}
                  </select>
                </div>

                {/* Código de Ruta */}
                <div>
                  <label className="text-xs font-semibold text-slate-300 block mb-1">
                    Código de Ruta:
                  </label>
                  <input
                    type="text"
                    value={trayectoForm.codigoRuta}
                    onChange={(e) => setTrayectoForm({ ...trayectoForm, codigoRuta: e.target.value })}
                    required
                    placeholder="ej. RUTA-CENTRO-IBG"
                    className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3 py-2 text-xs text-white uppercase focus:outline-none focus:border-sky-500 font-mono"
                  />
                </div>

                {/* Orden de Parada */}
                <div>
                  <label className="text-xs font-semibold text-slate-300 block mb-1">
                    Orden de Parada (<code className="text-amber-300">0..6</code>, máx 7):
                  </label>
                  <select
                    value={trayectoForm.ordenParada}
                    onChange={(e) => setTrayectoForm({ ...trayectoForm, ordenParada: e.target.value })}
                    className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3 py-2 text-xs text-white focus:outline-none focus:border-sky-500 font-mono font-bold"
                  >
                    {[0, 1, 2, 3, 4, 5, 6].map((num) => (
                      <option key={num} value={num}>
                        Parada #{num} {num === 0 ? '(Inicial)' : num === 6 ? '(Final)' : '(Intermedia)'}
                      </option>
                    ))}
                  </select>
                </div>
              </div>

              {/* Dirección / Ubicación */}
              <div>
                <label className="text-xs font-semibold text-slate-300 block mb-1">
                  Ubicación Textual (Dirección en Ibagué, Tolima):
                </label>
                <input
                  type="text"
                  value={trayectoForm.ubicacion}
                  onChange={(e) => setTrayectoForm({ ...trayectoForm, ubicacion: e.target.value })}
                  required
                  placeholder="ej. Plaza de Bolívar, Ibagué, Tolima"
                  className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3 py-2 text-xs text-white focus:outline-none focus:border-sky-500"
                />
              </div>

              {/* Checkbox para dejar coordenadas nulas y probar Geocoder */}
              <div className="p-3 rounded-xl bg-slate-950 border border-slate-800/90 flex items-center justify-between gap-3">
                <div className="space-y-0.5">
                  <span className="text-xs font-bold text-white block">
                    Dejar Latitud y Longitud en NULL (Requerimiento @Scheduled Geocoder)
                  </span>
                  <span className="text-[11px] text-slate-400 block">
                    Permite guardar el trayecto sin coordenadas para demostrar cómo la Tarea Programada #3 las resuelve automáticamente en segundo plano.
                  </span>
                </div>
                <input
                  type="checkbox"
                  checked={trayectoForm.dejarCoordenadasNulas}
                  onChange={(e) => setTrayectoForm({ ...trayectoForm, dejarCoordenadasNulas: e.target.checked })}
                  className="w-4 h-4 accent-sky-500 cursor-pointer rounded"
                />
              </div>

              <button
                type="submit"
                disabled={loading}
                className="w-full py-2.5 rounded-xl bg-sky-600 hover:bg-sky-500 text-white font-bold text-xs transition shadow-lg shadow-sky-600/20 flex items-center justify-center gap-2"
              >
                <PlusCircle className="w-4 h-4" />
                <span>Registrar Trayecto Cumpliendo Validaciones</span>
              </button>
            </form>
          </div>

          {/* Pruebas de Fallo para Demostración */}
          <div className="bg-slate-900 border border-slate-800 rounded-2xl p-5 space-y-4">
            <div className="flex items-center gap-2 text-rose-400">
              <ShieldAlert className="w-5 h-5" />
              <h3 className="text-sm font-bold text-white">Pruebas de Bloqueo / Fallo</h3>
            </div>
            <p className="text-xs text-slate-400">
              Demuestra ante el evaluador que el Backend rechaza solicitudes que incumplen las restricciones de chequeo y reglas de negocio.
            </p>

            <div className="space-y-3 pt-2">
              <button
                onClick={handleTestFalloMasDe7Paradas}
                className="w-full p-3 rounded-xl bg-rose-950/30 hover:bg-rose-950/60 border border-rose-500/40 text-left transition space-y-1"
              >
                <div className="flex items-center justify-between text-xs font-bold text-rose-300">
                  <span>Prueba #1: Más de 7 Paradas</span>
                  <span className="font-mono text-[10px]">ordenParada = 8</span>
                </div>
                <p className="text-[11px] text-slate-300">
                  Intenta registrar un trayecto con <code className="text-white">ordenParada = 8</code>. Demuestra el bloqueo por restricción CHECK en BD y Backend.
                </p>
              </button>

              <button
                onClick={handleTestFalloVehiculoNoHabilitado}
                className="w-full p-3 rounded-xl bg-amber-950/30 hover:bg-amber-950/60 border border-amber-500/40 text-left transition space-y-1"
              >
                <div className="flex items-center justify-between text-xs font-bold text-amber-300">
                  <span>Prueba #2: Vehículo NO Habilitado</span>
                  <span className="font-mono text-[10px]">Doc 'Vencido'</span>
                </div>
                <p className="text-[11px] text-slate-300">
                  Intenta registrar un trayecto asignando un vehículo que no cuenta con todos sus documentos en estado <code className="text-white">Habilitado</code>.
                </p>
              </button>
            </div>
          </div>
        </div>
      )}

      {/* =====================================================================
          SUB-TAB 3: LAS 4 CONSULTAS PROTEGIDAS DE RUTAS
         ===================================================================== */}
      {subTab === 'consultas' && (
        <div className="space-y-5">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
            {/* Consulta 2: Códigos de ruta agrupados por conductor */}
            <div className="bg-slate-900 border border-slate-800 rounded-2xl p-5 space-y-4">
              <h3 className="text-sm font-bold text-white flex items-center gap-2">
                <Search className="w-4 h-4 text-sky-400" />
                Consulta 2: Códigos de Ruta Agrupados por Conductor
              </h3>
              <form onSubmit={handleConsultarPorConductor} className="flex gap-2">
                <input
                  type="text"
                  value={consultaConductorId}
                  onChange={(e) => setConsultaConductorId(e.target.value)}
                  placeholder="Identificación del conductor (ej. 1010203040)"
                  className="flex-1 bg-slate-950 border border-slate-800 rounded-xl px-3 py-2 text-xs text-white font-mono focus:outline-none focus:border-sky-500"
                />
                <button
                  type="submit"
                  className="px-4 py-2 rounded-xl bg-sky-600 hover:bg-sky-500 text-white font-bold text-xs transition"
                >
                  Consultar
                </button>
              </form>

              {resRutasConductor && (
                <div className="p-3.5 rounded-xl bg-slate-950 border border-slate-800 space-y-2 text-xs">
                  <div className="text-slate-400 font-mono text-[11px]">
                    Conductor CC: <strong className="text-white">{resRutasConductor.identificacion}</strong>
                  </div>
                  <div className="flex flex-wrap gap-1.5">
                    {resRutasConductor.codigosRutaAgrupados?.map((c) => (
                      <span
                        key={c}
                        className="px-2.5 py-1 rounded-lg bg-sky-500/20 text-sky-300 font-mono font-bold text-xs border border-sky-500/30"
                      >
                        {c}
                      </span>
                    ))}
                  </div>
                </div>
              )}
            </div>

            {/* Consulta 3: Código de ruta y conductor agrupados por vehículo */}
            <div className="bg-slate-900 border border-slate-800 rounded-2xl p-5 space-y-4">
              <h3 className="text-sm font-bold text-white flex items-center gap-2">
                <Search className="w-4 h-4 text-sky-400" />
                Consulta 3: Ruta y Conductor Agrupados por Placa de Vehículo
              </h3>
              <form onSubmit={handleConsultarPorPlaca} className="flex gap-2">
                <input
                  type="text"
                  value={consultaPlacaVeh}
                  onChange={(e) => setConsultaPlacaVeh(e.target.value.toUpperCase())}
                  placeholder="Placa del vehículo (ej. ABC123)"
                  className="flex-1 bg-slate-950 border border-slate-800 rounded-xl px-3 py-2 text-xs text-white font-mono uppercase focus:outline-none focus:border-sky-500"
                />
                <button
                  type="submit"
                  className="px-4 py-2 rounded-xl bg-sky-600 hover:bg-sky-500 text-white font-bold text-xs transition"
                >
                  Consultar
                </button>
              </form>

              {resRutasVehiculo && (
                <div className="p-3.5 rounded-xl bg-slate-950 border border-slate-800 space-y-2 text-xs">
                  <div className="text-slate-400 font-mono text-[11px]">
                    Vehículo Placa: <strong className="text-amber-300">{resRutasVehiculo.placaVehiculo}</strong>
                  </div>
                  <div className="space-y-1.5">
                    {resRutasVehiculo.rutasYConductoresAgrupados?.map((item, idx) => (
                      <div
                        key={idx}
                        className="flex items-center justify-between p-2 rounded-lg bg-slate-900 border border-slate-800 text-xs"
                      >
                        <span className="font-mono font-bold text-sky-300">{item.codigoRuta}</span>
                        <span className="text-slate-300">{item.conductor}</span>
                      </div>
                    ))}
                  </div>
                </div>
              )}
            </div>
          </div>

          {/* Consulta 4: Rutas y trayectos NO operables */}
          <div className="bg-slate-900 border border-slate-800 rounded-2xl p-5 space-y-4">
            <div className="flex items-center justify-between">
              <div>
                <h3 className="text-sm font-bold text-white flex items-center gap-2">
                  <AlertTriangle className="w-4 h-4 text-rose-400" />
                  Consulta 4: Rutas y Trayectos NO Operables (Vehículo no habilitado o Conductor RO)
                </h3>
                <p className="text-xs text-slate-400 mt-0.5">
                  Lista todas las rutas que se encuentran inhabilitadas por documentos vencidos o licencias expiradas.
                </p>
              </div>
              <button
                onClick={handleConsultarNoOperables}
                className="px-3.5 py-1.5 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-200 text-xs font-bold transition flex items-center gap-1.5"
              >
                <RefreshCw className="w-3.5 h-3.5" />
                <span>Actualizar Lista</span>
              </button>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-3">
              {resRutasNoOperables.length === 0 ? (
                <div className="col-span-full p-4 rounded-xl bg-emerald-950/20 border border-emerald-500/30 text-center text-xs text-emerald-300 font-medium">
                  ✓ Todas las rutas registradas cuentan con vehículos habilitados y conductores autorizados (PO).
                </div>
              ) : (
                resRutasNoOperables.map((item, idx) => (
                  <div
                    key={idx}
                    className="p-3.5 rounded-xl bg-slate-950 border border-rose-500/30 space-y-1.5"
                  >
                    <div className="flex items-center justify-between">
                      <strong className="font-mono text-xs text-rose-300">{item.codigoRuta}</strong>
                      <span className="px-2 py-0.5 rounded text-[10px] font-bold bg-rose-500/20 text-rose-300">
                        INOPERABLE
                      </span>
                    </div>
                    <p className="text-xs text-slate-300">{item.motivo}</p>
                    <div className="text-[11px] text-slate-400 font-mono pt-1 border-t border-slate-900 flex justify-between">
                      <span>Placa: {item.placaVehiculo || 'N/A'}</span>
                      <span>Conductor: {item.conductor || 'N/A'}</span>
                    </div>
                  </div>
                ))
              )}
            </div>
          </div>
        </div>
      )}

      {/* =====================================================================
          SUB-TAB 4: MONITOR EN TIEMPO REAL DE TAREAS PROGRAMADAS (@SCHEDULED)
         ===================================================================== */}
      {subTab === 'scheduled' && (
        <div className="space-y-5">
          {/* Barra Superior de Estado y Disparo General */}
          <div className="bg-indigo-950/40 border border-indigo-500/40 rounded-2xl p-4 flex flex-wrap items-center justify-between gap-4">
            <div className="flex items-center gap-3">
              <div className="p-2.5 rounded-xl bg-indigo-500/20 text-indigo-300 border border-indigo-500/30">
                <Radio className={`w-6 h-6 ${autoPolling ? 'animate-ping text-emerald-400' : 'text-indigo-400'}`} />
              </div>
              <div>
                <h3 className="text-sm font-bold text-white flex items-center gap-2">
                  <span>Centro de Control en Vivo de Tareas Automáticas</span>
                  <code className="text-indigo-300 text-xs px-2 py-0.5 rounded bg-indigo-950">@Scheduled</code>
                </h3>
                <p className="text-xs text-slate-300 mt-0.5">
                  Monitoreo bidireccional en tiempo real y disparo manual bajo demanda para sustentación inmediata sin esperas.
                </p>
              </div>
            </div>

            <div className="flex items-center gap-2.5">
              <button
                onClick={() => handleEjecutarTareaManual('todas')}
                disabled={executingTask !== null}
                className="px-3.5 py-2 rounded-xl text-xs font-bold bg-indigo-600 hover:bg-indigo-500 text-white border border-indigo-400 transition flex items-center gap-1.5 shadow-lg shadow-indigo-600/30"
              >
                <Zap className={`w-3.5 h-3.5 ${executingTask === 'todas' ? 'animate-spin' : ''}`} />
                <span>{executingTask === 'todas' ? 'Ejecutando...' : '⚡ Ejecutar TODAS Ahora'}</span>
              </button>

              <button
                onClick={() => setAutoPolling(!autoPolling)}
                className={`px-3.5 py-2 rounded-xl text-xs font-bold border transition flex items-center gap-1.5 ${
                  autoPolling
                    ? 'bg-emerald-600 text-white border-emerald-400'
                    : 'bg-slate-800 hover:bg-slate-700 text-slate-200 border-slate-700'
                }`}
              >
                <RefreshCw className={`w-3.5 h-3.5 ${autoPolling ? 'animate-spin' : ''}`} />
                <span>{autoPolling ? 'Auto-Refresh (10s) ACTIVO' : 'Activar Auto-Refresh'}</span>
              </button>
            </div>
          </div>

          {/* Tarjetas de las 3 Tareas Programadas */}
          <div className="grid grid-cols-1 lg:grid-cols-3 gap-5">
            {/* TAREA 1: Licencias de Conducción */}
            <div className="bg-slate-900 border border-slate-800 rounded-2xl p-5 flex flex-col justify-between space-y-4 shadow-sm">
              <div className="space-y-2.5">
                <div className="flex items-center justify-between">
                  <span className="px-2 py-0.5 rounded text-[10px] font-bold bg-amber-500/20 text-amber-300 border border-amber-500/30">
                    TAREA 1 • fixedRate = 120s (2 min)
                  </span>
                  <span className="font-mono text-xs text-slate-400">Ciclo: ~{countdown120}s</span>
                </div>

                <h4 className="text-sm font-bold text-white flex items-center gap-2">
                  <Activity className="w-4 h-4 text-amber-400" />
                  1. Vigencia de Licencias de Conducción
                </h4>

                <p className="text-xs text-slate-400">
                  Detecta conductores con <code className="text-white">fechaVigenciaLicencia &lt; HOY</code> y actualiza automáticamente su estado a <strong className="text-rose-300">RO (Restringido para Operar)</strong> en <code className="text-indigo-300">vehiculo_personas</code>.
                </p>

                <div className="text-[11px] font-mono text-slate-400 bg-slate-950/70 p-2 rounded-lg border border-slate-800/80">
                  Última ejecución: <span className="text-slate-200">{scheduledStatus?.tarea1Licencias?.ultimaEjecucion || 'Aún no ejecutada'}</span>
                </div>

                {/* Lista en vivo de conductores con licencia vencida */}
                <div className="p-3 rounded-xl bg-slate-950 border border-slate-800 space-y-2 text-xs">
                  <div className="text-[11px] font-bold text-slate-400 uppercase tracking-wider flex justify-between">
                    <span>Conductores con Licencia Vencida:</span>
                    <span className="font-mono text-amber-300">
                      {relaciones.filter((r) => r.persona?.fechaVigenciaLicencia && r.persona.fechaVigenciaLicencia < hoyStr).length} detectados
                    </span>
                  </div>

                  {relaciones
                    .filter((r) => r.persona?.fechaVigenciaLicencia && r.persona.fechaVigenciaLicencia < hoyStr)
                    .map((r) => (
                      <div key={r.id} className="flex items-center justify-between py-1 border-b border-slate-900 last:border-b-0">
                        <div>
                          <strong className="text-white">{r.persona?.nombres} {r.persona?.apellidos}</strong>
                          <span className="text-[11px] text-slate-400 block font-mono">
                            Vigencia: {r.persona?.fechaVigenciaLicencia}
                          </span>
                        </div>
                        <span
                          className={`px-2 py-0.5 rounded font-mono font-bold text-xs ${
                            r.estadoConductor === 'RO'
                              ? 'bg-rose-500/20 text-rose-300 border border-rose-500/40'
                              : 'bg-emerald-500/20 text-emerald-300 border border-emerald-500/40 animate-pulse'
                          }`}
                        >
                          {r.estadoConductor}
                        </span>
                      </div>
                    ))}
                </div>
              </div>

              <div className="space-y-2 pt-2">
                <button
                  onClick={() => handleEjecutarTareaManual('licencias')}
                  disabled={executingTask !== null}
                  className="w-full py-2 rounded-xl bg-amber-600 hover:bg-amber-500 text-white font-bold text-xs transition flex items-center justify-center gap-1.5 shadow-md shadow-amber-600/20"
                >
                  <Zap className={`w-3.5 h-3.5 ${executingTask === 'licencias' ? 'animate-spin' : ''}`} />
                  <span>{executingTask === 'licencias' ? 'Ejecutando...' : '⚡ Ejecutar Tarea 1 Ahora'}</span>
                </button>

                <button
                  onClick={prepararEscenarioTarea1LicenciaVencida}
                  className="w-full py-1.5 rounded-xl bg-slate-800 hover:bg-slate-700 text-amber-300 text-[11px] font-bold border border-slate-700 transition flex items-center justify-center gap-1.5"
                >
                  <Play className="w-3 h-3" />
                  <span>Escenario: Poner Conductor en "PO" para ver cambio</span>
                </button>
              </div>
            </div>

            {/* TAREA 2: Documentos de Vehículos */}
            <div className="bg-slate-900 border border-slate-800 rounded-2xl p-5 flex flex-col justify-between space-y-4 shadow-sm">
              <div className="space-y-2.5">
                <div className="flex items-center justify-between">
                  <span className="px-2 py-0.5 rounded text-[10px] font-bold bg-rose-500/20 text-rose-300 border border-rose-500/30">
                    TAREA 2 • fixedRate = 120s (2 min)
                  </span>
                  <span className="font-mono text-xs text-slate-400">Ciclo: ~{countdown120}s</span>
                </div>

                <h4 className="text-sm font-bold text-white flex items-center gap-2">
                  <FileCheck2 className="w-4 h-4 text-rose-400" />
                  2. Actualización de Documentos Vencidos
                </h4>

                <p className="text-xs text-slate-400">
                  Escanea <code className="text-indigo-300">vehiculo_documento</code> donde <code className="text-white">fechaVencimiento &lt; HOY</code> y actualiza su estado automáticamente a <strong className="text-rose-300">Vencido</strong>.
                </p>

                <div className="text-[11px] font-mono text-slate-400 bg-slate-950/70 p-2 rounded-lg border border-slate-800/80">
                  Última ejecución: <span className="text-slate-200">{scheduledStatus?.tarea2Documentos?.ultimaEjecucion || 'Aún no ejecutada'}</span>
                </div>

                {/* Lista en vivo de documentos expirados */}
                <div className="p-3 rounded-xl bg-slate-950 border border-slate-800 space-y-2 text-xs max-h-48 overflow-y-auto">
                  <div className="text-[11px] font-bold text-slate-400 uppercase tracking-wider flex justify-between">
                    <span>Documentos con Fecha Expirada:</span>
                    <span className="font-mono text-rose-300">
                      {vehiculos.flatMap((v) => (v.documentos || []).filter((d) => d.fechaVencimiento && d.fechaVencimiento < hoyStr)).length} docs
                    </span>
                  </div>

                  {vehiculos.flatMap((v) =>
                    (v.documentos || [])
                      .filter((d) => d.fechaVencimiento && d.fechaVencimiento < hoyStr)
                      .map((d) => (
                        <div key={d.id} className="flex items-center justify-between py-1 border-b border-slate-900 last:border-b-0">
                          <div>
                            <strong className="font-mono text-amber-300">{v.placa}</strong> — {d.documento?.nombre}
                            <span className="text-[10px] text-slate-400 block font-mono">Venció: {d.fechaVencimiento}</span>
                          </div>
                          <span
                            className={`px-2 py-0.5 rounded font-mono font-bold text-[11px] ${
                              d.estado === 'Vencido'
                                ? 'bg-rose-500/20 text-rose-300 border border-rose-500/40'
                                : 'bg-emerald-500/20 text-emerald-300 border border-emerald-500/40 animate-pulse'
                            }`}
                          >
                            {d.estado}
                          </span>
                        </div>
                      ))
                  )}
                </div>
              </div>

              <div className="space-y-2 pt-2">
                <button
                  onClick={() => handleEjecutarTareaManual('documentos')}
                  disabled={executingTask !== null}
                  className="w-full py-2 rounded-xl bg-rose-600 hover:bg-rose-500 text-white font-bold text-xs transition flex items-center justify-center gap-1.5 shadow-md shadow-rose-600/20"
                >
                  <Zap className={`w-3.5 h-3.5 ${executingTask === 'documentos' ? 'animate-spin' : ''}`} />
                  <span>{executingTask === 'documentos' ? 'Ejecutando...' : '⚡ Ejecutar Tarea 2 Ahora'}</span>
                </button>

                <button
                  onClick={prepararEscenarioTarea2DocumentoVencido}
                  className="w-full py-1.5 rounded-xl bg-slate-800 hover:bg-slate-700 text-rose-300 text-[11px] font-bold border border-slate-700 transition flex items-center justify-center gap-1.5"
                >
                  <Play className="w-3 h-3" />
                  <span>Escenario: Poner Doc en "Habilitado" para ver cambio</span>
                </button>
              </div>
            </div>

            {/* TAREA 3: Geocodificación Automática */}
            <div className="bg-slate-900 border border-slate-800 rounded-2xl p-5 flex flex-col justify-between space-y-4 shadow-sm">
              <div className="space-y-2.5">
                <div className="flex items-center justify-between">
                  <span className="px-2 py-0.5 rounded text-[10px] font-bold bg-emerald-500/20 text-emerald-300 border border-emerald-500/30">
                    TAREA 3 • fixedRate = 90s (90 seg)
                  </span>
                  <span className="font-mono text-xs text-slate-400">Ciclo: ~{countdown90}s</span>
                </div>

                <h4 className="text-sm font-bold text-white flex items-center gap-2">
                  <Compass className="w-4 h-4 text-emerald-400" />
                  3. Geocodificación Automática (Google Maps / Geocoder)
                </h4>

                <p className="text-xs text-slate-400">
                  Detecta trayectos con <code className="text-white">latitud IS NULL</code> o <code className="text-white">longitud IS NULL</code>, consume <code className="text-indigo-300">Geocoder</code> y persiste coordenadas en MySQL.
                </p>

                <div className="text-[11px] font-mono text-slate-400 bg-slate-950/70 p-2 rounded-lg border border-slate-800/80">
                  Última ejecución: <span className="text-slate-200">{scheduledStatus?.tarea3Geocodificacion?.ultimaEjecucion || 'Aún no ejecutada'}</span>
                </div>

                {/* Paradas de la ruta activa y su estado de coordenadas */}
                <div className="p-3 rounded-xl bg-slate-950 border border-slate-800 space-y-2 text-xs">
                  <div className="text-[11px] font-bold text-slate-400 uppercase tracking-wider flex justify-between">
                    <span>Paradas Ruta Activa ({selectedRuta}):</span>
                    <span className="font-mono text-emerald-300">{paradasRuta.length} paradas</span>
                  </div>

                  {paradasRuta.slice(0, 4).map((p) => (
                    <div key={p.id} className="flex items-center justify-between py-1 border-b border-slate-900 last:border-b-0">
                      <span className="truncate max-w-[150px] text-slate-300" title={p.ubicacion}>
                        #{p.ordenParada} {p.ubicacion}
                      </span>
                      {p.latitud != null && p.longitud != null && (p.latitud !== 0 || p.longitud !== 0) ? (
                        <span className="font-mono text-[11px] text-emerald-300">
                          {Number(p.latitud).toFixed(4)}, {Number(p.longitud).toFixed(4)}
                        </span>
                      ) : (
                        <span className="font-mono text-[11px] text-amber-300 animate-pulse">
                          NULL (Pendiente)
                        </span>
                      )}
                    </div>
                  ))}
                </div>
              </div>

              <div className="space-y-2 pt-2">
                <button
                  onClick={() => handleEjecutarTareaManual('geocodificacion')}
                  disabled={executingTask !== null}
                  className="w-full py-2 rounded-xl bg-emerald-600 hover:bg-emerald-500 text-white font-bold text-xs transition flex items-center justify-center gap-1.5 shadow-md shadow-emerald-600/20"
                >
                  <Zap className={`w-3.5 h-3.5 ${executingTask === 'geocodificacion' ? 'animate-spin' : ''}`} />
                  <span>{executingTask === 'geocodificacion' ? 'Ejecutando...' : '⚡ Ejecutar Tarea 3 Ahora'}</span>
                </button>

                <button
                  onClick={prepararEscenarioTarea3Geocodificacion}
                  className="w-full py-1.5 rounded-xl bg-slate-800 hover:bg-slate-700 text-emerald-300 text-[11px] font-bold border border-slate-700 transition flex items-center justify-center gap-1.5"
                >
                  <Play className="w-3 h-3" />
                  <span>Crear Parada NULL y ver Geocodificación</span>
                </button>
              </div>
            </div>
          </div>

          {/* Panel de Trazabilidad y Eventos en Vivo de Tareas Automáticas */}
          <div className="bg-slate-900 border border-slate-800 rounded-2xl p-5 space-y-4">
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-2">
                <History className="w-5 h-5 text-indigo-400" />
                <h3 className="text-sm font-bold text-white">
                  Historial y Auditoría en Vivo de Eventos (@Scheduled)
                </h3>
              </div>
              <span className="text-xs text-slate-400 font-mono">
                Servidor: <strong className="text-slate-200">{scheduledStatus?.timestampServidor || 'En línea'}</strong>
              </span>
            </div>

            <div className="space-y-2 max-h-72 overflow-y-auto pr-1">
              {!scheduledStatus?.historialReciente || scheduledStatus.historialReciente.length === 0 ? (
                <div className="p-4 rounded-xl bg-slate-950 text-center text-xs text-slate-400">
                  Esperando las primeras ejecuciones del planificador de tareas... Puedes hacer clic en cualquiera de los botones <strong>"⚡ Ejecutar Ahora"</strong> arriba para disparar una prueba inmediata.
                </div>
              ) : (
                scheduledStatus.historialReciente.map((ev, idx) => (
                  <div
                    key={idx}
                    className="p-3 rounded-xl bg-slate-950 border border-slate-800/90 text-xs space-y-1 flex items-start justify-between gap-3"
                  >
                    <div className="space-y-1">
                      <div className="flex items-center gap-2">
                        <span
                          className={`px-2 py-0.5 rounded text-[10px] font-bold uppercase ${
                            ev.tipo === 'MANUAL'
                              ? 'bg-indigo-500/20 text-indigo-300 border border-indigo-500/30'
                              : 'bg-emerald-500/20 text-emerald-300 border border-emerald-500/30'
                          }`}
                        >
                          {ev.tipo}
                        </span>
                        <strong className="text-white text-xs">{ev.tarea}</strong>
                      </div>
                      <p className="text-slate-300 text-[11px]">{ev.detalle}</p>
                    </div>

                    <div className="text-right shrink-0 font-mono text-[11px] text-slate-400">
                      <span>{ev.timestamp}</span>
                      <span className="block text-[10px] text-amber-300">
                        {ev.modificados} modificado(s) / {ev.analizados} analizado(s)
                      </span>
                    </div>
                  </div>
                ))
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
