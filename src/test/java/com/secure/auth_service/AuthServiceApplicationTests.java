package com.secure.auth_service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
class AuthServiceApplicationTests {

//	@Test
//	void contextLoads() {
//		assertDoesNotThrow(() -> SpringApplication.run(AuthServiceApplication.class));
//	}

	@Test
	void mainMethodExecutesWithoutErrors() {
		String[] args = {};
		assertDoesNotThrow(() -> AuthServiceApplication.main(args));
	}
}
