package com.portfolio.commerce;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Context smoke test.
 *
 * Flyway, Hibernate schema validation and JDBC metadata lookup are
 * disabled here so the unit test suite runs without an external
 * PostgreSQL. The real schema (Flyway migrations + ddl-auto=validate)
 * is exercised when the application starts, e.g. via Docker Compose.
 */
@SpringBootTest(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=none",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect",
        "spring.jpa.properties.hibernate.temp.use_jdbc_metadata_defaults=false"
})
class CommerceBackendApplicationTests {

	@Test
	void contextLoads() {
	}

}
