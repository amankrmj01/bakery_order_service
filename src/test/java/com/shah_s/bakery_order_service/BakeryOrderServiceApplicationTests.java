package com.shah_s.bakery_order_service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class BakeryOrderServiceApplicationTests {

    @Test
    void contextLoads() {
    }

}
