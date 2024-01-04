package domaindrivers.smartschedule.resource.employee;

import domaindrivers.smartschedule.allocation.AllocationFacade;
import domaindrivers.smartschedule.allocation.capabilityscheduling.AllocatableCapabilityId;
import domaindrivers.smartschedule.allocation.capabilityscheduling.CapabilityScheduler;
import domaindrivers.smartschedule.shared.CapabilitySelector;
import domaindrivers.smartschedule.shared.timeslot.TimeSlot;

import java.util.List;

class ScheduleEmployeeCapabilities {

    private final EmployeeRepository employeeRepository;
    private final CapabilityScheduler capabilityScheduler;

    ScheduleEmployeeCapabilities(EmployeeRepository employeeRepository, CapabilityScheduler capabilityScheduler) {
        this.employeeRepository = employeeRepository;
        this.capabilityScheduler = capabilityScheduler;
    }

    public List<AllocatableCapabilityId> setupEmployeeCapabilities(EmployeeId employeeId, TimeSlot timeSlot) {
        EmployeeSummary summary = employeeRepository.findSummary(employeeId);
        EmployeeAllocationPolicy policy = findAllocationPolicy(summary);
        List<CapabilitySelector> capabilities = policy.simultaneousCapabilitiesOf(summary);
        return capabilityScheduler.scheduleResourceCapabilitiesForPeriod(employeeId.toAllocatableResourceId(), capabilities, timeSlot);
    }

    private EmployeeAllocationPolicy findAllocationPolicy(EmployeeSummary employee) {
        if (employee.seniority().equals(Seniority.LEAD)) {
            return EmployeeAllocationPolicy.simultaneous(EmployeeAllocationPolicy.oneOfSkills(), EmployeeAllocationPolicy.permissionsInMultipleProjects(3));
        }
        return EmployeeAllocationPolicy.defaultPolicy();
    }

}
