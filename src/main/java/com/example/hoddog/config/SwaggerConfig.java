package com.example.hoddog.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .addServersItem(new Server()
                        .url("https://YOUR-RAILWAY-APP-URL"))
                .info(new Info()
                        .title("HotDog API")
                        .version("1.0.0")
                        .description("Hotdog POS Backend API"));
    }
}
