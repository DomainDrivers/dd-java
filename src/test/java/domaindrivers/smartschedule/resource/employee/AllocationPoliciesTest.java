package domaindrivers.smartschedule.resource.employee;

import domaindrivers.smartschedule.shared.CapabilitySelector;
import domaindrivers.smartschedule.shared.capability.Capability;
import org.junit.jupiter.api.Test;

import java.util.List;

import static domaindrivers.smartschedule.resource.employee.EmployeeAllocationPolicy.permissionsInMultipleProjects;
import static domaindrivers.smartschedule.resource.employee.EmployeeAllocationPolicy.simultaneous;
import static domaindrivers.smartschedule.shared.capability.Capability.permission;
import static domaindrivers.smartschedule.shared.capability.Capability.skill;
import static org.assertj.core.api.Assertions.assertThat;

class AllocationPoliciesTest {

    @Test
    public void defaultPolicyShouldReturnJustOneSkillAtOnce() {
        //given
        EmployeeSummary employee = new EmployeeSummary(EmployeeId.newOne(), "resourceName", "lastName", Seniority.LEAD, Capability.skills("JAVA"), Capability.permissions("ADMIN"));

        //when
        List<CapabilitySelector> capabilities = EmployeeAllocationPolicy.defaultPolicy().simultaneousCapabilitiesOf(employee);

        //then
        assertThat(capabilities).hasSize(1);
        assertThat(capabilities.get(0).capabilities())
                .containsExactlyInAnyOrder(
                        skill("JAVA"),
                        permission("ADMIN"));
    }

    @Test
    public void permissionsCanBeSharedBetweenProjects() {
        //given
        EmployeeAllocationPolicy policy = permissionsInMultipleProjects(3);
        EmployeeSummary employee = new EmployeeSummary(EmployeeId.newOne(), "resourceName", "lastName", Seniority.LEAD, Capability.skills("JAVA"), Capability.permissions("ADMIN"));

        //when
        List<CapabilitySelector> capabilities = policy.simultaneousCapabilitiesOf(employee);

        //then
        assertThat(capabilities).hasSize(3);

        assertThat(capabilities.stream().flatMap(cap -> cap.capabilities().stream()))
                .containsExactlyInAnyOrder(
                        permission("ADMIN"),
                        permission("ADMIN"),
                        permission("ADMIN"));

    }

    @Test
    public void canCreateCompositePolicy() {
        //given
        CompositePolicy policy = simultaneous(permissionsInMultipleProjects(3), EmployeeAllocationPolicy.oneOfSkills());
        EmployeeSummary employee = new EmployeeSummary(EmployeeId.newOne(), "resourceName", "lastName", Seniority.LEAD, Capability.skills("JAVA", "PYTHON"), Capability.permissions("ADMIN"));

        //when
        List<CapabilitySelector> capabilities = policy.simultaneousCapabilitiesOf(employee);

        //then
        assertThat(capabilities).hasSize(4);
        assertThat(capabilities).containsExactlyInAnyOrder(
                CapabilitySelector.canPerformOneOf(Capability.skills("JAVA", "PYTHON")),
                CapabilitySelector.canJustPerform(permission("ADMIN")),
                CapabilitySelector.canJustPerform(permission("ADMIN")),
                CapabilitySelector.canJustPerform(permission("ADMIN")));

    }


}