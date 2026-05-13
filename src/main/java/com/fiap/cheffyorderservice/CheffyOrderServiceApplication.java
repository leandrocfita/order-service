package com.fiap.cheffyorderservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class CheffyOrderServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CheffyOrderServiceApplication.class, args);
    }

}
