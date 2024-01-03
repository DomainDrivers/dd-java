package domaindrivers.smartschedule.resource.employee;


import domaindrivers.smartschedule.shared.capability.Capability;

import java.util.Set;

public record EmployeeSummary(EmployeeId id, String name, String lastName, Seniority seniority, Set<Capability> skills, Set<Capability> permissions) {

}
