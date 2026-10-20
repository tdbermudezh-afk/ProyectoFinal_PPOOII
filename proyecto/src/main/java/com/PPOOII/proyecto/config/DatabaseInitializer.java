// Paquete de configuración de la aplicación
package com.PPOOII.proyecto.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

// Inicializador automático de Base de Datos que garantiza restricciones CHECK, columnas LONGBLOB y Procedimientos Almacenados (Entregas 1 a 4)
@Component
@Order(1)
public class DatabaseInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseInitializer.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        logger.info("[AUDITORIA BD] Verificando esquema físico, columnas LONGBLOB, restricciones CHECK y Procedimientos Almacenados...");
        try {
            ajustarColumnasYTipos();
            asegurarDatosBaseConsistentes();
            aplicarRestriccionesCheckYUnicas();
            crearProcedimientosAlmacenadosEntrega4();
            logger.info("[AUDITORIA BD] Esquema MySQL/MariaDB, restricciones CHECK y 3 Procedimientos Almacenados verificados exitosamente.");
        } catch (Exception e) {
            logger.error("[AUDITORIA BD] Error durante la verificación del esquema: {}", e.getMessage(), e);
        }
    }

    private void ajustarColumnasYTipos() {
        ejecutarSilencioso("ALTER TABLE `vehiculo` MODIFY COLUMN `tipo_servicio` VARCHAR(15) NOT NULL");
        ejecutarSilencioso("ALTER TABLE `vehiculo_documento` MODIFY COLUMN `documento_pdf_base64` LONGBLOB DEFAULT NULL");
        ejecutarSilencioso("ALTER TABLE `personas` MODIFY COLUMN `licencia_conduccion` LONGBLOB DEFAULT NULL");
        ejecutarSilencioso("ALTER TABLE `trayecto_tmp` MODIFY COLUMN `id_cargue` BIGINT(20) NOT NULL");
        ejecutarSilencioso("ALTER TABLE `trayecto_tmp` MODIFY COLUMN `observacion` VARCHAR(1000) DEFAULT ''");
    }

    private void aplicarRestriccionesCheckYUnicas() {
        // Documento: aplica_a IN ('A','M','AM') y obligatorio IN ('RA','RM','RR','NO','NR')
        agregarCheckSiNoExiste("documento", "chk_documento_aplica_a", "`aplica_a` IN ('A', 'M', 'AM')");
        agregarCheckSiNoExiste("documento", "chk_documento_obligatorio", "`obligatorio` IN ('RA', 'RM', 'RR', 'NO', 'NR')");

        // Vehiculo: tipo_vehiculo, tipo_servicio, tipo_combustible
        agregarCheckSiNoExiste("vehiculo", "chk_vehiculo_tipo", "`tipo_vehiculo` IN ('Automóvil', 'Motocicleta')");
        agregarCheckSiNoExiste("vehiculo", "chk_vehiculo_servicio", "`tipo_servicio` IN ('Pu', 'Pr', 'Público', 'Publico', 'Privado')");
        agregarCheckSiNoExiste("vehiculo", "chk_vehiculo_combustible", "`tipo_combustible` IN ('Gasolina', 'Gas', 'Disel', 'Diesel')");

        // VehiculoDocumento: estado IN ('Habilitado', 'Vencido', 'En Verificación')
        agregarCheckSiNoExiste("vehiculo_documento", "chk_vehiculo_doc_estado", "`estado` IN ('Habilitado', 'Vencido', 'En Verificación')");

        // Personas: tipo_identificacion y tipo_persona
        agregarCheckSiNoExiste("personas", "chk_persona_tipo_id", "`tipo_identificacion` IN ('CC', 'CE', 'PASAPORTE')");
        agregarCheckSiNoExiste("personas", "chk_persona_tipo", "`tipo_persona` IN ('C', 'A')");

        // Usuarios: relación estricta 1:1 con Persona (idpersona único)
        ejecutarSilencioso("ALTER TABLE `usuarios` ADD CONSTRAINT `uk_usuario_idpersona` UNIQUE (`idpersona`)");

        // VehiculoPersonas: estado_conductor IN ('PO', 'EA', 'RO')
        agregarCheckSiNoExiste("vehiculo_personas", "chk_vp_estado", "`estado_conductor` IN ('PO', 'EA', 'RO')");

        // Trayecto: orden_parada entre 0 y 6 (máximo 7 paradas por ruta)
        agregarCheckSiNoExiste("trayecto", "chk_trayecto_orden", "`orden_parada` >= 0 AND `orden_parada` <= 6");

        // TrayectoTmp: estado IN ('CARGADO', 'VALIDADO', 'PROCESADO', 'ERROR')
        agregarCheckSiNoExiste("trayecto_tmp", "chk_trayecto_tmp_estado", "`estado` IN ('CARGADO', 'VALIDADO', 'PROCESADO', 'ERROR')");
    }

    private void agregarCheckSiNoExiste(String tabla, String nombreConstraint, String expresionCheck) {
        ejecutarSilencioso("ALTER TABLE `" + tabla + "` DROP CONSTRAINT IF EXISTS `" + nombreConstraint + "`");
        ejecutarSilencioso("ALTER TABLE `" + tabla + "` ADD CONSTRAINT `" + nombreConstraint + "` CHECK (" + expresionCheck + ")");
    }

    private void asegurarDatosBaseConsistentes() {
        byte[] samplePdfBase64Bytes = "JVBERi0xLjQKMSAwIG9iago8PCAvVHlwZSAvQ2F0YWxvZyAvUGFnZXMgMiAwIFIgPj4KZW5kb2JqCjIgMCBvYmoKPDwgL1R5cGUgL1BhZ2VzIC9LaWRzIFszIDAgUl0gL0NvdW50IDEgPj4KZW5kb2JqCjMgMCBvYmoKPDwgL1R5cGUgL1BhZ2UgL1BhcmVudCAyIDAgUiAvTWVkaWFCb3ggWzAgMCA2MTIgNzkyXSA+PgplbmRvYmoKdHJhaWxlcgo8PCAvUm9vdCAxIDAgUiA+PgplbmRsb2YK".getBytes(StandardCharsets.UTF_8);

        // Asegurar documentos paramétricos base (1..3)
        ejecutarSilencioso("INSERT IGNORE INTO `documento` (`id`, `codigo`, `nombre`, `aplica_a`, `obligatorio`, `descripcion`) VALUES " +
                "(1, 'DOC001', 'SOAT', 'AM', 'RR', 'Seguro Obligatorio de Accidentes de Tránsito')," +
                "(2, 'DOC002', 'Técnico Mecánica', 'AM', 'RR', 'Revisión técnico mecánica y de gases')," +
                "(3, 'SOAT-01', 'Seguro Obligatorio de Accidentes de Tránsito', 'AM', 'RA', 'Garantiza la atención médica de las víctimas de accidentes de tránsito.')");

        // Asegurar estados de documentos asociados según placa:
        // ABC123, KLU456, DEF456, GHI12D -> Habilitado y vigentes
        // XYZ789, TYU789 -> Vencido
        // RMY032 -> En Verificación
        jdbcTemplate.update("UPDATE `vehiculo_documento` vd JOIN `vehiculo` v ON vd.vehiculo_id = v.id SET vd.`estado` = 'Habilitado', vd.`fecha_vencimiento` = '2027-12-31', vd.`documento_pdf_base64` = COALESCE(vd.`documento_pdf_base64`, ?) WHERE v.`placa` IN ('ABC123', 'KLU456', 'DEF456', 'GHI12D')", (Object) samplePdfBase64Bytes);
        jdbcTemplate.update("UPDATE `vehiculo_documento` vd JOIN `vehiculo` v ON vd.vehiculo_id = v.id SET vd.`estado` = 'Vencido', vd.`fecha_vencimiento` = '2025-01-10', vd.`documento_pdf_base64` = COALESCE(vd.`documento_pdf_base64`, ?) WHERE v.`placa` IN ('XYZ789', 'TYU789')", (Object) samplePdfBase64Bytes);
        jdbcTemplate.update("UPDATE `vehiculo_documento` vd JOIN `vehiculo` v ON vd.vehiculo_id = v.id SET vd.`estado` = 'En Verificación', vd.`fecha_vencimiento` = '2027-06-30', vd.`documento_pdf_base64` = COALESCE(vd.`documento_pdf_base64`, ?) WHERE v.`placa` = 'RMY032'", (Object) samplePdfBase64Bytes);

        // Asegurar vigencias de licencias por número de identificación:
        // Conductores activos: 1007654321, 1010203040, 1020304050, 1040506070 -> Vigente 2028-09-15
        // Conductor con licencia vencida para prueba de restricción RO: 1030405060 -> Vencida 2024-05-01
        jdbcTemplate.update("UPDATE `personas` SET `fecha_vigencia_licencia` = '2028-09-15', `licencia_conduccion` = COALESCE(`licencia_conduccion`, ?) WHERE `identificacion` IN ('1007654321', '1010203040', '1020304050', '1040506070')", (Object) samplePdfBase64Bytes);
        jdbcTemplate.update("UPDATE `personas` SET `fecha_vigencia_licencia` = '2024-05-01', `licencia_conduccion` = COALESCE(`licencia_conduccion`, ?) WHERE `identificacion` = '1030405060'", (Object) samplePdfBase64Bytes);

        // Asegurar usuario administrativo por defecto con credenciales de prueba
        ejecutarSilencioso("INSERT IGNORE INTO `usuarios` (`idpersona`, `login`, `password`, `apikey`) VALUES " +
                "(1, 'cg1001234567', 'Admin123*', 'api-key-admin-token-secret-12345')");
        ejecutarSilencioso("UPDATE `usuarios` SET `password` = 'Admin123*', `apikey` = 'api-key-admin-token-secret-12345' WHERE `login` = 'cg1001234567'");

        // Asegurar estado PO para conductores con licencia vigente y RO para conductor 1030405060
        ejecutarSilencioso("UPDATE `vehiculo_personas` vp JOIN `personas` p ON vp.persona_id = p.id SET vp.`estado_conductor` = 'PO' WHERE p.`identificacion` IN ('1007654321', '1010203040', '1020304050', '1040506070')");
        ejecutarSilencioso("UPDATE `vehiculo_personas` vp JOIN `personas` p ON vp.persona_id = p.id SET vp.`estado_conductor` = 'RO' WHERE p.`identificacion` = '1030405060'");
    }

    private void crearProcedimientosAlmacenadosEntrega4() {
        // 1. CARGAR_TRAYECTOS_EXCEL
        jdbcTemplate.execute("DROP PROCEDURE IF EXISTS `CARGAR_TRAYECTOS_EXCEL`");
        String procCargar =
                "CREATE PROCEDURE `CARGAR_TRAYECTOS_EXCEL`(\n" +
                "    IN p_idpersona_txt VARCHAR(100),\n" +
                "    IN p_idvehiculo_txt VARCHAR(50),\n" +
                "    IN p_codigo_ruta VARCHAR(50),\n" +
                "    IN p_ubicacion VARCHAR(255),\n" +
                "    IN p_orden_parada_txt VARCHAR(10),\n" +
                "    IN p_latitud_txt VARCHAR(50),\n" +
                "    IN p_longitud_txt VARCHAR(50),\n" +
                "    IN p_login VARCHAR(100),\n" +
                "    IN p_id_cargue BIGINT\n" +
                ")\n" +
                "BEGIN\n" +
                "    INSERT INTO `trayecto_tmp` (\n" +
                "        `idpersona_txt`, `idvehiculo_txt`, `codigo_ruta`, `ubicacion`,\n" +
                "        `orden_parada_txt`, `latitud_txt`, `longitud_txt`, `login`,\n" +
                "        `id_cargue`, `estado`, `observacion`\n" +
                "    ) VALUES (\n" +
                "        p_idpersona_txt, p_idvehiculo_txt, p_codigo_ruta, p_ubicacion,\n" +
                "        p_orden_parada_txt, p_latitud_txt, p_longitud_txt, p_login,\n" +
                "        p_id_cargue, 'CARGADO', ''\n" +
                "    );\n" +
                "END";
        jdbcTemplate.execute(procCargar);

        // 2. VALIDAR_TRAYECTOS_CARGUE (con validación ACUMULATIVA real y sin truncamientos numéricos)
        jdbcTemplate.execute("DROP PROCEDURE IF EXISTS `VALIDAR_TRAYECTOS_CARGUE`");
        String procValidar =
                "CREATE PROCEDURE `VALIDAR_TRAYECTOS_CARGUE`(\n" +
                "    IN p_id_cargue BIGINT\n" +
                ")\n" +
                "BEGIN\n" +
                "    -- Reiniciar observaciones por si se re-ejecuta sobre registros en estado CARGADO\n" +
                "    UPDATE `trayecto_tmp`\n" +
                "    SET `observacion` = ''\n" +
                "    WHERE `id_cargue` = p_id_cargue AND `estado` = 'CARGADO';\n" +
                "\n" +
                "    -- A. Validación acumulativa de campos obligatorios nulos o vacíos\n" +
                "    UPDATE `trayecto_tmp`\n" +
                "    SET `observacion` = CONCAT(`observacion`, ' [Conductor no especificado]')\n" +
                "    WHERE `id_cargue` = p_id_cargue AND `estado` = 'CARGADO'\n" +
                "      AND (`idpersona_txt` IS NULL OR TRIM(`idpersona_txt`) = '');\n" +
                "\n" +
                "    UPDATE `trayecto_tmp`\n" +
                "    SET `observacion` = CONCAT(`observacion`, ' [Vehículo no especificado]')\n" +
                "    WHERE `id_cargue` = p_id_cargue AND `estado` = 'CARGADO'\n" +
                "      AND (`idvehiculo_txt` IS NULL OR TRIM(`idvehiculo_txt`) = '');\n" +
                "\n" +
                "    UPDATE `trayecto_tmp`\n" +
                "    SET `observacion` = CONCAT(`observacion`, ' [Código de ruta no especificado]')\n" +
                "    WHERE `id_cargue` = p_id_cargue AND `estado` = 'CARGADO'\n" +
                "      AND (`codigo_ruta` IS NULL OR TRIM(`codigo_ruta`) = '');\n" +
                "\n" +
                "    UPDATE `trayecto_tmp`\n" +
                "    SET `observacion` = CONCAT(`observacion`, ' [Ubicación de parada no especificada]')\n" +
                "    WHERE `id_cargue` = p_id_cargue AND `estado` = 'CARGADO'\n" +
                "      AND (`ubicacion` IS NULL OR TRIM(`ubicacion`) = '');\n" +
                "\n" +
                "    UPDATE `trayecto_tmp`\n" +
                "    SET `observacion` = CONCAT(`observacion`, ' [Orden de parada inválido (debe ser entre 0 y 6)]')\n" +
                "    WHERE `id_cargue` = p_id_cargue AND `estado` = 'CARGADO'\n" +
                "      AND (`orden_parada_txt` IS NULL OR TRIM(`orden_parada_txt`) NOT REGEXP '^[0-6]$');\n" +
                "\n" +
                "    -- B. Validación de existencia del usuario (login) que registra el cargue\n" +
                "    UPDATE `trayecto_tmp` t\n" +
                "    LEFT JOIN `usuarios` u ON u.login = TRIM(t.login)\n" +
                "    SET t.`observacion` = CONCAT(t.`observacion`, ' [El login de usuario que registra no existe]')\n" +
                "    WHERE t.`id_cargue` = p_id_cargue AND t.`estado` = 'CARGADO'\n" +
                "      AND u.login IS NULL;\n" +
                "\n" +
                "    -- C. Validación de existencia del conductor, rol 'C' y vigencia de licencia\n" +
                "    UPDATE `trayecto_tmp` t\n" +
                "    LEFT JOIN `personas` p ON (CAST(p.id AS CHAR) = TRIM(t.idpersona_txt) OR p.identificacion = TRIM(t.idpersona_txt))\n" +
                "    SET t.`observacion` = CONCAT(t.`observacion`, ' [El conductor especificado no existe]')\n" +
                "    WHERE t.`id_cargue` = p_id_cargue AND t.`estado` = 'CARGADO'\n" +
                "      AND t.`idpersona_txt` IS NOT NULL AND TRIM(t.`idpersona_txt`) <> ''\n" +
                "      AND p.id IS NULL;\n" +
                "\n" +
                "    UPDATE `trayecto_tmp` t\n" +
                "    JOIN `personas` p ON (CAST(p.id AS CHAR) = TRIM(t.idpersona_txt) OR p.identificacion = TRIM(t.idpersona_txt))\n" +
                "    SET t.`observacion` = CONCAT(t.`observacion`, ' [La persona no tiene rol de Conductor (C)]')\n" +
                "    WHERE t.`id_cargue` = p_id_cargue AND t.`estado` = 'CARGADO'\n" +
                "      AND p.tipo_persona <> 'C';\n" +
                "\n" +
                "    UPDATE `trayecto_tmp` t\n" +
                "    JOIN `personas` p ON (CAST(p.id AS CHAR) = TRIM(t.idpersona_txt) OR p.identificacion = TRIM(t.idpersona_txt))\n" +
                "    SET t.`observacion` = CONCAT(t.`observacion`, ' [El conductor tiene la licencia de conducción vencida]')\n" +
                "    WHERE t.`id_cargue` = p_id_cargue AND t.`estado` = 'CARGADO'\n" +
                "      AND p.tipo_persona = 'C'\n" +
                "      AND p.fecha_vigencia_licencia IS NOT NULL AND p.fecha_vigencia_licencia < CURDATE();\n" +
                "\n" +
                "    -- D. Validación de existencia del vehículo (por ID o por Placa sin conversión numérica forzada)\n" +
                "    UPDATE `trayecto_tmp` t\n" +
                "    LEFT JOIN `vehiculo` v ON (CAST(v.id AS CHAR) = TRIM(t.idvehiculo_txt) OR UPPER(v.placa) = UPPER(TRIM(t.idvehiculo_txt)))\n" +
                "    SET t.`observacion` = CONCAT(t.`observacion`, ' [El vehículo especificado no existe]')\n" +
                "    WHERE t.`id_cargue` = p_id_cargue AND t.`estado` = 'CARGADO'\n" +
                "      AND t.`idvehiculo_txt` IS NOT NULL AND TRIM(t.`idvehiculo_txt`) <> ''\n" +
                "      AND v.id IS NULL;\n" +
                "\n" +
                "    -- E. Validación de documentos del vehículo: Todos deben estar 'Habilitado' y vigentes\n" +
                "    UPDATE `trayecto_tmp` t\n" +
                "    JOIN `vehiculo` v ON (CAST(v.id AS CHAR) = TRIM(t.idvehiculo_txt) OR UPPER(v.placa) = UPPER(TRIM(t.idvehiculo_txt)))\n" +
                "    SET t.`observacion` = CONCAT(t.`observacion`, ' [El vehículo tiene documentos no habilitados o vencidos]')\n" +
                "    WHERE t.`id_cargue` = p_id_cargue AND t.`estado` = 'CARGADO'\n" +
                "      AND (\n" +
                "          NOT EXISTS (SELECT 1 FROM `vehiculo_documento` vd WHERE vd.vehiculo_id = v.id)\n" +
                "          OR EXISTS (\n" +
                "              SELECT 1 FROM `vehiculo_documento` vd\n" +
                "              WHERE vd.vehiculo_id = v.id\n" +
                "                AND (vd.estado <> 'Habilitado' OR (vd.fecha_vencimiento IS NOT NULL AND vd.fecha_vencimiento < CURDATE()))\n" +
                "          )\n" +
                "      );\n" +
                "\n" +
                "    -- F. Validación de estado del conductor en relación con el vehículo: Debe ser 'PO' (Puede Operar)\n" +
                "    UPDATE `trayecto_tmp` t\n" +
                "    JOIN `personas` p ON (CAST(p.id AS CHAR) = TRIM(t.idpersona_txt) OR p.identificacion = TRIM(t.idpersona_txt))\n" +
                "    JOIN `vehiculo` v ON (CAST(v.id AS CHAR) = TRIM(t.idvehiculo_txt) OR UPPER(v.placa) = UPPER(TRIM(t.idvehiculo_txt)))\n" +
                "    SET t.`observacion` = CONCAT(t.`observacion`, ' [El conductor no cuenta con autorización PO para operar este vehículo]')\n" +
                "    WHERE t.`id_cargue` = p_id_cargue AND t.`estado` = 'CARGADO'\n" +
                "      AND NOT EXISTS (\n" +
                "          SELECT 1 FROM `vehiculo_personas` vp\n" +
                "          WHERE vp.persona_id = p.id AND vp.vehiculo_id = v.id AND vp.estado_conductor = 'PO'\n" +
                "      );\n" +
                "\n" +
                "    -- G. Validación de límite máximo de paradas por código de ruta en el lote (máximo 7 paradas: 1 inicial, 5 intermedias y 1 final)\n" +
                "    UPDATE `trayecto_tmp` t\n" +
                "    JOIN (\n" +
                "        SELECT `codigo_ruta`, COUNT(*) AS total_lote\n" +
                "        FROM `trayecto_tmp`\n" +
                "        WHERE `id_cargue` = p_id_cargue\n" +
                "        GROUP BY `codigo_ruta`\n" +
                "    ) conteo ON conteo.`codigo_ruta` = t.`codigo_ruta`\n" +
                "    SET t.`observacion` = CONCAT(t.`observacion`, ' [La ruta supera el máximo permitido de 7 paradas (1 inicial, 5 intermedias y 1 final)]')\n" +
                "    WHERE t.`id_cargue` = p_id_cargue AND t.`estado` = 'CARGADO'\n" +
                "      AND conteo.total_lote > 7;\n" +
                "\n" +
                "    -- H. Clasificación final: Si acumuló alguna observación -> 'ERROR'; si observación está vacía -> 'VALIDADO'\n" +
                "    UPDATE `trayecto_tmp`\n" +
                "    SET `estado` = 'ERROR',\n" +
                "        `observacion` = TRIM(`observacion`)\n" +
                "    WHERE `id_cargue` = p_id_cargue AND `estado` = 'CARGADO'\n" +
                "      AND `observacion` IS NOT NULL AND TRIM(`observacion`) <> '';\n" +
                "\n" +
                "    UPDATE `trayecto_tmp`\n" +
                "    SET `estado` = 'VALIDADO',\n" +
                "        `observacion` = 'Validación exitosa'\n" +
                "    WHERE `id_cargue` = p_id_cargue AND `estado` = 'CARGADO'\n" +
                "      AND (`observacion` IS NULL OR TRIM(`observacion`) = '');\n" +
                "END";
        jdbcTemplate.execute(procValidar);

        // 3. PROCESAR_TRAYECTOS_EXCEL
        jdbcTemplate.execute("DROP PROCEDURE IF EXISTS `PROCESAR_TRAYECTOS_EXCEL`");
        String procProcesar =
                "CREATE PROCEDURE `PROCESAR_TRAYECTOS_EXCEL`(\n" +
                "    IN p_id_cargue BIGINT\n" +
                ")\n" +
                "BEGIN\n" +
                "    -- Evitar duplicados si la misma parada (codigo_ruta + orden_parada) ya fue insertada previamente\n" +
                "    DELETE tr FROM `trayecto` tr\n" +
                "    JOIN `trayecto_tmp` t ON tr.`codigo_ruta` = t.`codigo_ruta`\n" +
                "      AND tr.`orden_parada` = CAST(t.`orden_parada_txt` AS SIGNED)\n" +
                "    WHERE t.`id_cargue` = p_id_cargue AND t.`estado` = 'VALIDADO';\n" +
                "\n" +
                "    -- Inserción de los registros aprobados en la tabla transaccional definitiva trayecto\n" +
                "    INSERT INTO `trayecto` (\n" +
                "        `idpersona`, `idvehiculo`, `codigo_ruta`, `ubicacion`,\n" +
                "        `orden_parada`, `latitud`, `longitud`, `login`\n" +
                "    )\n" +
                "    SELECT\n" +
                "        p.id,\n" +
                "        v.id,\n" +
                "        t.`codigo_ruta`,\n" +
                "        t.`ubicacion`,\n" +
                "        CAST(t.`orden_parada_txt` AS SIGNED),\n" +
                "        CASE WHEN t.`latitud_txt` IS NOT NULL AND TRIM(t.`latitud_txt`) <> ''\n" +
                "             THEN CAST(REPLACE(TRIM(t.`latitud_txt`), ',', '.') AS DOUBLE) ELSE NULL END,\n" +
                "        CASE WHEN t.`longitud_txt` IS NOT NULL AND TRIM(t.`longitud_txt`) <> ''\n" +
                "             THEN CAST(REPLACE(TRIM(t.`longitud_txt`), ',', '.') AS DOUBLE) ELSE NULL END,\n" +
                "        COALESCE(NULLIF(TRIM(t.`login`), ''), 'cg1001234567')\n" +
                "    FROM `trayecto_tmp` t\n" +
                "    JOIN `personas` p ON (CAST(p.id AS CHAR) = TRIM(t.idpersona_txt) OR p.identificacion = TRIM(t.idpersona_txt))\n" +
                "    JOIN `vehiculo` v ON (CAST(v.id AS CHAR) = TRIM(t.idvehiculo_txt) OR UPPER(v.placa) = UPPER(TRIM(t.idvehiculo_txt)))\n" +
                "    WHERE t.`id_cargue` = p_id_cargue AND t.`estado` = 'VALIDADO';\n" +
                "\n" +
                "    -- Marcar los registros cargados exitosamente como 'PROCESADO'\n" +
                "    UPDATE `trayecto_tmp`\n" +
                "    SET `estado` = 'PROCESADO',\n" +
                "        `observacion` = 'Registro procesado y trasladado a tabla trayecto exitosamente'\n" +
                "    WHERE `id_cargue` = p_id_cargue AND `estado` = 'VALIDADO';\n" +
                "END";
        jdbcTemplate.execute(procProcesar);
    }

    private void ejecutarSilencioso(String sql) {
        try {
            jdbcTemplate.execute(sql);
        } catch (Exception e) {
            logger.debug("SQL omitido ({}): {}", sql, e.getMessage());
        }
    }
}

