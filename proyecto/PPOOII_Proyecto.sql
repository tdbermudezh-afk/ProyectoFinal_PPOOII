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
  `estado` varchar(20) NOT NULL
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
-- Filtros para la tabla `vehiculo_documento`
--
ALTER TABLE `vehiculo_documento`
  ADD CONSTRAINT `vehiculo_documento_ibfk_1` FOREIGN KEY (`vehiculo_id`) REFERENCES `vehiculo` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `vehiculo_documento_ibfk_2` FOREIGN KEY (`documento_id`) REFERENCES `documento` (`id`) ON DELETE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
