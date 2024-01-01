package domaindrivers.smartschedule.allocation;

import domaindrivers.smartschedule.shared.timeslot.TimeSlot;
import domaindrivers.smartschedule.simulation.ProjectId;
import domaindrivers.smartschedule.simulation.SimulatedProject;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

record Projects(Map<UUID, Project> projects) {

    Projects transfer(UUID projectFrom, UUID projectTo, AllocatedCapability capability, TimeSlot forSlot) {
        Project from = projects.get(projectFrom);
        Project to = projects.get(projectTo);
        if (from == null || to == null) {
            return this;
        }
        Optional<AllocatedCapability> removed = from.remove(capability, forSlot);
        if (removed.isEmpty()) {
            return this;
        }
        to.add(new AllocatedCapability(removed.get().resourceId(), removed.get().capability(), forSlot));
        return new Projects(projects);
    }

    List<SimulatedProject> toSimulatedProjects() {
        return projects.entrySet().stream().map(entry -> new SimulatedProject(ProjectId.from(entry.getKey()), () -> entry.getValue().earnings(), getMissingDemands(entry.getValue()))).toList();
    }

    domaindrivers.smartschedule.simulation.Demands getMissingDemands(Project project) {
        Demands allDemands = project.missingDemands();
        return new domaindrivers.smartschedule.simulation.Demands(
                allDemands
                        .all()
                        .stream()
                        .map(demand -> new domaindrivers.smartschedule.simulation.Demand(demand.capability(), demand.slot()))
                        .toList());
    }

}

