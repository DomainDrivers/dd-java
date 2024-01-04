package domaindrivers.smartschedule.resource.employee;

import domaindrivers.smartschedule.shared.CapabilitySelector;
import domaindrivers.smartschedule.shared.capability.Capability;

import java.util.*;
import java.util.stream.IntStream;

public interface EmployeeAllocationPolicy {

    List<CapabilitySelector> simultaneousCapabilitiesOf(EmployeeSummary employee);

    static EmployeeAllocationPolicy defaultPolicy() {
        return employee -> {
            Set<Capability> all = new HashSet<>();
            all.addAll(employee.skills());
            all.addAll(employee.permissions());
            return List.of(CapabilitySelector.canPerformOneOf(all));
        };
    }

    static EmployeeAllocationPolicy permissionsInMultipleProjects(int howMany) {
        return employee -> employee
                .permissions()
                .stream()
                .flatMap(permission -> IntStream.range(0, howMany).mapToObj(i -> permission))
                .map(CapabilitySelector::canJustPerform)
                .toList();
    }

    static EmployeeAllocationPolicy oneOfSkills() {
        return employee -> List.of(CapabilitySelector.canPerformOneOf(employee.skills()));
    }

    static CompositePolicy simultaneous(EmployeeAllocationPolicy... policies) {
        return new CompositePolicy(Arrays.asList(policies));
    }
}

class CompositePolicy implements EmployeeAllocationPolicy {

    final List<EmployeeAllocationPolicy> policies;

    CompositePolicy(List<EmployeeAllocationPolicy> policies) {
        this.policies = policies;
    }

    @Override
    public List<CapabilitySelector> simultaneousCapabilitiesOf(EmployeeSummary employee) {
        return policies
                .stream()
                .flatMap(p -> p.simultaneousCapabilitiesOf(employee).stream())
                .toList();
    }
}