package com.erez.ticketbot.inventoryservice.config;


import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI inventoryServiceAPI() {
        Info in = new Info().title("Inventory Service API").description("Inventory Service API By Erez Cohen").version("v1.0.0");
        return new OpenAPI().info(in);

    }
}
