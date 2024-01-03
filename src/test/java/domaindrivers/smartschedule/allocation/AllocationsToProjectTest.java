package domaindrivers.smartschedule.allocation;

import domaindrivers.smartschedule.shared.capability.Capability;
import domaindrivers.smartschedule.shared.timeslot.TimeSlot;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static domaindrivers.smartschedule.shared.capability.Capability.permission;
import static org.assertj.core.api.Assertions.assertThat;

class AllocationsToProjectTest {

    static final Instant WHEN = Instant.MIN;
    static final ProjectAllocationsId PROJECT_ID = ProjectAllocationsId.newOne();
    static final ResourceId ADMIN_ID = ResourceId.newOne();
    static final TimeSlot FEB_1 = TimeSlot.createDailyTimeSlotAtUTC(2020, 2, 1);
    static final TimeSlot FEB_2 = TimeSlot.createDailyTimeSlotAtUTC(2020, 2, 2);
    static final TimeSlot JANUARY = TimeSlot.createMonthlyTimeSlotAtUTC(2020, 1);
    static final TimeSlot FEBRUARY = TimeSlot.createMonthlyTimeSlotAtUTC(2020, 2);

    @Test
    void canAllocate() {
        //given
        ProjectAllocations allocations = ProjectAllocations.empty(PROJECT_ID);

        //when
        Optional<CapabilitiesAllocated> event = allocations.allocate(ADMIN_ID, permission("ADMIN"), FEB_1, WHEN);

        //then
        assertThat(event).isPresent();
        CapabilitiesAllocated capabilitiesAllocated = event.get();
        assertThat(event).contains(new CapabilitiesAllocated(capabilitiesAllocated.eventId(), capabilitiesAllocated.allocatedCapabilityId(), PROJECT_ID, Demands.none(), WHEN));
    }

    @Test
    void cantAllocateWhenRequestedTimeSlotNotWithingProjectSlot() {
        //given
        ProjectAllocations allocations = new ProjectAllocations(PROJECT_ID, Allocations.none(), Demands.none(), JANUARY);

        //when
        Optional<CapabilitiesAllocated> event = allocations.allocate(ADMIN_ID, permission("ADMIN"), FEB_1, WHEN);

        //then
        assertThat(event).isEmpty();
    }

    @Test
    void allocatingHasNoEffectWhenCapabilityAlreadyAllocated() {
        //given
        ProjectAllocations allocations = ProjectAllocations.empty(PROJECT_ID);

        //and
        allocations.allocate(ADMIN_ID, permission("ADMIN"), FEB_1, WHEN);

        //when
        Optional<CapabilitiesAllocated> event = allocations.allocate(ADMIN_ID, permission("ADMIN"), FEB_1, WHEN);

        //then
        assertThat(event).isEmpty();
    }

    @Test
    void thereAreNoMissingDemandsWhenAllAllocated() {
        //given
        Demands demands = Demands.of(new Demand(permission("ADMIN"), FEB_1), new Demand(Capability.skill("JAVA"), FEB_1));
        //and
        ProjectAllocations allocations = ProjectAllocations.withDemands(PROJECT_ID, demands);
        //and
        allocations.allocate(ADMIN_ID, permission("ADMIN"), FEB_1, WHEN);
        //when
        Optional<CapabilitiesAllocated> event = allocations.allocate(ADMIN_ID, Capability.skill("JAVA"), FEB_1, WHEN);
        //then
        assertThat(event).isPresent();
        CapabilitiesAllocated capabilitiesAllocated = event.get();
        assertThat(event).contains(new CapabilitiesAllocated(capabilitiesAllocated.eventId(), capabilitiesAllocated.allocatedCapabilityId(), PROJECT_ID, Demands.none(), WHEN));
    }

    @Test
    void missingDemandsArePresentWhenAllocatingForDifferentThanDemandedSlot() {
        //given
        Demands demands = Demands.of(new Demand(permission("ADMIN"), FEB_1), new Demand(Capability.skill("JAVA"), FEB_1));
        //and
        ProjectAllocations allocations = ProjectAllocations.withDemands(PROJECT_ID, demands);
        //and
        allocations.allocate(ADMIN_ID, permission("ADMIN"), FEB_1, WHEN);
        //when
        Optional<CapabilitiesAllocated> event = allocations.allocate(ADMIN_ID, Capability.skill("JAVA"), FEB_2, WHEN);
        //then
        assertThat(event).isPresent();
        assertThat(allocations.missingDemands()).isEqualTo(Demands.of(new Demand(Capability.skill("JAVA"), FEB_1)));
        CapabilitiesAllocated capabilitiesAllocated = event.get();
        assertThat(event).contains(new CapabilitiesAllocated(capabilitiesAllocated.eventId(), capabilitiesAllocated.allocatedCapabilityId(), PROJECT_ID, Demands.of(new Demand(Capability.skill("JAVA"), FEB_1)), WHEN));
    }

    @Test
    void canRelease() {
        //given
        ProjectAllocations allocations = ProjectAllocations.empty(PROJECT_ID);
        //and
        Optional<CapabilitiesAllocated> allocatedAdmin = allocations.allocate(ADMIN_ID, permission("ADMIN"), FEB_1, WHEN);
        //when
        Optional<CapabilityReleased> event = allocations.release(allocatedAdmin.get().allocatedCapabilityId(), FEB_1, WHEN);

        //then
        assertThat(event).isPresent();
        assertThat(event).contains(new CapabilityReleased(event.get().eventId(), PROJECT_ID, Demands.none(), WHEN));
    }

