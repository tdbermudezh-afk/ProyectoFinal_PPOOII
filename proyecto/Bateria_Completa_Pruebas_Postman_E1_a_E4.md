# Batería Maestra Exhaustiva de Pruebas Unitarias en Postman (100% Cobertura: Entregas 1, 2, 3 y 4)

Este documento contiene las **50 pruebas unitarias individuales de Postman** que cubren de manera atómica **absolutamente todos los ítems y sub-ítems** de la matriz oficial de requerimientos (Base de Datos, Restricciones de Chequeo, CRUDs completos `POST-GET-PUT-DELETE`, Consultas Protegidas, Consultas Públicas, Tareas Programadas `@Scheduled` y Procedimientos Almacenados con Excel).

> [!IMPORTANT]
> **Credenciales de Seguridad para Todos los Endpoints Protegidos:**
> * **`x-api-key`**: `0ac02b5d-468d-4bf8-ac11-db0d64f96c15` *(o la llave vigente de tu usuario Administrador)*.
> * **`token`**: `Bearer token-admin-2026`

---

## Matriz de Cobertura 1 a 1 (Todos los Requerimientos vs. Tests Unitarios)

| Entrega | Categoría | Ítem de la Rúbrica | Tests Unitarios en Postman |
| :---: | :--- | :--- | :--- |
| **E2** | **API (15%)** | **8.** Seguridad de servicios con Token y APIKey + Rol Admin (`A`). | **Tests 1, 2, 3 y 4** |
| **E2** | **BD (20%) & API (15%)** | **1.** Funciones `POST – GET – PUT` de Persona, autogeneración de Usuario Admin con nemotecnia, Conductor sin usuario y Restricciones `CHECK` (`CC/CE/PASAPORTE`, `C/A`). | **Tests 5, 6, 7, 8, 9 y 10** |
| **E3** | **BD (10%)** | **2.1.** Adición de campos en `Persona`: fecha de vigencia de licencia y archivo BLOB en Base64. | **Test 11** |
| **E2** | **API (10%)** | **2.** Servicio `PUT` para cambio de password enviando login por URL y clave por Body. | **Test 12** |
| **E2** | **API (10%)** | **3.** Servicio `GET` para regenerar el APIKey de un usuario específico. | **Test 13** |
| **E1** | **BD (35%) & API (25%)** | **2.** CRUD completo (`POST – GET – PUT – DELETE`) de la entidad paramétrica `Documento` + Restricciones `CHECK` (`aplica_a: A/M/AM`, `obligatorio: RA/RM/RR`). | **Tests 14, 15, 16, 17, 18 y 19** |
| **E1** | **BD (35%) & API (25%)** | **1.** CRUD completo (`POST – GET – PUT – DELETE`) de `Vehículo`, estado inicial *"En Verificación"*, prohibición sin documentos, sintaxis de placa y Restricciones `CHECK` (`tipo_vehiculo`, `tipo_servicio`, `tipo_combustible`). | **Tests 20, 21, 22, 23, 24, 25, 26, 27, 28, 29 y 30** |
| **E1** | **API (30%)** | **3.1 a 3.4.** Las 4 Consultas de Vehículos (por placa, por tipo de vehículo, por tipo de documento en común, por estado del documento). | **Tests 31, 32, 33 y 34** |
| **E1** | **API (20%)** | **4.** Servicio para agregar documentos asociados a un vehículo existente + Restricción `CHECK` de estado (`Habilitado`, `Vencido`, `En Verificación`). | **Tests 35 y 36** |
| **E2** | **API (20%) & BD** | **4.** Servicio `POST` para cargue/actualización de uno o varios documentos con archivo PDF en Base64 (`BLOB`). | **Test 37** |
| **E2** | **API (5%) & BD** | **5.** Servicio para asociar vehículos a un conductor específico (validando rol `'C'`). | **Tests 38 y 39** |
| **E2** | **API (5%) & BD** | **6.** Servicio para cambiar el estado del conductor (`PO`, `EA`, `RO`) + Restricción `CHECK` de estado. | **Tests 40 y 41** |
| **E2** | **API (20%)** | **7.** Los 5 Servicios Públicos que no requieren de token ni APIKey. | **Tests 42, 43, 44, 45 y 46** |
| **E3** | **BD (10%) & API (45%)** | **1.1 y Servicios 1 a 4.** Creación de `Trayecto` condicionado a documentos `Habilitado` + conductor `PO` + límite de paradas, y las 4 consultas protegidas de rutas. | **Tests 47, 48, 49, 50, 51, 52, 53, 54 y 55** |
| **E3** | **Técnicos (25%)** | **1.1, 1.2 y 1.3.** Las 3 Tareas Programadas `@Scheduled` (Licencias vencidas a `RO` cada 2 min, Documentos vencidos a `Vencido` cada 2 min, y Geocodificación Google Maps cada 90 s). | **Tests 56, 57 y 58** |
| **E4** | **BD (60%) & API (40%)** | **1 y 2.** Tabla `trayecto_tmp`, 3 Procedimientos Almacenados (`CARGUE`, `VALIDACION`, `PROCESADO`), cargue masivo Excel (`MultipartFile`) y consulta por `idCargue`. | **Tests 59 y 60** |

---

# BLOQUE A: Seguridad, Autenticación y Autorización por Roles (Entrega 2 - Punto 8 - 15% API)

### **Test 1: Rechazo de petición sin cabeceras (`401 Unauthorized`)**
* **Requerimiento por cumplir:** Entrega 2 - Servicios Punto 8: Protección obligatoria con `x-api-key` y `token`.
* **Finalidad / Objetivo:** Verificar que cualquier consumo sin el encabezado `x-api-key` sea rechazado inmediatamente por el interceptor de seguridad.
* **Método:** `GET`
* **URL:** `http://localhost:9001/api/vehiculos`
* **Headers:** *(Ninguno)*
* **Respuesta esperada (`401 Unauthorized`):**
  ```json
  {
    "error": "Acceso denegado: Se requiere el encabezado 'x-api-key'"
  }
  ```

---

### **Test 2: Rechazo de petición con `x-api-key` pero sin `token` (`401 Unauthorized`)**
* **Requerimiento por cumplir:** Entrega 2 - Servicios Punto 8: Exigencia de Token junto al APIKey.
* **Finalidad / Objetivo:** Comprobar que el sistema exija el encabezado `token` o `Authorization` además de la `x-api-key`.
* **Método:** `GET`
* **URL:** `http://localhost:9001/api/vehiculos`
* **Headers:**
  * `x-api-key`: `0ac02b5d-468d-4bf8-ac11-db0d64f96c15`
* **Respuesta esperada (`401 Unauthorized`):**
  ```json
  {
    "error": "Acceso denegado: Se requiere el encabezado 'token' o 'Authorization'"
  }
  ```

---

### **Test 3: Rechazo de petición con `x-api-key` inexistente o inválida (`401 Unauthorized`)**
* **Requerimiento por cumplir:** Entrega 2 - Servicios Punto 8: Validación de existencia de la APIKey en la tabla `usuarios`.
* **Finalidad / Objetivo:** Confirmar que una llave inventada o revocada no permita el acceso a los servicios protegidos.
* **Método:** `GET`
* **URL:** `http://localhost:9001/api/vehiculos`
* **Headers:**
  * `x-api-key`: `llave-falsa-inexistente-999`
  * `token`: `Bearer token-admin-2026`
* **Respuesta esperada (`401 Unauthorized`):**
  ```json
  {
    "error": "Acceso denegado: 'x-api-key' invalida o no reconocida"
  }
  ```

---

