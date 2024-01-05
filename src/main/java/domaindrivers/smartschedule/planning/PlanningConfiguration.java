package domaindrivers.smartschedule.planning;


import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import domaindrivers.smartschedule.availability.AvailabilityFacade;
import domaindrivers.smartschedule.planning.parallelization.StageParallelization;
import domaindrivers.smartschedule.shared.EventsPublisher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Clock;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;

@Configuration
class PlanningConfiguration {

    @Bean
    PlanningFacade planningFacade(ProjectRepository projectRepository, PlanChosenResources planChosenResourcesService, EventsPublisher eventsPublisher, Clock clock) {
        return new PlanningFacade(projectRepository, new StageParallelization(), planChosenResourcesService, eventsPublisher, clock);
    }

    @Bean
    PlanChosenResources planChosenResourcesService(ProjectRepository projectRepository, AvailabilityFacade availabilityFacade, EventsPublisher eventsPublisher, Clock clock) {
        return new PlanChosenResources(projectRepository, availabilityFacade, eventsPublisher, clock);
    }

}


@Configuration
class RedisConfiguration {

    @Value("${spring.data.redis.host:localhost}")
    private String redisHostName;

    @Value("${spring.data.redis.port:6379}")
    private int redisPort;

    @Bean
    ProjectRepository projectRepository(RedisTemplate<String, Project> redisTemplate) {
        return new RedisProjectRepository(redisTemplate);
    }

    @Bean
    RedisTemplate<String, Project> redisTemplate(RedisConnectionFactory rcf, JedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Project> template = new RedisTemplate<>();
        template.setConnectionFactory(rcf);
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer();
        serializer.configure(mapper -> mapper.setVisibility(PropertyAccessor.FIELD, ANY));
        serializer.configure(mapper -> mapper.registerModule(new JavaTimeModule()));
        template.setHashValueSerializer(serializer);
        return template;
    }

    @Bean
    JedisConnectionFactory jedisConnectionFactory() {
        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration(redisHostName, redisPort);
        return new JedisConnectionFactory(redisStandaloneConfiguration);
    }
}
