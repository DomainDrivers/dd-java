package domaindrivers.smartschedule.allocation;

import domaindrivers.smartschedule.allocation.capabilityscheduling.AllocatableCapabilityId;
import domaindrivers.smartschedule.allocation.capabilityscheduling.AllocatableCapabilitySummary;
import domaindrivers.smartschedule.allocation.cashflow.Earnings;
import domaindrivers.smartschedule.shared.timeslot.TimeSlot;
import domaindrivers.smartschedule.simulation.ProjectId;
import domaindrivers.smartschedule.simulation.SimulatedProject;

import java.util.List;
import java.util.Map;

record PotentialTransfers(ProjectsAllocationsSummary summary, Map<ProjectAllocationsId, Earnings> earnings) {

    PotentialTransfers transfer(ProjectAllocationsId projectFrom, ProjectAllocationsId projectTo, AllocatedCapability allocatedCapability, TimeSlot forSlot) {
        Allocations from = summary.projectAllocations().get(projectFrom);
        Allocations to = summary.projectAllocations().get(projectTo);
        if (from == null || to == null) {
            return this;
        }
        Allocations newAllocationsProjectFrom = from.remove(allocatedCapability.allocatedCapabilityID(), forSlot);
        if (newAllocationsProjectFrom.equals(from)) {
            return this;
        }
        summary.projectAllocations().put(projectFrom, newAllocationsProjectFrom);
        Allocations newAllocationsProjectTo = to.add(new AllocatedCapability(allocatedCapability.allocatedCapabilityID(), allocatedCapability.capability(), forSlot));
        summary.projectAllocations().put(projectTo, newAllocationsProjectTo);
        return new PotentialTransfers(summary, earnings);
    }

    List<SimulatedProject> toSimulatedProjects() {
        return summary.projectAllocations().keySet().stream().map(project -> new SimulatedProject(ProjectId.from(project.id()), () -> earnings.get(project).toBigDecimal(), getMissingDemands(project))).toList();
    }

    domaindrivers.smartschedule.simulation.Demands getMissingDemands(ProjectAllocationsId projectAllocationsId) {
        Demands allDemands = summary.demands().get(projectAllocationsId).missingDemands(summary.projectAllocations().get(projectAllocationsId));
        return new domaindrivers.smartschedule.simulation.Demands(allDemands.all().stream().map(demand -> new domaindrivers.smartschedule.simulation.Demand(demand.capability(), demand.slot())).toList());
    }

    public PotentialTransfers transfer(ProjectAllocationsId projectTo, AllocatableCapabilitySummary capabilityToTransfer, TimeSlot forSlot) {
        ProjectAllocationsId projectToMoveFrom = findProjectToMoveFrom(capabilityToTransfer.id(), forSlot);
        if (projectToMoveFrom != null) {
            return transfer(projectToMoveFrom, projectTo, new AllocatedCapability(capabilityToTransfer.id(), capabilityToTransfer.capabilities(), capabilityToTransfer.timeSlot()), forSlot);
        }
        return this;
    }

    private ProjectAllocationsId findProjectToMoveFrom(AllocatableCapabilityId cap, TimeSlot inSlot) {
        return summary.projectAllocations().entrySet().stream().filter(entry -> entry.getValue().find(cap).isPresent()).map(Map.Entry::getKey).findFirst().orElse(null);
    }

}

