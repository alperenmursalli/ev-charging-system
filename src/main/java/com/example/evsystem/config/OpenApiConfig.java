package com.example.evsystem.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "EV Charging System API",
                version = "v1",
                description = "API for stations, chargers, reservations, vehicles, and charging sessions.",
                contact = @Contact(name = "EV Charging System"),
                license = @License(name = "Internal Use")
        ),
        servers = {
                @Server(url = "/", description = "Current environment")
        }
)
public class OpenApiConfig {
}
