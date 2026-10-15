// Paquete de configuración de la aplicación
package com.PPOOII.proyecto.config;

// Importaciones de Spring para configuración de tareas asíncronas y programadas
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

// Anotación de clase de configuración Spring
@Configuration
// Habilitación global de tareas programadas (@Scheduled) en el contexto de Spring Boot
@EnableScheduling
public class SchedulerConfig implements SchedulingConfigurer {

    // Tamaño del pool de hilos para ejecución concurrente de las tareas programadas
    private static final int POOL_SIZE = 10;

    // Configuración del registrador de tareas programadas con pool de hilos
    @Override
    public void configureTasks(ScheduledTaskRegistrar scheduledTaskRegistrar) {
        // Creación del programador de tareas basado en hilos concurrentes
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        // Definición de la cantidad máxima de hilos paralelos
        threadPoolTaskScheduler.setPoolSize(POOL_SIZE);
        // Prefijo asignado a cada hilo para trazabilidad en logs
        threadPoolTaskScheduler.setThreadNamePrefix("task-scheduler-pool-");
        // Inicialización del scheduler
        threadPoolTaskScheduler.initialize();

        // Asignación del scheduler personalizado al registrador de Spring
        scheduledTaskRegistrar.setTaskScheduler(threadPoolTaskScheduler);
    }
}

