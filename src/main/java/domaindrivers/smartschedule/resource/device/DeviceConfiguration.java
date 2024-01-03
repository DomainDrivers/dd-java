package domaindrivers.smartschedule.resource.device;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class DeviceConfiguration {
    @Bean
    DeviceFacade deviceFacade(DeviceRepository deviceRepository) {
        return new DeviceFacade(deviceRepository);
    }
}
