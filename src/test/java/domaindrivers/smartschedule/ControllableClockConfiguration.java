package domaindrivers.smartschedule;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.time.Instant;
import java.time.ZoneOffset;

@TestConfiguration(proxyBeanMethods = false)
public class ControllableClockConfiguration {

    @Bean
    @Primary
    public MutableClock testClock() {
        return new MutableClock(Instant.now(), ZoneOffset.UTC);
    }
}