### **Test 4: Acceso exitoso con credenciales válidas de Administrador (`200 OK`)**
* **Requerimiento por cumplir:** Entrega 2 - Servicios Punto 8: Acceso autorizado únicamente a usuarios de tipo `ADMINISTRADOR ('A')`.
* **Finalidad / Objetivo:** Verificar que con `x-api-key` y `token` válidos de un usuario Administrativo el interceptor conceda el paso al controlador.
* **Método:** `GET`
* **URL:** `http://localhost:9001/api/vehiculos`
* **Headers:**
  * `x-api-key`: `0ac02b5d-468d-4bf8-ac11-db0d64f96c15`
  * `token`: `Bearer token-admin-2026`
* **Respuesta esperada (`200 OK`):** Retorna el listado JSON de vehículos registrados.

---

# BLOQUE B: Gestión de Personas, Usuarios y Licencias (Entrega 2 Puntos 1, 2, 3 y Entrega 3 BD 2.1)

### **Test 5: Crear Persona Administrativo (`POST`) - Autogeneración de Usuario con Nemotecnia**
* **Requerimiento por cumplir:** Entrega 2 - Servicios Punto 1 (15%): *"POST de una persona, generando automáticamente un usuario en el caso de persona tipo ADMINISTRATIVO con login por nemotecnia, password automático y APIKey automática"*.
* **Finalidad / Objetivo:** Comprobar que al crear una persona con `tipoPersona: "A"`, se cree automáticamente su entidad `Usuario` con login `sn1055667788` (primera letra nombre + primera letra apellido + identificación), password y APIKey.
* **Método:** `POST`
* **URL:** `http://localhost:9001/api/personas`
* **Headers:** `Content-Type: application/json`
* **Body:**
  ```json
  {
    "identificacion": "1055667788",
    "tipoIdentificacion": "CC",
    "nombres": "Sofia",
    "apellidos": "Navarro",
    "correo": "sofia.navarro@transporte.com",
    "tipoPersona": "A"
  }
  ```
* **Respuesta esperada (`201 Created`):** Retorna la persona creada con el objeto `usuarioGenerado` poblado.

---

### **Test 6: Crear Persona Conductor (`POST`) - Sin generación de Usuario**
* **Requerimiento por cumplir:** Entrega 2 - BD Punto 1.1 y Servicios Punto 1: Registro de persona tipo Conductor (`'C'`), la cual no debe generar registro en la tabla `usuarios`.
* **Finalidad / Objetivo:** Verificar que al registrar un Conductor (`tipoPersona: "C"`), se guarde en `personas` con su licencia y vigencia, pero `usuarioGenerado` sea `null`.
* **Método:** `POST`
* **URL:** `http://localhost:9001/api/personas`
* **Headers:** `Content-Type: application/json`
* **Body:**
  ```json
  {
    "identificacion": "1088990011",
    "tipoIdentificacion": "CC",
    "nombres": "camilo",
    "apellidos": "vargas",
    "correo": "camilo.vargas@conductores.com",
    "tipoPersona": "C",
    "licenciaConduccion": "JVBERi0xLjQKJcTl8uXprKWvozMgMCBvYmoKPDwK...",
    "fechaVigenciaLicencia": "2028-06-30"
  }
  ```
* **Respuesta esperada (`201 Created`):** Retorna el conductor creado con `"usuarioGenerado": null`.

---

### **Test 7: Validación de Restricción de Chequeo en Persona (`tipoIdentificacion` y `tipoPersona`)**
* **Requerimiento por cumplir:** Entrega 2 - BD Punto 3.1 (20%): *"Para el caso de la entidad PERSONA crear restricciones de chequeo o validación sobre el tipo de identificación (`CC`, `CE`, `PASAPORTE`) y el tipo de persona (`C`, `A`)"*.
* **Finalidad / Objetivo:** Comprobar que el sistema rechace con `400 Bad Request` valores no permitidos en `tipoIdentificacion` (ej. `"TI"`) o en `tipoPersona` (ej. `"X"`).
* **Método:** `POST`
* **URL:** `http://localhost:9001/api/personas`
* **Headers:** `Content-Type: application/json`
* **Body:**
  ```json
  {
    "identificacion": "1099999999",
    "tipoIdentificacion": "TI",
    "nombres": "Juan",
    "apellidos": "Perez",
    "correo": "juan.perez@test.com",
    "tipoPersona": "X"
  }
  ```
* **Respuesta esperada (`400 Bad Request`):** Mensaje indicando que el tipo de identificación y el tipo de persona son inválidos.

---

### **Test 8: Consultar todas las Personas (`GET`)**
* **Requerimiento por cumplir:** Entrega 2 - Servicios Punto 1: Función `GET` de Persona.
* **Finalidad / Objetivo:** Obtener el listado completo de personas registradas en la base de datos.
* **Método:** `GET`
* **URL:** `http://localhost:9001/api/personas`
* **Headers:** *(Ninguno)*
* **Respuesta esperada (`200 OK`):** Arreglo JSON con todas las personas registradas.

---

### **Test 9: Consultar una Persona por su ID (`GET /{id}`)**
* **Requerimiento por cumplir:** Entrega 2 - Servicios Punto 1: Función `GET` individual de Persona.
* **Finalidad / Objetivo:** Recuperar los datos de una persona específica mediante su clave primaria.
* **Método:** `GET`
* **URL:** `http://localhost:9001/api/personas/3`
* **Headers:** *(Ninguno)*
* **Respuesta esperada (`200 OK`):** Objeto JSON correspondiente al conductor Pedro Ramírez (`id: 3`).

---

### **Test 10: Actualizar datos de una Persona (`PUT /{id}`)**
* **Requerimiento por cumplir:** Entrega 2 - Servicios Punto 1: Función `PUT` de Persona.
* **Finalidad / Objetivo:** Verificar la modificación de atributos de una persona existente.
* **Método:** `PUT`
* **URL:** `http://localhost:9001/api/personas/1`
* **Headers:** `Content-Type: application/json`
* **Body:**
  ```json
  {
    "identificacion": "1001234567",
    "tipoIdentificacion": "CC",
    "nombres": "Carlos Alberto",
    "apellidos": "Gomez Perez",
    "correo": "carlos.gomez@transporte.com",
    "tipoPersona": "A"
  }
  ```
* **Respuesta esperada (`200 OK`):** Retorna el objeto `Persona` con los nombres y apellidos actualizados.

---

### **Test 11: Registrar / Actualizar Licencia de Conducción en Base64 y Fecha de Vigencia en un Conductor (`PUT`)**
* **Requerimiento por cumplir:** Entrega 3 - BD Punto 2.1 (40% BD): *"Adición de campos en la entidad PERSONA: fecha de licencia y campo BLOB para almacenar el archivo de licencia de una persona en formato BASE64"*.
* **Finalidad / Objetivo:** Comprobar que un conductor (`tipoPersona: "C"`) almacene su soporte de licencia de conducción en Base64 y su fecha de vigencia.
* **Método:** `PUT`
* **URL:** `http://localhost:9001/api/personas/3`
* **Headers:** `Content-Type: application/json`
* **Body:**
  ```json
  {
    "identificacion": "1020304050",
    "tipoIdentificacion": "CC",
    "nombres": "Pedro",
    "apellidos": "Ramirez",
    "correo": "pedro.ramirez@conductores.com",
    "tipoPersona": "C",
    "licenciaConduccion": "JVBERi0xLjQKJcTl8uXprKWvozMgMCBvYmoKPDwK...",
    "fechaVigenciaLicencia": "2027-12-31"
  }
  ```
* **Respuesta esperada (`200 OK`):** Retorna el conductor con `licenciaConduccion` y `fechaVigenciaLicencia: "2027-12-31"` persistidos.

---

### **Test 12: Cambiar Contraseña de un Usuario (`PUT`)**
* **Requerimiento por cumplir:** Entrega 2 - Servicios Punto 2 (10%): *"Servicio que permita el cambio de password de un usuario específico, donde la nueva contraseña se envíe por el body y el login por la URL"*.
* **Finalidad / Objetivo:** Validar el cambio de clave de acceso del usuario `cg1001234567`.
* **Método:** `PUT`
* **URL:** `http://localhost:9001/api/usuarios/cg1001234567/password`
* **Headers:** `Content-Type: application/json`
* **Body:**
  ```json
  {
    "password": "NuevoPasswordSeguro2026#"
  }
  ```
