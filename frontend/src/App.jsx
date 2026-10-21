import React, { useState, useEffect } from 'react';
import {
  Car,
  Users,
  MapPin,
  FileSpreadsheet,
  CheckCircle2
} from 'lucide-react';
import {
  subscribeApiState,
  getSecurityState,
  loginAndObtainJwt
} from './services/api';
import HeaderSecurityBar from './components/HeaderSecurityBar';
import ApiInspectorDrawer from './components/ApiInspectorDrawer';
import PdfViewerModal from './components/PdfViewerModal';
import SustainabilityChecklistModal, {
  SUSTAINABILITY_ITEMS
} from './components/SustainabilityChecklistModal';
import Modulo1Entrega1 from './modules/Modulo1Entrega1';
import Modulo2Entrega2 from './modules/Modulo2Entrega2';
import Modulo3Entrega3 from './modules/Modulo3Entrega3';
import Modulo4Entrega4 from './modules/Modulo4Entrega4';

export default function App() {
  const [activeModule, setActiveModule] = useState('entrega1');
  const [activeSubTab, setActiveSubTab] = useState(null);

  // Estado global sincronizado con el servicio API
  const [apiSnapshot, setApiSnapshot] = useState({
    securityState: getSecurityState(),
    apiLogs: [],
    latestLog: null,
    inspectorOpen: false
  });

  // Estado del visor PDF Base64 (LONGBLOB)
  const [pdfModal, setPdfModal] = useState({
    isOpen: false,
    title: '',
    subtitle: '',
    base64Data: ''
  });

  // Estado del Checklist de Sustentación
  const [checklistOpen, setChecklistOpen] = useState(false);
  const [checkedItems, setCheckedItems] = useState({});

  useEffect(() => {
    const unsub = subscribeApiState((snap) => {
      setApiSnapshot(snap);
    });
    // Autenticación inicial silenciosa para obtener un JWT real firmado al arrancar la app
    loginAndObtainJwt();
    return () => unsub();
  }, []);

  const handleToggleCheck = (id) => {
    setCheckedItems((prev) => ({ ...prev, [id]: !prev[id] }));
  };

  const handleNavigateFromChecklist = (moduleKey, subTabKey) => {
    setActiveModule(moduleKey);
    setActiveSubTab(subTabKey);
  };

  const completedChecksCount = Object.values(checkedItems).filter(Boolean).length;

  const MODULES_NAV = [
    {
      id: 'entrega1',
      badge: 'Entrega 1',
      title: 'Vehículos, Documentos y 4 Consultas',
      icon: Car,
      color: 'from-indigo-600 to-blue-600'
    },
    {
      id: 'entrega2',
      badge: 'Entrega 2',
      title: 'Personas, Usuarios, PDFs Base64 y Públicos',
      icon: Users,
      color: 'from-emerald-600 to-teal-600'
    },
    {
      id: 'entrega3',
      badge: 'Entrega 3',
      title: 'Rutas, Mapa Leaflet y Tareas @Scheduled',
      icon: MapPin,
      color: 'from-sky-600 to-indigo-600'
    },
    {
      id: 'entrega4',
      badge: 'Entrega 4',
      title: 'Cargue Masivo Excel y 3 Procedimientos',
      icon: FileSpreadsheet,
      color: 'from-amber-600 to-orange-600'
    }
  ];

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 flex flex-col pb-16">
      {/* 1. Barra Superior Global de Seguridad e Inspector */}
      <HeaderSecurityBar
        securityState={apiSnapshot.securityState}
        latestLog={apiSnapshot.latestLog}
        inspectorOpen={apiSnapshot.inspectorOpen}
        logsCount={apiSnapshot.apiLogs.length}
        onOpenChecklist={() => setChecklistOpen(true)}
        completedChecksCount={completedChecksCount}
        totalChecksCount={SUSTAINABILITY_ITEMS.length}
      />

      {/* 2. Selector Principal de los 4 Módulos (Entregas 1, 2, 3 y 4) */}
      <div className="max-w-[1600px] w-full mx-auto px-4 pt-4">
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-3">
          {MODULES_NAV.map((mod) => {
            const Icon = mod.icon;
            const isActive = activeModule === mod.id;
            return (
              <button
                key={mod.id}
                onClick={() => {
                  setActiveModule(mod.id);
                  setActiveSubTab(null);
                }}
                className={`p-3.5 rounded-2xl border text-left transition flex items-center gap-3.5 ${
                  isActive
                    ? `bg-gradient-to-r ${mod.color} text-white border-white/20 shadow-lg`
                    : 'bg-slate-900/90 hover:bg-slate-900 text-slate-300 border-slate-800 hover:border-slate-700'
                }`}
              >
                <div
                  className={`p-2.5 rounded-xl ${
                    isActive ? 'bg-white/20 text-white' : 'bg-slate-800 text-indigo-400'
                  }`}
                >
                  <Icon className="w-5 h-5" />
                </div>
                <div className="min-w-0">
                  <div
                    className={`text-[10px] font-bold uppercase tracking-wider ${
                      isActive ? 'text-white/90' : 'text-indigo-400'
                    }`}
                  >
                    {mod.badge}
                  </div>
                  <div className="text-xs sm:text-sm font-bold truncate">{mod.title}</div>
                </div>
              </button>
            );
          })}
        </div>
      </div>

      {/* 3. Contenido Dinámico del Módulo Seleccionado */}
      <main className="max-w-[1600px] w-full mx-auto px-4 py-5 flex-1">
        {activeModule === 'entrega1' && (
          <Modulo1Entrega1
            activeSubTab={activeSubTab}
            setActiveSubTab={setActiveSubTab}
            onOpenPdfModal={setPdfModal}
          />
        )}
        {activeModule === 'entrega2' && (
          <Modulo2Entrega2
            activeSubTab={activeSubTab}
            setActiveSubTab={setActiveSubTab}
            onOpenPdfModal={setPdfModal}
          />
        )}
        {activeModule === 'entrega3' && (
          <Modulo3Entrega3
            activeSubTab={activeSubTab}
            setActiveSubTab={setActiveSubTab}
          />
        )}
        {activeModule === 'entrega4' && <Modulo4Entrega4 />}
      </main>

      {/* 4. Modal Visor de PDFs Base64 (LONGBLOB) */}
      <PdfViewerModal
        pdfModal={pdfModal}
        onClose={() => setPdfModal({ isOpen: false, title: '', subtitle: '', base64Data: '' })}
      />

      {/* 5. Modal Guía Interactiva de Sustentación */}
      <SustainabilityChecklistModal
        isOpen={checklistOpen}
        onClose={() => setChecklistOpen(false)}
        checkedItems={checkedItems}
        onToggleCheck={handleToggleCheck}
        onNavigate={handleNavigateFromChecklist}
      />

      {/* 6. Barra Inferior y Drawer Colapsable del Inspector de Peticiones API */}
      <ApiInspectorDrawer
        apiLogs={apiSnapshot.apiLogs}
        latestLog={apiSnapshot.latestLog}
        inspectorOpen={apiSnapshot.inspectorOpen}
      />
    </div>
  );
}

