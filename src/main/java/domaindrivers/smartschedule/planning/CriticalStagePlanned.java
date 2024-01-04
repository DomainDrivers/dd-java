package domaindrivers.smartschedule.planning;

import domaindrivers.smartschedule.allocation.capabilityscheduling.AllocatableCapabilityId;
import domaindrivers.smartschedule.availability.ResourceId;
import domaindrivers.smartschedule.shared.Event;
import domaindrivers.smartschedule.shared.timeslot.TimeSlot;

import java.time.Instant;

public record CriticalStagePlanned(ProjectId projectId, TimeSlot stageTimeSlot, ResourceId criticalResource, Instant occurredAt) implements Event {

}
