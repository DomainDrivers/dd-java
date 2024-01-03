package domaindrivers.smartschedule.resource.employee;


import domaindrivers.smartschedule.shared.capability.Capability;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class EmployeeFacade {

    private final EmployeeRepository employeeRepository;

    public EmployeeFacade(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    public EmployeeSummary findEmployee(EmployeeId employeeId) {
        return employeeRepository.findSummary(employeeId);
    }

    public List<Capability> findAllCapabilities() {
        return employeeRepository.findAllCapabilities();
    }

    public EmployeeId addEmployee(String name, String lastName, Seniority seniority, Set<Capability> skills, Set<Capability> permissions) {
        EmployeeId employeeId = EmployeeId.newOne();
        Set<Capability> capabilities = Stream.concat(skills.stream(), permissions.stream()).collect(Collectors.toSet());
        Employee employee = new Employee(employeeId, name, lastName, seniority, capabilities);
        return employeeRepository.save(employee).id();
    }

    //add vacation
        // calls availability
    //add sick leave
        // calls availability
    //change skills

}
