-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Servidor: localhost
-- Versión del servidor: 10.4.32-MariaDB
-- Base de datos: `PPOOII_Proyecto`

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

-- --------------------------------------------------------
-- ENTREGA 1: Estructura de tabla para la tabla `documento`
-- --------------------------------------------------------

CREATE TABLE IF NOT EXISTS `documento` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `codigo` varchar(20) NOT NULL,
  `nombre` varchar(100) NOT NULL,
  `aplica_a` varchar(2) NOT NULL,
  `obligatorio` varchar(2) NOT NULL,
  `descripcion` text DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `codigo` (`codigo`),
  CONSTRAINT `chk_documento_aplica_a` CHECK (`aplica_a` IN ('A', 'M', 'AM')),
  CONSTRAINT `chk_documento_obligatorio` CHECK (`obligatorio` IN ('RA', 'RM', 'RR', 'NO', 'NR'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

INSERT IGNORE INTO `documento` (`id`, `codigo`, `nombre`, `aplica_a`, `obligatorio`, `descripcion`) VALUES
(1, 'DOC001', 'SOAT', 'AM', 'RR', 'Seguro Obligatorio de Accidentes de Tránsito'),
(2, 'DOC002', 'Técnico Mecánica', 'AM', 'RR', 'Revisión técnico mecánica y de gases'),
(3, 'SOAT-01', 'Seguro Obligatorio de Accidentes de Tránsito', 'AM', 'RA', 'Garantiza la atención médica de las víctimas de accidentes de tránsito.');

-- --------------------------------------------------------
-- ENTREGA 1: Estructura de tabla para la tabla `vehiculo`
-- --------------------------------------------------------

CREATE TABLE IF NOT EXISTS `vehiculo` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `tipo_vehiculo` varchar(20) NOT NULL,
  `placa` varchar(6) NOT NULL,
  `tipo_servicio` varchar(15) NOT NULL,
  `tipo_combustible` varchar(20) NOT NULL,
  `capacidad_pasajeros` int(11) NOT NULL,
  `color` varchar(7) NOT NULL,
  `modelo` int(11) NOT NULL,
  `marca` varchar(50) NOT NULL,
  `linea` varchar(50) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `placa` (`placa`),
  CONSTRAINT `chk_vehiculo_tipo` CHECK (`tipo_vehiculo` IN ('Automóvil', 'Motocicleta')),
  CONSTRAINT `chk_vehiculo_servicio` CHECK (`tipo_servicio` IN ('Pu', 'Pr', 'Público', 'Publico', 'Privado')),
  CONSTRAINT `chk_vehiculo_combustible` CHECK (`tipo_combustible` IN ('Gasolina', 'Gas', 'Disel', 'Diesel'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

INSERT IGNORE INTO `vehiculo` (`id`, `tipo_vehiculo`, `placa`, `tipo_servicio`, `tipo_combustible`, `capacidad_pasajeros`, `color`, `modelo`, `marca`, `linea`) VALUES
(1, 'Automóvil', 'ABC123', 'Pr', 'Gasolina', 5, '#FF0000', 2022, 'Mazda', 'Mazda 3'),
(2, 'Automóvil', 'XYZ789', 'Pu', 'Gasolina', 5, '#1E90FF', 2021, 'Chevrolet', 'Tracker'),
(3, 'Automóvil', 'DEF456', 'Pu', 'Diesel', 7, '#2E8B57', 2023, 'Toyota', 'Fortuner SW'),
(4, 'Motocicleta', 'GHI12D', 'Pr', 'Gasolina', 2, '#FF5733', 2024, 'Yamaha', 'FZ25'),
(5, 'Automóvil', 'TYU789', 'Pu', 'Gas', 5, '#FFD700', 2019, 'Hyundai', 'Accent'),
(6, 'Automóvil', 'RMY032', 'Pr', 'Gasolina', 5, '#4B0082', 2022, 'Renault', 'Duster');

-- --------------------------------------------------------
-- ENTREGA 1 y 2: Estructura de tabla para la tabla `vehiculo_documento`
-- Incluye campo LONGBLOB para almacenar el archivo PDF en formato BASE64
-- --------------------------------------------------------

CREATE TABLE IF NOT EXISTS `vehiculo_documento` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `vehiculo_id` int(11) NOT NULL,
  `documento_id` int(11) NOT NULL,
  `fecha_expedicion` date NOT NULL,
  `fecha_vencimiento` date NOT NULL,
  `estado` varchar(20) NOT NULL,
  `documento_pdf_base64` longblob DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `vehiculo_id` (`vehiculo_id`),
  KEY `documento_id` (`documento_id`),
  CONSTRAINT `vehiculo_documento_ibfk_1` FOREIGN KEY (`vehiculo_id`) REFERENCES `vehiculo` (`id`) ON DELETE CASCADE,
  CONSTRAINT `vehiculo_documento_ibfk_2` FOREIGN KEY (`documento_id`) REFERENCES `documento` (`id`) ON DELETE CASCADE,
  CONSTRAINT `chk_vehiculo_doc_estado` CHECK (`estado` IN ('Habilitado', 'Vencido', 'En Verificación'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

INSERT IGNORE INTO `vehiculo_documento` (`id`, `vehiculo_id`, `documento_id`, `fecha_expedicion`, `fecha_vencimiento`, `estado`, `documento_pdf_base64`) VALUES
(1, 1, 1, '2026-01-15', '2027-12-31', 'Habilitado', 'JVBERi0xLjQKMSAwIG9iago8PCAvVHlwZSAvQ2F0YWxvZyAvUGFnZXMgMiAwIFIgPj4KZW5kb2JqCg=='),
(2, 2, 1, '2024-01-10', '2025-01-10', 'Vencido', 'JVBERi0xLjQKMSAwIG9iago8PCAvVHlwZSAvQ2F0YWxvZyAvUGFnZXMgMiAwIFIgPj4KZW5kb2JqCg=='),
(3, 3, 1, '2026-02-01', '2027-12-31', 'Habilitado', 'JVBERi0xLjQKMSAwIG9iago8PCAvVHlwZSAvQ2F0YWxvZyAvUGFnZXMgMiAwIFIgPj4KZW5kb2JqCg=='),
(4, 4, 1, '2026-03-01', '2027-12-31', 'Habilitado', 'JVBERi0xLjQKMSAwIG9iago8PCAvVHlwZSAvQ2F0YWxvZyAvUGFnZXMgMiAwIFIgPj4KZW5kb2JqCg=='),
(5, 5, 1, '2024-01-01', '2025-01-01', 'Vencido', 'JVBERi0xLjQKMSAwIG9iago8PCAvVHlwZSAvQ2F0YWxvZyAvUGFnZXMgMiAwIFIgPj4KZW5kb2JqCg=='),
(6, 6, 1, '2026-06-01', '2027-06-30', 'En Verificación', 'JVBERi0xLjQKMSAwIG9iago8PCAvVHlwZSAvQ2F0YWxvZyAvUGFnZXMgMiAwIFIgPj4KZW5kb2JqCg==');

-- --------------------------------------------------------
-- ENTREGA 2 y 3: Estructura de tabla para la tabla `personas`
-- Incluye licencia_conduccion tipo LONGBLOB para almacenar el PDF en BASE64
-- --------------------------------------------------------

CREATE TABLE IF NOT EXISTS `personas` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `identificacion` varchar(50) NOT NULL,
  `tipo_identificacion` varchar(20) NOT NULL,
  `nombres` varchar(100) NOT NULL,
  `apellidos` varchar(100) NOT NULL,
  `correo` varchar(150) NOT NULL,
  `tipo_persona` varchar(1) NOT NULL,
  `licencia_conduccion` longblob DEFAULT NULL COMMENT 'Documento de licencia en formato Base64 almacenado en LONGBLOB',
  `fecha_vigencia_licencia` date DEFAULT NULL COMMENT 'Fecha de vigencia de la licencia',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_persona_identificacion` (`identificacion`),
  UNIQUE KEY `uk_persona_correo` (`correo`),
  CONSTRAINT `chk_persona_tipo_id` CHECK (`tipo_identificacion` IN ('CC', 'CE', 'PASAPORTE')),
  CONSTRAINT `chk_persona_tipo` CHECK (`tipo_persona` IN ('C', 'A'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

INSERT IGNORE INTO `personas` (`id`, `identificacion`, `tipo_identificacion`, `nombres`, `apellidos`, `correo`, `tipo_persona`, `licencia_conduccion`, `fecha_vigencia_licencia`) VALUES
(1, '1001234567', 'CC', 'Carlos', 'Gomez', 'carlos.admin@example.com', 'A', NULL, NULL),
(2, '1007654321', 'CC', 'Pedro', 'Ramirez', 'pedro.conductor@example.com', 'C', 'JVBERi0xLjQKMSAwIG9iago8PCAvVHlwZSAvQ2F0YWxvZyAvUGFnZXMgMiAwIFIgPj4KZW5kb2JqCg==', '2028-09-15'),
(3, '1010203040', 'CC', 'Andres', 'Castro', 'andres.castro@ibague.co', 'C', 'JVBERi0xLjQKMSAwIG9iago8PCAvVHlwZSAvQ2F0YWxvZyAvUGFnZXMgMiAwIFIgPj4KZW5kb2JqCg==', '2028-09-15'),
(4, '1020304050', 'CC', 'Camilo', 'Vargas', 'camilo.vargas@ibague.co', 'C', 'JVBERi0xLjQKMSAwIG9iago8PCAvVHlwZSAvQ2F0YWxvZyAvUGFnZXMgMiAwIFIgPj4KZW5kb2JqCg==', '2028-09-15'),
(5, '1040506070', 'CC', 'Luisa', 'Morales', 'luisa.morales@ibague.co', 'C', 'JVBERi0xLjQKMSAwIG9iago8PCAvVHlwZSAvQ2F0YWxvZyAvUGFnZXMgMiAwIFIgPj4KZW5kb2JqCg==', '2028-09-15'),
(6, '1030405060', 'CC', 'Jorge', 'Mendoza', 'jorge.mendoza@ibague.co', 'C', 'JVBERi0xLjQKMSAwIG9iago8PCAvVHlwZSAvQ2F0YWxvZyAvUGFnZXMgMiAwIFIgPj4KZW5kb2JqCg==', '2024-05-01');

-- --------------------------------------------------------
-- ENTREGA 2: Estructura de tabla para la tabla `usuarios`
-- Clave Primaria Compuesta (idpersona, login) + UNIQUE en idpersona (1:1)
-- --------------------------------------------------------

CREATE TABLE IF NOT EXISTS `usuarios` (
  `idpersona` bigint(20) NOT NULL,
  `login` varchar(100) NOT NULL,
  `password` varchar(255) NOT NULL,
  `apikey` varchar(255) NOT NULL,
  PRIMARY KEY (`idpersona`, `login`),
  UNIQUE KEY `uk_usuario_idpersona` (`idpersona`),
  UNIQUE KEY `uk_usuario_login` (`login`),
  UNIQUE KEY `uk_usuario_apikey` (`apikey`),
  CONSTRAINT `fk_usuario_persona` FOREIGN KEY (`idpersona`) REFERENCES `personas` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

INSERT IGNORE INTO `usuarios` (`idpersona`, `login`, `password`, `apikey`) VALUES
(1, 'cg1001234567', 'Admin123*', 'api-key-admin-token-secret-12345');

-- --------------------------------------------------------
-- ENTREGA 2: Estructura de tabla para la tabla `vehiculo_personas`
-- --------------------------------------------------------

CREATE TABLE IF NOT EXISTS `vehiculo_personas` (
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

INSERT IGNORE INTO `vehiculo_personas` (`id`, `vehiculo_id`, `persona_id`, `fecha_asociacion`, `estado_conductor`) VALUES
(1, 1, 2, '2026-02-01', 'PO'),
(2, 1, 3, '2026-02-15', 'PO'),
(3, 4, 3, '2026-02-15', 'PO'),
(4, 6, 3, '2026-02-15', 'PO'),
(5, 2, 4, '2026-02-20', 'PO'),
(6, 3, 5, '2026-03-01', 'PO'),
(7, 5, 6, '2025-01-10', 'RO');

-- --------------------------------------------------------
-- ENTREGA 3: Estructura de tabla para la entidad `trayecto`
-- --------------------------------------------------------

CREATE TABLE IF NOT EXISTS `trayecto` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `idpersona` bigint(20) NOT NULL,
  `idvehiculo` int(11) NOT NULL,
  `codigo_ruta` varchar(50) NOT NULL,
  `ubicacion` varchar(255) NOT NULL,
  `orden_parada` int(11) NOT NULL,
  `latitud` double DEFAULT NULL,
  `longitud` double DEFAULT NULL,
  `login` varchar(100) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_trayecto_persona` (`idpersona`),
  KEY `fk_trayecto_vehiculo` (`idvehiculo`),
  KEY `idx_trayecto_codigo_ruta` (`codigo_ruta`),
  CONSTRAINT `fk_trayecto_persona` FOREIGN KEY (`idpersona`) REFERENCES `personas` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_trayecto_vehiculo` FOREIGN KEY (`idvehiculo`) REFERENCES `vehiculo` (`id`) ON DELETE CASCADE,
  CONSTRAINT `chk_trayecto_orden` CHECK (`orden_parada` >= 0 AND `orden_parada` <= 6)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

INSERT IGNORE INTO `trayecto` (`id`, `idpersona`, `idvehiculo`, `codigo_ruta`, `ubicacion`, `orden_parada`, `latitud`, `longitud`, `login`) VALUES
(1, 2, 1, 'RUTA-IBG-01', 'Conservatorio del Tolima, Ibagué, Tolima', 0, 4.43265, -75.23412, 'cg1001234567'),
(2, 2, 1, 'RUTA-IBG-01', 'Centro Comercial La Estacion, Ibagué, Tolima', 1, 4.43956, -75.20875, 'cg1001234567'),
(3, 2, 1, 'RUTA-IBG-01', 'Acqua Power Center, Ibagué, Tolima', 2, 4.43710, -75.20450, 'cg1001234567');

-- ----------------------------------------------------------------------------------------------------
-- ENTREGA 4: Creación de la tabla temporal `trayecto_tmp` para el cargue masivo desde Excel
-- ----------------------------------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS `trayecto_tmp` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `idpersona_txt` varchar(100) DEFAULT NULL,
  `idvehiculo_txt` varchar(50) DEFAULT NULL,
  `codigo_ruta` varchar(50) DEFAULT NULL,
  `ubicacion` varchar(255) DEFAULT NULL,
  `orden_parada_txt` varchar(10) DEFAULT NULL,
  `latitud_txt` varchar(50) DEFAULT NULL,
  `longitud_txt` varchar(50) DEFAULT NULL,
  `login` varchar(100) DEFAULT NULL,
  `id_cargue` bigint(20) NOT NULL,
  `estado` varchar(15) NOT NULL DEFAULT 'CARGADO',
  `observacion` varchar(1000) DEFAULT '',
  PRIMARY KEY (`id`),
  KEY `idx_trayecto_tmp_idcargue` (`id_cargue`),
  CONSTRAINT `chk_trayecto_tmp_estado` CHECK (`estado` IN ('CARGADO', 'VALIDADO', 'PROCESADO', 'ERROR'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------------------------------------------------------------------------------
-- PROCEDIMIENTOS ALMACENADOS PARA EL CARGUE MASIVO (ENTREGA 4)
-- ----------------------------------------------------------------------------------------------------

-- 1. PROCEDIMIENTO: CARGAR_TRAYECTOS_EXCEL
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
    IN p_id_cargue BIGINT
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

-- 2. PROCEDIMIENTO: VALIDAR_TRAYECTOS_CARGUE (Validación ACUMULATIVA real y completa)
DROP PROCEDURE IF EXISTS `VALIDAR_TRAYECTOS_CARGUE`$$
CREATE PROCEDURE `VALIDAR_TRAYECTOS_CARGUE`(
    IN p_id_cargue BIGINT
)
BEGIN
    -- Reiniciar observaciones por si se re-ejecuta sobre registros en estado CARGADO
    UPDATE `trayecto_tmp`
    SET `observacion` = ''
    WHERE `id_cargue` = p_id_cargue AND `estado` = 'CARGADO';

    -- A. Validación acumulativa de campos obligatorios nulos o vacíos
    UPDATE `trayecto_tmp`
    SET `observacion` = CONCAT(`observacion`, ' [Conductor no especificado]')
    WHERE `id_cargue` = p_id_cargue AND `estado` = 'CARGADO'
      AND (`idpersona_txt` IS NULL OR TRIM(`idpersona_txt`) = '');

    UPDATE `trayecto_tmp`
    SET `observacion` = CONCAT(`observacion`, ' [Vehículo no especificado]')
    WHERE `id_cargue` = p_id_cargue AND `estado` = 'CARGADO'
      AND (`idvehiculo_txt` IS NULL OR TRIM(`idvehiculo_txt`) = '');

    UPDATE `trayecto_tmp`
    SET `observacion` = CONCAT(`observacion`, ' [Código de ruta no especificado]')
    WHERE `id_cargue` = p_id_cargue AND `estado` = 'CARGADO'
      AND (`codigo_ruta` IS NULL OR TRIM(`codigo_ruta`) = '');

    UPDATE `trayecto_tmp`
    SET `observacion` = CONCAT(`observacion`, ' [Ubicación de parada no especificada]')
    WHERE `id_cargue` = p_id_cargue AND `estado` = 'CARGADO'
      AND (`ubicacion` IS NULL OR TRIM(`ubicacion`) = '');

    UPDATE `trayecto_tmp`
    SET `observacion` = CONCAT(`observacion`, ' [Orden de parada inválido (debe ser entre 0 y 6)]')
    WHERE `id_cargue` = p_id_cargue AND `estado` = 'CARGADO'
      AND (`orden_parada_txt` IS NULL OR TRIM(`orden_parada_txt`) NOT REGEXP '^[0-6]$');

    -- B. Validación de existencia del usuario (login) que registra el cargue
    UPDATE `trayecto_tmp` t
    LEFT JOIN `usuarios` u ON u.login = TRIM(t.login)
    SET t.`observacion` = CONCAT(t.`observacion`, ' [El login de usuario que registra no existe]')
    WHERE t.`id_cargue` = p_id_cargue AND t.`estado` = 'CARGADO'
      AND u.login IS NULL;

    -- C. Validación de existencia del conductor, rol 'C' y vigencia de licencia
    UPDATE `trayecto_tmp` t
    LEFT JOIN `personas` p ON (CAST(p.id AS CHAR) = TRIM(t.idpersona_txt) OR p.identificacion = TRIM(t.idpersona_txt))
    SET t.`observacion` = CONCAT(t.`observacion`, ' [El conductor especificado no existe]')
    WHERE t.`id_cargue` = p_id_cargue AND t.`estado` = 'CARGADO'
      AND t.`idpersona_txt` IS NOT NULL AND TRIM(t.`idpersona_txt`) <> ''
      AND p.id IS NULL;

    UPDATE `trayecto_tmp` t
    JOIN `personas` p ON (CAST(p.id AS CHAR) = TRIM(t.idpersona_txt) OR p.identificacion = TRIM(t.idpersona_txt))
    SET t.`observacion` = CONCAT(t.`observacion`, ' [La persona no tiene rol de Conductor (C)]')
    WHERE t.`id_cargue` = p_id_cargue AND t.`estado` = 'CARGADO'
      AND p.tipo_persona <> 'C';

    UPDATE `trayecto_tmp` t
    JOIN `personas` p ON (CAST(p.id AS CHAR) = TRIM(t.idpersona_txt) OR p.identificacion = TRIM(t.idpersona_txt))
    SET t.`observacion` = CONCAT(t.`observacion`, ' [El conductor tiene la licencia de conducción vencida]')
    WHERE t.`id_cargue` = p_id_cargue AND t.`estado` = 'CARGADO'
      AND p.tipo_persona = 'C'
      AND p.fecha_vigencia_licencia IS NOT NULL AND p.fecha_vigencia_licencia < CURDATE();

    -- D. Validación de existencia del vehículo (por ID o por Placa)
    UPDATE `trayecto_tmp` t
    LEFT JOIN `vehiculo` v ON (CAST(v.id AS CHAR) = TRIM(t.idvehiculo_txt) OR UPPER(v.placa) = UPPER(TRIM(t.idvehiculo_txt)))
    SET t.`observacion` = CONCAT(t.`observacion`, ' [El vehículo especificado no existe]')
    WHERE t.`id_cargue` = p_id_cargue AND t.`estado` = 'CARGADO'
      AND t.`idvehiculo_txt` IS NOT NULL AND TRIM(t.`idvehiculo_txt`) <> ''
      AND v.id IS NULL;

    -- E. Validación de documentos del vehículo: Todos deben estar 'Habilitado' y vigentes
    UPDATE `trayecto_tmp` t
    JOIN `vehiculo` v ON (CAST(v.id AS CHAR) = TRIM(t.idvehiculo_txt) OR UPPER(v.placa) = UPPER(TRIM(t.idvehiculo_txt)))
    SET t.`observacion` = CONCAT(t.`observacion`, ' [El vehículo tiene documentos no habilitados o vencidos]')
    WHERE t.`id_cargue` = p_id_cargue AND t.`estado` = 'CARGADO'
      AND (
          NOT EXISTS (SELECT 1 FROM `vehiculo_documento` vd WHERE vd.vehiculo_id = v.id)
          OR EXISTS (
              SELECT 1 FROM `vehiculo_documento` vd
              WHERE vd.vehiculo_id = v.id
                AND (vd.estado <> 'Habilitado' OR (vd.fecha_vencimiento IS NOT NULL AND vd.fecha_vencimiento < CURDATE()))
          )
      );

    -- F. Validación de estado del conductor en relación con el vehículo: Debe ser 'PO' (Puede Operar)
    UPDATE `trayecto_tmp` t
    JOIN `personas` p ON (CAST(p.id AS CHAR) = TRIM(t.idpersona_txt) OR p.identificacion = TRIM(t.idpersona_txt))
    JOIN `vehiculo` v ON (CAST(v.id AS CHAR) = TRIM(t.idvehiculo_txt) OR UPPER(v.placa) = UPPER(TRIM(t.idvehiculo_txt)))
    SET t.`observacion` = CONCAT(t.`observacion`, ' [El conductor no cuenta con autorización PO para operar este vehículo]')
    WHERE t.`id_cargue` = p_id_cargue AND t.`estado` = 'CARGADO'
      AND NOT EXISTS (
          SELECT 1 FROM `vehiculo_personas` vp
          WHERE vp.persona_id = p.id AND vp.vehiculo_id = v.id AND vp.estado_conductor = 'PO'
      );

    -- G. Validación de límite máximo de paradas por código de ruta en el lote (máximo 7 paradas: 1 inicial, 5 intermedias y 1 final)
    UPDATE `trayecto_tmp` t
    JOIN (
        SELECT `codigo_ruta`, COUNT(*) AS total_lote
        FROM `trayecto_tmp`
        WHERE `id_cargue` = p_id_cargue
        GROUP BY `codigo_ruta`
    ) conteo ON conteo.`codigo_ruta` = t.`codigo_ruta`
    SET t.`observacion` = CONCAT(t.`observacion`, ' [La ruta supera el máximo permitido de 7 paradas (1 inicial, 5 intermedias y 1 final)]')
    WHERE t.`id_cargue` = p_id_cargue AND t.`estado` = 'CARGADO'
      AND conteo.total_lote > 7;

    -- H. Clasificación final: Si acumuló alguna observación -> 'ERROR'; si observación está vacía -> 'VALIDADO'
    UPDATE `trayecto_tmp`
    SET `estado` = 'ERROR',
        `observacion` = TRIM(`observacion`)
    WHERE `id_cargue` = p_id_cargue AND `estado` = 'CARGADO'
      AND `observacion` IS NOT NULL AND TRIM(`observacion`) <> '';

    UPDATE `trayecto_tmp`
    SET `estado` = 'VALIDADO',
        `observacion` = 'Validación exitosa'
    WHERE `id_cargue` = p_id_cargue AND `estado` = 'CARGADO'
      AND (`observacion` IS NULL OR TRIM(`observacion`) = '');
END$$

-- 3. PROCEDIMIENTO: PROCESAR_TRAYECTOS_EXCEL
DROP PROCEDURE IF EXISTS `PROCESAR_TRAYECTOS_EXCEL`$$
CREATE PROCEDURE `PROCESAR_TRAYECTOS_EXCEL`(
    IN p_id_cargue BIGINT
)
BEGIN
    -- Evitar duplicados si la misma parada (codigo_ruta + orden_parada) ya fue insertada previamente
    DELETE tr FROM `trayecto` tr
    JOIN `trayecto_tmp` t ON tr.`codigo_ruta` = t.`codigo_ruta`
      AND tr.`orden_parada` = CAST(t.`orden_parada_txt` AS SIGNED)
    WHERE t.`id_cargue` = p_id_cargue AND t.`estado` = 'VALIDADO';

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
        CAST(t.`orden_parada_txt` AS SIGNED),
        CASE WHEN t.`latitud_txt` IS NOT NULL AND TRIM(t.`latitud_txt`) <> ''
             THEN CAST(REPLACE(TRIM(t.`latitud_txt`), ',', '.') AS DOUBLE) ELSE NULL END,
        CASE WHEN t.`longitud_txt` IS NOT NULL AND TRIM(t.`longitud_txt`) <> ''
             THEN CAST(REPLACE(TRIM(t.`longitud_txt`), ',', '.') AS DOUBLE) ELSE NULL END,
        COALESCE(NULLIF(TRIM(t.`login`), ''), 'cg1001234567')
    FROM `trayecto_tmp` t
    JOIN `personas` p ON (CAST(p.id AS CHAR) = TRIM(t.idpersona_txt) OR p.identificacion = TRIM(t.idpersona_txt))
    JOIN `vehiculo` v ON (CAST(v.id AS CHAR) = TRIM(t.idvehiculo_txt) OR UPPER(v.placa) = UPPER(TRIM(t.idvehiculo_txt)))
    WHERE t.`id_cargue` = p_id_cargue AND t.`estado` = 'VALIDADO';

    -- Marcar los registros cargados exitosamente como 'PROCESADO'
    UPDATE `trayecto_tmp`
    SET `estado` = 'PROCESADO',
        `observacion` = 'Registro procesado y trasladado a tabla trayecto exitosamente'
    WHERE `id_cargue` = p_id_cargue AND `estado` = 'VALIDADO';
END$$
DELIMITER ;

COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
