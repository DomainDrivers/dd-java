package domaindrivers.smartschedule.resource.employee;


import domaindrivers.smartschedule.allocation.capabilityscheduling.CapabilityScheduler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class EmployeeConfiguration {

    @Bean
    EmployeeFacade employeeFacade(EmployeeRepository employeeRepository, CapabilityScheduler capabilityScheduler) {
        return new EmployeeFacade(employeeRepository, new ScheduleEmployeeCapabilities(employeeRepository, capabilityScheduler));
    }

}
