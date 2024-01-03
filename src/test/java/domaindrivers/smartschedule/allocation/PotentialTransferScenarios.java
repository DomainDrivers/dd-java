package domaindrivers.smartschedule.allocation;

import domaindrivers.smartschedule.allocation.cashflow.Earnings;
import domaindrivers.smartschedule.optimization.OptimizationFacade;
import domaindrivers.smartschedule.shared.timeslot.TimeSlot;
import domaindrivers.smartschedule.simulation.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static domaindrivers.smartschedule.shared.capability.Capability.skill;
import static java.math.BigDecimal.valueOf;
import static org.junit.jupiter.api.Assertions.assertEquals;

class PotentialTransferScenarios {

    static final TimeSlot JAN_1 = TimeSlot.createDailyTimeSlotAtUTC(2021, 1, 1);
    static final TimeSlot FIFTEEN_MINUTES_IN_JAN = new TimeSlot(JAN_1.from(), JAN_1.from().plus(15, ChronoUnit.MINUTES));
    static final Demands DEMAND_FOR_JAVA_JUST_FOR_15MIN_IN_JAN = new Demands(List.of(new Demand(skill("JAVA-MID"), FIFTEEN_MINUTES_IN_JAN)));
    static final Demands DEMAND_FOR_JAVA_MID_IN_JAN = new Demands(List.of(new Demand(skill("JAVA-MID"), JAN_1)));
    static final Demands DEMANDS_FOR_JAVA_AND_PYTHON_IN_JAN = new Demands(List.of(new Demand(skill("JAVA-MID"), JAN_1), new Demand(skill("PYTHON-MID"), JAN_1)));

    static final ProjectAllocationsId BANKING_SOFT_ID = ProjectAllocationsId.newOne();
    static final ProjectAllocationsId INSURANCE_SOFT_ID = ProjectAllocationsId.newOne();
    static final AllocatedCapability STASZEK_JAVA_MID = new AllocatedCapability(UUID.randomUUID(), skill("JAVA-MID"), JAN_1);

    PotentialTransfersService potentialTransfers = new PotentialTransfersService(new SimulationFacade(new OptimizationFacade()));

    @Test
    void simulatesMovingCapabilitiesToDifferentProject() {
        //given
        Project bankingSoft =
                new Project(BANKING_SOFT_ID, DEMAND_FOR_JAVA_MID_IN_JAN, Earnings.of(9));
        Project insuranceSoft =
                new Project(INSURANCE_SOFT_ID, DEMAND_FOR_JAVA_MID_IN_JAN, Earnings.of(90));
        bankingSoft.add(STASZEK_JAVA_MID);
        PotentialTransfers projects = toPotentialTransfers(bankingSoft, insuranceSoft);

        //when
        Double result = potentialTransfers.checkPotentialTransfer(projects, BANKING_SOFT_ID, INSURANCE_SOFT_ID, STASZEK_JAVA_MID, JAN_1);

        //then
        assertEquals(81d, result);
    }

    @Test
    void simulatesMovingCapabilitiesToDifferentProjectJustForAWhile() {
        //given
        Project bankingSoft =
                new Project(BANKING_SOFT_ID, DEMAND_FOR_JAVA_MID_IN_JAN, Earnings.of(9));
        Project insuranceSoft =
                new Project(INSURANCE_SOFT_ID, DEMAND_FOR_JAVA_JUST_FOR_15MIN_IN_JAN, Earnings.of(99));
        bankingSoft.add(STASZEK_JAVA_MID);
        PotentialTransfers projects = toPotentialTransfers(bankingSoft, insuranceSoft);

        //when
        Double result = potentialTransfers.checkPotentialTransfer(projects, BANKING_SOFT_ID, INSURANCE_SOFT_ID, STASZEK_JAVA_MID, FIFTEEN_MINUTES_IN_JAN);

        //then
        assertEquals(90d, result);
    }

    @Test
    void theMoveGivesZeroProfitWhenThereAreStillMissingDemands() {
        //given
        Project bankingSoft =
                new Project(BANKING_SOFT_ID, DEMAND_FOR_JAVA_MID_IN_JAN, Earnings.of(9));
        Project insuranceSoft =
                new Project(INSURANCE_SOFT_ID, DEMANDS_FOR_JAVA_AND_PYTHON_IN_JAN, Earnings.of(99));
        bankingSoft.add(STASZEK_JAVA_MID);
        PotentialTransfers projects = toPotentialTransfers(bankingSoft, insuranceSoft);

        //when
        Double result = potentialTransfers.checkPotentialTransfer(projects, BANKING_SOFT_ID, INSURANCE_SOFT_ID, STASZEK_JAVA_MID, JAN_1);

        //then
        assertEquals(-9d, result);
    }

    PotentialTransfers toPotentialTransfers(Project... projects) {
        Map<ProjectAllocationsId, Allocations> allocations = new HashMap<>();
        Map<ProjectAllocationsId, Demands> demands = new HashMap<>();
        Map<ProjectAllocationsId, Earnings> earnings = new HashMap<>();
        for (Project project : projects) {
            allocations.put(project.id, project.allocations);
            demands.put(project.id, project.demands);
            earnings.put(project.id, project.earnings);
        }
        return new PotentialTransfers(new ProjectsAllocationsSummary(Map.of(), allocations, demands), earnings);
    }

}

class Project {

    ProjectAllocationsId id;
    Earnings earnings;
    Demands demands;
    Allocations allocations;

    Project(ProjectAllocationsId id, Demands demands, Earnings earnings) {
        this.id = id;
        this.demands = demands;
        this.earnings = earnings;
        this.allocations = Allocations.none();
    }

    Allocations add(AllocatedCapability allocatedCapability) {
        this.allocations = allocations.add(allocatedCapability);
        return allocations;
    }

}