    @Test
    void releasingHasNoEffectWhenCapabilityWasNotAllocated() {
        //given
        ProjectAllocations allocations = ProjectAllocations.empty(PROJECT_ID);

        //when
        Optional<CapabilityReleased> event = allocations.release(UUID.randomUUID(), FEB_1, WHEN);

        //then
        assertThat(event).isEmpty();
    }

    @Test
    void missingDemandsArePresentAfterReleasingSomeOfAllocatedCapabilities() {
        //given
        Demand demandForJava = new Demand(Capability.skill("JAVA"), FEB_1);
        Demand demandForAdmin = new Demand(permission("ADMIN"), FEB_1);
        ProjectAllocations allocations = ProjectAllocations.withDemands(PROJECT_ID, Demands.of(demandForAdmin, demandForJava));
        //and
        Optional<CapabilitiesAllocated> allocatedAdmin = allocations.allocate(ADMIN_ID, permission("ADMIN"), FEB_1, WHEN);
        allocations.allocate(ADMIN_ID, Capability.skill("JAVA"), FEB_1, WHEN);
        //when
        Optional<CapabilityReleased> event = allocations.release(allocatedAdmin.get().allocatedCapabilityId(), FEB_1, WHEN);

        //then
        assertThat(event).isPresent();
        assertThat(event).contains(new CapabilityReleased(event.get().eventId(), PROJECT_ID, Demands.of(demandForAdmin), WHEN));
    }

    @Test
    void releasingHasNoEffectWhenReleasingSlotNotWithinAllocatedSlot() {
        //given
        ProjectAllocations allocations = ProjectAllocations.empty(PROJECT_ID);
        //and
        Optional<CapabilitiesAllocated> allocatedAdmin = allocations.allocate(ADMIN_ID, permission("ADMIN"), FEB_1, WHEN);

        //when
        Optional<CapabilityReleased> event = allocations.release(allocatedAdmin.get().allocatedCapabilityId(), FEB_2, WHEN);

        //then
        assertThat(event).isEmpty();
    }

    @Test
    void releasingSmallPartOfSlotLeavesTheRest() {
        //given
        ProjectAllocations allocations = ProjectAllocations.empty(PROJECT_ID);
        //and
        Optional<CapabilitiesAllocated> allocatedAdmin = allocations.allocate(ADMIN_ID, permission("ADMIN"), FEB_1, WHEN);

        //when
        TimeSlot fifteenMinutesIn1Feb =
                new TimeSlot(FEB_1.from().plus(1, ChronoUnit.HOURS), FEB_1.from().plus(2, ChronoUnit.HOURS));
        TimeSlot oneHourBefore = new TimeSlot(FEB_1.from(), FEB_1.from().plus(1, ChronoUnit.HOURS));
        TimeSlot theRest = new TimeSlot(FEB_1.from().plus(2, ChronoUnit.HOURS), FEB_1.to());

        //when
        Optional<CapabilityReleased> event = allocations.release(allocatedAdmin.get().allocatedCapabilityId(), fifteenMinutesIn1Feb, WHEN);

        //then
        assertThat(event).contains(new CapabilityReleased(event.get().eventId(), PROJECT_ID, Demands.none(), WHEN));
        assertThat(allocations.allocations().all()).containsExactlyInAnyOrder(
                new AllocatedCapability(ADMIN_ID.id(), permission("ADMIN"), oneHourBefore),
                new AllocatedCapability(ADMIN_ID.id(), permission("ADMIN"), theRest));
    }

    @Test
    void canChangeDemands() {
        //given
        Demands demands = Demands.of(new Demand(permission("ADMIN"), FEB_1), new Demand(Capability.skill("JAVA"), FEB_1));
        //and
        ProjectAllocations allocations = ProjectAllocations.withDemands(PROJECT_ID, demands);
        //and
        allocations.allocate(ADMIN_ID, permission("ADMIN"), FEB_1, WHEN);
        //when
        Optional<ProjectAllocationsDemandsScheduled> event = allocations.addDemands(Demands.of(new Demand(Capability.skill("PYTHON"), FEB_1)), WHEN);
        //then
        assertThat(allocations.missingDemands()).isEqualTo(Demands.allInSameTimeSlot(FEB_1, Capability.skill("JAVA"), Capability.skill("PYTHON")));
        assertThat(event).contains(new ProjectAllocationsDemandsScheduled(event.get().uuid(), PROJECT_ID, Demands.allInSameTimeSlot(FEB_1, Capability.skill("JAVA"), Capability.skill("PYTHON")), WHEN));
    }


    @Test
    void canChangeProjectDates() {
        //given
        ProjectAllocations allocations = new ProjectAllocations(PROJECT_ID, Allocations.none(), Demands.none(), JANUARY);

        //when
        Optional<ProjectAllocationScheduled> event = allocations.defineSlot(FEBRUARY, WHEN);

        //then
        assertThat(event).isNotNull();
        assertThat(event).contains(new ProjectAllocationScheduled(event.get().uuid(), PROJECT_ID, FEBRUARY, WHEN));
    }

}