package domaindrivers.smartschedule.resource.employee;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class EmployeeConfiguration {

    @Bean
    EmployeeFacade employeeFacade(EmployeeRepository employeeRepository) {
        return new EmployeeFacade(employeeRepository);
    }

}
