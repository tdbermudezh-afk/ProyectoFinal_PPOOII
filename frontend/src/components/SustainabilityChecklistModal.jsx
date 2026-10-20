import React from 'react';
import {
  X,
  CheckSquare,
  Square,
  ArrowRight,
  Award,
  ShieldCheck,
  Car,
  Users,
  MapPin,
  FileSpreadsheet
} from 'lucide-react';

export const SUSTAINABILITY_ITEMS = [
  // ENTREGA 1
  {
    id: 'e1-doc-crud',
    entrega: 'Entrega 1',
    title: 'CRUD Entidad Paramétrica Documentos + CHECK (A/M/AM y RA/RM/RR)',
    endpoint: 'GET/POST/PUT/DELETE /api/documentos',
    module: 'entrega1',
    subTab: 'documentos'
  },
  {
    id: 'e1-veh-crud',
    entrega: 'Entrega 1',
    title: 'CRUD Vehículos + Placa Exacta (Auto ABC123 / Moto ABC12D) + Color Hex',
    endpoint: 'GET/POST/PUT/DELETE /api/vehiculos',
    module: 'entrega1',
    subTab: 'vehiculos'
  },
  {
    id: 'e1-veh-regla-doc',
    entrega: 'Entrega 1',
    title: 'Regla Crítica: Bloqueo sin documento y estado inicial "En Verificación"',
    endpoint: 'POST /api/vehiculos',
    module: 'entrega1',
    subTab: 'vehiculos'
  },
  {
    id: 'e1-veh-add-doc',
    entrega: 'Entrega 1',
    title: 'Asociar nuevos documentos a un vehículo existente',
    endpoint: 'POST /api/vehiculos/{id}/documentos',
    module: 'entrega1',
    subTab: 'asociar'
  },
  {
    id: 'e1-consultas-4',
    entrega: 'Entrega 1',
    title: '4 Consultas E1: Por Placa, Tipo Vehículo, Tipo Documento y Estado Documento',
    endpoint: 'GET /api/vehiculos/placa|tipo|documento|estado-documento',
    module: 'entrega1',
    subTab: 'consultas'
  },

  // ENTREGA 2
  {
    id: 'e2-persona-usuario',
    entrega: 'Entrega 2',
    title: 'Crear Persona Administrativa -> Autogeneración Usuario (Nemotecnia login, password y APIKey)',
    endpoint: 'POST /api/personas',
    module: 'entrega2',
    subTab: 'personas'
  },
  {
    id: 'e2-seguridad-user',
    entrega: 'Entrega 2',
    title: 'Cambio de Password (login en URL) y Regeneración de APIKey',
    endpoint: 'PUT /api/usuarios/{login}/password & GET .../apikey/regenerar',
    module: 'entrega2',
    subTab: 'seguridad'
  },
  {
    id: 'e2-pdf-base64',
    entrega: 'Entrega 2',
    title: 'Subida de Archivos PDF -> Conversión Base64 en LONGBLOB y Visor PDF',
    endpoint: 'POST /api/operaciones/documentos/cargar',
    module: 'entrega2',
    subTab: 'pdfs'
  },
  {
    id: 'e2-conductor-estados',
    entrega: 'Entrega 2',
    title: 'Asociación Conductor–Vehículo y Cambio de Estado (PO, EA, RO)',
    endpoint: 'POST /api/operaciones/conductores/asociar & PUT .../estado',
    module: 'entrega2',
    subTab: 'conductores'
  },
  {
    id: 'e2-public-5',
    entrega: 'Entrega 2',
    title: '5 Servicios Públicos (Sin Token ni APIKey): Vencidos, PO, Placa, Por Vencer y Agrupados',
    endpoint: 'GET /api/public/...',
    module: 'entrega2',
    subTab: 'publicos'
  },

  // ENTREGA 3
  {
    id: 'e3-mapa-rutas',
    entrega: 'Entrega 3',
    title: 'Interfaz Web Visual de Rutas y Paradas Ordenadas + Mapa Interactivo Leaflet',
    endpoint: 'GET /api/trayectos/ruta/{codigoRuta}',
    module: 'entrega3',
    subTab: 'mapa'
  },
  {
    id: 'e3-crear-trayecto',
    entrega: 'Entrega 3',
    title: 'Creación de Trayecto validando Vehículo Habilitado, Conductor PO y Máx 7 Paradas',
    endpoint: 'POST /api/trayectos',
    module: 'entrega3',
    subTab: 'crear'
  },
  {
    id: 'e3-consultas-3',
    entrega: 'Entrega 3',
    title: 'Consultas de Rutas por Conductor, por Placa y Rutas NO Operables',
    endpoint: 'GET /api/trayectos/conductor|vehiculo|no-operables',
    module: 'entrega3',
    subTab: 'consultas'
  },
  {
    id: 'e3-scheduled-3',
    entrega: 'Entrega 3',
    title: 'Monitor en Vivo de 3 Tareas @Scheduled (120s Licencias RO, 120s Docs Vencidos, 90s Geocoder)',
    endpoint: '@Scheduled Tasks + Google Maps / Nominatim Geocoder',
    module: 'entrega3',
    subTab: 'scheduled'
  },

  // ENTREGA 4
  {
    id: 'e4-excel-upload',
    entrega: 'Entrega 4',
    title: 'Cargue Masivo Excel (.xlsx Apache POI) + 3 Procedimientos Almacenados MySQL',
    endpoint: 'POST /api/cargue-masivo/trayectos',
    module: 'entrega4',
    subTab: 'cargue'
  },
  {
    id: 'e4-auditoria-lote',
    entrega: 'Entrega 4',
    title: 'Auditoría por idCargue en trayecto_tmp (CARGADO, VALIDADO, PROCESADO y ERROR acumulativo)',
    endpoint: 'GET /api/cargue-masivo/trayectos/{idCargue}',
    module: 'entrega4',
    subTab: 'cargue'
  }
];

