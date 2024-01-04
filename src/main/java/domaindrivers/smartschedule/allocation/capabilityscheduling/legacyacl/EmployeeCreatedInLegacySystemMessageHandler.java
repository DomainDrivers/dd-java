package domaindrivers.smartschedule.allocation.capabilityscheduling.legacyacl;

import domaindrivers.smartschedule.allocation.capabilityscheduling.AllocatableResourceId;
import domaindrivers.smartschedule.allocation.capabilityscheduling.CapabilityScheduler;
import domaindrivers.smartschedule.shared.CapabilitySelector;
import domaindrivers.smartschedule.shared.timeslot.TimeSlot;

import java.util.List;
import java.util.UUID;

public class EmployeeCreatedInLegacySystemMessageHandler {

    private final CapabilityScheduler capabilityScheduler;

    public EmployeeCreatedInLegacySystemMessageHandler(CapabilityScheduler capabilityScheduler) {
        this.capabilityScheduler = capabilityScheduler;
    }

    //subscribe to message bus
    //StreamListener to (message_bus)
    public void handle(EmployeeDataFromLegacyEsbMessage message) {
        AllocatableResourceId allocatableResourceId = new AllocatableResourceId(message.resourceId);
        List<CapabilitySelector> capabilitySelectors = new TranslateToCapabilitySelector().translate(message);
        capabilityScheduler.scheduleResourceCapabilitiesForPeriod(allocatableResourceId, capabilitySelectors, message.timeSlot);
    }

}
class EmployeeDataFromLegacyEsbMessage {
    UUID resourceId;
    List<List<String>> skillsPerformedTogether;
    List<String> exclusiveSkills;
    List<String> permissions;
    TimeSlot timeSlot;

    EmployeeDataFromLegacyEsbMessage(UUID resourceId, List<List<String>> skillsPerformedTogether, List<String> exclusiveSkills, List<String> permissions, TimeSlot timeSlot) {
        this.resourceId = resourceId;
        this.skillsPerformedTogether = skillsPerformedTogether;
        this.exclusiveSkills = exclusiveSkills;
        this.permissions = permissions;
        this.timeSlot = timeSlot;
    }
}

