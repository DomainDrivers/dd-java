package domaindrivers.smartschedule.allocation;

import domaindrivers.smartschedule.allocation.capabilityscheduling.AllocatableCapabilityId;
import domaindrivers.smartschedule.shared.CapabilitySelector;
import domaindrivers.smartschedule.shared.timeslot.TimeSlot;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static domaindrivers.smartschedule.shared.capability.Capability.permission;
import static domaindrivers.smartschedule.shared.capability.Capability.skill;
import static org.assertj.core.api.Assertions.assertThat;

abstract class ProjectAllocationsScenario {

    static final Instant WHEN = Instant.MIN;

    static final TimeSlot FEB_1 = TimeSlot.createDailyTimeSlotAtUTC(2020, 2, 1);
    static final TimeSlot FEB_2 = TimeSlot.createDailyTimeSlotAtUTC(2020, 2, 2);
    static final TimeSlot JANUARY = TimeSlot.createMonthlyTimeSlotAtUTC(2020, 1);
    static final TimeSlot FEBRUARY = TimeSlot.createMonthlyTimeSlotAtUTC(2020, 2);
    static final TimeSlot JAN = TimeSlot.createDailyTimeSlotAtUTC(2021, 1, 1);
    static final TimeSlot FEB = TimeSlot.createDailyTimeSlotAtUTC(2021, 2, 1);

    static final CapabilitySelector ADMIN = CapabilitySelector.canJustPerform(permission("ADMIN"));
    static final CapabilitySelector JAVA = CapabilitySelector.canJustPerform(skill("JAVA"));

    abstract ProjectAllocationsDsl dsl();

    // ----- allocate -----

    @Test
    void canAllocate() {
        ProjectAllocationsId projectId = ProjectAllocationsId.newOne();
        dsl().scheduleDemands(projectId, Demands.none(), WHEN);
        AllocatableCapabilityId adminId = dsl().scheduleAvailableCapability(ADMIN, FEB_1);

        Optional<AllocationOutcome> outcome = dsl().allocate(projectId, adminId, ADMIN, FEB_1, WHEN);

        assertThat(outcome).isPresent();
        assertThat(outcome.get().missingDemandsAfter()).isEqualTo(Demands.none());
        outcome.get().occurredAt().ifPresent(occurredAt -> assertThat(occurredAt).isEqualTo(WHEN));
    }

    @Test
    void cantAllocateWhenRequestedTimeSlotNotWithingProjectSlot() {
        ProjectAllocationsId projectId = dsl().createProject(JANUARY, Demands.none()).projectId();
        AllocatableCapabilityId adminId = dsl().scheduleAvailableCapability(ADMIN, FEB_1);

        Optional<AllocationOutcome> outcome = dsl().allocate(projectId, adminId, ADMIN, FEB_1, WHEN);

        assertThat(outcome).isEmpty();
    }

    @Test
    void allocatingHasNoEffectWhenCapabilityAlreadyAllocated() {
        ProjectAllocationsId projectId = ProjectAllocationsId.newOne();
        dsl().scheduleDemands(projectId, Demands.none(), WHEN);
        AllocatableCapabilityId adminId = dsl().scheduleAvailableCapability(ADMIN, FEB_1);
        dsl().allocate(projectId, adminId, ADMIN, FEB_1, WHEN);

        Optional<AllocationOutcome> outcome = dsl().allocate(projectId, adminId, ADMIN, FEB_1, WHEN);

        assertThat(outcome).isEmpty();
    }

    @Test
    void thereAreNoMissingDemandsWhenAllAllocated() {
        ProjectAllocationsId projectId = ProjectAllocationsId.newOne();
        Demands demands = Demands.of(new Demand(permission("ADMIN"), FEB_1), new Demand(skill("JAVA"), FEB_1));
        dsl().scheduleDemands(projectId, demands, WHEN);
        AllocatableCapabilityId adminId = dsl().scheduleAvailableCapability(ADMIN, FEB_1);
        AllocatableCapabilityId javaId = dsl().scheduleAvailableCapability(JAVA, FEB_1);
        dsl().allocate(projectId, adminId, ADMIN, FEB_1, WHEN);

        Optional<AllocationOutcome> outcome = dsl().allocate(projectId, javaId, JAVA, FEB_1, WHEN);

        assertThat(outcome).isPresent();
        assertThat(outcome.get().missingDemandsAfter()).isEqualTo(Demands.none());
        outcome.get().occurredAt().ifPresent(occurredAt -> assertThat(occurredAt).isEqualTo(WHEN));
    }

