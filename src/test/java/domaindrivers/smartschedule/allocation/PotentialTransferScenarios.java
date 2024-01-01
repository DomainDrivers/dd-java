package domaindrivers.smartschedule.allocation;

import domaindrivers.smartschedule.optimization.OptimizationFacade;
import domaindrivers.smartschedule.shared.timeslot.TimeSlot;
import domaindrivers.smartschedule.simulation.*;
import org.junit.jupiter.api.Test;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static domaindrivers.smartschedule.shared.capability.Capability.skill;
import static java.math.BigDecimal.valueOf;
import static org.junit.jupiter.api.Assertions.assertEquals;

class PotentialTransferScenarios {

    static final TimeSlot JAN_1 = TimeSlot.createDailyTimeSlotAtUTC(2021, 1, 1);
    static final TimeSlot FIFTEEN_MINUTES_IN_JAN = new TimeSlot(JAN_1.from(), JAN_1.from().plus(15, ChronoUnit.MINUTES));
    static final Demands DEMAND_FOR_JAVA_JUST_FOR_15MIN_IN_JAN = new Demands(List.of(new Demand(skill("JAVA-MID"), FIFTEEN_MINUTES_IN_JAN)));
    static final Demands DEMAND_FOR_JAVA_MID_IN_JAN = new Demands(List.of(new Demand(skill("JAVA-MID"), JAN_1)));
    static final Demands DEMANDS_FOR_JAVA_AND_PYTHON_IN_JAN = new Demands(List.of(new Demand(skill("JAVA-MID"), JAN_1), new Demand(skill("PYTHON-MID"), JAN_1)));

    static final UUID BANKING_SOFT_ID = UUID.randomUUID();
    static final UUID INSURANCE_SOFT_ID = UUID.randomUUID();
    static final AllocatedCapability STASZEK_JAVA_MID = new AllocatedCapability(UUID.randomUUID(), skill("JAVA-MID"), JAN_1);

    AllocationFacade simulationFacade = new AllocationFacade(new SimulationFacade(new OptimizationFacade()));

    @Test
    void simulatesMovingCapabilitiesToDifferentProject() {
        //given
        Project bankingSoft =
                new Project(DEMAND_FOR_JAVA_MID_IN_JAN, valueOf(9));
        Project insuranceSoft =
                new Project(DEMAND_FOR_JAVA_MID_IN_JAN, valueOf(90));
        Projects projects = new Projects(
                Map.of(BANKING_SOFT_ID, bankingSoft, INSURANCE_SOFT_ID, insuranceSoft));
        //and
        bankingSoft.add(STASZEK_JAVA_MID);

        //when
        Double result = simulationFacade.checkPotentialTransfer(projects, BANKING_SOFT_ID, INSURANCE_SOFT_ID, STASZEK_JAVA_MID, JAN_1);

        //then
        assertEquals(81d, result);
    }

    @Test
    void simulatesMovingCapabilitiesToDifferentProjectJustForAWhile() {
        //given
        Project bankingSoft =
                new Project(DEMAND_FOR_JAVA_MID_IN_JAN, valueOf(9));
        Project insuranceSoft =
                new Project(DEMAND_FOR_JAVA_JUST_FOR_15MIN_IN_JAN, valueOf(99));
        Projects projects = new Projects(
                Map.of(BANKING_SOFT_ID, bankingSoft, INSURANCE_SOFT_ID, insuranceSoft));
        //and
        bankingSoft.add(STASZEK_JAVA_MID);

        //when
        Double result = simulationFacade.checkPotentialTransfer(projects, BANKING_SOFT_ID, INSURANCE_SOFT_ID, STASZEK_JAVA_MID, FIFTEEN_MINUTES_IN_JAN);

        //then
        assertEquals(90d, result);
    }

    @Test
    void theMoveGivesZeroProfitWhenThereAreStillMissingDemands() {
        //given
        Project bankingSoft =
                new Project(DEMAND_FOR_JAVA_MID_IN_JAN, valueOf(9));
        Project insuranceSoft =
                new Project(DEMANDS_FOR_JAVA_AND_PYTHON_IN_JAN, valueOf(99));
        Projects projects = new Projects(
                Map.of(BANKING_SOFT_ID, bankingSoft, INSURANCE_SOFT_ID, insuranceSoft));
        //and
        bankingSoft.add(STASZEK_JAVA_MID);

        //when
        Double result = simulationFacade.checkPotentialTransfer(projects, BANKING_SOFT_ID, INSURANCE_SOFT_ID, STASZEK_JAVA_MID, JAN_1);

        //then
        assertEquals(-9d, result);
    }

}