package domaindrivers.smartschedule.availability;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class AvailabilityConfiguration {

    @Bean
    AvailabilityFacade availabilityFacade(JdbcTemplate jdbcTemplate) {
        return new AvailabilityFacade(new ResourceAvailabilityRepository(jdbcTemplate), new ResourceAvailabilityReadModel(jdbcTemplate));
    }
}