    @Test
    void missingDemandsArePresentWhenAllocatingForDifferentThanDemandedSlot() {
        ProjectAllocationsId projectId = ProjectAllocationsId.newOne();
        Demands demands = Demands.of(new Demand(permission("ADMIN"), FEB_1), new Demand(skill("JAVA"), FEB_1));
        dsl().scheduleDemands(projectId, demands, WHEN);
        AllocatableCapabilityId adminId = dsl().scheduleAvailableCapability(ADMIN, FEB_1);
        AllocatableCapabilityId javaId = dsl().scheduleAvailableCapability(JAVA, FEB_2);
        dsl().allocate(projectId, adminId, ADMIN, FEB_1, WHEN);

        Optional<AllocationOutcome> outcome = dsl().allocate(projectId, javaId, JAVA, FEB_2, WHEN);

        assertThat(outcome).isPresent();
        assertThat(dsl().projectSnapshot(projectId).missingDemands()).isEqualTo(Demands.of(new Demand(skill("JAVA"), FEB_1)));
        assertThat(outcome.get().missingDemandsAfter()).isEqualTo(Demands.of(new Demand(skill("JAVA"), FEB_1)));
        outcome.get().occurredAt().ifPresent(occurredAt -> assertThat(occurredAt).isEqualTo(WHEN));
    }

    // ----- release -----

    @Test
    void canRelease() {
        ProjectAllocationsId projectId = ProjectAllocationsId.newOne();
        dsl().scheduleDemands(projectId, Demands.none(), WHEN);
        AllocatableCapabilityId adminId = dsl().scheduleAvailableCapability(ADMIN, FEB_1);
        Optional<AllocationOutcome> allocated = dsl().allocate(projectId, adminId, ADMIN, FEB_1, WHEN);
        assertThat(allocated).isPresent();

        Optional<ReleaseOutcome> outcome = dsl().release(projectId, adminId, FEB_1, WHEN);

        assertThat(outcome).isPresent();
        assertThat(outcome.get().missingDemandsAfter()).isEqualTo(Demands.none());
        outcome.get().occurredAt().ifPresent(occurredAt -> assertThat(occurredAt).isEqualTo(WHEN));
    }

    @Test
    void releasingHasNoEffectWhenCapabilityWasNotAllocated() {
        ProjectAllocationsId projectId = ProjectAllocationsId.newOne();
        dsl().scheduleDemands(projectId, Demands.none(), WHEN);

        Optional<ReleaseOutcome> outcome = dsl().release(projectId, AllocatableCapabilityId.newOne(), FEB_1, WHEN);

        assertThat(outcome).isEmpty();
    }

    @Test
    void missingDemandsArePresentAfterReleasingSomeOfAllocatedCapabilities() {
        ProjectAllocationsId projectId = ProjectAllocationsId.newOne();
        Demand demandForJava = new Demand(skill("JAVA"), FEB_1);
        Demand demandForAdmin = new Demand(permission("ADMIN"), FEB_1);
        dsl().scheduleDemands(projectId, Demands.of(demandForAdmin, demandForJava), WHEN);
        AllocatableCapabilityId adminId = dsl().scheduleAvailableCapability(ADMIN, FEB_1);
        AllocatableCapabilityId javaId = dsl().scheduleAvailableCapability(JAVA, FEB_1);
        Optional<AllocationOutcome> allocatedAdmin = dsl().allocate(projectId, adminId, ADMIN, FEB_1, WHEN);
        dsl().allocate(projectId, javaId, JAVA, FEB_1, WHEN);
        assertThat(allocatedAdmin).isPresent();

        Optional<ReleaseOutcome> outcome = dsl().release(projectId, adminId, FEB_1, WHEN);

        assertThat(outcome).isPresent();
        assertThat(outcome.get().missingDemandsAfter()).isEqualTo(Demands.of(demandForAdmin));
        outcome.get().occurredAt().ifPresent(occurredAt -> assertThat(occurredAt).isEqualTo(WHEN));
    }

    @Test
    void releasingHasNoEffectWhenReleasingSlotNotWithinAllocatedSlot() {
        ProjectAllocationsId projectId = ProjectAllocationsId.newOne();
        dsl().scheduleDemands(projectId, Demands.none(), WHEN);
        AllocatableCapabilityId adminId = dsl().scheduleAvailableCapability(ADMIN, FEB_1);
        Optional<AllocationOutcome> allocatedAdmin = dsl().allocate(projectId, adminId, ADMIN, FEB_1, WHEN);
        assertThat(allocatedAdmin).isPresent();

        Optional<ReleaseOutcome> outcome = dsl().release(projectId, adminId, FEB_2, WHEN);

        assertThat(outcome).isEmpty();
    }

