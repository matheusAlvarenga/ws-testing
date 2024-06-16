package com.alvarenga.ws_testing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class WsTestingApplication {

    public static void main(String[] args) {
        SpringApplication.run(WsTestingApplication.class, args);
    }

}
