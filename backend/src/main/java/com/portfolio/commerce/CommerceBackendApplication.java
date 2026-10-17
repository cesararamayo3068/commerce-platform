package com.portfolio.commerce;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@org.springframework.scheduling.annotation.EnableScheduling
@SpringBootApplication
public class CommerceBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(CommerceBackendApplication.class, args);
	}

}
