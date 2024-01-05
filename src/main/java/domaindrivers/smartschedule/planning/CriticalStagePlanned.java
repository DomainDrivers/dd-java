package domaindrivers.smartschedule.planning;

import domaindrivers.smartschedule.availability.ResourceId;
import domaindrivers.smartschedule.shared.PrivateEvent;
import domaindrivers.smartschedule.shared.PublishedEvent;
import domaindrivers.smartschedule.shared.timeslot.TimeSlot;

import java.time.Instant;

public record CriticalStagePlanned(ProjectId projectId, TimeSlot stageTimeSlot, ResourceId criticalResource, Instant occurredAt) implements PublishedEvent {

}
