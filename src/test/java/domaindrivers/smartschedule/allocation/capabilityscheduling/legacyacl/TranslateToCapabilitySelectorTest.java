package domaindrivers.smartschedule.allocation.capabilityscheduling.legacyacl;

import domaindrivers.smartschedule.shared.CapabilitySelector;
import domaindrivers.smartschedule.shared.capability.Capability;
import domaindrivers.smartschedule.shared.timeslot.TimeSlot;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static domaindrivers.smartschedule.shared.CapabilitySelector.canPerformOneOf;
import static domaindrivers.smartschedule.shared.capability.Capability.permission;
import static domaindrivers.smartschedule.shared.capability.Capability.skill;
import static org.assertj.core.api.Assertions.assertThat;


class TranslateToCapabilitySelectorTest {

    @Test
    void translateLegacyEsbMessageToCapabilitySelectorModel() {
        //given
        List<String> legacyPermissions =
                List.of("ADMIN<>2", "ROOT<>1");
        List<List<String>> legacySkillsPerformedTogether = List.of(
                List.of("JAVA", "CSHARP", "PYTHON"),
                List.of("RUST", "CSHARP", "PYTHON")
        );
        List<String> legacyExclusiveSkills = List.of("YT DRAMA COMMENTS");

        //when
        List<CapabilitySelector> result = translate(legacySkillsPerformedTogether, legacyExclusiveSkills, legacyPermissions);

        //then
        assertThat(result)
                .containsExactlyInAnyOrder(
                        canPerformOneOf(Set.of(skill("YT DRAMA COMMENTS"))),
                        CapabilitySelector.canPerformAllAtTheTime(Capability.skills("JAVA", "CSHARP", "PYTHON")),
                        CapabilitySelector.canPerformAllAtTheTime(Capability.skills("RUST", "CSHARP", "PYTHON")),
                        canPerformOneOf(Set.of(permission("ADMIN"))),
                        canPerformOneOf(Set.of(permission("ADMIN"))),
                        canPerformOneOf(Set.of(permission("ROOT")))
                );

    }

    @Test
    void zeroMeansNoPermissionNowhere() {
        List<String> legacyPermissions =
                List.of("ADMIN<>0");

        //when
        List< CapabilitySelector> result = translate(List.of(), List.of(), legacyPermissions);

        //then
        assertThat(result)
                .isEmpty();
    }

    List<CapabilitySelector> translate(List<List<String>> legacySkillsPerformedTogether, List<String> legacyExclusiveSkills, List<String> legacyPermissions) {
        return new TranslateToCapabilitySelector().translate(new EmployeeDataFromLegacyEsbMessage(UUID.randomUUID(), legacySkillsPerformedTogether, legacyExclusiveSkills, legacyPermissions, TimeSlot.empty()));
    }

}