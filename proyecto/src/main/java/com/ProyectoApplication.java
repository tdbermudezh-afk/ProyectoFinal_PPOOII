// Paquete raíz de la aplicación
package com;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Anotación principal que habilita autoconfiguración, escaneo de componentes y configuración de Spring Boot
@SpringBootApplication
public class ProyectoApplication {

    // Método de entrada principal que arranca el servidor y el contexto de Spring Boot
    public static void main(String[] args) {
        SpringApplication.run(ProyectoApplication.class, args);
    }

}
