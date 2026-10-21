import React from 'react';
import { X, FileText, Download, CheckCircle2, Database } from 'lucide-react';

export default function PdfViewerModal({ pdfModal, onClose }) {
  if (!pdfModal || !pdfModal.isOpen) return null;

  const { title, subtitle, base64Data } = pdfModal;

  // Normaliza el contenido Base64 para incrustarlo en un data URI válido
  const cleanBase64 = (base64Data || '').includes(',')
    ? base64Data.split(',')[1].trim()
    : (base64Data || '').trim();

  // Verifica si el Base64 inicia con la cabecera estándar de un archivo PDF (%PDF -> JVBERi0)
  const isStandardPdf = cleanBase64.startsWith('JVBERi0');
  const dataUri = `data:application/pdf;base64,${cleanBase64}`;
  const approxByteSize = Math.round((cleanBase64.length * 3) / 4);

  // Intenta decodificar texto legible si era un Base64 demostrativo corto
  let decodedTextPreview = '';
  try {
    decodedTextPreview = atob(cleanBase64.slice(0, 1200));
  } catch {
    decodedTextPreview = '';
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-950/80 backdrop-blur-sm p-4">
      <div className="bg-slate-900 border border-slate-700 rounded-2xl shadow-2xl w-full max-w-4xl overflow-hidden flex flex-col max-h-[90vh]">
        {/* Header */}
        <div className="px-5 py-3.5 bg-slate-800/80 border-b border-slate-700 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="p-2 rounded-lg bg-indigo-500/20 text-indigo-300 border border-indigo-500/30">
              <FileText className="w-5 h-5" />
            </div>
            <div>
              <h3 className="text-sm font-bold text-white">{title || 'Visor de Documento PDF en Base64 (LONGBLOB)'}</h3>
              <p className="text-xs text-slate-400">
                {subtitle || 'Decodificación en tiempo real desde columna LONGBLOB en MySQL'}
              </p>
            </div>
          </div>
          <div className="flex items-center gap-2">
            <a
              href={dataUri}
              download={`${(title || 'documento').replace(/\s+/g, '_')}.pdf`}
              className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-indigo-600 hover:bg-indigo-500 text-white text-xs font-semibold transition"
            >
              <Download className="w-3.5 h-3.5" />
              <span>Descargar PDF</span>
            </a>
            <button
              onClick={onClose}
              className="p-1.5 rounded-lg bg-slate-800 hover:bg-slate-700 text-slate-300 hover:text-white"
            >
              <X className="w-4 h-4" />
            </button>
          </div>
        </div>

        {/* Metadatos del LONGBLOB Base64 */}
        <div className="px-5 py-2.5 bg-slate-950/70 border-b border-slate-800 flex flex-wrap items-center justify-between gap-2 text-xs">
          <div className="flex items-center gap-2 text-emerald-300">
            <Database className="w-4 h-4" />
            <span>
              Columna BD: <strong>LONGBLOB</strong> (<code className="text-indigo-300">Base64BlobConverter</code>) • Tamaño aprox: <strong>{approxByteSize.toLocaleString()} bytes</strong>
            </span>
          </div>
          <div className="font-mono text-[11px] text-slate-400 truncate max-w-md">
            Prefijo Base64: <code className="text-amber-300">{cleanBase64.slice(0, 36)}...</code>
          </div>
        </div>

        {/* Cuerpo del Visor */}
        <div className="flex-1 bg-slate-950 p-4 overflow-y-auto space-y-3">
          {isStandardPdf ? (
            <iframe
              title="Visor PDF Base64"
              src={dataUri}
              className="w-full h-[55vh] rounded-xl border border-slate-800 bg-white"
            />
          ) : (
            <div className="p-4 rounded-xl bg-slate-900 border border-amber-500/30 space-y-2">
              <div className="text-xs font-semibold text-amber-300">
                El registro contiene una cadena Base64 demostrativa de semilla SQL (no binaria PDF completa) o texto codificado:
              </div>
              <pre className="text-xs font-mono text-slate-200 bg-slate-950 p-3 rounded-lg overflow-x-auto border border-slate-800">
                {decodedTextPreview || cleanBase64}
              </pre>
            </div>
          )}

          <details className="bg-slate-900 rounded-xl border border-slate-800 p-3">
            <summary className="text-xs font-semibold text-indigo-300 cursor-pointer">
              Inspeccionar Cadena Base64 Cruda Almacenada en el LONGBLOB ({cleanBase64.length} caracteres)
            </summary>
            <textarea
              readOnly
              value={cleanBase64}
              rows={4}
              className="mt-2 w-full p-2 rounded bg-slate-950 text-[11px] font-mono text-slate-300 border border-slate-800 focus:outline-none"
            />
          </details>
        </div>
      </div>
    </div>
  );
}

