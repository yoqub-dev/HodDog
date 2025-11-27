package com.example.hoddog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class HodDogApplication {

    public static void main(String[] args) {
        SpringApplication.run(HodDogApplication.class, args);
        System.out.println("HodDog Application Started");
    }

}
