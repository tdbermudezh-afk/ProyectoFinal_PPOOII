-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Servidor: localhost
-- Tiempo de generación: 05-09-2026 a las 20:52:08
-- Versión del servidor: 10.4.28-MariaDB
-- Versión de PHP: 8.2.4

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Base de datos: `PPOOII_Proyecto`
--

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `documento`
--

CREATE TABLE `documento` (
  `id` int(11) NOT NULL,
  `codigo` varchar(20) NOT NULL,
  `nombre` varchar(100) NOT NULL,
  `aplica_a` varchar(2) NOT NULL,
  `obligatorio` varchar(2) NOT NULL,
  `descripcion` text DEFAULT NULL
) ;

--
-- Volcado de datos para la tabla `documento`
--

INSERT INTO `documento` (`id`, `codigo`, `nombre`, `aplica_a`, `obligatorio`, `descripcion`) VALUES
(1, 'DOC001', 'SOAT', 'AM', 'RR', 'Seguro Obligatorio de Accidentes de Tránsito'),
(2, 'DOC002', 'Técnico Mecánica', 'AM', 'RR', 'Revisión técnico mecánica y de gases'),
(3, 'SOAT-01', 'Seguro Obligatorio de Accidentes de Tránsito', 'AM', 'RA', 'Garantiza la atención médica de las víctimas de accidentes de tránsito.');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `vehiculo`
--

CREATE TABLE `vehiculo` (
  `id` int(11) NOT NULL,
  `tipo_vehiculo` varchar(20) NOT NULL,
  `placa` varchar(6) NOT NULL,
  `tipo_servicio` varchar(2) NOT NULL,
  `tipo_combustible` varchar(20) NOT NULL,
  `capacidad_pasajeros` int(11) NOT NULL,
  `color` varchar(7) NOT NULL,
  `modelo` int(11) NOT NULL,
  `marca` varchar(50) NOT NULL,
  `linea` varchar(50) NOT NULL
) ;

--
-- Volcado de datos para la tabla `vehiculo`
--

INSERT INTO `vehiculo` (`id`, `tipo_vehiculo`, `placa`, `tipo_servicio`, `tipo_combustible`, `capacidad_pasajeros`, `color`, `modelo`, `marca`, `linea`) VALUES
(1, 'Automóvil', 'ABC123', 'Pr', 'Gasolina', 5, '#FF0000', 2022, 'Mazda', 'Mazda 3');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `vehiculo_documento`
--

CREATE TABLE `vehiculo_documento` (
  `id` int(11) NOT NULL,
  `vehiculo_id` int(11) NOT NULL,
  `documento_id` int(11) NOT NULL,
  `fecha_expedicion` date NOT NULL,
  `fecha_vencimiento` date NOT NULL,
  `estado` varchar(20) NOT NULL,
  `documento_pdf_base64` longtext DEFAULT NULL
) ;

--
-- Volcado de datos para la tabla `vehiculo_documento`
--

INSERT INTO `vehiculo_documento` (`id`, `vehiculo_id`, `documento_id`, `fecha_expedicion`, `fecha_vencimiento`, `estado`) VALUES
(1, 1, 3, '2026-01-15', '2027-01-15', 'Habilitado');

--
-- Índices para tablas volcadas
--

--
-- Indices de la tabla `documento`
--
ALTER TABLE `documento`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `codigo` (`codigo`);

--
-- Indices de la tabla `vehiculo`
--
ALTER TABLE `vehiculo`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `placa` (`placa`);

--
-- Indices de la tabla `vehiculo_documento`
--
ALTER TABLE `vehiculo_documento`
  ADD PRIMARY KEY (`id`),
  ADD KEY `vehiculo_id` (`vehiculo_id`),
  ADD KEY `documento_id` (`documento_id`);

--
-- AUTO_INCREMENT de las tablas volcadas
--

--
-- AUTO_INCREMENT de la tabla `documento`
--
ALTER TABLE `documento`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de la tabla `vehiculo`
--
ALTER TABLE `vehiculo`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de la tabla `vehiculo_documento`
--
ALTER TABLE `vehiculo_documento`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- Restricciones para tablas volcadas
--

