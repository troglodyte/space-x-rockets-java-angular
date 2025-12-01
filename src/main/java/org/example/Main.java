package org.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;


/**
 * Main application class that serves as the entry point for the SpaceX information web application.
 * This class initializes the Spring Boot application context and starts the server.
 *
 * @since 1.0
 */
@SpringBootApplication
public class Main {
    /**
     * The main method that bootstraps the Spring Boot application.
     * Initializes the application context and prints a confirmation message when the server is running.
     *
     * @param args Command line arguments passed to the application
     */
    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(Main.class, args);
        Main main = context.getBean(Main.class);
        System.out.println("Server is up!");
    }
}
