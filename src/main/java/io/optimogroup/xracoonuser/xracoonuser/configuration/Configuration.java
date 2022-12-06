package io.optimogroup.xracoonuser.xracoonuser.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@AutoConfiguration
public class Configuration {

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
