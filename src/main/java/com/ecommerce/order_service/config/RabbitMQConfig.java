package com.ecommerce.order_service.config;

import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;

@Configuration
public class RabbitMQConfig {
    @Bean
    public MessageConverter messageConverter(){
        return new JacksonJsonMessageConverter();
    }

}