import os
import re

FRONTEND_DIR = r"d:\ProyectoFinal_PPOOII\frontend"
SRC_DIR = os.path.join(FRONTEND_DIR, "src")

FILES_ORDER = [
    os.path.join(SRC_DIR, "services", "api.js"),
    os.path.join(SRC_DIR, "components", "HeaderSecurityBar.jsx"),
    os.path.join(SRC_DIR, "components", "ApiInspectorDrawer.jsx"),
    os.path.join(SRC_DIR, "components", "PdfViewerModal.jsx"),
    os.path.join(SRC_DIR, "components", "SustainabilityChecklistModal.jsx"),
    os.path.join(SRC_DIR, "modules", "Modulo1Entrega1.jsx"),
    os.path.join(SRC_DIR, "modules", "Modulo2Entrega2.jsx"),
    os.path.join(SRC_DIR, "modules", "Modulo3Entrega3.jsx"),
    os.path.join(SRC_DIR, "modules", "Modulo4Entrega4.jsx"),
    os.path.join(SRC_DIR, "App.jsx"),
]

lucide_icons = set()

def strip_imports_and_exports(code):
    lines = code.splitlines()
    out = []
    i = 0
    while i < len(lines):
        line = lines[i]
        stripped = line.strip()

        # Detectar bloques import ... from '...' (de una o varias líneas)
        if stripped.startswith("import "):
            import_block = [line]
            while ";" not in line and "from " not in line and i + 1 < len(lines):
                i += 1
                line = lines[i]
                import_block.append(line)
            full_import = "\n".join(import_block)
            if "lucide-react" in full_import:
                m = re.search(r"\{([^}]+)\}", full_import, re.DOTALL)
                if m:
                    for tok in m.group(1).split(","):
                        icon_name = tok.strip()
                        if icon_name:
                            lucide_icons.add(icon_name)
            i += 1
            continue

        # Reemplazar 'export default function X' -> 'function X'
        if stripped.startswith("export default function "):
            out.append(line.replace("export default function ", "function ", 1))
        elif stripped.startswith("export function "):
            out.append(line.replace("export function ", "function ", 1))
        elif stripped.startswith("export async function "):
            out.append(line.replace("export async function ", "async function ", 1))
        elif stripped.startswith("export const "):
            out.append(line.replace("export const ", "const ", 1))
        else:
            out.append(line)
        i += 1
    return "\n".join(out)

bundled_parts = []
for fpath in FILES_ORDER:
    with open(fpath, "r", encoding="utf-8") as f:
        raw = f.read()
    cleaned = strip_imports_and_exports(raw)
    rel_name = os.path.relpath(fpath, FRONTEND_DIR)
    bundled_parts.append(f"// =====================================================================\n// ARCHIVO FUENTE: {rel_name}\n// =====================================================================\n{cleaned}\n")

icons_list = ",\n  ".join(sorted(lucide_icons))

final_js = f"""import React, {{ useState, useEffect, useRef }} from 'react';
import ReactDOM from 'react-dom/client';
import L from 'leaflet';
import {{
  {icons_list}
}} from 'lucide-react';

{chr(10).join(bundled_parts)}

const root = ReactDOM.createRoot(document.getElementById('root'));
root.render(<App />);
"""

html_output = f"""<!doctype html>
<html lang="es">
  <head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Plataforma de Operación y Sustentación Académica — PPOOII (Entregas 1, 2, 3 y 4)</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&family=JetBrains+Mono:wght@400;500;600&display=swap" rel="stylesheet">
    <link
      rel="stylesheet"
      href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css"
      integrity="sha256-p4NxAoJBhIIN+hmNHrzRCf9tD/miZyoHS5obTRR9BMY="
      crossorigin=""
    />
    <script src="https://cdn.tailwindcss.com"></script>
    <script>
      tailwind.config = {{
        theme: {{
          extend: {{
            fontFamily: {{
              sans: ['Inter', 'system-ui', '-apple-system', 'sans-serif'],
              mono: ['JetBrains Mono', 'Fira Code', 'monospace'],
            }},
          }},
        }},
      }};
    </script>
    <style>
      ::-webkit-scrollbar {{ width: 8px; height: 8px; }}
      ::-webkit-scrollbar-track {{ background: #0f172a; }}
      ::-webkit-scrollbar-thumb {{ background: #334155; border-radius: 9999px; }}
      ::-webkit-scrollbar-thumb:hover {{ background: #475569; }}
      .leaflet-container {{ font-family: 'Inter', system-ui, sans-serif; z-index: 10; }}
    </style>
    <script type="importmap">
      {{
        "imports": {{
          "react": "https://esm.sh/react@18.3.1",
          "react-dom/client": "https://esm.sh/react-dom@18.3.1/client",
          "lucide-react": "https://esm.sh/lucide-react@0.460.0?deps=react@18.3.1",
          "leaflet": "https://esm.sh/leaflet@1.9.4"
        }}
      }}
    </script>
    <script src="https://unpkg.com/@babel/standalone@7.26.2/babel.min.js"></script>
  </head>
  <body class="bg-slate-950 text-slate-100 antialiased selection:bg-indigo-500 selection:text-white">
    <div id="root">
      <div class="min-h-screen flex items-center justify-center bg-slate-950 text-slate-300 font-sans">
        <div class="text-center space-y-2">
          <div class="w-10 h-10 border-4 border-indigo-500 border-t-transparent rounded-full animate-spin mx-auto"></div>
          <p class="text-sm font-semibold">Cargando Plataforma de Operación y Sustentación PPOOII...</p>
        </div>
      </div>
    </div>
    <script type="text/babel" data-type="module">
{final_js}
    </script>
  </body>
</html>
"""

targets = [
    os.path.join(FRONTEND_DIR, "dist", "index.html"),
    r"d:\ProyectoFinal_PPOOII\proyecto\src\main\resources\static\index.html",
    r"d:\ProyectoFinal_PPOOII\proyecto\target\classes\static\index.html",
]

for t in targets:
    os.makedirs(os.path.dirname(t), exist_ok=True)
    with open(t, "w", encoding="utf-8") as f:
        f.write(html_output)
    print(f"Generado: {t} ({len(html_output)} bytes)")