export default function SustainabilityChecklistModal({
  isOpen,
  onClose,
  checkedItems,
  onToggleCheck,
  onNavigate
}) {
  if (!isOpen) return null;

  const completedCount = Object.values(checkedItems).filter(Boolean).length;
  const totalCount = SUSTAINABILITY_ITEMS.length;
  const progressPct = Math.round((completedCount / totalCount) * 100);

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-950/80 backdrop-blur-sm p-4">
      <div className="bg-slate-900 border border-slate-700 rounded-2xl shadow-2xl w-full max-w-3xl overflow-hidden flex flex-col max-h-[88vh]">
        {/* Header */}
        <div className="px-6 py-4 bg-slate-800/90 border-b border-slate-700 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="p-2.5 rounded-xl bg-amber-500/20 text-amber-300 border border-amber-500/30">
              <Award className="w-6 h-6" />
            </div>
            <div>
              <h2 className="text-base font-bold text-white">
                Guía Interactiva de Sustentación Académica (Entregas 1, 2, 3 y 4)
              </h2>
              <p className="text-xs text-slate-400">
                Marca cada requerimiento validado o salta directamente al módulo correspondiente con un clic
              </p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="p-1.5 rounded-lg bg-slate-800 hover:bg-slate-700 text-slate-400 hover:text-white"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Barra de Progreso */}
        <div className="px-6 py-3 bg-slate-950/80 border-b border-slate-800">
          <div className="flex items-center justify-between text-xs mb-1.5">
            <span className="font-semibold text-slate-300">
              Progreso de Demostración ante el Evaluador
            </span>
            <span className="font-mono font-bold text-amber-300">
              {completedCount} / {totalCount} ({progressPct}%)
            </span>
          </div>
          <div className="w-full h-2 rounded-full bg-slate-800 overflow-hidden">
            <div
              className="h-full bg-gradient-to-r from-indigo-500 via-emerald-500 to-amber-400 transition-all duration-300"
              style={{ width: `${progressPct}%` }}
            />
          </div>
        </div>

        {/* Lista de Ítems */}
        <div className="flex-1 overflow-y-auto p-5 space-y-2.5">
          {SUSTAINABILITY_ITEMS.map((item) => {
            const isChecked = !!checkedItems[item.id];
            return (
              <div
                key={item.id}
                className={`p-3 rounded-xl border transition flex items-center justify-between gap-3 ${
                  isChecked
                    ? 'bg-emerald-950/20 border-emerald-500/30'
                    : 'bg-slate-950/60 border-slate-800 hover:border-slate-700'
                }`}
              >
                <div
                  onClick={() => onToggleCheck(item.id)}
                  className="flex items-start gap-3 cursor-pointer flex-1"
                >
                  <button className="mt-0.5 text-emerald-400">
                    {isChecked ? (
                      <CheckSquare className="w-5 h-5 text-emerald-400" />
                    ) : (
                      <Square className="w-5 h-5 text-slate-500" />
                    )}
                  </button>
                  <div>
                    <div className="flex items-center gap-2">
                      <span className="px-2 py-0.5 rounded text-[10px] font-bold uppercase bg-indigo-500/20 text-indigo-300 border border-indigo-500/30">
                        {item.entrega}
                      </span>
                      <span
                        className={`text-xs font-semibold ${
                          isChecked ? 'line-through text-slate-400' : 'text-white'
                        }`}
                      >
                        {item.title}
                      </span>
                    </div>
                    <div className="text-[11px] font-mono text-slate-400 mt-1">
                      Endpoint: <code className="text-sky-300">{item.endpoint}</code>
                    </div>
                  </div>
                </div>

                <button
                  onClick={() => {
                    onNavigate(item.module, item.subTab);
                    onClose();
                  }}
                  className="flex items-center gap-1 px-3 py-1.5 rounded-lg bg-indigo-600/20 hover:bg-indigo-600 text-indigo-300 hover:text-white border border-indigo-500/30 text-xs font-semibold transition shrink-0"
                >
                  <span>Ir a probar</span>
                  <ArrowRight className="w-3.5 h-3.5" />
                </button>
              </div>
            );
          })}
        </div>
      </div>
    </div>
  );
}

