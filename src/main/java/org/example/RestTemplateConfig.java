package org.example;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Configuration class for setting up RestTemplate beans.
 * Provides configuration for HTTP client communication within the application.
 *
 * @author Michael Harris
 * @version 1.0
 * @since 1.0
 */
@Configuration
public class RestTemplateConfig {

    /**
     * Creates and configures a RestTemplate bean for HTTP client operations.
     * This template is used for making REST API calls throughout the application.
     *
     * @return A new instance of RestTemplate configured for HTTP operations
     * @since 1.0
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
