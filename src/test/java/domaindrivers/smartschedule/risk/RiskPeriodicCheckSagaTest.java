package domaindrivers.smartschedule.risk;

import domaindrivers.smartschedule.allocation.*;
import domaindrivers.smartschedule.allocation.capabilityscheduling.AllocatableCapabilityId;
import domaindrivers.smartschedule.allocation.cashflow.Earnings;
import domaindrivers.smartschedule.allocation.cashflow.EarningsRecalculated;
import domaindrivers.smartschedule.availability.Owner;
import domaindrivers.smartschedule.availability.ResourceTakenOver;
import domaindrivers.smartschedule.shared.capability.Capability;
import domaindrivers.smartschedule.shared.timeslot.TimeSlot;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Set;

import static domaindrivers.smartschedule.risk.RiskPeriodicCheckSagaStep.*;
import static domaindrivers.smartschedule.shared.capability.Capability.skill;
import static org.junit.jupiter.api.Assertions.assertEquals;

class RiskPeriodicCheckSagaTest {

    static final Clock clock = Clock.fixed(Instant.now(), ZoneOffset.UTC);
    static final Capability JAVA = skill("JAVA");
    static final TimeSlot ONE_DAY = TimeSlot.createDailyTimeSlotAtUTC(2022, 1, 1);
    static final Demands SINGLE_DEMAND = Demands.of(new Demand(JAVA, ONE_DAY));
    static final Demands MANY_DEMANDS = Demands.of(new Demand(JAVA, ONE_DAY), new Demand(JAVA, ONE_DAY));
    static final TimeSlot PROJECT_DATES = new TimeSlot(
            Instant.parse("2021-01-01T00:00:00.00Z"),
            Instant.parse("2021-01-05T00:00:00.00Z"));
    static final ProjectAllocationsId PROJECT_ID = ProjectAllocationsId.newOne();
    static final AllocatableCapabilityId CAPABILITY_ID = AllocatableCapabilityId.newOne();

    @Test
    void updatesInitialDemandsOnSagaCreation() {
        //when
        RiskPeriodicCheckSaga saga = new RiskPeriodicCheckSaga(PROJECT_ID, SINGLE_DEMAND);

        //then
        assertEquals(SINGLE_DEMAND, saga.missingDemands());
    }

    @Test
    void updatesDeadlineOnDeadlineSet() {
        //given
        RiskPeriodicCheckSaga saga = new RiskPeriodicCheckSaga(PROJECT_ID, SINGLE_DEMAND);
        //and
        saga.handle(new ProjectAllocationScheduled(PROJECT_ID, PROJECT_DATES, Instant.now(clock)));

        //then
        assertEquals(PROJECT_DATES.to(), saga.deadline());
    }

    @Test
    void updateMissingDemands() {
        //given
        RiskPeriodicCheckSaga saga = new RiskPeriodicCheckSaga(PROJECT_ID, SINGLE_DEMAND);

        //when
        RiskPeriodicCheckSagaStep nextStep = saga.missingDemands(MANY_DEMANDS);

        //then
        assertEquals(DO_NOTHING, nextStep);
        assertEquals(MANY_DEMANDS, saga.missingDemands());
    }

    @Test
    void noNewStepsOnWhenMissingDemands() {
        //given
        RiskPeriodicCheckSaga saga = new RiskPeriodicCheckSaga(PROJECT_ID, MANY_DEMANDS);

        //when
        RiskPeriodicCheckSagaStep nextStep = saga.missingDemands(MANY_DEMANDS);

        //then
        assertEquals(DO_NOTHING, nextStep);
    }

    @Test
    void updatedEarningsOnEarningsRecalculated() {
        //given
        RiskPeriodicCheckSaga saga = new RiskPeriodicCheckSaga(PROJECT_ID, SINGLE_DEMAND);

        //when
        RiskPeriodicCheckSagaStep nextStep = saga.handle(new EarningsRecalculated(PROJECT_ID, Earnings.of(1000), Instant.now(clock)));
        assertEquals(DO_NOTHING, nextStep);

        //then
        assertEquals(Earnings.of(1000), saga.earnings());

        //when
        nextStep = saga.handle(new EarningsRecalculated(PROJECT_ID, Earnings.of(900), Instant.now(clock)));

        //then
        assertEquals(Earnings.of(900), saga.earnings());
        assertEquals(DO_NOTHING, nextStep);

    }

