package com.shah_s.bakery_order_service;

import org.springframework.boot.SpringApplication;

public class TestBakeryOrderServiceApplication {

    public static void main(String[] args) {
        SpringApplication.from(BakeryOrderServiceApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
