package domaindrivers.smartschedule;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;

@Configuration
public class TaskExecutorConfiguration {

    @Bean
    @Primary
    public TaskExecutor taskExecutor() {
        return new SyncTaskExecutor();
    }
}