* **Respuesta esperada (`200 OK`):** Usuario actualizado con la nueva contraseña.

---

### **Test 13: Regenerar APIKey de un Usuario (`GET`)**
* **Requerimiento por cumplir:** Entrega 2 - Servicios Punto 3 (10%): *"Servicio GET que permita nuevamente la generación del APIKey de un usuario específico"*.
* **Finalidad / Objetivo:** Comprobar que el sistema genere y asigne una nueva `apikey` aleatoria para el login indicado.
* **Método:** `GET`
* **URL:** `http://localhost:9001/api/usuarios/sn1055667788/apikey/regenerar`
* **Headers:** *(Ninguno)*
* **Respuesta esperada (`200 OK`):**
  ```json
  {
    "login": "sn1055667788",
    "nuevaApiKey": "...",
    "mensaje": "APIKey regenerada con éxito"
  }
  ```

---

# BLOQUE C: CRUD Completo y Restricciones de la Entidad Paramétrica `Documento` (Entrega 1 - Punto 2 - 25% API)

### **Test 14: Crear Nuevo Tipo de Documento Paramétrico (`POST`)**
* **Requerimiento por cumplir:** Entrega 1 - Servicios Punto 2: Función `POST` del CRUD de `Documento`.
* **Finalidad / Objetivo:** Registrar un nuevo documento en el catálogo paramétrico cumpliendo las restricciones de `aplicaA` (`A`, `M`, `AM`) y `obligatorio` (`RA`, `RM`, `RR`).
* **Método:** `POST`
* **URL:** `http://localhost:9001/api/documentos`
* **Headers:** `Content-Type`, `x-api-key`, `token`
* **Body:**
  ```json
  {
    "codigo": "DOC-POLIZA-01",
    "nombre": "Póliza Contractual Pasajeros",
    "aplicaA": "A",
    "obligatorio": "RA",
    "descripcion": "Póliza de responsabilidad civil para automóviles"
  }
  ```
* **Respuesta esperada (`201 Created`):** Retorna el documento paramétrico creado con su ID autogenerado.

---

### **Test 15: Validación de Restricciones de Chequeo en Tabla `Documento` (`aplicaA` y `obligatorio`)**
* **Requerimiento por cumplir:** Entrega 1 - BD Punto 2.2 (40% BD): *"Restricciones de chequeo para los campos de la tabla documentos: Tipo de Vehículo al que aplica (`A`, `M`, `AM`) y Documento obligatorio (`RA`, `RM`, `RR`)"*.
* **Finalidad / Objetivo:** Verificar que el sistema rechace con `400 Bad Request` valores fuera de dominio en `aplicaA` o `obligatorio`.
* **Método:** `POST`
* **URL:** `http://localhost:9001/api/documentos`
* **Headers:** `Content-Type`, `x-api-key`, `token`
* **Body:**
  ```json
  {
    "codigo": "DOC-INV-99",
    "nombre": "Documento Inválido",
    "aplicaA": "Z",
    "obligatorio": "XX",
    "descripcion": "Prueba de chequeo"
  }
  ```
* **Respuesta esperada (`400 Bad Request`):** Mensaje indicando que `aplicaA` debe ser `'A'`, `'M'` o `'AM'` y `obligatorio` debe ser `'RA'`, `'RM'` o `'RR'`.

---

### **Test 16: Listar Todos los Documentos Paramétricos (`GET`)**
* **Requerimiento por cumplir:** Entrega 1 - Servicios Punto 2: Función `GET` general del CRUD de `Documento`.
* **Finalidad / Objetivo:** Consultar el catálogo completo de tipos de documentos configurados en el sistema.
* **Método:** `GET`
* **URL:** `http://localhost:9001/api/documentos`
* **Headers:** `x-api-key`, `token`
* **Respuesta esperada (`200 OK`):** Lista JSON con todos los documentos del catálogo.

---

### **Test 17: Buscar Documento Paramétrico por ID (`GET /{id}`)**
* **Requerimiento por cumplir:** Entrega 1 - Servicios Punto 2: Función `GET` por ID del CRUD de `Documento`.
* **Finalidad / Objetivo:** Obtener el detalle de un tipo de documento específico por su identificador único.
* **Método:** `GET`
* **URL:** `http://localhost:9001/api/documentos/1`
* **Headers:** `x-api-key`, `token`
* **Respuesta esperada (`200 OK`):** Objeto JSON del documento ID `1` (`SOAT`).

---

### **Test 18: Actualizar Documento Paramétrico (`PUT /{id}`)**
* **Requerimiento por cumplir:** Entrega 1 - Servicios Punto 2: Función `PUT` del CRUD de `Documento`.
* **Finalidad / Objetivo:** Modificar la descripción o atributos de un documento paramétrico existente.
* **Método:** `PUT`
* **URL:** `http://localhost:9001/api/documentos/1`
* **Headers:** `Content-Type`, `x-api-key`, `token`
* **Body:**
  ```json
  {
    "codigo": "DOC001",
    "nombre": "SOAT Vigente",
    "aplicaA": "AM",
    "obligatorio": "RR",
    "descripcion": "Seguro Obligatorio de Accidentes de Tránsito Actualizado"
  }
  ```
* **Respuesta esperada (`200 OK`):** Objeto `Documento` actualizado.

---

### **Test 19: Eliminar Documento Paramétrico (`DELETE /{id}`)**
* **Requerimiento por cumplir:** Entrega 1 - Servicios Punto 2: Función `DELETE` del CRUD de `Documento`.
* **Finalidad / Objetivo:** Comprobar la eliminación física de un documento paramétrico creado para pruebas (ej. el creado en el Test 14).
* **Método:** `DELETE`
* **URL:** `http://localhost:9001/api/documentos/6` *(Usar el ID retornado en el Test 14)*
* **Headers:** `x-api-key`, `token`
* **Respuesta esperada (`204 No Content`):** Eliminación exitosa sin cuerpo de respuesta.

---

# BLOQUE D: CRUD Completo, Consultas y Reglas de Negocio de `Vehículo` (Entrega 1 - Puntos 1, 3 y 4)

### **Test 20: Crear Automóvil Exitoso (`POST`) - Fuerza estado inicial "En Verificación"**
* **Requerimiento por cumplir:** Entrega 1 - Servicios Punto 1 (25%): *"Realizar el CRUD de un vehículo, poniendo como estado inicial de los documentos asociados En Verificación al momento de crearlo"*.
* **Finalidad / Objetivo:** Verificar que el vehículo se registre y que su documento asociado quede automáticamente en estado `"En Verificación"`.
* **Método:** `POST`
* **URL:** `http://localhost:9001/api/vehiculos`
* **Headers:** `Content-Type`, `x-api-key`, `token`
* **Body:**
  ```json
  {
    "placa": "GHJ456",
    "tipoVehiculo": "Automóvil",
    "marca": "Chevrolet",
    "modelo": 2025,
    "tipoServicio": "Pr",
    "tipoCombustible": "Gasolina",
    "capacidadPasajeros": 5,
    "color": "#1D3557",
    "linea": "Onix Turbo",
    "documentos": [
      {
        "documento": { "id": 1 },
        "fechaExpedicion": "2026-01-01",
        "fechaVencimiento": "2027-01-01"
      }
    ]
  }
  ```
* **Respuesta esperada (`201 Created`):** Retorna el vehículo creado con su documento en `"estado": "En Verificación"`.

---

