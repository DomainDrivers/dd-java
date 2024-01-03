package domaindrivers.smartschedule.resource.employee;


import domaindrivers.smartschedule.shared.capability.Capability;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;



interface EmployeeRepository extends JpaRepository<Employee, EmployeeId> {

    default EmployeeSummary findSummary(EmployeeId employeeId) {
        Employee employee = this.findById(employeeId).orElseThrow();
        Set<Capability> skills = filterCapabilities(employee.capabilities(), cap -> cap.isOfType("SKILL"));
        Set<Capability> permissions = filterCapabilities(employee.capabilities(), cap -> cap.isOfType("PERMISSION"));
        return new EmployeeSummary(employeeId, employee.name(), employee.lastName(), employee.seniority(), skills, permissions);
    }

    default List<Capability> findAllCapabilities() {
        return this.findAll().stream().flatMap(employee -> employee.capabilities().stream()).collect(Collectors.toList());
    }

    private Set<Capability> filterCapabilities(Set<Capability> capabilities, Predicate<Capability> p) {
        return capabilities.stream().filter(p).collect(Collectors.toSet());
    }

}