    @Test
    void informsAboutDemandsSatisfiedWhenNoMissingDemands() {
        //given
        RiskPeriodicCheckSaga saga = new RiskPeriodicCheckSaga(PROJECT_ID, MANY_DEMANDS);
        //and
        saga.handle(new EarningsRecalculated(PROJECT_ID, Earnings.of(1000), Instant.now(clock)));
        //when
        RiskPeriodicCheckSagaStep stillMissing = saga.missingDemands(SINGLE_DEMAND);
        RiskPeriodicCheckSagaStep zeroDemands = saga.missingDemands(Demands.none());

        //then
        assertEquals(DO_NOTHING, stillMissing);
        assertEquals(NOTIFY_ABOUT_DEMANDS_SATISFIED, zeroDemands);
    }

    @Test
    void doNothingOnResourceTakenOverWhenAfterDeadline() {
        //given
        RiskPeriodicCheckSaga saga = new RiskPeriodicCheckSaga(PROJECT_ID, MANY_DEMANDS);
        //and
        saga.handle(new ProjectAllocationScheduled(PROJECT_ID, PROJECT_DATES, Instant.now(clock)));

        //when
        Instant afterDeadline = PROJECT_DATES.to().plus(100, ChronoUnit.HOURS);
        RiskPeriodicCheckSagaStep nextStep = saga.handle(new ResourceTakenOver(CAPABILITY_ID.toAvailabilityResourceId(), Set.of(Owner.of(PROJECT_ID.id())), ONE_DAY, afterDeadline));

        //then
        assertEquals(DO_NOTHING, nextStep);
    }

    @Test
    void notifyAboutRiskOnResourceTakenOverWhenBeforeDeadline() {
        //given
        RiskPeriodicCheckSaga saga = new RiskPeriodicCheckSaga(PROJECT_ID, MANY_DEMANDS);
        //and
        saga.handle(new ProjectAllocationScheduled(PROJECT_ID, PROJECT_DATES, Instant.now(clock)));

        //when
        Instant beforeDeadline = PROJECT_DATES.to().minus(100, ChronoUnit.HOURS);
        RiskPeriodicCheckSagaStep nextStep = saga.handle(new ResourceTakenOver(CAPABILITY_ID.toAvailabilityResourceId(), Set.of(Owner.of(PROJECT_ID.id())), ONE_DAY, beforeDeadline));

        //then
        assertEquals(RiskPeriodicCheckSagaStep.NOTIFY_ABOUT_POSSIBLE_RISK, nextStep);
    }

    @Test
    void noNextStepOnCapabilityReleased() {
        //given
        RiskPeriodicCheckSaga saga = new RiskPeriodicCheckSaga(PROJECT_ID, SINGLE_DEMAND);

        //when
        RiskPeriodicCheckSagaStep nextStep = saga.missingDemands(SINGLE_DEMAND);

        //then
        assertEquals(DO_NOTHING, nextStep);
    }

    @Test
    void weeklyCheckShouldResultInNothingWhenAllDemandsSatisfied() {
        //given
        RiskPeriodicCheckSaga saga = new RiskPeriodicCheckSaga(PROJECT_ID, SINGLE_DEMAND);
        //and
        saga.handle(new EarningsRecalculated(PROJECT_ID, Earnings.of(1000), Instant.now(clock)));
        //and
        saga.missingDemands(Demands.none());
        //and
        saga.handle(new ProjectAllocationScheduled(PROJECT_ID, PROJECT_DATES, Instant.now(clock)));


        //when
        Instant wayBeforeDeadline = PROJECT_DATES.to().minus(Duration.ofDays(1));
        RiskPeriodicCheckSagaStep nextStep = saga.handleWeeklyCheck(wayBeforeDeadline);

        //then
        assertEquals(DO_NOTHING, nextStep);
    }