### **Test 21: Rechazar creación de Vehículo sin documentos asociados (`400 Bad Request`)**
* **Requerimiento por cumplir:** Entrega 1 - Servicios Punto 1: *"Importante: No se puede crear un vehículo sin que tenga un documento asociado"*.
* **Finalidad / Objetivo:** Confirmar que no se permita persistir un vehículo si el arreglo `documentos` está vacío.
* **Método:** `POST`
* **URL:** `http://localhost:9001/api/vehiculos`
* **Headers:** `Content-Type`, `x-api-key`, `token`
* **Body:**
  ```json
  {
    "placa": "ERT456",
    "tipoVehiculo": "Automóvil",
    "marca": "Mazda",
    "modelo": 2024,
    "tipoServicio": "Pr",
    "tipoCombustible": "Gasolina",
    "capacidadPasajeros": 5,
    "color": "#2B2D42",
    "linea": "Mazda 3",
    "documentos": []
  }
  ```
* **Respuesta esperada (`400 Bad Request`):**
  ```json
  {
    "status": 400,
    "message": "No se puede crear un vehículo sin que tenga un documento asociado."
  }
  ```

---

### **Test 22: Rechazar creación por incompatibilidad de `aplica_a` (`400 Bad Request`)**
* **Requerimiento por cumplir:** Entrega 1 - BD Punto 2.2: Validación de compatibilidad entre el tipo de vehículo y el campo `aplica_a` del documento.
* **Finalidad / Objetivo:** Comprobar que no se pueda asociar un documento exclusivo de Motocicleta (`id: 5`, `aplica_a: 'M'`) a un Automóvil.
* **Método:** `POST`
* **URL:** `http://localhost:9001/api/vehiculos`
* **Headers:** `Content-Type`, `x-api-key`, `token`
* **Body:**
  ```json
  {
    "placa": "BNM789",
    "tipoVehiculo": "Automóvil",
    "marca": "Mazda",
    "modelo": 2024,
    "tipoServicio": "Pr",
    "tipoCombustible": "Gasolina",
    "capacidadPasajeros": 5,
    "color": "#2B2D42",
    "linea": "Mazda 3",
    "documentos": [
      {
        "documento": { "id": 5 },
        "fechaExpedicion": "2026-01-01",
        "fechaVencimiento": "2027-01-01"
      }
    ]
  }
  ```
* **Respuesta esperada (`400 Bad Request`):** `"El documento 'Certificado Gases Motos' solo aplica para Motocicletas."`

---

### **Test 23: Rechazar Automóvil con sintaxis de placa de Motocicleta (`400 Bad Request`)**
* **Requerimiento por cumplir:** Entrega 1 - Regla de negocio de sintaxis de placa para Automóvil (3 letras + 3 números).
* **Finalidad / Objetivo:** Verificar que un Automóvil con placa `ABC12D` sea rechazado.
* **Método:** `POST`
* **URL:** `http://localhost:9001/api/vehiculos`
* **Headers:** `Content-Type`, `x-api-key`, `token`
* **Body:** Automóvil con `"placa": "ABC12D"`.
* **Respuesta esperada (`400 Bad Request`):** `"Para tipo Automóvil, la placa debe contener 3 letras seguidas de 3 números (ejemplo: ABC123)."`

---

### **Test 24: Rechazar Motocicleta con sintaxis de placa de Automóvil (`400 Bad Request`)**
* **Requerimiento por cumplir:** Entrega 1 - Regla de negocio de sintaxis de placa para Motocicleta (3 letras + 2 números + 1 letra).
* **Finalidad / Objetivo:** Verificar que una Motocicleta con placa `ABC123` sea rechazada.
* **Método:** `POST`
* **URL:** `http://localhost:9001/api/vehiculos`
* **Headers:** `Content-Type`, `x-api-key`, `token`
* **Body:** Motocicleta con `"placa": "ABC123"`.
* **Respuesta esperada (`400 Bad Request`):** `"Para tipo Motocicleta, la placa debe contener 3 letras seguidas de 2 números y terminar en una letra (ejemplo: ABC12D)."`

---

### **Test 25: Validación de Restricción de Chequeo en Vehículo - `tipoVehiculo` inválido**
* **Requerimiento por cumplir:** Entrega 1 - BD Punto 2.1: *"Restricciones de chequeo para la tabla vehículo: Tipo de Vehículo (`Automóvil` o `Motocicleta`)"*.
* **Finalidad / Objetivo:** Comprobar que el sistema rechace tipos de vehículo no permitidos como `"Camión"`.
* **Método:** `POST`
* **URL:** `http://localhost:9001/api/vehiculos`
* **Headers:** `Content-Type`, `x-api-key`, `token`
* **Body:** Vehículo con `"tipoVehiculo": "Camión"`.
* **Respuesta esperada (`400 Bad Request`):** `"El tipo de vehículo debe ser 'Automóvil' o 'Motocicleta'"`.

---

### **Test 26: Validación de Restricción de Chequeo en Vehículo - `tipoServicio` y `tipoCombustible` inválidos**
* **Requerimiento por cumplir:** Entrega 1 - BD Punto 2.1: *"Restricciones de chequeo para la tabla vehículo: Tipo de Servicios (`Pu`, `Pr`) y Tipo de Combustible (`Gasolina`, `Gas`, `Disel`)"*.
* **Finalidad / Objetivo:** Comprobar que valores fuera de dominio en `tipoServicio` (ej. `"Oficial"`) o en `tipoCombustible` (ej. `"Eléctrico"`) sean rechazados con `400 Bad Request`.
* **Método:** `POST`
* **URL:** `http://localhost:9001/api/vehiculos`
* **Headers:** `Content-Type`, `x-api-key`, `token`
* **Body:**
  ```json
  {
    "placa": "ZXC123",
    "tipoVehiculo": "Automóvil",
    "marca": "BYD",
    "modelo": 2025,
    "tipoServicio": "Of",
    "tipoCombustible": "Electrico",
    "capacidadPasajeros": 5,
    "color": "#FFFFFF",
    "linea": "Yuan Plus",
    "documentos": [
      {
        "documento": { "id": 1 },
        "fechaExpedicion": "2026-01-01",
        "fechaVencimiento": "2027-01-01"
      }
    ]
  }
  ```
* **Respuesta esperada (`400 Bad Request`):** Error de validación indicando los valores permitidos para `tipoServicio` y `tipoCombustible`.

---

### **Test 27: Listar Todos los Vehículos (`GET`)**
* **Requerimiento por cumplir:** Entrega 1 - Servicios Punto 1: Función `GET` general del CRUD de `Vehículo`.
* **Finalidad / Objetivo:** Obtener todos los vehículos registrados en el sistema.
* **Método:** `GET`
* **URL:** `http://localhost:9001/api/vehiculos`
* **Headers:** `x-api-key`, `token`
* **Respuesta esperada (`200 OK`):** Arreglo JSON de vehículos.

---

### **Test 28: Buscar Vehículo por su ID (`GET /{id}`)**
* **Requerimiento por cumplir:** Entrega 1 - Servicios Punto 1: Función `GET` individual por ID del CRUD de `Vehículo`.
* **Finalidad / Objetivo:** Consultar un vehículo específico mediante su clave primaria.
* **Método:** `GET`
* **URL:** `http://localhost:9001/api/vehiculos/1`
* **Headers:** `x-api-key`, `token`
* **Respuesta esperada (`200 OK`):** Objeto JSON del vehículo ID `1` (`ABC123`).

---

### **Test 29: Actualizar Vehículo existente (`PUT /{id}`)**
* **Requerimiento por cumplir:** Entrega 1 - Servicios Punto 1: Función `PUT` del CRUD de `Vehículo`.
* **Finalidad / Objetivo:** Actualizar atributos de un vehículo existente validando las reglas de negocio.
* **Método:** `PUT`
* **URL:** `http://localhost:9001/api/vehiculos/1`
* **Headers:** `Content-Type`, `x-api-key`, `token`
* **Body:**
  ```json
  {
    "placa": "ABC123",
    "tipoVehiculo": "Automóvil",
    "marca": "Toyota",
    "modelo": 2023,
    "tipoServicio": "Pr",
    "tipoCombustible": "Gasolina",
    "capacidadPasajeros": 5,
    "color": "#E63946",
    "linea": "Corolla Cross SEG"
  }
  ```
