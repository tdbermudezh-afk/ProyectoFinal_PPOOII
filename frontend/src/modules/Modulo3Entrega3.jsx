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
  Compass
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

  const notify = (type, title, detail, status) => {
    setFeedback({ type, title, detail, status });
  };

  const cargarCatalogosYRutas = async () => {
    setLoading(true);
    try {
      const [resRutas, resP, resV, resRel, resNoOp] = await Promise.all([
        apiRequest('/api/trayectos/rutas', { method: 'GET', description: 'Listar todos los códigos de ruta registrados' }),
        apiRequest('/api/personas', { method: 'GET', description: 'Listar personas para trayectos' }),
        apiRequest('/api/vehiculos', { method: 'GET', description: 'Listar vehículos para trayectos' }),
        apiRequest('/api/operaciones/conductores/relaciones', { method: 'GET', description: 'Listar estados de conductores' }),
        apiRequest('/api/trayectos/no-operables', { method: 'GET', description: 'Consultar rutas que NO se pueden operar' })
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

  // Inicialización y actualización del Mapa Interactivo Leaflet
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

      // 2. Consulta al servicio público OSRM (Open Source Routing Machine) para seguir las calles reales
      const coordsString = latLngs.map(c => `${c[1]},${c[0]}`).join(';');
      const osrmUrl = `https://router.project-osrm.org/route/v1/driving/${coordsString}?overview=full&geometries=geojson`;

      fetch(osrmUrl)
        .then(res => res.json())
        .then(data => {
          if (data && data.code === 'Ok' && data.routes && data.routes.length > 0) {
            // Elimina la línea recta provisional
            layerGroup.removeLayer(tempPolyline);

            // Coordenadas de la geometría de calles (OSRM devuelve [lon, lat])
            const streetCoords = data.routes[0].geometry.coordinates.map(coord => [coord[1], coord[0]]);

            // Trazado de ruta sólida profesional por las calles
            const routePolyline = L.polyline(streetCoords, {
              color: '#4f46e5',
              weight: 5,
              opacity: 0.9,
              lineJoin: 'round',
              lineCap: 'round'
            });

            // Resplandor / sombra exterior para resaltar la vía
            const routeGlow = L.polyline(streetCoords, {
              color: '#818cf8',
              weight: 8,
              opacity: 0.35,
              lineJoin: 'round',
              lineCap: 'round'
            });

            layerGroup.addLayer(routeGlow);
            layerGroup.addLayer(routePolyline);
          }
        })
        .catch(err => {
          console.warn('No fue posible obtener la ruta por calles desde OSRM, manteniendo trazado directo:', err);
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
    // Buscamos un vehículo con documentos vencidos o no habilitados (ej. XYZ789 o TYU789)
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
  // 4. ESCENARIOS EN VIVO PARA DEMOSTRAR LAS 3 TAREAS PROGRAMADAS (@SCHEDULED)
  // =========================================================================
  const prepararEscenarioTarea1LicenciaVencida = async () => {
    // Busca la relación del conductor con licencia vencida (ej. Pedro Nel Ospina 1030405060) y la pone temporalmente en PO
    const relVencida = relaciones.find(
      (r) => r.persona?.identificacion === '1030405060' || (r.persona?.fechaVigenciaLicencia && r.persona.fechaVigenciaLicencia < '2026-01-01')
    );
    if (!relVencida) {
      notify('error', 'No se encontró relación con licencia vencida', 'Verifica los conductores registrados.');
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
        'Su licencia está vencida. El auto-refresh está ACTIVO: en máximo 120 segundos verás que la Tarea Programada #1 cambia su estado automáticamente a "RO" (Restringido para Operar).',
        200
      );
    }
  };

  const prepararEscenarioTarea2DocumentoVencido = async () => {
    // Busca un documento con fechaVencimiento pasada (ej. vehículo TYU789) y lo pone en 'Habilitado'
    let docVencidoId = null;
    let placaObjetivo = '';
    for (const v of vehiculos) {
      for (const d of v.documentos || []) {
        if (d.fechaVencimiento && d.fechaVencimiento < '2026-01-01') {
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
        'Su fecha de vencimiento ya expiró. En máximo 120 segundos verás que la Tarea Programada #2 lo cambia automáticamente a "Vencido".',
        200
      );
    }
  };

  const prepararEscenarioTarea3Geocodificacion = async () => {
    // Crea o agrega una parada con coordenadas NULL en una ruta operable (vehículo ABC123, conductor 1010203040)
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
        'En máximo 90 segundos la Tarea Programada #3 invocará Geocoder y completará automáticamente la latitud y longitud reales en MySQL.',
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
            Visualización de paradas ordenadas (<code className="text-amber-300">0..6</code>, máx 7 paradas), mapa Leaflet OpenStreetMap y monitoreo en vivo de las 3 tareas automáticas de fondo
          </p>
        </div>

        <div className="flex flex-wrap items-center gap-2">
          {[
            { id: 'mapa', label: '1. Mapa Visual y Timeline de Rutas', icon: MapPin },
            { id: 'crear', label: '2. Crear Trayecto y Validaciones', icon: PlusCircle },
            { id: 'consultas', label: '3. Consultas (Conductor / Placa / No Operables)', icon: Search },
            { id: 'scheduled', label: '4. Monitor en Vivo @Scheduled (3 Tareas)', icon: Clock }
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
          SUB-TAB 1: INTERFAZ WEB VISUAL DE RUTAS Y PARADAS + MAPA LEAFLET
         ===================================================================== */}
      {subTab === 'mapa' && (
        <div className="space-y-4">
          {/* Barra Buscadora de Código de Ruta */}
          <div className="bg-slate-900 border border-slate-800 rounded-2xl p-4 flex flex-wrap items-center justify-between gap-3">
            <div className="flex flex-wrap items-center gap-3 flex-1">
              <span className="text-xs font-bold text-slate-300">
                Seleccionar o Buscar Código de Ruta (<code className="text-sky-300">GET /api/trayectos/ruta/&#123;codigoRuta&#125;</code>):
              </span>
              <div className="flex flex-wrap gap-1.5">
                {codigosRuta.map((cod) => (
                  <button
                    key={cod}
                    onClick={() => {
                      setSelectedRuta(cod);
                      consultarParadasPorRuta(cod);
                    }}
                    className={`px-3 py-1.5 rounded-lg text-xs font-mono font-bold border transition ${
                      selectedRuta === cod
                        ? 'bg-indigo-600 text-white border-indigo-400'
                        : 'bg-slate-950 hover:bg-slate-800 text-amber-300 border-slate-800'
                    }`}
                  >
                    {cod}
                  </button>
                ))}
              </div>
            </div>
            <button
              onClick={() => consultarParadasPorRuta(selectedRuta)}
              className="px-3 py-1.5 rounded-lg bg-slate-800 hover:bg-slate-700 text-xs font-semibold text-slate-200 flex items-center gap-1.5"
            >
              <RefreshCw className="w-3.5 h-3.5" /> Refrescar Paradas
            </button>
          </div>

          <div className="grid grid-cols-1 lg:grid-cols-12 gap-5">
            {/* Timeline Secuencial de Paradas Ordenadas (0 a N) */}
            <div className="lg:col-span-5 bg-slate-900 border border-slate-800 rounded-2xl p-5 space-y-3">
              <div className="flex items-center justify-between border-b border-slate-800 pb-2.5">
                <div>
                  <h3 className="text-sm font-bold text-white">
                    Línea de Tiempo de Paradas: <code className="text-amber-300">{selectedRuta}</code>
                  </h3>
                  <p className="text-[11px] text-slate-400">
                    Orden secuencial (<code className="text-indigo-300">ordenParada ASC</code>) • {paradasRuta.length} de máx. 7 paradas
                  </p>
                </div>
              </div>

              <div className="space-y-3 max-h-[52vh] overflow-y-auto pr-1">
                {paradasRuta.length === 0 ? (
                  <div className="text-xs text-slate-500 py-8 text-center">
                    No hay paradas registradas para este código de ruta.
                  </div>
                ) : (
                  paradasRuta.map((p, idx) => {
                    const tieneCoords = p.latitud != null && p.longitud != null && (p.latitud !== 0 || p.longitud !== 0);
                    return (
                      <div
                        key={p.id}
                        className="relative pl-9 pr-3 py-3 rounded-xl bg-slate-950 border border-slate-800 hover:border-indigo-500/40 transition"
                      >
                        {/* Círculo con número de ordenParada */}
                        <div className="absolute left-2.5 top-3.5 w-5 h-5 rounded-full bg-indigo-600 text-white text-[11px] font-bold flex items-center justify-center shadow">
                          {p.ordenParada}
                        </div>
                        <div className="flex items-start justify-between gap-2">
                          <div>
                            <div className="text-xs font-bold text-white">{p.ubicacion}</div>
                            <div className="text-[11px] text-slate-400 mt-0.5">
                              Vehículo: <strong className="font-mono text-amber-300">{p.vehiculo?.placa}</strong> • Conductor:{' '}
                              <strong className="text-slate-200">
                                {p.persona?.nombres} {p.persona?.apellidos}
                              </strong>
                            </div>
                          </div>
                          <span className="text-[10px] font-mono text-slate-500">ID #{p.id}</span>
                        </div>

                        <div className="mt-2 pt-2 border-t border-slate-900 flex flex-wrap items-center justify-between gap-2 text-[11px]">
                          {tieneCoords ? (
                            <span className="font-mono text-emerald-300 flex items-center gap-1">
                              <Compass className="w-3.5 h-3.5" />
                              Lat: {Number(p.latitud).toFixed(5)}, Lng: {Number(p.longitud).toFixed(5)}
                            </span>
                          ) : (
                            <span className="font-mono text-amber-300 animate-pulse">
                              ⏳ Pendiente Geocodificación (@Scheduled 90s)
                            </span>
                          )}
                          <span className="text-slate-500 font-mono">Usuario: {p.login}</span>
                        </div>
                      </div>
                    );
                  })
                )}
              </div>
            </div>

            {/* Mapa Interactivo Leaflet con Marcadores y Polilínea */}
            <div className="lg:col-span-7 bg-slate-900 border border-slate-800 rounded-2xl p-4 flex flex-col">
              <div className="flex items-center justify-between mb-2">
                <span className="text-xs font-bold text-white flex items-center gap-1.5">
                  <Navigation className="w-4 h-4 text-indigo-400" />
                  <span>Mapa Geográfico de Paradas y Trazado de Ruta ({selectedRuta})</span>
                </span>
                <span className="text-[11px] text-slate-400">
                  Coordenadas geocodificadas en Ibagué, Tolima
                </span>
              </div>
              <div
                ref={mapContainerRef}
                className="w-full h-[54vh] rounded-xl border border-slate-800 overflow-hidden"
              />
            </div>
          </div>
        </div>
      )}

      {/* =====================================================================
          SUB-TAB 2: CREACIÓN Y VALIDACIÓN DE TRAYECTOS
         ===================================================================== */}
      {subTab === 'crear' && (
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-5">
          <div className="lg:col-span-6 bg-slate-900 border border-slate-800 rounded-2xl p-5 space-y-4">
            <h3 className="text-sm font-bold text-white flex items-center gap-2 border-b border-slate-800 pb-3">
              <PlusCircle className="w-4 h-4 text-indigo-400" />
              <span>Registrar Nueva Parada / Trayecto (POST /api/trayectos)</span>
            </h3>

            <form onSubmit={handleCrearTrayecto} className="space-y-3">
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-semibold text-slate-300 mb-1">
                    Conductor (Debe estar en estado <code className="text-emerald-300">PO</code>)
                  </label>
                  <select
                    value={trayectoForm.personaId}
                    onChange={(e) => setTrayectoForm({ ...trayectoForm, personaId: e.target.value })}
                    className="w-full px-3 py-2 rounded-lg bg-slate-950 border border-slate-700 text-xs text-white"
                  >
                    {personas
                      .filter((p) => p.tipoPersona === 'C')
                      .map((p) => (
                        <option key={p.id} value={p.id}>
                          {p.nombres} {p.apellidos} (CC: {p.identificacion})
                        </option>
                      ))}
                  </select>
                </div>

                <div>
                  <label className="block text-xs font-semibold text-slate-300 mb-1">
                    Vehículo (Documentos <code className="text-emerald-300">Habilitados</code>)
                  </label>
                  <select
                    value={trayectoForm.vehiculoId}
                    onChange={(e) => setTrayectoForm({ ...trayectoForm, vehiculoId: e.target.value })}
                    className="w-full px-3 py-2 rounded-lg bg-slate-950 border border-slate-700 text-xs text-white"
                  >
                    {vehiculos.map((v) => (
                      <option key={v.id} value={v.id}>
                        {v.placa} — {v.marca} {v.linea}
                      </option>
                    ))}
                  </select>
                </div>
              </div>

              <div className="grid grid-cols-3 gap-3">
                <div className="col-span-2">
                  <label className="block text-xs font-semibold text-slate-300 mb-1">Código de Ruta</label>
                  <input
                    type="text"
                    required
                    value={trayectoForm.codigoRuta}
                    onChange={(e) => setTrayectoForm({ ...trayectoForm, codigoRuta: e.target.value.toUpperCase() })}
                    placeholder="RUTA-NORTE-01"
                    className="w-full px-3 py-2 rounded-lg bg-slate-950 border border-slate-700 text-xs font-mono text-amber-300"
                  />
                </div>
                <div>
                  <label className="block text-xs font-semibold text-slate-300 mb-1">
                    Orden Parada (<code className="text-indigo-400">0..6</code>)
                  </label>
                  <input
                    type="number"
                    min={0}
                    max={6}
                    required
                    value={trayectoForm.ordenParada}
                    onChange={(e) => setTrayectoForm({ ...trayectoForm, ordenParada: e.target.value })}
                    className="w-full px-3 py-2 rounded-lg bg-slate-950 border border-slate-700 text-xs font-mono text-white"
                  />
                </div>
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-300 mb-1">
                  Ubicación / Dirección en Ibagué (Para Geocodificación)
                </label>
                <input
                  type="text"
                  required
                  value={trayectoForm.ubicacion}
                  onChange={(e) => setTrayectoForm({ ...trayectoForm, ubicacion: e.target.value })}
                  className="w-full px-3 py-2 rounded-lg bg-slate-950 border border-slate-700 text-xs text-white"
                />
              </div>

              <div className="p-3 rounded-xl bg-indigo-950/30 border border-indigo-500/30 space-y-2">
                <label className="flex items-center gap-2 text-xs font-semibold text-indigo-200 cursor-pointer">
                  <input
                    type="checkbox"
                    checked={trayectoForm.dejarCoordenadasNulas}
                    onChange={(e) => setTrayectoForm({ ...trayectoForm, dejarCoordenadasNulas: e.target.checked })}
                    className="rounded border-slate-700"
                  />
                  <span>
                    Enviar <code className="text-amber-300">latitud = null</code> y <code className="text-amber-300">longitud = null</code> para que la Tarea Programada #3 las calcule automáticamente
                  </span>
                </label>

                {!trayectoForm.dejarCoordenadasNulas && (
                  <div className="grid grid-cols-2 gap-2 pt-1">
                    <input
                      type="text"
                      value={trayectoForm.latitud}
                      onChange={(e) => setTrayectoForm({ ...trayectoForm, latitud: e.target.value })}
                      placeholder="Latitud (4.4447)"
                      className="px-2.5 py-1.5 rounded bg-slate-950 border border-slate-700 text-xs font-mono text-white"
                    />
                    <input
                      type="text"
                      value={trayectoForm.longitud}
                      onChange={(e) => setTrayectoForm({ ...trayectoForm, longitud: e.target.value })}
                      placeholder="Longitud (-75.2424)"
                      className="px-2.5 py-1.5 rounded bg-slate-950 border border-slate-700 text-xs font-mono text-white"
                    />
                  </div>
                )}
              </div>

              <button
                type="submit"
                className="w-full py-2.5 rounded-xl bg-indigo-600 hover:bg-indigo-500 text-white text-xs font-bold transition"
              >
                Registrar Parada en Ruta (POST /api/trayectos)
              </button>
            </form>
          </div>

          {/* Panel de Pruebas de Reglas de Negocio de Entrega 3 */}
          <div className="lg:col-span-6 bg-slate-900 border border-slate-800 rounded-2xl p-5 space-y-4">
            <h3 className="text-sm font-bold text-white flex items-center gap-2 border-b border-slate-800 pb-3">
              <ShieldAlert className="w-4 h-4 text-rose-400" />
              <span>Demostración de Reglas Críticas de Negocio (Entrega 3)</span>
            </h3>

            <div className="space-y-3 text-xs text-slate-300">
              <div className="p-3.5 rounded-xl bg-slate-950 border border-slate-800 space-y-2">
                <div className="font-bold text-amber-300">
                  Regla 1: Máximo 7 paradas por código de ruta (ordenParada 0 a 6)
                </div>
                <p className="text-slate-400">
                  Si se intenta registrar una parada con <code className="text-white">ordenParada &gt; 6</code> o superar 7 paradas en la misma ruta, el Backend y la restricción <code className="text-indigo-300">CHECK</code> bloquean la operación.
                </p>
                <button
                  type="button"
                  onClick={handleTestFalloMasDe7Paradas}
                  className="px-3 py-2 rounded-lg bg-rose-950/60 hover:bg-rose-900/70 text-rose-300 border border-rose-500/40 font-semibold transition"
                >
                  ⚡ Probar Fallo: Enviar ordenParada = 8 (Supera 7 paradas)
                </button>
              </div>

              <div className="p-3.5 rounded-xl bg-slate-950 border border-slate-800 space-y-2">
                <div className="font-bold text-amber-300">
                  Regla 2: El Vehículo debe tener todos sus documentos Habilitados y el Conductor debe estar en estado PO
                </div>
                <p className="text-slate-400">
                  Si el vehículo tiene algún documento <code className="text-rose-300">Vencido</code> o <code className="text-amber-300">En Verificación</code>, o el conductor no tiene relación <code className="text-emerald-300">PO</code> con ese vehículo, se rechaza la creación del trayecto.
                </p>
                <button
                  type="button"
                  onClick={handleTestFalloVehiculoNoHabilitado}
                  className="px-3 py-2 rounded-lg bg-rose-950/60 hover:bg-rose-900/70 text-rose-300 border border-rose-500/40 font-semibold transition"
                >
                  ⚡ Probar Fallo: Intentar crear ruta con Vehículo No Habilitado
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* =====================================================================
          SUB-TAB 3: 3 CONSULTAS ESPECIALIZADAS DE RUTAS (CONDUCTOR, PLACA, NO OPERABLES)
         ===================================================================== */}
      {subTab === 'consultas' && (
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-5">
          {/* Consulta 2.2: Por Identificación del Conductor */}
          <div className="lg:col-span-6 bg-slate-900 border border-slate-800 rounded-2xl p-5 space-y-3">
            <h3 className="text-sm font-bold text-white">
              2.2. Consultar Códigos de Ruta Agrupados por Conductor
            </h3>
            <code className="text-[11px] font-mono text-sky-300 block">
              GET /api/trayectos/conductor/&#123;identificacion&#125;
            </code>
            <form onSubmit={handleConsultarPorConductor} className="flex gap-2">
              <select
                value={consultaConductorId}
                onChange={(e) => setConsultaConductorId(e.target.value)}
                className="flex-1 px-3 py-2 rounded-lg bg-slate-950 border border-slate-700 text-xs text-white"
              >
                {personas
                  .filter((p) => p.tipoPersona === 'C')
                  .map((p) => (
                    <option key={p.id} value={p.identificacion}>
                      {p.identificacion} — {p.nombres} {p.apellidos}
                    </option>
                  ))}
              </select>
              <button
                type="submit"
                className="px-4 py-2 rounded-lg bg-indigo-600 hover:bg-indigo-500 text-white text-xs font-bold"
              >
                Consultar
              </button>
            </form>

            {resRutasConductor && (
              <div className="p-3 rounded-xl bg-slate-950 border border-slate-800 text-xs space-y-2">
                <div className="text-slate-400">
                  Conductor CC: <strong className="text-white">{resRutasConductor.identificacionConductor}</strong> • Total rutas:{' '}
                  <strong className="text-emerald-300">{resRutasConductor.totalRutasAsignadas}</strong>
                </div>
                <div className="flex flex-wrap gap-1.5">
                  {(resRutasConductor.codigosRutaAgrupados || []).map((r) => (
                    <span
                      key={r}
                      className="px-2.5 py-1 rounded-lg bg-indigo-500/20 text-indigo-300 border border-indigo-500/30 font-mono font-bold"
                    >
                      {r}
                    </span>
                  ))}
                </div>
              </div>
            )}
          </div>

          {/* Consulta 2.3: Por Placa del Vehículo */}
          <div className="lg:col-span-6 bg-slate-900 border border-slate-800 rounded-2xl p-5 space-y-3">
            <h3 className="text-sm font-bold text-white">
              2.3. Consultar Rutas y Conductor Agrupados por Placa
            </h3>
            <code className="text-[11px] font-mono text-sky-300 block">
              GET /api/trayectos/vehiculo/&#123;placa&#125;
            </code>
            <form onSubmit={handleConsultarPorPlaca} className="flex gap-2">
              <select
                value={consultaPlacaVeh}
                onChange={(e) => setConsultaPlacaVeh(e.target.value)}
                className="flex-1 px-3 py-2 rounded-lg bg-slate-950 border border-slate-700 text-xs font-mono text-amber-300"
              >
                {vehiculos.map((v) => (
                  <option key={v.id} value={v.placa}>
                    {v.placa} ({v.marca} {v.linea})
                  </option>
                ))}
              </select>
              <button
                type="submit"
                className="px-4 py-2 rounded-lg bg-indigo-600 hover:bg-indigo-500 text-white text-xs font-bold"
              >
                Consultar
              </button>
            </form>

            {resRutasVehiculo && (
              <div className="p-3 rounded-xl bg-slate-950 border border-slate-800 text-xs space-y-1.5">
                {(resRutasVehiculo.rutasYConductoresAgrupados || []).map((item, i) => (
                  <div key={i} className="flex items-center justify-between py-1 border-b border-slate-900">
                    <span className="font-mono font-bold text-amber-300">{item.codigoRuta}</span>
                    <span className="text-slate-300">
                      Conductor: <strong>{item.nombreConductor}</strong> ({item.identificacionConductor})
                    </span>
                  </div>
                ))}
              </div>
            )}
          </div>

          {/* Consulta 2.4: Monitor de Rutas que NO se pueden operar */}
          <div className="lg:col-span-12 bg-slate-900 border border-slate-800 rounded-2xl p-5 space-y-3">
            <div className="flex items-center justify-between">
              <div>
                <h3 className="text-sm font-bold text-rose-300">
                  2.4. Monitor de Rutas que NO se Pueden Operar (<code className="text-sky-300">GET /api/trayectos/no-operables</code>)
                </h3>
                <p className="text-xs text-slate-400">
                  Rutas bloqueadas porque el vehículo no está habilitado (documentos vencidos/en verificación) o el conductor está en estado <code className="text-rose-300">RO</code>
                </p>
              </div>
              <button
                onClick={handleConsultarNoOperables}
                className="px-3.5 py-1.5 rounded-xl bg-rose-600 hover:bg-rose-500 text-white text-xs font-bold"
              >
                Actualizar Rutas No Operables
              </button>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
              {resRutasNoOperables.length === 0 ? (
                <div className="col-span-full text-xs text-slate-500 py-6 text-center">
                  Todas las rutas registradas se encuentran operables en este momento.
                </div>
              ) : (
                resRutasNoOperables.map((item, idx) => (
                  <div
                    key={idx}
                    className="p-3.5 rounded-xl bg-rose-950/25 border border-rose-500/40 text-xs space-y-1.5"
                  >
                    <div className="flex items-center justify-between">
                      <span className="font-mono font-bold text-sm text-amber-300">{item.codigoRuta}</span>
                      <span className="px-2 py-0.5 rounded bg-rose-500/20 text-rose-300 font-bold text-[10px]">
                        NO OPERABLE
                      </span>
                    </div>
                    <div className="text-slate-200">
                      Vehículo: <strong className="font-mono">{item.placaVehiculo}</strong> • Conductor:{' '}
                      <strong>{item.conductor}</strong> ({item.identificacionConductor})
                    </div>
                    <div className="text-[11px] text-rose-300 bg-slate-950/80 p-2 rounded border border-rose-500/20">
                      <strong>Causal de Bloqueo:</strong> {item.motivoNoOperable}
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
        <div className="space-y-4">
          <div className="bg-indigo-950/30 border border-indigo-500/40 rounded-2xl p-4 flex flex-wrap items-center justify-between gap-4">
            <div className="flex items-center gap-3">
              <div className="p-2.5 rounded-xl bg-indigo-500/20 text-indigo-300 border border-indigo-500/30">
                <Radio className={`w-6 h-6 ${autoPolling ? 'animate-ping text-emerald-400' : ''}`} />
              </div>
              <div>
                <h3 className="text-sm font-bold text-white">
                  Centro de Demostración en Vivo de Tareas Automáticas (<code className="text-indigo-300">@Scheduled</code>)
                </h3>
                <p className="text-xs text-slate-300">
                  Prepara cualquiera de los 3 escenarios con un clic y activa el Auto-Refresh para ver cómo el Backend actualiza la Base de Datos sin intervención humana.
                </p>
              </div>
            </div>

            <div className="flex items-center gap-2">
              {lastPollTime && (
                <span className="text-xs font-mono text-slate-400">
                  Última lectura BD: <strong className="text-white">{lastPollTime}</strong>
                </span>
              )}
              <button
                onClick={() => setAutoPolling(!autoPolling)}
                className={`px-3.5 py-2 rounded-xl text-xs font-bold border transition flex items-center gap-1.5 ${
                  autoPolling
                    ? 'bg-emerald-600 text-white border-emerald-400'
                    : 'bg-slate-800 hover:bg-slate-700 text-slate-200 border-slate-700'
                }`}
              >
                <RefreshCw className={`w-3.5 h-3.5 ${autoPolling ? 'animate-spin' : ''}`} />
                <span>{autoPolling ? 'Auto-Refresh Activo (cada 10s)' : 'Activar Auto-Refresh (10s)'}</span>
              </button>
            </div>
          </div>

          <div className="grid grid-cols-1 lg:grid-cols-3 gap-4">
            {/* TAREA 1: Licencias de Conducción (Cada 2 minutos = 120s) */}
            <div className="bg-slate-900 border border-slate-800 rounded-2xl p-5 flex flex-col justify-between space-y-4">
              <div className="space-y-2">
                <div className="flex items-center justify-between">
                  <span className="px-2 py-0.5 rounded text-[10px] font-bold bg-amber-500/20 text-amber-300 border border-amber-500/30">
                    TAREA 1 • fixedRate = 120000 ms (2 min)
                  </span>
                  <span className="font-mono text-xs text-slate-400">Ciclo: ~{countdown120}s</span>
                </div>
                <h4 className="text-sm font-bold text-white">
                  1. Verificación de Vigencia de Licencias de Conducción
                </h4>
                <p className="text-xs text-slate-400">
                  Revisa todos los conductores con <code className="text-white">fechaVigenciaLicencia &lt; HOY</code> y cambia automáticamente su estado en <code className="text-indigo-300">vehiculo_personas</code> a <strong className="text-rose-300">RO (Restringido para Operar)</strong>.
                </p>

                <div className="p-3 rounded-xl bg-slate-950 border border-slate-800 space-y-1.5 text-xs">
                  <div className="text-[11px] font-bold text-slate-400 uppercase">
                    Estado Actual en BD (Conductores con Licencia Vencida):
                  </div>
                  {relaciones
                    .filter((r) => r.persona?.fechaVigenciaLicencia && r.persona.fechaVigenciaLicencia < '2026-01-01')
                    .map((r) => (
                      <div key={r.id} className="flex items-center justify-between py-1">
                        <div>
                          <strong className="text-white">{r.persona?.nombres}</strong>
                          <span className="text-[11px] text-slate-400 block">
                            Licencia venció: {r.persona?.fechaVigenciaLicencia}
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

              <button
                onClick={prepararEscenarioTarea1LicenciaVencida}
                className="w-full py-2 rounded-xl bg-amber-600/25 hover:bg-amber-600 text-amber-300 hover:text-white border border-amber-500/40 text-xs font-bold transition flex items-center justify-center gap-1.5"
              >
                <Play className="w-3.5 h-3.5" />
                <span>Poner Conductor Vencido en "PO" y Observar Cambio a "RO"</span>
              </button>
            </div>

            {/* TAREA 2: Documentos de Vehículos (Cada 2 minutos = 120s) */}
            <div className="bg-slate-900 border border-slate-800 rounded-2xl p-5 flex flex-col justify-between space-y-4">
              <div className="space-y-2">
                <div className="flex items-center justify-between">
                  <span className="px-2 py-0.5 rounded text-[10px] font-bold bg-rose-500/20 text-rose-300 border border-rose-500/30">
                    TAREA 2 • fixedRate = 120000 ms (2 min)
                  </span>
                  <span className="font-mono text-xs text-slate-400">Ciclo: ~{countdown120}s</span>
                </div>
                <h4 className="text-sm font-bold text-white">
                  2. Actualización Automática de Documentos Vencidos
                </h4>
                <p className="text-xs text-slate-400">
                  Verifica en <code className="text-indigo-300">vehiculo_documento</code> aquellos registros cuya <code className="text-white">fechaVencimiento &lt; HOY</code> y cambia su estado automáticamente a <strong className="text-rose-300">Vencido</strong>.
                </p>

                <div className="p-3 rounded-xl bg-slate-950 border border-slate-800 space-y-1.5 text-xs max-h-40 overflow-y-auto">
                  <div className="text-[11px] font-bold text-slate-400 uppercase">
                    Documentos con Fecha Expirada en BD:
                  </div>
                  {vehiculos.flatMap((v) =>
                    (v.documentos || [])
                      .filter((d) => d.fechaVencimiento && d.fechaVencimiento < '2026-01-01')
                      .map((d) => (
                        <div key={d.id} className="flex items-center justify-between py-1">
                          <div>
                            <strong className="font-mono text-amber-300">{v.placa}</strong> — {d.documento?.nombre}
                            <span className="text-[10px] text-slate-400 block">Venció: {d.fechaVencimiento}</span>
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

              <button
                onClick={prepararEscenarioTarea2DocumentoVencido}
                className="w-full py-2 rounded-xl bg-rose-600/25 hover:bg-rose-600 text-rose-300 hover:text-white border border-rose-500/40 text-xs font-bold transition flex items-center justify-center gap-1.5"
              >
                <Play className="w-3.5 h-3.5" />
                <span>Poner Doc Expirado en "Habilitado" y Ver Cambio a "Vencido"</span>
              </button>
            </div>

            {/* TAREA 3: Geocodificación de Trayectos (Cada 90 segundos = 90s) */}
            <div className="bg-slate-900 border border-slate-800 rounded-2xl p-5 flex flex-col justify-between space-y-4">
              <div className="space-y-2">
                <div className="flex items-center justify-between">
                  <span className="px-2 py-0.5 rounded text-[10px] font-bold bg-emerald-500/20 text-emerald-300 border border-emerald-500/30">
                    TAREA 3 • fixedRate = 90000 ms (90 seg)
                  </span>
                  <span className="font-mono text-xs text-slate-400">Ciclo: ~{countdown90}s</span>
                </div>
                <h4 className="text-sm font-bold text-white">
                  3. Geocodificación Automática de Trayectos sin Coordenadas
                </h4>
                <p className="text-xs text-slate-400">
                  Detecta trayectos con <code className="text-white">latitud IS NULL</code> o <code className="text-white">longitud IS NULL</code>, consume el servicio <code className="text-indigo-300">Geocoder</code> y actualiza las coordenadas reales en BD.
                </p>

                <div className="p-3 rounded-xl bg-slate-950 border border-slate-800 space-y-1.5 text-xs">
                  <div className="text-[11px] font-bold text-slate-400 uppercase">
                    Paradas de la Ruta Activa ({selectedRuta}):
                  </div>
                  {paradasRuta.slice(0, 3).map((p) => (
                    <div key={p.id} className="flex items-center justify-between py-1">
                      <span className="truncate max-w-[150px] text-slate-300">
                        #{p.ordenParada} {p.ubicacion}
                      </span>
                      {p.latitud != null && p.longitud != null ? (
                        <span className="font-mono text-[11px] text-emerald-300">
                          {Number(p.latitud).toFixed(4)}, {Number(p.longitud).toFixed(4)}
                        </span>
                      ) : (
                        <span className="font-mono text-[11px] text-amber-300 animate-pulse">
                          NULL (Esperando 90s...)
                        </span>
                      )}
                    </div>
                  ))}
                </div>
              </div>

              <button
                onClick={prepararEscenarioTarea3Geocodificacion}
                className="w-full py-2 rounded-xl bg-emerald-600/25 hover:bg-emerald-600 text-emerald-300 hover:text-white border border-emerald-500/40 text-xs font-bold transition flex items-center justify-center gap-1.5"
              >
                <Play className="w-3.5 h-3.5" />
                <span>Crear Trayecto con Coordenadas NULL y Observar Geocoder</span>
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

