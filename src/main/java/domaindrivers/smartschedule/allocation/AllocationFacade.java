package domaindrivers.smartschedule.allocation;

import domaindrivers.smartschedule.optimization.Result;
import domaindrivers.smartschedule.shared.timeslot.TimeSlot;
import domaindrivers.smartschedule.simulation.SimulatedCapabilities;
import domaindrivers.smartschedule.simulation.SimulationFacade;

import java.util.UUID;

public class AllocationFacade {

    private final SimulationFacade simulationFacade;

    public AllocationFacade(SimulationFacade simulationFacade) {
        this.simulationFacade = simulationFacade;
    }

    double checkPotentialTransfer(Projects projects, UUID projectFrom, UUID projectTo, AllocatedCapability capability, TimeSlot forSlot) {
        //Project rather fetched from db.
        Result resultBefore =
                simulationFacade.whatIsTheOptimalSetup(projects.toSimulatedProjects(), SimulatedCapabilities.none());
        projects = projects.transfer(projectFrom, projectTo, capability, forSlot);
        Result resultAfter =
                simulationFacade.whatIsTheOptimalSetup(projects.toSimulatedProjects(), SimulatedCapabilities.none());
        return resultAfter.profit() - resultBefore.profit();
    }

}