* **Respuesta esperada (`200 OK`):** Retorna el vehículo con modelo `2023` y línea `Corolla Cross SEG`.

---

### **Test 30: Eliminar Vehículo (`DELETE /{id}`)**
* **Requerimiento por cumplir:** Entrega 1 - Servicios Punto 1: Función `DELETE` del CRUD de `Vehículo`.
* **Finalidad / Objetivo:** Eliminar por completo un vehículo de prueba (ej. el creado en el Test 20) y sus documentos en cascada.
* **Método:** `DELETE`
* **URL:** `http://localhost:9001/api/vehiculos/8` *(Usar el ID retornado en el Test 20)*
* **Headers:** `x-api-key`, `token`
* **Respuesta esperada (`204 No Content`):** Vehículo eliminado exitosamente.

---

### **Test 31: Consulta 3.1 - Buscar vehículo por número de placa (`GET`)**
* **Requerimiento por cumplir:** Entrega 1 - Servicios Punto 3.1: *"Buscar vehículo por número de placa"*.
* **Finalidad / Objetivo:** Localizar un vehículo único utilizando su número de placa en la URL.
* **Método:** `GET`
* **URL:** `http://localhost:9001/api/vehiculos/placa/ABC123`
* **Headers:** `x-api-key`, `token`
* **Respuesta esperada (`200 OK`):** Retorna el vehículo Toyota con placa `ABC123`.

---

### **Test 32: Consulta 3.2 - Buscar vehículos por tipo de vehículo (`GET`)**
* **Requerimiento por cumplir:** Entrega 1 - Servicios Punto 3.2: *"Buscar vehículos por tipo de vehículo"*.
* **Finalidad / Objetivo:** Filtrar y retornar todos los vehículos que pertenezcan al tipo `Automóvil` (o `Motocicleta`).
* **Método:** `GET`
* **URL:** `http://localhost:9001/api/vehiculos/tipo/Automóvil`
* **Headers:** `x-api-key`, `token`
* **Respuesta esperada (`200 OK`):** Lista de todos los automóviles registrados.

---

### **Test 33: Consulta 3.3 - Buscar vehículos que tengan en común un tipo de documento (`GET`)**
* **Requerimiento por cumplir:** Entrega 1 - Servicios Punto 3.3: *"Buscar vehículos que tengan en común un tipo de documento"*.
* **Finalidad / Objetivo:** Obtener todos los vehículos que tengan asociado el documento paramétrico con ID `1` (`SOAT`).
* **Método:** `GET`
* **URL:** `http://localhost:9001/api/vehiculos/documento/1`
* **Headers:** `x-api-key`, `token`
* **Respuesta esperada (`200 OK`):** Lista de vehículos que comparten el documento ID `1`.

---

### **Test 34: Consulta 3.4 - Buscar vehículos según el estado del documento asociado (`GET`)**
* **Requerimiento por cumplir:** Entrega 1 - Servicios Punto 3.4: *"Buscar vehículos según el estado del documento asociado al vehículo (Habilitado, Vencido o En Verificación)"*.
* **Finalidad / Objetivo:** Filtrar los vehículos cuyos documentos se encuentren actualmente en estado `Habilitado`.
* **Método:** `GET`
* **URL:** `http://localhost:9001/api/vehiculos/estado-documento/Habilitado`
* **Headers:** `x-api-key`, `token`
* **Respuesta esperada (`200 OK`):** Lista de vehículos con documentación en estado `Habilitado`.

---

### **Test 35: Agregar nuevo documento asociado a un vehículo (`POST /{id}/documentos`)**
* **Requerimiento por cumplir:** Entrega 1 - Servicios Punto 4 (20%): *"Crear un servicio que permita agregar documentos asociados a un vehículo"*.
* **Finalidad / Objetivo:** Vincular un nuevo documento del catálogo al historial de un vehículo existente.
* **Método:** `POST`
* **URL:** `http://localhost:9001/api/vehiculos/1/documentos`
* **Headers:** `Content-Type`, `x-api-key`, `token`
* **Body:**
  ```json
  {
    "documento": { "id": 2 },
    "fechaExpedicion": "2026-03-01",
    "fechaVencimiento": "2027-03-01",
    "estado": "Habilitado"
  }
  ```
* **Respuesta esperada (`200 OK`):** Retorna el vehículo ID `1` con el nuevo documento agregado a su lista.

---

### **Test 36: Validación de Restricción de Chequeo en Estado del Documento del Vehículo (`400 Bad Request`)**
* **Requerimiento por cumplir:** Entrega 1 - BD Punto 2.3: *"Restricción de chequeo para el campo Estado del documento asociado al vehículo (`Habilitado`, `Vencido`, `En Verificación`)"*.
* **Finalidad / Objetivo:** Comprobar que el sistema rechace estados de documento no válidos (ej. `"Activo"` o `"Pendiente"`).
* **Método:** `POST`
* **URL:** `http://localhost:9001/api/vehiculos/1/documentos`
* **Headers:** `Content-Type`, `x-api-key`, `token`
* **Body:**
  ```json
  {
    "documento": { "id": 1 },
    "fechaExpedicion": "2026-03-01",
    "fechaVencimiento": "2027-03-01",
    "estado": "Pendiente"
  }
  ```
* **Respuesta esperada (`400 Bad Request`):** `"El estado del documento debe ser 'Habilitado', 'Vencido' o 'En Verificación'"`.

---

# BLOQUE E: Documentos PDF en Base64 y Asignaciones Conductor-Vehículo (Entrega 2 - Puntos 4, 5 y 6)

### **Test 37: Cargue / Actualización de uno o varios documentos con PDF en Base64 (`POST`)**
* **Requerimiento por cumplir:** Entrega 2 - BD Punto 2.1 y Servicios Punto 4 (20%): *"Servicio POST que permita el cargue y/o actualización de uno o varios documentos relacionados con los vehículos contemplando un archivo PDF en Base64"*.
* **Finalidad / Objetivo:** Verificar el procesamiento de un arreglo de documentos almacenando la cadena Base64 del PDF en el campo `LONGTEXT`/`BLOB`.
* **Método:** `POST`
* **URL:** `http://localhost:9001/api/operaciones/documentos/cargar`
* **Headers:** `Content-Type`, `x-api-key`, `token`
* **Body:**
  ```json
  [
    {
      "vehiculoId": 1,
      "documentoId": 3,
      "fechaExpedicion": "2026-03-01",
      "fechaVencimiento": "2027-03-01",
      "estado": "Habilitado",
      "documentoPdfBase64": "JVBERi0xLjQKJcTl8uXprKWvozMgMCBvYmoKPDwK..."
    }
  ]
  ```
* **Respuesta esperada (`201 Created`):** Lista de registros `VehiculoDocumento` persistidos con el PDF Base64.

---

### **Test 38: Asociar Vehículo a un Conductor específico (`POST`)**
* **Requerimiento por cumplir:** Entrega 2 - BD Punto 1.3 y Servicios Punto 5 (5%): *"Realizar un servicio que permita asociar los vehículos que puede operar un conductor específico"*.
* **Finalidad / Objetivo:** Crear la relación en `vehiculo_personas` entre un conductor (`tipoPersona: 'C'`) y un vehículo con estado inicial `EA`.
* **Método:** `POST`
* **URL:** `http://localhost:9001/api/asignaciones/conductor/3/vehiculo/2`
* **Headers:** `Content-Type`, `x-api-key`, `token`
* **Body:**
  ```json
  {
    "estadoInicial": "EA"
  }
  ```
* **Respuesta esperada (`201 Created`):** Relación creada con `"estadoConductor": "EA"`.

---