--
-- Filtros y restricciones para las tablas
--
ALTER TABLE `documento`
  ADD CONSTRAINT `chk_documento_aplica_a` CHECK (`aplica_a` IN ('A', 'M', 'AM')),
  ADD CONSTRAINT `chk_documento_obligatorio` CHECK (`obligatorio` IN ('RA', 'RM', 'RR'));

ALTER TABLE `vehiculo`
  ADD CONSTRAINT `chk_vehiculo_tipo` CHECK (`tipo_vehiculo` IN ('Automóvil', 'Motocicleta')),
  ADD CONSTRAINT `chk_vehiculo_servicio` CHECK (`tipo_servicio` IN ('Pu', 'Pr')),
  ADD CONSTRAINT `chk_vehiculo_combustible` CHECK (`tipo_combustible` IN ('Gasolina', 'Gas', 'Disel', 'Diesel'));

ALTER TABLE `vehiculo_documento`
  ADD CONSTRAINT `vehiculo_documento_ibfk_1` FOREIGN KEY (`vehiculo_id`) REFERENCES `vehiculo` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `vehiculo_documento_ibfk_2` FOREIGN KEY (`documento_id`) REFERENCES `documento` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `chk_vehiculo_doc_estado` CHECK (`estado` IN ('Habilitado', 'Vencido', 'En Verificación'));

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `personas`
--

