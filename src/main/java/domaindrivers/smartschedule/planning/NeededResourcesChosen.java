package domaindrivers.smartschedule.planning;

import domaindrivers.smartschedule.availability.ResourceId;
import domaindrivers.smartschedule.shared.PrivateEvent;
import domaindrivers.smartschedule.shared.PublishedEvent;
import domaindrivers.smartschedule.shared.timeslot.TimeSlot;

import java.time.Instant;
import java.util.Set;

public record NeededResourcesChosen(ProjectId projectId, Set<ResourceId> neededResources, TimeSlot timeSlot,
                                    Instant occurredAt) implements PublishedEvent {
}