### **Test 39: Rechazar asociación de Vehículo a una Persona que NO es Conductor (`400 Bad Request`)**
* **Requerimiento por cumplir:** Entrega 2 - BD Punto 1.3: *"Entidad relación vehículo con persona que tengan como tipo de persona CONDUCTOR"*.
* **Finalidad / Objetivo:** Comprobar que si se intenta asociar un vehículo a un Administrativo (ej. `id: 1` o `id: 6`), el sistema lo rechace.
* **Método:** `POST`
* **URL:** `http://localhost:9001/api/asignaciones/conductor/6/vehiculo/1`
* **Headers:** `Content-Type`, `x-api-key`, `token`
* **Body:**
  ```json
  {
    "estadoInicial": "EA"
  }
  ```
* **Respuesta esperada (`400 Bad Request`):** `"La persona seleccionada no es de tipo CONDUCTOR"`.

---

### **Test 40: Cambiar el estado del Conductor en relación con el Vehículo a `PO` (`PUT`)**
* **Requerimiento por cumplir:** Entrega 2 - Servicios Punto 6 (5%): *"Realizar un servicio que permita cambiar el estado del conductor en relación con el vehículo (PO – Puede Operar, EA – Espera de Aprobación, RO – Restringido para Operar)"*.
* **Finalidad / Objetivo:** Actualizar el estado de una relación existente a `PO` para autorizar la operación del conductor.
* **Método:** `PUT`
* **URL:** `http://localhost:9001/api/asignaciones/2/estado`
* **Headers:** `Content-Type`, `x-api-key`, `token`
* **Body:**
  ```json
  {
    "estado": "PO"
  }
  ```
* **Respuesta esperada (`200 OK`):** Relación actualizada con `"estadoConductor": "PO"`.

---

### **Test 41: Validación de Restricción de Chequeo en Estado del Conductor (`400 Bad Request`)**
* **Requerimiento por cumplir:** Entrega 2 - BD Punto 3.2: *"Para el caso de la entidad relación de vehículo con persona crear restricciones de chequeo o validación sobre el campo de estado del conductor asociado al vehículo (`PO`, `EA`, `RO`)"*.
* **Finalidad / Objetivo:** Verificar que el sistema rechace cualquier código de estado distinto de `PO`, `EA` o `RO` (ej. `"ACTIVO"`).
* **Método:** `PUT`
* **URL:** `http://localhost:9001/api/asignaciones/2/estado`
* **Headers:** `Content-Type`, `x-api-key`, `token`
* **Body:**
  ```json
  {
    "estado": "ACTIVO"
  }
  ```
* **Respuesta esperada (`400 Bad Request`):** `"Estado inválido. Los estados permitidos son: PO, EA, RO"`.

---

# BLOQUE F: Los 5 Servicios Públicos sin Token ni APIKey (Entrega 2 - Punto 7 - 20% API)

### **Test 42: Consulta Pública 1 - Vehículos con documentos vencidos (`GET`)**
* **Requerimiento por cumplir:** Entrega 2 - Servicios Punto 7.1: *"Consultar todos los vehículos que tengan documentos vencidos (sin token)"*.
* **Finalidad / Objetivo:** Obtener sin cabeceras de autenticación la lista de vehículos que tienen al menos un documento vencido.
* **Método:** `GET`
* **URL:** `http://localhost:9001/api/public/vehiculos/documentos-vencidos`
* **Headers:** *(Ninguno)*
* **Respuesta esperada (`200 OK`):** Lista de vehículos con documentos vencidos.

---

### **Test 43: Consulta Pública 2 - Conductores que pueden operar `PO` (`GET`)**
* **Requerimiento por cumplir:** Entrega 2 - Servicios Punto 7.2: *"Consultar todos los conductores que puedan operar (sin token)"*.
* **Finalidad / Objetivo:** Consultar libremente todos los conductores que tienen estado `PO`.
* **Método:** `GET`
* **URL:** `http://localhost:9001/api/public/conductores/autorizados`
* **Headers:** *(Ninguno)*
* **Respuesta esperada (`200 OK`):** Lista de conductores habilitados para operar.

---

### **Test 44: Consulta Pública 3 - Vehículo por placa relacionando Conductores y Documentos (`GET`)**
* **Requerimiento por cumplir:** Entrega 2 - Servicios Punto 7.3: *"Consultar un vehículo por placa donde se relacione la información de los conductores asociados, así como los documentos (sin token)"*.
* **Finalidad / Objetivo:** Recuperar el detalle integral de un vehículo por su placa junto con sus arreglos de documentos y conductores.
* **Método:** `GET`
* **URL:** `http://localhost:9001/api/public/vehiculos/placa/ABC123/detalles`
* **Headers:** *(Ninguno)*
* **Respuesta esperada (`200 OK`):** Objeto `Vehiculo` con sus listas `documentos` y `conductores`.

---

### **Test 45: Consulta Pública 4 - Vehículos con documentos por vencer según parámetro de tiempo (`GET`)**
* **Requerimiento por cumplir:** Entrega 2 - Servicios Punto 7.4: *"Consultar los vehículos que tienen documentos por vencer con un tiempo que se especifique como parámetro en la consulta (sin token)"*.
* **Finalidad / Objetivo:** Filtrar los vehículos cuyos documentos vencen dentro de los próximos `N` días indicados en `diasFaltantes`.
* **Método:** `GET`
* **URL:** `http://localhost:9001/api/public/vehiculos/documentos-por-vencer?diasFaltantes=30`
* **Headers:** *(Ninguno)*
* **Respuesta esperada (`200 OK`):** Lista de vehículos con documentos próximos a vencer en 30 días.

---

### **Test 46: Consulta Pública 5 - Total de personas agrupadas por tipo (`GET`)**
* **Requerimiento por cumplir:** Entrega 2 - Servicios Punto 7.5: *"Consultar el total de las personas agrupadas por tipo (sin token)"*.
* **Finalidad / Objetivo:** Obtener el conteo estadístico de personas agrupadas por tipo (`A` y `C`).
* **Método:** `GET`
* **URL:** `http://localhost:9001/api/public/personas/agrupadas-por-tipo`
* **Headers:** *(Ninguno)*
* **Respuesta esperada (`200 OK`):** Objeto JSON del tipo `{"A": 3, "C": 6}`.

---

# BLOQUE G: Entidad `Trayecto`, Reglas de Creación y 4 Consultas de Rutas (Entrega 3 - BD 10% y API 45%)

### **Test 47: Rechazo al crear Trayecto - Conductor en estado `EA` (No está en `PO`)**
* **Requerimiento por cumplir:** Entrega 3 - BD Punto 1.1: *"La creación de un trayecto debe estar condicionado a que el conductor asociado al trayecto pueda operar PO"*.
* **Finalidad / Objetivo:** Comprobar que un conductor en estado `EA` (`id: 4`) no pueda registrar trayectos.
* **Método:** `POST`
* **URL:** `http://localhost:9001/api/trayectos`
* **Headers:** `Content-Type`, `x-api-key`, `token`
* **Body:**
  ```json
  {
    "persona": { "id": 4 },
    "vehiculo": { "id": 2 },
    "codigoRuta": "RUTA-FAIL-EA",
    "ubicacion": "Parque Manuel Murillo Toro, Ibagué",
    "ordenParada": 0,
    "login": "cg1001234567"
  }
  ```
* **Respuesta esperada (`400 Bad Request`):** `"No se puede registrar el trayecto: El conductor no cuenta con estado 'PO' (Puede Operar) para este vehículo."`

---

### **Test 48: Rechazo al crear Trayecto - Conductor en estado `RO` (Restringido para Operar)**
* **Requerimiento por cumplir:** Entrega 3 - BD Punto 1.1: Bloqueo de creación de trayectos para conductores en estado `RO`.
* **Finalidad / Objetivo:** Verificar que un conductor restringido (`RO`) sea rechazado al intentar asignarle un trayecto.
* **Método:** `POST`
* **URL:** `http://localhost:9001/api/trayectos`
* **Headers:** `Content-Type`, `x-api-key`, `token`
* **Body:**
  ```json
  {
    "persona": { "id": 7 },
    "vehiculo": { "id": 1 },
    "codigoRuta": "RUTA-FAIL-RO",
    "ubicacion": "Terminal de Transportes, Ibagué",
    "ordenParada": 0,
    "login": "cg1001234567"
  }
  ```
