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

COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
