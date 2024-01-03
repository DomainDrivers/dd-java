package domaindrivers.smartschedule.allocation;

import domaindrivers.smartschedule.optimization.Result;
import domaindrivers.smartschedule.shared.timeslot.TimeSlot;
import domaindrivers.smartschedule.simulation.SimulatedCapabilities;
import domaindrivers.smartschedule.simulation.SimulationFacade;


public class PotentialTransfersService {

    private final SimulationFacade simulationFacade;

    public PotentialTransfersService(SimulationFacade simulationFacade) {
        this.simulationFacade = simulationFacade;
    }

    double checkPotentialTransfer(PotentialTransfers transfers, ProjectAllocationsId projectFrom, ProjectAllocationsId projectTo, AllocatedCapability capability, TimeSlot forSlot) {
        Result resultBefore =
                simulationFacade.whatIsTheOptimalSetup(transfers.toSimulatedProjects(), SimulatedCapabilities.none());
        transfers = transfers.transfer(projectFrom, projectTo, capability, forSlot);
        Result resultAfter =
                simulationFacade.whatIsTheOptimalSetup(transfers.toSimulatedProjects(), SimulatedCapabilities.none());
        return resultAfter.profit() - resultBefore.profit();
    }

}