* **Respuesta esperada (`400 Bad Request`):** `"No se puede registrar el trayecto: El conductor no cuenta con estado 'PO' (Puede Operar) para este vehículo."`

---

### **Test 49: Rechazo al crear Trayecto - Vehículo con documentos NO Habilitados (`Vencido` / `En Verificación`)**
* **Requerimiento por cumplir:** Entrega 3 - BD Punto 1.1: *"La creación de un trayecto debe estar condicionado a que la relación de los documentos con el vehículo deben estar en estado Habilitado"*.
* **Finalidad / Objetivo:** Confirmar que no se permita registrar trayectos en vehículos cuyos documentos estén en verificación (`id: 6`) o vencidos (`id: 5`).
* **Método:** `POST`
* **URL:** `http://localhost:9001/api/trayectos`
* **Headers:** `Content-Type`, `x-api-key`, `token`
* **Body:**
  ```json
  {
    "persona": { "id": 3 },
    "vehiculo": { "id": 6 },
    "codigoRuta": "RUTA-FAIL-DOCS",
    "ubicacion": "Centro Comercial La Estación, Ibagué",
    "ordenParada": 0,
    "login": "cg1001234567"
  }
  ```
* **Respuesta esperada (`400 Bad Request`):** `"No se puede registrar el trayecto: Todos los documentos del vehículo deben estar en estado 'Habilitado'."`

---

### **Test 50: Rechazo al crear Trayecto - Persona asociada es Administrativo (`'A'`) y no Conductor (`'C'`)**
* **Requerimiento por cumplir:** Entrega 3 - BD Punto 1.1: *"idpersona: para este caso solo deben realizarse asociaciones de personas de tipo conductor"*.
* **Finalidad / Objetivo:** Comprobar que el sistema impida registrar un trayecto si la persona enviada tiene rol Administrativo (`id: 6`).
* **Método:** `POST`
* **URL:** `http://localhost:9001/api/trayectos`
* **Headers:** `Content-Type`, `x-api-key`, `token`
* **Body:**
  ```json
  {
    "persona": { "id": 6 },
    "vehiculo": { "id": 1 },
    "codigoRuta": "RUTA-FAIL-ADMIN",
    "ubicacion": "Conservatorio del Tolima, Ibagué",
    "ordenParada": 0,
    "login": "cg1001234567"
  }
  ```
* **Respuesta esperada (`400 Bad Request`):** `"La persona asociada debe ser de tipo CONDUCTOR ('C')."`

---

### **Test 51: Crear Trayecto Exitoso cumpliendo todas las condiciones (`POST`)**
* **Requerimiento por cumplir:** Entrega 3 - BD Punto 1.1: Creación exitosa de trayecto con conductor `'C'` en estado `'PO'` y vehículo con todos sus documentos en `'Habilitado'`.
* **Finalidad / Objetivo:** Registrar satisfactoriamente una parada de ruta y verificar su persistencia.
* **Método:** `POST`
* **URL:** `http://localhost:9001/api/trayectos`
* **Headers:** `Content-Type`, `x-api-key`, `token`
* **Body:**
  ```json
  {
    "persona": { "id": 5 },
    "vehiculo": { "id": 2 },
    "codigoRuta": "RUTA-EXPRESS-03",
    "ubicacion": "Plaza de Bolívar, Ibagué, Tolima",
    "ordenParada": 0,
    "login": "cg1001234567"
  }
  ```
* **Respuesta esperada (`201 Created`):** Objeto `Trayecto` creado exitosamente.

---

### **Test 52: Servicio 1 (E3) - Consultar ruta por `codigoRuta` visualizando trayectos en orden (`GET`)**
* **Requerimiento por cumplir:** Entrega 3 - Servicios Punto 1 (25%): *"Crear un servicio de consulta de rutas teniendo como parámetro de entrada el código de ruta, éste deberá ser un servicio protegido (requiere token) y deberá visualizar en orden los trayectos y su respectiva información asociada"*.
* **Finalidad / Objetivo:** Obtener la secuencia ordenada de paradas (`ordenParada: 0, 1, 2...`) y la información completa del vehículo y conductor asociados.
* **Método:** `GET`
* **URL:** `http://localhost:9001/api/trayectos/ruta/RUTA-IBG-01`
* **Headers:** `x-api-key`, `token`
* **Respuesta esperada (`200 OK`):** Arreglo de trayectos ordenados ascendentemente por `ordenParada`.

---

### **Test 53: Servicio 2 (E3) - Consultar códigos de ruta agrupados por identificación de conductor (`GET`)**
* **Requerimiento por cumplir:** Entrega 3 - Servicios Punto 2 (25%): *"Crear un servicio de consulta teniendo como parámetro el número de identificación de un conductor específico donde se visualice únicamente los códigos de rutas de forma agrupada (requiere token)"*.
* **Finalidad / Objetivo:** Verificar que se retornen únicamente los strings de los códigos de ruta sin duplicados (`DISTINCT`) para la cédula `1020304050`.
* **Método:** `GET`
* **URL:** `http://localhost:9001/api/trayectos/conductor/1020304050`
* **Headers:** `x-api-key`, `token`
* **Respuesta esperada (`200 OK`):**
  ```json
  [
    "RUTA-IBG-01",
    "RUTA-CENTRO-IBG",
    "RUTA-ZONA-NORTE"
  ]
  ```

---

### **Test 54: Servicio 3 (E3) - Consultar código de ruta y conductor agrupados por placa de vehículo (`GET`)**
* **Requerimiento por cumplir:** Entrega 3 - Servicios Punto 3 (25%): *"Crear un servicio de consulta teniendo como parámetro la placa de un vehículo específico donde se visualice el código de ruta y el conductor asociado al trayecto de forma agrupada (requiere token)"*.
* **Finalidad / Objetivo:** Comprobar la obtención agrupada del código de ruta y los datos del conductor para la placa `KLU456`.
* **Método:** `GET`
* **URL:** `http://localhost:9001/api/trayectos/vehiculo/KLU456`
* **Headers:** `x-api-key`, `token`
* **Respuesta esperada (`200 OK`):**
  ```json
  [
    {
      "codigoRuta": "RUTA-BUS-02",
      "identificacionConductor": "1040506070",
      "nombresConductor": "Luisa",
      "apellidosConductor": "Morales"
    }
  ]
  ```

---

### **Test 55: Servicio 4 (E3) - Consultar rutas y trayectos donde el vehículo NO esté habilitado o el conductor esté en `RO` (`GET`)**
* **Requerimiento por cumplir:** Entrega 3 - Servicios Punto 4 (25%): *"Crear un servicio de consulta de las rutas y la información de los trayectos donde el vehículo NO esté habilitado o el conductor esté Restringido para Operar"*.
* **Finalidad / Objetivo:** Detectar y listar cualquier ruta registrada cuyo vehículo tenga documentos vencidos/en verificación o cuyo conductor haya pasado a estado `RO`.
* **Método:** `GET`
* **URL:** `http://localhost:9001/api/trayectos/no-operables`
* **Headers:** `x-api-key`, `token`
* **Respuesta esperada (`200 OK`):** Lista con la información de las rutas inoperables, su `motivoInoperable` y sus `trayectos`.

---

# BLOQUE H: Pruebas de las 3 Tareas Programadas `@Scheduled` (Entrega 3 - Técnicos 25%)

