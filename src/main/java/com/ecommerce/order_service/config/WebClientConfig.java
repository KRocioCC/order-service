package com.ecommerce.order_service.config;

import com.ecommerce.order_service.service.client.InventoryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class WebClientConfig {

    //Sabe como conectarse a internet,hacer peticiones HTTP, y manejar las respuestas
    // Es una herramienta para la comunicacion entre servicios
    @Bean
    public WebClient webClientBuilder() {
        return WebClient.builder()
                .baseUrl("http://localhost:8082") // URL base de inventary-service
                .build();

    }

    @Bean
    public InventoryClient inventoryClient(WebClient webClient){

        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(WebClientAdapter.create(webClient)).build();
        return factory.createClient(InventoryClient.class);
    }
}