    @Test
    void releasingSmallPartOfSlotLeavesTheRest() {
        ProjectAllocationsId projectId = ProjectAllocationsId.newOne();
        dsl().scheduleDemands(projectId, Demands.none(), WHEN);
        AllocatableCapabilityId adminId = dsl().scheduleAvailableCapability(ADMIN, FEB_1);
        Optional<AllocationOutcome> allocatedAdmin = dsl().allocate(projectId, adminId, ADMIN, FEB_1, WHEN);
        assertThat(allocatedAdmin).isPresent();

        TimeSlot fifteenMinutesIn1Feb =
                new TimeSlot(FEB_1.from().plus(1, ChronoUnit.HOURS), FEB_1.from().plus(2, ChronoUnit.HOURS));
        TimeSlot oneHourBefore = new TimeSlot(FEB_1.from(), FEB_1.from().plus(1, ChronoUnit.HOURS));
        TimeSlot theRest = new TimeSlot(FEB_1.from().plus(2, ChronoUnit.HOURS), FEB_1.to());

        Optional<ReleaseOutcome> outcome = dsl().release(projectId, adminId, fifteenMinutesIn1Feb, WHEN);

        assertThat(outcome).isPresent();
        assertThat(outcome.get().missingDemandsAfter()).isEqualTo(Demands.none());
        outcome.get().occurredAt().ifPresent(occurredAt -> assertThat(occurredAt).isEqualTo(WHEN));
        assertThat(dsl().projectSnapshot(projectId).allocations().all()).containsExactlyInAnyOrder(
                new AllocatedCapability(adminId, ADMIN, oneHourBefore),
                new AllocatedCapability(adminId, ADMIN, theRest));
    }

    // ----- changeProjectSlot / createProject -----

    @Test
    void canChangeProjectDates() {
        ProjectAllocationsId projectId = dsl().createProject(JANUARY, Demands.none()).projectId();

        SlotDefined outcome = dsl().changeProjectSlot(projectId, FEBRUARY, WHEN);

        assertThat(outcome).isNotNull();
        assertThat(outcome.slot()).isEqualTo(FEBRUARY);
        assertThat(outcome.occurredAt()).isEqualTo(WHEN);
    }

    @Test
    void canCreateNewProject() {
        Demand demand = new Demand(skill("JAVA"), JAN);
        Demands demands = Demands.of(demand);

        ProjectCreated created = dsl().createProject(JAN, demands);

        ProjectSnapshot snapshot = dsl().projectSnapshot(created.projectId());
        assertThat(snapshot.demands()).isEqualTo(demands);
        assertThat(snapshot.timeSlot()).isEqualTo(JAN);
        assertThat(created.slot()).isEqualTo(JAN);
        assertThat(created.occurredAt()).isNotNull();
    }

    @Test
    void canRedefineProjectDeadline() {
        Demand demand = new Demand(skill("JAVA"), JAN);
        Demands demands = Demands.of(demand);
        ProjectAllocationsId projectId = dsl().createProject(JAN, demands).projectId();

        SlotDefined outcome = dsl().changeProjectSlot(projectId, FEB, WHEN);

        ProjectSnapshot snapshot = dsl().projectSnapshot(projectId);
        assertThat(snapshot.timeSlot()).isEqualTo(FEB);
        assertThat(outcome.slot()).isEqualTo(FEB);
        assertThat(outcome.occurredAt()).isNotNull();
    }

    // ----- scheduleDemands -----

    @Test
    void canChangeDemands() {
        ProjectAllocationsId projectId = ProjectAllocationsId.newOne();
        Demands demands = Demands.of(new Demand(permission("ADMIN"), FEB_1), new Demand(skill("JAVA"), FEB_1));
        dsl().scheduleDemands(projectId, demands, WHEN);
        AllocatableCapabilityId adminId = dsl().scheduleAvailableCapability(ADMIN, FEB_1);
        dsl().allocate(projectId, adminId, ADMIN, FEB_1, WHEN);

        DemandsScheduled outcome = dsl().scheduleDemands(projectId, Demands.of(new Demand(skill("PYTHON"), FEB_1)), WHEN);

        assertThat(dsl().projectSnapshot(projectId).missingDemands()).isEqualTo(Demands.allInSameTimeSlot(FEB_1, skill("JAVA"), skill("PYTHON")));
        assertThat(outcome.missingDemandsAfter()).isEqualTo(Demands.allInSameTimeSlot(FEB_1, skill("JAVA"), skill("PYTHON")));
        outcome.occurredAt().ifPresent(occurredAt -> assertThat(occurredAt).isEqualTo(WHEN));
    }

    @Test
    void canScheduleProjectDemands() {
        ProjectAllocationsId projectId = ProjectAllocationsId.newOne();
        Demand javaDemand = new Demand(skill("JAVA"), TimeSlot.createDailyTimeSlotAtUTC(2022, 2, 2));

        DemandsScheduled outcome = dsl().scheduleDemands(projectId, Demands.of(javaDemand), WHEN);

        assertThat(outcome.demandsAfter().all()).containsOnlyOnceElementsOf(Demands.of(javaDemand).all());
        ProjectSnapshot snapshot = dsl().projectSnapshot(projectId);
        assertThat(snapshot.allocations().all()).hasSize(0);
        assertThat(snapshot.demands().all()).containsOnlyOnceElementsOf(Demands.of(javaDemand).all());
    }
}