### **Test 56: Tarea Programada 1.1 (Cada 2 min) - Vencimiento de Licencia cambia Conductor a `RO`**
* **Requerimiento por cumplir:** Entrega 3 - Técnicos Punto 1.1 (30%): *"Crear una tarea programada cada 2 minutos verifique la fecha de vigencia de las licencias de los conductores registrados del sistema y quienes estén vencidas se deberá cambiar el estado del conductor en la relación con el vehículo a un estado RO – Restringido para Operar"*.
* **Finalidad / Objetivo:** Comprobar en vivo que al ponerle una fecha de licencia vencida (`2020-01-01`) a un conductor que está en `PO`, la tarea `@Scheduled` lo detecta automáticamente y cambia su relación a `RO`.
* **Paso 1 (Simular licencia vencida con `PUT`):**
  * **Método:** `PUT` | **URL:** `http://localhost:9001/api/personas/5`
  * **Body:**
    ```json
    {
      "identificacion": "1040506070",
      "tipoIdentificacion": "CC",
      "nombres": "Luisa",
      "apellidos": "Morales",
      "correo": "luisa.morales@conductores.com",
      "tipoPersona": "C",
      "fechaVigenciaLicencia": "2020-01-01"
    }
    ```
* **Paso 2 (Esperar máximo 2 minutos y consultar rutas no operables con `GET`):**
  * **Método:** `GET` | **URL:** `http://localhost:9001/api/trayectos/no-operables`
  * **Headers:** `x-api-key`, `token`
* **Respuesta esperada (`200 OK`):** La consola de Spring Boot imprimirá `[ALERTA LICENCIA VENCIDA]` y el endpoint `/api/trayectos/no-operables` mostrará la ruta de Luisa Morales con `"motivoInoperable": "Conductor restringido para operar (RO)"`. *(Luego puedes restaurar su fecha a `2027-12-31` y su estado a `PO`)*.

---

### **Test 57: Tarea Programada 1.2 (Cada 2 min) - Vencimiento de Documento cambia estado a `Vencido`**
* **Requerimiento por cumplir:** Entrega 3 - Técnicos Punto 1.2 (30%): *"Crear una tarea programada cada 2 minutos verifique la fecha de vigencia de los documentos asociados al vehículo y documentos que estén vencidos se deberá cambiar el estado de documento asociado al vehículo a VENCIDO"*.
* **Finalidad / Objetivo:** Verificar que si un documento asociado a un vehículo tiene `fechaVencimiento` pasada pero aún figura como `Habilitado` o `En Verificación`, la tarea `@Scheduled` de 2 minutos lo cambia automáticamente a `"Vencido"`.
* **Paso 1 (Cargar documento con fecha pasada):**
  * **Método:** `POST` | **URL:** `http://localhost:9001/api/operaciones/documentos/cargar`
  * **Headers:** `Content-Type`, `x-api-key`, `token`
  * **Body:**
    ```json
    [
      {
        "vehiculoId": 7,
        "documentoId": 1,
        "fechaExpedicion": "2023-01-01",
        "fechaVencimiento": "2024-01-01",
        "estado": "Habilitado",
        "documentoPdfBase64": "JVBERi0xLjQK..."
      }
    ]
    ```
* **Paso 2 (Esperar máximo 2 minutos y consultar el vehículo `GET /api/vehiculos/7`):**
* **Respuesta esperada (`200 OK`):** La consola mostrará `[ALERTA DOCUMENTO VENCIDO]` y al consultar `GET /api/vehiculos/7`, el documento aparecerá automáticamente con `"estado": "Vencido"`.

---

### **Test 58: Tarea Programada 1.3 (Cada 90 seg) - Geocodificación automática con Google Maps API**
* **Requerimiento por cumplir:** Entrega 3 - Técnicos Punto 1.3 (40%): *"Crear una tarea programada cada 90 segundos verifique los trayectos que no contienen una longitud y latitud asociada a la ubicación registrada en el trayecto, valores que deberán ser extraídos en apoyo del API externa Google Maps y persistir en la base de datos"*.
* **Finalidad / Objetivo:** Confirmar que el trayecto creado en el Test 51 con `latitud: null` y `longitud: null` para `"Plaza de Bolívar, Ibagué, Tolima"` sea procesado por `ScheduledTasks` y `Geocoder.java` asignándole sus coordenadas reales en la base de datos.
* **Método:** `GET`
* **URL:** `http://localhost:9001/api/trayectos/ruta/RUTA-EXPRESS-03`
* **Headers:** `x-api-key`, `token`
* **Respuesta esperada (`200 OK`):** Pasados 90 segundos desde el Test 51, el registro retorna `latitud: 4.438...` y `longitud: -75.232...` extraídos de Google Maps.

---

# BLOQUE I: Cargue Masivo de Trayectos vía Excel y Procedimientos Almacenados (Entrega 4 - 100%)

### **Test 59: Servicio 1 (E4 - 80% API y 60% BD) - Cargue Masivo de Plantilla Excel e Invocación de los 3 Procedimientos Almacenados**
* **Requerimiento por cumplir:**
  * **BD 1.1:** Creación de la entidad temporal `trayecto_tmp`.
  * **BD 2.1, 2.2 y 2.3:** Creación e invocación de los procedimientos almacenados `CARGAR_TRAYECTOS_EXCEL`, `VALIDAR_TRAYECTOS_CARGUE` y `PROCESAR_TRAYECTOS_EXCEL`.
  * **BD 3.1:** Restricción de chequeo sobre el campo `estado` (`CARGADO`, `VALIDADO`, `PROCESADO`, `ERROR`).
  * **API 1:** Servicio de cargue masivo a través de plantilla Excel (`MultipartFile`).
* **Finalidad / Objetivo:** Subir el archivo `Plantilla_Cargue_Trayectos_Ibague.xlsx` (20 filas) y comprobar que los 3 procedimientos almacenados carguen las 20 filas en `trayecto_tmp`, validen las reglas de negocio marcando 15 como `PROCESADO` (insertándolas en la tabla transaccional `trayecto`) y 5 como `ERROR` con su respectiva observación acumulativa.
* **Método:** `POST`
* **URL:** `http://localhost:9001/api/cargue-masivo/trayectos`
* **Headers:**
  * `x-api-key`: `0ac02b5d-468d-4bf8-ac11-db0d64f96c15`
  * `token`: `Bearer token-admin-2026`
* **Body (`form-data`):**
  * **Key 1:** `file` *(Tipo: **File**)* $\rightarrow$ Seleccionar archivo `d:\ProyectoFinal_PPOOII\proyecto\Plantilla_Cargue_Trayectos_Ibague.xlsx`
  * **Key 2:** `login` *(Tipo: **Text**)* $\rightarrow$ `cg1001234567`
* **Respuesta esperada (`201 Created`):**
  ```json
  {
    "idCargue": 202610171555,
    "totalRegistros": 20,
    "exitososProcesados": 15,
    "rechazadosConError": 5,
    "pendientesValidacion": 0,
    "mensaje": "Cargue finalizado. Total: 20, Exitosos: 15, Errores: 5",
    "detalleRegistros": [ ... ]
  }
  ```

---

### **Test 60: Servicio 2 (E4 - 20% API) - Consulta de Cargues Realizados por Identificador de Cargue (`idCargue`)**
* **Requerimiento por cumplir:** Entrega 4 - Servicios Punto 2 (20%): *"Crear un servicio de consulta de los cargues realizados teniendo como parámetro el identificador de cargue"*.
* **Finalidad / Objetivo:** Consultar en cualquier momento la trazabilidad completa de un lote de cargue específico mediante su `idCargue`, verificando el estado final (`PROCESADO` o `ERROR`) y las observaciones de cada fila en `trayecto_tmp`.
* **Método:** `GET`
* **URL:** `http://localhost:9001/api/cargue-masivo/trayectos/202610171555` *(Usar el `idCargue` devuelto en el Test 59)*
* **Headers:**
  * `x-api-key`: `0ac02b5d-468d-4bf8-ac11-db0d64f96c15`
  * `token`: `Bearer token-admin-2026`
* **Respuesta esperada (`200 OK`):** Retorna el resumen estadístico y los 20 registros detallados correspondientes a ese identificador de cargue.
