package domaindrivers.smartschedule.allocation;

import domaindrivers.smartschedule.allocation.capabilityscheduling.AllocatableCapabilitySummary;
import domaindrivers.smartschedule.allocation.cashflow.CashFlowFacade;
import domaindrivers.smartschedule.optimization.Result;
import domaindrivers.smartschedule.shared.timeslot.TimeSlot;
import domaindrivers.smartschedule.simulation.SimulatedCapabilities;
import domaindrivers.smartschedule.simulation.SimulationFacade;


public class PotentialTransfersService {

    private final SimulationFacade simulationFacade;
    private final CashFlowFacade cashFlowFacade;
    private final ProjectAllocationsRepository projectAllocationsRepository;

    public PotentialTransfersService(SimulationFacade simulationFacade, CashFlowFacade cashFlowFacade, ProjectAllocationsRepository projectAllocationsRepository) {
        this.simulationFacade = simulationFacade;
        this.cashFlowFacade = cashFlowFacade;
        this.projectAllocationsRepository = projectAllocationsRepository;
    }

    public double profitAfterMovingCapabilities(ProjectAllocationsId projectId, AllocatableCapabilitySummary capabilityToMove, TimeSlot timeSlot) {
        //cached?
        PotentialTransfers potentialTransfers = new PotentialTransfers(ProjectsAllocationsSummary.of(projectAllocationsRepository.findAll()), cashFlowFacade.findAllEarnings());
        return checkPotentialTransfer(potentialTransfers, projectId, capabilityToMove, timeSlot);
    }

    private double checkPotentialTransfer(PotentialTransfers transfers, ProjectAllocationsId projectTo, AllocatableCapabilitySummary capabilityToMove, TimeSlot forSlot) {
        Result resultBefore =
                simulationFacade.whatIsTheOptimalSetup(transfers.toSimulatedProjects(), SimulatedCapabilities.none());
        transfers = transfers.transfer(projectTo, capabilityToMove, forSlot);
        Result resultAfter =
                simulationFacade.whatIsTheOptimalSetup(transfers.toSimulatedProjects(), SimulatedCapabilities.none());
        return resultAfter.profit() - resultBefore.profit();
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

