package io.optimogroup.xracoonuser.xracoonuser;

//import io.optimogroup.xracoon.shared.s3.client.EnableS3Shared;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
//@EnableS3Shared
public class XracoonUserApplication {

    public static void main(String[] args) {
        SpringApplication.run(XracoonUserApplication.class, args);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }


}
