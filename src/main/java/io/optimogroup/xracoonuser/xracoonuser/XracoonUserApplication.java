package io.optimogroup.xracoonuser.xracoonuser;

import io.optimogroup.xracoon.shared.s3.client.EnableS3Shared;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableS3Shared
public class XracoonUserApplication {

    public static void main(String[] args) {
        SpringApplication.run(XracoonUserApplication.class, args);
    }

}
