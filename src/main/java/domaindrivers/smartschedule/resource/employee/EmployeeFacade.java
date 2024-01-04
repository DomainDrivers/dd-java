package domaindrivers.smartschedule.resource.employee;


import domaindrivers.smartschedule.allocation.capabilityscheduling.AllocatableCapabilityId;
import domaindrivers.smartschedule.allocation.capabilityscheduling.CapabilityScheduler;
import domaindrivers.smartschedule.shared.capability.Capability;
import domaindrivers.smartschedule.shared.timeslot.TimeSlot;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class EmployeeFacade {

    private final EmployeeRepository employeeRepository;
    private final ScheduleEmployeeCapabilities scheduleEmployeeCapabilities;

    public EmployeeFacade(EmployeeRepository employeeRepository, ScheduleEmployeeCapabilities scheduleEmployeeCapabilities) {
        this.employeeRepository = employeeRepository;
        this.scheduleEmployeeCapabilities = scheduleEmployeeCapabilities;
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

    public List<AllocatableCapabilityId> scheduleCapabilities(EmployeeId employeeId, TimeSlot oneDay) {
        return scheduleEmployeeCapabilities.setupEmployeeCapabilities(employeeId, oneDay);
    }

    //add vacation
        // calls availability
    //add sick leave
        // calls availability
    //change skills

}
