package domaindrivers.smartschedule.resource;

import domaindrivers.smartschedule.resource.device.DeviceFacade;
import domaindrivers.smartschedule.resource.employee.EmployeeFacade;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class ResourceConfiguration {

    @Bean
    ResourceFacade resourceFacade(EmployeeFacade employeeFacade, DeviceFacade deviceFacade) {
        return new ResourceFacade(employeeFacade, deviceFacade);
    }

}
