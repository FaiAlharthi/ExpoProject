package com.example.expo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class ExpoApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExpoApplication.class, args);
    }

}
