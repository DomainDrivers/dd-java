package domaindrivers.smartschedule.risk;

import domaindrivers.smartschedule.allocation.Demand;
import domaindrivers.smartschedule.allocation.ProjectAllocationsId;
import domaindrivers.smartschedule.allocation.capabilityscheduling.AllocatableCapabilitiesSummary;
import domaindrivers.smartschedule.allocation.capabilityscheduling.AllocatableCapabilityId;
import domaindrivers.smartschedule.availability.ResourceId;
import domaindrivers.smartschedule.planning.ProjectId;
import domaindrivers.smartschedule.shared.timeslot.TimeSlot;

import java.util.Map;
import java.util.Set;

public class RiskPushNotification {

    public void notifyDemandsSatisfied(ProjectAllocationsId projectId) {
    }

    public void notifyAboutAvailability(ProjectAllocationsId projectId, Map<Demand, AllocatableCapabilitiesSummary> available) {
    }

    public void notifyProfitableRelocationFound(ProjectAllocationsId projectId, AllocatableCapabilityId allocatableCapabilityId) {
    }

    public void notifyAboutPossibleRisk(ProjectAllocationsId projectId) {
    }

    public void notifyAboutPossibleRiskDuringPlanning(ProjectId cause, domaindrivers.smartschedule.planning.Demands demands) {
    }

    public void notifyAboutCriticalResourceNotAvailable(ProjectId cause, ResourceId criticalResource, TimeSlot timeSlot) {
    }

    public void notifyAboutResourcesNotAvailable(ProjectId projectId, Set<ResourceId> notAvailable) {
    }
}
