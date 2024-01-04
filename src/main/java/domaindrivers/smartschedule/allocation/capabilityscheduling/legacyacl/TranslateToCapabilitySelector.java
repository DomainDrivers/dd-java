package domaindrivers.smartschedule.allocation.capabilityscheduling.legacyacl;

import domaindrivers.smartschedule.shared.CapabilitySelector;
import domaindrivers.smartschedule.shared.capability.Capability;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static domaindrivers.smartschedule.shared.capability.Capability.permission;

class TranslateToCapabilitySelector {

    public List<CapabilitySelector> translate(EmployeeDataFromLegacyEsbMessage message) {
        List<CapabilitySelector> employeeSkills = message.skillsPerformedTogether
                .stream()
                .map(skills -> CapabilitySelector.canPerformAllAtTheTime(
                        skills.stream().map(Capability::skill).collect(Collectors.toSet())))
                .toList();
        List<CapabilitySelector> employeeExclusiveSkills = message.exclusiveSkills
                .stream()
                .map(skill -> CapabilitySelector.canJustPerform(
                        Capability.skill(skill)))
                .toList();
        List<CapabilitySelector> employeePermissions = message.permissions
                .stream()
                .map(this::multiplePermission)
                .flatMap(List::stream)
                .toList();
        //schedule or rewrite if exists;
        return Stream.concat(Stream.concat(employeeSkills.stream(), employeeExclusiveSkills.stream()), employeePermissions.stream()).toList();
    }

    private List<CapabilitySelector> multiplePermission(String permissionLegacyCode) {
        List<String> parts = Arrays
                        .stream(permissionLegacyCode
                        .split("<>"))
                        .toList();
        String permission = parts.get(0);
        int times = Integer.parseInt(parts.get(1));
        return IntStream
                .range(0, times)
                .mapToObj(value -> CapabilitySelector.canJustPerform(permission(permission)))
                .toList();
    }
}
