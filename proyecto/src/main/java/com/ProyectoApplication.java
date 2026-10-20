// Paquete raíz de la aplicación
package com; // Define el paquete principal del proyecto

import org.springframework.boot.SpringApplication; // Importa la clase que arranca la aplicación Spring Boot
import org.springframework.boot.autoconfigure.SpringBootApplication; // Importa la anotación de autoconfiguración de Spring Boot
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer; // Importa el inicializador de servlets del Laboratorio 2
import org.springframework.scheduling.annotation.EnableScheduling; // Importa la anotación que habilita las tareas @Scheduled del Laboratorio 3

// Anotación principal que habilita autoconfiguración, escaneo de componentes y configuración de Spring Boot
@SpringBootApplication // Define la clase principal de configuración de Spring Boot
@EnableScheduling // Habilita la ejecución de tareas programadas (@Scheduled) según el Laboratorio 3
public class ProyectoApplication extends SpringBootServletInitializer { //Extiende SpringBootServletInitializer según el Laboratorio 2

    // Método de entrada principal que arranca el servidor y el contexto de Spring Boot
    public static void main(String[] args) { // Punto de entrada estándar de Java
        SpringApplication.run(ProyectoApplication.class, args); // Inicia el contenedor embebido Tomcat y todos los beans de Spring
    } // Fin del método main

} // Fin de la clase ProyectoApplication