CREATE TABLE `personas` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `identificacion` varchar(50) NOT NULL,
  `tipo_identificacion` varchar(20) NOT NULL,
  `nombres` varchar(100) NOT NULL,
  `apellidos` varchar(100) NOT NULL,
  `correo` varchar(150) NOT NULL,
  `tipo_persona` varchar(1) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_persona_identificacion` (`identificacion`),
  UNIQUE KEY `uk_persona_correo` (`correo`),
  CONSTRAINT `chk_persona_tipo_id` CHECK (`tipo_identificacion` IN ('CC', 'CE', 'PASAPORTE')),
  CONSTRAINT `chk_persona_tipo` CHECK (`tipo_persona` IN ('C', 'A'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `personas`
--

INSERT INTO `personas` (`id`, `identificacion`, `tipo_identificacion`, `nombres`, `apellidos`, `correo`, `tipo_persona`) VALUES
(1, '1001234567', 'CC', 'Carlos', 'Gomez', 'carlos.admin@example.com', 'A'),
(2, '1007654321', 'CC', 'Pedro', 'Ramirez', 'pedro.conductor@example.com', 'C');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `usuarios`
--

CREATE TABLE `usuarios` (
  `idpersona` bigint(20) NOT NULL,
  `login` varchar(100) NOT NULL,
  `password` varchar(255) NOT NULL,
  `apikey` varchar(255) NOT NULL,
  PRIMARY KEY (`idpersona`, `login`),
  UNIQUE KEY `uk_usuario_apikey` (`apikey`),
  CONSTRAINT `fk_usuario_persona` FOREIGN KEY (`idpersona`) REFERENCES `personas` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `usuarios`
--

INSERT INTO `usuarios` (`idpersona`, `login`, `password`, `apikey`) VALUES
(1, 'cg1001234567', 'Admin123*', 'api-key-admin-token-secret-12345');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `vehiculo_personas`
--

CREATE TABLE `vehiculo_personas` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `vehiculo_id` int(11) NOT NULL,
  `persona_id` bigint(20) NOT NULL,
  `fecha_asociacion` date NOT NULL,
  `estado_conductor` varchar(2) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_vp_vehiculo` (`vehiculo_id`),
  KEY `fk_vp_persona` (`persona_id`),
  CONSTRAINT `fk_vp_vehiculo` FOREIGN KEY (`vehiculo_id`) REFERENCES `vehiculo` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_vp_persona` FOREIGN KEY (`persona_id`) REFERENCES `personas` (`id`) ON DELETE CASCADE,
  CONSTRAINT `chk_vp_estado` CHECK (`estado_conductor` IN ('PO', 'EA', 'RO'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `vehiculo_personas`
--

INSERT INTO `vehiculo_personas` (`id`, `vehiculo_id`, `persona_id`, `fecha_asociacion`, `estado_conductor`) VALUES
(1, 1, 2, '2026-02-01', 'PO');

-- --------------------------------------------------------
-- ENTREGA 3: Adición de campos en la entidad 'personas' para conductores
-- --------------------------------------------------------

-- Adición del campo para almacenar el documento de licencia en formato BASE64
ALTER TABLE `personas` ADD COLUMN `licencia_conduccion` LONGTEXT NULL COMMENT 'Documento de licencia en formato Base64';

-- Adición del campo para la fecha de vigencia de la licencia de conducción
ALTER TABLE `personas` ADD COLUMN `fecha_vigencia_licencia` DATE NULL COMMENT 'Fecha de vigencia de la licencia';

-- Actualización de datos de prueba para el conductor existente (Pedro Ramírez, id: 2)
UPDATE `personas` SET `fecha_vigencia_licencia` = '2027-12-31' WHERE `id` = 2;

-- --------------------------------------------------------
-- ENTREGA 3: Estructura de tabla para la entidad 'trayecto'
-- --------------------------------------------------------

-- Creación de la tabla trayecto que almacena las paradas de cada ruta
CREATE TABLE `trayecto` (
  -- Identificador único autoincremental de cada trayecto/parada
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  -- Clave foránea referenciando a la persona que realiza el recorrido (debe ser conductor 'C')
  `idpersona` bigint(20) NOT NULL,
  -- Clave foránea referenciando al vehículo asignado al recorrido
  `idvehiculo` int(11) NOT NULL,
  -- Código identificador de la ruta que agrupa los trayectos (entre 2 y 7 paradas)
  `codigo_ruta` varchar(50) NOT NULL,
  -- Nombre o descripción textual del punto de parada
  `ubicacion` varchar(255) NOT NULL,
  -- Orden de la parada: 0 = inicial, número mayor = final, valores intermedios = paradas intermedias
  `orden_parada` int(11) NOT NULL,
  -- Coordenada de latitud geográfica obtenida vía Google Maps
  `latitud` double DEFAULT NULL,
  -- Coordenada de longitud geográfica obtenida vía Google Maps
  `longitud` double DEFAULT NULL,
  -- Login del usuario que registra el trayecto para fines de trazabilidad
  `login` varchar(100) NOT NULL,
  -- Definición de clave primaria
  PRIMARY KEY (`id`),
  -- Índices para optimizar las consultas por persona, vehículo y código de ruta
  KEY `fk_trayecto_persona` (`idpersona`),
  KEY `fk_trayecto_vehiculo` (`idvehiculo`),
  KEY `idx_trayecto_codigo_ruta` (`codigo_ruta`),
  -- Restricción de integridad referencial con eliminación en cascada hacia personas
  CONSTRAINT `fk_trayecto_persona` FOREIGN KEY (`idpersona`) REFERENCES `personas` (`id`) ON DELETE CASCADE,
  -- Restricción de integridad referencial con eliminación en cascada hacia vehiculo
  CONSTRAINT `fk_trayecto_vehiculo` FOREIGN KEY (`idvehiculo`) REFERENCES `vehiculo` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------
-- Volcado de datos de prueba para la tabla 'trayecto' (Ruta RUTA-IBG-01)
-- --------------------------------------------------------

-- Parada 0: Parada inicial en el Conservatorio del Tolima
INSERT INTO `trayecto` (`id`, `idpersona`, `idvehiculo`, `codigo_ruta`, `ubicacion`, `orden_parada`, `latitud`, `longitud`, `login`) VALUES
(1, 2, 1, 'RUTA-IBG-01', 'Conservatorio del Tolima, Ibagué, Tolima', 0, 4.43265, -75.23412, 'cg1001234567'),
-- Parada 1: Parada intermedia en Centro Comercial La Estación
(2, 2, 1, 'RUTA-IBG-01', 'Centro Comercial La Estacion, Ibagué, Tolima', 1, 4.43956, -75.20875, 'cg1001234567'),
-- Parada 2: Parada final en Acqua Power Center
(3, 2, 1, 'RUTA-IBG-01', 'Acqua Power Center, Ibagué, Tolima', 2, 4.43710, -75.20450, 'cg1001234567');

-- ----------------------------------------------------------------------------------------------------
-- ENTREGA 4: Creación de la tabla temporal 'trayecto_tmp' para el cargue masivo desde Excel
-- ----------------------------------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS `trayecto_tmp` (
  -- Identificador único autoincremental de la tabla temporal
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  -- Identificación o ID del conductor diligenciado en el archivo Excel (como texto para flexibilidad)
  `idpersona_txt` varchar(100) DEFAULT NULL,
  -- Placa o ID del vehículo diligenciado en el archivo Excel
  `idvehiculo_txt` varchar(50) DEFAULT NULL,
  -- Código de ruta que agrupa las paradas
  `codigo_ruta` varchar(50) DEFAULT NULL,
  -- Nombre o texto descriptivo de la parada o ubicación
  `ubicacion` varchar(255) DEFAULT NULL,
  -- Orden de parada indicado en el Excel (0 = inicial, mayor = final)
  `orden_parada_txt` varchar(10) DEFAULT NULL,
  -- Latitud geográfica (opcional en el Excel)
  `latitud_txt` varchar(50) DEFAULT NULL,
  -- Longitud geográfica (opcional en el Excel)
  `longitud_txt` varchar(50) DEFAULT NULL,
  -- Login del usuario que realiza el cargue masivo
  `login` varchar(100) DEFAULT NULL,
  -- Identificador numérico del cargue generado con la fecha y hora: yyyyMMddHHmm
  `id_cargue` double NOT NULL,
  -- Estado del registro en el ciclo de cargue masivo: CARGADO, VALIDADO, PROCESADO o ERROR
  `estado` varchar(15) NOT NULL DEFAULT 'CARGADO',
  -- Descripción acumulativa de observaciones o inconsistencias detectadas
  `observacion` varchar(1000) DEFAULT '',
  -- Definición de clave primaria
  PRIMARY KEY (`id`),
  -- Índice para búsquedas por lote de cargue
  KEY `idx_trayecto_tmp_idcargue` (`id_cargue`),
  -- Restricción de chequeo sobre los estados permitidos
  CONSTRAINT `chk_trayecto_tmp_estado` CHECK (`estado` IN ('CARGADO', 'VALIDADO', 'PROCESADO', 'ERROR'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------------------------------------------------------------------------------
-- PROCEDIMIENTOS ALMACENADOS PARA EL CARGUE MASIVO (ENTREGA 4)
-- ----------------------------------------------------------------------------------------------------

-- 1. PROCEDIMIENTO: CARGAR_TRAYECTOS_EXCEL
-- Inserta registro por registro en trayecto_tmp con estado inicial 'CARGADO'
DELIMITER $$
DROP PROCEDURE IF EXISTS `CARGAR_TRAYECTOS_EXCEL`$$
CREATE PROCEDURE `CARGAR_TRAYECTOS_EXCEL`(
    IN p_idpersona_txt VARCHAR(100),
    IN p_idvehiculo_txt VARCHAR(50),
    IN p_codigo_ruta VARCHAR(50),
    IN p_ubicacion VARCHAR(255),
    IN p_orden_parada_txt VARCHAR(10),
    IN p_latitud_txt VARCHAR(50),
    IN p_longitud_txt VARCHAR(50),
    IN p_login VARCHAR(100),
    IN p_id_cargue DOUBLE
)
BEGIN
    INSERT INTO `trayecto_tmp` (
        `idpersona_txt`, `idvehiculo_txt`, `codigo_ruta`, `ubicacion`, 
        `orden_parada_txt`, `latitud_txt`, `longitud_txt`, `login`, 
        `id_cargue`, `estado`, `observacion`
    ) VALUES (
        p_idpersona_txt, p_idvehiculo_txt, p_codigo_ruta, p_ubicacion, 
        p_orden_parada_txt, p_latitud_txt, p_longitud_txt, p_login, 
        p_id_cargue, 'CARGADO', ''
    );
END$$

-- 2. PROCEDIMIENTO: VALIDAR_TRAYECTOS_CARGUE
-- Aplica las validaciones de negocio acumulativas y clasifica en 'VALIDADO' o 'ERROR'
DROP PROCEDURE IF EXISTS `VALIDAR_TRAYECTOS_CARGUE`$$
CREATE PROCEDURE `VALIDAR_TRAYECTOS_CARGUE`(
    IN p_id_cargue DOUBLE
)
BEGIN
    -- A. Validación de campos obligatorios nulos o vacíos
    UPDATE `trayecto_tmp`
    SET `estado` = 'ERROR',
        `observacion` = CONCAT(`observacion`, ' [Conductor no especificado]')
    WHERE `id_cargue` = p_id_cargue AND `estado` = 'CARGADO'
      AND (`idpersona_txt` IS NULL OR TRIM(`idpersona_txt`) = '');

    UPDATE `trayecto_tmp`
    SET `estado` = 'ERROR',
        `observacion` = CONCAT(`observacion`, ' [Vehículo no especificado]')
    WHERE `id_cargue` = p_id_cargue AND `estado` = 'CARGADO'
      AND (`idvehiculo_txt` IS NULL OR TRIM(`idvehiculo_txt`) = '');

    UPDATE `trayecto_tmp`
    SET `estado` = 'ERROR',
        `observacion` = CONCAT(`observacion`, ' [Código de ruta no especificado]')
    WHERE `id_cargue` = p_id_cargue AND `estado` = 'CARGADO'
      AND (`codigo_ruta` IS NULL OR TRIM(`codigo_ruta`) = '');

    UPDATE `trayecto_tmp`
    SET `estado` = 'ERROR',
        `observacion` = CONCAT(`observacion`, ' [Ubicación de parada no especificada]')
    WHERE `id_cargue` = p_id_cargue AND `estado` = 'CARGADO'
      AND (`ubicacion` IS NULL OR TRIM(`ubicacion`) = '');

    -- B. Validación de existencia del conductor y verificación de rol CONDUCTOR ('C')
    UPDATE `trayecto_tmp` t
    LEFT JOIN `personas` p ON (p.id = CONVERT(t.idpersona_txt, SIGNED INTEGER) OR p.identificacion = t.idpersona_txt)
    SET t.`estado` = 'ERROR',
        t.`observacion` = CONCAT(t.`observacion`, ' [El conductor especificado no existe]')
    WHERE t.`id_cargue` = p_id_cargue AND t.`estado` = 'CARGADO'
      AND p.id IS NULL;

    UPDATE `trayecto_tmp` t
    JOIN `personas` p ON (p.id = CONVERT(t.idpersona_txt, SIGNED INTEGER) OR p.identificacion = t.idpersona_txt)
    SET t.`estado` = 'ERROR',
        t.`observacion` = CONCAT(t.`observacion`, ' [La persona no tiene rol de Conductor (C)]')
    WHERE t.`id_cargue` = p_id_cargue AND t.`estado` = 'CARGADO'
      AND p.tipo_persona <> 'C';

    -- C. Validación de existencia del vehículo (por ID o por Placa)
    UPDATE `trayecto_tmp` t
    LEFT JOIN `vehiculo` v ON (v.id = CONVERT(t.idvehiculo_txt, SIGNED INTEGER) OR v.placa = t.idvehiculo_txt)
    SET t.`estado` = 'ERROR',
        t.`observacion` = CONCAT(t.`observacion`, ' [El vehículo especificado no existe]')
    WHERE t.`id_cargue` = p_id_cargue AND t.`estado` = 'CARGADO'
      AND v.id IS NULL;

    -- D. Validación de documentos del vehículo: Todos deben estar 'Habilitado'
    UPDATE `trayecto_tmp` t
    JOIN `vehiculo` v ON (v.id = CONVERT(t.idvehiculo_txt, SIGNED INTEGER) OR v.placa = t.idvehiculo_txt)
    SET t.`estado` = 'ERROR',
        t.`observacion` = CONCAT(t.`observacion`, ' [El vehículo tiene documentos no habilitados o vencidos]')
    WHERE t.`id_cargue` = p_id_cargue AND t.`estado` = 'CARGADO'
      AND (
          NOT EXISTS (SELECT 1 FROM `vehiculo_documento` vd WHERE vd.vehiculo_id = v.id)
          OR EXISTS (SELECT 1 FROM `vehiculo_documento` vd WHERE vd.vehiculo_id = v.id AND vd.estado <> 'Habilitado')
      );

    -- E. Validación de estado del conductor en relación con el vehículo: Debe ser 'PO' (Puede Operar)
    UPDATE `trayecto_tmp` t
    JOIN `personas` p ON (p.id = CONVERT(t.idpersona_txt, SIGNED INTEGER) OR p.identificacion = t.idpersona_txt)
    JOIN `vehiculo` v ON (v.id = CONVERT(t.idvehiculo_txt, SIGNED INTEGER) OR v.placa = t.idvehiculo_txt)
    SET t.`estado` = 'ERROR',
        t.`observacion` = CONCAT(t.`observacion`, ' [El conductor no cuenta con autorización PO para operar este vehículo]')
    WHERE t.`id_cargue` = p_id_cargue AND t.`estado` = 'CARGADO'
      AND NOT EXISTS (
          SELECT 1 FROM `vehiculo_personas` vp 
          WHERE vp.persona_id = p.id AND vp.vehiculo_id = v.id AND vp.estado_conductor = 'PO'
      );

    -- F. Si superó todas las validaciones (observación vacía), marcar como 'VALIDADO'
    UPDATE `trayecto_tmp`
    SET `estado` = 'VALIDADO'
    WHERE `id_cargue` = p_id_cargue AND `estado` = 'CARGADO'
      AND (`observacion` IS NULL OR TRIM(`observacion`) = '');

END$$

-- 3. PROCEDIMIENTO: PROCESAR_TRAYECTOS_EXCEL
-- Traslada registros 'VALIDADO' a la tabla transaccional definitiva 'trayecto' y marca 'PROCESADO'
DROP PROCEDURE IF EXISTS `PROCESAR_TRAYECTOS_EXCEL`$$
CREATE PROCEDURE `PROCESAR_TRAYECTOS_EXCEL`(
    IN p_id_cargue DOUBLE
)
BEGIN
    -- Inserción de los registros aprobados en la tabla transaccional definitiva trayecto
    INSERT INTO `trayecto` (
        `idpersona`, `idvehiculo`, `codigo_ruta`, `ubicacion`, 
        `orden_parada`, `latitud`, `longitud`, `login`
    )
    SELECT 
        p.id,
        v.id,
        t.`codigo_ruta`,
        t.`ubicacion`,
        COALESCE(CONVERT(t.`orden_parada_txt`, SIGNED INTEGER), 0),
        CASE WHEN t.`latitud_txt` IS NOT NULL AND t.`latitud_txt` <> '' 
             THEN CONVERT(REPLACE(t.`latitud_txt`, ',', '.'), DOUBLE) ELSE NULL END,
        CASE WHEN t.`longitud_txt` IS NOT NULL AND t.`longitud_txt` <> '' 
             THEN CONVERT(REPLACE(t.`longitud_txt`, ',', '.'), DOUBLE) ELSE NULL END,
        COALESCE(t.`login`, 'SISTEMA')
    FROM `trayecto_tmp` t
    JOIN `personas` p ON (p.id = CONVERT(t.idpersona_txt, SIGNED INTEGER) OR p.identificacion = t.idpersona_txt)
    JOIN `vehiculo` v ON (v.id = CONVERT(t.idvehiculo_txt, SIGNED INTEGER) OR v.placa = t.idvehiculo_txt)
    WHERE t.`id_cargue` = p_id_cargue AND t.`estado` = 'VALIDADO';

    -- Marcar los registros cargados exitosamente como 'PROCESADO'
    UPDATE `trayecto_tmp`
    SET `estado` = 'PROCESADO'
    WHERE `id_cargue` = p_id_cargue AND `estado` = 'VALIDADO';

END$$
DELIMITER ;

COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;


