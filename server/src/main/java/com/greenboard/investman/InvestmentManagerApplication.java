package com.greenboard.investman;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
@EntityScan(basePackages = "com.greenboard.investman.model")
public class InvestmentManagerApplication {

    public static void main(String[] args) {
        SpringApplication.run(InvestmentManagerApplication.class, args);
    }
}
