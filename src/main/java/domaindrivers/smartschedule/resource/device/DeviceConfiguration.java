package domaindrivers.smartschedule.resource.device;

import domaindrivers.smartschedule.allocation.capabilityscheduling.CapabilityScheduler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class DeviceConfiguration {
    @Bean
    DeviceFacade deviceFacade(DeviceRepository deviceRepository, CapabilityScheduler capabilityScheduler) {
        return new DeviceFacade(deviceRepository, new ScheduleDeviceCapabilities(deviceRepository, capabilityScheduler));
    }
}
