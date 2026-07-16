package domaindrivers.smartschedule;

import domaindrivers.smartschedule.planning.PlanningDbTestConfiguration;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.testcontainers.containers.PostgreSQLContainer;

import javax.sql.DataSource;

@TestConfiguration(proxyBeanMethods = false)
@Import(PlanningDbTestConfiguration.class)
public class TestDbConfiguration {

    @Bean
    @ServiceConnection
    public PostgreSQLContainer postgreSQLContainer() {
        return new PostgreSQLContainer("postgres:15-alpine");
    }

    @Bean
    JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    public DataSource dataSource(PostgreSQLContainer postgres) {
        DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();
        dataSourceBuilder.username(postgres.getUsername());
        dataSourceBuilder.password(postgres.getPassword());
        dataSourceBuilder.driverClassName(postgres.getDriverClassName());
        dataSourceBuilder.url(postgres.getJdbcUrl());
        return dataSourceBuilder.build();
    }

    // OutboxRelay is a globally scanned @Scheduled component, so every @SpringBootTest
    // needs the outbox table regardless of which module-specific @Sql scripts it runs.
    @Bean
    InitializingBean outboxSchemaInitializer(DataSource dataSource) {
        return () -> new ResourceDatabasePopulator(new ClassPathResource("schema-outbox.sql")).execute(dataSource);
    }

}
