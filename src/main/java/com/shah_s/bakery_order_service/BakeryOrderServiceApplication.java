package com.shah_s.bakery_order_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class BakeryOrderServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(BakeryOrderServiceApplication.class, args);
    }

}