    @Test
    void weeklyCheckShouldResultInNothingWhenAfterDeadline() {
        //given
        RiskPeriodicCheckSaga saga = new RiskPeriodicCheckSaga(PROJECT_ID, SINGLE_DEMAND);
        //and
        saga.handle(new EarningsRecalculated(PROJECT_ID, Earnings.of(1000), Instant.now(clock)));
        //and
        saga.handle(new ProjectAllocationScheduled(PROJECT_ID, PROJECT_DATES, Instant.now(clock)));

        //when
        Instant wayAfterDeadline = PROJECT_DATES.to().plus(Duration.ofDays(300));
        RiskPeriodicCheckSagaStep nextStep = saga.handleWeeklyCheck(wayAfterDeadline);

        //then
        assertEquals(DO_NOTHING, nextStep);
    }

    @Test
    void weeklyCheckDoesNothingWhenNoDeadline() {
        //given
        RiskPeriodicCheckSaga saga = new RiskPeriodicCheckSaga(PROJECT_ID, SINGLE_DEMAND);

        //when
        RiskPeriodicCheckSagaStep nextStep = saga.handleWeeklyCheck(Instant.now(clock));

        //then
        assertEquals(DO_NOTHING, nextStep);
    }

    @Test
    void weeklyCheckShouldResultInNothingWhenNotCloseToDeadlineAndDemandsNotSatisfied() {
        //given
        RiskPeriodicCheckSaga saga = new RiskPeriodicCheckSaga(PROJECT_ID, SINGLE_DEMAND);
        //and
        saga.handle(new EarningsRecalculated(PROJECT_ID, Earnings.of(1000), Instant.now(clock)));
        //and
        saga.handle(new ProjectAllocationScheduled(PROJECT_ID, PROJECT_DATES, Instant.now(clock)));

        //when
        Instant wayBeforeDeadline = PROJECT_DATES.to().minus(Duration.ofDays(300));
        RiskPeriodicCheckSagaStep nextStep = saga.handleWeeklyCheck(wayBeforeDeadline);

        //then
        assertEquals(DO_NOTHING, nextStep);
    }

    @Test
    void weeklyCheckShouldResultInFindAvailableWhenCloseToDeadlineAndDemandsNotSatisfied() {
        //given
        RiskPeriodicCheckSaga saga = new RiskPeriodicCheckSaga(PROJECT_ID, MANY_DEMANDS);
        //and
        saga.handle(new EarningsRecalculated(PROJECT_ID, Earnings.of(1000), Instant.now(clock)));
        //and
        saga.handle(new ProjectAllocationScheduled(PROJECT_ID, PROJECT_DATES, Instant.now(clock)));

        //when
        Instant closeToDeadline = PROJECT_DATES.to().minus(Duration.ofDays(20));
        RiskPeriodicCheckSagaStep nextStep = saga.handleWeeklyCheck(closeToDeadline);

        //then
        assertEquals(FIND_AVAILABLE, nextStep);
    }

    @Test
    void weeklyCheckShouldResultInReplacementSuggestingWhenHighValueProjectReallyCloseToDeadlineAndDemandsNotSatisfied() {
        //given
        RiskPeriodicCheckSaga saga = new RiskPeriodicCheckSaga(PROJECT_ID, MANY_DEMANDS);
        //and
        saga.handle(new EarningsRecalculated(PROJECT_ID, Earnings.of(10000), Instant.now(clock)));
        //and
        saga.handle(new ProjectAllocationScheduled(PROJECT_ID, PROJECT_DATES, Instant.now(clock)));

        //when
        Instant reallyCloseToDeadline = PROJECT_DATES.to().minus(Duration.ofDays(2));
        RiskPeriodicCheckSagaStep nextStep = saga.handleWeeklyCheck(reallyCloseToDeadline);

        //then
        assertEquals(SUGGEST_REPLACEMENT, nextStep);
    }

}